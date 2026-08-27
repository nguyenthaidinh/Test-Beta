package nro.models.boss.ghost;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstItem;
import nro.models.consts.ConstPlayer;
import nro.models.interfaces.ControlEffectImmune;
import nro.models.map.ItemMap;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.GlobalSkyService;
import nro.models.services.HellWolfPetService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.skill.Skill;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 * Năm Boss Vương của sự kiện Hồn Ma. Mỗi loại là một boss độc lập, dùng trực
 * tiếp bộ head/body/leg của các item 1087-1091 và cùng xuất hiện theo đợt 2 giờ.
 */
public final class RoyalGhostBoss extends Boss implements ControlEffectImmune {

    private static final long MAX_HP = 10_000_000_000L;
    private static final int DAMAGE = 300_000;
    private static final int DAMAGE_REDUCTION_PERCENT = 60;
    private static final int THAN_LINH_DROP_RATE = 100;
    private static final int DA_NGU_SAC_DROP_RATE = 50;
    private static final int DA_NGU_SAC_MIN_QUANTITY = 1;
    private static final int DA_NGU_SAC_MAX_QUANTITY = 5;
    private static final int HELL_WOLF_DROP_RATE = 20;
    private static final int DRAGON_BALL_DROP_RATE = 50;
    private static final long FIRST_PHASE_HP = 2_000_000_000L;
    private static final long SECOND_PHASE_HP = 800_000_000L;
    private static final long FIRST_RECOVERY_TARGET_HP = MAX_HP;
    private static final long SECOND_RECOVERY_TARGET_HP = 7_000_000_000L;
    private static final int RESPAWN_SECONDS = 2 * 60 * 60;
    private static final long RESPAWN_WAVE_MILLIS = RESPAWN_SECONDS * 1_000L;

    private static final Object WAVE_LOCK = new Object();
    private static boolean eventWasDark;
    private static long waveStartTime;

    // Các map thường thuộc ba hành tinh gốc, không lấy phó bản/map sự kiện riêng.
    private static final int[] THREE_PLANET_MAPS = {
        // Trái Đất
        0, 1, 2, 3, 4, 5, 6, 24, 27, 28, 29, 30, 42, 47,
        // Namek
        7, 8, 9, 10, 11, 12, 13, 25, 31, 32, 33, 34, 43,
        // Xayda
        14, 15, 16, 17, 18, 19, 20, 26, 35, 36, 37, 38, 44
    };

    private static final int[] DRAGON_BALL_IDS = {
        ConstItem.NGOC_RONG_1_SAO,
        ConstItem.NGOC_RONG_2_SAO,
        ConstItem.NGOC_RONG_3_SAO,
        ConstItem.NGOC_RONG_4_SAO,
        ConstItem.NGOC_RONG_5_SAO,
        ConstItem.NGOC_RONG_6_SAO,
        ConstItem.NGOC_RONG_7_SAO
    };

    private final int royalType;
    private boolean firstRecoveryUsed;
    private boolean secondRecoveryUsed;
    private long recoveryTargetHp;

    public RoyalGhostBoss(int royalType) throws Exception {
        super(royalType, true, false, createData(royalType));
        this.royalType = royalType;
    }

    public int getRoyalType() {
        return royalType;
    }

    private static BossData createData(int royalType) {
        return switch (royalType) {
            // Item 1087 - Tanjiro
            case BossID.GHOST_KING_CAY -> createData(
                    "Cầy Vương", ConstPlayer.XAYDA, 1119, 1120, 1121, Skill.GALICK);
            // Item 1088 - Inosuke Hashibira
            case BossID.GHOST_KING_NGAO -> createData(
                    "Ngao Vương", ConstPlayer.NAMEC, 1122, 1123, 1124, Skill.MASENKO);
            // Item 1089 - Inosuke
            case BossID.GHOST_KING_ALO_VU_A -> createData(
                    "Alo Vũ à", ConstPlayer.TRAI_DAT, 1131, 1132, 1133, Skill.KAMEJOKO);
            // Item 1090 - Zenitsu
            case BossID.GHOST_KING_SVK -> createData(
                    "Svk Vương", ConstPlayer.TRAI_DAT, 1125, 1126, 1127, Skill.KAMEJOKO);
            // Item 1091 - Nezuko
            case BossID.GHOST_KING_NEZUKO -> createData(
                    "Nezuko Vương", ConstPlayer.TRAI_DAT, 1128, 1129, 1130, Skill.DRAGON);
            default -> throw new IllegalArgumentException("Loại Boss Vương không hợp lệ: " + royalType);
        };
    }

