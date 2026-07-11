package nro.models.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import nro.models.data.LocalManager;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services.ItemService;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

public class TopBossHunter {

    private static final int TOP_LIMIT = 100;

    @Getter
    private final List<Player> list = new ArrayList<>();
    private static final TopBossHunter INSTANCE = new TopBossHunter();

    public static TopBossHunter getInstance() {
        return INSTANCE;
    }

    public void load() {
        list.clear();
        Map<Long, Player> playersById = new HashMap<>();

        try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT id, name, gender, head, items_body, event_point "
                + "FROM player "
                + "WHERE event_point > 0 "
                + "ORDER BY event_point DESC, id ASC LIMIT " + TOP_LIMIT)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Player player = extractPlayerFromResultSet(rs);
                    playersById.put(player.id, player);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mergeOnlinePlayers(playersById);
        list.addAll(playersById.values());
        list.sort(Comparator
                .comparingInt((Player player) -> player.event.getEventPoint()).reversed()
                .thenComparingLong(player -> player.id));
        if (list.size() > TOP_LIMIT) {
            list.subList(TOP_LIMIT, list.size()).clear();
        }
    }

    private Player extractPlayerFromResultSet(ResultSet rs) throws SQLException {
        Player player = new Player();

        player.id = rs.getLong("id");
        player.name = rs.getString("name");
        player.gender = rs.getByte("gender");
        player.head = rs.getShort("head");
        player.event.setEventPoint(rs.getInt("event_point"));

        extractItemsBody(rs.getString("items_body"), player);
        ensureBodySlots(player);

        return player;
    }

    private void extractItemsBody(String itemsBody, Player player) {
        Object parsedData = JSONValue.parse(itemsBody);
        if (!(parsedData instanceof JSONArray dataArray)) {
            return;
        }

        for (Object itemDataObject : dataArray) {
            Item item = createItemFromDataObject(itemDataObject);
            player.inventory.itemsBody.add(item);
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
            if (tempId == -1) {
                return ItemService.gI().createItemNull();
            }
            return ItemService.gI().createNewItem(tempId);
        } catch (Exception e) {
            return ItemService.gI().createItemNull();
        }
    }

    private void ensureBodySlots(Player player) {
        while (player.inventory.itemsBody.size() < 10) {
            player.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
    }

    private void mergeOnlinePlayers(Map<Long, Player> playersById) {
        for (Player player : new ArrayList<>(Client.gI().getPlayers())) {
            if (player == null || player.event == null) {
                continue;
            }
            if (player.event.getEventPoint() > 0) {
                playersById.put(player.id, player);
            } else {
                playersById.remove(player.id);
            }
        }
    }
}
