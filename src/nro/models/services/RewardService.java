package nro.models.services;

import nro.models.consts.ConstItem;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author By Mr Blue
 *
 */
public class RewardService {

    private static final short GOKU_NGAY_XUA_ID = 1868;
    private static final short MOTO_BUN_MA_ID = 1541;
    private static final short PET_BABY_SHARK_ID = 1620;
    private static final short PET_CAPYBARA_BACKPACK_ID = 1629;
    private static final short PET_CAPYBARA_PINK_ID = 1668;
    private static final short PET_KHUNG_LONG_NGOK_ID = 1750;
    private static final short PHONG_XICH_LAN_ID = 1734;
    private static final short SANTA_TANK_ID = 1573;
    private static final short[] LUCKY_ROUND_PET_AND_MOUNT_IDS = {
        1620, 1629, 1668, 1748, 1750, 1729, 1727, 1714, 1682,
        1541, 1563, 1573, 1724, 1733, 1734, 1749
    };
    private static final short[] LUCKY_ROUND_FRAGMENT_IDS = {1792, 1791, 1204};
    private static final short[] LUCKY_ROUND_SUPPORT_ITEM_IDS = {1152, 1150, 1153, 1151, 1154};
    private static final short FOUR_STAR_DRAGON_BALL_ID = 17;
    private static final short FIVE_STAR_DRAGON_BALL_ID = 18;
    private static final short[] LUCKY_ROUND_UPGRADE_STONE_IDS = {220, 223, 224, 221, 222};
    private static final int[] LUCKY_ROUND_DEFAULT_EVENT_ITEMS = {999, 1000, 1001};
    private static final int LUCKY_ROUND_LOCKED_GOLD_BAR_RATE = 100;
    private static final int LUCKY_ROUND_LOCKED_GOLD_BAR_QUANTITY = 1;

    private static final int[][][] ACTIVATION_SET = {
        {{129, 141, 1, 1000}, {127, 139, 1, 1000}, {128, 140, 1, 1000}}, // songoku - thien xin hang - kirin
        {{131, 143, 1, 1000}, {132, 144, 1, 1000}, {130, 142, 1, 1000}}, // oc tieu - pikkoro daimao -
        // picolo
        {{135, 138, 1, 1000}, {133, 136, 1, 1000}, {134, 137, 1, 1000}} // kakarot - cadic - nappa
    };
    private static RewardService I;

    private RewardService() {

    }

    public static RewardService gI() {
        if (RewardService.I == null) {
            RewardService.I = new RewardService();
        }
        return RewardService.I;
    }

