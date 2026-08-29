package nro.models.boss.ghost;

import java.util.ArrayList;
import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstItem;
import nro.models.consts.ConstPlayer;
import nro.models.interfaces.ControlEffectImmune;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.network.Message;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.GlobalSkyService;
import nro.models.services.HellWolfPetService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.skill.Skill;
import nro.models.utils.Logger;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 * Năm Boss Vương của sự kiện Hồn Ma. Mỗi loại là một boss độc lập, dùng trực
 * tiếp bộ head/body/leg của các item 1087-1091 và cùng xuất hiện theo đợt 1 giờ.
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
    private static final long CAY_TRANSFORMATION_HP = 500_000_000L;
    private static final long CAY_TRANSFORMED_MAX_HP = 20_000_000_000L;
    private static final long CAY_TRANSFORMATION_CHAT_DELAY_MS = 2_000L;
    private static final long SVK_ESCAPE_DELAY_MS = 2_000L;
    private static final long REPEAT_CHAT_DELAY_MS = 2_000L;
    private static final long NGAO_INTRO_DELAY_MS = 2_000L;
    private static final long NGAO_MASS_KILL_INTERVAL_MS = 1_000L;
    private static final int NGAO_MASS_KILL_WAVES = 5;
    private static final int NGAO_QCKK_CHARGE_MS = 4_000;
    private static final String[] SVK_REPEAT_CHATS = {
        "Am bách am bách",
        "Tao cay thằng Sơn lắm rồi",
        "Cà phê không?",
        "Đi cà phê không?"
    };
    private static final String[] CAY_REPEAT_CHATS = {
        "Được ấy, gâu gâu.",
        "Xương rồng đơm lá đơm hoa",
        "Nước đong đầy trên cao nguyên đá",
        "Là ngày Cầy Vương trở về nhà"
    };
    private static final String[] NGAO_RAMPAGE_CHATS = {
        "Đang ngủ",
        "Dậy rồi",
        "Dậy từ lâu rồi",
        "Im im im tao thích im đấy"
    };
    private static final String[] NGAO_REPEAT_CHATS = {
        "Cái gì đang học bài hả?",
        "Đang ngủ",
        "Dậy rồi",
        "Dậy từ lâu rồi",
        "Im im im tao thích im đấy"
    };
    private static final String[] ALO_VU_A_REPEAT_CHATS = {
        "Alo Vũ à!"
    };
    private static final String[] NEZUKO_REPEAT_CHATS = {
        "Ta là Nezuko Vương!"
    };
    private static final int RESPAWN_SECONDS = 60 * 60;
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
    private boolean cayTransformationUsed;
    private boolean cayTransformationInProgress;
    private int cayTransformationStage;
    private long cayTransformationNextActionTime;
    private boolean svkEscapeUsed;
    private boolean svkEscapeInProgress;
    private long svkNextActionTime;
    private boolean ngaoRampageUsed;
    private boolean ngaoRampageInProgress;
    private int ngaoRampageStage;
    private int ngaoMassKillCount;
    private int ngaoRampageChatIndex;
    private long ngaoNextActionTime;
    private int repeatChatIndex;
    private long nextRepeatChatTime;

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
                    "Cầy Vương", ConstPlayer.XAYDA, 1119, 1120, 1121, Skill.GALICK, true);
            // Item 1088 - Inosuke Hashibira
            case BossID.GHOST_KING_NGAO -> createData(
                    "Ngao Vương", ConstPlayer.NAMEC, 1122, 1123, 1124,
                    Skill.MASENKO, false, true);
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
        return createData(name, gender, head, body, leg, skillId, false, false);
    }

    private static BossData createData(String name, byte gender, int head, int body,
            int leg, int skillId, boolean hasMonkeyTransformation) {
        return createData(name, gender, head, body, leg, skillId,
                hasMonkeyTransformation, false);
    }

    private static BossData createData(String name, byte gender, int head, int body,
            int leg, int skillId, boolean hasMonkeyTransformation, boolean hasQckk) {
        int[][] skills = hasMonkeyTransformation
                ? new int[][]{
                    {skillId, 7, 1_000},
                    {Skill.KHIEN_NANG_LUONG, 7, 1_000_000},
                    {Skill.TAI_TAO_NANG_LUONG, 7, 1_000_000},
                    {Skill.THAI_DUONG_HA_SAN, 7, 1_000_000},
                    {Skill.BIEN_KHI, 7, 1_000_000}
                }
                : hasQckk
                ? new int[][]{
                    {skillId, 7, 1_000},
                    {Skill.KHIEN_NANG_LUONG, 7, 1_000_000},
                    {Skill.TAI_TAO_NANG_LUONG, 7, 1_000_000},
                    {Skill.THAI_DUONG_HA_SAN, 7, 1_000_000},
                    {Skill.QUA_CAU_KENH_KHI, 7, 1_000_000}
                }
                : new int[][]{
                    {skillId, 7, 1_000},
                    {Skill.KHIEN_NANG_LUONG, 7, 1_000_000},
                    {Skill.TAI_TAO_NANG_LUONG, 7, 1_000_000},
                    {Skill.THAI_DUONG_HA_SAN, 7, 1_000_000}
                };
        return new BossData(
                name,
                gender,
                new short[]{(short) head, (short) body, (short) leg, -1, -1, -1},
                DAMAGE,
                new int[]{1}, // HP thật được gán bằng long trong initBase().
                THREE_PLANET_MAPS,
                skills,
                new String[]{},
                new String[]{},
                new String[]{},
                RESPAWN_SECONDS
        );
    }

    @Override
    public void initBase() {
        if (this.effectSkill != null) {
            this.effectSkill.isMonkey = false;
            this.effectSkill.levelMonkey = 0;
            this.effectSkill.lastTimeUpMonkey = 0;
            this.effectSkill.timeMonkey = 0;
        }
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
        this.cayTransformationUsed = false;
        this.cayTransformationInProgress = false;
        this.cayTransformationStage = 0;
        this.cayTransformationNextActionTime = 0;
        this.svkEscapeUsed = false;
        this.svkEscapeInProgress = false;
        this.svkNextActionTime = 0;
        this.ngaoRampageUsed = false;
        this.ngaoRampageInProgress = false;
        this.ngaoRampageStage = 0;
        this.ngaoMassKillCount = 0;
        this.ngaoRampageChatIndex = 0;
        this.ngaoNextActionTime = 0;
        this.repeatChatIndex = 0;
        this.nextRepeatChatTime = 0;
        this.playerSkill.prepareQCKK = false;
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
        // Pha khỉ của Cầy Vương kéo dài tới khi chết, không dùng thời hạn 120 giây của skill thường.
        if (isCayKing() && this.cayTransformationUsed
                && this.effectSkill != null && this.effectSkill.isMonkey) {
            this.effectSkill.lastTimeUpMonkey = System.currentTimeMillis();
        }
        super.update();
        updateRecoveryState();
        updateCayTransformationState();
        updateSvkSpecialState();
        updateNgaoRampageState();
        updateRepeatChat();
    }

    @Override
    public void active() {
        if (this.recoveryTargetHp > 0 || isSpecialTransitionInProgress()) {
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

        // SVK Vương né hoàn toàn ba loại chưởng thường: Kamejoko, Masenko và Antomic.
        if (isSvkKing() && isNormalEnergyBlast(attacker)) {
            return 0;
        }

        // Hai lần hồi phục là các chuyển pha bắt buộc. Trong lúc đang tái tạo,
        // boss không nhận thêm sát thương để đòn xuyên khiên hoặc đòn kế tiếp
        // không thể giết boss trước khi hồi đủ HP của giai đoạn.
        if (this.recoveryTargetHp > 0 || isSpecialTransitionInProgress()) {
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
        this.cayTransformationUsed = false;
        this.cayTransformationInProgress = false;
        this.cayTransformationStage = 0;
        this.cayTransformationNextActionTime = 0;
        this.svkEscapeUsed = false;
        this.svkEscapeInProgress = false;
        this.svkNextActionTime = 0;
        this.ngaoRampageUsed = false;
        this.ngaoRampageInProgress = false;
        this.ngaoRampageStage = 0;
        this.ngaoMassKillCount = 0;
        this.ngaoRampageChatIndex = 0;
        this.ngaoNextActionTime = 0;
        this.repeatChatIndex = 0;
        this.nextRepeatChatTime = 0;
        if (this.playerSkill != null) {
            this.playerSkill.prepareQCKK = false;
        }
        if (this.effectSkill != null) {
            this.effectSkill.isMonkey = false;
            this.effectSkill.levelMonkey = 0;
            this.effectSkill.lastTimeUpMonkey = 0;
            this.effectSkill.timeMonkey = 0;
        }
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
        if (this.recoveryTargetHp > 0 || isSpecialTransitionInProgress()) {
            return 0;
        }
        if (!this.firstRecoveryUsed) {
            return FIRST_PHASE_HP;
        }
        if (!this.secondRecoveryUsed) {
            return SECOND_PHASE_HP;
        }
        if (isCayKing() && !this.cayTransformationUsed) {
            return CAY_TRANSFORMATION_HP;
        }
        return 0;
    }

    private void triggerPendingPhase() {
        if (this.recoveryTargetHp > 0 || isSpecialTransitionInProgress()) {
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
            return;
        }
        if (isCayKing() && this.firstRecoveryUsed && this.secondRecoveryUsed
                && !this.cayTransformationUsed && this.nPoint.hp <= CAY_TRANSFORMATION_HP) {
            startCayTransformation();
        }
    }

    private boolean isCayKing() {
        return this.royalType == BossID.GHOST_KING_CAY;
    }

    private boolean isSvkKing() {
        return this.royalType == BossID.GHOST_KING_SVK;
    }

    private boolean isNgaoKing() {
        return this.royalType == BossID.GHOST_KING_NGAO;
    }

    private boolean isSpecialTransitionInProgress() {
        return this.cayTransformationInProgress || this.svkEscapeInProgress
                || this.ngaoRampageInProgress;
    }

    private boolean isNormalEnergyBlast(Player attacker) {
        return attacker != null
                && attacker.playerSkill != null
                && attacker.playerSkill.skillSelect != null
                && attacker.playerSkill.skillSelect.template != null
                && SkillUtil.isUseSkillChuong(attacker);
    }

    private void startCayTransformation() {
        this.cayTransformationUsed = true;
        this.cayTransformationInProgress = true;
        this.cayTransformationStage = 0;
        this.cayTransformationNextActionTime
                = System.currentTimeMillis() + CAY_TRANSFORMATION_CHAT_DELAY_MS;
        this.chat("Được ấy, gâu gâu.");
    }

    private void updateCayTransformationState() {
        if (!this.cayTransformationInProgress
                || System.currentTimeMillis() < this.cayTransformationNextActionTime) {
            return;
        }
        switch (this.cayTransformationStage) {
            case 0 -> {
                transformCayKingToMonkey();
                this.chat("Xương rồng đơm lá đơm hoa");
                this.cayTransformationStage = 1;
                this.cayTransformationNextActionTime
                        = System.currentTimeMillis() + CAY_TRANSFORMATION_CHAT_DELAY_MS;
            }
            case 1 -> {
                this.chat("Nước đong đầy trên cao nguyên đá");
                this.cayTransformationStage = 2;
                this.cayTransformationNextActionTime
                        = System.currentTimeMillis() + CAY_TRANSFORMATION_CHAT_DELAY_MS;
            }
            default -> {
                this.chat("Là ngày Cầy Vương trở về nhà");
                this.cayTransformationInProgress = false;
                this.cayTransformationStage = 3;
                this.cayTransformationNextActionTime = 0;
                this.nextRepeatChatTime = System.currentTimeMillis() + REPEAT_CHAT_DELAY_MS;
            }
        }
    }

    private void transformCayKingToMonkey() {
        if (selectSkill(Skill.BIEN_KHI)) {
            EffectSkillService.gI().startUseSkillMonkey(this);
        } else {
            this.effectSkill.isMonkey = true;
            this.effectSkill.levelMonkey = 7;
        }
        // Đây là pha cuối của Cầy Vương nên giữ hình khỉ tới khi boss chết hoặc sự kiện tắt.
        this.effectSkill.isMonkey = true;
        this.effectSkill.levelMonkey = 7;
        this.effectSkill.lastTimeUpMonkey = System.currentTimeMillis();
        this.effectSkill.timeMonkey = Integer.MAX_VALUE;
        this.nPoint.hpg = MAX_HP;
        Service.gI().point(this);
        this.nPoint.hpMax = CAY_TRANSFORMED_MAX_HP;
        this.nPoint.hp = CAY_TRANSFORMED_MAX_HP;
        this.nPoint.setScaleClientHpToIntRange(true);
        Service.gI().Send_Caitrang(this);
        Service.gI().Send_Info_NV(this);
    }

    private void startSvkEscape() {
        this.svkEscapeUsed = true;
        this.svkEscapeInProgress = true;
        this.svkNextActionTime = System.currentTimeMillis() + SVK_ESCAPE_DELAY_MS;
        this.chat("Có kỹ năng không?");
    }

    private void updateSvkSpecialState() {
        if (!isSvkKing()) {
            return;
        }
        if (this.isDie() || this.zone == null) {
            this.svkEscapeInProgress = false;
            this.svkNextActionTime = 0;
            return;
        }
        long now = System.currentTimeMillis();
        if (this.svkEscapeInProgress) {
            if (now < this.svkNextActionTime) {
                return;
            }
            relocateSvkAndHeal();
            this.svkEscapeInProgress = false;
            this.repeatChatIndex = 0;
            this.nextRepeatChatTime = 0;
            this.svkNextActionTime = 0;
        }
    }

    private void relocateSvkAndHeal() {
        Zone targetZone = getRandomDifferentZone();
        // Hồi đầy trước khi nạp boss vào khu mới để người chơi tại khu đích
        // nhìn thấy ngay đúng 10 tỷ HP, không thoáng thấy giá trị 7 tỷ của pha trước.
        this.nPoint.hpg = MAX_HP;
        this.nPoint.hpMax = MAX_HP;
        this.nPoint.hp = MAX_HP;
        this.nPoint.setScaleClientHpToIntRange(true);
        if (targetZone != null) {
            int x = targetZone.map.mapWidth > 200
                    ? Util.nextInt(100, targetZone.map.mapWidth - 100) : 100;
            int y = targetZone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, targetZone, x, y);
        }
        Service.gI().Send_Info_NV(this);
    }

    private Zone getRandomDifferentZone() {
        if (this.zone == null || this.zone.map == null) {
            return null;
        }
        List<Zone> zones = this.zone.map.zones;
        if (zones == null || zones.size() <= 1) {
            return null;
        }
        int start = Util.nextInt(0, zones.size() - 1);
        for (int i = 0; i < zones.size(); i++) {
            Zone candidate = zones.get((start + i) % zones.size());
            if (candidate != null && !candidate.equals(this.zone)
                    && candidate.isBossCanJoin(this)) {
                return candidate;
            }
        }
        return null;
    }

    private void startNgaoRampage() {
        this.ngaoRampageUsed = true;
        this.ngaoRampageInProgress = true;
        this.ngaoRampageStage = 0;
        this.ngaoMassKillCount = 0;
        this.ngaoRampageChatIndex = 0;
        this.ngaoNextActionTime = System.currentTimeMillis() + NGAO_INTRO_DELAY_MS;
        this.chat("Cái gì đang học bài hả?");
    }

    private void updateNgaoRampageState() {
        if (!isNgaoKing()) {
            return;
        }
        if (this.isDie() || this.zone == null) {
            this.ngaoRampageInProgress = false;
            this.ngaoNextActionTime = 0;
            if (this.playerSkill != null) {
                this.playerSkill.prepareQCKK = false;
            }
            return;
        }
        if (!this.ngaoRampageInProgress
                || System.currentTimeMillis() < this.ngaoNextActionTime) {
            return;
        }

        long now = System.currentTimeMillis();
        switch (this.ngaoRampageStage) {
            case 0 -> executeNgaoMassKillWave(now);
            case 1 -> prepareNgaoShieldAndQckk(now);
            default -> finishNgaoQckkRampage();
        }
    }

    private void executeNgaoMassKillWave(long now) {
        killAllPlayersInCurrentZone();
        if (this.ngaoRampageChatIndex < NGAO_RAMPAGE_CHATS.length) {
            this.chat(NGAO_RAMPAGE_CHATS[this.ngaoRampageChatIndex++]);
        }
        this.ngaoMassKillCount++;
        if (this.ngaoMassKillCount >= NGAO_MASS_KILL_WAVES) {
            this.ngaoRampageStage = 1;
        }
        this.ngaoNextActionTime = now + NGAO_MASS_KILL_INTERVAL_MS;
    }

    private void prepareNgaoShieldAndQckk(long now) {
        activateEnergyShield();
        if (selectSkill(Skill.QUA_CAU_KENH_KHI)) {
            this.playerSkill.prepareQCKK = true;
            this.playerSkill.lastTimePrepareQCKK = now;
            SkillService.gI().sendPlayerPrepareSkill(this, NGAO_QCKK_CHARGE_MS);
        }
        this.ngaoRampageStage = 2;
        this.ngaoNextActionTime = now + NGAO_QCKK_CHARGE_MS;
    }

    private void finishNgaoQckkRampage() {
        try {
            Player target = getRandomAlivePlayerInCurrentZone();
            if (selectSkill(Skill.QUA_CAU_KENH_KHI)) {
                // Gọi lần hai của QCKK để phát đúng hoạt ảnh ném cầu. Sát thương
                // toàn khu vẫn được chốt riêng bên dưới, không phụ thuộc phạm vi skill.
                boolean standardAnimationSent = target != null
                        && SkillService.gI().useSkill(this, target, null, -1, null);
                if (!standardAnimationSent) {
                    // Sau 5 đợt quét có thể không còn mục tiêu sống. Khi đó
                    // useSkill gốc không phát gói ném cầu, nên dùng một nhân vật
                    // vẫn còn trong khu làm đích hiển thị cho toàn bộ client.
                    sendNgaoQckkFallbackAnimation(getRandomPlayerInCurrentZone());
                }
            }
        } catch (Exception e) {
            Logger.logException(RoyalGhostBoss.class, e,
                    "Lỗi khi Ngao Vương ném QCKK");
        } finally {
            // Luôn thoát chuyển pha, kể cả dữ liệu skill/target phát sinh lỗi.
            if (this.playerSkill != null) {
                this.playerSkill.prepareQCKK = false;
            }
            this.ngaoRampageInProgress = false;
            this.ngaoRampageStage = 3;
            this.ngaoNextActionTime = 0;
            killAllPlayersInCurrentZone();
            this.nextRepeatChatTime = System.currentTimeMillis() + REPEAT_CHAT_DELAY_MS;
        }
    }

    private void updateRepeatChat() {
        if (this.zone == null || this.isDie()
                || this.bossStatus != BossStatus.ACTIVE
                || this.recoveryTargetHp > 0 || isSpecialTransitionInProgress()) {
            return;
        }
        String[] chats = getRepeatChats();
        if (chats.length == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextRepeatChatTime) {
            return;
        }
        this.repeatChatIndex %= chats.length;
        this.chat(chats[this.repeatChatIndex]);
        this.repeatChatIndex = (this.repeatChatIndex + 1) % chats.length;
        this.nextRepeatChatTime = now + REPEAT_CHAT_DELAY_MS;
    }

    private String[] getRepeatChats() {
        return switch (this.royalType) {
            case BossID.GHOST_KING_CAY -> CAY_REPEAT_CHATS;
            case BossID.GHOST_KING_NGAO -> NGAO_REPEAT_CHATS;
            case BossID.GHOST_KING_ALO_VU_A -> ALO_VU_A_REPEAT_CHATS;
            case BossID.GHOST_KING_SVK -> SVK_REPEAT_CHATS;
            case BossID.GHOST_KING_NEZUKO -> NEZUKO_REPEAT_CHATS;
            default -> new String[0];
        };
    }

    private Player getRandomAlivePlayerInCurrentZone() {
        if (this.zone == null) {
            return null;
        }
        List<Player> alivePlayers = new ArrayList<>();
        for (Player player : new ArrayList<>(this.zone.getPlayers())) {
            if (player != null && player.zone == this.zone && !player.isDie()) {
                alivePlayers.add(player);
            }
        }
        return alivePlayers.isEmpty()
                ? null : alivePlayers.get(Util.nextInt(0, alivePlayers.size() - 1));
    }

    private Player getRandomPlayerInCurrentZone() {
        if (this.zone == null) {
            return null;
        }
        List<Player> players = new ArrayList<>();
        for (Player player : new ArrayList<>(this.zone.getPlayers())) {
            if (player != null && player.zone == this.zone) {
                players.add(player);
            }
        }
        return players.isEmpty()
                ? null : players.get(Util.nextInt(0, players.size() - 1));
    }

    private void sendNgaoQckkFallbackAnimation(Player target) {
        if (target == null || this.playerSkill == null
                || this.playerSkill.skillSelect == null) {
            return;
        }
        Message message = null;
        try {
            message = new Message(-60);
            message.writer().writeInt((int) this.id);
            message.writer().writeByte(this.playerSkill.skillSelect.skillId);
            message.writer().writeByte(1);
            message.writer().writeInt((int) target.id);
            message.writer().writeByte(1);
            message.writer().writeByte(0);
            message.writer().writeInt(target.nPoint == null ? 0 : target.nPoint.getClientHp());
            message.writer().writeBoolean(true);
            message.writer().writeBoolean(false);
            Service.gI().sendMessAllPlayerInMap(this, message);
        } catch (Exception e) {
            Logger.logException(RoyalGhostBoss.class, e,
                    "Lỗi khi gửi hoạt ảnh QCKK dự phòng của Ngao Vương");
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    private void killAllPlayersInCurrentZone() {
        if (this.zone == null) {
            return;
        }
        Zone currentZone = this.zone;
        for (Player target : new ArrayList<>(currentZone.getPlayers())) {
            if (target != null && target.zone == currentZone && !target.isDie()) {
                try {
                    // Kiểm tra lại ngay trước khi xử lý vì người chơi có thread
                    // đổi khu riêng; không giết nhầm sau khi họ đã sang khu khác.
                    if (target.zone == currentZone && !target.isDie()) {
                        target.setDie();
                    }
                } catch (Exception e) {
                    // Một nhân vật lỗi trạng thái không được phép ngắt cả đợt
                    // quét hoặc khiến Ngao Vương kẹt vĩnh viễn trong chuyển pha.
                    Logger.logException(RoyalGhostBoss.class, e,
                            "Lỗi khi Ngao Vương quét người chơi trong khu");
                }
            }
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
            completeEnergyRecovery();
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
            completeEnergyRecovery();
        } else if (!this.effectSkill.isCharging) {
            if (selectSkill(Skill.TAI_TAO_NANG_LUONG)) {
                this.effectSkill.countCharging = 0;
                EffectSkillService.gI().startCharge(this);
            } else {
                // Không để boss kẹt bất tử ở trạng thái chuyển pha nếu dữ liệu
                // kỹ năng bị thiếu hoặc bị thay đổi khi máy chủ đang chạy.
                completeEnergyRecovery();
            }
        }
    }

    private void completeEnergyRecovery() {
        if (this.recoveryTargetHp <= 0) {
            return;
        }
        boolean completedFirstRecovery = this.recoveryTargetHp == FIRST_RECOVERY_TARGET_HP;
        boolean completedSecondRecovery = this.recoveryTargetHp == SECOND_RECOVERY_TARGET_HP;
        this.nPoint.setHp(this.recoveryTargetHp);
        if (this.effectSkill != null && this.effectSkill.isCharging) {
            EffectSkillService.gI().stopCharge(this);
        }
        this.recoveryTargetHp = 0;
        if (completedFirstRecovery && this.effectSkill != null && this.effectSkill.isShielding) {
            EffectSkillService.gI().removeShield(this);
        }
        Service.gI().Send_Info_NV(this);
        if (completedSecondRecovery && isSvkKing() && !this.svkEscapeUsed) {
            startSvkEscape();
        }
        if (completedSecondRecovery && isNgaoKing() && !this.ngaoRampageUsed) {
            startNgaoRampage();
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
        if (this.playerSkill != null) {
            this.playerSkill.prepareQCKK = false;
        }
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
