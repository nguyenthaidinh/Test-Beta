package nro.models.services_dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.boss.BossID;
import nro.models.boss.doanh_trai.NinjaAoTim;
import nro.models.boss.doanh_trai.RobotVeSi;
import nro.models.boss.doanh_trai.TrungUyThep;
import nro.models.boss.doanh_trai.TrungUyTrang;
import nro.models.boss.doanh_trai.TrungUyXanhLo;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstItem;
import nro.models.consts.ConstMap;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.map.service.NpcService;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.server.Maintenance;
import nro.models.server.Manager;
import nro.models.server.ServerManager;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.ItemTimeService;
import nro.models.services.Service;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.utils.Functions;
import nro.models.utils.Util;

public class AncientCastleService implements Runnable {

    private static final int DAILY_LIMIT = 2;
    private static final int STAGE_1_COMBO_COUNT = 4;
    private static final int FIGHT_COST = 10;
    private static final int CLONE_MULTIPLIER = 50;
    private static final long TIME_LIMIT = 90L * 60L * 1000L;
    private static final int JOIN_X = 80;
    private static final int MAP2_X = 120;
    private static final int MAP3_X = 120;
    private static final int TEAM_DISTANCE = 700;
    private static final int FINAL_BOSS_DAME = 5_000_000;
    private static final int FINAL_BOSS_BASE_HP = 50_000_000;
    private static final long CLEAR_RETURN_DELAY = 30_000L;
    private static final int FIRST_CLEAR_REWARD_THOI_VANG = 300;
    private static final int NEXT_CLEAR_REWARD_THOI_VANG = 200;
    private static final short[] HUY_DIET_IDS = {650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662};
    private static final int[] DRAGON_BALL_IDS = {
        ConstItem.NGOC_RONG_1_SAO, ConstItem.NGOC_RONG_2_SAO, ConstItem.NGOC_RONG_3_SAO,
        ConstItem.NGOC_RONG_4_SAO, ConstItem.NGOC_RONG_5_SAO, ConstItem.NGOC_RONG_6_SAO,
        ConstItem.NGOC_RONG_7_SAO
    };

    private static AncientCastleService instance;

    private final Map<Long, CastleRun> activeRuns = new ConcurrentHashMap<>();
    private final Map<Long, DailyCounter> dailyCounters = new ConcurrentHashMap<>();
    private final Map<Long, DailyCounter> dailyClearCounters = new ConcurrentHashMap<>();
    private final List<CastleRun> runs = new ArrayList<>();
    private int nextRunId = 1;
    private boolean updaterStarted;

    public static AncientCastleService gI() {
        if (instance == null) {
            instance = new AncientCastleService();
        }
        return instance;
    }

    public synchronized void startOrRejoin(Player player) {
        if (player == null || player.zone == null) {
            return;
        }
        CastleRun currentRun = activeRuns.get(player.id);
        if (currentRun != null) {
            rejoin(player, currentRun);
            return;
        }
        List<Player> members = collectMembers(player);
        if (members.isEmpty()) {
            Service.gI().sendThongBao(player, "Không tìm thấy đồng đội gần Nồi Bánh.");
            return;
        }
        nro.models.map.Map map1 = MapService.gI().getMapById(ConstMap.THANH_CO_1);
        nro.models.map.Map map2 = MapService.gI().getMapById(ConstMap.THANH_CO_2);
        nro.models.map.Map map3 = MapService.gI().getMapById(ConstMap.DAU_TRUONG_THANH_CO);
        if (map1 == null || map2 == null || map3 == null) {
            Service.gI().sendThongBao(player, "Thử thách Thành cổ chưa sẵn sàng.");
            return;
        }
        if (members.size() > map2.zones.size()) {
            Service.gI().sendThongBao(player, "Thành cổ 2 không đủ khu để tách từng người.");
            return;
        }
        for (Player member : members) {
            if (activeRuns.containsKey(member.id)) {
                Service.gI().sendThongBao(player, member.name + " đang ở trong một lượt Thành cổ khác.");
                return;
            }
            if (!member.isAdmin() && getDailyCount(member.id) >= DAILY_LIMIT) {
                Service.gI().sendThongBao(player, member.name + " đã hết 2 lượt Thành cổ hôm nay.");
                return;
            }
        }

        List<Integer> zone1Ids = findRandomFreeZoneIds(map1, STAGE_1_COMBO_COUNT, usedZone1Ids());
        int zone3Id = findFreeZoneId(map3, usedZone3Ids());
        List<Integer> zone2Ids = findFreeZoneIds(map2, members.size(), usedZone2Ids());
        if (zone1Ids.size() < STAGE_1_COMBO_COUNT || zone3Id == -1 || zone2Ids.size() < members.size()) {
            Service.gI().sendThongBao(player, "Thành cổ đang đầy, hãy quay lại sau.");
            return;
        }

        CastleRun run = new CastleRun(nextRunId++, zone1Ids, zone3Id);
        for (int i = 0; i < members.size(); i++) {
            Player member = members.get(i);
            run.memberIds.add(member.id);
            run.memberNames.put(member.id, member.name);
            run.cloneZoneIds.put(member.id, zone2Ids.get(i));
            run.returnPoints.put(member.id, new ReturnPoint(member.zone.map.mapId, member.zone.zoneId, member.location.x));
        }
        runs.add(run);
        for (Player member : members) {
            activeRuns.put(member.id, run);
            if (!member.isAdmin()) {
                increaseDailyCount(member.id);
            }
        }
        ensureUpdater();

        Zone zoneJoin = map1.zones.get(run.zone1Id);
        prepareStage1Bosses(run);
        for (Player member : members) {
            Service.gI().sendThongBao(member, "Thử thách Thành cổ bắt đầu! Thời gian: 1 tiếng 30 phút.");
            Service.gI().sendThongBao(member, "4 cum boss Thanh co 1 da xuat hien o khu " + getStage1ZoneText(run) + ". Hay ha het de sang Thanh co 2.");
            ChangeMapService.gI().changeMapInYard(member, zoneJoin, JOIN_X);
            sendChallengeTime(member, run);
        }
    }