    private static BossData createData(String name, byte gender, int head, int body,
            int leg, int skillId) {
        return new BossData(
                name,
                gender,
                new short[]{(short) head, (short) body, (short) leg, -1, -1, -1},
                DAMAGE,
                new int[]{1}, // HP thật được gán bằng long trong initBase().
                THREE_PLANET_MAPS,
                new int[][]{
                    {skillId, 7, 1_000},
                    {Skill.KHIEN_NANG_LUONG, 7, 1_000_000},
                    {Skill.TAI_TAO_NANG_LUONG, 7, 1_000_000},
                    {Skill.THAI_DUONG_HA_SAN, 7, 1_000_000}
                },
                new String[]{},
                new String[]{},
                new String[]{},
                RESPAWN_SECONDS
        );
    }

    @Override
    public void initBase() {
        super.initBase();
        this.nPoint.hpg = MAX_HP;
        this.nPoint.hpMax = MAX_HP;
        this.nPoint.hp = MAX_HP;
        this.nPoint.setScaleClientHpToIntRange(true);
        this.nPoint.defg = 0;
        this.nPoint.def = 0;
        this.nPoint.tlNeDon = 0;
        this.firstRecoveryUsed = false;
        this.secondRecoveryUsed = false;
        this.recoveryTargetHp = 0;
        this.effectSkill.isCharging = false;
        this.effectSkill.countCharging = 0;
        this.effectSkill.isShielding = false;
    }

    @Override
    public void update() {
        boolean dark = GlobalSkyService.gI().isDark();
        observeEventState(dark);
        if (!dark) {
            hideUntilAdminDarkensSky();
            return;
        }
        super.update();
        updateRecoveryState();
    }

    @Override
    public void active() {
        if (this.recoveryTargetHp > 0) {
            return;
        }
        super.active();
    }

