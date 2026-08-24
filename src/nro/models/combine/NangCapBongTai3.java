package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.ChatGlobalService;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author MinhDu
 */
public class NangCapBongTai3 {
    
    private static final int GOLD_BONG_TAI = 200_000_000;
    private static final int GEM_BONG_TAI = 1_000;
    private static final int RATIO_BONG_TAI = 50;   
    private static final int ITEM_ID_BONG_TAI_C2 = 921;   
    private static final int ITEM_ID_BONG_TAI_C3 = 1819;
    private static final int ITEM_ID_MANH_VO_BT3 = 1820;
    private static final int ITEM_OPTION_ID_CAP = 72;
    private static final int ITEM_OPTION_VALUE_CAP_3 = 3;
    private static final int ITEM_PARAM_INDEX = 31;
    private static final int REQUIRED_MANH_VO_FULL = 1_000;
    private static final int REQUIRED_MANH_VO_FAIL = 200;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item bongTai = null;
            Item manhVo = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == ITEM_ID_BONG_TAI_C2) {
                    bongTai = item;
                } else if (item.template.id == ITEM_ID_MANH_VO_BT3) {
                    manhVo = item;
                }
            }

            if (bongTai != null && manhVo != null) {
                player.combineNew.goldCombine = GOLD_BONG_TAI;
                player.combineNew.gemCombine = GEM_BONG_TAI;
                player.combineNew.ratioCombine = RATIO_BONG_TAI;

                String npcSay = "|2|Bông tai Porata [+3]\n\n";
                npcSay += "|2|Tỉ lệ thành công: " + RATIO_BONG_TAI + "%\n";

                int currentMvp = InventoryService.gI().getParam(manhVo, ITEM_PARAM_INDEX);

                if (currentMvp < REQUIRED_MANH_VO_FULL) {
                    npcSay += "|7|Cần " + REQUIRED_MANH_VO_FULL + " " + manhVo.template.name + "\n";
                    npcSay += "|2|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
                    npcSay += "|2|Cần: " + GEM_BONG_TAI + " ngọc\n";
                    npcSay += "|7|Thất bại -" + REQUIRED_MANH_VO_FAIL + " " + manhVo.template.name + "\n";
                    npcSay += "Còn thiếu " + (REQUIRED_MANH_VO_FULL - currentMvp) + " " + manhVo.template.name;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else if (player.inventory.gem >= GEM_BONG_TAI && player.inventory.gold >= GOLD_BONG_TAI) {
                    npcSay += "|2|Cần " + REQUIRED_MANH_VO_FULL + " " + manhVo.template.name + "\n";
                    npcSay += "|2|Cần: " + GEM_BONG_TAI + " ngọc\n";
                    npcSay += "|2|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
                    npcSay += "|7|Thất bại -" + REQUIRED_MANH_VO_FAIL + " " + manhVo.template.name + "\n";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\n"
                                    + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n"
                                    + GEM_BONG_TAI + " ngọc\n", "Từ chối");
                } else if (player.inventory.gem < GEM_BONG_TAI) {
                    npcSay += "|2|Cần " + REQUIRED_MANH_VO_FULL + " " + manhVo.template.name + "\n";
                    npcSay += "|7|Cần: " + GEM_BONG_TAI + " ngọc xanh\n";
                    npcSay += "|2|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
                    npcSay += "|7|Thất bại -" + REQUIRED_MANH_VO_FAIL + " " + manhVo.template.name + "\n";
                    npcSay += "Còn thiếu\n" + (GEM_BONG_TAI - player.inventory.gem) + " ngọc xanh";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else {
                    npcSay += "|2|Cần " + REQUIRED_MANH_VO_FULL + " " + manhVo.template.name + "\n";
                    npcSay += "|2|Cần: " + GEM_BONG_TAI + " ngọc\n";
                    npcSay += "|7|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
                    npcSay += "|7|Thất bại -" + REQUIRED_MANH_VO_FAIL + " " + manhVo.template.name + "\n";
                    npcSay += "Còn thiếu " + Util.powerToString(GOLD_BONG_TAI - player.inventory.gold) + " vàng";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                }

            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 Bông tai Porata cấp 2 và Mảnh vỡ bông tai (BT3)", "Đóng");
            }

        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 2 và Mảnh vỡ bông tai (BT3)", "Đóng");
        }
    }

    public static void nangCapBongTai(Player player) {
        synchronized (player) {
            if (player.combineNew.itemsCombine.size() != 2) {
                Service.gI().sendThongBao(player, "Cần chọn đúng Bông tai Porata cấp 2 và Mảnh vỡ bông tai cấp 3.");
                return;
            }
            int gold = GOLD_BONG_TAI;
            int gem = GEM_BONG_TAI;

            Item bongTai = null;
            Item manhVo = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == ITEM_ID_BONG_TAI_C2) {
                    bongTai = item;
                } else if (item.template.id == ITEM_ID_MANH_VO_BT3) {
                    manhVo = item;
                }
            }

            if (bongTai == null || manhVo == null
                    || InventoryService.gI().getIndexItemBag(player, bongTai) < 0
                    || InventoryService.gI().getIndexItemBag(player, manhVo) < 0) {
                Service.gI().sendThongBao(player, "Vật phẩm nâng cấp không còn hợp lệ trong hành trang.");
                return;
            }
            int currentFragments = InventoryService.gI().getParam(manhVo, ITEM_PARAM_INDEX);
            if (currentFragments < REQUIRED_MANH_VO_FULL) {
                Service.gI().sendThongBao(player, "Không đủ " + REQUIRED_MANH_VO_FULL
                        + " Mảnh vỡ bông tai cấp 3 để thực hiện.");
                return;
            }
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            if (bongTai.quantity > 1 && InventoryService.gI().getCountEmptyBag(player) < 1) {
                Service.gI().sendThongBao(player,
                        "Bông tai cũ đang gộp thành stack, cần trống 1 ô hành trang để tách chiếc được nâng cấp.");
                return;
            }

            boolean success = Util.isTrue(RATIO_BONG_TAI, 100);
            Item upgradedEarring = bongTai;
            if (success) {
                upgradedEarring = InventoryService.gI().separateOneEarringInBag(player, bongTai);
                if (upgradedEarring == null) {
                    Service.gI().sendThongBao(player, "Không thể tách Bông tai cần nâng cấp.");
                    return;
                }
            }

            player.inventory.gold -= gold;
            player.inventory.gem -= gem;

            if (success) {
                upgradedEarring.template = ItemService.gI().getTemplate(ITEM_ID_BONG_TAI_C3);
                upgradedEarring.itemOptions.clear();
                upgradedEarring.itemOptions.add(new Item.ItemOption(
                        ITEM_OPTION_ID_CAP, ITEM_OPTION_VALUE_CAP_3));
                upgradedEarring.info = upgradedEarring.getInfo();
                upgradedEarring.content = upgradedEarring.getContent();
                InventoryService.gI().subParamItemBag(player, manhVo,
                        ITEM_PARAM_INDEX, REQUIRED_MANH_VO_FULL);
                CombineService.gI().sendEffectSuccessCombine(player);
                ChatGlobalService.gI().ThongBaoDapDo(player,
                    "Lio đẹp trai chúc mừng " + player.name
                    + " vừa nâng cấp thành công [Bông tai Porata] lên cấp 3!"
                );
            } else {
                InventoryService.gI().subParamItemBag(player, manhVo,
                        ITEM_PARAM_INDEX, REQUIRED_MANH_VO_FAIL);
                CombineService.gI().sendEffectFailCombine(player);
            }
            if (InventoryService.gI().getIndexItemBag(player, manhVo) < 0) {
                player.combineNew.itemsCombine.remove(manhVo);
            }

            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
