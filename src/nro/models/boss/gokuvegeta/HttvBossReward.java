package nro.models.boss.gokuvegeta;

import java.util.ArrayList;
import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossDropRateManager;
import nro.models.consts.ConstTaskBadges;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.task.BadgesTaskService;
import nro.models.utils.Util;

final class HttvBossReward {

    private static final short[] HUY_DIET_IDS = { 650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662 };
    private static final int[] DRAGON_BALL_IDS = { 15, 16, 17, 18, 19, 20 };

    private HttvBossReward() {
    }

    static void drop(Boss boss, Player plKill) {
        if (boss == null || plKill == null || boss.zone == null || boss.location == null) {
            return;
        }

        int x = boss.location.x;
        int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);

        if (BossDropRateManager.gI().shouldDrop(boss, 70)) {
            ItemMap thanLinhDrop = ItemService.gI().randDoTLBoss(boss.zone, 1, x + Util.nextInt(-50, 50), y, plKill.id);
            if (thanLinhDrop != null) {
                Service.gI().dropItemMap(boss.zone, thanLinhDrop);
            }
        }

        if (Util.isTrue(10, 100)) {
            Item doHuyDietItem = createBillHuyDietItem(HUY_DIET_IDS[Util.nextInt(HUY_DIET_IDS.length)]);
            if (doHuyDietItem != null && doHuyDietItem.template != null) {
                ItemMap doHuyDietDrop = new ItemMap(boss.zone, doHuyDietItem.template, 1, x + Util.nextInt(-50, 50), y, plKill.id);
                doHuyDietDrop.options.addAll(doHuyDietItem.itemOptions);
                Service.gI().dropItemMap(boss.zone, doHuyDietDrop);
            }
        }

        int nrId = DRAGON_BALL_IDS[Util.nextInt(DRAGON_BALL_IDS.length)];
        Service.gI().dropItemMap(boss.zone, new ItemMap(boss.zone, nrId, 1, x + Util.nextInt(-50, 50), y, plKill.id));

        if (boss instanceof BlackGoku) {
            BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
        }
        if (plKill.event != null) {
            plKill.event.addEventPoint(20);
            Service.gI().sendThongBao(plKill, "+20 Point");
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, boss);
    }

    private static Item createBillHuyDietItem(short itemId) {
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
        if (item == null || item.template == null) {
            return null;
        }
        applyBillHuyDietOptions(item);
        return item;
    }

    private static void applyBillHuyDietOptions(Item item) {
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

    private static boolean canUpgradeBillHuyDietOption(int optionId) {
        return optionId == 0 || optionId == 22 || optionId == 23 || optionId == 14
                || optionId == 27 || optionId == 28 || optionId == 47;
    }
}
