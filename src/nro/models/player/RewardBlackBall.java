package nro.models.player;

import nro.models.item.Item;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.TimeUtil;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class RewardBlackBall {

    private static final int TIME_REWARD = 79200000;

    public static final int R1S_1 = 20;
    public static final int R1S_2 = 21;
    public static final int R2S_1 = 35;
    public static final int R2S_2 = 20;
    public static final int R3S_1 = 35;
    public static final int R3S_2 = 10;
    public static final int R4S_1 = 10;
    public static final int R4S_2 = 35;
    public static final int R5S_1 = 35;
    public static final int R5S_2 = 20;
    public static final int R5S_3 = 20;
    public static final int R6S_1 = 40;
    public static final int R6S_2 = 20;
    public static final int R7S_1 = 14;
    public static final int R7S_2 = 15;

    public static final int TIME_WAIT = 3600000;
    public static long time8h;
    private Player player;

    public long[] timeOutOfDateReward;
    public int[] quantilyBlackBall;
    public long[] lastTimeGetReward;

    public RewardBlackBall(Player player) {
        this.player = player;
        this.timeOutOfDateReward = new long[7];
        this.lastTimeGetReward = new long[7];
        this.quantilyBlackBall = new int[7];
        time8h = TimeUtil.getStartTimeBlackBallWar();
    }

    public void reward(byte star) {
        if (this.timeOutOfDateReward[star - 1] > time8h) {
            quantilyBlackBall[star - 1]++;
        }
        this.timeOutOfDateReward[star - 1] = System.currentTimeMillis() + TIME_REWARD;
        Service.gI().point(player);
    }

    public void getRewardSelect(byte select) {
        int index = 0;
        for (int i = 0; i < timeOutOfDateReward.length; i++) {
            if (timeOutOfDateReward[i] > System.currentTimeMillis()) {
                index++;
                if (index == select + 1) {
                    getReward(i + 1);
                    break;
                }
            }
        }
    }

    private void getReward(int star) {
        if (timeOutOfDateReward[star - 1] <= System.currentTimeMillis()) {
            Service.gI().sendThongBao(player, "Chờ Đi....");
            return;
        }
        if (star == 5) {
            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                Service.gI().sendThongBao(player, "Hành trang đầy");
                return;
            }
            Item daBaoVe = ItemService.gI().daBaoVe();
            InventoryService.gI().addItemBag(player, daBaoVe);
            InventoryService.gI().sendItemBags(player);
            lastTimeGetReward[star - 1] = System.currentTimeMillis();
            timeOutOfDateReward[star - 1] = 0;
            Service.gI().sendThongBao(player, "Bạn nhận được 1 " + daBaoVe.template.name);
            return;
        }
        if (!Util.canDoWithTime(lastTimeGetReward[star - 1], TIME_WAIT)) {
            Service.gI().sendThongBao(player, "Chờ Đi....");
            return;
        }
        lastTimeGetReward[star - 1] = System.currentTimeMillis();
        switch (star) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
                Service.gI().sendThongBao(player, "Chỉ số tự cộng khi nhặt xong");
                break;
        }
    }

    public void dispose() {
        this.player = null;
    }
}