    /**
     * Chỉ cho AI chọn chiêu tấn công chính. Ba kỹ năng đặc biệt phía sau chỉ
     * được gọi đúng tại hai mốc HP, không bị chọn ngẫu nhiên khi đánh thường.
     */
    @Override
    public void attack() {
        if (!Util.canDoWithTime(this.lastTimeAttack, 100)
                || this.typePk != ConstPlayer.PK_ALL || this.playerSkill.skills.isEmpty()) {
            return;
        }
        this.lastTimeAttack = System.currentTimeMillis();
        try {
            Player target = getPlayerAttack();
            if (target == null || target.isDie()) {
                return;
            }
            this.playerSkill.skillSelect = this.playerSkill.skills.get(0);
            if (Util.getDistance(this, target) <= this.getRangeCanAttackWithSkillSelect()) {
                if (Util.isTrue(5, 20)) {
                    if (SkillUtil.isUseSkillChuong(this)) {
                        this.moveTo(target.location.x + Util.getOne(-1, 1) * Util.nextInt(20, 200),
                                Util.nextInt(10) % 2 == 0
                                        ? target.location.y : target.location.y - Util.nextInt(0, 70));
                    } else {
                        this.moveTo(target.location.x + Util.getOne(-1, 1) * Util.nextInt(10, 40),
                                Util.nextInt(10) % 2 == 0
                                        ? target.location.y : target.location.y - Util.nextInt(0, 50));
                    }
                }
                SkillService.gI().useSkill(this, target, null, -1, null);
                checkPlayerDie(target);
            } else if (Util.isTrue(1, 2)) {
                this.moveToPlayer(target);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public synchronized int injured(Player attacker, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie() || damage <= 0) {
            return 0;
        }

        // Hai lần hồi phục là các chuyển pha bắt buộc. Trong lúc đang tái tạo,
        // boss không nhận thêm sát thương để đòn xuyên khiên hoặc đòn kế tiếp
        // không thể giết boss trước khi hồi đủ HP của giai đoạn.
        if (this.recoveryTargetHp > 0) {
            return 0;
        }

        int reduction = getEffectiveDamageReductionPercent(attacker, DAMAGE_REDUCTION_PERCENT);
        long reducedDamage = Math.max(1L, damage * (100L - reduction) / 100L);

        // Khiên giai đoạn một bảo vệ theo đúng cơ chế khiên năng lượng hiện có.
        if (!piercing && this.effectSkill != null && this.effectSkill.isShielding) {
            if (reducedDamage > this.nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            reducedDamage = 1;
        }

        long phaseThreshold = getPendingPhaseThreshold();
        if (phaseThreshold > 0 && this.nPoint.hp - reducedDamage <= phaseThreshold) {
            reducedDamage = Math.max(0L, this.nPoint.hp - phaseThreshold);
        }

        int actualDamage = super.injured(attacker, reducedDamage, true, isMobAttack);
        if (!this.isDie()) {
            triggerPendingPhase();
        }
        return actualDamage;
    }

    @Override
    public void reward(Player killer) {
        super.reward(killer);
        if (killer == null || this.zone == null || this.zone.map == null) {
            return;
        }

        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        // Bốn phần thưởng có tỉ lệ độc lập, một boss có thể rơi nhiều loại cùng lúc.
        if (Util.isTrue(THAN_LINH_DROP_RATE, 100)) {
            ItemMap thanLinh = ItemService.gI().randDoTLBoss(this.zone, 1,
                    randomDropX(x), y, killer.id);
            if (thanLinh != null) {
                Service.gI().dropItemMap(this.zone, thanLinh);
            }
        }

        if (Util.isTrue(DA_NGU_SAC_DROP_RATE, 100)) {
            int quantity = randomDaNguSacQuantity();
            dropItem(ConstItem.DA_NGU_SAC, quantity, randomDropX(x), y, killer.id);
        }

        if (Util.isTrue(HELL_WOLF_DROP_RATE, 100)) {
            ItemMap hellWolf = new ItemMap(this.zone, ConstItem.SOI_DIA_NGUC, 1,
                    randomDropX(x), y, killer.id);
            hellWolf.options.addAll(HellWolfPetService.gI().createInitialPetOptions());
            Service.gI().dropItemMap(this.zone, hellWolf);
        }

        if (Util.isTrue(DRAGON_BALL_DROP_RATE, 100)) {
            int dragonBallId = DRAGON_BALL_IDS[Util.nextInt(0, DRAGON_BALL_IDS.length - 1)];
            dropItem(dragonBallId, 1, randomDropX(x), y, killer.id);
        }
    }

    /** Đệ tử kết liễu thì toàn bộ quyền nhặt thưởng được chuyển về sư phụ. */
    @Override
    public void die(Player killer) {
        Player creditedKiller = killer instanceof Pet pet && pet.master != null
                ? pet.master : killer;
        super.die(creditedKiller);
    }

    @Override
    public void rest() {
        if (this.lastTimeRest <= 0
                || System.currentTimeMillis() >= nextRespawnWave(this.lastTimeRest)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    private void hideUntilAdminDarkensSky() {
        if (this.zone == null && this.bossStatus == BossStatus.REST
                && this.currentLevel == -1 && this.lastTimeRest == 0) {
            return;
        }
        if (this.zone != null) {
            stopPhaseEffects();
            ChangeMapService.gI().exitMap(this);
        }
        this.zone = null;
        this.lastZone = null;
        this.playerTarger = null;
        this.prepareBom = false;
        this.currentLevel = -1;
        this.lastTimeRest = 0;
        this.firstRecoveryUsed = false;
        this.secondRecoveryUsed = false;
        this.recoveryTargetHp = 0;
        this.changeStatus(BossStatus.REST);
    }

    private void dropItem(int itemId, int quantity, int x, int y, long ownerId) {
        Service.gI().dropItemMap(this.zone,
                new ItemMap(this.zone, itemId, quantity, x, y, ownerId));
    }

    private static int randomDropX(int centerX) {
        return centerX + Util.nextInt(-45, 45);
    }

    private static int randomDaNguSacQuantity() {
        return Util.nextInt(DA_NGU_SAC_MIN_QUANTITY, DA_NGU_SAC_MAX_QUANTITY);
    }

    private long getPendingPhaseThreshold() {
        if (this.recoveryTargetHp > 0) {
            return 0;
        }
        if (!this.firstRecoveryUsed) {
            return FIRST_PHASE_HP;
        }
        if (!this.secondRecoveryUsed) {
            return SECOND_PHASE_HP;
        }
        return 0;
    }

    private void triggerPendingPhase() {
        if (this.recoveryTargetHp > 0) {
            return;
        }
        if (!this.firstRecoveryUsed && this.nPoint.hp <= FIRST_PHASE_HP) {
            this.firstRecoveryUsed = true;
            this.chat("Khiên năng lượng! Tái tạo năng lượng!");
            activateEnergyShield();
            startEnergyRecovery(FIRST_RECOVERY_TARGET_HP);
            return;
        }
        if (this.firstRecoveryUsed && !this.secondRecoveryUsed
                && this.nPoint.hp <= SECOND_PHASE_HP) {
            this.secondRecoveryUsed = true;
            this.chat("Thái Dương Hạ San! Tái tạo năng lượng!");
            useSolarFlare();
            startEnergyRecovery(SECOND_RECOVERY_TARGET_HP);
        }
    }

    private void activateEnergyShield() {
        if (!selectSkill(Skill.KHIEN_NANG_LUONG)) {
            return;
        }
        EffectSkillService.gI().setStartShield(this);
        EffectSkillService.gI().sendEffectPlayer(this, this,
                EffectSkillService.TURN_ON_EFFECT, EffectSkillService.SHIELD_EFFECT);
    }

    private void useSolarFlare() {
        if (selectSkill(Skill.THAI_DUONG_HA_SAN)) {
            SkillService.gI().useSkill(this, null, null, -1, null);
        }
    }

    private void startEnergyRecovery(long targetHp) {
        this.recoveryTargetHp = Math.min(targetHp, this.nPoint.hpMax);
        if (!selectSkill(Skill.TAI_TAO_NANG_LUONG)) {
            this.nPoint.setHp(this.recoveryTargetHp);
            this.recoveryTargetHp = 0;
            Service.gI().Send_Info_NV(this);
            return;
        }
        this.effectSkill.countCharging = 0;
        EffectSkillService.gI().startCharge(this);
        Service.gI().sendEffAllPlayer(this, 284, 1, -1, -1);
    }

    private void updateRecoveryState() {
        if (this.recoveryTargetHp <= 0 || this.isDie()) {
            return;
        }
        if (this.nPoint.hp >= this.recoveryTargetHp) {
            boolean completedFirstRecovery = this.recoveryTargetHp == FIRST_RECOVERY_TARGET_HP;
            this.nPoint.setHp(this.recoveryTargetHp);
            EffectSkillService.gI().stopCharge(this);
            this.recoveryTargetHp = 0;
            if (completedFirstRecovery && this.effectSkill.isShielding) {
                EffectSkillService.gI().removeShield(this);
            }
            Service.gI().Send_Info_NV(this);
        } else if (!this.effectSkill.isCharging) {
            if (selectSkill(Skill.TAI_TAO_NANG_LUONG)) {
                this.effectSkill.countCharging = 0;
                EffectSkillService.gI().startCharge(this);
            }
        }
    }

    private boolean selectSkill(int templateId) {
        for (Skill skill : this.playerSkill.skills) {
            if (skill != null && skill.template != null && skill.template.id == templateId) {
                this.playerSkill.skillSelect = skill;
                return true;
            }
        }
        return false;
    }

    private void stopPhaseEffects() {
        if (this.effectSkill == null) {
            return;
        }
        if (this.effectSkill.isCharging) {
            EffectSkillService.gI().stopCharge(this);
        }
        if (this.effectSkill.isShielding) {
            EffectSkillService.gI().removeShield(this);
        }
    }

    private static void observeEventState(boolean dark) {
        synchronized (WAVE_LOCK) {
            if (dark && !eventWasDark) {
                eventWasDark = true;
                waveStartTime = System.currentTimeMillis();
            } else if (!dark && eventWasDark) {
                eventWasDark = false;
                waveStartTime = 0;
            }
        }
    }

    static long nextRespawnWave(long restTimeMillis) {
        synchronized (WAVE_LOCK) {
            if (restTimeMillis <= 0 || waveStartTime <= 0) {
                return 0;
            }
            long elapsed = Math.max(0L, restTimeMillis - waveStartTime);
            long waveNumber = Math.floorDiv(elapsed, RESPAWN_WAVE_MILLIS) + 1L;
            return waveStartTime + waveNumber * RESPAWN_WAVE_MILLIS;
        }
    }
}