    public synchronized boolean handleWaypoint(Player player, Zone zoneJoin, int xGo, int yGo) {
        if (player == null || player.zone == null || zoneJoin == null) {
            return false;
        }
        CastleRun run = activeRuns.get(player.id);
        if (run == null) {
            return false;
        }
        if (isExpired(run)) {
            finishRun(run, true);
            return true;
        }
        int fromMap = player.zone.map.mapId;
        int toMap = zoneJoin.map.mapId;
        if (fromMap == ConstMap.THANH_CO_1 && toMap == ConstMap.THANH_CO_2) {
            if (hasLiveStage1Boss(run)) {
                ChangeMapService.gI().resetPoint(player);
                Service.gI().sendThongBao(player, "Hay ha het boss o Thanh co 1 truoc.");
                return true;
            }
            enterCloneStage(player, run);
            return true;
        }
        if (fromMap == ConstMap.THANH_CO_2 && toMap == ConstMap.THANH_CO_1) {
            ChangeMapService.gI().resetPoint(player);
            Service.gI().sendThongBao(player, "Hãy đối mặt với bản sao của mình trước.");
            showCloneChoice(player);
            return true;
        }
        if (fromMap == ConstMap.THANH_CO_2 && toMap == ConstMap.DAU_TRUONG_THANH_CO) {
            if (!run.finalUnlocked) {
                ChangeMapService.gI().resetPoint(player);
                Service.gI().sendThongBao(player, "Cả đội phải hạ hết bản sao trước khi vào đấu trường.");
                return true;
            }
            spawnFinalBosses(run);
            Zone finalZone = MapService.gI().getMapById(ConstMap.DAU_TRUONG_THANH_CO).zones.get(run.zone3Id);
            ChangeMapService.gI().changeMap(player, finalZone, MAP3_X, finalZone.map.yPhysicInTop(MAP3_X, 100));
            sendChallengeTime(player, run);
            return true;
        }
        return false;
    }

    public synchronized boolean isZoneLocked(Player player) {
        if (player == null || player.zone == null) {
            return false;
        }
        CastleRun run = activeRuns.get(player.id);
        return run != null && !run.finished && MapService.gI().isMapThanhCo(player.zone.map.mapId);
    }

    public synchronized boolean canOpenStage1ZoneUI(Player player) {
        CastleRun run = player != null ? activeRuns.get(player.id) : null;
        return run != null && !run.finished && isInStage1(player);
    }

    public synchronized boolean canChangeStage1Zone(Player player, int zoneId) {
        CastleRun run = player != null ? activeRuns.get(player.id) : null;
        return run != null && !run.finished && isInStage1(player) && run.stage1ZoneIds.contains(zoneId);
    }

    public synchronized boolean hasLiveStage1Boss(Player player) {
        CastleRun run = player != null ? activeRuns.get(player.id) : null;
        return run != null && !run.finished && hasLiveStage1Boss(run);
    }

    public synchronized boolean openCloneMenu(Player player) {
        if (player == null || player.zone == null || player.zone.map == null
                || player.zone.map.mapId != ConstMap.THANH_CO_2) {
            return false;
        }
        CastleRun run = activeRuns.get(player.id);
        if (run == null) {
            Service.gI().sendThongBao(player, "Ban khong co luot Thanh co dang chay.");
            return true;
        }
        if (run.finished || isExpired(run)) {
            finishRun(run, true);
            return true;
        }
        if (run.surrendered.contains(player.id)) {
            Service.gI().sendThongBao(player, "Ban da dau hang luot Thanh co nay.");
            return true;
        }
        if (hasLiveStage1Boss(run)) {
            Service.gI().sendThongBao(player, "Hay ha het boss o Thanh co 1 truoc.");
            return true;
        }
        Integer zoneId = run.cloneZoneIds.get(player.id);
        if (zoneId == null) {
            Service.gI().sendThongBao(player, "Ban khong thuoc luot Thanh co nay.");
            return true;
        }
        if (player.zone.zoneId != zoneId) {
            Service.gI().sendThongBao(player, "Hay ve dung khu ban sao cua minh: khu " + zoneId + ".");
            return true;
        }
        if (run.cloneKilled.contains(player.id)) {
            Service.gI().sendThongBao(player, run.finalUnlocked
                    ? "Ban da ha ban sao. Hay vao Dau truong Thanh co."
                    : "Ban da ha ban sao. Hay cho dong doi hoan thanh.");
            return true;
        }
        run.cloneStageEntered.add(player.id);
        sendChallengeTime(player, run);
        showCloneChoice(player);
        return true;
    }

