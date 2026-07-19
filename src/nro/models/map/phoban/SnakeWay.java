package nro.models.map.phoban;

import nro.models.utils.Functions;
import nro.models.boss_con_duong_ran_doc.SAIBAMEN;
import nro.models.boss_con_duong_ran_doc.NADIC;
import nro.models.boss_con_duong_ran_doc.CADICH;
import nro.models.boss.Boss;
import nro.models.consts.BossStatus;
import nro.models.clan.Clan;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.ItemTimeService;
import nro.models.map.service.MapService;
import nro.models.services.Service;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import lombok.Data;
import nro.models.server.Maintenance;
import nro.models.map.service.ItemMapService;
import nro.models.utils.TimeUtil;

@Data
public class SnakeWay implements Runnable {

    public static final long POWER_CAN_GO_TO_CDRD = 2000000000;
    public static final int AVAILABLE = 50;
    public static final int TIME_CON_DUONG_RAN_DOC = 90 * 60 * 1000;
    public static final int HP_CON_DUONG_RAN_DOC = 2_000_000_000;
    private static final short REWARD_GOLD_BAR_ID = 457;
    private static final int REWARD_GOLD_BAR_QUANTITY = 500;
    private static final int REWARD_GEM_QUANTITY = 10_000;

    public int id;
    public byte level;
    public final List<Zone> zones;

