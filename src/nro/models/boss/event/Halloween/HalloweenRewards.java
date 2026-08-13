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
    private static final int OPTION_TELEPORT = 33;
    private static final int OPTION_NO_COLD = 106;
    private static final int OPTION_RESIST_TDHS = 116;
    private static final int HALLOWEEN_STAT_MIN = 25;
    private static final int HALLOWEEN_STAT_MAX = 50;
    private static final int HALLOWEEN_COSTUME_EXTRA_PERCENT = 30;
    private static final int HALLOWEEN_PET_CRITICAL_DAMAGE_MIN = 15;
    private static final int HALLOWEEN_PET_CRITICAL_DAMAGE_MAX = 25;
    private static final int HALLOWEEN_GHOST_GOKU_HP_MIN = 20;
    private static final int HALLOWEEN_GHOST_GOKU_HP_MAX = 50;
    private static final int HALLOWEEN_GHOST_CADIC_DAMAGE_MIN = 20;
    private static final int HALLOWEEN_GHOST_CADIC_DAMAGE_MAX = 40;
    private static final int HALLOWEEN_GHOST_POCOLO_KI_MIN = 20;
    private static final int HALLOWEEN_GHOST_POCOLO_KI_MAX = 50;
    private static final int CAPSULE_GOLD_MIN = 20;
    private static final int CAPSULE_GOLD_MAX = 200;
    private static final int THAN_CHET_CUTE_HP_KI_MIN = 20;
    private static final int THAN_CHET_CUTE_HP_KI_MAX = 40;
    private static final int THAN_CHET_CUTE_DEF_MIN = 10;
    private static final int THAN_CHET_CUTE_DEF_MAX = 25;
    private static final int BI_NGO_NHI_NHANH_STAT_MIN = 20;
    private static final int BI_NGO_NHI_NHANH_STAT_MAX = 30;
    private static final int BI_NGO_NHI_NHANH_CRITICAL_MIN = 20;
    private static final int BI_NGO_NHI_NHANH_CRITICAL_MAX = 25;
    private static final int LUOI_HAI_THAN_CHET_STAT_MIN = 20;
    private static final int LUOI_HAI_THAN_CHET_STAT_MAX = 50;
    private static final int LUOI_HAI_THAN_CHET_DEF_MIN = 20;
    private static final int LUOI_HAI_THAN_CHET_DEF_MAX = 25;
    private static final int LUOI_HAI_THAN_CHET_CRITICAL_MIN = 20;
    private static final int LUOI_HAI_THAN_CHET_CRITICAL_MAX = 30;
    private static final int SUPER_GOD_COSTUME_DAMAGE_PERCENT = 70;
    private static final int SUPER_GOD_COSTUME_HP_KI_PERCENT = 80;
    private static final int SUPER_GOD_COSTUME_DAMAGE_BOSS_PERCENT = 30;
    private static final int[] HALLOWEEN_CAPSULE_EXPIRE_DAYS = {1, 3, 5, 7};
    private static final int HALLOWEEN_REWARD_PERMANENT_RATE = 10;
    private static final int[] HALLOWEEN_REWARD_EXPIRE_DAYS = {3, 5, 7, 15};

    private static final int[] COSTUME_REWARDS = {
        ConstItem.CAI_TRANG_BONG_BANG_HALLOWEEN,
        ConstItem.CAI_TRANG_VUA_QUY_SATAN_HALLOWEEN,
        ConstItem.CAI_TRANG_DRACULA_HALLOWEEN
    };
    private static final int[] PUMPKIN_DRAGON_BALL_REWARDS = {
        ConstItem.BI_NGO_1_SAO,
        ConstItem.BI_NGO_2_SAO,
        ConstItem.BI_NGO_3_SAO,
        ConstItem.BI_NGO_4_SAO,
        ConstItem.BI_NGO_5_SAO,
        ConstItem.BI_NGO_6_SAO,
        ConstItem.BI_NGO_7_SAO
    };
    private static final int[] SUPER_GOD_COSTUME_REWARDS = {
        ConstItem.CAI_TRANG_SIEU_THAN_TRAI_DAT,
        ConstItem.CAI_TRANG_SIEU_THAN_NAMEC,
        ConstItem.CAI_TRANG_SIEU_THAN_XAYDA
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
        if (roll <= 300) {
            return createRandomPumpkinDragonBallReward();
        }
        if (roll <= 470) {
            return ItemService.gI().createNewItem((short) GEM_ID, Util.nextInt(100, 300));
        }
        if (roll <= 720) {
            return ItemService.gI().createNewItem((short) ConstItem.THIEP_HALLOWEEN, Util.nextInt(1, 3));
        }
        return ItemService.gI().createNewItem((short) ConstItem.BI_NGO, Util.nextInt(20, 50));
    }

    public static Item createRandomPumpkinDragonBallReward() {
        return ItemService.gI().createNewItem((short) randomPumpkinDragonBallId(), 1);
    }

    public static boolean removeExpireFromOpenedReward(Item item) {
        if (item == null || item.template == null || item.itemOptions == null
                || !isTimedHalloweenOpenedReward(item.template.id)) {
            return false;
        }
        boolean removed = item.itemOptions.removeIf(option -> option != null
                && option.optionTemplate != null
                && option.optionTemplate.id == OPTION_EXPIRE_DAYS);
        if (removed) {
            refresh(item);
        }
        return removed;
    }

    public static Item createHalloweenCapsuleReward() {
        int roll = Util.nextInt(10);
        switch (roll) {
            case 0:
                return createHalloweenGhostGokuReward();
            case 1:
                return createHalloweenGhostCadicReward();
            case 2:
                return createHalloweenGhostPocoloReward();
            case 3:
                return ItemService.gI().createNewItem((short) ConstItem.THOI_VANG,
                        Util.nextInt(CAPSULE_GOLD_MIN, CAPSULE_GOLD_MAX));
            case 4:
                return createThanChetCuteReward();
            case 5:
                return createBiNgoNhiNhanhReward();
            case 6:
                return createLuoiHaiThanChetReward();
            default:
                return createSuperGodCostumeReward(SUPER_GOD_COSTUME_REWARDS[roll - 7]);
        }
    }

    private static int randomPumpkinDragonBallId() {
        return PUMPKIN_DRAGON_BALL_REWARDS[Util.nextInt(PUMPKIN_DRAGON_BALL_REWARDS.length)];
    }

    private static Item createHalloweenGhostGokuReward() {
        Item item = ItemService.gI().createNewItem((short) ConstItem.HON_MA_GOKU);
        item.itemOptions.add(new ItemOption(OPTION_HP_PERCENT,
                Util.nextInt(HALLOWEEN_GHOST_GOKU_HP_MIN, HALLOWEEN_GHOST_GOKU_HP_MAX)));
        addHalloweenCapsuleExpire(item);
        return item;
    }

    private static Item createHalloweenGhostCadicReward() {
        Item item = ItemService.gI().createNewItem((short) ConstItem.HON_MA_CA_DIC);
        item.itemOptions.add(new ItemOption(OPTION_DAMAGE_PERCENT,
                Util.nextInt(HALLOWEEN_GHOST_CADIC_DAMAGE_MIN, HALLOWEEN_GHOST_CADIC_DAMAGE_MAX)));
        addHalloweenCapsuleExpire(item);
        return item;
    }

    private static Item createHalloweenGhostPocoloReward() {
        Item item = ItemService.gI().createNewItem((short) ConstItem.HON_MA_POCOLO);
        item.itemOptions.add(new ItemOption(OPTION_KI_PERCENT,
                Util.nextInt(HALLOWEEN_GHOST_POCOLO_KI_MIN, HALLOWEEN_GHOST_POCOLO_KI_MAX)));
        addHalloweenCapsuleExpire(item);
        return item;
    }

    private static Item createThanChetCuteReward() {
        Item item = ItemService.gI().createNewItem((short) ConstItem.THAN_CHET_CUTE);
        item.itemOptions.add(new ItemOption(OPTION_HP_PERCENT,
                Util.nextInt(THAN_CHET_CUTE_HP_KI_MIN, THAN_CHET_CUTE_HP_KI_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_KI_PERCENT,
                Util.nextInt(THAN_CHET_CUTE_HP_KI_MIN, THAN_CHET_CUTE_HP_KI_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_DEF_PERCENT,
                Util.nextInt(THAN_CHET_CUTE_DEF_MIN, THAN_CHET_CUTE_DEF_MAX)));
        addHalloweenCapsuleExpire(item);
        return item;
    }

    private static Item createBiNgoNhiNhanhReward() {
        Item item = ItemService.gI().createNewItem((short) ConstItem.BI_NGO_NHI_NHANH);
        item.itemOptions.add(new ItemOption(OPTION_DAMAGE_PERCENT,
                Util.nextInt(BI_NGO_NHI_NHANH_STAT_MIN, BI_NGO_NHI_NHANH_STAT_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_KI_PERCENT,
                Util.nextInt(BI_NGO_NHI_NHANH_STAT_MIN, BI_NGO_NHI_NHANH_STAT_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_HP_PERCENT,
                Util.nextInt(BI_NGO_NHI_NHANH_STAT_MIN, BI_NGO_NHI_NHANH_STAT_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_CRITICAL_DAMAGE_PERCENT,
                Util.nextInt(BI_NGO_NHI_NHANH_CRITICAL_MIN, BI_NGO_NHI_NHANH_CRITICAL_MAX)));
        addHalloweenCapsuleExpire(item);
        return item;
    }

    private static Item createLuoiHaiThanChetReward() {
        Item item = ItemService.gI().createNewItem((short) ConstItem.LUOI_HAI_THAN_CHET);
        item.itemOptions.add(new ItemOption(OPTION_DAMAGE_PERCENT,
                Util.nextInt(LUOI_HAI_THAN_CHET_STAT_MIN, LUOI_HAI_THAN_CHET_STAT_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_HP_PERCENT,
                Util.nextInt(LUOI_HAI_THAN_CHET_STAT_MIN, LUOI_HAI_THAN_CHET_STAT_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_KI_PERCENT,
                Util.nextInt(LUOI_HAI_THAN_CHET_STAT_MIN, LUOI_HAI_THAN_CHET_STAT_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_DEF_PERCENT,
                Util.nextInt(LUOI_HAI_THAN_CHET_DEF_MIN, LUOI_HAI_THAN_CHET_DEF_MAX)));
        item.itemOptions.add(new ItemOption(OPTION_CRITICAL_DAMAGE_PERCENT,
                Util.nextInt(LUOI_HAI_THAN_CHET_CRITICAL_MIN, LUOI_HAI_THAN_CHET_CRITICAL_MAX)));
        addHalloweenCapsuleExpire(item);
        return item;
    }

    private static Item createSuperGodCostumeReward(int itemId) {
        Item item = ItemService.gI().createNewItem((short) itemId);
        item.itemOptions.add(new ItemOption(OPTION_DAMAGE_PERCENT, SUPER_GOD_COSTUME_DAMAGE_PERCENT));
        item.itemOptions.add(new ItemOption(OPTION_HP_PERCENT, SUPER_GOD_COSTUME_HP_KI_PERCENT));
        item.itemOptions.add(new ItemOption(OPTION_KI_PERCENT, SUPER_GOD_COSTUME_HP_KI_PERCENT));
        item.itemOptions.add(new ItemOption(OPTION_DAMAGE_BOSS_PERCENT, SUPER_GOD_COSTUME_DAMAGE_BOSS_PERCENT));
        item.itemOptions.add(new ItemOption(OPTION_TELEPORT, 0));
        item.itemOptions.add(new ItemOption(OPTION_RESIST_TDHS, 0));
        item.itemOptions.add(new ItemOption(OPTION_NO_COLD, 0));
        addHalloweenCapsuleExpire(item);
        return item;
    }

    private static void addHalloweenCapsuleExpire(Item item) {
        item.itemOptions.add(new ItemOption(OPTION_EXPIRE_DAYS, randomHalloweenCapsuleExpireDays()));
        refresh(item);
    }

    private static int randomHalloweenCapsuleExpireDays() {
        return HALLOWEEN_CAPSULE_EXPIRE_DAYS[Util.nextInt(HALLOWEEN_CAPSULE_EXPIRE_DAYS.length)];
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

    public static Item createHalloweenCostumeReward() {
        Item costume = ItemService.gI().createNewItem((short) COSTUME_REWARDS[Util.nextInt(COSTUME_REWARDS.length)]);
        addCostumeOptions(costume, randomHalloweenRewardExpireDays());
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
        addHalloweenExpireOption(options, expireDays);
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
        options.add(new ItemOption(OPTION_CRITICAL_DAMAGE_PERCENT, randomHalloweenPetCriticalDamage()));
        options.add(new ItemOption(OPTION_DEF_PERCENT, 5));
        options.add(new ItemOption(OPTION_CAN_NOT_TRADE, 0));
        addHalloweenExpireOption(options, randomHalloweenRewardExpireDays());
    }

    private static int randomHalloweenStat() {
        return Util.nextInt(HALLOWEEN_STAT_MIN, HALLOWEEN_STAT_MAX);
    }

    private static int randomHalloweenPetCriticalDamage() {
        return Util.nextInt(HALLOWEEN_PET_CRITICAL_DAMAGE_MIN, HALLOWEEN_PET_CRITICAL_DAMAGE_MAX);
    }

    private static int randomHalloweenRewardExpireDays() {
        if (Util.isTrue(HALLOWEEN_REWARD_PERMANENT_RATE, 100)) {
            return -1;
        }
        return HALLOWEEN_REWARD_EXPIRE_DAYS[Util.nextInt(HALLOWEEN_REWARD_EXPIRE_DAYS.length)];
    }

    private static void addHalloweenExpireOption(java.util.List<ItemOption> options, int expireDays) {
        if (expireDays > 0) {
            options.add(new ItemOption(OPTION_EXPIRE_DAYS, expireDays));
        }
    }

    private static boolean isHalloweenCostume(int itemId) {
        return itemId == ConstItem.CAI_TRANG_BONG_BANG_HALLOWEEN
                || itemId == ConstItem.CAI_TRANG_VUA_QUY_SATAN_HALLOWEEN
                || itemId == ConstItem.CAI_TRANG_DRACULA_HALLOWEEN;
    }

    private static boolean isTimedHalloweenOpenedReward(int itemId) {
        return isHalloweenCostume(itemId)
                || itemId == ConstItem.PET_BI_MA_VUONG
                || itemId == ConstItem.HON_MA_GOKU
                || itemId == ConstItem.HON_MA_CA_DIC
                || itemId == ConstItem.HON_MA_POCOLO
                || itemId == ConstItem.THAN_CHET_CUTE
                || itemId == ConstItem.BI_NGO_NHI_NHANH
                || itemId == ConstItem.CAI_TRANG_SIEU_THAN_TRAI_DAT
                || itemId == ConstItem.CAI_TRANG_SIEU_THAN_NAMEC
                || itemId == ConstItem.CAI_TRANG_SIEU_THAN_XAYDA;
    }

    private static void refresh(Item item) {
        item.info = item.getInfo();
        item.content = item.getContent();
    }
}
