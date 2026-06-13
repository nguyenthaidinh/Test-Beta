
package nro.models.combine;

import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import java.util.ArrayList;
import java.util.Arrays;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.InventoryService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */

public class CheTaoTrangBiThienSu {

    // Chỉ validate và hiển thị preview — KHÔNG craft
    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 4) {
            Service.gI().sendThongBao(player,
                    "Cần đúng 4 vật phẩm: Công Thức Vip + 999 Mảnh Thiên Sứ + Đá Nâng Cấp + Đá May Mắn");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThucVip())
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Công Thức Vip");
            return;
        }
        if (player.combineNew.itemsCombine.stream()
                .filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Mảnh Thiên Sứ (cần 999)");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaNangCap1())
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Đá Nâng Cấp");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDaMayMan())
                .count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Đá May Mắn");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        if (player.inventory.gold < 2_000_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng (cần 2 tỷ)");
            return;
        }
        // Hiển thị preview
        player.combineNew.goldCombine = 2_000_000_000;
        player.combineNew.ratioCombine = 90;
    }

    // Thực thi chế tạo — cùng công thức với showInfoCombine
    public static void CheTaoTS(Player player) {
        if (player.combineNew.itemsCombine.size() != 4) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm");
            return;
        }

        // Tìm item an toàn, không dùng .get() trực tiếp
        Item mTS = null, daNC = null, daMM = null, CtVip = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.isNotNullItem()) {
                if (item.isCongThucVip()) {
                    CtVip = item;
                } else if (item.isManhTS() && item.quantity >= 999) {
                    mTS = item;
                } else if (item.isDaNangCap1()) {
                    daNC = item;
                } else if (item.isDaMayMan()) {
                    daMM = item;
                }
            }
        }

        if (CtVip == null || mTS == null) {
            Service.gI().sendThongBao(player, "Thiếu Công Thức Vip hoặc Mảnh Thiên Sứ (999)");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        if (player.inventory.gold < 2_000_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }

        player.inventory.gold -= 2_000_000_000;

        // Tính tỉ lệ thành công
        int tilemacdinh = 90;
        int tileLucky = 5;
        if (daNC != null) {
            tilemacdinh += (daNC.template.id - 1073);
        }
        if (daMM != null) {
            tileLucky += tileLucky * (daMM.template.id - 1078);
        }

        if (Util.nextInt(0, 100) < tilemacdinh) {
            // Thành công — tạo đồ Thiên Sứ
            short[][] itemIds = {
                    { 1048, 1051, 1054, 1057, 1060 },
                    { 1049, 1052, 1055, 1058, 1061 },
                    { 1050, 1053, 1056, 1059, 1062 }
            };

            int genderIndex = player.gender;
            Item itemTS = ItemService.gI().DoThienSu(itemIds[genderIndex][mTS.typeIdManh()], player.gender);

            // Bonus chỉ số 100%
            for (int w = 0; w < itemTS.itemOptions.size(); w++) {
                int optId = itemTS.itemOptions.get(w).optionTemplate.id;
                if (optId != 0 && optId != 20 && optId != 21 && optId != 30) {
                    itemTS.itemOptions.get(w).param += (itemTS.itemOptions.get(w).param * 100 / 100);
                }
            }

            // Lucky bonus — thêm sao và option phụ
            int luckyRoll = Util.nextInt(0, 50);
            if (luckyRoll <= tileLucky) {
                int starCount;
                if (luckyRoll >= (tileLucky - 3)) {
                    starCount = 3;
                } else if (luckyRoll >= (tileLucky - 10)) {
                    starCount = 2;
                } else {
                    starCount = 1;
                }
                itemTS.itemOptions.add(new ItemOption(15, starCount));
                ArrayList<Integer> listOptionBonus = new ArrayList<>(Arrays.asList(50, 77, 103, 94, 5));
                for (int j = 0; j < starCount; j++) {
                    int randIdx = Util.nextInt(0, listOptionBonus.size() - 1);
                    itemTS.itemOptions.add(new ItemOption(listOptionBonus.get(randIdx), Util.nextInt(1, 3)));
                    listOptionBonus.remove(randIdx);
                }
            }

            InventoryService.gI().addItemBag(player, itemTS);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Chế tạo thành công! Nhận được " + itemTS.template.name);
        } else {
            // Thất bại
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Chế tạo thất bại!");
        }

        // Trừ nguyên liệu (luôn trừ dù thành công hay thất bại)
        InventoryService.gI().subQuantityItemsBag(player, CtVip, 1);
        InventoryService.gI().subQuantityItemsBag(player, mTS, 999);
        if (daNC != null) {
            InventoryService.gI().subQuantityItemsBag(player, daNC, 1);
        }
        if (daMM != null) {
            InventoryService.gI().subQuantityItemsBag(player, daMM, 1);
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
