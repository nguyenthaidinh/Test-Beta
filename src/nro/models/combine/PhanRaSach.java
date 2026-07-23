package nro.models.combine;

import nro.models.consts.ConstFont;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class PhanRaSach {

    private static final int CUON_SACH_CU_ID = 1283;
    private static final int CUON_SACH_CU_REWARD = 5;
    private static final long PHAN_RA_GOLD_COST = 10_000_000L;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "Kh\u00f4ng t\u00ecm th\u1ea5y v\u1eadt ph\u1ea9m");
            return;
        }
        Item sachTuyetKy = getSachTuyetKy(player);
        if (sachTuyetKy == null) {
            Service.gI().sendDialogMessage(player, "Kh\u00f4ng t\u00ecm th\u1ea5y v\u1eadt ph\u1ea9m");
            return;
        }
        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_BLUE).append("Ph\u00e2n r\u00e3 s\u00e1ch\n");
        text.append(ConstFont.BOLD_BLUE).append("Nh\u1eadn l\u1ea1i 5 cu\u1ed1n s\u00e1ch c\u0169\n");
        text.append(player.inventory.gold >= PHAN_RA_GOLD_COST ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Ph\u00ed r\u00e3 10 tri\u1ec7u v\u00e0ng");
        if (player.inventory.gold < PHAN_RA_GOLD_COST) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(),
                    "C\u00f2n thi\u1ebfu\n" + Util.numberToMoney(PHAN_RA_GOLD_COST - player.inventory.gold) + " v\u00e0ng");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(),
                "\u0110\u1ed3ng \u00fd", "T\u1eeb ch\u1ed1i");
    }

    public static void phanRaSach(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            return;
        }
        Item sachTuyetKy = getSachTuyetKy(player);
        if (sachTuyetKy == null || player.inventory.gold < PHAN_RA_GOLD_COST) {
            return;
        }
        if (!hasSpaceForReward(player, sachTuyetKy)) {
            Service.gI().sendThongBao(player, "C\u1ea7n 1 \u00f4 tr\u1ed1ng trong h\u00e0nh trang.");
            return;
        }
        player.inventory.gold -= PHAN_RA_GOLD_COST;
        InventoryService.gI().subQuantityItemsBag(player, sachTuyetKy, 1);

        Item cuonSachCu = ItemService.gI().createNewItem((short) CUON_SACH_CU_ID, CUON_SACH_CU_REWARD);
        cuonSachCu.itemOptions.add(new Item.ItemOption(30, 0));
        InventoryService.gI().addItemBag(player, cuonSachCu);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendMoney(player);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static Item getSachTuyetKy(Player player) {
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && (item.isSachTuyetKy() || item.isSachTuyetKy2())) {
                return item;
            }
        }
        return null;
    }

    private static boolean hasSpaceForReward(Player player, Item sachTuyetKy) {
        return InventoryService.gI().getCountEmptyBag(player) > 0
                || InventoryService.gI().findItemBag(player, CUON_SACH_CU_ID) != null
                || sachTuyetKy.quantity <= 1;
    }
}
