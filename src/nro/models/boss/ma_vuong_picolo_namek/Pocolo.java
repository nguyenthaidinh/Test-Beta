package nro.models.boss.ma_vuong_picolo_namek;

import java.util.ArrayList;
import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.EffectSkillService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.skill.Skill;
import nro.models.utils.Util;

public class Pocolo extends Boss {

    private static final long MAX_DAMAGE_TAKEN = 20_000_000L;
    private static final int[] CLONE_HP_PERCENTS = {70, 40, 20};
    private static final int CLONE_PER_WAVE = 2;
    private static final int CLONE_HP = 250_000_000;
    private static final int CLONE_DAMAGE = 10_000_000;

    private final List<PocoloClone> clones = new ArrayList<>();
    private int cloneWave;
    private long lastTimeSummonClone;
    private long lastTimeHavePlayer;

    public Pocolo() throws Exception {
        super(BossID.POCOLO_NAMEK, BossesData.POCOLO_NAMEK);
    }

    @Override
    public void respawn() {
        removeClones();
        this.cloneWave = 0;
        this.lastTimeSummonClone = 0;
        this.lastTimeHavePlayer = System.currentTimeMillis();
        super.respawn();
    }

    @Override
    public void joinMap() {
        super.joinMap();
        this.lastTimeHavePlayer = System.currentTimeMillis();
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        summonClonesIfNeeded();
        this.attack();
    }

