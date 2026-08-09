package nro.models.boss.pirate;

import nro.models.boss.Boss;
import nro.models.consts.ConstItem;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.utils.Util;

public final class PirateBossRewards {

    private static final int DRAGON_BALL_DROP_RATE = 30;
    private static final int MOON_CAKE_DROP_RATE = 20;
    private static final int SILVER_CHEST_DROP_RATE = 20;
    private static final int GOLD_CHEST_DROP_RATE = 5;
    private static final int[] DRAGON_BALL_IDS = {14, 15, 16, 17};
    private static final int[] MOON_CAKE_IDS = {
        ConstItem.BANH_TRUNG_THU_DAC_BIET,
        ConstItem.HOP_BANH_TRUNG_THU
    };

    private PirateBossRewards() {
    }

    public static void drop(Boss boss, Player plKill) {
        if (boss == null || plKill == null || boss.zone == null || boss.zone.map == null || boss.location == null) {
            return;
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, boss);

        int x = boss.location.x;
        int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);
        dropThanLinh(boss, plKill, x, y);
        dropRandomByRate(boss, plKill, DRAGON_BALL_IDS, DRAGON_BALL_DROP_RATE, x, y);
        dropRandomByRate(boss, plKill, MOON_CAKE_IDS, MOON_CAKE_DROP_RATE, x, y);
        dropByRate(boss, plKill, ConstItem.RUONG_BAC, SILVER_CHEST_DROP_RATE, x, y);
        dropByRate(boss, plKill, ConstItem.RUONG_VANG, GOLD_CHEST_DROP_RATE, x, y);
    }

    private static void dropThanLinh(Boss boss, Player plKill, int x, int y) {
        ItemMap item = ItemService.gI().randDoTLBoss(
                boss.zone,
                1,
                x + Util.nextInt(-50, 50),
                y,
                plKill.id);
        if (item != null) {
            Service.gI().dropItemMap(boss.zone, item);
        }
    }

    private static void dropRandomByRate(Boss boss, Player plKill, int[] itemIds, int rate, int x, int y) {
        if (!Util.isTrue(rate, 100)) {
            return;
        }
        int itemId = itemIds[Util.nextInt(itemIds.length)];
        dropItem(boss, plKill, itemId, x, y);
    }

    private static void dropByRate(Boss boss, Player plKill, int itemId, int rate, int x, int y) {
        if (!Util.isTrue(rate, 100)) {
            return;
        }
        dropItem(boss, plKill, itemId, x, y);
    }

    private static void dropItem(Boss boss, Player plKill, int itemId, int x, int y) {
        Service.gI().dropItemMap(boss.zone, new ItemMap(
                boss.zone,
                itemId,
                1,
                x + Util.nextInt(-50, 50),
                y,
                plKill.id));
    }
}
