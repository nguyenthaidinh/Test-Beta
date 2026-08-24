package nro.models.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.database.GhostHuntLeaderboardDAO;
import nro.models.database.GhostHuntLeaderboardDAO.Row;
import nro.models.database.GhostHuntLeaderboardDAO.Standing;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services.ItemService;
import nro.models.utils.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/** Dữ liệu hiển thị và quyền riêng tư của BXH săn Hồn Ma. */
public final class TopGhostHunter {

    public static final int TOP_LIMIT = 100;

    private static final TopGhostHunter INSTANCE = new TopGhostHunter();

    private final Set<Long> adminsViewingDetails = ConcurrentHashMap.newKeySet();

    private TopGhostHunter() {
    }

    public static TopGhostHunter getInstance() {
        return INSTANCE;
    }

    public synchronized boolean recordKill(Player player) {
        if (player == null || !player.isPl() || player.isBot) {
            return false;
        }
        try {
            GhostHuntLeaderboardDAO.addPoint(player.id, 1);
            return true;
        } catch (Exception e) {
            Logger.logException(TopGhostHunter.class, e);
            return false;
        }
    }

    public List<Entry> loadTop() {
        List<Entry> entries = new ArrayList<>();
        try {
            for (Row row : GhostHuntLeaderboardDAO.loadTop(TOP_LIMIT)) {
                entries.add(createEntry(row));
            }
        } catch (Exception e) {
            Logger.logException(TopGhostHunter.class, e);
        }
        return entries;
    }

    public Standing getStanding(long playerId) {
        try {
            return GhostHuntLeaderboardDAO.getStanding(playerId);
        } catch (Exception e) {
            Logger.logException(TopGhostHunter.class, e);
            return new Standing(0, -1);
        }
    }

    public synchronized int clearAll() throws Exception {
        return GhostHuntLeaderboardDAO.clearAll();
    }

    public boolean isAdminDetailsVisible(long adminId) {
        return adminsViewingDetails.contains(adminId);
    }

    public boolean toggleAdminDetailsVisible(long adminId) {
        if (adminsViewingDetails.remove(adminId)) {
            return false;
        }
        adminsViewingDetails.add(adminId);
        return true;
    }

    private Entry createEntry(Row row) {
        Player onlinePlayer = Client.gI().getPlayer(row.playerId());
        if (onlinePlayer != null) {
            return new Entry(row.playerId(), row.point(), onlinePlayer.name,
                    onlinePlayer.getHead(), onlinePlayer.getBody(), onlinePlayer.getLeg());
        }

        Player displayPlayer = new Player();
        displayPlayer.id = row.playerId();
        displayPlayer.name = row.name();
        displayPlayer.gender = row.gender();
        displayPlayer.head = row.head();
        extractItemsBody(row.itemsBody(), displayPlayer);
        ensureBodySlots(displayPlayer);
        return new Entry(row.playerId(), row.point(), row.name(),
                displayPlayer.getHead(), displayPlayer.getBody(), displayPlayer.getLeg());
    }

    private void extractItemsBody(String itemsBody, Player player) {
        Object parsedData = JSONValue.parse(itemsBody);
        if (!(parsedData instanceof JSONArray dataArray)) {
            return;
        }
        for (Object itemDataObject : dataArray) {
            player.inventory.itemsBody.add(createItemFromDataObject(itemDataObject));
        }
    }

    private Item createItemFromDataObject(Object itemData) {
        try {
            if (itemData instanceof String str) {
                itemData = JSONValue.parse(str);
            }
            if (!(itemData instanceof JSONArray dataItem) || dataItem.isEmpty()) {
                return ItemService.gI().createItemNull();
            }
            short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
            return tempId == -1 ? ItemService.gI().createItemNull() : ItemService.gI().createNewItem(tempId);
        } catch (Exception e) {
            return ItemService.gI().createItemNull();
        }
    }

    private void ensureBodySlots(Player player) {
        while (player.inventory.itemsBody.size() < 10) {
            player.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
    }

    public record Entry(long playerId, long point, String name, short head, short body, short leg) {
    }
}
