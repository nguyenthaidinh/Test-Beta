package nro.models.boss.event.Halloween;

import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.service.NpcService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.GoldBarSpendService;
import nro.models.services.ItemService;
import nro.models.services.Service;

public final class HalloweenExchangeService {

    private static final int BOX_CARD_COST = 20;
    private static final int BOX_PUMPKIN_COST = 200;
    private static final int COSTUME_CARD_COST = 50;
    private static final int COSTUME_PUMPKIN_COST = 500;
    private static final int PET_CARD_COST = 400;
    private static final int PET_PUMPKIN_COST = 4_000;
    private static final int HAND_CANDY_COST = 100;
    private static final int DEVIL_CANDY_BOX_GOLD_BAR_COST = 200;

    private HalloweenExchangeService() {
    }

    public static void openExchangeMenu(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_HALLOWEEN_EXCHANGE, -1,
                createMenuText(player), createMenuOptions());
    }

    public static void openExchangeMenu(Player player, Npc npc) {
        if (npc == null) {
            openExchangeMenu(player);
            return;
        }
        npc.createOtherMenu(player, ConstNpc.MENU_HALLOWEEN_EXCHANGE, createMenuText(player), createMenuOptions());
    }

    public static void openHandCandyExchangeMenu(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_HAND_CANDY_EXCHANGE, -1,
                createHandCandyMenuText(player), createHandCandyMenuOptions());
    }

    public static void openHandCandyExchangeMenu(Player player, Npc npc) {
        if (npc == null) {
            openHandCandyExchangeMenu(player);
            return;
        }
        npc.createOtherMenu(player, ConstNpc.MENU_HAND_CANDY_EXCHANGE,
                createHandCandyMenuText(player), createHandCandyMenuOptions());
    }

    public static void handleExchange(Player player, int select) {
        switch (select) {
            case 0:
                exchange(player, BOX_CARD_COST, BOX_PUMPKIN_COST,
                        ItemService.gI().createNewItem((short) ConstItem.HOM_HALLOWEEN, 1));
                break;
            case 1:
                exchange(player, COSTUME_CARD_COST, COSTUME_PUMPKIN_COST,
                        HalloweenRewards.createHalloweenCostumeReward());
                break;
            case 2:
                exchange(player, PET_CARD_COST, PET_PUMPKIN_COST,
                        HalloweenRewards.createHalloweenPetReward());
                break;
            case 3:
                buyDevilCandyBox(player);
                break;
            case 4:
                removeExpireFromHalloweenItems(player);
                break;
            default:
                break;
        }
    }

    public static void handleHandCandyExchange(Player player, int select) {
        switch (select) {
            case 0:
                exchangeHandCandyForDevilCandyBox(player);
                break;
            default:
                break;
        }
    }

    private static String createMenuText(Player player) {
        int cardCount = countItemBag(player, ConstItem.THIEP_HALLOWEEN);
        int pumpkinCount = countItemBag(player, ConstItem.BI_NGO);
        int goldBarCount = countItemBag(player, ConstItem.THOI_VANG);
        return "Đổi quà Halloween"
                + "\nĐang có: " + cardCount + " Thiệp, " + pumpkinCount + " Bí ngô, "
                + goldBarCount + " Thỏi vàng"
                + "\n\n20 Thiệp + 200 Bí ngô = 1 Hòm Halloween"
                + "\n50 Thiệp + 500 Bí ngô = Cải trang Halloween random"
                + "\n400 Thiệp + 4000 Bí ngô = Pet Bí Ma Vương"
                + "\n200 Thỏi vàng = 1 Hộp Kẹo Ma Quỷ"
                + "\nXóa vật phẩm Halloween HSD trong hành trang và rương";
    }

    private static String[] createMenuOptions() {
        return new String[]{
            "Đổi\nHòm",
            "Đổi\nCải trang",
            "Đổi\nPet",
            "Mua\nHộp Kẹo\n200 TV",
            "Xóa\nHSD",
            "Đóng"
        };
    }

    private static String createHandCandyMenuText(Player player) {
        int handCandyCount = countItemBag(player, ConstItem.KEO_BAN_TAY);
        return "Đổi Kẹo bàn tay"
                + "\nĐang có: " + handCandyCount + " Kẹo bàn tay"
                + "\n\n100 Kẹo bàn tay = 1 Hộp Kẹo Ma Quỷ";
    }

    private static String[] createHandCandyMenuOptions() {
        return new String[]{
            "Đổi\nHộp\nKẹo",
            "Đóng"
        };
    }

    private static void exchange(Player player, int cardCost, int pumpkinCost, Item reward) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }

        int cardCount = countItemBag(player, ConstItem.THIEP_HALLOWEEN);
        int pumpkinCount = countItemBag(player, ConstItem.BI_NGO);
        if (cardCount < cardCost || pumpkinCount < pumpkinCost) {
            Service.gI().sendThongBao(player, "Không đủ nguyên liệu. Cần " + cardCost
                    + " Thiệp Halloween và " + pumpkinCost + " Bí ngô.");
            return;
        }
        if (reward == null || !reward.isNotNullItem()) {
            Service.gI().sendThongBao(player, "Không thể tạo phần thưởng Halloween.");
            return;
        }
        if (!InventoryService.gI().addItemBag(player, reward)) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }

        subItemBag(player, ConstItem.THIEP_HALLOWEEN, cardCost);
        subItemBag(player, ConstItem.BI_NGO, pumpkinCost);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Đổi thành công " + reward.template.name + ".");
    }

    private static void buyDevilCandyBox(Player player) {
        int goldBarCount = countItemBag(player, ConstItem.THOI_VANG);
        if (goldBarCount < DEVIL_CANDY_BOX_GOLD_BAR_COST) {
            Service.gI().sendThongBao(player, "Không đủ thỏi vàng. Cần "
                    + DEVIL_CANDY_BOX_GOLD_BAR_COST + " thỏi vàng.");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0
                && !canCreateEmptySlotAfterSubItem(player, ConstItem.THOI_VANG,
                        DEVIL_CANDY_BOX_GOLD_BAR_COST)) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }

        Item reward = ItemService.gI().createNewItem((short) ConstItem.HOP_KEO_MA_QUY, 1);
        if (reward == null || !reward.isNotNullItem()) {
            Service.gI().sendThongBao(player, "Không thể tạo Hộp Kẹo Ma Quỷ.");
            return;
        }

        subItemBag(player, ConstItem.THOI_VANG, DEVIL_CANDY_BOX_GOLD_BAR_COST);
        if (!InventoryService.gI().addItemBag(player, reward)) {
            InventoryService.gI().addItemBag(player,
                    ItemService.gI().createNewItem((short) ConstItem.THOI_VANG,
                            DEVIL_CANDY_BOX_GOLD_BAR_COST));
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }

        GoldBarSpendService.gI().addPoint(player, DEVIL_CANDY_BOX_GOLD_BAR_COST);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Mua thành công " + reward.template.name
                + " với giá " + DEVIL_CANDY_BOX_GOLD_BAR_COST + " thỏi vàng.");
    }

    private static void exchangeHandCandyForDevilCandyBox(Player player) {
        int handCandyCount = countItemBag(player, ConstItem.KEO_BAN_TAY);
        if (handCandyCount < HAND_CANDY_COST) {
            Service.gI().sendThongBao(player, "Không đủ Kẹo bàn tay. Cần "
                    + HAND_CANDY_COST + " Kẹo bàn tay.");
            return;
        }

        Item reward = ItemService.gI().createNewItem((short) ConstItem.HOP_KEO_MA_QUY, 1);
        if (reward == null || !reward.isNotNullItem()) {
            Service.gI().sendThongBao(player, "Không thể tạo Hộp Kẹo Ma Quỷ.");
            return;
        }

        subItemBag(player, ConstItem.KEO_BAN_TAY, HAND_CANDY_COST);
        if (!InventoryService.gI().addItemBag(player, reward)) {
            InventoryService.gI().addItemBag(player,
                    ItemService.gI().createNewItem((short) ConstItem.KEO_BAN_TAY, HAND_CANDY_COST));
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Đổi thành công " + reward.template.name + ".");
    }

    private static void removeExpireFromHalloweenItems(Player player) {
        if (player == null || player.inventory == null) {
            return;
        }

        int removed = removeExpireFromBag(player)
                + removeExpireFromBox(player);
        if (removed > 0) {
            InventoryService.gI().sendItemBags(player);
            InventoryService.gI().sendItemBox(player);
            Service.gI().sendThongBao(player, "Đã xóa " + removed
                    + " vật phẩm Halloween HSD.");
        } else {
            Service.gI().sendThongBao(player,
                    "Không có vật phẩm Halloween HSD trong hành trang hoặc rương.");
        }
    }

    private static int removeExpireFromBag(Player player) {
        if (player.inventory.itemsBag == null) {
            return 0;
        }

        int removed = 0;
        for (Item item : player.inventory.itemsBag) {
            if (HalloweenRewards.isTimedOpenedRewardWithExpire(item)) {
                InventoryService.gI().subQuantityItemsBag(player, item, Math.max(1, item.quantity));
                removed++;
            }
        }
        return removed;
    }

    private static int removeExpireFromBox(Player player) {
        if (player.inventory.itemsBox == null) {
            return 0;
        }

        int removed = 0;
        for (Item item : player.inventory.itemsBox) {
            if (HalloweenRewards.isTimedOpenedRewardWithExpire(item)) {
                InventoryService.gI().subQuantityItemsBox(player, item, Math.max(1, item.quantity));
                removed++;
            }
        }
        return removed;
    }

    private static int countItemBag(Player player, int itemId) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem() && item.template.id == itemId) {
                count += item.quantity;
            }
        }
        return count;
    }

    private static void subItemBag(Player player, int itemId, int quantity) {
        int remain = quantity;
        for (Item item : player.inventory.itemsBag) {
            if (remain <= 0) {
                return;
            }
            if (item != null && item.isNotNullItem() && item.template.id == itemId) {
                int quantitySub = Math.min(remain, item.quantity);
                InventoryService.gI().subQuantityItemsBag(player, item, quantitySub);
                remain -= quantitySub;
            }
        }
    }

    private static boolean canCreateEmptySlotAfterSubItem(Player player, int itemId, int quantity) {
        int remain = quantity;
        for (Item item : player.inventory.itemsBag) {
            if (item == null || !item.isNotNullItem() || item.template.id != itemId || item.quantity <= 0) {
                continue;
            }
            int quantitySub = Math.min(remain, item.quantity);
            if (quantitySub >= item.quantity) {
                return true;
            }
            remain -= quantitySub;
            if (remain <= 0) {
                return false;
            }
        }
        return false;
    }
}
