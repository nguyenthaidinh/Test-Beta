package nro.models.boss.gokuvegeta;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Util;

public class Goku extends Boss {

    public Goku() throws Exception {
        super(BossID.HTTV_GOKU, false, true, BossesData.HTTV_GOKU);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        if (this.zone != null) {
            Service.gI().changeFlag(this, 9);
        }
    }

    @Override
    public void autoLeaveMap() {
        // Stay on Hanh Tinh Thuc Vat until defeated; REST_2_H controls respawn.
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
        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1)) {
            this.chat("Xi hut");
            return 0;
        }

        damage = Math.max(1, damage * 30 / 100);
        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return this.nPoint.getClientDamage(damage);
    }
}
