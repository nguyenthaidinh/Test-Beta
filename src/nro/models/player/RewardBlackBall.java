package nro.models.player;

import nro.models.item.Item;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
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
    public static final int R1S_2 = 10; // Giảm từ 21% → 10% sức đánh
    public static final int R2S_1 = 15; // Giảm từ 35% → 15% HP
    public static final int R2S_KI = 15; // Thêm +15% KI cho 2 sao đen
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
        if (timeOutOfDateReward[star - 1] > System.currentTimeMillis()
                && Util.canDoWithTime(lastTimeGetReward[star - 1], TIME_WAIT)) {
            lastTimeGetReward[star - 1] = System.currentTimeMillis();
            switch (star) {
                case 1:
                case 2:
                case 3:
                    Service.gI().sendThongBao(player, "Chỉ Số Tự Cộng Khi Nhặt xong");
                    break;
                case 4: // Ngọc rồng random 3-7 sao
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        short nrId = (short) Util.nextInt(16, 20); // NR 3-7 sao
                        Item nr = ItemService.gI().createNewItem(nrId);
                        nr.quantity = 1;
                        InventoryService.gI().addItemBag(player, nr);
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được " + nr.template.name);
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đầy");
                    }
                    break;
                case 5: // x10 đá nâng cấp các loại
                    if (InventoryService.gI().getCountEmptyBag(player) >= 5) {
                        short[] stones = {220, 221, 222, 223, 224}; // Lục Bảo, Saphia, Ruby, Titan, Thạch Anh Tím
                        for (short stoneId : stones) {
                            Item stone = ItemService.gI().createNewItem(stoneId);
                            stone.quantity = 10;
                            InventoryService.gI().addItemBag(player, stone);
                        }
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được x10 mỗi loại đá nâng cấp");
                    } else {
                        Service.gI().sendThongBao(player, "Cần ít nhất 5 ô hành trang trống");
                    }
                    break;
                case 6: // 10x đá ngũ sắc
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        Item daNguSac = ItemService.gI().createNewItem((short) 674); // Đá Ngũ Sắc
                        daNguSac.quantity = 10;
                        InventoryService.gI().addItemBag(player, daNguSac);
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được 10 Đá Ngũ Sắc");
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đầy");
                    }
                    break;
                case 7: // 20 ngọc
                    player.inventory.gem += 20;
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được 20 ngọc");
                    break;
            }
        } else {
            Service.gI().sendThongBao(player, "Chờ Đi....");
        }
    }

    public void dispose() {
        this.player = null;
    }
}
