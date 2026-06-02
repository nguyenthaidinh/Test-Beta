package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Hiến tế trang bị Thần Linh thành Set Kích Hoạt
 * Đặt 1 đồ Thần Linh → nhận 1 đồ SKH cùng loại với option set ngẫu nhiên
 *
 * @author Lio
 */
public class HienTeTrangBiThanLinh {

    private static final int GOLD_HIEN_TE = 1_000_000_000;
    private static final int RATIO_HIEN_TE = 80; // 80% thành công

    // Đồ Thần Linh theo type và gender
    // [type][gender] → item ID
    // type: 0=Áo, 1=Quần, 2=Găng, 3=Giày
    private static final short[][] DO_THAN_LINH = {
        {555, 557, 559}, // Áo: TD, NM, XD
        {556, 558, 560}, // Quần: TD, NM, XD
        {562, 564, 566}, // Găng: TD, NM, XD
        {563, 565, 567}, // Giày: TD, NM, XD
    };

    // Nhẫn Thần Linh (chung 3 race)
    private static final short NHAN_THAN_LINH = 561;

    // ==================== POOL ĐỒ SKH PHÂN BẬC ====================
    // Mỗi loại trang bị chia 4 bậc: Thường → Chiến Binh → Kaio → Zelot/Lưỡng Long
    // [type][gender] → int[][] { {bậc1}, {bậc2}, {bậc3}, {bậc4} }

    // Tỉ lệ ra từng bậc (tổng = 100)
    private static final int TIER_1_RATE = 40;  // Thường (Vải thô...)     - 40%
    private static final int TIER_2_RATE = 30;  // Chiến Binh              - 30%
    private static final int TIER_3_RATE = 20;  // Kaio                    - 20%
    private static final int TIER_4_RATE = 10;  // Zelot / Lưỡng Long      - 10%

    // === TRÁI ĐẤT (gender=0) ===
    private static final int[][][] TD_TIERS = {
        // Áo TD
        {{0, 3}, {33, 34}, {136, 137, 138, 139}, {230, 231, 232, 233}},
        // Quần TD
        {{6, 9}, {35, 36}, {140, 141, 142, 143}, {242, 243, 244, 245}},
        // Găng TD
        {{21, 24}, {37, 38}, {144, 145, 146, 147}, {254, 256, 257}},
        // Giày TD
        {{27, 30}, {39, 40}, {148, 149, 150, 151}, {266, 267, 268, 269}},
        // Rada
        {{12, 57}, {58, 59}, {184, 185, 186, 187}, {278, 279, 280, 281}},
    };

    // === NAMEK (gender=1) ===
    private static final int[][][] NM_TIERS = {
        // Áo NM
        {{1, 4}, {41, 42}, {152, 153, 154, 155}, {235, 236, 237}},
        // Quần NM
        {{7, 10}, {43, 44}, {156, 157, 158, 159}, {246, 247, 248, 249}},
        // Găng NM
        {{22, 25}, {45, 46}, {160, 161, 162, 163}, {259, 260, 261}},
        // Giày NM
        {{28, 31}, {47, 48}, {164, 165, 166, 167}, {270, 271, 272, 273}},
        // Rada
        {{12, 57}, {58, 59}, {184, 185, 186, 187}, {278, 279, 280, 281}},
    };

    // === XAYDA (gender=2) ===
    private static final int[][][] XD_TIERS = {
        // Áo XD
        {{2, 5}, {49, 50}, {168, 169, 170, 171}, {238, 239, 240, 241}},
        // Quần XD
        {{8, 11}, {51, 52}, {172, 173, 174}, {250, 251, 252, 253}},
        // Găng XD
        {{23, 26}, {53, 54}, {176, 177, 178, 179}, {262, 263, 264, 265}},
        // Giày XD
        {{29, 32}, {55, 56}, {180, 181, 182, 183}, {274, 275, 276, 277}},
        // Rada
        {{12, 57}, {58, 59}, {184, 185, 186, 187}, {278, 279, 280, 281}},
    };

    // Gom 3 race lại để truy cập theo gender index
    private static final int[][][][] ALL_TIERS = {TD_TIERS, NM_TIERS, XD_TIERS};

    // Option set theo gender
    private static final int[][] OPTION_IDS = {
        {128, 129, 127, 233, 245}, // Trái Đất
        {130, 131, 132, 233, 237}, // Namek
        {133, 135, 134, 233, 241}, // Xayda
    };

