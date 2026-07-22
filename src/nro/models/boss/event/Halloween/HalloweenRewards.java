package nro.models.boss.event.Halloween;

import nro.models.boss.Boss;
import nro.models.consts.ConstItem;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

public final class HalloweenRewards {

    private static final int GEM_ID = 77;

    private static final int OPTION_CAN_NOT_TRADE = 30;
    private static final int OPTION_EXPIRE_DAYS = 93;
    private static final int OPTION_DAMAGE_PERCENT = 50;
    private static final int OPTION_HP_PERCENT = 77;
    private static final int OPTION_KI_PERCENT = 103;
    private static final int OPTION_DEF_PERCENT = 94;
    private static final int OPTION_CRITICAL_DAMAGE_PERCENT = 5;
    private static final int OPTION_DAMAGE_BOSS_PERCENT = 204;
    private static final int HALLOWEEN_STAT_MIN = 25;
    private static final int HALLOWEEN_STAT_MAX = 50;
    private static final int HALLOWEEN_COSTUME_EXTRA_PERCENT = 30;

    private static final int[] COSTUME_REWARDS = {
        ConstItem.CAI_TRANG_BONG_BANG_HALLOWEEN,
        ConstItem.CAI_TRANG_VUA_QUY_SATAN_HALLOWEEN,
        ConstItem.CAI_TRANG_DRACULA_HALLOWEEN
    };

    private HalloweenRewards() {
    }

    static void dropBossReward(Boss boss, Player killer) {
        if (boss == null || boss.zone == null || killer == null) {
            return;
        }

        drop(boss, ConstItem.BI_NGO, Util.nextInt(30, 60), killer.id, 0);

        if (Util.isTrue(20, 100)) {
            drop(boss, ConstItem.THIEP_HALLOWEEN, 1, killer.id, -30);
        }
        if (Util.isTrue(10, 100)) {
            drop(boss, ConstItem.HOM_HALLOWEEN, 1, killer.id, 30);
        }
        if (Util.isTrue(2, 100)) {
            drop(boss, COSTUME_REWARDS[Util.nextInt(COSTUME_REWARDS.length)], 1, killer.id, -60);
        }
        if (Util.isTrue(1, 500)) {
            drop(boss, ConstItem.PET_BI_MA_VUONG, 1, killer.id, 60);
        }
    }

    public static Item createHalloweenBoxReward() {
        int roll = Util.nextInt(1, 1000);

        if (roll <= 10) {
            return createHalloweenPetReward();
        }
        if (roll <= 60) {
            return createHalloweenCostumeReward(15);
        }
        if (roll <= 180) {
            return ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, Util.nextInt(30, 70));
        }
        if (roll <= 350) {
            return ItemService.gI().createNewItem((short) GEM_ID, Util.nextInt(100, 300));
        }
        if (roll <= 600) {
            return ItemService.gI().createNewItem((short) ConstItem.THIEP_HALLOWEEN, Util.nextInt(1, 3));
        }
        return ItemService.gI().createNewItem((short) ConstItem.BI_NGO, Util.nextInt(20, 50));
    }

    private static void drop(Boss boss, int itemId, int quantity, long playerId, int offsetX) {
        int x = Math.max(0, boss.location.x + offsetX);
        int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);
        ItemMap itemMap = new ItemMap(boss.zone, itemId, quantity, x, y, playerId);
        addDropOptions(itemMap);
        Service.gI().dropItemMap(boss.zone, itemMap);
    }

    public static Item createHalloweenCostumeReward(int expireDays) {
        Item costume = ItemService.gI().createNewItem((short) COSTUME_REWARDS[Util.nextInt(COSTUME_REWARDS.length)]);
        addCostumeOptions(costume, expireDays);
        return costume;
    }

    public static Item createHalloweenPetReward() {
        Item pet = ItemService.gI().createNewItem((short) ConstItem.PET_BI_MA_VUONG);
        addPetOptions(pet);
        return pet;
    }

    private static void addDropOptions(ItemMap itemMap) {
        if (itemMap == null || itemMap.itemTemplate == null) {
            return;
        }

        int itemId = itemMap.itemTemplate.id;
        if (isHalloweenCostume(itemId)) {
            addCostumeOptions(itemMap.options, 30);
        } else if (itemId == ConstItem.PET_BI_MA_VUONG) {
            addPetOptions(itemMap.options);
        }
    }

    private static void addCostumeOptions(Item item, int expireDays) {
        if (item == null || item.itemOptions == null) {
            return;
        }
        addCostumeOptions(item.itemOptions, expireDays);
        refresh(item);
    }

    private static void addCostumeOptions(java.util.List<ItemOption> options, int expireDays) {
        options.add(new ItemOption(OPTION_DAMAGE_PERCENT, randomHalloweenStat()));
        options.add(new ItemOption(OPTION_HP_PERCENT, randomHalloweenStat()));
        options.add(new ItemOption(OPTION_KI_PERCENT, randomHalloweenStat()));
        options.add(new ItemOption(OPTION_CRITICAL_DAMAGE_PERCENT, HALLOWEEN_COSTUME_EXTRA_PERCENT));
        options.add(new ItemOption(OPTION_DAMAGE_BOSS_PERCENT, HALLOWEEN_COSTUME_EXTRA_PERCENT));
        options.add(new ItemOption(OPTION_CAN_NOT_TRADE, 0));
        options.add(new ItemOption(OPTION_EXPIRE_DAYS, expireDays));
    }

    private static void addPetOptions(Item item) {
        if (item == null || item.itemOptions == null) {
            return;
        }
        addPetOptions(item.itemOptions);
        refresh(item);
    }

    private static void addPetOptions(java.util.List<ItemOption> options) {
        options.add(new ItemOption(OPTION_DAMAGE_PERCENT, randomHalloweenStat()));
        options.add(new ItemOption(OPTION_HP_PERCENT, randomHalloweenStat()));
        options.add(new ItemOption(OPTION_KI_PERCENT, randomHalloweenStat()));
        options.add(new ItemOption(OPTION_DEF_PERCENT, 5));
        options.add(new ItemOption(OPTION_CAN_NOT_TRADE, 0));
    }

    private static int randomHalloweenStat() {
        return Util.nextInt(HALLOWEEN_STAT_MIN, HALLOWEEN_STAT_MAX);
    }

    private static boolean isHalloweenCostume(int itemId) {
        return itemId == ConstItem.CAI_TRANG_BONG_BANG_HALLOWEEN
                || itemId == ConstItem.CAI_TRANG_VUA_QUY_SATAN_HALLOWEEN
                || itemId == ConstItem.CAI_TRANG_DRACULA_HALLOWEEN;
    }

    private static void refresh(Item item) {
        item.info = item.getInfo();
        item.content = item.getContent();
    }
}
