package nro.models.combine;

import nro.models.consts.ConstFont;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;

/**
 *
 * @author By Mr Blue
 */
public class TaySach {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendDialogMessage(player, "C\u1ea7n S\u00e1ch Tuy\u1ec7t K\u1ef9 \u0111\u1ec3 t\u1ea9y.");
            return;
        }
        Item sachTuyetKy = player.combineNew.itemsCombine.get(0);
        if (sachTuyetKy == null || !sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2()) {
            Service.gI().sendDialogMessage(player, "C\u1ea7n S\u00e1ch Tuy\u1ec7t K\u1ef9 \u0111\u1ec3 t\u1ea9y.");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                ConstFont.BOLD_BLUE + "T\u1ea9y S\u00e1ch Tuy\u1ec7t K\u1ef9 ?", "\u0110\u1ed3ng \u00fd", "T\u1eeb ch\u1ed1i");
    }

    public static void taySach(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            return;
        }
        Item sachTuyetKy = player.combineNew.itemsCombine.get(0);
        if (sachTuyetKy == null || !sachTuyetKy.isSachTuyetKy() && !sachTuyetKy.isSachTuyetKy2()) {
            return;
        }
        if (sachTuyetKy.getOptionParam(219) <= 0 || sachTuyetKy.isHaveOption(217)) {
            Service.gI().sendServerMessage(player, "Kh\u00f4ng th\u1ec3 th\u1ef1c hi\u1ec7n");
            return;
        }
        for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
            Item.ItemOption io = sachTuyetKy.itemOptions.get(i);
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            if (io.optionTemplate.id == 21) {
                break;
            }
            sachTuyetKy.itemOptions.set(i, new Item.ItemOption(217, 0));
        }
        sachTuyetKy.subOptionParam(219, 1);
        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
