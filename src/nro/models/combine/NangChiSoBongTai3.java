package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author MinhDu
 */
public class NangChiSoBongTai3 {
    // Chi phí & tỉ lệ
    private static final int GEM_NANG_BT = 1_000;
    private static final int RATIO_NANG_CAP = 30;
    private static final int ITEM_PARAM_INDEX = 31;
    private static final int BONG_TAI_C3_ID = 1819;
    private static final int HON_BONG_TAI_ID = 934;
    private static final int DA_XANH_LAM_ID = 935;
    private static final int REQUIRED_HON_BONG_TAI = 99;
    private static final byte[] UPGRADE_OPTIONS = {77, 80, 81, 103, 50, 94, 5};
    private static final byte PARAM_MIN = 5;
    private static final byte PARAM_MAX = 15;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            Item bongTai = null, honBongTai = null, daXanhLam = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (item != null && item.isNotNullItem()) {
                    int id = item.template.id;
                    if (id == BONG_TAI_C3_ID) bongTai = item;
                    else if (id == HON_BONG_TAI_ID) honBongTai = item;
                    else if (id == DA_XANH_LAM_ID) daXanhLam = item;
                }
            }

            if (bongTai != null && honBongTai != null && daXanhLam != null) {
                player.combineNew.gemCombine = GEM_NANG_BT;
                player.combineNew.ratioCombine = RATIO_NANG_CAP;

                int currentHon = InventoryService.gI().getParam(honBongTai, ITEM_PARAM_INDEX);

                String npcSay = "|2|Mở chỉ số Bông tai Porata [+3]\n\n";
                npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n";

                if (daXanhLam.quantity < 1) {
                    npcSay += "|2|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|7|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|Kết quả: +2 dòng chỉ số ngẫu nhiên (có thể trùng nhau)\n";
                    npcSay += "|2|Còn thiếu " + (1 - daXanhLam.quantity) + " " + daXanhLam.template.name;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else if (currentHon < REQUIRED_HON_BONG_TAI) {
                    npcSay += "|7|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|Kết quả: +2 dòng chỉ số ngẫu nhiên (có thể trùng nhau)\n";
                    npcSay += "|2|Còn thiếu " + (REQUIRED_HON_BONG_TAI - currentHon) + " " + honBongTai.template.name;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else if (player.inventory.gem < player.combineNew.gemCombine) {
                    npcSay += "|2|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|7|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|Kết quả: +2 dòng chỉ số ngẫu nhiên (có thể trùng nhau)\n";
                    npcSay += "|2|Còn thiếu " + (player.combineNew.gemCombine - player.inventory.gem) + " ngọc";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else {
                    npcSay += "|2|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|Kết quả: +2 dòng chỉ số ngẫu nhiên (có thể trùng nhau)";
                    CombineService.gI().baHatMit.createOtherMenu(
                            player,
                            ConstNpc.MENU_START_COMBINE,
                            npcSay,
                            "Nâng cấp\n" + player.combineNew.gemCombine + " ngọc",
                            "Từ chối"
                    );
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(
                        player,
                        ConstNpc.IGNORE_MENU,
                        "Cần 1 Bông tai Porata cấp 3, x" + REQUIRED_HON_BONG_TAI + " Hồn bông tai và 1 Đá xanh lam",
                        "Đóng"
                );
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(
                    player,
                    ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 3, x" + REQUIRED_HON_BONG_TAI + " Hồn bông tai và 1 Đá xanh lam",
                    "Đóng"
            );
        }
    }
    public static void nangChiSoBongTai(Player player) {
        synchronized (player) {
            if (player.combineNew.itemsCombine.size() != 3) {
                Service.gI().sendThongBao(player,
                        "Cần chọn đúng Bông tai cấp 3, Mảnh hồn bông tai và Đá xanh lam.");
                return;
            }
            Item bongTai = null;
            Item honBongTai = null;
            Item daXanhLam = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item == null || !item.isNotNullItem()) {
                    continue;
                }
                int id = item.template.id;
                if (id == BONG_TAI_C3_ID) {
                    bongTai = item;
                } else if (id == HON_BONG_TAI_ID) {
                    honBongTai = item;
                } else if (id == DA_XANH_LAM_ID) {
                    daXanhLam = item;
                }
            }
            if (bongTai == null || honBongTai == null || daXanhLam == null
                    || InventoryService.gI().getIndexItemBag(player, bongTai) < 0
                    || InventoryService.gI().getIndexItemBag(player, honBongTai) < 0
                    || InventoryService.gI().getIndexItemBag(player, daXanhLam) < 0
                    || InventoryService.gI().getParam(honBongTai, ITEM_PARAM_INDEX)
                    < REQUIRED_HON_BONG_TAI || daXanhLam.quantity < 1) {
                Service.gI().sendThongBao(player, "Không đủ vật phẩm để thực hiện.");
                return;
            }
            if (player.inventory.gem < GEM_NANG_BT) {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu "
                        + (GEM_NANG_BT - player.inventory.gem) + " ngọc nữa!");
                return;
            }
            if (bongTai.quantity > 1 && InventoryService.gI().getCountEmptyBag(player) < 1) {
                Service.gI().sendThongBao(player,
                        "Bông tai cũ đang gộp thành stack, cần trống 1 ô hành trang để tách chiếc mở chỉ số.");
                return;
            }

            boolean success = Util.isTrue(RATIO_NANG_CAP, 100);
            Item upgradedEarring = bongTai;
            if (success) {
                upgradedEarring = InventoryService.gI().separateOneEarringInBag(player, bongTai);
                if (upgradedEarring == null) {
                    Service.gI().sendThongBao(player, "Không thể tách Bông tai cần mở chỉ số.");
                    return;
                }
            }

            player.inventory.gem -= GEM_NANG_BT;
            if (success) {
                byte opt1 = randomOpt();
                byte opt2 = randomOpt();
                byte p1 = (byte) Util.nextInt(PARAM_MIN, PARAM_MAX);
                byte p2 = (byte) Util.nextInt(PARAM_MIN, PARAM_MAX);
                upgradedEarring.itemOptions.clear();
                upgradedEarring.itemOptions.add(new Item.ItemOption(opt1, p1));
                upgradedEarring.itemOptions.add(new Item.ItemOption(opt2, p2));
                upgradedEarring.itemOptions.add(new Item.ItemOption((short) 72, 3));
                upgradedEarring.info = upgradedEarring.getInfo();
                upgradedEarring.content = upgradedEarring.getContent();
                CombineService.gI().sendEffectSuccessCombine(player);
            } else {
                CombineService.gI().sendEffectFailCombine(player);
            }
            InventoryService.gI().subParamItemBag(player, honBongTai,
                    ITEM_PARAM_INDEX, REQUIRED_HON_BONG_TAI);
            InventoryService.gI().subQuantityItemsBag(player, daXanhLam, 1);
            if (InventoryService.gI().getIndexItemBag(player, honBongTai) < 0) {
                player.combineNew.itemsCombine.remove(honBongTai);
            }
            if (InventoryService.gI().getIndexItemBag(player, daXanhLam) < 0) {
                player.combineNew.itemsCombine.remove(daXanhLam);
            }

            Service.gI().sendMoney(player);
            InventoryService.gI().sendItemBags(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }

    private static byte randomOpt() {
        return UPGRADE_OPTIONS[Util.nextInt(0, UPGRADE_OPTIONS.length - 1)];
    }
}
