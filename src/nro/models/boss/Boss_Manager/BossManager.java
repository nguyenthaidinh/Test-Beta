package nro.models.boss.Boss_Manager;

import nro.models.boss.Android.Android13;
import nro.models.boss.Android.Android14;
import nro.models.boss.Android.Android15;
import nro.models.boss.Android.Android19;
import nro.models.boss.Android.DrKore;
import nro.models.boss.Android.KingKong;
import nro.models.boss.Android.Pic;
import nro.models.boss.Android.Poc;
import nro.models.boss.Black_Goku.BlackGoku;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.Boss_mini.AnTrom;
import nro.models.boss.Boss_mini.Odo;
import nro.models.boss.Boss_mini.RongNhi;
import nro.models.boss.Boss_mini.SoiHecQuyn;
import nro.models.boss.Boss_mini.Virut;
import nro.models.boss.Broly.Broly;
import nro.models.boss.Cell.SieuBoHung;
import nro.models.boss.Cell.XENCON1;
import nro.models.boss.Cell.XENCON2;
import nro.models.boss.Cell.XENCON3;
import nro.models.boss.Cell.XENCON4;
import nro.models.boss.Cell.XENCON5;
import nro.models.boss.Cell.XENCON6;
import nro.models.boss.Cell.XENCON7;
import nro.models.boss.Cell.XenBoHung;
import nro.models.boss.Cold.Cooler;
import nro.models.boss.trai_dat.BIDO;
import nro.models.boss.trai_dat.BOJACK;
import nro.models.boss.trai_dat.BUJIN;
import nro.models.boss.trai_dat.KOGU;
import nro.models.boss.trai_dat.SUPER_BOJACK;
import nro.models.boss.trai_dat.ZANGYA;
import nro.models.boss.Frieza.Fide;
import nro.models.boss.Golden_fireza.DeathBeam1;
import nro.models.boss.Golden_fireza.DeathBeam2;
import nro.models.boss.Golden_fireza.DeathBeam3;
import nro.models.boss.Golden_fireza.DeathBeam4;
import nro.models.boss.Golden_fireza.DeathBeam5;
import nro.models.boss.Golden_fireza.GoldenFrieza;
import nro.models.boss.MajinBuu_12h.BuiBui;
import nro.models.boss.MajinBuu_12h.BuiBui2;
import nro.models.boss.MajinBuu_12h.Cadic;
import nro.models.boss.MajinBuu_12h.Drabura;
import nro.models.boss.MajinBuu_12h.Drabura2;
import nro.models.boss.MajinBuu_12h.Drabura3;
import nro.models.boss.MajinBuu_12h.Goku;
import nro.models.boss.MajinBuu_12h.Mabu;
import nro.models.boss.MajinBuu_12h.Yacon;
import nro.models.boss.MajinBuu_14h.Mabu2H;
import nro.models.boss.MajinBuu_14h.SuperBu;
import nro.models.boss.ma_vuong_picolo_namek.Pocolo;
import nro.models.boss.tieu_doi_sat_thu_namek.SO1_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.SO2_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.SO3_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.SO4_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.TDT_NM;
import nro.models.boss.Nappa.Kuku;
import nro.models.boss.Nappa.MapDauDinh;
import nro.models.boss.Nappa.Rambo;
import nro.models.boss.tieu_doi_sat_thu.SO1;
import nro.models.boss.tieu_doi_sat_thu.SO2;
import nro.models.boss.tieu_doi_sat_thu.SO3;
import nro.models.boss.tieu_doi_sat_thu.SO4;
import nro.models.boss.tieu_doi_sat_thu.TDT;
import nro.models.boss.Tau_PayPay.TaoPaiPai;
import nro.models.boss.yardrat.CHIENBINH0;
import nro.models.boss.yardrat.CHIENBINH1;
import nro.models.boss.yardrat.CHIENBINH2;
import nro.models.boss.yardrat.CHIENBINH3;
import nro.models.boss.yardrat.CHIENBINH4;
import nro.models.boss.yardrat.CHIENBINH5;
import nro.models.boss.yardrat.DOITRUONG5;
import nro.models.boss.yardrat.TANBINH0;
import nro.models.boss.yardrat.TANBINH1;
import nro.models.boss.yardrat.TANBINH2;
import nro.models.boss.yardrat.TANBINH3;
import nro.models.boss.yardrat.TANBINH4;
import nro.models.boss.yardrat.TANBINH5;
import nro.models.boss.yardrat.TAPSU0;
import nro.models.boss.yardrat.TAPSU1;
import nro.models.boss.yardrat.TAPSU2;
import nro.models.boss.yardrat.TAPSU3;
import nro.models.boss.yardrat.TAPSU4;
import nro.models.boss.event.Halloween.BiMa;
import nro.models.boss.event.Halloween.Doi;
import nro.models.boss.event.Halloween.MaTroi;
import nro.models.boss.event_hung_vuong.SonTinh;
import nro.models.boss.event_hung_vuong.ThuyTinh;
import nro.models.boss.event_trung_thu.KhiDot;
import nro.models.boss.event_trung_thu.NguyetThan;
import nro.models.boss.event_trung_thu.NhatThan;
import nro.models.boss.event_tet.LanCon;
import nro.models.boss.event_noel.OngGiaNoel;
import nro.models.boss.than_huy_diet.BeerusBoss;
import nro.models.boss.than_huy_diet.WhisBoss;
import nro.models.player.Player;
import nro.models.network.Message;
import nro.models.map.service.MapService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.boss.Baby.Baby;
import nro.models.boss.BossDropRateManager;
import nro.models.boss.Boss_mini.MatTroi;
import nro.models.boss.cumber.Cumber;
import nro.models.consts.ConstNpc;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.NpcService;
import nro.models.mob_bigboss.GauTuongCuop;
import nro.models.server.Maintenance;
import nro.models.server.ServerManager;
import nro.models.services.Service;
import nro.models.utils.Functions;
import nro.models.utils.Logger;

