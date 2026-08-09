package nro.models.boss.pirate;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;

public class PirateBlackBoss extends Boss {

    private static final int DAMAGE_REDUCTION_PERCENT = 90;

    public PirateBlackBoss() throws Exception {
        super(BossID.PIRATE_BLACK, false, true, BossesData.PIRATE_BLACK);
    }

    @Override
    public void reward(Player plKill) {
        PirateBossRewards.drop(this, plKill);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie() || damage <= 0) {
            return 0;
        }
        int effectiveReduction = getEffectiveDamageReductionPercent(plAtt, DAMAGE_REDUCTION_PERCENT);
        long reducedDamage = Math.max(1L, damage * (100 - effectiveReduction) / 100);
        return super.injured(plAtt, reducedDamage, true, isMobAttack);
    }
}