    /**
     * Random item SKH theo tỉ lệ phân bậc
     * Bậc 1 (Thường): 40% | Bậc 2 (Chiến Binh): 30% | Bậc 3 (Kaio): 20% | Bậc 4 (Zelot+): 10%
     */
    private static int randomItemByTier(int type, int gender) {
        int[][] tiers = ALL_TIERS[gender][type]; // { {bậc1}, {bậc2}, {bậc3}, {bậc4} }
        int roll = Util.nextInt(1, 100);

        int[] selectedTier;
        String tierName;

        if (roll <= TIER_4_RATE) {
            // 1-10: Bậc 4 - Zelot/Lưỡng Long (hiếm nhất)
            selectedTier = tiers[3];
            tierName = "Zelot/Lưỡng Long";
        } else if (roll <= TIER_4_RATE + TIER_3_RATE) {
            // 11-30: Bậc 3 - Kaio
            selectedTier = tiers[2];
            tierName = "Kaio";
        } else if (roll <= TIER_4_RATE + TIER_3_RATE + TIER_2_RATE) {
            // 31-60: Bậc 2 - Chiến Binh
            selectedTier = tiers[1];
            tierName = "Chiến Binh";
        } else {
            // 61-100: Bậc 1 - Thường (phổ biến nhất)
            selectedTier = tiers[0];
            tierName = "Thường";
        }

        return selectedTier[Util.nextInt(selectedTier.length)];
    }

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần đặt đúng 1 trang bị Thần Linh!", "Đóng");
            return;
        }

        Item item = player.combineNew.itemsCombine.get(0);

        if (!isDoThanLinh(item)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Vật phẩm phải là trang bị Thần Linh\n(Áo, Quần, Găng, Giày hoặc Nhẫn Thần Linh)!", "Đóng");
            return;
        }

        player.combineNew.goldCombine = GOLD_HIEN_TE;
        player.combineNew.ratioCombine = RATIO_HIEN_TE;

        String raceName = getRaceName(item, player);
        String npcSay = "Hiến tế: " + item.template.name + "\n"
                + "Cần: 1 món đồ Thần Linh + 1 Tỷ vàng\n"
                + "Nhận: 1 trang bị Set Kích Hoạt ngẫu nhiên\n"
                + "Tỉ lệ thành công: " + RATIO_HIEN_TE + "%\n"
                + "Thất bại sẽ mất trang bị Thần Linh";

        if (player.inventory.gold < GOLD_HIEN_TE) {
            npcSay += "\nCòn thiếu " + Util.powerToString(GOLD_HIEN_TE - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Hiến tế\n" + Util.numberToMoney(GOLD_HIEN_TE) + " vàng", "Từ chối");
        }
    }

    public static void thucHienHienTe(Player player) {
        try {
            if (player.combineNew.itemsCombine.size() != 1) {
                Service.gI().sendThongBao(player, "Cần đặt đúng 1 trang bị Thần Linh!");
                return;
            }

            Item item = player.combineNew.itemsCombine.get(0);

            if (!isDoThanLinh(item)) {
                Service.gI().sendThongBao(player, "Vật phẩm không phải trang bị Thần Linh!");
                return;
            }

            if (player.inventory.gold < GOLD_HIEN_TE) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện!");
                return;
            }

            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống!");
                return;
            }

            // Lấy gender TRƯỚC KHI xóa item (vì subQuantityItemsBag sẽ set template = null)
            int gender = getGenderFromItem(item, player);

            // Trừ vàng
            player.inventory.gold -= GOLD_HIEN_TE;

            // Xóa đồ Thần Linh
            InventoryService.gI().subQuantityItemsBag(player, item, 1);

            if (Util.isTrue(RATIO_HIEN_TE, 100)) {
                // Thành công - tạo đồ SKH
                int type = Util.nextInt(5);
                int itemId = randomItemByTier(type, gender);
                int skhOptionId = OPTION_IDS[gender][Util.nextInt(OPTION_IDS[gender].length)];

                Item newItem = ItemService.gI().createItemSKH(itemId, skhOptionId);

                if (newItem != null) {
                    InventoryService.gI().addItemBag(player, newItem);
                    CombineService.gI().sendEffectSuccessCombine(player);
                    Service.gI().sendThongBao(player, "Hiến tế thành công! Nhận được " + newItem.template.name);
                } else {
                    // createItemSKH trả null → hoàn lại vàng
                    player.inventory.gold += GOLD_HIEN_TE;
                    CombineService.gI().sendEffectFailCombine(player);
                    Service.gI().sendThongBao(player, "Lỗi tạo vật phẩm, vui lòng thử lại!");
                }
            } else {
                // Thất bại
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Hiến tế thất bại! Mất trang bị Thần Linh!");
            }

            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Lỗi hệ thống hiến tế, vui lòng thử lại!");
        }
    }

    /**
     * Kiểm tra có phải đồ Thần Linh không (ID 555-567)
     */
    private static boolean isDoThanLinh(Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        return item.template.id >= 555 && item.template.id <= 567;
    }

    /**
     * Xác định gender (race) từ item Thần Linh
     * TD=0, NM=1, XD=2
     * Nhẫn Thần Linh (561) chung 3 race → dùng race của player
     */
    private static int getGenderFromItem(Item item, Player player) {
        int id = item.template.id;
        if (id == NHAN_THAN_LINH) {
            // Nhẫn chung 3 race → dùng race nhân vật
            return Math.min(player.gender, 2);
        }
        for (int type = 0; type < DO_THAN_LINH.length; type++) {
            for (int gender = 0; gender < 3; gender++) {
                if (DO_THAN_LINH[type][gender] == id) {
                    return gender;
                }
            }
        }
        return Math.min(player.gender, 2);
    }

    /**
     * Xác định type trang bị từ item Thần Linh
     * 0=Áo, 1=Quần, 2=Găng, 3=Giày, 4=Rada (nhẫn → rada)
     */
    private static int getTypeFromItem(Item item) {
        int id = item.template.id;
        if (id == 561) return 4; // Nhẫn → Rada
        for (int type = 0; type < DO_THAN_LINH.length; type++) {
            for (int gender = 0; gender < 3; gender++) {
                if (DO_THAN_LINH[type][gender] == id) {
                    return type;
                }
            }
        }
        return 0;
    }

    private static String getTypeName(Item item) {
        int type = getTypeFromItem(item);
        return switch (type) {
            case 0 -> "Áo";
            case 1 -> "Quần";
            case 2 -> "Găng";
            case 3 -> "Giày";
            case 4 -> "Rada";
            default -> "";
        };
    }

    private static String getRaceName(Item item, Player player) {
        int gender = getGenderFromItem(item, player);
        return switch (gender) {
            case 0 -> "Trái Đất";
            case 1 -> "Namek";
            case 2 -> "Xayda";
            default -> "";
        };
    }
}
