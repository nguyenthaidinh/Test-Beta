package nro.models.boss.gokuvegeta;

import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.PlayerService;
import nro.models.utils.Util;

public class BlackGoku extends Boss {

    private static final int ESCAPE_HP_PERCENT = 20;
    private static final int ESCAPE_HEAL_PERCENT = 70;
    private static final int DAMAGE_REDUCTION_PERCENT = 40;
    private static final int ADDITIONAL_DAMAGE_REDUCTION_PERCENT = 60;

    private boolean usedEscapeSkill;

    public BlackGoku() throws Exception {
        super(BossID.HTTV_BLACK_GOKU, false, true, BossesData.HTTV_BLACK_GOKU);
    }

    @Override
    public void reward(Player plKill) {
        HttvBossReward.drop(this, plKill);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xi hut");
            return 0;
        }

        damage -= damage * getEffectiveDamageReductionPercent(plAtt, DAMAGE_REDUCTION_PERCENT) / 100;
        damage -= damage * getEffectiveDamageReductionPercent(plAtt, ADDITIONAL_DAMAGE_REDUCTION_PERCENT) / 100;
        damage = this.nPoint.subDameInjureWithDeff(damage - Util.nextInt(100000));
        if (!piercing && this.effectSkill != null && this.effectSkill.isShielding) {
            if (damage > this.nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }

        if (shouldEscapeAfter(damage)) {
            long damageToTake = Math.max(0, this.nPoint.hp - 1);
            if (damageToTake > 0) {
                this.nPoint.subHP(damageToTake);
            }
            tryUseEscapeSkill();
            return (int) Math.min(damageToTake, Integer.MAX_VALUE);
        }

        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        } else {
            tryUseEscapeSkill();
        }
        return (int) Math.min(damage, Integer.MAX_VALUE);
    }

    @Override
    public void autoLeaveMap() {
        // Stay on Hanh Tinh Thuc Vat until defeated; REST_2_H controls respawn.
    }

    @Override
    public void joinMap() {
        this.usedEscapeSkill = false;
        if (this.currentLevel >= 0 && this.currentLevel < this.data.length) {
            this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        }
        super.joinMap();
    }

    private void tryUseEscapeSkill() {
        if (!canUseEscapeSkill()) {
            return;
        }
        if (this.nPoint.hp <= 0 || this.nPoint.hp > this.nPoint.hpMax * ESCAPE_HP_PERCENT / 100) {
            return;
        }

        usedEscapeSkill = true;
        this.chat("Ta se khong chet o noi nay dau!");
        Zone escapeZone = getEscapeZone();
        if (escapeZone != null) {
            int x = escapeZone.map.mapWidth > 100 ? Util.nextInt(100, escapeZone.map.mapWidth - 100) : Util.nextInt(100);
            int y = escapeZone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, escapeZone, x, y);
        }
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax * ESCAPE_HEAL_PERCENT / 100, 0);
    }

    private boolean shouldEscapeAfter(long damage) {
        return canUseEscapeSkill() && this.nPoint.hp - damage <= this.nPoint.hpMax * ESCAPE_HP_PERCENT / 100;
    }

    private boolean canUseEscapeSkill() {
        return !usedEscapeSkill && this.zone != null && this.effectSkill != null && !isControlled();
    }

    private boolean isControlled() {
        return this.effectSkill.isHaveEffectSkill()
                || this.effectSkill.isBinh
                || this.effectSkill.isSocola
                || this.effectSkill.isChibi;
    }

    private Zone getEscapeZone() {
        List<Zone> zones = this.zone.map.zones;
        if (zones == null || zones.size() <= 1) {
            return null;
        }
        int start = Util.nextInt(0, zones.size() - 1);
        for (int i = 0; i < zones.size(); i++) {
            Zone zoneEscape = zones.get((start + i) % zones.size());
            if (!zoneEscape.equals(this.zone) && zoneEscape.isBossCanJoin(this)) {
                return zoneEscape;
            }
        }
        return null;
    }
}
