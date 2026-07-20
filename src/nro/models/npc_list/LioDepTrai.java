package nro.models.npc_list;

import nro.models.combine.CombineService;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.shop_lio.LioShopManager;
import nro.models.shop_lio.LioShopService;

/**
 * NPC Lio Đẹp Trai - Mua bán đồ Thần Linh
 * Đặt tại làng KKR (map 5)
 * Hoạt ảnh: Mabu mập (head=297, body=298, leg=299)
 *
 * Chức năng:
 * - Bán đồ TL cho NPC: nhận 100 thỏi vàng
 * - Mua đồ TL từ shop: trả 100 thỏi vàng
 *
 * @author Lio
 */
public class LioDepTrai extends Npc {

    public LioDepTrai(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            int soLuongShop = LioShopManager.gI() != null ? LioShopManager.gI().getAvailableCount() : 0;
            String npcSay = "|2|Chào " + player.name + "! Ta là Lio Đẹp Trai!\n"
                    + "|1|Chuyên thu mua và bán lại Đồ Thần Linh.\n"
                    + "|0|Bán cho ta: Nhận " + LioShopManager.PRICE_BUY_IN + " thỏi vàng/món\n"
                    + "|0|Mỗi ngày bán tối đa " + LioShopService.DAILY_SELL_LIMIT + " món\n"
                    + "|0|Mua từ ta: " + LioShopManager.PRICE_SELL_OUT + " thỏi vàng/món\n"
                    + "|0|Khi đã Bế Tắc: Đồ Thần Linh 150 thỏi vàng/món\n"
                    + "|7|Shop reset sau mỗi lần bảo trì\n"
                    + "|2|Đang có " + soLuongShop + "/" + LioShopManager.MAX_ITEMS + " món trong shop";

            createOtherMenu(player, ConstNpc.BASE_MENU, npcSay,
                    "Bán đồ\nThần Linh", "Mua đồ\nThần Linh", "Khi đã\nBế Tắc",
                    "Mua nhanh\nSet TL\n800 TV", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.idMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                            // Bán đồ Thần Linh → mở tab combine để player đặt đồ vào
                            CombineService.gI().openTabCombine(player, CombineService.BAN_DO_THAN_LINH_LIO);
                        }
                        case 1 -> {
                            // Mua đồ Thần Linh → mở shop
                            if (LioShopManager.gI() == null) {
                                Service.gI().sendThongBao(player, "Shop đang bảo trì, vui lòng quay lại sau!");
                                return;
                            }
                            if (LioShopManager.gI().getAvailableCount() == 0) {
                                createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Shop hiện đang trống, chưa có ai bán đồ Thần Linh!", "Đóng");
                            } else {
                                LioShopService.gI().openShop(player);
                            }
                        }
                        case 2 -> {
                            LioShopService.gI().openBeTacShop(player);
                        }
                        case 3 -> {
                            createOtherMenu(player, ConstNpc.MENU_SHOP_LIO_CHON_SET_THAN_LINH,
                                    "Chọn hành tinh của Set Thần Linh muốn mua\n"
                                    + "Giá trọn bộ 5 món: 800 thỏi vàng",
                                    "Trái Đất", "Namek", "Xayda", "Đóng");
                        }
                    }
                }
                case ConstNpc.MENU_SHOP_LIO_CHON_SET_THAN_LINH -> {
                    switch (select) {
                        case 0 -> openConfirmBuySet(player,
                                ConstNpc.MENU_SHOP_LIO_CONFIRM_SET_TRAI_DAT, "Trái Đất");
                        case 1 -> openConfirmBuySet(player,
                                ConstNpc.MENU_SHOP_LIO_CONFIRM_SET_NAMEK, "Namek");
                        case 2 -> openConfirmBuySet(player,
                                ConstNpc.MENU_SHOP_LIO_CONFIRM_SET_XAYDA, "Xayda");
                    }
                }
                case ConstNpc.MENU_SHOP_LIO_CONFIRM_SET_TRAI_DAT -> {
                    if (select == 0) {
                        LioShopService.gI().buyBeTacSet(player, 0);
                    }
                }
                case ConstNpc.MENU_SHOP_LIO_CONFIRM_SET_NAMEK -> {
                    if (select == 0) {
                        LioShopService.gI().buyBeTacSet(player, 1);
                    }
                }
                case ConstNpc.MENU_SHOP_LIO_CONFIRM_SET_XAYDA -> {
                    if (select == 0) {
                        LioShopService.gI().buyBeTacSet(player, 2);
                    }
                }
                case ConstNpc.MENU_SHOP_LIO_CONFIRM_BAN -> {
                    if (select == 0) {
                        // Xác nhận bán
                        if (player.combineNew.itemsCombine.size() == 1) {
                            Item item = player.combineNew.itemsCombine.get(0);
                            LioShopService.gI().sellItem(player, item);
                            player.combineNew.clearCombine();
                        }
                    }
                }
                case ConstNpc.MENU_START_COMBINE -> {
                    // Start combine cho chức năng bán
                    if (player.combineNew.typeCombine == CombineService.BAN_DO_THAN_LINH_LIO) {
                        if (select == 0) {
                            if (player.combineNew.itemsCombine.size() == 1) {
                                Item item = player.combineNew.itemsCombine.get(0);
                                LioShopService.gI().sellItem(player, item);
                                player.combineNew.clearCombine();
                            }
                        }
                    }
                }
            }
        }
    }

    private void openConfirmBuySet(Player player, int menuId, String planetName) {
        createOtherMenu(player, menuId,
                "Mua trọn bộ 5 món Thần Linh " + planetName
                + " với giá 800 thỏi vàng?",
                "Mua", "Từ chối");
    }
}
