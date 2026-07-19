package nro.models.boss.event.Halloween;

import nro.models.boss.Boss;
import nro.models.consts.ConstItem;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Util;

final class HalloweenRewards {

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

    private static void drop(Boss boss, int itemId, int quantity, long playerId, int offsetX) {
        int x = Math.max(0, boss.location.x + offsetX);
        int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);
        ItemMap itemMap = new ItemMap(boss.zone, itemId, quantity, x, y, playerId);
        Service.gI().dropItemMap(boss.zone, itemMap);
    }
}
