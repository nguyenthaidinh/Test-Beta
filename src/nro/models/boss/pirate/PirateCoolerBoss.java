package nro.models.boss.pirate;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;

public class PirateCoolerBoss extends Boss {

    private static final int EVENT_POINTS = 35;
    private static final int DAMAGE_REDUCTION_PERCENT = 90;
    private static final int ADDITIONAL_DAMAGE_REDUCTION_PERCENT = 70;

    public PirateCoolerBoss() throws Exception {
        super(BossID.COOLER_PIRATE, false, true, BossesData.COOLER_PIRATE);
    }

    @Override
    public void reward(Player plKill) {
        PirateBossRewards.drop(this, plKill, EVENT_POINTS);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie() || damage <= 0) {
            return 0;
        }
        int effectiveReduction = getEffectiveDamageReductionPercent(plAtt, DAMAGE_REDUCTION_PERCENT);
        long reducedDamage = Math.max(1L, damage * (100 - effectiveReduction) / 100);
        int additionalReduction = getEffectiveDamageReductionPercent(plAtt, ADDITIONAL_DAMAGE_REDUCTION_PERCENT);
        reducedDamage = Math.max(1L, reducedDamage * (100 - additionalReduction) / 100);
        return super.injured(plAtt, reducedDamage, true, isMobAttack);
    }
}
