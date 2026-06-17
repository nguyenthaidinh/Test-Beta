package nro.models.mob;

import nro.models.player.Player;

public class BigBoss extends Mob {

    public int action = 0;

    public long lastBigBossAttackTime;

    public BigBoss(Mob mob) {
        super(mob);
    }

    @Override
    public void update() {
        if (zone.isGoldenFriezaAlive) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        effectSkill.update();
        attack();
    }

}