public class BossManager implements Runnable {

    public static final int MENU_BOSS_LIST = 3;
    public static final int MENU_THAN_LINH_DROP_RATE = 4;

    private static BossManager instance;
    public static byte ratioReward = 10;
    private static final Map<Long, List<Boss>> ADMIN_BOSS_SELECTIONS = new ConcurrentHashMap<>();
    private static final Map<Long, List<Boss>> ADMIN_DROP_RATE_SELECTIONS = new ConcurrentHashMap<>();

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    protected final List<Boss> bosses;

    public List<Boss> getBosses() {
        return this.bosses;
    }

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public void loadBoss() {
        this.createBoss(BossID.TIEU_DOI_TRUONG);
        this.createBoss(BossID.TIEU_DOI_TRUONG_NM);
        this.createBoss(BossID.BOJACK);
        this.createBoss(BossID.SUPER_BOJACK);
        this.createBoss(BossID.KING_KONG);
        this.createBoss(BossID.XEN_BO_HUNG, 1);
        this.createBoss(BossID.SIEU_BO_HUNG, 1);
        this.createBoss(BossID.KUKU, 5);
        this.createBoss(BossID.MAP_DAU_DINH, 5);
        this.createBoss(BossID.RAMBO, 5);
        this.createBoss(BossID.FIDE);
        this.createBoss(BossID.ANDROID_14);
        this.createBoss(BossID.DR_KORE);
        this.createBoss(BossID.CUMBER);
        this.createBoss(BossID.COOLER, 1);
        this.createBoss(BossID.BLACK_GOKU, 2);
        this.createBoss(BossID.HTTV_GOKU);
        this.createBoss(BossID.HTTV_CADIC);
        this.createBoss(BossID.HTTV_BLACK_GOKU);
        this.createBoss(BossID.GOLDEN_FRIEZA, 3);
        this.createBoss(BossID.SOI_HEC_QUYN1, 2);
        this.createBoss(BossID.AN_TROM, 5);
        this.createBoss(BossID.O_DO1, 5);
        this.createBoss(BossID.BABY, 2);
        this.createBoss(BossID.MAT_TROI, 20);
        this.createBoss(BossID.POCOLO_NAMEK, 1);
        this.createBoss(BossID.WHIS_BOSS);
        this.createBoss(BossID.BEERUS_BOSS);

    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            return switch (bossID) {               
                case BossID.BROLY ->
                    new Broly();
                case BossID.TAP_SU_0 ->
                    new TAPSU0();
                case BossID.TAP_SU_1 ->
                    new TAPSU1();
                case BossID.TAP_SU_2 ->
                    new TAPSU2();
                case BossID.TAP_SU_3 ->
                    new TAPSU3();
                case BossID.TAP_SU_4 ->
                    new TAPSU4();
                case BossID.TAN_BINH_5 ->
                    new TANBINH5();
                case BossID.TAN_BINH_0 ->
                    new TANBINH0();
                case BossID.TAN_BINH_1 ->
                    new TANBINH1();
                case BossID.TAN_BINH_2 ->
                    new TANBINH2();
                case BossID.TAN_BINH_3 ->
                    new TANBINH3();
                case BossID.TAN_BINH_4 ->
                    new TANBINH4();
                case BossID.CHIEN_BINH_5 ->
                    new CHIENBINH5();
                case BossID.CHIEN_BINH_0 ->
                    new CHIENBINH0();
                case BossID.CHIEN_BINH_1 ->
                    new CHIENBINH1();
                case BossID.CHIEN_BINH_2 ->
                    new CHIENBINH2();
                case BossID.CHIEN_BINH_3 ->
                    new CHIENBINH3();
                case BossID.CHIEN_BINH_4 ->
                    new CHIENBINH4();
                case BossID.DOI_TRUONG_5 ->
                    new DOITRUONG5();
                case BossID.SO_4 ->
                    new SO4();
                case BossID.SO_3 ->
                    new SO3();
                case BossID.SO_2 ->
                    new SO2();
                case BossID.SO_1 ->
                    new SO1();
                case BossID.TIEU_DOI_TRUONG ->
                    new TDT();
                case BossID.SO_4_NM ->
                    new SO4_NM();
                case BossID.SO_3_NM ->
                    new SO3_NM();
                case BossID.SO_2_NM ->
                    new SO2_NM();
                case BossID.SO_1_NM ->
                    new SO1_NM();
                case BossID.TIEU_DOI_TRUONG_NM ->
                    new TDT_NM();
                case BossID.BUJIN ->
                    new BUJIN();
                case BossID.KOGU ->
                    new KOGU();
                case BossID.ZANGYA ->
                    new ZANGYA();
                case BossID.BIDO ->
                    new BIDO();
                case BossID.BOJACK ->
                    new BOJACK();
                case BossID.SUPER_BOJACK ->
                    new SUPER_BOJACK();
                case BossID.KUKU ->
                    new Kuku();
                case BossID.MAP_DAU_DINH ->
                    new MapDauDinh();
                case BossID.RAMBO ->
                    new Rambo();
                case BossID.TAU_PAY_PAY_DONG_NAM_KARIN ->
                    new TaoPaiPai();
                case BossID.DRABURA ->
                    new Drabura();
                case BossID.BUI_BUI ->
                    new BuiBui();
                case BossID.BUI_BUI_2 ->
                    new BuiBui2();
                case BossID.YA_CON ->
                    new Yacon();
                case BossID.DRABURA_2 ->
                    new Drabura2();
                case BossID.GOKU ->
                    new Goku();
                case BossID.CADIC ->
                    new Cadic();
                case BossID.MABU_12H ->
                    new Mabu();
                case BossID.DRABURA_3 ->
                    new Drabura3();
                case BossID.MABU ->
                    new Mabu2H();
                case BossID.SUPERBU ->
                    new SuperBu();
                case BossID.FIDE ->
                    new Fide();
                case BossID.DR_KORE ->
                    new DrKore();
                case BossID.ANDROID_19 ->
                    new Android19();
                case BossID.ANDROID_13 ->
                    new Android13();
                case BossID.ANDROID_14 ->
                    new Android14();
                case BossID.ANDROID_15 ->
                    new Android15();
                case BossID.PIC ->
                    new Pic();
                case BossID.POC ->
                    new Poc();
                case BossID.KING_KONG ->
                    new KingKong();
                case BossID.XEN_BO_HUNG ->
                    new XenBoHung();
                case BossID.SIEU_BO_HUNG ->
                    new SieuBoHung();
                case BossID.XEN_CON_1 ->
                    new XENCON1();
                case BossID.XEN_CON_2 ->
                    new XENCON2();
                case BossID.XEN_CON_3 ->
                    new XENCON3();
                case BossID.XEN_CON_4 ->
                    new XENCON4();
                case BossID.XEN_CON_5 ->
                    new XENCON5();
                case BossID.XEN_CON_6 ->
                    new XENCON6();
                case BossID.XEN_CON_7 ->
                    new XENCON7();
                case BossID.COOLER ->
                    new Cooler();
                case BossID.KHIDOT ->
                    new KhiDot();
                case BossID.KHIDOT_THANH_CO ->
                    new KhiDot(BossID.KHIDOT_THANH_CO, BossesData.KHIDOT_THANH_CO);
                case BossID.NGUYETTHAN ->
                    new NguyetThan();
                case BossID.NHATTHAN ->
                    new NhatThan();
                case BossID.GOLDEN_FRIEZA ->
                    new GoldenFrieza();
                case BossID.DEATH_BEAM_1 ->
                    new DeathBeam1();
                case BossID.DEATH_BEAM_2 ->
                    new DeathBeam2();
                case BossID.DEATH_BEAM_3 ->
                    new DeathBeam3();
                case BossID.DEATH_BEAM_4 ->
                    new DeathBeam4();
                case BossID.DEATH_BEAM_5 ->
                    new DeathBeam5();
                case BossID.BIMA ->
                    new BiMa();
                case BossID.MATROI ->
                    new MaTroi();
                case BossID.DOI ->
                    new Doi();
                case BossID.ONG_GIA_NOEL ->
                    new OngGiaNoel();
                case BossID.SON_TINH ->
                    new SonTinh();
                case BossID.THUY_TINH ->
                    new ThuyTinh();
                case BossID.THUY_TINH_THANH_CO ->
                    new ThuyTinh(BossID.THUY_TINH_THANH_CO, BossesData.THUY_TINH_THANH_CO);
                case BossID.LAN_CON ->
                    new LanCon();
                case BossID.SOI_HEC_QUYN1 ->
                    new SoiHecQuyn();
                case BossID.O_DO1 ->
                    new Odo();
                case BossID.Virut ->
                    new Virut();
                case BossID.MAT_TROI ->
                    new MatTroi();
                case BossID.POCOLO_NAMEK ->
                    new Pocolo();
                case BossID.BLACK_GOKU ->
                    new BlackGoku();
                case BossID.HTTV_GOKU ->
                    new nro.models.boss.gokuvegeta.Goku();
                case BossID.HTTV_CADIC ->
                    new nro.models.boss.gokuvegeta.Cadic();
                case BossID.HTTV_BLACK_GOKU ->
                    new nro.models.boss.gokuvegeta.BlackGoku();
                case BossID.CUMBER ->
                    new Cumber();
                case BossID.AN_TROM ->
                    new AnTrom();
                case BossID.RONG_NHI ->
                    new RongNhi();
                case BossID.BABY ->
                    new Baby();
                case BossID.WHIS_BOSS ->
                    new WhisBoss();
                case BossID.BEERUS_BOSS ->
                    new BeerusBoss();
                default ->
                    null;
            };
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }

