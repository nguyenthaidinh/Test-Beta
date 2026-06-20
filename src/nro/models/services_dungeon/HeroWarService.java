package nro.models.services_dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.consts.ConstItem;
import nro.models.consts.ConstMap;
import nro.models.data.LocalManager;
import nro.models.data.LocalResultSet;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.ItemMapService;
import nro.models.map.service.MapService;
import nro.models.player.Player;
import nro.models.server.Maintenance;
import nro.models.services.InventoryService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.utils.Functions;
import nro.models.utils.Logger;
import nro.models.utils.TimeUtil;
import nro.models.utils.Util;

public class HeroWarService implements Runnable {

    private static final int MAP_ID = ConstMap.VO_DAI_SIEU_CAP;
    private static final int BALL_ID = ConstItem.NGOC_RONG_1_SAO_DEN;
    private static final int BALL_X = 754;
    private static final int BALL_Y = 160;
    private static final int TIME_WIN = 300_000;
    private static final int TIME_CAN_PICK_AFTER_DROP = 5_000;
    private static final int TIME_HOLD_TO_CHANGE_ZONE = 120_000;
    private static final int MINUTE_CAN_PICK = 15;
    private static final int RETURN_MAP_ID = ConstMap.HANH_TINH_KAIO;
    private static final int RETURN_X = 754;
    private static final int RETURN_Y = 336;
    private static final String OPEN_TIME_TEXT = "21h00 đến 22h00";
    private static final int HISTORY_LIMIT = 20;
    private static final int HOLDER_POWER_MULTIPLIER = 10;
    private static final int HOLDER_POWER_COST = 20;
    private static final int[] FRIEND_POWER_MULTIPLIERS = {1, 2, 3, 5};
    private static final int[] FRIEND_POWER_COSTS = {2, 5, 8, 10};

    private static HeroWarService instance;
    private final Set<Long> changingZoneHolders = ConcurrentHashMap.newKeySet();

    public static HeroWarService gI() {
        if (instance == null) {
            instance = new HeroWarService();
        }
        return instance;
    }

    public boolean isHeroWarMap(int mapId) {
        return mapId == MAP_ID;
    }

    public String getFriendPowerMenuText(Player player) {
        String text = "Sức mạnh tình bạn sẽ tăng HP, KI và sức đánh trong Đại chiến Anh Hùng.";
        if (player != null && player.idMark != null && player.idMark.isHoldBlackBall()) {
            text += "\nNgười đang giữ Ngọc Rồng Đen có thể dùng x" + HOLDER_POWER_MULTIPLIER + ".";
        }
        return text;
    }

    public String[] getFriendPowerMenuOptions(Player player) {
        List<String> options = new ArrayList<>();
        for (int multiplier : FRIEND_POWER_MULTIPLIERS) {
            options.add("x" + multiplier + "\n" + getFriendPowerCost(multiplier) + " TV");
        }
        if (player != null && player.idMark != null && player.idMark.isHoldBlackBall()) {
            options.add("x" + HOLDER_POWER_MULTIPLIER + "\n" + getFriendPowerCost(HOLDER_POWER_MULTIPLIER) + " TV");
        }
        options.add("Từ chối");
        return options.toArray(new String[0]);
    }

    public void selectFriendPower(Player player, int select) {
        if (player == null || select < 0 || select >= FRIEND_POWER_MULTIPLIERS.length + 1) {
            return;
        }
        if (select < FRIEND_POWER_MULTIPLIERS.length) {
            useFriendPower(player, FRIEND_POWER_MULTIPLIERS[select]);
            return;
        }
        if (player.idMark != null && player.idMark.isHoldBlackBall()) {
            useFriendPower(player, HOLDER_POWER_MULTIPLIER);
        }
    }

