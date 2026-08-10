package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class CuongHoaLoSaoPhaLe {

    private static final int COST = 500_000_000;

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 3) {
                Item item = null, Hematite = null, DuiDuc = null;

                for (Item i : player.combineNew.itemsCombine) {
                    if (CombineSystem.isTrangBiPhaLeHoa(i)) {
                        item = i;
                    } else if (i.template.id == 1423) { // Hematite
                        Hematite = i;
                    } else if (i.template.id == 1438) { // Dùi Đục
                        DuiDuc = i;
                    }
                }

                if (item != null && Hematite != null && DuiDuc != null && Hematite.quantity >= 1 && DuiDuc.quantity >= 1) {
                    int star = 0;
                    int insertedStars = 0;
                    int enhancedSlot = 0;
                    for (ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id == 107) {
                            star = io.param;
                        } else if (io.optionTemplate.id == 102) {
                            insertedStars = io.param;
                        } else if (io.optionTemplate.id == 228) {
                            enhancedSlot = io.param;
                        }
                    }

                    if (star < 8) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Trang bị phải có ít nhất 8 sao pha lê mới có thể cường hóa!", "Đóng");
                        return;
                    }

                    int nextSlot = enhancedSlot < 8 ? 8 : enhancedSlot + 1;
                    if (nextSlot > CombineService.MAX_STAR_ITEM) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Không thể cường hóa thêm.", "Đóng");
                        return;
                    }

                    if (nextSlot == 8 && insertedStars < 7) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Trang bị cần ép đủ 7 sao trước khi cường hóa ô sao thứ 8.", "Đóng");
                        return;
                    }

                    if (star < nextSlot) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Vui lòng nâng cấp trang bị lên " + nextSlot + " sao trước khi cường hóa.", "Đóng");
                        return;
                    }

                    String npcSay = item.template.name + "\n|2|";
                    for (ItemOption io : Hematite.itemOptions) {
                        npcSay += io.getOptionString() + "\n";
                    }
                    npcSay += "Cường hóa\n" + " Ô sao pha lê thứ 8, 9 hoặc 10\n" + item.template.name
                            + "\nTỉ lệ thành công: ô 8 100%, ô 9/10 50%\n"
                            + "|7| Cần 1 " + Hematite.template.name
                            + "\n|7| Cần 1 " + DuiDuc.template.name
                            + "\nCần " + Util.numberToMoney(COST) + " vàng";

                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Cường Hóa", "Từ chối");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public static void cuongHoaLoSaoPhaLe(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang.");
            return;
        }

        if (player.inventory.gold < COST) {
            Service.gI().sendThongBao(player, "Con cần thêm vàng để cường hóa...");
            return;
        }

        if (player.combineNew.itemsCombine.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có vật phẩm để cường hóa.");
            return;
        }

        Item item = null;
        Item hematite = null;
        Item duiDuc = null;

        for (Item i : player.combineNew.itemsCombine) {
            if (CombineSystem.isTrangBiPhaLeHoa(i)) {
                item = i;
            } else if (i.template.id == 1423) {
                hematite = i;
            } else if (i.template.id == 1438) {
                duiDuc = i;
            }
        }

        if (item == null || hematite == null || duiDuc == null || hematite.quantity < 1 || duiDuc.quantity < 1) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm hoặc số lượng không đủ.");
            return;
        }

        int star = 0;
        int insertedStars = 0;
        ItemOption opt228 = null;
        boolean hasOption218 = false;

        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 107) {
                star = io.param;
            } else if (io.optionTemplate.id == 228) {
                opt228 = io;
            } else if (io.optionTemplate.id == 218) {
                hasOption218 = true;
            } else if (io.optionTemplate.id == 102) {
                insertedStars = io.param;
            }
        }

        if (star < 8) {
            Service.gI().sendThongBao(player, "Vui lòng nâng cấp trang bị lên 8, 9 hoặc 10 sao trước khi cường hóa.");
            return;
        }

        int enhancedSlot = opt228 == null ? 0 : opt228.param;
        if (enhancedSlot >= CombineService.MAX_STAR_ITEM) {
            Service.gI().sendThongBao(player, "Không thể cường hóa thêm.");
            return;
        }

        int nextSlot = enhancedSlot < 8 ? 8 : enhancedSlot + 1;
        if (nextSlot > CombineService.MAX_STAR_ITEM) {
            Service.gI().sendThongBao(player, "Không thể cường hóa thêm.");
            return;
        }

        if (nextSlot == 8 && insertedStars < 7) {
            Service.gI().sendThongBao(player, "Trang bị cần có đủ 7 lỗ để cường hóa.");
            return;
        }

        if (star < nextSlot) {
            Service.gI().sendThongBao(player, "Vui lòng nâng cấp trang bị lên " + nextSlot + " sao trước khi cường hóa.");
            return;
        }

        boolean success = nextSlot == 8 || Util.isTrue(50, 100);
        player.inventory.gold -= COST;

        if (success) {
            if (!hasOption218) {
                item.itemOptions.add(new ItemOption(218, 0));
            }
            if (opt228 == null) {
                item.itemOptions.add(new ItemOption(228, nextSlot));
            } else {
                opt228.param = nextSlot;
            }
            CombineService.gI().sendEffectSuccessCombine(player);
            CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con nhé");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
        }

        InventoryService.gI().subQuantityItemsBag(player, hematite, 1);
        InventoryService.gI().subQuantityItemsBag(player, duiDuc, 1);
        Service.gI().sendMoney(player);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
        CombineService.gI().sendEffectCombineDB(player, item.template.iconID);
    }

}
