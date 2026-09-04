package nro.models.boss.ngu_hanh_son;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.AppearType;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstMap;
import nro.models.consts.ConstPlayer;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.player.Player;
import nro.models.services.PlayerService;
import nro.models.skill.Skill;
import nro.models.utils.Util;

/**
 * Chuỗi boss Ngũ Hành Sơn: Bát Giới (map 123) -> Ngộ Không (map 124).
 * Database nối đường đi theo thứ tự 123 -> 124 -> 122. NPC Ngộ Không đứng ở
 * map thứ ba (map 122).
 *
 * Dùng chung một đối tượng boss có hai cấp để bảo đảm Ngộ Không không thể tự
 * xuất hiện khi Bát Giới chưa bị tiêu diệt và không tạo trùng nhiều chuỗi boss.
 */
public final class NguHanhSonBoss extends Boss {

    private static final int BAT_GIOI_LEVEL = 0;
    private static final int NGO_KHONG_LEVEL = 1;

    private static final long BAT_GIOI_HP = 5_000_000_000L;
    private static final long NGO_KHONG_HP = 30_000_000_000L;
    private static final long BAT_GIOI_DAMAGE_LIMIT = 10_000L;

    private static final int BAT_GIOI_BASE_DAMAGE = 1_000_000;
    private static final int NGO_KHONG_PUNCH_DAMAGE = 3_000_000;

    /**
     * 150% giáp được quy đổi theo công thức giáp tăng thêm:
     * damage nhận = damage gốc * 100 / (100 + giáp).
     * Vì vậy 150% giáp giảm 60% sát thương, không gây lỗi sát thương âm/bất tử.
     */
    private static final int NGO_KHONG_ARMOR_PERCENT = 150;

    private static final long NGO_KHONG_HEAL_INTERVAL_MS = 5_000L;
    private static final int NGO_KHONG_HEAL_PERCENT = 1;
    private static final int CHAIN_RESTART_SECONDS = 300;

    private long lastTimeNgoKhongHeal;

    public NguHanhSonBoss() throws Exception {
        super(BossID.NGU_HANH_SON_CHAIN, createBatGioiData(), createNgoKhongData());
    }

    private static BossData createBatGioiData() {
        return new BossData(
                "Bát Giới",
                ConstPlayer.TRAI_DAT,
                // Tạo hình của item 548: đầu 465, thân 466, chân 464.
                new short[]{465, 466, 464, -1, -1, -1},
                BAT_GIOI_BASE_DAMAGE,
                // BossData chỉ chứa int; HP long thật được gán lại trong initBase().
                new int[]{1},
                new int[]{ConstMap.NGU_HANH_SON_123},
                new int[][]{{Skill.DRAGON, 1}},
                new String[]{"|-1|Ta sẽ không để các ngươi vượt qua!"},
                new String[]{},
                new String[]{},
                CHAIN_RESTART_SECONDS
        );
    }

    private static BossData createNgoKhongData() {
        return new BossData(
                "Ngộ Không",
                ConstPlayer.TRAI_DAT,
                // Tạo hình của item 547: đầu 462, thân 463, chân 464.
                new short[]{462, 463, 464, -1, -1, -1},
                NGO_KHONG_PUNCH_DAMAGE,
                // BossData chỉ chứa int; HP long thật được gán lại trong initBase().
                new int[]{1},
                new int[]{ConstMap.NGU_HANH_SON_124},
                new int[][]{
                    // Đấm Dragon cấp 1 dùng đúng sức đánh nền 3 triệu.
                    {Skill.DRAGON, 1},
                    {Skill.KAMEJOKO, 1},
                    {Skill.THAI_DUONG_HA_SAN, 7, 30_000}
                },
                new String[]{"|-1|Khá lắm! Để Tôn Ngộ Không ta tiếp chiêu."},
                new String[]{},
                new String[]{},
                CHAIN_RESTART_SECONDS,
                AppearType.ANOTHER_LEVEL
        );
    }