    public void useFriendPower(Player player, int multiplier) {
        if (player == null || player.zone == null || !isHeroWarMap(player.zone.map.mapId)) {
            return;
        }
        if (!isOpen()) {
            Service.gI().sendThongBao(player, "Đã hết giờ Đại chiến Anh Hùng.");
            return;
        }
        if (player.isDie()) {
            Service.gI().sendThongBao(player, "Hãy hồi sinh trước khi dùng Sức mạnh tình bạn.");
            return;
        }
        boolean holder = player.idMark != null && player.idMark.isHoldBlackBall();
        if (!isFriendPowerMultiplier(multiplier) && multiplier != HOLDER_POWER_MULTIPLIER) {
            return;
        }
        if (multiplier == HOLDER_POWER_MULTIPLIER && !holder) {
            Service.gI().sendThongBao(player, "Chỉ người đang giữ Ngọc Rồng Đen mới dùng được x" + HOLDER_POWER_MULTIPLIER + ".");
            return;
        }
        boolean hasBuff = player.effectSkin.xHPKI > 1 || player.effectSkin.xDame > 1;
        if (hasBuff && multiplier != HOLDER_POWER_MULTIPLIER) {
            Service.gI().sendThongBao(player, "Bạn đang có Sức mạnh tình bạn, chết rồi hồi sinh mới có thể x lại.");
            return;
        }
        if (multiplier == HOLDER_POWER_MULTIPLIER
                && player.effectSkin.xHPKI == HOLDER_POWER_MULTIPLIER
                && player.effectSkin.xDame == HOLDER_POWER_MULTIPLIER) {
            Service.gI().sendThongBao(player, "Bạn đang có Sức mạnh tình bạn x" + HOLDER_POWER_MULTIPLIER + " rồi.");
            return;
        }

        int cost = getFriendPowerCost(multiplier);
        Item thoiVang = InventoryService.gI().findItemBag(player, ConstItem.THOI_VANG);
        if (thoiVang == null || thoiVang.quantity < cost) {
            Service.gI().sendThongBao(player, "Cần " + cost + " Thỏi vàng để dùng Sức mạnh tình bạn.");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, cost);
        InventoryService.gI().sendItemBags(player);

        long now = System.currentTimeMillis();
        player.effectSkin.xHPKI = multiplier;
        player.effectSkin.xDame = multiplier;
        player.effectSkin.lastTimeXHPKI = now;
        player.effectSkin.lastTimeXDame = now;
        player.nPoint.calPoint();
        player.nPoint.setHp(player.nPoint.hpMax);
        player.nPoint.setMp(player.nPoint.mpMax);
        Service.gI().point(player);
        PlayerService.gI().sendInfoHpMp(player);
        Service.gI().sendThongBao(player, "Đã kích hoạt Sức mạnh tình bạn x" + multiplier + ".");
    }

    private boolean isFriendPowerMultiplier(int multiplier) {
        for (int value : FRIEND_POWER_MULTIPLIERS) {
            if (value == multiplier) {
                return true;
            }
        }
        return false;
    }

    private int getFriendPowerCost(int multiplier) {
        if (multiplier == HOLDER_POWER_MULTIPLIER) {
            return HOLDER_POWER_COST;
        }
        for (int i = 0; i < FRIEND_POWER_MULTIPLIERS.length; i++) {
            if (FRIEND_POWER_MULTIPLIERS[i] == multiplier) {
                return FRIEND_POWER_COSTS[i];
            }
        }
        return multiplier;
    }

    private boolean isOpen() {
        return TimeUtil.is21H();
    }

    private boolean canPickBallNow() {
        return isOpen() && TimeUtil.getCurrHour() == 21 && TimeUtil.getCurrMin() >= MINUTE_CAN_PICK;
    }

    public boolean canChangeZone(Player player) {
        if (player == null || player.idMark == null || !player.idMark.isHoldBlackBall()) {
            return true;
        }
        long now = System.currentTimeMillis();
        long waitHold = player.idMark.getLastTimeHoldBlackBall() + TIME_HOLD_TO_CHANGE_ZONE - now;
        long waitZone = player.idMark.getLastTimeChangeZone() + TIME_HOLD_TO_CHANGE_ZONE - now;
        long wait = Math.max(waitHold, waitZone);
        if (wait > 0) {
            Service.gI().sendThongBao(player, "Đang giữ Ngọc Rồng Đen, cần đợi "
                    + ((wait + 999) / 1000) + " giây nữa mới được đổi khu.");
            return false;
        }
        return true;
    }