    public Boss getBoss(int id) {
        try {
            Boss boss = this.bosses.get(id);
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void showListBoss(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(MENU_BOSS_LIST);
        List<Boss> selections = getAdminBossSelections();
        ADMIN_BOSS_SELECTIONS.put(player.id, selections);
        if (selections.isEmpty()) {
            NpcService.gI().createTutorial(player, -1, "Chưa có boss trong danh sách.");
            return;
        }
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Quản lý Boss");
            msg.writer().writeByte(selections.size());
            for (int i = 0; i < selections.size(); i++) {
                Boss boss = selections.get(i);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                msg.writer().writeUTF(getBossStatusText(boss));
                msg.writer().writeUTF(getBossLocationText(boss));
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    private List<Boss> getAdminBossSelections() {
        List<Boss> selections = new ArrayList<>();
        for (Boss boss : new ArrayList<>(this.bosses)) {
            if (canShowInAdminBossList(boss)) {
                selections.add(boss);
            }
        }
        return selections;
    }

    private boolean canShowInAdminBossList(Boss boss) {
        if (boss == null || boss.data == null || boss.data.length == 0 || boss.data[0].getMapJoin().length == 0) {
            return false;
        }
        int mapJoin = boss.data[0].getMapJoin()[0];
        return !MapService.gI().isMapBossFinal(mapJoin)
                && !MapService.gI().isMapHuyDiet(mapJoin)
                && !MapService.gI().isMapCadic(mapJoin)
                && !MapService.gI().isMapYardart(mapJoin)
                && !MapService.gI().isMapMaBu(mapJoin)
                && !MapService.gI().isMapBlackBallWar(mapJoin);
    }

    private String getBossStatusText(Boss boss) {
        if (boss.zone != null && !boss.isDie()) {
            return "Đang xuất hiện - " + boss.bossStatus;
        }
        return "Chưa xuất hiện - " + boss.bossStatus;
    }

    private String getBossLocationText(Boss boss) {
        if (boss.zone == null) {
            return "Chưa có map/khu";
        }
        return boss.zone.map.mapName + " (" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId;
    }

    public void showBossActionMenu(Player player, int selection) {
        if (!player.isAdmin()) {
            Service.gI().sendThongBao(player, "Không đủ quyền.");
            return;
        }
        Boss boss = getAdminBossSelection(player, selection);
        if (boss == null) {
            Service.gI().sendThongBao(player, "Boss không còn trong danh sách.");
            showListBoss(player);
            return;
        }
        player.idMark.setTempId(selection);
        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN_BOSS_ACTION, -1,
                boss.data[0].getName()
                + "\n" + getBossStatusText(boss)
                + "\n" + getBossLocationText(boss),
                "Đi tới", "Hồi sinh", "Làm mới", "Đóng");
    }

    public void handleBossAction(Player player, int select) {
        if (!player.isAdmin()) {
            Service.gI().sendThongBao(player, "Không đủ quyền.");
            return;
        }
        Boss boss = getAdminBossSelection(player, (int) player.idMark.getTempId());
        if (boss == null) {
            Service.gI().sendThongBao(player, "Boss không còn trong danh sách.");
            showListBoss(player);
            return;
        }
        switch (select) {
            case 0 -> {
                if (boss.zone == null) {
                    Service.gI().sendThongBao(player, "Boss chưa xuất hiện, hãy hồi sinh trước.");
                    return;
                }
                ChangeMapService.gI().changeMapYardrat(player, boss.zone, boss.location.x, boss.location.y);
            }
            case 1 -> {
                Boss respawnTarget = boss.getRespawnTarget();
                respawnTarget.forceRespawnNow();
                Service.gI().sendThongBao(player, "Đã hồi sinh " + respawnTarget.data[0].getName() + ".");
                showListBoss(player);
            }
            case 2 -> showListBoss(player);
            default -> ADMIN_BOSS_SELECTIONS.remove(player.id);
        }
    }

    private Boss getAdminBossSelection(Player player, int selection) {
        List<Boss> selections = ADMIN_BOSS_SELECTIONS.get(player.id);
        if (selections == null || selection < 0 || selection >= selections.size()) {
            return null;
        }
        Boss boss = selections.get(selection);
        return this.bosses.contains(boss) ? boss : null;
    }

    public void showThanLinhDropRateList(Player player) {
        if (!player.isAdmin()) {
            return;
        }

        Map<String, Boss> bossTypes = new LinkedHashMap<>();
        BossManager[] managers = {
            this,
            BrolyManager.gI(),
            OtherBossManager.gI(),
            RedRibbonHQManager.gI(),
            TreasureUnderSeaManager.gI(),
            SnakeWayManager.gI(),
            GasDestroyManager.gI(),
            FinalBossManager.gI(),
            SkillSummonedManager.gI(),
            YardartManager.gI(),
            TrungThuEventManager.gI(),
            HalloweenEventManager.gI(),
            ChristmasEventManager.gI(),
            HungVuongEventManager.gI(),
            LunarNewYearEventManager.gI()
        };

        BossDropRateManager rateManager = BossDropRateManager.gI();
        for (BossManager manager : managers) {
            List<Boss> managerBosses = manager.getBosses();
            for (int i = 0; i < managerBosses.size(); i++) {
                Boss boss;
                try {
                    boss = managerBosses.get(i);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
                if (rateManager.supports(boss)) {
                    bossTypes.putIfAbsent(boss.getClass().getName(), boss);
                }
            }
        }

        List<Boss> selections = new ArrayList<>(bossTypes.values());
        ADMIN_DROP_RATE_SELECTIONS.put(player.id, selections);
        player.idMark.setMenuType(MENU_THAN_LINH_DROP_RATE);

        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Tỉ lệ đồ Thần Linh");
            msg.writer().writeByte(selections.size());
            for (int i = 0; i < selections.size(); i++) {
                Boss boss = selections.get(i);
                int rate = rateManager.getEffectiveRate(boss);
                boolean customized = rateManager.getConfiguredRate(boss) != null;

                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                msg.writer().writeUTF("Thần Linh: " + rate + "%" + (customized ? " (đã chỉnh)" : " (mặc định)"));
                msg.writer().writeUTF(boss.zone == null
                        ? "Chưa xuất hiện"
                        : boss.zone.map.mapName + " (" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.error("Lỗi mở danh sách tỉ lệ Thần Linh: " + e.getMessage() + "\n");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public Boss getThanLinhDropRateSelection(Player player, int selection) {
        List<Boss> selections = ADMIN_DROP_RATE_SELECTIONS.remove(player.id);
        if (selections == null || selection < 0 || selection >= selections.size()) {
            return null;
        }
        return selections.get(selection);
    }

    public Boss getBossById(int bossId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return this.bosses.stream().filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie()).findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream().filter(boss -> boss.id < -100_000_000 && !boss.isDie()).findFirst().orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && boss.zone != null && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst().orElse(null);
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                for (Boss boss : this.bosses) {
                    boss.update();
                }
                Thread.sleep(1500 - (System.currentTimeMillis() - st));
            } catch (Exception ignored) {
            }
        }
    }
}