    //========================LUCKY ROUND========================
    public List<Item> getListItemLuckyRound(Player player, int num, boolean vip) {
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Item it = ItemService.gI().createNewItem((short) 189); // Vàng mặc định
            it.quantity = Util.nextInt(5, 50) * 1000;
            boolean success = Util.isTrue(1, 2);

            if (vip || Util.isTrue(1, 2)) {
                // VIP - Thay thế vật phẩm nếu thỏa mãn điều kiện
                if (Util.isTrue(5, 100)) {
                    it = ItemService.gI().createNewItem((short) 884);// hit
                    it.itemOptions.clear();
                    it.itemOptions.add(new ItemOption(50, 40));
                    it.itemOptions.add(new ItemOption(77, 40));
                    it.itemOptions.add(new ItemOption(103, 40));
                    it.itemOptions.add(new ItemOption(5, Util.nextInt(30)));
                    it.itemOptions.add(new Item.ItemOption(154, 0)); // Không bán lại
                    // Xác suất 99% thêm option 93
                    if (!Util.isTrue(1, 100)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 10))); // HSD
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(5, 100)) {
                    it = createGokuNgayXuaLuckyRoundItem();
                } else if (Util.isTrue(5, 100)) {
                    it = createLuckyRoundPetOrMountItem();
                } else if (Util.isTrue(5, 100)) {
                    it = ItemService.gI().createNewItem((short) 860); //mị nương
                    it.itemOptions.clear();
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(24)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(24)));
                    it.itemOptions.add(new Item.ItemOption(117, 15));
                    it.itemOptions.add(new Item.ItemOption(154, 0)); // Không bán lại
                    // Xác suất 99% thêm option 93
                    if (!Util.isTrue(1, 100)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 10))); // HSD
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(1, 100)) {
                    // Thay thế bó hoa gold 1 (ID 955)
                    it = ItemService.gI().createNewItem((short) 955); // Bó hoa hồng 1
                    it.itemOptions.clear();
                    it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(1, 10))); // Sức đánh
                    it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(1, 10))); // HP
                    it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(1, 10))); // Ki
                    // Xác suất 99% thêm option 93
                    if (!Util.isTrue(1, 100)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 10))); // HSD
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(1, LUCKY_ROUND_LOCKED_GOLD_BAR_RATE)) {
                    it = createLockedGoldBarLuckyRoundItem();
                // Compensate for earlier reward checks so the overall fragment rate stays near 1%.
                } else if (Util.isTrue(124, 10000)) {
                    it = createLuckyRoundFragmentItem();
                } else if (Util.isTrue(5, 100)) {
                    it = createLuckyRoundSupportItem();
                } else if (Util.isTrue(5, 100)) {
                    it = createLuckyRoundDragonBallItem();
                } else if (Util.isTrue(15, 100)) {
                    it = createLuckyRoundUpgradeStoneItem();
                } else if (Util.isTrue(5, 100)) {
                    it = ItemService.gI().createNewItem((short) 956);// Đá bảo vệ
                    it.itemOptions.clear();
                    it.quantity = 1;
                }

                // Nếu không chọn được vật phẩm từ danh sách VIP thì gọi itemRand để lấy vật phẩm mặc định
                if (it.quantity == 0) {
                    it = itemRand(it, success);
                }
            } else {
                // Không VIP
                if (Util.isTrue(1, 2)) {
                    // Các item mặc định khi không có item VIP
                    int itemid = LUCKY_ROUND_DEFAULT_EVENT_ITEMS[Util.nextInt(LUCKY_ROUND_DEFAULT_EVENT_ITEMS.length)];
                    if (Util.isTrue(20, 100)) {
                        itemid = LUCKY_ROUND_DEFAULT_EVENT_ITEMS[Util.nextInt(LUCKY_ROUND_DEFAULT_EVENT_ITEMS.length)];
                    }
                    byte[] option = {77, 80, 81, 103, 50, 94, 5};
                    byte optionid;
                    byte param;
                    Item vpdl = ItemService.gI().createNewItem((short) itemid);
                    vpdl.itemOptions.clear();
                    optionid = option[Util.nextInt(0, 6)];
                    param = (byte) Util.nextInt(5, 10);
                    vpdl.itemOptions.add(new Item.ItemOption(optionid, param));
                    vpdl.itemOptions.add(new Item.ItemOption(30, 0));
                    vpdl.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
                    it = vpdl;
                    it.quantity = 1;
                } else if (Util.isTrue(1, 100)) {
                    // Thay thế với các item ngẫu nhiên khác
                    it = ItemService.gI().createNewItem((short) Util.nextInt(18, 20));
                    it.quantity = Util.nextInt(1, 5);
                } else if (Util.isTrue(1, 30)) {
                    // Các vật phẩm từ 220 đến 224
                    it = ItemService.gI().createNewItem((short) Util.nextInt(220, 224));
                    it.quantity = Util.nextInt(1, 5);
                } else if (Util.isTrue(1, 100)) {
                    // Các vật phẩm từ 828 đến 842
                    it = ItemService.gI().createNewItem((short) Util.nextInt(828, 842));
                    it.quantity = Util.nextInt(1, 5);
                }

                // Nếu không chọn được vật phẩm từ danh sách không VIP thì gọi itemRand
                it = itemRand(it, success);
            }

            list.add(it);
        }
        return list;
    }

    private Item createGokuNgayXuaLuckyRoundItem() {
        Item item = ItemService.gI().createNewItem(GOKU_NGAY_XUA_ID);
        item.itemOptions.clear();
        item.itemOptions.add(new ItemOption(101, Util.nextInt(50, 100)));
        item.itemOptions.add(new ItemOption(50, Util.nextInt(15, 30)));
        item.itemOptions.add(new ItemOption(95, Util.nextInt(15, 30)));
        item.itemOptions.add(new ItemOption(96, Util.nextInt(15, 30)));
        item.itemOptions.add(new ItemOption(106, 0));
        item.itemOptions.add(new Item.ItemOption(154, 0));
        if (Util.isTrue(10, 100)) {
            item.itemOptions.add(new Item.ItemOption(73, 0));
        } else {
            item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 10)));
        }
        item.quantity = 1;
        item.info = item.getInfo();
        item.content = item.getContent();
        return item;
    }

    private Item createLuckyRoundPetOrMountItem() {
        short itemId = LUCKY_ROUND_PET_AND_MOUNT_IDS[Util.nextInt(LUCKY_ROUND_PET_AND_MOUNT_IDS.length)];
        Item item = ItemService.gI().createNewItem(itemId);
        item.itemOptions.clear();
        item.itemOptions.add(new ItemOption(50, Util.nextInt(10, 25)));
        item.itemOptions.add(new ItemOption(77, itemId == PET_KHUNG_LONG_NGOK_ID ? Util.nextInt(10, 30) : Util.nextInt(10, 25)));
        item.itemOptions.add(new ItemOption(103, itemId == SANTA_TANK_ID ? Util.nextInt(10, 40) : Util.nextInt(10, 25)));
        if (itemId == MOTO_BUN_MA_ID) {
            item.itemOptions.add(new ItemOption(94, Util.nextInt(10, 20)));
        }
        if (itemId == PET_BABY_SHARK_ID) {
            item.itemOptions.add(new ItemOption(94, Util.nextInt(5, 15)));
        }
        if (itemId == PET_CAPYBARA_BACKPACK_ID) {
            item.itemOptions.add(new ItemOption(5, Util.nextInt(10, 25)));
        }
        if (itemId == PET_CAPYBARA_PINK_ID) {
            item.itemOptions.add(new ItemOption(5, Util.nextInt(10, 20)));
            item.itemOptions.add(new ItemOption(14, Util.nextInt(10, 15)));
        }
        if (itemId == PHONG_XICH_LAN_ID) {
            item.itemOptions.add(new ItemOption(5, Util.nextInt(10, 20)));
        }
        item.itemOptions.add(new Item.ItemOption(154, 0));
        if (Util.isTrue(10, 100)) {
            item.itemOptions.add(new Item.ItemOption(73, 0));
        } else {
            item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 10)));
        }
        item.quantity = 1;
        item.info = item.getInfo();
        item.content = item.getContent();
        return item;
    }

    private Item createLuckyRoundFragmentItem() {
        short itemId = LUCKY_ROUND_FRAGMENT_IDS[Util.nextInt(LUCKY_ROUND_FRAGMENT_IDS.length)];
        Item item = ItemService.gI().createNewItem(itemId);
        item.itemOptions.clear();
        item.itemOptions.add(new ItemOption(30, 0));
        item.quantity = 1;
        item.info = item.getInfo();
        item.content = item.getContent();
        return item;
    }

    private Item createLuckyRoundSupportItem() {
        short itemId = LUCKY_ROUND_SUPPORT_ITEM_IDS[Util.nextInt(LUCKY_ROUND_SUPPORT_ITEM_IDS.length)];
        return ItemService.gI().createNewItem(itemId);
    }

    private Item createLuckyRoundDragonBallItem() {
        short itemId = Util.isTrue(1, 4) ? FOUR_STAR_DRAGON_BALL_ID : FIVE_STAR_DRAGON_BALL_ID;
        return ItemService.gI().createNewItem(itemId);
    }

    private Item createLuckyRoundUpgradeStoneItem() {
        short itemId = LUCKY_ROUND_UPGRADE_STONE_IDS[Util.nextInt(LUCKY_ROUND_UPGRADE_STONE_IDS.length)];
        return ItemService.gI().createNewItem(itemId, Util.nextInt(1, 5));
    }

    private Item createLockedGoldBarLuckyRoundItem() {
        Item item = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, LUCKY_ROUND_LOCKED_GOLD_BAR_QUANTITY);
        item.itemOptions.clear();
        item.itemOptions.add(new ItemOption(30, 0));
        item.info = item.getInfo();
        item.content = item.getContent();
        return item;
    }

