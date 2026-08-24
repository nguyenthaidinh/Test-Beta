package nro.models.boss.ghost;

import java.util.concurrent.atomic.AtomicInteger;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstItem;
import nro.models.consts.ConstPlayer;
import nro.models.map.ItemMap;
import nro.models.map.service.ChangeMapService;
import nro.models.managers.TopGhostHunter;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.services.GlobalSkyService;
import nro.models.services.ItemService;
import nro.models.services.HellWolfPetService;
import nro.models.services.Service;
import nro.models.skill.Skill;
import nro.models.utils.Util;

/**
 * Boss hồn ma xuất hiện độc lập ở ba hành tinh gốc.
 *
 * Phần nhân vật được dùng bộ part trong suốt; hình hiển thị thật nằm ở flag bag
 * 47/48/49. Mỗi instance dùng một id runtime riêng để nhiều boss không ghi đè
 * nhau trên client khi vô tình xuất hiện cùng map/khu.
 */
public class PlanetGhostBoss extends Boss {

    private static final long MAX_HP = 5_000_000_000L;
    private static final int DAMAGE = 300_000;
    private static final int RESPAWN_SECONDS = 30 * 60;
    private static final int THAN_LINH_DROP_RATE = 50;
    private static final int DRAGON_BALL_DROP_RATE = 70;
    private static final int FRESH_MEAT_MIN_QUANTITY = 1;
    private static final int FRESH_MEAT_MAX_QUANTITY = 20;
    private static final int[] DRAGON_BALL_IDS = {
        ConstItem.NGOC_RONG_1_SAO,
        ConstItem.NGOC_RONG_2_SAO,
        ConstItem.NGOC_RONG_3_SAO,
        ConstItem.NGOC_RONG_4_SAO,
        ConstItem.NGOC_RONG_5_SAO,
        ConstItem.NGOC_RONG_6_SAO,
        ConstItem.NGOC_RONG_7_SAO
    };
    private static final AtomicInteger NEXT_INSTANCE_ID = new AtomicInteger(-1_100_000);

    private final int ghostType;

    // Chỉ lấy map thường thuộc hành tinh gốc, không lấy nhà riêng/phó bản/sự kiện.
    private static final int[] EARTH_MAPS = {
        0, 1, 2, 3, 4, 5, 6, 24, 27, 28, 29, 30, 42, 47
    };
    private static final int[] NAMEC_MAPS = {
        7, 8, 9, 10, 11, 12, 13, 25, 31, 32, 33, 34, 43
    };
    private static final int[] XAYDA_MAPS = {
        14, 15, 16, 17, 18, 19, 20, 26, 35, 36, 37, 38, 44
    };

    // Các part này đều trong suốt ở toàn bộ frame head/body/leg.
    private static final short TRANSPARENT_HEAD = 406;
    private static final short TRANSPARENT_BODY = 470;
    private static final short TRANSPARENT_LEG = 471;

    public PlanetGhostBoss(int ghostType) throws Exception {
        super(NEXT_INSTANCE_ID.getAndDecrement(), true, false, createData(ghostType));
        this.ghostType = ghostType;
    }

    private static BossData createData(int ghostType) {
        return switch (ghostType) {
            case BossID.GHOST_SVK -> createData(
                    "svk", ConstPlayer.TRAI_DAT, (short) 47, EARTH_MAPS, Skill.KAMEJOKO);
            case BossID.GHOST_CAY_CON -> createData(
                    "cầy con", ConstPlayer.XAYDA, (short) 48, XAYDA_MAPS, Skill.GALICK);
            case BossID.GHOST_NGAO_CON -> createData(
                    "ngao con", ConstPlayer.NAMEC, (short) 49, NAMEC_MAPS, Skill.MASENKO);
            default -> throw new IllegalArgumentException("Loại boss hồn ma không hợp lệ: " + ghostType);
        };
    }

    private static BossData createData(String name, byte gender, short flagBag,
            int[] mapJoin, int skillId) {
        return new BossData(
                name,
                gender,
                new short[]{
                    TRANSPARENT_HEAD, TRANSPARENT_BODY, TRANSPARENT_LEG,
                    flagBag, -1, -1
                },
                DAMAGE,
                new int[]{1}, // HP thật được gán bằng long trong initBase().
                mapJoin,
                new int[][]{{skillId, 7, 1_000}},
                new String[]{},
                new String[]{},
                new String[]{},
                RESPAWN_SECONDS
        );
    }