    @Override
    public void initBase() {
        super.initBase();

        long maxHp = isBatGioi() ? BAT_GIOI_HP : NGO_KHONG_HP;
        this.nPoint.hpg = maxHp;
        this.nPoint.hpMax = maxHp;
        this.nPoint.hp = maxHp;
        this.nPoint.setScaleClientHpToIntRange(true);

        // Miễn nhiễm riêng Thái Dương Hạ San, không miễn nhiễm các khống chế khác.
        this.nPoint.khangTDHS = true;
        this.nPoint.defg = 0;
        this.nPoint.def = 0;
        this.nPoint.tlGiap = 0;
        this.nPoint.tlNeDon = 0;
        this.nPoint.crit = 0;
        this.nPoint.isCrit = false;
        this.nPoint.isCrit100 = false;
        this.nPoint.fixedBossMeleeDamage = isNgoKhong()
                ? NGO_KHONG_PUNCH_DAMAGE : 0;

        this.lastTimeNgoKhongHeal = System.currentTimeMillis();
    }

    @Override
    public void update() {
        // Giữ cờ kháng TDHS kể cả khi một hiệu ứng khác buộc tính lại chỉ số boss.
        this.nPoint.khangTDHS = true;
        this.nPoint.fixedBossMeleeDamage = isNgoKhong()
                ? NGO_KHONG_PUNCH_DAMAGE : 0;
        super.update();
        healNgoKhongContinuously();
    }

    private void healNgoKhongContinuously() {
        if (!isNgoKhong() || this.zone == null || this.isDie()
                || this.bossStatus != BossStatus.ACTIVE
                || !Util.canDoWithTime(this.lastTimeNgoKhongHeal, NGO_KHONG_HEAL_INTERVAL_MS)) {
            return;
        }

        long missingHp = this.nPoint.hpMax - this.nPoint.hp;
        if (missingHp > 0) {
            long healAmount = this.nPoint.hpMax * NGO_KHONG_HEAL_PERCENT / 100L;
            PlayerService.gI().hoiPhuc(this, Math.min(healAmount, missingHp), 0);
        }
        this.lastTimeNgoKhongHeal = System.currentTimeMillis();
    }

    @Override
    public synchronized int injured(Player attacker, long damage,
            boolean piercing, boolean isMobAttack) {
        if (this.isDie() || damage <= 0) {
            return 0;
        }

        if (isBatGioi()) {
            // Mọi nguồn sát thương đều bị giới hạn tối đa 10.000 cho mỗi lần đánh.
            return super.injured(attacker, Math.min(damage, BAT_GIOI_DAMAGE_LIMIT),
                    piercing, isMobAttack);
        }

        int effectiveArmor = getEffectiveDamageReductionPercent(
                attacker, NGO_KHONG_ARMOR_PERCENT);
        long reducedDamage = Math.max(1L,
                damage * 100L / (100L + effectiveArmor));
        return super.injured(attacker, reducedDamage, true, isMobAttack);
    }

    @Override
    public void leaveMap() {
        if (isBatGioi()) {
            moveChainToNgoKhongMap();
            return;
        }

        // Ngộ Không chết: kết thúc lượt hiện tại; sau thời gian nghỉ chuỗi quay
        // lại Bát Giới tại map 123.
        this.zoneFinal = null;
        super.leaveMap();
    }

    private void moveChainToNgoKhongMap() {
        int zoneId = this.zone == null ? 0 : this.zone.zoneId;
        Zone nextZone = MapService.gI().getZoneByMapIDAndZoneID(
                ConstMap.NGU_HANH_SON_124, zoneId);
        if (nextZone == null) {
            nextZone = MapService.gI().getZoneByMapIDAndZoneID(
                    ConstMap.NGU_HANH_SON_124, 0);
        }

        if (this.zone != null) {
            ChangeMapService.gI().exitMap(this);
        }
        this.zone = null;
        this.lastZone = null;
        this.playerTarger = null;
        this.zoneFinal = nextZone;
        this.changeStatus(BossStatus.RESPAWN);
    }

    private boolean isBatGioi() {
        return this.currentLevel == BAT_GIOI_LEVEL;
    }

    private boolean isNgoKhong() {
        return this.currentLevel == NGO_KHONG_LEVEL;
    }
}