    public synchronized void selectCloneMenu(Player player, int select) {
        if (select == 0) {
            surrender(player);
        } else if (select == 1) {
            fightClone(player);
        }
    }

    public synchronized void onCloneKilled(Player player) {
        if (player == null) {
            return;
        }
        CastleRun run = activeRuns.get(player.id);
        if (run == null || run.finished || run.surrendered.contains(player.id)) {
            return;
        }
        if (run.cloneKilled.add(player.id)) {
            Service.gI().sendThongBao(player, "Bạn đã đánh bại bản sao Thành cổ.");
            checkUnlockFinal(run);
        }
    }

    public synchronized void onCloneLeft(Player player) {
        if (player == null) {
            return;
        }
        CastleRun run = activeRuns.get(player.id);
        if (run != null && !run.cloneKilled.contains(player.id)) {
            run.cloneStarted.remove(player.id);
        }
    }

    public synchronized boolean isInCloneStage(Player player) {
        CastleRun run = player != null ? activeRuns.get(player.id) : null;
        return run != null && run.cloneStageEntered.contains(player.id) && !run.finished;
    }

    public void dropCastleBossReward(Player boss, Player plKill) {
        if (boss == null || plKill == null || boss.zone == null || boss.zone.map == null || boss.location == null) {
            return;
        }
        if (!MapService.gI().isMapThanhCo(boss.zone.map.mapId)) {
            return;
        }
        int x = boss.location.x;
        int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);
        if (Util.isTrue(30, 100)) {
            ItemMap thanLinhDrop = ItemService.gI().randDoTLBoss(boss.zone, 1, x + Util.nextInt(-50, 50), y, plKill.id);
            if (thanLinhDrop != null) {
                Service.gI().dropItemMap(boss.zone, thanLinhDrop);
            }
        }
        if (Util.isTrue(10, 100)) {
            Item doHuyDietItem = createBillHuyDietItem(HUY_DIET_IDS[Util.nextInt(HUY_DIET_IDS.length)]);
            if (doHuyDietItem != null && doHuyDietItem.template != null) {
                ItemMap doHuyDietDrop = new ItemMap(boss.zone, doHuyDietItem.template, 1, x + Util.nextInt(-50, 50), y, plKill.id);
                doHuyDietDrop.options.addAll(doHuyDietItem.itemOptions);
                Service.gI().dropItemMap(boss.zone, doHuyDietDrop);
            }
        }
        if (Util.isTrue(50, 100)) {
            int nrId = DRAGON_BALL_IDS[Util.nextInt(DRAGON_BALL_IDS.length)];
            Service.gI().dropItemMap(boss.zone, new ItemMap(boss.zone, nrId, 1, x + Util.nextInt(-50, 50), y, plKill.id));
        }
    }

    @Override
    public void run() {
        while (ServerManager.isRunning && !Maintenance.isRunning) {
            try {
                update();
                Functions.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        synchronized (this) {
            updaterStarted = false;
        }
    }

    private void update() {
        synchronized (this) {
            Iterator<CastleRun> iterator = runs.iterator();
            while (iterator.hasNext()) {
                CastleRun run = iterator.next();
                if (run.finished) {
                    iterator.remove();
                    continue;
                }
                if (run.clearCompleted) {
                    if (Util.canDoWithTime(run.clearCompleteTime, CLEAR_RETURN_DELAY)) {
                        finishRun(run, false);
                        iterator.remove();
                    }
                    continue;
                }
                if (isExpired(run)) {
                    finishRun(run, true);
                    iterator.remove();
                    continue;
                }
                checkStage1Cleared(run);
                checkUnlockFinal(run);
                if (run.finalUnlocked && !run.finalBossSpawned) {
                    spawnFinalBosses(run);
                }
                if (isFinalCleared(run)) {
                    completeFinal(run);
                }
            }
        }
    }

    private void rejoin(Player player, CastleRun run) {
        if (run.finished || isExpired(run)) {
            finishRun(run, true);
            return;
        }
        if (run.surrendered.contains(player.id)) {
            removeChallengeTime(player);
            Service.gI().sendThongBao(player, "Bạn đã đầu hàng lượt Thành cổ này, không thể vào lại.");
            return;
        }
        if (run.clearCompleted) {
            removeChallengeTime(player);
            Service.gI().sendThongBao(player, "Thành cổ đã được chinh phục, capsule sẽ đưa cả đội về sau ít giây.");
            return;
        }
        if (run.finalUnlocked) {
            spawnFinalBosses(run);
            Zone zone = MapService.gI().getMapById(ConstMap.DAU_TRUONG_THANH_CO).zones.get(run.zone3Id);
            ChangeMapService.gI().changeMapInYard(player, zone, MAP3_X);
            sendChallengeTime(player, run);
            return;
        }
        if (run.cloneStageEntered.contains(player.id)) {
            enterCloneStage(player, run);
            return;
        }
        Zone zone = MapService.gI().getMapById(ConstMap.THANH_CO_1).zones.get(run.zone1Id);
        ChangeMapService.gI().changeMapInYard(player, zone, JOIN_X);
        sendChallengeTime(player, run);
        Service.gI().sendThongBao(player, "Boss Thanh co 1 dang o khu " + getStage1ZoneText(run) + ".");
        Service.gI().sendThongBao(player, "Bạn đã quay lại lượt Thành cổ đang chạy.");
    }

    private void enterCloneStage(Player player, CastleRun run) {
        if (run.surrendered.contains(player.id)) {
            Service.gI().sendThongBao(player, "Bạn đã đầu hàng lượt Thành cổ này.");
            return;
        }
        Integer zoneId = run.cloneZoneIds.get(player.id);
        if (zoneId == null) {
            Service.gI().sendThongBao(player, "Bạn không thuộc lượt Thành cổ này.");
            return;
        }
        run.cloneStageEntered.add(player.id);
        Zone cloneZone = MapService.gI().getMapById(ConstMap.THANH_CO_2).zones.get(zoneId);
        ChangeMapService.gI().changeMapInYard(player, cloneZone, MAP2_X);
        sendChallengeTime(player, run);
        showCloneChoice(player);
    }

    private void showCloneChoice(Player player) {
        CastleRun run = activeRuns.get(player.id);
        if (run == null || run.finished || run.surrendered.contains(player.id) || run.cloneKilled.contains(player.id)) {
            return;
        }
        if (run.cloneStarted.contains(player.id)) {
            Service.gI().sendThongBao(player, "Bản sao của bạn đang chờ trong khu này.");
            return;
        }
        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_THANH_CO_CLONE, -1,
                "Bản sao của chính ngươi đang chờ phía trước.\nChiến đấu sẽ mất 10 Thỏi vàng.",
                "Đầu hàng\nVề nhà", "Chiến đấu\n10 TV");
    }

    private void surrender(Player player) {
        CastleRun run = activeRuns.get(player.id);
        if (run == null || run.finished) {
            return;
        }
        run.surrendered.add(player.id);
        ItemTimeService.gI().removeTextThachThucGioiHan(player);
        Service.gI().sendThongBao(player, "Bạn đã đầu hàng và không thể vào lại lượt Thành cổ này.");
        ChangeMapService.gI().changeMapBySpaceShip(player, 21 + player.gender, -1, -1);
        if (allMembersSurrendered(run)) {
            finishRun(run, false);
        } else {
            checkUnlockFinal(run);
        }
    }

    private void fightClone(Player player) {
        CastleRun run = activeRuns.get(player.id);
        if (run == null || run.finished || player.zone == null) {
            return;
        }
        if (run.surrendered.contains(player.id)) {
            Service.gI().sendThongBao(player, "Bạn đã đầu hàng lượt Thành cổ này.");
            return;
        }
        if (!run.cloneStageEntered.contains(player.id) || player.zone.map.mapId != ConstMap.THANH_CO_2) {
            Service.gI().sendThongBao(player, "Hãy vào Thành cổ 2 trước.");
            return;
        }
        if (run.cloneKilled.contains(player.id)) {
            Service.gI().sendThongBao(player, "Bạn đã hạ bản sao của mình rồi.");
            return;
        }
        if (run.cloneStarted.contains(player.id) || BossManager.gI().findBossClone(player) != null) {
            Service.gI().sendThongBao(player, "Bản sao của bạn đã xuất hiện.");
            return;
        }
        Item thoiVang = InventoryService.gI().findItemBag(player, ConstItem.THOI_VANG);
        if (thoiVang == null || thoiVang.quantity < FIGHT_COST) {
            Service.gI().sendThongBao(player, "Cần 10 Thỏi vàng để chiến đấu với bản sao.");
            return;
        }
        if (Service.gI().callNhanBan(player, CLONE_MULTIPLIER, (int) TIME_LIMIT, false)) {
            InventoryService.gI().subQuantityItemsBag(player, thoiVang, FIGHT_COST);
            InventoryService.gI().sendItemBags(player);
            run.cloneStarted.add(player.id);
            Service.gI().sendThongBao(player, "Bản sao x50 sức mạnh đã xuất hiện!");
        } else {
            Service.gI().sendThongBao(player, "Chưa thể tạo bản sao lúc này, Thỏi vàng không bị trừ.");
        }
    }

    private void checkUnlockFinal(CastleRun run) {
        if (run.finalUnlocked || !allRemainingMembersCleared(run)) {
            return;
        }
        run.finalUnlocked = true;
        spawnFinalBosses(run);
        for (Player member : getOnlineMembers(run)) {
            Service.gI().sendThongBao(member, "Cả đội đã hạ hết bản sao. Đấu trường Thành cổ đã mở!");
        }
    }

    private void checkStage1Cleared(CastleRun run) {
        if (run.stage1Cleared || hasLiveStage1Boss(run)) {
            return;
        }
        run.stage1Cleared = true;
        for (Player member : getOnlineMembers(run)) {
            if (member.zone != null && member.zone.map != null && member.zone.map.mapId == ConstMap.THANH_CO_1) {
                Service.gI().sendThongBao(member, "Da ha het boss Thanh co 1. Co the sang Thanh co 2.");
            }
        }
    }

    private void spawnFinalBosses(CastleRun run) {
        if (run.finalBossSpawned) {
            return;
        }
        nro.models.map.Map map = MapService.gI().getMapById(ConstMap.DAU_TRUONG_THANH_CO);
        if (map == null || run.zone3Id < 0 || run.zone3Id >= map.zones.size()) {
            return;
        }
        Zone zone = map.zones.get(run.zone3Id);
        try {
            addFinalBoss(run, new TrungUyTrang(zone, FINAL_BOSS_DAME, getFinalBossHp(run)));
            addFinalBoss(run, new TrungUyXanhLo(zone, FINAL_BOSS_DAME, getFinalBossHp(run)));
            addFinalBoss(run, new TrungUyThep(zone, FINAL_BOSS_DAME, getFinalBossHp(run)));
            addFinalBoss(run, new NinjaAoTim(zone, null, FINAL_BOSS_DAME, getFinalBossHp(run)));
            for (int i = 0; i < 4; i++) {
                addFinalBoss(run, new RobotVeSi(zone, i, FINAL_BOSS_DAME, getFinalBossHp(run)));
            }
            run.finalBossSpawned = true;
            for (Boss boss : run.finalBosses) {
                boss.changeStatus(BossStatus.RESPAWN);
            }
        } catch (Exception ignored) {
            clearFinalBosses(run);
        }
    }

    private int getFinalBossHp(CastleRun run) {
        return FINAL_BOSS_BASE_HP * (run.finalBosses.size() + 1);
    }

    private void addFinalBoss(CastleRun run, Boss boss) {
        if (boss != null) {
            run.finalBosses.add(boss);
        }
    }

    private void clearFinalBosses(CastleRun run) {
        for (Boss boss : run.finalBosses) {
            if (boss != null && boss.zone != null && boss.zone.map != null && MapService.gI().isMapThanhCo(boss.zone.map.mapId)) {
                boss.changeStatus(BossStatus.LEAVE_MAP);
            }
        }
        run.finalBosses.clear();
    }

    private boolean isFinalCleared(CastleRun run) {
        if (!run.finalBossSpawned || run.finalBosses.isEmpty()) {
            return false;
        }
        for (Boss boss : run.finalBosses) {
            if (boss != null && isFinalBossFighting(boss)) {
                return false;
            }
        }
        return true;
    }

    private void completeFinal(CastleRun run) {
        if (run.clearCompleted) {
            return;
        }
        rewardClear(run);
        run.clearCompleted = true;
        run.clearCompleteTime = System.currentTimeMillis();
        for (Player member : getOnlineMembers(run)) {
            removeChallengeTime(member);
            if (member.zone != null && MapService.gI().isMapThanhCo(member.zone.map.mapId)) {
                Service.gI().sendThongBao(member, "Cả đội đã chinh phục Đấu trường Thành cổ. Capsule sẽ đưa bạn về sau 30 giây.");
            }
        }
    }

    private boolean isFinalBossFighting(Boss boss) {
        return boss.bossStatus == BossStatus.RESPAWN
                || boss.bossStatus == BossStatus.JOIN_MAP
                || boss.bossStatus == BossStatus.CHAT_S
                || boss.bossStatus == BossStatus.AFK
                || boss.bossStatus == BossStatus.ACTIVE;
    }

    private void rewardClear(CastleRun run) {
        if (run.clearRewardGiven) {
            return;
        }
        run.clearRewardGiven = true;
        for (Player member : getOnlineMembers(run)) {
            if (member == null || run.surrendered.contains(member.id) || !isInFinalWinZone(member, run)) {
                continue;
            }
            run.clearWinners.add(member.id);
            int rewardQuantity = getClearRewardQuantity(member);
            Item reward = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, rewardQuantity);
            reward.itemOptions.add(new Item.ItemOption(30, 0));
            if (InventoryService.gI().addItemBag(member, reward)) {
                increaseDailyClearCount(member.id);
                InventoryService.gI().sendItemBags(member);
                Service.gI().sendThongBao(member, "Bạn nhận được " + rewardQuantity + " Thỏi vàng khóa giao dịch.");
            } else {
                Service.gI().sendThongBao(member, "Hành trang không đủ chỗ để nhận " + rewardQuantity + " Thỏi vàng.");
            }
        }
    }

    private int getClearRewardQuantity(Player player) {
        return getDailyClearCount(player.id) == 0 ? FIRST_CLEAR_REWARD_THOI_VANG : NEXT_CLEAR_REWARD_THOI_VANG;
    }

    private boolean isInFinalWinZone(Player player, CastleRun run) {
        return player.zone != null
                && player.zone.map != null
                && player.zone.map.mapId == ConstMap.DAU_TRUONG_THANH_CO
                && player.zone.zoneId == run.zone3Id;
    }

    private boolean allRemainingMembersCleared(CastleRun run) {
        boolean hasRemainingMember = false;
        for (long memberId : run.memberIds) {
            if (run.surrendered.contains(memberId)) {
                continue;
            }
            hasRemainingMember = true;
            if (!run.cloneKilled.contains(memberId)) {
                return false;
            }
        }
        return hasRemainingMember;
    }

    private boolean allMembersSurrendered(CastleRun run) {
        for (long memberId : run.memberIds) {
            if (!run.surrendered.contains(memberId)) {
                return false;
            }
        }
        return true;
    }

    private void finishRun(CastleRun run, boolean timeout) {
        if (run.finished) {
            return;
        }
        run.finished = true;
        clearStage1Bosses(run);
        clearFinalBosses(run);
        for (long memberId : run.memberIds) {
            activeRuns.remove(memberId);
        }
        for (Player member : getOnlineMembers(run)) {
            removeChallengeTime(member);
            if (member.zone != null && MapService.gI().isMapThanhCo(member.zone.map.mapId)) {
                if (timeout) {
                    Service.gI().sendThongBao(member, "Đã hết 1 tiếng 30 phút, bạn sẽ được đưa về nhà.");
                }
                if (timeout || !run.clearCompleted) {
                    ChangeMapService.gI().changeMapBySpaceShip(member, 21 + member.gender, -1, -1);
                } else {
                    returnMemberToEntry(member, run);
                }
            }
        }
    }

    private void returnMemberToEntry(Player member, CastleRun run) {
        ReturnPoint returnPoint = run.returnPoints.get(member.id);
        if (returnPoint == null) {
            ChangeMapService.gI().changeMapBySpaceShip(member, 21 + member.gender, -1, -1);
            return;
        }
        ChangeMapService.gI().changeMapBySpaceShip(member, returnPoint.mapId, returnPoint.zoneId, returnPoint.x);
    }

    private void prepareStage1Bosses(CastleRun run) {
        nro.models.map.Map map = MapService.gI().getMapById(ConstMap.THANH_CO_1);
        if (run == null || map == null) {
            return;
        }
        for (int zoneId : run.stage1ZoneIds) {
            if (zoneId < 0 || zoneId >= map.zones.size()) {
                continue;
            }
            spawnStage1Combo(run, map.zones.get(zoneId));
        }
    }

    private void spawnStage1Combo(CastleRun run, Zone zone) {
        if (run == null || zone == null) {
            return;
        }
        spawnBossNow(run, BossManager.gI().createBoss(BossID.THUY_TINH_THANH_CO), zone);
        spawnBossNow(run, BossManager.gI().createBoss(BossID.KHIDOT_THANH_CO), zone);
    }

    private void spawnBossNow(CastleRun run, Boss boss, Zone zone) {
        if (boss == null || zone == null) {
            return;
        }
        addStage1Boss(run, boss);
        boss.zoneFinal = zone;
        boss.respawn();
        boss.joinMap();
        if (boss.bossAppearTogether == null || boss.currentLevel < 0
                || boss.currentLevel >= boss.bossAppearTogether.length
                || boss.bossAppearTogether[boss.currentLevel] == null) {
            return;
        }
        for (Boss child : boss.bossAppearTogether[boss.currentLevel]) {
            if (child != null) {
                addStage1Boss(run, child);
                child.zoneFinal = zone;
                if (child.zone == null) {
                    child.respawn();
                    child.joinMap();
                }
            }
        }
    }

    private void addStage1Boss(CastleRun run, Boss boss) {
        if (run != null && boss != null && !run.stage1Bosses.contains(boss)) {
            run.stage1Bosses.add(boss);
        }
    }

    private void clearStage1Bosses(CastleRun run) {
        for (Boss boss : run.stage1Bosses) {
            if (boss != null && boss.zone != null && boss.zone.map != null && boss.zone.map.mapId == ConstMap.THANH_CO_1) {
                boss.changeStatus(BossStatus.LEAVE_MAP);
            }
        }
        run.stage1Bosses.clear();
    }

    private boolean hasLiveStage1Boss(CastleRun run) {
        if (run == null || run.stage1Bosses.isEmpty()) {
            return false;
        }
        for (Boss boss : run.stage1Bosses) {
            if (isStage1BossFighting(boss)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStage1BossFighting(Boss boss) {
        return boss != null
                && boss.zone != null
                && boss.zone.map != null
                && boss.zone.map.mapId == ConstMap.THANH_CO_1
                && (boss.bossStatus == BossStatus.RESPAWN
                || boss.bossStatus == BossStatus.JOIN_MAP
                || boss.bossStatus == BossStatus.CHAT_S
                || boss.bossStatus == BossStatus.AFK
                || boss.bossStatus == BossStatus.ACTIVE);
    }

    private boolean isInStage1(Player player) {
        return player != null && player.zone != null && player.zone.map != null
                && player.zone.map.mapId == ConstMap.THANH_CO_1;
    }

    private List<Player> collectMembers(Player leader) {
        List<Player> members = new ArrayList<>();
        if (leader.zone == null) {
            return members;
        }
        for (Player player : new ArrayList<>(leader.zone.getPlayers())) {
            if (player == null || !player.isPl() || player.isDie()) {
                continue;
            }
            if (Util.getDistance(player.location.x, player.location.y, leader.location.x, leader.location.y) <= TEAM_DISTANCE) {
                members.add(player);
            }
        }
        if (!members.contains(leader)) {
            members.add(0, leader);
        }
        return members;
    }

    private List<Player> getOnlineMembers(CastleRun run) {
        List<Player> members = new ArrayList<>();
        for (Player player : new ArrayList<>(Client.gI().getPlayers())) {
            if (player != null && run.memberIds.contains(player.id)) {
                members.add(player);
            }
        }
        return members;
    }

    private int findFreeZoneId(nro.models.map.Map map, Set<Integer> usedZoneIds) {
        return findFreeZoneId(map, usedZoneIds, map.zones.size());
    }

    private int findFreeZoneId(nro.models.map.Map map, Set<Integer> usedZoneIds, int zoneLimit) {
        int limit = Math.min(zoneLimit, map.zones.size());
        for (int i = 0; i < limit; i++) {
            Zone zone = map.zones.get(i);
            if (!usedZoneIds.contains(zone.zoneId) && zone.getPlayers().isEmpty()) {
                return zone.zoneId;
            }
        }
        return -1;
    }

    private List<Integer> findFreeZoneIds(nro.models.map.Map map, int amount, Set<Integer> usedZoneIds) {
        List<Integer> zoneIds = new ArrayList<>();
        for (Zone zone : map.zones) {
            if (!usedZoneIds.contains(zone.zoneId) && zone.getPlayers().isEmpty()) {
                zoneIds.add(zone.zoneId);
                if (zoneIds.size() >= amount) {
                    break;
                }
            }
        }
        return zoneIds;
    }

    private List<Integer> findRandomFreeZoneIds(nro.models.map.Map map, int amount, Set<Integer> usedZoneIds) {
        List<Integer> candidates = new ArrayList<>();
        for (Zone zone : map.zones) {
            if (!usedZoneIds.contains(zone.zoneId) && zone.getPlayers().isEmpty() && zone.getBosses().isEmpty()) {
                candidates.add(zone.zoneId);
            }
        }
        List<Integer> zoneIds = new ArrayList<>();
        while (!candidates.isEmpty() && zoneIds.size() < amount) {
            int index = Util.nextInt(candidates.size());
            zoneIds.add(candidates.remove(index));
        }
        return zoneIds;
    }

    private Set<Integer> usedZone1Ids() {
        Set<Integer> used = new HashSet<>();
        for (CastleRun run : runs) {
            if (!run.finished) {
                used.addAll(run.stage1ZoneIds);
            }
        }
        return used;
    }

    private String getStage1ZoneText(CastleRun run) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < run.stage1ZoneIds.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(run.stage1ZoneIds.get(i));
        }
        return text.toString();
    }

    private Set<Integer> usedZone2Ids() {
        Set<Integer> used = new HashSet<>();
        for (CastleRun run : runs) {
            if (!run.finished) {
                used.addAll(run.cloneZoneIds.values());
            }
        }
        return used;
    }

    private Set<Integer> usedZone3Ids() {
        Set<Integer> used = new HashSet<>();
        for (CastleRun run : runs) {
            if (!run.finished) {
                used.add(run.zone3Id);
            }
        }
        return used;
    }

    private int getDailyCount(long playerId) {
        DailyCounter counter = dailyCounters.get(playerId);
        if (counter == null) {
            return 0;
        }
        if (Util.isAfterMidnight(counter.lastTime)) {
            counter.count = 0;
            counter.lastTime = System.currentTimeMillis();
        }
        return counter.count;
    }

    private void increaseDailyCount(long playerId) {
        DailyCounter counter = dailyCounters.computeIfAbsent(playerId, id -> new DailyCounter());
        if (Util.isAfterMidnight(counter.lastTime)) {
            counter.count = 0;
        }
        counter.count++;
        counter.lastTime = System.currentTimeMillis();
    }

    private int getDailyClearCount(long playerId) {
        DailyCounter counter = dailyClearCounters.get(playerId);
        if (counter == null) {
            return 0;
        }
        if (Util.isAfterMidnight(counter.lastTime)) {
            counter.count = 0;
            counter.lastTime = System.currentTimeMillis();
        }
        return counter.count;
    }

    private void increaseDailyClearCount(long playerId) {
        DailyCounter counter = dailyClearCounters.computeIfAbsent(playerId, id -> new DailyCounter());
        if (Util.isAfterMidnight(counter.lastTime)) {
            counter.count = 0;
        }
        counter.count++;
        counter.lastTime = System.currentTimeMillis();
    }

    private boolean isExpired(CastleRun run) {
        return Util.canDoWithTime(run.startTime, TIME_LIMIT);
    }

    private void sendChallengeTime(Player player, CastleRun run) {
        if (player == null || run == null || run.finished) {
            return;
        }
        int secondsLeft = (int) ((TIME_LIMIT - (System.currentTimeMillis() - run.startTime)) / 1000);
        ItemTimeService.gI().sendTextThachThucGioiHan(player, secondsLeft);
    }

    private void removeChallengeTime(Player player) {
        if (player != null) {
            ItemTimeService.gI().removeTextThachThucGioiHan(player);
        }
    }

    private void ensureUpdater() {
        if (!updaterStarted) {
            updaterStarted = true;
            new Thread(this, "Ancient Castle Service").start();
        }
    }

    private Item createBillHuyDietItem(short itemId) {
        Item item = null;
        for (Shop shop : Manager.SHOPS) {
            if (shop.tagName != null && shop.tagName.equals("BILL")) {
                ItemShop itemShop = shop.getItemShop(itemId);
                if (itemShop != null) {
                    item = ItemService.gI().createItemFromItemShop(itemShop);
                }
                break;
            }
        }
        if (item == null || item.template == null) {
            return null;
        }
        applyBillHuyDietOptions(item);
        return item;
    }

    private void applyBillHuyDietOptions(Item item) {
        int param = 0;
        if (item.template.level == 14) {
            int random = Util.nextInt(1, 100);
            if (random <= 1) {
                param = 15;
            } else if (random <= 15) {
                param = Util.nextInt(11, 14);
            } else if (random <= 35) {
                param = Util.nextInt(7, 10);
            } else if (random <= 60) {
                param = Util.nextInt(4, 6);
            } else {
                param = Util.nextInt(0, 3);
            }
        }

        List<Item.ItemOption> itemOptions = new ArrayList<>();
        if (!item.itemOptions.isEmpty()) {
            for (Item.ItemOption option : item.itemOptions) {
                if (item.template.level == 14 && canUpgradeBillHuyDietOption(option.optionTemplate.id) && param > 0) {
                    int optionId = option.optionTemplate.id;
                    int optionParam = option.param + (option.param * param) / 100;
                    itemOptions.add(new Item.ItemOption(optionId, optionParam));
                } else if (option.optionTemplate.id != 164) {
                    itemOptions.add(new Item.ItemOption(option.optionTemplate.id, option.param));
                }
            }
        } else {
            itemOptions.add(new Item.ItemOption(73, (short) 0));
        }
        itemOptions.add(new Item.ItemOption(30, (short) 0));

        if (item.template.level == 14) {
            int roll = Util.nextInt(3);
            switch (roll) {
                case 0 -> itemOptions.add(new Item.ItemOption(77, Util.nextInt(1, 5)));
                case 1 -> itemOptions.add(new Item.ItemOption(50, Util.nextInt(1, 3)));
                case 2 -> itemOptions.add(new Item.ItemOption(103, Util.nextInt(1, 5)));
            }
        }
        item.itemOptions.clear();
        item.itemOptions.addAll(itemOptions);
    }

    private boolean canUpgradeBillHuyDietOption(int optionId) {
        return optionId == 0 || optionId == 22 || optionId == 23 || optionId == 14
                || optionId == 27 || optionId == 28 || optionId == 47;
    }

    private static class CastleRun {

        private final int id;
        private final int zone1Id;
        private final int zone3Id;
        private final long startTime;
        private final List<Integer> stage1ZoneIds = new ArrayList<>();
        private final List<Long> memberIds = new ArrayList<>();
        private final Map<Long, String> memberNames = new HashMap<>();
        private final Map<Long, Integer> cloneZoneIds = new HashMap<>();
        private final Map<Long, ReturnPoint> returnPoints = new HashMap<>();
        private final Set<Long> cloneStageEntered = new HashSet<>();
        private final Set<Long> cloneStarted = new HashSet<>();
        private final Set<Long> cloneKilled = new HashSet<>();
        private final Set<Long> surrendered = new HashSet<>();
        private final Set<Long> clearWinners = new HashSet<>();
        private final List<Boss> stage1Bosses = new ArrayList<>();
        private final List<Boss> finalBosses = new ArrayList<>();
        private boolean stage1Cleared;
        private boolean finalUnlocked;
        private boolean finalBossSpawned;
        private boolean clearCompleted;
        private long clearCompleteTime;
        private boolean clearRewardGiven;
        private boolean finished;

        private CastleRun(int id, List<Integer> zone1Ids, int zone3Id) {
            this.id = id;
            this.zone1Id = zone1Ids.get(0);
            this.stage1ZoneIds.addAll(zone1Ids);
            this.zone3Id = zone3Id;
            this.startTime = System.currentTimeMillis();
        }
    }

    private static class ReturnPoint {

        private final int mapId;
        private final int zoneId;
        private final int x;

        private ReturnPoint(int mapId, int zoneId, int x) {
            this.mapId = mapId;
            this.zoneId = zoneId;
            this.x = x;
        }
    }

    private static class DailyCounter {

        private int count;
        private long lastTime;
    }
}