    @Override
    public void initBase() {
        super.initBase();

        // BossData dùng int[] nên không chứa được 5 tỷ. Gán lại bằng long sau
        // khi calPoint() hoàn tất để giữ đúng HP phía server.
        this.nPoint.hpg = MAX_HP;
        this.nPoint.hpMax = MAX_HP;
        this.nPoint.hp = MAX_HP;
        this.nPoint.setScaleClientHpToIntRange(true);

        // Không giáp, không né và không có chỉ số phòng thủ ẩn.
        this.nPoint.defg = 0;
        this.nPoint.def = 0;
        this.nPoint.tlNeDon = 0;
    }

    @Override
    public void update() {
        // Chỉ trạng thái tối toàn server do admin điều khiển mới mở sự kiện.
        // Hiệu ứng tối tạm thời khi gọi Rồng Thần không thay đổi cờ này.
        if (!GlobalSkyService.gI().isDark()) {
            hideUntilAdminDarkensSky();
            return;
        }
        super.update();
    }

    @Override
    public void rest() {
        if (Util.canDoWithTime(lastTimeRest, secondsRest * 1000)
                && GhostBossSpawnLimiter.gI().tryReserveSpawn(ghostType)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    private void hideUntilAdminDarkensSky() {
        if (this.zone == null && this.bossStatus == BossStatus.REST && this.currentLevel == -1) {
            return;
        }
        if (this.zone != null) {
            ChangeMapService.gI().exitMap(this);
        }
        this.zone = null;
        this.lastZone = null;
        this.playerTarger = null;
        this.prepareBom = false;
        this.currentLevel = -1;
        this.lastTimeRest = 0;
        this.changeStatus(BossStatus.REST);
    }

    @Override
    public void reward(Player plKill) {
        super.reward(plKill);
        if (plKill == null || this.zone == null || this.zone.map == null) {
            return;
        }

        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // Ba phần thưởng có tỉ lệ độc lập; một lần hạ boss có thể nhận nhiều loại.
        if (isThanLinhDrop(THAN_LINH_DROP_RATE)) {
            ItemMap thanLinh = ItemService.gI().randDoTLBoss(this.zone, 1,
                    x + Util.nextInt(-30, 30), y, plKill.id);
            if (thanLinh != null) {
                Service.gI().dropItemMap(this.zone, thanLinh);
            }
        }

        if (Util.isTrue(DRAGON_BALL_DROP_RATE, 100)) {
            int dragonBallId = DRAGON_BALL_IDS[Util.nextInt(0, DRAGON_BALL_IDS.length - 1)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, dragonBallId, 1,
                    x + Util.nextInt(-30, 30), y, plKill.id));
        }

        // Luôn rơi đúng một stack Thịt tươi, số lượng ngẫu nhiên từ 1 đến 20.
        int freshMeatQuantity = randomFreshMeatQuantity();
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone,
                ConstItem.THIT_TUOI_NANG_CAP_SOI, freshMeatQuantity,
                x + Util.nextInt(-30, 30), y, plKill.id));

        // Mỗi khung 7 phút 12 giây được rơi tối đa 1 Hồn ma, đủ 200 khung trong ngày.
        if (GhostSoulDropLimiter.gI().tryReserveDrop()) {
            ItemMap soul = new ItemMap(this.zone, ConstItem.HON_MA, 1,
                    x + Util.nextInt(-30, 30), y, plKill.id);
            soul.options.add(HellWolfPetService.gI().createRandomSoulOption());
            Service.gI().dropItemMap(this.zone, soul);
        }
    }

    private static int randomFreshMeatQuantity() {
        return Util.nextInt(FRESH_MEAT_MIN_QUANTITY, FRESH_MEAT_MAX_QUANTITY);
    }

    /**
     * Không phát 60 thông báo tiêu diệt lên toàn server. Vẫn giữ xử lý nhiệm vụ
     * boss chung cho người kết liễu.
     */
    @Override
    public void die(Player plKill) {
        Player creditedKiller = resolveCreditedKiller(plKill);
        if (creditedKiller != null && !creditedKiller.isBot) {
            TopGhostHunter.getInstance().recordKill(creditedKiller);
            this.reward(creditedKiller);
        }
        this.changeStatus(BossStatus.DIE);
    }

    /** Đệ tử kết liễu vẫn cộng điểm và khóa phần thưởng cho sư phụ. */
    private Player resolveCreditedKiller(Player killer) {
        if (killer instanceof Pet pet && pet.master != null && pet.master.isPl()) {
            return pet.master;
        }
        return killer != null && killer.isPl() ? killer : null;
    }
}