    private void summonClonesIfNeeded() {
        if (this.zone == null || this.nPoint.hpMax <= 0 || this.cloneWave >= CLONE_HP_PERCENTS.length) {
            return;
        }
        int hpPercent = (int) ((long) this.nPoint.hp * 100 / this.nPoint.hpMax);
        if (hpPercent > CLONE_HP_PERCENTS[this.cloneWave] || !Util.canDoWithTime(this.lastTimeSummonClone, 5000)) {
            return;
        }
        cleanupClones();
        for (int i = 0; i < CLONE_PER_WAVE; i++) {
            try {
                PocoloClone clone = new PocoloClone(this, CLONE_HP, CLONE_DAMAGE);
                clone.changeStatus(BossStatus.RESPAWN);
                this.clones.add(clone);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.chat("Phan than cua ta se ket lieu cac nguoi!");
        this.lastTimeSummonClone = System.currentTimeMillis();
        this.cloneWave++;
    }

    private void cleanupClones() {
        this.clones.removeIf(clone -> clone == null || clone.zone == null || clone.isDie());
    }

    private void removeClones() {
        List<PocoloClone> currentClones = new ArrayList<>(this.clones);
        for (PocoloClone clone : currentClones) {
            if (clone != null) {
                clone.leaveMapNew();
            }
        }
        this.clones.clear();
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
        damage = this.nPoint.subDameInjureWithDeff(damage);
        if (!piercing && this.effectSkill.isShielding) {
            if (damage > this.nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }
        damage = Math.min(damage, MAX_DAMAGE_TAKEN);
        this.nPoint.subHP(damage);
        if (this.isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public void reward(Player plKill) {
        if (plKill == null) {
            return;
        }
        int diem = 25;
        plKill.event.addEventPoint(diem);
        Service.gI().sendThongBao(plKill, "+25 Point");
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, Util.nextInt(20000, 30001), x, y, plKill.id));
        if (isThanLinhDrop(30)) {
            ItemMap thanLinhDrop = ItemService.gI().randDoTLBoss(this.zone, 1, x + Util.nextInt(-50, 50), y, plKill.id);
            if (thanLinhDrop != null) {
                Service.gI().dropItemMap(this.zone, thanLinhDrop);
            }
        }
        if (Util.isTrue(3, 100)) {
            short[] doHuyDietIds = {650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662};
            short doHuyDietId = doHuyDietIds[Util.nextInt(doHuyDietIds.length)];
            Item doHuyDietItem = createBillHuyDietItem(doHuyDietId);
            if (doHuyDietItem != null) {
                ItemMap doHuyDietDrop = new ItemMap(this.zone, doHuyDietItem.template, 1, x + Util.nextInt(-50, 50), y, plKill.id);
                doHuyDietDrop.options.addAll(doHuyDietItem.itemOptions);
                Service.gI().dropItemMap(this.zone, doHuyDietDrop);
            }
        }
        super.reward(plKill);
    }

    private Item createBillHuyDietItem(short itemId) {
        Item item = null;
        for (Shop shop : Manager.SHOPS) {
            if (shop.tagName != null && shop.tagName.equals("BILL")) {
                ItemShop itemShop = shop.getItemShop(itemId);
                if (itemShop != null) {
                    item = ItemService.gI().createItemFromItemShop(itemShop);
                }
                break;
            }
        }
        if (item == null) {
            return null;
        }
        applyBillHuyDietOptions(item);
        return item;
    }

    private void applyBillHuyDietOptions(Item item) {
        int param = 0;
        if (item.template.level == 14) {
            int random = Util.nextInt(1, 100);
            if (random <= 1) {
                param = 15;
            } else if (random <= 15) {
                param = Util.nextInt(11, 14);
            } else if (random <= 35) {
                param = Util.nextInt(7, 10);
            } else if (random <= 60) {
                param = Util.nextInt(4, 6);
            } else {
                param = Util.nextInt(0, 3);
            }
        }

        List<Item.ItemOption> itemOptions = new ArrayList<>();
        if (!item.itemOptions.isEmpty()) {
            for (Item.ItemOption option : item.itemOptions) {
                if (item.template.level == 14 && canUpgradeBillHuyDietOption(option.optionTemplate.id) && param > 0) {
                    int optionId = option.optionTemplate.id;
                    int optionParam = option.param + (option.param * param) / 100;
                    itemOptions.add(new Item.ItemOption(optionId, optionParam));
                } else if (option.optionTemplate.id != 164) {
                    itemOptions.add(new Item.ItemOption(option.optionTemplate.id, option.param));
                }
            }
        } else {
            itemOptions.add(new Item.ItemOption(73, (short) 0));
        }
        itemOptions.add(new Item.ItemOption(30, (short) 0));

        if (item.template.level == 14) {
            int roll = Util.nextInt(3);
            switch (roll) {
                case 0 -> itemOptions.add(new Item.ItemOption(77, Util.nextInt(1, 5)));
                case 1 -> itemOptions.add(new Item.ItemOption(50, Util.nextInt(1, 3)));
                case 2 -> itemOptions.add(new Item.ItemOption(103, Util.nextInt(1, 5)));
            }
        }
        item.itemOptions.clear();
        item.itemOptions.addAll(itemOptions);
    }

    private boolean canUpgradeBillHuyDietOption(int optionId) {
        return optionId == 0 || optionId == 22 || optionId == 23 || optionId == 14
                || optionId == 27 || optionId == 28 || optionId == 47;
    }

    @Override
    public void die(Player plKill) {
        removeClones();
        super.die(plKill);
    }

    @Override
    public void autoLeaveMap() {
        if (this.zone == null) {
            return;
        }
        if (this.zone.getNumOfPlayers() > 0) {
            this.lastTimeHavePlayer = System.currentTimeMillis();
            return;
        }
        if (Util.canDoWithTime(this.lastTimeHavePlayer, 900000)) {
            this.leaveMapNew();
        }
    }

    @Override
    public void leaveMap() {
        removeClones();
        super.leaveMap();
    }
}

class PocoloClone extends Boss {

    private static final long MAX_DAMAGE_TAKEN = 10_000_000L;

    private Pocolo mainBoss;
    private long lastTimeHavePlayer;

    PocoloClone(Pocolo mainBoss, int hp, int dame) throws Exception {
        super(BossID.POCOLO_NAMEK_CLONE, true, false, createData(hp, dame));
        this.mainBoss = mainBoss;
        this.zone = mainBoss.zone;
        this.lastTimeHavePlayer = System.currentTimeMillis();
    }

    private static BossData createData(int hp, int dame) {
        return new BossData(
                "Phan than Pocolo Namek",
                ConstPlayer.NAMEC,
                new short[] { 739, 740, 741, -1, -1, -1 },
                dame,
                new int[] { hp },
                new int[] { 7 },
                new int[][] {
                        { Skill.DEMON, 7, 1000 },
                        { Skill.MASENKO, 7, 1000 },
                        { Skill.KAMEJOKO, 7, 1000 },
                        { Skill.ANTOMIC, 7, 1000 }, },
                new String[] {},
                new String[] { "|-1|Ta chi la cai bong cua Ma vuong." },
                new String[] {},
                60
        );
    }

    @Override
    public void joinMap() {
        if (this.mainBoss == null || this.mainBoss.zone == null || this.mainBoss.isDie()) {
            this.changeStatus(BossStatus.LEAVE_MAP);
            return;
        }
        this.zone = this.mainBoss.zone;
        int x = this.mainBoss.location.x + Util.nextInt(-150, 150);
        if (this.zone.map.mapWidth > 100) {
            x = Math.max(50, Math.min(this.zone.map.mapWidth - 50, x));
        }
        int y = this.zone.map.yPhysicInTop(x, this.mainBoss.location.y);
        ChangeMapService.gI().changeMap(this, this.zone, x, y);
        Service.gI().sendFlagBag(this);
        this.changeStatus(BossStatus.CHAT_S);
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
        damage = this.nPoint.subDameInjureWithDeff(damage);
        if (!piercing && this.effectSkill.isShielding) {
            if (damage > this.nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }
        damage = Math.min(damage, MAX_DAMAGE_TAKEN);
        this.nPoint.subHP(damage);
        if (this.isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) damage;
    }

    @Override
    public void reward(Player plKill) {
    }

    @Override
    public void die(Player plKill) {
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void autoLeaveMap() {
        if (this.mainBoss == null || this.mainBoss.zone == null || this.mainBoss.isDie()
                || this.zone == null || this.zone != this.mainBoss.zone) {
            this.leaveMapNew();
            return;
        }
        if (this.zone.getNumOfPlayers() > 0) {
            this.lastTimeHavePlayer = System.currentTimeMillis();
            return;
        }
        if (Util.canDoWithTime(this.lastTimeHavePlayer, 900000)) {
            this.leaveMapNew();
        }
    }

    @Override
    public void leaveMap() {
        if (this.zone != null) {
            ChangeMapService.gI().exitMap(this);
        }
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        BossManager.gI().removeBoss(this);
        this.mainBoss = null;
        this.dispose();
    }
}
