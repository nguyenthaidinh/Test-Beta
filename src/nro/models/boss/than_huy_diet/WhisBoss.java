package nro.models.boss.than_huy_diet;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.pirate.PirateBossRewards;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class WhisBoss extends Boss {

    private static final int DAMAGE_REDUCTION_PERCENT = 90;
    private static final int DODGE_PERCENT = 50;
    private static final int MIN_TELEPORT_DELAY = 3_000;
    private static final int MAX_TELEPORT_DELAY = 5_000;

    private long lastTeleport;
    private int teleportDelay = Util.nextInt(MIN_TELEPORT_DELAY, MAX_TELEPORT_DELAY);

    public WhisBoss() throws Exception {
        super(BossID.WHIS_BOSS, false, true, BossesData.WHIS_BOSS);
    }

    @Override
    public void initBase() {
        super.initBase();
        this.nPoint.tlNeDon = DODGE_PERCENT * 10;
    }

    @Override
    public void update() {
        removeCrowdControl();
        super.update();
    }

    @Override
    public void active() {
        teleportAroundMap();
        super.active();
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
        if (Util.isTrue(DODGE_PERCENT, 100)) {
            this.chat("Xí hụt.");
            return 0;
        }

        int effectiveReduction = getEffectiveDamageReductionPercent(plAtt, DAMAGE_REDUCTION_PERCENT);
        long reducedDamage = Math.max(1L, damage - damage * effectiveReduction / 100);
        return super.injured(plAtt, reducedDamage, true, isMobAttack);
    }

    private void teleportAroundMap() {
        if (this.zone == null || !Util.canDoWithTime(this.lastTeleport, this.teleportDelay)) {
            return;
        }

        int minX = 50;
        int maxX = Math.max(minX, this.zone.map.mapWidth - 50);
        int x = Util.nextInt(minX, maxX);
        int y = this.zone.map.yPhysicInTop(x, 100);
        Service.gI().setPos(this, x, y);

        this.lastTeleport = System.currentTimeMillis();
        this.teleportDelay = Util.nextInt(MIN_TELEPORT_DELAY, MAX_TELEPORT_DELAY);
    }

    private void removeCrowdControl() {
        if (this.effectSkill == null) {
            return;
        }

        EffectSkillService effectService = EffectSkillService.gI();
        if (this.effectSkill.anTroi) {
            Player holder = this.effectSkill.plTroi;
            if (holder != null && holder.effectSkill != null && holder.effectSkill.plAnTroi == this) {
                effectService.removeUseTroi(holder);
            } else {
                effectService.removeAnTroi(this);
            }
        }
        if (this.effectSkill.isStun) {
            effectService.removeStun(this);
        }
        if (this.effectSkill.isThoiMien) {
            effectService.removeThoiMien(this);
        }
        if (this.effectSkill.isBlindDCTT) {
            effectService.removeBlindDCTT(this);
        }
        if (this.effectSkill.isStone) {
            effectService.removeStone(this);
        }
        if (this.effectSkill.isSocola) {
            effectService.removeSocola(this);
        }
        if (this.effectSkill.isLamCham) {
            effectService.removeLamCham(this);
        }
        if (this.effectSkill.isMabuHold) {
            effectService.removeMabuHold(this);
        }
        if (this.effectSkill.isBinh) {
            effectService.BinhDown(this);
        }
    }
}
