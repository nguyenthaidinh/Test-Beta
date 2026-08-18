package nro.models.boss.than_huy_diet;

import java.util.ArrayList;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.pirate.PirateBossRewards;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.skill.Skill;
import nro.models.utils.Util;

public class BeerusBoss extends Boss {

    private static final int EVENT_POINTS = 50;
    private static final int DAMAGE_REDUCTION_PERCENT = 99;
    private static final int ADDITIONAL_DAMAGE_REDUCTION_PERCENT = 60;
    private static final int ANGRY_HP_PERCENT = 20;
    private static final int ANGRY_WARNING_TIME = 3_000;

    private boolean angry;
    private boolean angryAttackFinished;
    private long angryStartTime;

    public BeerusBoss() throws Exception {
        super(BossID.BEERUS_BOSS, false, true, BossesData.BEERUS_BOSS);
    }

    @Override
    public void initBase() {
        super.initBase();
        this.angry = false;
        this.angryAttackFinished = false;
        this.angryStartTime = 0;
    }

    @Override
    public void update() {
        removeBind();
        updateAngryState();
        super.update();
    }

    @Override
    public void active() {
        if (isChargingAngryAttack()) {
            return;
        }
        super.active();
    }

    @Override
    public void reward(Player plKill) {
        PirateBossRewards.drop(this, plKill, EVENT_POINTS);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie() || damage <= 0 || isChargingAngryAttack()) {
            return 0;
        }
        if (isImmuneDamage(plAtt)) {
            this.chat("Vô ích.");
            return 0;
        }

        int effectiveReduction = getEffectiveDamageReductionPercent(plAtt, DAMAGE_REDUCTION_PERCENT);
        long reducedDamage = Math.max(1L, damage * (100 - effectiveReduction) / 100);
        int additionalReduction = getEffectiveDamageReductionPercent(plAtt, ADDITIONAL_DAMAGE_REDUCTION_PERCENT);
        reducedDamage = Math.max(1L, reducedDamage * (100 - additionalReduction) / 100);
        if (!this.angry) {
            long angryHp = this.nPoint.hpMax * ANGRY_HP_PERCENT / 100;
            if (this.nPoint.hp - reducedDamage <= angryHp) {
                reducedDamage = Math.max(0L, this.nPoint.hp - angryHp);
            }
        }

        int actualDamage = super.injured(plAtt, reducedDamage, true, isMobAttack);
        if (!this.isDie() && !this.angry
                && this.nPoint.hp <= this.nPoint.hpMax * ANGRY_HP_PERCENT / 100) {
            startAngryState();
        }
        return actualDamage;
    }

    private boolean isImmuneDamage(Player attacker) {
        if (attacker == null || attacker.playerSkill == null
                || attacker.playerSkill.skillSelect == null
                || attacker.playerSkill.skillSelect.template == null) {
            return false;
        }
        int skillId = attacker.playerSkill.skillSelect.template.id;
        return skillId == Skill.TU_SAT || skillId == Skill.QUA_CAU_KENH_KHI;
    }

    private void startAngryState() {
        this.angry = true;
        this.angryStartTime = System.currentTimeMillis();
        this.chat("Các ngươi đã chọc giận Thần Hủy Diệt!");
    }

    private boolean isChargingAngryAttack() {
        return this.angry && !this.angryAttackFinished;
    }

    private void updateAngryState() {
        if (!isChargingAngryAttack()
                || !Util.canDoWithTime(this.angryStartTime, ANGRY_WARNING_TIME)) {
            return;
        }

        this.angryAttackFinished = true;
        this.chat("HAKAI!");
        if (this.zone == null) {
            return;
        }

        for (Player target : new ArrayList<>(this.zone.getNotBosses())) {
            if (target != null && target.zone == this.zone && !target.isDie()) {
                target.setDie();
            }
        }
    }

    private void removeBind() {
        if (this.effectSkill == null || !this.effectSkill.anTroi) {
            return;
        }

        Player holder = this.effectSkill.plTroi;
        EffectSkillService effectService = EffectSkillService.gI();
        if (holder != null && holder.effectSkill != null && holder.effectSkill.plAnTroi == this) {
            effectService.removeUseTroi(holder);
        } else {
            effectService.removeAnTroi(this);
        }
    }
}