// Phương thức itemRand sẽ trả về vật phẩm mặc định nếu không thành công
    public Item itemRand(Item item, boolean success) {
        if (!success) {
            // Trả về item vàng mặc định nếu không thành công
            item = ItemService.gI().createNewItem((short) 189, Util.nextInt(5, 12) * 1000);
        }
        return item;
    }

    public void rewardLancon(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            player.canReward = false;
            player.haveReward = true;
            int[] items = {734, 920, 849, 743, 733};
            Item item = ItemService.gI().createNewItem((short) items[Util.nextInt(items.length)]);
            byte[] option = {77, 80, 81, 103, 50, 94, 5};
            byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
            if (Util.isTrue(5, 100)) {
                item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
            } else {
                item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 10)));
                if (Util.isTrue(1, 10)) {
                    item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 10)));
                }
                item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
            }
            item.itemOptions.add(new Item.ItemOption(89, 0));
            item.itemOptions.add(new Item.ItemOption(30, 0));
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn vừa nhận được " + item.template.name);
        } else {
            Service.gI().sendThongBao(player, "Cần 1 ô hành trang trống");
        }
    }
//   public void initBaseOptionClothes(Item item) {
//        SetClothes(item.template.id, item.template.type, item.itemOptions);
//    }

    public void initChiSoItem(Item item) {
        SetClothes(item.template.id, item.template.type, item.itemOptions);
    }

    // chỉ số cơ bản: hp, ki, hồi phục, sđ, crit
    private static void SetClothes(int tempId, int type, List<ItemOption> list) {
        int[][] option_param = {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};
        switch (type) {
            case 0: // áo
                option_param[0][0] = 47; // giáp
                switch (tempId) {
                    case 0:
                        option_param[0][1] = 2;
                        break;
                    case 33:
                        option_param[0][1] = 4;
                        break;
                    case 3:
                        option_param[0][1] = 8;
                        break;
                    case 34:
                        option_param[0][1] = 16;
                        break;
                    case 136:
                        option_param[0][1] = 24;
                        break;
                    case 137:
                        option_param[0][1] = 40;
                        break;
                    case 138:
                        option_param[0][1] = 60;
                        break;
                    case 139:
                        option_param[0][1] = 90;
                        break;
                    case 230:
                        option_param[0][1] = 200;
                        break;
                    case 231:
                        option_param[0][1] = 250;
                        break;
                    case 232:
                        option_param[0][1] = 300;
                        break;
                    case 233:
                        option_param[0][1] = 400;
                        break;
                    case 1:
                        option_param[0][1] = 2;
                        break;
                    case 41:
                        option_param[0][1] = 4;
                        break;
                    case 4:
                        option_param[0][1] = 8;
                        break;
                    case 42:
                        option_param[0][1] = 16;
                        break;
                    case 152:
                        option_param[0][1] = 24;
                        break;
                    case 153:
                        option_param[0][1] = 40;
                        break;
                    case 154:
                        option_param[0][1] = 60;
                        break;
                    case 155:
                        option_param[0][1] = 90;
                        break;
                    case 234:
                        option_param[0][1] = 200;
                        break;
                    case 235:
                        option_param[0][1] = 250;
                        break;
                    case 236:
                        option_param[0][1] = 300;
                        break;
                    case 237:
                        option_param[0][1] = 400;
                        break;
                    case 2:
                        option_param[0][1] = 3;
                        break;
                    case 49:
                        option_param[0][1] = 5;
                        break;
                    case 5:
                        option_param[0][1] = 10;
                        break;
                    case 50:
                        option_param[0][1] = 20;
                        break;
                    case 168:
                        option_param[0][1] = 30;
                        break;
                    case 169:
                        option_param[0][1] = 50;
                        break;
                    case 170:
                        option_param[0][1] = 70;
                        break;
                    case 171:
                        option_param[0][1] = 100;
                        break;
                    case 238:
                        option_param[0][1] = 230;
                        break;
                    case 239:
                        option_param[0][1] = 280;
                        break;
                    case 240:
                        option_param[0][1] = 330;
                        break;
                    case 241:
                        option_param[0][1] = 450;
                        break;
                    case 555: // áo thần trái đất
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 700;
                        option_param[2][1] = 15;
                        break;
                    case 557: // áo thần namếc
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 600;
                        option_param[2][1] = 15;
                        break;
                    case 559: // áo thần xayda
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 800;
                        option_param[2][1] = 15;
                        break;
                    case 650: // áo huỷ diệt trái đất
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // Không thể giao dịch

                        option_param[0][1] = 1100;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 652: // áo huỷ diệt namếc
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // Không thể giao dịch

                        option_param[0][1] = 1000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 654: // áo huỷ diệt xayda
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // Không thể giao dịch

                        option_param[0][1] = 1300;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                }
                break;
            case 1: // quần
                option_param[0][0] = 6; // hp
                option_param[1][0] = 27; // hp hồi/30s
                switch (tempId) {
                    case 6:
                        option_param[0][1] = 30;
                        break;
                    case 35:
                        option_param[0][1] = 150;
                        option_param[1][1] = 12;
                        break;
                    case 9:
                        option_param[0][1] = 300;
                        option_param[1][1] = 40;
                        break;
                    case 36:
                        option_param[0][1] = 600;
                        option_param[1][1] = 120;
                        break;
                    case 140:
                        option_param[0][1] = 1400;
                        option_param[1][1] = 280;
                        break;
                    case 141:
                        option_param[0][1] = 3000;
                        option_param[1][1] = 600;
                        break;
                    case 142:
                        option_param[0][1] = 6000;
                        option_param[1][1] = 1200;
                        break;
                    case 143:
                        option_param[0][1] = 10000;
                        option_param[1][1] = 2000;
                        break;
                    case 242:
                        option_param[0][1] = 14000;
                        option_param[1][1] = 2500;
                        break;
                    case 243:
                        option_param[0][1] = 18000;
                        option_param[1][1] = 3000;
                        break;
                    case 244:
                        option_param[0][1] = 22000;
                        option_param[1][1] = 3500;
                        break;
                    case 245:
                        option_param[0][1] = 26000;
                        option_param[1][1] = 4000;
                        break;
                    case 7:
                        option_param[0][1] = 20;
                        break;
                    case 43:
                        option_param[0][1] = 25;
                        option_param[1][1] = 10;
                        break;
                    case 10:
                        option_param[0][1] = 120;
                        option_param[1][1] = 28;
                        break;
                    case 44:
                        option_param[0][1] = 250;
                        option_param[1][1] = 100;
                        break;
                    case 156:
                        option_param[0][1] = 600;
                        option_param[1][1] = 240;
                        break;
                    case 157:
                        option_param[0][1] = 1200;
                        option_param[1][1] = 480;
                        break;
                    case 158:
                        option_param[0][1] = 2400;
                        option_param[1][1] = 960;
                        break;
                    case 159:
                        option_param[0][1] = 4800;
                        option_param[1][1] = 1800;
                        break;
                    case 246:
                        option_param[0][1] = 13000;
                        option_param[1][1] = 2200;
                        break;
                    case 247:
                        option_param[0][1] = 17000;
                        option_param[1][1] = 2700;
                        break;
                    case 248:
                        option_param[0][1] = 21000;
                        option_param[1][1] = 3200;
                        break;
                    case 249:
                        option_param[0][1] = 25000;
                        option_param[1][1] = 3700;
                        break;
                    case 8:
                        option_param[0][1] = 20;
                        break;
                    case 51:
                        option_param[0][1] = 20;
                        option_param[1][1] = 8;
                        break;
                    case 11:
                        option_param[0][1] = 100;
                        option_param[1][1] = 20;
                        break;
                    case 52:
                        option_param[0][1] = 200;
                        option_param[1][1] = 80;
                        break;
                    case 172:
                        option_param[0][1] = 500;
                        option_param[1][1] = 200;
                        break;
                    case 173:
                        option_param[0][1] = 1000;
                        option_param[1][1] = 400;
                        break;
                    case 174:
                        option_param[0][1] = 2000;
                        option_param[1][1] = 800;
                        break;
                    case 175:
                        option_param[0][1] = 4000;
                        option_param[1][1] = 1600;
                        break;
                    case 250:
                        option_param[0][1] = 12000;
                        option_param[1][1] = 2100;
                        break;
                    case 251:
                        option_param[0][1] = 16000;
                        option_param[1][1] = 2600;
                        break;
                    case 252:
                        option_param[0][1] = 20000;
                        option_param[1][1] = 3100;
                        break;
                    case 253:
                        option_param[0][1] = 24000;
                        option_param[1][1] = 3600;
                        break;
                    case 556: // quần thần trái đất
                        option_param[0][0] = 22; // hp
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 65;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                        break;
                    case 558: // quần thần namếc
                        option_param[0][0] = 22; // hp
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 55;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                        break;
                    case 560: // quần thần xayda
                        option_param[0][0] = 22; // hp
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 60;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                        break;
                    case 651: // quần huỷ diệt trái đất
                        option_param[0][0] = 22; // hp
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 100;
                        option_param[1][1] = 25000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 653: // quần huỷ diệt namếc
                        option_param[0][0] = 22; // hp
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 90;
                        option_param[1][1] = 22000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 655: // quần huỷ diệt xayda
                        option_param[0][0] = 22; // hp
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 80;
                        option_param[1][1] = 24000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                }
                break;
            case 2: // găng
                option_param[0][0] = 0; // sđ
                switch (tempId) {
                    case 21:
                        option_param[0][1] = 4;
                        break;
                    case 24:
                        option_param[0][1] = 7;
                        break;
                    case 37:
                        option_param[0][1] = 14;
                        break;
                    case 38:
                        option_param[0][1] = 28;
                        break;
                    case 144:
                        option_param[0][1] = 55;
                        break;
                    case 145:
                        option_param[0][1] = 110;
                        break;
                    case 146:
                        option_param[0][1] = 220;
                        break;
                    case 147:
                        option_param[0][1] = 530;
                        break;
                    case 254:
                        option_param[0][1] = 680;
                        break;
                    case 255:
                        option_param[0][1] = 1000;
                        break;
                    case 256:
                        option_param[0][1] = 1500;
                        break;
                    case 257:
                        option_param[0][1] = 2200;
                        break;
                    case 22:
                        option_param[0][1] = 3;
                        break;
                    case 46:
                        option_param[0][1] = 6;
                        break;
                    case 25:
                        option_param[0][1] = 12;
                        break;
                    case 45:
                        option_param[0][1] = 24;
                        break;
                    case 160:
                        option_param[0][1] = 50;
                        break;
                    case 161:
                        option_param[0][1] = 100;
                        break;
                    case 162:
                        option_param[0][1] = 200;
                        break;
                    case 163:
                        option_param[0][1] = 500;
                        break;
                    case 258:
                        option_param[0][1] = 630;
                        break;
                    case 259:
                        option_param[0][1] = 950;
                        break;
                    case 260:
                        option_param[0][1] = 1450;
                        break;
                    case 261:
                        option_param[0][1] = 2150;
                        break;
                    case 23:
                        option_param[0][1] = 5;
                        break;
                    case 53:
                        option_param[0][1] = 8;
                        break;
                    case 26:
                        option_param[0][1] = 16;
                        break;
                    case 54:
                        option_param[0][1] = 32;
                        break;
                    case 176:
                        option_param[0][1] = 60;
                        break;
                    case 177:
                        option_param[0][1] = 120;
                        break;
                    case 178:
                        option_param[0][1] = 240;
                        break;
                    case 179:
                        option_param[0][1] = 560;
                        break;
                    case 262:
                        option_param[0][1] = 700;
                        break;
                    case 263:
                        option_param[0][1] = 1050;
                        break;
                    case 264:
                        option_param[0][1] = 1550;
                        break;
                    case 265:
                        option_param[0][1] = 2250;
                        break;
                    case 562: // găng thần trái đất
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 3200;
                        option_param[2][1] = 17;
                        break;
                    case 564: // găng thần namếc
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 3000;
                        option_param[2][1] = 17;
                        break;
                    case 566: // găng thần xayda
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 3500;
                        option_param[2][1] = 17;
                        break;
                    case 657: // găng huỷ diệt trái đất
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 7000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 659: // găng huỷ diệt namếc
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 6500;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 661: // găng huỷ diệt xayda
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 7500;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                }
                break;
            case 3: // giày
                option_param[0][0] = 7; // ki
                option_param[1][0] = 28; // ki hồi /30s
                switch (tempId) {
                    case 27:
                        option_param[0][1] = 10;
                        break;
                    case 30:
                        option_param[0][1] = 25;
                        option_param[1][1] = 5;
                        break;
                    case 39:
                        option_param[0][1] = 120;
                        option_param[1][1] = 24;
                        break;
                    case 40:
                        option_param[0][1] = 250;
                        option_param[1][1] = 50;
                        break;
                    case 148:
                        option_param[0][1] = 500;
                        option_param[1][1] = 100;
                        break;
                    case 149:
                        option_param[0][1] = 1200;
                        option_param[1][1] = 240;
                        break;
                    case 150:
                        option_param[0][1] = 2400;
                        option_param[1][1] = 480;
                        break;
                    case 151:
                        option_param[0][1] = 5000;
                        option_param[1][1] = 1000;
                        break;
                    case 266:
                        option_param[0][1] = 9000;
                        option_param[1][1] = 1500;
                        break;
                    case 267:
                        option_param[0][1] = 14000;
                        option_param[1][1] = 2000;
                        break;
                    case 268:
                        option_param[0][1] = 19000;
                        option_param[1][1] = 2500;
                        break;
                    case 269:
                        option_param[0][1] = 24000;
                        option_param[1][1] = 3000;
                        break;
                    case 28:
                        option_param[0][1] = 15;
                        break;
                    case 47:
                        option_param[0][1] = 30;
                        option_param[1][1] = 6;
                        break;
                    case 31:
                        option_param[0][1] = 150;
                        option_param[1][1] = 30;
                        break;
                    case 48:
                        option_param[0][1] = 300;
                        option_param[1][1] = 60;
                        break;
                    case 164:
                        option_param[0][1] = 600;
                        option_param[1][1] = 120;
                        break;
                    case 165:
                        option_param[0][1] = 1500;
                        option_param[1][1] = 300;
                        break;
                    case 166:
                        option_param[0][1] = 3000;
                        option_param[1][1] = 600;
                        break;
                    case 167:
                        option_param[0][1] = 6000;
                        option_param[1][1] = 1200;
                        break;
                    case 270:
                        option_param[0][1] = 10000;
                        option_param[1][1] = 1700;
                        break;
                    case 271:
                        option_param[0][1] = 15000;
                        option_param[1][1] = 2200;
                        break;
                    case 272:
                        option_param[0][1] = 20000;
                        option_param[1][1] = 2700;
                        break;
                    case 273:
                        option_param[0][1] = 25000;
                        option_param[1][1] = 3200;
                        break;
                    case 29:
                        option_param[0][1] = 10;
                        break;
                    case 55:
                        option_param[0][1] = 20;
                        option_param[1][1] = 4;
                        break;
                    case 32:
                        option_param[0][1] = 100;
                        option_param[1][1] = 20;
                        break;
                    case 56:
                        option_param[0][1] = 200;
                        option_param[1][1] = 40;
                        break;
                    case 180:
                        option_param[0][1] = 400;
                        option_param[1][1] = 80;
                        break;
                    case 181:
                        option_param[0][1] = 1000;
                        option_param[1][1] = 200;
                        break;
                    case 182:
                        option_param[0][1] = 2000;
                        option_param[1][1] = 400;
                        break;
                    case 183:
                        option_param[0][1] = 4000;
                        option_param[1][1] = 800;
                        break;
                    case 274:
                        option_param[0][1] = 8000;
                        option_param[1][1] = 1300;
                        break;
                    case 275:
                        option_param[0][1] = 13000;
                        option_param[1][1] = 1800;
                        break;
                    case 276:
                        option_param[0][1] = 18000;
                        option_param[1][1] = 2300;
                        break;
                    case 277:
                        option_param[0][1] = 23000;
                        option_param[1][1] = 2800;
                        break;
                    case 563: // giày thần trái đất
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 40;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                        break;
                    case 565: // giày thần namếc
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 60;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                        break;
                    case 567: // giày thần xayda
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 50;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                        break;
                    case 658: // giày huỷ diệt trái đất
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 80;
                        option_param[1][1] = 22000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 660: // giày huỷ diệt namếc
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 90;
                        option_param[1][1] = 25000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                    case 662: // giày huỷ diệt xayda
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 85;
                        option_param[1][1] = 20000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                        break;
                }
                break;
            case 4: // rada
                option_param[0][0] = 14; // crit
                switch (tempId) {
                    case 12:
                        option_param[0][1] = 1;
                        break;
                    case 57:
                        option_param[0][1] = 2;
                        break;
                    case 58:
                        option_param[0][1] = 3;
                        break;
                    case 59:
                        option_param[0][1] = 4;
                        break;
                    case 184:
                        option_param[0][1] = 5;
                        break;
                    case 185:
                        option_param[0][1] = 6;
                        break;
                    case 186:
                        option_param[0][1] = 7;
                        break;
                    case 187:
                        option_param[0][1] = 8;
                        break;
                    case 278:
                        option_param[0][1] = 9;
                        break;
                    case 279:
                        option_param[0][1] = 10;
                        break;
                    case 280:
                        option_param[0][1] = 11;
                        break;
                    case 281:
                        option_param[0][1] = 12;
                        break;
                    case 561: // nhẫn thần linh
                        option_param[2][0] = 21; // yêu cầu sức mạnh

                        option_param[0][1] = 16;
                        option_param[2][1] = 18;

                        break;
                    case 656: // nhẫn huỷ diệt
                        option_param[2][0] = 21; // yêu cầu sức mạnh
                        option_param[3][0] = 30; // không thể gd

                        option_param[0][1] = 18;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1; // không thể gd
                        break;
                }
                break;
        }

        for (int i = 0; i < option_param.length; i++) {
            if (option_param[i][0] != -1 && option_param[i][1] != -1) {
                if (option_param[i][0] == 21) {
                    list.add(new ItemOption(option_param[i][0],
                            (option_param[i][1])));
                } else {

                    list.add(new ItemOption(option_param[i][0],
                            (option_param[i][1] + Util.nextInt(-(option_param[i][1] * 10 / 100),
                                    option_param[i][1] * 10 / 100))));
                }

            }
        }

    }

    //chỉ số cơ bản: hp, ki, hồi phục, sđ, crit
    public void initBaseOptionClothes(Item itemid, int tempId, int type, List<ItemOption> list) {
        int[][] option_param = {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};
        switch (type) {
            case 0: //áo
                option_param[0][0] = 47; //giáp
                switch (tempId) {
                    case 0:
                        option_param[0][1] = 2;
                        break;
                    case 33:
                        option_param[0][1] = 4;
                        break;
                    case 3:
                        option_param[0][1] = 8;
                        break;
                    case 34:
                        option_param[0][1] = 16;
                        break;
                    case 136:
                        option_param[0][1] = 24;
                        break;
                    case 137:
                        option_param[0][1] = 40;
                        break;
                    case 138:
                        option_param[0][1] = 60;
                        break;
                    case 139:
                        option_param[0][1] = 90;
                        break;
                    case 230:
                        option_param[0][1] = 200;
                        break;
                    case 231:
                        option_param[0][1] = 250;
                        break;
                    case 232:
                        option_param[0][1] = 300;
                        break;
                    case 233:
                        option_param[0][1] = 400;
                        break;
                    case 1:
                        option_param[0][1] = 2;
                        break;
                    case 41:
                        option_param[0][1] = 4;
                        break;
                    case 4:
                        option_param[0][1] = 8;
                        break;
                    case 42:
                        option_param[0][1] = 16;
                        break;
                    case 152:
                        option_param[0][1] = 24;
                        break;
                    case 153:
                        option_param[0][1] = 40;
                        break;
                    case 154:
                        option_param[0][1] = 60;
                        break;
                    case 155:
                        option_param[0][1] = 90;
                        break;
                    case 234:
                        option_param[0][1] = 200;
                        break;
                    case 235:
                        option_param[0][1] = 250;
                        break;
                    case 236:
                        option_param[0][1] = 300;
                        break;
                    case 237:
                        option_param[0][1] = 400;
                        break;
                    case 2:
                        option_param[0][1] = 3;
                        break;
                    case 49:
                        option_param[0][1] = 5;
                        break;
                    case 5:
                        option_param[0][1] = 10;
                        break;
                    case 50:
                        option_param[0][1] = 20;
                        break;
                    case 168:
                        option_param[0][1] = 30;
                        break;
                    case 169:
                        option_param[0][1] = 50;
                        break;
                    case 170:
                        option_param[0][1] = 70;
                        break;
                    case 171:
                        option_param[0][1] = 100;
                        break;
                    case 238:
                        option_param[0][1] = 230;
                        break;
                    case 239:
                        option_param[0][1] = 280;
                        break;
                    case 240:
                        option_param[0][1] = 330;
                        break;
                    case 241:
                        option_param[0][1] = 450;
                        break;
                    case 555: //áo thần trái đất
                        option_param[2][0] = 21; //yêu cầu sức mạnh
                        option_param[0][1] = 800;
                        option_param[2][1] = 15;
                        break;
                    case 557: //áo thần namếc
                        option_param[2][0] = 21; //yêu cầu sức mạnh
                        option_param[0][1] = 800;
                        option_param[2][1] = 15;
                        break;
                    case 559: //áo thần xayda
                        option_param[2][0] = 21; //yêu cầu sức mạnh
                        option_param[0][1] = 800;
                        option_param[2][1] = 15;
                        break;
                }
                break;
            case 1: //quần
                option_param[0][0] = 6; //hp
                option_param[1][0] = 27; //hp hồi/30s
                switch (tempId) {
                    case 6:
                        option_param[0][1] = 30;
                        break;
                    case 35:
                        option_param[0][1] = 150;
                        option_param[1][1] = 12;
                        break;
                    case 9:
                        option_param[0][1] = 300;
                        option_param[1][1] = 40;
                        break;
                    case 36:
                        option_param[0][1] = 600;
                        option_param[1][1] = 120;
                        break;
                    case 140:
                        option_param[0][1] = 1400;
                        option_param[1][1] = 280;
                        break;
                    case 141:
                        option_param[0][1] = 3000;
                        option_param[1][1] = 600;
                        break;
                    case 142:
                        option_param[0][1] = 6000;
                        option_param[1][1] = 1200;
                        break;
                    case 143:
                        option_param[0][1] = 10000;
                        option_param[1][1] = 2000;
                        break;
                    case 242:
                        option_param[0][1] = 14000;
                        option_param[1][1] = 2500;
                        break;
                    case 243:
                        option_param[0][1] = 18000;
                        option_param[1][1] = 3000;
                        break;
                    case 244:
                        option_param[0][1] = 22000;
                        option_param[1][1] = 3500;
                        break;
                    case 245:
                        option_param[0][1] = 26000;
                        option_param[1][1] = 4000;
                        break;
                    case 7:
                        option_param[0][1] = 20;
                        break;
                    case 43:
                        option_param[0][1] = 25;
                        option_param[1][1] = 10;
                        break;
                    case 10:
                        option_param[0][1] = 120;
                        option_param[1][1] = 28;
                        break;
                    case 44:
                        option_param[0][1] = 250;
                        option_param[1][1] = 100;
                        break;
                    case 156:
                        option_param[0][1] = 600;
                        option_param[1][1] = 240;
                        break;
                    case 157:
                        option_param[0][1] = 1200;
                        option_param[1][1] = 480;
                        break;
                    case 158:
                        option_param[0][1] = 2400;
                        option_param[1][1] = 960;
                        break;
                    case 159:
                        option_param[0][1] = 4800;
                        option_param[1][1] = 1800;
                        break;
                    case 246:
                        option_param[0][1] = 13000;
                        option_param[1][1] = 2200;
                        break;
                    case 247:
                        option_param[0][1] = 17000;
                        option_param[1][1] = 2700;
                        break;
                    case 248:
                        option_param[0][1] = 21000;
                        option_param[1][1] = 3200;
                        break;
                    case 249:
                        option_param[0][1] = 25000;
                        option_param[1][1] = 3700;
                        break;
                    case 8:
                        option_param[0][1] = 20;
                        break;
                    case 51:
                        option_param[0][1] = 20;
                        option_param[1][1] = 8;
                        break;
                    case 11:
                        option_param[0][1] = 100;
                        option_param[1][1] = 20;
                        break;
                    case 52:
                        option_param[0][1] = 200;
                        option_param[1][1] = 80;
                        break;
                    case 172:
                        option_param[0][1] = 500;
                        option_param[1][1] = 200;
                        break;
                    case 173:
                        option_param[0][1] = 1000;
                        option_param[1][1] = 400;
                        break;
                    case 174:
                        option_param[0][1] = 2000;
                        option_param[1][1] = 800;
                        break;
                    case 175:
                        option_param[0][1] = 4000;
                        option_param[1][1] = 1600;
                        break;
                    case 250:
                        option_param[0][1] = 12000;
                        option_param[1][1] = 2100;
                        break;
                    case 251:
                        option_param[0][1] = 16000;
                        option_param[1][1] = 2600;
                        break;
                    case 252:
                        option_param[0][1] = 20000;
                        option_param[1][1] = 3100;
                        break;
                    case 253:
                        option_param[0][1] = 24000;
                        option_param[1][1] = 3600;
                        break;
                    case 556: //quần thần trái đất
                        option_param[0][0] = 22; //hp
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 52;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                        break;
                    case 558: //quần thần namếc
                        option_param[0][0] = 22; //hp
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 50;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                        break;
                    case 560: //quần thần xayda
                        option_param[0][0] = 22; //hp
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 48;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                        break;
                }
                break;
            case 2: //găng
                option_param[0][0] = 0; //sđ
                switch (tempId) {
                    case 21:
                        option_param[0][1] = 4;
                        break;
                    case 24:
                        option_param[0][1] = 7;
                        break;
                    case 37:
                        option_param[0][1] = 14;
                        break;
                    case 38:
                        option_param[0][1] = 28;
                        break;
                    case 144:
                        option_param[0][1] = 55;
                        break;
                    case 145:
                        option_param[0][1] = 110;
                        break;
                    case 146:
                        option_param[0][1] = 220;
                        break;
                    case 147:
                        option_param[0][1] = 530;
                        break;
                    case 254:
                        option_param[0][1] = 680;
                        break;
                    case 255:
                        option_param[0][1] = 1000;
                        break;
                    case 256:
                        option_param[0][1] = 1500;
                        break;
                    case 257:
                        option_param[0][1] = 2200;
                        break;
                    case 22:
                        option_param[0][1] = 3;
                        break;
                    case 46:
                        option_param[0][1] = 6;
                        break;
                    case 25:
                        option_param[0][1] = 12;
                        break;
                    case 45:
                        option_param[0][1] = 24;
                        break;
                    case 160:
                        option_param[0][1] = 50;
                        break;
                    case 161:
                        option_param[0][1] = 100;
                        break;
                    case 162:
                        option_param[0][1] = 200;
                        break;
                    case 163:
                        option_param[0][1] = 500;
                        break;
                    case 258:
                        option_param[0][1] = 630;
                        break;
                    case 259:
                        option_param[0][1] = 950;
                        break;
                    case 260:
                        option_param[0][1] = 1450;
                        break;
                    case 261:
                        option_param[0][1] = 2150;
                        break;
                    case 23:
                        option_param[0][1] = 5;
                        break;
                    case 53:
                        option_param[0][1] = 8;
                        break;
                    case 26:
                        option_param[0][1] = 16;
                        break;
                    case 54:
                        option_param[0][1] = 32;
                        break;
                    case 176:
                        option_param[0][1] = 60;
                        break;
                    case 177:
                        option_param[0][1] = 120;
                        break;
                    case 178:
                        option_param[0][1] = 240;
                        break;
                    case 179:
                        option_param[0][1] = 560;
                        break;
                    case 262:
                        option_param[0][1] = 700;
                        break;
                    case 263:
                        option_param[0][1] = 1050;
                        break;
                    case 264:
                        option_param[0][1] = 1550;
                        break;
                    case 265:
                        option_param[0][1] = 2250;
                        break;
                    case 562: //găng thần trái đất
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 3700;
                        option_param[2][1] = 17;
                        break;
                    case 564: //găng thần namếc
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 3500;
                        option_param[2][1] = 17;
                        break;
                    case 566: //găng thần xayda
                        option_param[2][0] = 21; //yêu cầu sức mạnh
                        option_param[0][1] = 3800;
                        option_param[2][1] = 17;
                        break;
                }
                break;
            case 3: //giày
                option_param[0][0] = 7; //ki
                option_param[1][0] = 28; //ki hồi /30s
                switch (tempId) {
                    case 27:
                        option_param[0][1] = 10;
                        break;
                    case 30:
                        option_param[0][1] = 25;
                        option_param[1][1] = 5;
                        break;
                    case 39:
                        option_param[0][1] = 120;
                        option_param[1][1] = 24;
                        break;
                    case 40:
                        option_param[0][1] = 250;
                        option_param[1][1] = 50;
                        break;
                    case 148:
                        option_param[0][1] = 500;
                        option_param[1][1] = 100;
                        break;
                    case 149:
                        option_param[0][1] = 1200;
                        option_param[1][1] = 240;
                        break;
                    case 150:
                        option_param[0][1] = 2400;
                        option_param[1][1] = 480;
                        break;
                    case 151:
                        option_param[0][1] = 5000;
                        option_param[1][1] = 1000;
                        break;
                    case 266:
                        option_param[0][1] = 9000;
                        option_param[1][1] = 1500;
                        break;
                    case 267:
                        option_param[0][1] = 14000;
                        option_param[1][1] = 2000;
                        break;
                    case 268:
                        option_param[0][1] = 19000;
                        option_param[1][1] = 2500;
                        break;
                    case 269:
                        option_param[0][1] = 24000;
                        option_param[1][1] = 3000;
                        break;
                    case 28:
                        option_param[0][1] = 15;
                        break;
                    case 47:
                        option_param[0][1] = 30;
                        option_param[1][1] = 6;
                        break;
                    case 31:
                        option_param[0][1] = 150;
                        option_param[1][1] = 30;
                        break;
                    case 48:
                        option_param[0][1] = 300;
                        option_param[1][1] = 60;
                        break;
                    case 164:
                        option_param[0][1] = 600;
                        option_param[1][1] = 120;
                        break;
                    case 165:
                        option_param[0][1] = 1500;
                        option_param[1][1] = 300;
                        break;
                    case 166:
                        option_param[0][1] = 3000;
                        option_param[1][1] = 600;
                        break;
                    case 167:
                        option_param[0][1] = 6000;
                        option_param[1][1] = 1200;
                        break;
                    case 270:
                        option_param[0][1] = 10000;
                        option_param[1][1] = 1700;
                        break;
                    case 271:
                        option_param[0][1] = 15000;
                        option_param[1][1] = 2200;
                        break;
                    case 272:
                        option_param[0][1] = 20000;
                        option_param[1][1] = 2700;
                        break;
                    case 273:
                        option_param[0][1] = 25000;
                        option_param[1][1] = 3200;
                        break;
                    case 29:
                        option_param[0][1] = 10;
                        break;
                    case 55:
                        option_param[0][1] = 20;
                        option_param[1][1] = 4;
                        break;
                    case 32:
                        option_param[0][1] = 100;
                        option_param[1][1] = 20;
                        break;
                    case 56:
                        option_param[0][1] = 200;
                        option_param[1][1] = 40;
                        break;
                    case 180:
                        option_param[0][1] = 400;
                        option_param[1][1] = 80;
                        break;
                    case 181:
                        option_param[0][1] = 1000;
                        option_param[1][1] = 200;
                        break;
                    case 182:
                        option_param[0][1] = 2000;
                        option_param[1][1] = 400;
                        break;
                    case 183:
                        option_param[0][1] = 4000;
                        option_param[1][1] = 800;
                        break;
                    case 274:
                        option_param[0][1] = 8000;
                        option_param[1][1] = 1300;
                        break;
                    case 275:
                        option_param[0][1] = 13000;
                        option_param[1][1] = 1800;
                        break;
                    case 276:
                        option_param[0][1] = 18000;
                        option_param[1][1] = 2300;
                        break;
                    case 277:
                        option_param[0][1] = 23000;
                        option_param[1][1] = 2800;
                        break;
                    case 563: //giày thần trái đất
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 48;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                        break;
                    case 565: //giày thần namếc
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 48;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                        break;
                    case 567: //giày thần xayda
                        option_param[0][0] = 23;
                        option_param[2][0] = 21; //yêu cầu sức mạnh

                        option_param[0][1] = 46;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                        break;
                }
                break;
            case 4: //rada
                option_param[0][0] = 14; //crit
                switch (tempId) {
                    case 12:
                        option_param[0][1] = 1;
                        break;
                    case 57:
                        option_param[0][1] = 2;
                        break;
                    case 58:
                        option_param[0][1] = 3;
                        break;
                    case 59:
                        option_param[0][1] = 4;
                        break;
                    case 184:
                        option_param[0][1] = 5;
                        break;
                    case 185:
                        option_param[0][1] = 6;
                        break;
                    case 186:
                        option_param[0][1] = 7;
                        break;
                    case 187:
                        option_param[0][1] = 8;
                        break;
                    case 278:
                        option_param[0][1] = 9;
                        break;
                    case 279:
                        option_param[0][1] = 10;
                        break;
                    case 280:
                        option_param[0][1] = 11;
                        break;
                    case 281:
                        option_param[0][1] = 12;
                        break;
                    case 561: //nhẫn thần linh
                        option_param[2][0] = 21; //yêu cầu sức mạnh
                        option_param[0][1] = 15;
                        option_param[2][1] = 18;
                        break;
                }
                break;
        }
        for (int i = 0; i < option_param.length; i++) {
            if (option_param[i][0] != -1 && option_param[i][1] != -1) {

                int randomPercent = Util.nextInt(101, 110);
                int value = (int) (option_param[i][1] * (randomPercent / 100.0));
                list.add(new ItemOption(option_param[i][0], value));
            }
        }

    }

    public void initActivationOption(int gender, int type, List<ItemOption> list) {
        if (type <= 4) {
            int[] idOption = ACTIVATION_SET[gender][Util.nextInt(0, ACTIVATION_SET[gender].length - 1)];
            list.add(new ItemOption(idOption[0], 1)); // tên set
            if (idOption[1] > 0) {
                list.add(new ItemOption(idOption[1], 1)); // hiệu ứng set
            }
            list.add(new ItemOption(30, 7)); // không thể giao dịch
        }
    }

    public Item rewardCapsuleTet(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (Util.isTrue(40, 100)) {
                int[] items = {734, 920, 849, 743, 733};
                Item item = ItemService.gI().createNewItem((short) items[Util.nextInt(items.length)]);
                byte[] option = {77, 80, 81, 103, 50, 94, 5};
                byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
                if (Util.isTrue(5, 100)) {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                    item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
                } else {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 10)));
                    if (Util.isTrue(1, 10)) {
                        item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 10)));
                    }
                    item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
                }
                item.itemOptions.add(new Item.ItemOption(89, 0));
                item.itemOptions.add(new Item.ItemOption(30, 0));
                return item;
            } else if (Util.isTrue(50, 100)) {
                int[] items = {942, 943, 944};
                Item item = ItemService.gI().createNewItem((short) items[Util.nextInt(items.length)]);
                byte[] option = {77, 80, 81, 103, 50, 94, 5};
                byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
                if (Util.isTrue(5, 100)) {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                    item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
                } else {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 10)));
                    if (Util.isTrue(1, 10)) {
                        item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 10)));
                    }
                    item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
                }
                item.itemOptions.add(new Item.ItemOption(30, 0));
                return item;
            } else {
                Item it = ItemService.gI().createNewItem((short) Util.nextInt(2148, 2152));
                it.quantity = 1;
                if (Util.isTrue(5, 100)) {
                    it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 20)));
                    it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 20)));
                    it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 20)));
                    it.itemOptions.add(new Item.ItemOption(94, Util.nextInt(20, 20)));
                    it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(2, 20)));
                    it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(2, 10)));
                    if (Util.isTrue(5, 10)) {
                        it.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 14)));
                    }
                    it.itemOptions.add(new Item.ItemOption(154, 0));
                } else {
                    it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 20)));
                    it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 20)));
                    if (Util.isTrue(5, 30)) {
                        it.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 5)));
                    }
                    it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 20)));
                    it.itemOptions.add(new Item.ItemOption(94, Util.nextInt(20, 10)));
                    it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(2, 10)));
                    it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 15)));
                }
                byte[] option = {80, 81, 103, 50, 94, 5};
                byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
                if (Util.isTrue(20, 100)) {
                    it.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                }
                if (Util.isTrue(20, 100)) {
                    it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 10)));
                }
                if (Util.isTrue(20, 100)) {
                    it.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
                }
                return it;
            }
        } else {
            Service.gI().sendThongBao(player, "Cần 1 ô hành trang trống");
            return null;
        }
    }

}
