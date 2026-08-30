package nro.models.combine;

import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.GoldBarSpendService;
import nro.models.services.InventoryService;
import nro.models.services.PhuongHoangLuaService;
import nro.models.services.Service;
import nro.models.utils.Util;

/** Tẩy và tạo lại toàn bộ chỉ số đã mở của Phượng hoàng lửa. */
public final class TayChiSoPhuongHoangLua {

    private TayChiSoPhuongHoangLua() {
    }

    public static void showInfoCombine(Player player) {
        Item phoenix = findPhoenix(player);
        if (!hasExactlyOneItem(player) || phoenix == null) {
            showCloseMenu(player, "Chỉ cần chọn đúng 1 Phượng hoàng lửa để tẩy chỉ số.");
            return;
        }

        PhuongHoangLuaService phoenixService = PhuongHoangLuaService.gI();
        phoenixService.normalize(phoenix);
        int level = phoenixService.getLevel(phoenix);
        int goldBarCost = getGoldBarCost(level);
        int gemCost = getGemCost(level);
        long currentGoldBars = countGoldBars(player);

        player.combineNew.goldCombine = 0;
        player.combineNew.gemCombine = gemCost;
        player.combineNew.ratioCombine = 100;

        String npcSay = "Tẩy chỉ số Phượng hoàng lửa cấp " + level + "\n"
                + "Chỉ số hiện tại: " + phoenixService.getStatSummary(phoenix) + "\n"
                + "Phí: " + Util.numberToMoney(goldBarCost) + " Thỏi vàng và "
                + Util.numberToMoney(gemCost) + " ngọc xanh\n"
                + "Tẩy thành công 100%, giữ nguyên cấp và random lại toàn bộ chỉ số đã mở."
                + getHiddenOptionWarning(level);

        if (currentGoldBars < goldBarCost) {
            showCloseMenu(player, npcSay + "\nCòn thiếu "
                    + Util.numberToMoney(goldBarCost - currentGoldBars) + " Thỏi vàng.");
            return;
        }
        if (player.inventory.gem < gemCost) {
            showCloseMenu(player, npcSay + "\nCòn thiếu "
                    + Util.numberToMoney(gemCost - player.inventory.gem) + " ngọc xanh.");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay, "Tẩy chỉ số\n100%", "Từ chối");
    }

    public static void reroll(Player player) {
        synchronized (player) {
            Item phoenix = findPhoenix(player);
            if (!hasExactlyOneItem(player) || phoenix == null
                    || InventoryService.gI().getIndexItemBag(player, phoenix) < 0) {
                Service.gI().sendThongBao(player,
                        "Phượng hoàng lửa không còn hợp lệ trong hành trang.");
                return;
            }

            PhuongHoangLuaService phoenixService = PhuongHoangLuaService.gI();
            phoenixService.normalize(phoenix);
            int level = phoenixService.getLevel(phoenix);
            int goldBarCost = getGoldBarCost(level);
            int gemCost = getGemCost(level);
            if (countGoldBars(player) < goldBarCost) {
                Service.gI().sendThongBao(player, "Không đủ "
                        + Util.numberToMoney(goldBarCost) + " Thỏi vàng để tẩy chỉ số.");
                return;
            }
            if (player.inventory.gem < gemCost) {
                Service.gI().sendThongBao(player, "Không đủ "
                        + Util.numberToMoney(gemCost) + " ngọc xanh để tẩy chỉ số.");
                return;
            }

            if (!phoenixService.rerollCurrentLevel(phoenix)) {
                Service.gI().sendThongBao(player,
                        "Không thể tẩy chỉ số Phượng hoàng lửa, nguyên liệu chưa bị trừ.");
                return;
            }
            if (!consumeGoldBars(player, goldBarCost)) {
                Service.gI().sendThongBao(player,
                        "Không thể trừ đủ Thỏi vàng, vui lòng kiểm tra lại hành trang.");
                return;
            }
            player.inventory.subGem(gemCost);
            GoldBarSpendService.gI().addPoint(player, goldBarCost);

            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player,
                    "Tẩy thành công Phượng hoàng lửa cấp " + level
                    + ". Chỉ số mới: " + phoenixService.getStatSummary(phoenix));
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }

    public static int getGoldBarCost(int level) {
        return switch (level) {
            case 1 -> 1_000;
            case 2 -> 2_000;
            case 3 -> 3_000;
            case 4 -> 5_000;
            case 5 -> 20_000;
            case 6 -> 30_000;
            default -> 0;
        };
    }

    public static int getGemCost(int level) {
        return switch (level) {
            case 1 -> 1_000;
            case 2 -> 2_000;
            case 3 -> 3_000;
            case 4 -> 10_000;
            case 5 -> 20_000;
            case 6 -> 30_000;
            default -> 0;
        };
    }

    public static String getRerollGuide() {
        return "Cấp 1: 1.000 Thỏi vàng + 1.000 ngọc\n"
                + "Cấp 2: 2.000 Thỏi vàng + 2.000 ngọc\n"
                + "Cấp 3: 3.000 Thỏi vàng + 3.000 ngọc\n"
                + "Cấp 4: 5.000 Thỏi vàng + 10.000 ngọc\n"
                + "Cấp 5: 20.000 Thỏi vàng + 20.000 ngọc\n"
                + "Cấp 6: 30.000 Thỏi vàng + 30.000 ngọc";
    }

    private static String getHiddenOptionWarning(int level) {
        if (level >= 6) {
            return "\n|7|Lưu ý: loại dòng sát thương đặc biệt và loại dòng Xuyên giáp/Giáp/Chí mạng cũng sẽ đổi ngẫu nhiên.";
        }
        if (level >= 5) {
            return "\n|7|Lưu ý: loại dòng QCKK/Tự sát/Laze cũng sẽ đổi ngẫu nhiên.";
        }
        return "";
    }

    private static Item findPhoenix(Player player) {
        if (player == null || player.combineNew == null
                || player.combineNew.itemsCombine == null) {
            return null;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (PhuongHoangLuaService.gI().isPhuongHoangLua(item)) {
                return item;
            }
        }
        return null;
    }

    private static boolean hasExactlyOneItem(Player player) {
        return player != null && player.combineNew != null
                && player.combineNew.itemsCombine != null
                && player.combineNew.itemsCombine.size() == 1;
    }

    private static long countGoldBars(Player player) {
        long total = 0;
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return total;
        }
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem()
                    && item.template.id == ConstItem.THOI_VANG && item.quantity > 0) {
                total += item.quantity;
            }
        }
        return total;
    }

    private static boolean consumeGoldBars(Player player, int quantity) {
        if (quantity <= 0 || countGoldBars(player) < quantity) {
            return false;
        }
        int remaining = quantity;
        for (Item item : player.inventory.itemsBag) {
            if (remaining <= 0) {
                break;
            }
            if (item == null || !item.isNotNullItem()
                    || item.template.id != ConstItem.THOI_VANG || item.quantity <= 0) {
                continue;
            }
            int used = Math.min(remaining, item.quantity);
            InventoryService.gI().subQuantityItemsBag(player, item, used);
            remaining -= used;
        }
        return remaining == 0;
    }

    private static void showCloseMenu(Player player, String text) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text, "Đóng");
    }
}
