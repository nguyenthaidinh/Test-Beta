package nro.models.boss.event_trung_thu;


import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossesData;
import nro.models.boss.BossID;
import nro.models.boss.Boss_Manager.TrungThuEventManager;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstMap;
import static nro.models.consts.BossType.TRUNGTHU_EVENT;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.map.service.ChangeMapService;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.services_dungeon.AncientCastleService;
import nro.models.utils.Util;

public class KhiDot extends Boss {

    public KhiDot() throws Exception {
        this(BossID.KHIDOT, BossesData.KHIDOT);
    }

    public KhiDot(int bossId, BossData bossData) throws Exception {
        super(TRUNGTHU_EVENT, bossId, true, true, bossData);
    }

    @Override
    public void reward(Player plKill) {
        ItemMap it = new ItemMap(this.zone, 1045, 5, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                this.location.y - 24), -1);
        Service.gI().dropItemMap(this.zone, it);
        AncientCastleService.gI().dropCastleBossReward(this, plKill);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 7);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 1;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }

    private long st;

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void leaveMap() {
        if (isThanhCoBoss()) {
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
            TrungThuEventManager.gI().removeBoss(this);
            this.dispose();
            return;
        }
        super.leaveMap();
    }

    private boolean isThanhCoBoss() {
        return this.zone != null && this.zone.map != null && this.zone.map.mapId == ConstMap.THANH_CO_1;
    }
}