    public void beginChangeZone(Player player) {
        if (player != null && player.idMark != null && player.idMark.isHoldBlackBall()) {
            changingZoneHolders.add(player.id);
        }
    }

    public void endChangeZone(Player player) {
        if (player != null) {
            changingZoneHolders.remove(player.id);
        }
    }

    public void joinFromWhis(Player player) {
        if (!isOpen()) {
            Service.gI().sendThongBao(player, "Đại chiến Anh Hùng chỉ mở cửa từ " + OPEN_TIME_TEXT + ".");
            return;
        }
        Zone zone = MapService.gI().getZoneByMapIDAndZoneID(MAP_ID, 0);
        if (zone == null) {
            Service.gI().sendThongBao(player, "Đại chiến Anh Hùng chưa sẵn sàng.");
            return;
        }
        if (zone.getNumOfPlayers() >= zone.maxPlayer && !player.isAdmin()) {
            Service.gI().sendThongBao(player, "Võ đài đang đầy, hãy quay lại sau.");
            return;
        }
        ChangeMapService.gI().changeMap(player, zone, BALL_X, RETURN_Y);
    }

    public void showWinnerHistory(Player player) {
        if (player == null) {
            return;
        }
        LocalResultSet rs = null;
        try {
            rs = LocalManager.executeQuery(
                    "SELECT player_name, DATE_FORMAT(win_time, '%d/%m/%Y %H:%i') AS win_time_text, hold_seconds "
                    + "FROM hero_war_history ORDER BY win_time DESC LIMIT ?",
                    HISTORY_LIMIT);
            StringBuilder text = new StringBuilder("Bảng xếp hạng Đại chiến Anh Hùng\n");
            int rank = 1;
            while (rs.next()) {
                int holdSeconds = rs.getInt("hold_seconds");
                text.append(rank++).append(". ")
                        .append(rs.getString("player_name"))
                        .append(" - ").append(rs.getString("win_time_text"))
                        .append(" - giữ ").append(TimeUtil.convertTime(holdSeconds)).append("\n");
            }
            if (rank == 1) {
                text.append("Chưa có lịch sử chiến thắng.");
            }
            Service.gI().sendThongBaoOK(player, text.toString().trim());
        } catch (Exception e) {
            Logger.logException(HeroWarService.class, e, "Lỗi tải lịch sử Đại chiến Anh Hùng");
            Service.gI().sendThongBaoOK(player,
                    "Chưa có dữ liệu bảng xếp hạng Đại chiến Anh Hùng.\n"
                    + "Nếu đây là lần đầu dùng, hãy chạy SQL tạo bảng hero_war_history.");
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public void join(Player player) {
        if (player == null || player.zone == null || !isHeroWarMap(player.zone.map.mapId)) {
            return;
        }
        if (!isOpen()) {
            Service.gI().sendThongBao(player, "Đại chiến Anh Hùng chỉ mở cửa từ " + OPEN_TIME_TEXT + ".");
            ChangeMapService.gI().changeMap(player, RETURN_MAP_ID, -1, RETURN_X, RETURN_Y);
            return;
        }
        if (player.cFlag != 8) {
            Service.gI().changeFlag(player, 8);
        }
        PlayerService.gI().sendInfoHpMp(player);
        Service.gI().sendThongBao(player, "Đại chiến Anh Hùng: giữ Ngọc Rồng Đen đủ 5 phút để chiến thắng.");
        ensureBall(player.zone.map);
    }

    public void leave(Player player) {
        if (player == null || player.zone == null || !isHeroWarMap(player.zone.map.mapId)) {
            return;
        }
        dropHeroBall(player);
        resetFriendPower(player);
        if (player.cFlag == 8) {
            Service.gI().changeFlag(player, 0);
        }
    }

    public boolean pickHeroBall(Player player, Item item) {
        if (player == null || item == null || player.zone == null || !isHeroWarMap(player.zone.map.mapId)) {
            return false;
        }
        if (!isOpen()) {
            Service.gI().sendThongBao(player, "Đã hết giờ Đại chiến Anh Hùng.");
            return false;
        }
        if (!canPickBallNow()) {
            Service.gI().sendThongBao(player, "21h15 mới có thể nhặt Ngọc Rồng Đen.");
            return false;
        }
        if (player.zone.finishBlackBallWar) {
            Service.gI().sendThongBao(player, "Trận đấu đã kết thúc.");
            return false;
        }
        if (getHolder(player.zone) != null) {
            Service.gI().sendThongBao(player, "Đã có người đang giữ Ngọc Rồng Đen.");
            return false;
        }
        if (!Util.canDoWithTime(player.zone.lastTimeDropBlackBall, TIME_CAN_PICK_AFTER_DROP)) {
            Service.gI().sendThongBao(player, "Chưa thể nhặt lúc này, hãy đợi "
                    + TimeUtil.getTimeLeft(player.zone.lastTimeDropBlackBall, TIME_CAN_PICK_AFTER_DROP / 1000) + " nữa");
            return false;
        }
        player.idMark.setHoldBlackBall(true);
        player.idMark.setTempIdBlackBallHold(item.template.id);
        player.idMark.setLastTimeHoldBlackBall(System.currentTimeMillis());
        player.idMark.setLastTimeNotifyTimeHoldBlackBall(System.currentTimeMillis());
        Service.gI().sendFlagBag(player);
        forceBlackFlags(player.zone);
        Service.gI().sendThongBao(player.zone.getPlayers(), player.name + " đang giữ Ngọc Rồng Đen!");
        return true;
    }

    public void dropHeroBall(Player player) {
        if (player == null || player.zone == null || !player.idMark.isHoldBlackBall()) {
            return;
        }
        int tempId = player.idMark.getTempIdBlackBallHold();
        player.idMark.setHoldBlackBall(false);
        player.idMark.setTempIdBlackBallHold(-1);
        Service.gI().sendFlagBag(player);

        if (isHeroWarMap(player.zone.map.mapId) && tempId >= 372 && tempId <= 378) {
            ItemMap itemMap = new ItemMap(player.zone, tempId, 1, player.location.x,
                    player.zone.map.yPhysicInTop(player.location.x, player.location.y - 24), -1);
            Service.gI().dropItemMap(itemMap.zone, itemMap);
            player.zone.lastTimeDropBlackBall = System.currentTimeMillis();
            Service.gI().sendThongBao(player.zone.getPlayers(), player.name + " đã làm rơi Ngọc Rồng Đen!");
        }
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long start = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - start), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        Map map = MapService.gI().getMapById(MAP_ID);
        if (map == null) {
            return;
        }
        if (!isOpen()) {
            Player holder = getHolder(map);
            if (holder != null) {
                win(holder);
            } else if (!changingZoneHolders.isEmpty()) {
                return;
            } else {
                closeNoWinner(map);
            }
            return;
        }
        if (!canPickBallNow()) {
            for (Zone zone : map.zones) {
                if (!zone.getPlayers().isEmpty()) {
                    forceBlackFlags(zone);
                }
                clearBlackBalls(zone);
            }
            return;
        }
        for (Zone zone : map.zones) {
            forceBlackFlags(zone);
        }
        Player holder = getHolder(map);
        if (holder == null) {
            if (changingZoneHolders.isEmpty()) {
                ensureBall(map);
            }
            return;
        }
        clearBlackBalls(map);
        updateHolder(holder);
    }

    private void updateHolder(Player holder) {
        if (holder == null || holder.zone == null || !isHeroWarMap(holder.zone.map.mapId)) {
            return;
        }
        if (holder.isDie()) {
            dropHeroBall(holder);
            return;
        }
        if (Util.canDoWithTime(holder.idMark.getLastTimeHoldBlackBall(), TIME_WIN)) {
            win(holder);
        } else if (Util.canDoWithTime(holder.idMark.getLastTimeNotifyTimeHoldBlackBall(), 10_000)) {
            Service.gI().sendThongBao(holder, "Cố giữ ngọc thêm "
                    + TimeUtil.getSecondLeft(holder.idMark.getLastTimeHoldBlackBall(), TIME_WIN / 1000)
                    + " giây nữa sẽ chiến thắng.");
            holder.idMark.setLastTimeNotifyTimeHoldBlackBall(System.currentTimeMillis());
        }
    }

    private void ensureBall(Map map) {
        if (!canPickBallNow() || map == null || map.zones.isEmpty() || !changingZoneHolders.isEmpty()
                || getHolder(map) != null) {
            return;
        }
        List<ItemMap> balls = getBlackBalls(map);
        if (balls.size() == 1) {
            return;
        }
        if (!balls.isEmpty()) {
            clearBlackBalls(map);
        }
        Zone zone = map.zones.get(Util.nextInt(map.zones.size()));
        ItemMap itemMap = new ItemMap(zone, BALL_ID, 1, BALL_X, zone.map.yPhysicInTop(BALL_X, BALL_Y), -1);
        Service.gI().dropItemMap(zone, itemMap);
    }

    private void forceBlackFlags(Zone zone) {
        for (Player player : zone.getPlayers()) {
            if (player != null && player.cFlag != 8) {
                Service.gI().changeFlag(player, 8);
            }
        }
    }

    private Player getHolder(Zone zone) {
        for (Player player : zone.getPlayers()) {
            if (player != null && player.idMark != null && player.idMark.isHoldBlackBall()) {
                return player;
            }
        }
        return null;
    }

    private Player getHolder(Map map) {
        for (Zone zone : map.zones) {
            Player holder = getHolder(zone);
            if (holder != null) {
                return holder;
            }
        }
        return null;
    }

    private boolean hasBlackBall(Zone zone) {
        for (ItemMap item : zone.items) {
            if (item != null && item.itemTemplate != null && ItemMapService.gI().isBlackBall(item.itemTemplate.id)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBlackBall(Map map) {
        for (Zone zone : map.zones) {
            if (hasBlackBall(zone)) {
                return true;
            }
        }
        return false;
    }

    private List<ItemMap> getBlackBalls(Map map) {
        List<ItemMap> balls = new ArrayList<>();
        for (Zone zone : map.zones) {
            for (ItemMap item : zone.items) {
                if (item != null && item.itemTemplate != null && ItemMapService.gI().isBlackBall(item.itemTemplate.id)) {
                    balls.add(item);
                }
            }
        }
        return balls;
    }

    private void clearBlackBalls(Zone zone) {
        List<ItemMap> items = new ArrayList<>(zone.items);
        for (ItemMap item : items) {
            if (item != null && item.itemTemplate != null && ItemMapService.gI().isBlackBall(item.itemTemplate.id)) {
                ItemMapService.gI().removeItemMapAndSendClient(item);
            }
        }
    }

    private void clearBlackBalls(Map map) {
        for (Zone zone : map.zones) {
            clearBlackBalls(zone);
        }
    }

    private List<Player> getPlayers(Map map) {
        List<Player> players = new ArrayList<>();
        for (Zone zone : map.zones) {
            players.addAll(zone.getPlayers());
        }
        return players;
    }

    private void resetFriendPower(Player player) {
        if (player == null || player.effectSkin == null) {
            return;
        }
        boolean changed = false;
        if (player.effectSkin.xHPKI > 1) {
            player.effectSkin.xHPKI = 1;
            changed = true;
        }
        if (player.effectSkin.xDame > 1) {
            player.effectSkin.xDame = 1;
            changed = true;
        }
        if (changed) {
            Service.gI().point(player);
        }
    }

    private void closeNoWinner(Zone zone) {
        zone.finishBlackBallWar = true;
        clearBlackBalls(zone);
        Service.gI().sendThongBao(zone.getPlayers(), "Đã hết giờ Đại chiến Anh Hùng, không có người giữ Ngọc Rồng Đen.");
        List<Player> players = new ArrayList<>(zone.getPlayers());
        for (Player player : players) {
            if (player == null || player.zone == null || !zone.equals(player.zone)) {
                continue;
            }
            player.idMark.setHoldBlackBall(false);
            player.idMark.setTempIdBlackBallHold(-1);
            Service.gI().sendFlagBag(player);
            if (player.cFlag == 8) {
                Service.gI().changeFlag(player, 0);
            }
            if (player.isDie()) {
                Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
            }
            ChangeMapService.gI().changeMap(player, RETURN_MAP_ID, -1, RETURN_X, RETURN_Y);
        }
        zone.finishBlackBallWar = false;
    }

    private void closeNoWinner(Map map) {
        clearBlackBalls(map);
        List<Player> players = new ArrayList<>(getPlayers(map));
        if (players.isEmpty()) {
            return;
        }
        Service.gI().sendThongBao(players, "Đã hết giờ Đại chiến Anh Hùng, không có người giữ Ngọc Rồng Đen.");
        for (Player player : players) {
            if (player == null || player.zone == null || !isHeroWarMap(player.zone.map.mapId)) {
                continue;
            }
            player.idMark.setHoldBlackBall(false);
            player.idMark.setTempIdBlackBallHold(-1);
            Service.gI().sendFlagBag(player);
            if (player.cFlag == 8) {
                Service.gI().changeFlag(player, 0);
            }
            if (player.isDie()) {
                Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
            }
            ChangeMapService.gI().changeMap(player, RETURN_MAP_ID, -1, RETURN_X, RETURN_Y);
        }
    }

    private void win(Player winner) {
        Map map = winner.zone.map;
        for (Zone zone : map.zones) {
            zone.finishBlackBallWar = true;
        }
        winner.idMark.setHoldBlackBall(false);
        winner.idMark.setTempIdBlackBallHold(-1);
        Service.gI().sendFlagBag(winner);
        clearBlackBalls(map);
        saveWinner(winner);

        Service.gI().sendThongBaoAllPlayer("Chúc mừng người chơi " + winner.name
                + " đã giành chiến thắng Đại chiến Anh Hùng vô cùng out trình!");
        List<Player> players = new ArrayList<>(getPlayers(map));
        Service.gI().sendThongBao(players, winner.name + " đã chiến thắng Đại chiến Anh Hùng!");
        for (Player player : players) {
            if (player == null || player.zone == null || !isHeroWarMap(player.zone.map.mapId)) {
                continue;
            }
            player.idMark.setHoldBlackBall(false);
            player.idMark.setTempIdBlackBallHold(-1);
            Service.gI().sendFlagBag(player);
            if (player.cFlag == 8) {
                Service.gI().changeFlag(player, 0);
            }
            if (player.isDie()) {
                Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
            }
            ChangeMapService.gI().changeMap(player, RETURN_MAP_ID, -1, RETURN_X, RETURN_Y);
        }
        for (Zone zone : map.zones) {
            zone.finishBlackBallWar = false;
        }
    }

    private void saveWinner(Player winner) {
        if (winner == null) {
            return;
        }
        try {
            LocalManager.executeUpdate(
                    "INSERT INTO hero_war_history (player_id, player_name, win_time, hold_seconds) VALUES (?, ?, ?, ?)",
                    winner.id, winner.name, TimeUtil.getTimeNow("yyyy-MM-dd HH:mm:ss"), getHoldSeconds(winner));
        } catch (Exception e) {
            Logger.logException(HeroWarService.class, e, "Lỗi lưu lịch sử Đại chiến Anh Hùng");
        }
    }

    private int getHoldSeconds(Player winner) {
        if (winner == null || winner.idMark == null || winner.idMark.getLastTimeHoldBlackBall() <= 0) {
            return TIME_WIN / 1000;
        }
        long holdSeconds = (System.currentTimeMillis() - winner.idMark.getLastTimeHoldBlackBall()) / 1000;
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, holdSeconds));
    }
}