    public Clan clan;
    public boolean isOpened;
    private long lastTimeOpen;
    private long lastTimeUpdateMessage;
    private boolean kickoutcdrd;
    private long timeKickOutCDRD;
    public List<Boss> bosses = new ArrayList<>();
    public boolean endCDRD;
    public boolean allMobsDead;
    private boolean winRewardGiven;

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public SnakeWay(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning && isOpened) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - startTime), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_CON_DUONG_RAN_DOC) || (kickoutcdrd && Util.canDoWithTime(timeKickOutCDRD, 60000))) {
                finish();
                dispose();
            }

            boolean allCharactersDead = true;
            for (Zone zone : zones) {
                for (Mob mob : zone.mobs) {
                    if (!mob.isDie()) {
                        allCharactersDead = false;
                        break;
                    }
                }
            }
            if (allCharactersDead) {
                allMobsDead = true;
            }

            if (endCDRD && !winRewardGiven) {
                rewardWinForPlayers();
            }

            if (!kickoutcdrd && (endCDRD || Util.canDoWithTime(lastTimeOpen, TIME_CON_DUONG_RAN_DOC - 60000))) {
                kickoutcdrd = true;
                timeKickOutCDRD = System.currentTimeMillis();
            }
            if (kickoutcdrd && Util.canDoWithTime(lastTimeUpdateMessage, 10000)) {
                lastTimeUpdateMessage = System.currentTimeMillis();
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Trận chiến với người Xayda sẽ kết thúc sau " + TimeUtil.getTimeLeft(timeKickOutCDRD, 60) + " nữa");
                    }

                }
            }

        }
    }

    public void openConDuongRanDoc(Player plOpen, Clan clan, byte level) {
        try {
            this.level = level;
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.lastTimeOpenConDuongRanDoc = this.lastTimeOpen;
            this.clan.playerOpenConDuongRanDoc = plOpen;
            this.clan.ConDuongRanDoc = this;
            this.isOpened = true;
            this.init();
            sendTextConDuongRanDoc();
        } catch (Exception e) {
            plOpen.clan.lastTimeOpenConDuongRanDoc = 0;
            this.dispose();
        }
    }

    public void sendThanhTichCDRD(Player pl) {
        if (pl == null || pl.clan == null || pl.clan.ConDuongRanDoc != this) {
            return;
        }
        long timeDoneCDRD = System.currentTimeMillis() - pl.clan.lastTimeOpenConDuongRanDoc;
        int levelDoneCDRD = pl.clan.ConDuongRanDoc.level;

        if (levelDoneCDRD > pl.clan.levelDoneCDRD) {
            pl.clan.levelDoneCDRD = levelDoneCDRD;
            pl.clan.thoiGianHoanThanhCDRD = timeDoneCDRD;
        } else if (levelDoneCDRD == pl.clan.levelDoneCDRD) {
            if (timeDoneCDRD < pl.clan.thoiGianHoanThanhCDRD) {
                pl.clan.thoiGianHoanThanhCDRD = timeDoneCDRD;
            }
        }

        pl.clan.updatethanhTichCDRDForLeader();
    }

    private void init() {
        //Hồi sinh quái
        for (Zone zone : this.zones) {
            List<Mob> mobs = zone.mobs;
            for (int i = 0; i < mobs.size(); i++) {
                Mob mob = mobs.get(i);
                if (i == 5) {
                    mob.lvMob = 1;
                    mob.point.dame = (int) level * 100 * mob.tempId * 12;
                } else {
                    mob.lvMob = 0;
                    mob.point.dame = (int) level * 10 * mob.tempId;
                }
                mob.point.maxHp = HP_CON_DUONG_RAN_DOC;
                mob.hoiSinh();
                mob.hoiSinhMobPhoBan();
            }

            if (zone.map.mapId == 144) {
                try {
                    long bossDamage = (200000L * level);
                    int bossMaxHealth = HP_CON_DUONG_RAN_DOC;
                    for (int i = 6; i > 0; i--) {
                        bossDamage = Math.min(bossDamage, 200000000L);
                        bosses.add(new SAIBAMEN(
                                zone,
                                clan,
                                i,
                                (int) bossDamage,
                                (int) bossMaxHealth
                        ));
                    }
                    bossDamage = Math.min(bossDamage * 5, 200000000L);
                    bosses.add(new NADIC(
                            zone,
                            clan,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                    bossDamage = Math.min(bossDamage * 10, 200000000L);
                    bosses.add(new CADICH(
                            zone,
                            clan,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                } catch (Exception exception) {
                }
            }
        }
        Executors.newSingleThreadExecutor().submit(this, "Con Đường Rắn Độc: " + this.clan.name);
    }

    public void finish() {
        boolean rewardWin = endCDRD && !winRewardGiven;
        if (rewardWin) {
            rewardWinForPlayers();
        }
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    sendThanhTichCDRD(pl);
                    kickOutOfCDRD(pl);
                }
            }

        }
    }

    private void rewardWinForPlayers() {
        for (Zone zone : zones) {
            for (Player player : zone.getPlayers()) {
                rewardWin(player);
            }
        }
        winRewardGiven = true;
    }

    private void rewardWin(Player player) {
        if (player == null || player.inventory == null) {
            return;
        }

        player.inventory.gem = (int) Math.min((long) player.inventory.gem + REWARD_GEM_QUANTITY, Integer.MAX_VALUE);

        boolean addedGoldBar = InventoryService.gI().addItemBag(
                player,
                ItemService.gI().createNewItem(REWARD_GOLD_BAR_ID, REWARD_GOLD_BAR_QUANTITY)
        );
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);

        if (addedGoldBar) {
            Service.gI().sendThongBao(player, "Bạn nhận được 500 thỏi vàng và 10.000 ngọc xanh khi hoàn thành Con đường rắn độc");
        } else {
            Service.gI().sendThongBao(player, "Bạn nhận được 10.000 ngọc xanh, hành trang không đủ chỗ để nhận 500 thỏi vàng");
        }
    }

    private void kickOutOfCDRD(Player player) {
        if (MapService.gI().isMapConDuongRanDoc(player.zone.map.mapId)) {
            ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1038);
        }
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void sendTextConDuongRanDoc() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextConDuongRanDoc(pl);
        }
    }

    private void removeTextConDuongRanDoc() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextConDuongRanDoc(pl);
        }
    }

    public long getNumBossAlive() {
        return bosses.stream().filter(boss -> boss.bossStatus != BossStatus.REST).count();
    }

    public void dispose() {
        // remove bosses
        for (Boss boss : bosses) {
            if (!boss.isDie()) {
                boss.leaveMap();
            }
        }
        for (Zone zone : zones) {
            for (int i = zone.items.size() - 1; i >= 0; i--) {
                if (i < zone.items.size()) {
                    ItemMapService.gI().removeItemMap(zone.items.get(i));
                }
            }
        }
        this.removeTextConDuongRanDoc();
        this.bosses.clear();
        this.allMobsDead = false;
        this.endCDRD = false;
        this.winRewardGiven = false;
        this.isOpened = false;
        this.clan.ConDuongRanDoc = null;
        this.clan = null;
        this.kickoutcdrd = false;
    }
}
