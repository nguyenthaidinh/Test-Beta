package nro.models.combine;

import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.PhuongHoangLuaService;
import nro.models.services.Service;
import nro.models.utils.Util;

/** Nâng cấp và mở dòng chỉ số cho Phượng hoàng lửa. */
public final class NangCapPhuongHoangLua {

    private NangCapPhuongHoangLua() {
    }

    public static void showInfoCombine(Player player) {
        Item phoenix = findPhoenix(player);
        Item stone = findNguSacStone(player);
        if (!hasExactlyTwoItems(player) || phoenix == null || stone == null) {
            showCloseMenu(player, "Cần chọn đúng 1 Phượng hoàng lửa và 1 chồng Đá ngũ sắc.");
            return;
        }

        PhuongHoangLuaService phoenixService = PhuongHoangLuaService.gI();
        boolean firstInitialization = !phoenixService.isInitialized(phoenix);
        phoenixService.normalize(phoenix);
        int currentLevel = phoenixService.getLevel(phoenix);
        if (currentLevel < PhuongHoangLuaService.MIN_LEVEL) {
            showCloseMenu(player, "Phượng hoàng lửa đang thiếu dòng cấp (option 72). "
                    + "Vật phẩm đã được giữ nguyên để tránh mất chỉ số. Hãy báo admin kiểm tra và phục hồi cấp trước khi nâng.");
            return;
        }
        if (firstInitialization) {
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player,
                    "Phượng hoàng lửa đã được khai mở cấp 1 với KI ngẫu nhiên từ 1-70%.");
        }

        if (currentLevel >= PhuongHoangLuaService.MAX_LEVEL) {
            showCloseMenu(player, "Phượng hoàng lửa đã đạt cấp tối đa 6.\nChỉ số: "
                    + phoenixService.getStatSummary(phoenix));
            return;
        }

        int targetLevel = currentLevel + 1;
        int gemCost = getGemCost(targetLevel);
        int stoneCost = getStoneCost(targetLevel);
        int successRate = getSuccessRate(targetLevel);
        player.combineNew.goldCombine = 0;
        player.combineNew.gemCombine = gemCost;
        player.combineNew.ratioCombine = successRate;

        String npcSay = "Nâng Phượng hoàng lửa từ cấp " + currentLevel + " lên cấp " + targetLevel + "\n"
                + "Cần: " + Util.numberToMoney(stoneCost) + " Đá ngũ sắc\n"
                + "Phí: " + Util.numberToMoney(gemCost) + " ngọc xanh\n"
                + "Tỉ lệ thành công: " + successRate + "%\n"
                + "Khi thành công: " + getUpgradeDescription(targetLevel) + "\n"
                + "Thành công hoặc thất bại đều tiêu hao đủ nguyên liệu.";
        if (targetLevel == 4) {
            npcSay += "\n|7|CẢNH BÁO: Lên cấp 4 sẽ tẩy toàn bộ KI, HP và Sức đánh cũ, "
                    + "sau đó tạo lại các chỉ số cấp 4.";
        }

        if (stone.quantity < stoneCost) {
            showCloseMenu(player, npcSay + "\nCòn thiếu "
                    + Util.numberToMoney(stoneCost - stone.quantity) + " Đá ngũ sắc.");
            return;
        }
        if (player.inventory.gem < gemCost) {
            showCloseMenu(player, npcSay + "\nCòn thiếu "
                    + Util.numberToMoney(gemCost - player.inventory.gem) + " ngọc xanh.");
            return;
        }

        String acceptButton = targetLevel == 4
                ? "Chấp nhận tẩy\nvà lên cấp 4"
                : "Nâng cấp\n" + successRate + "%";
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay, acceptButton, "Từ chối");
    }

    public static void upgrade(Player player) {
        synchronized (player) {
            Item phoenix = findPhoenix(player);
            Item stone = findNguSacStone(player);
            if (!hasExactlyTwoItems(player) || phoenix == null || stone == null
                    || InventoryService.gI().getIndexItemBag(player, phoenix) < 0
                    || InventoryService.gI().getIndexItemBag(player, stone) < 0) {
                Service.gI().sendThongBao(player,
                        "Vật phẩm nâng cấp không còn hợp lệ trong hành trang.");
                return;
            }

            PhuongHoangLuaService phoenixService = PhuongHoangLuaService.gI();
            phoenixService.normalize(phoenix);
            int currentLevel = phoenixService.getLevel(phoenix);
            if (currentLevel < PhuongHoangLuaService.MIN_LEVEL) {
                Service.gI().sendThongBao(player,
                        "Phượng hoàng lửa đang thiếu dòng cấp (option 72). Vật phẩm được khóa nâng để tránh mất chỉ số; hãy báo admin.");
                return;
            }
            if (currentLevel >= PhuongHoangLuaService.MAX_LEVEL) {
                Service.gI().sendThongBao(player, "Phượng hoàng lửa đã đạt cấp tối đa 6.");
                return;
            }

            int targetLevel = currentLevel + 1;
            int gemCost = getGemCost(targetLevel);
            int stoneCost = getStoneCost(targetLevel);
            int successRate = getSuccessRate(targetLevel);
            if (stoneCost <= 0 || stone.quantity < stoneCost) {
                Service.gI().sendThongBao(player, "Không đủ "
                        + Util.numberToMoney(stoneCost) + " Đá ngũ sắc để nâng lên cấp " + targetLevel + ".");
                return;
            }
            if (player.inventory.gem < gemCost) {
                Service.gI().sendThongBao(player, "Không đủ "
                        + Util.numberToMoney(gemCost) + " ngọc xanh để nâng cấp.");
                return;
            }

            boolean success = Util.isTrue(successRate, 100);
            if (success && !phoenixService.upgrade(phoenix, targetLevel)) {
                Service.gI().sendThongBao(player,
                        "Không thể cập nhật cấp Phượng hoàng lửa, nguyên liệu chưa bị trừ.");
                return;
            }

            player.inventory.subGem(gemCost);
            InventoryService.gI().subQuantityItemsBag(player, stone, stoneCost);
            if (InventoryService.gI().getIndexItemBag(player, stone) < 0) {
                player.combineNew.itemsCombine.remove(stone);
            }
            boolean savedImmediately = PlayerDAO.updateInventoryAndBag(player);

            if (success) {
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player,
                        "Nâng cấp thành công Phượng hoàng lửa lên cấp " + targetLevel
                        + ". Chỉ số hiện tại: " + phoenixService.getStatSummary(phoenix));
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player,
                        "Nâng cấp thất bại, Phượng hoàng lửa vẫn ở cấp " + currentLevel
                        + ". Đã tiêu hao " + Util.numberToMoney(stoneCost) + " Đá ngũ sắc và "
                        + Util.numberToMoney(gemCost) + " ngọc xanh.");
            }

            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
            if (!savedImmediately) {
                Service.gI().sendThongBao(player,
                        "Cảnh báo: chưa thể lưu ngay dữ liệu Phượng hoàng lửa, vui lòng không thoát game và báo admin.");
            }
        }
    }

    public static String getUpgradeGuide() {
        return "Cấp 1: KI 1-70%\n"
                + "1 → 2: 10 Đá ngũ sắc, 10.000 ngọc, 50% - mở HP 1-70%\n"
                + "2 → 3: 30 Đá ngũ sắc, 30.000 ngọc, 30% - mở Sức đánh 1-50%\n"
                + "3 → 4: 200 Đá ngũ sắc, 100.000 ngọc, 100% - tẩy và tạo lại KI/HP/SĐ, mở ST chí mạng\n"
                + "4 → 5: 50 Đá ngũ sắc, 20.000 ngọc, 5% - mở 1 dòng QCKK/Tự sát/Laze\n"
                + "5 → 6: 50 Đá ngũ sắc, 20.000 ngọc, 1% - mở 1 dòng Xuyên giáp/Giáp/Chí mạng";
    }

    public static int getGemCost(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> 10_000;
            case 3 -> 30_000;
            case 4 -> 100_000;
            case 5, 6 -> 20_000;
            default -> 0;
        };
    }

    public static int getStoneCost(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> 10;
            case 3 -> 30;
            case 4 -> 200;
            case 5, 6 -> 50;
            default -> 0;
        };
    }

    public static int getSuccessRate(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> 50;
            case 3 -> 30;
            case 4 -> 100;
            case 5 -> 5;
            case 6 -> 1;
            default -> 0;
        };
    }

    private static String getUpgradeDescription(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> "mở HP ngẫu nhiên 1-70%, giữ nguyên KI";
            case 3 -> "mở Sức đánh ngẫu nhiên 1-50%, giữ nguyên KI và HP";
            case 4 -> "tạo lại KI 1-100%, HP 1-100%, Sức đánh 1-70% và Sát thương chí mạng 1-30%";
            case 5 -> "mở ngẫu nhiên 1 dòng Sát thương QCKK, Tự sát hoặc Laze từ 1-20%";
            case 6 -> "mở ngẫu nhiên 1 dòng Xuyên giáp, Giáp hoặc Chí mạng từ 1-25%";
            default -> "không xác định";
        };
    }

    private static Item findPhoenix(Player player) {
        if (!hasCombineItems(player)) {
            return null;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (PhuongHoangLuaService.gI().isPhuongHoangLua(item)) {
                return item;
            }
        }
        return null;
    }

    private static Item findNguSacStone(Player player) {
        if (!hasCombineItems(player)) {
            return null;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem()
                    && item.template.id == ConstItem.DA_NGU_SAC) {
                return item;
            }
        }
        return null;
    }

    private static boolean hasExactlyTwoItems(Player player) {
        return hasCombineItems(player) && player.combineNew.itemsCombine.size() == 2;
    }

    private static boolean hasCombineItems(Player player) {
        return player != null && player.combineNew != null
                && player.combineNew.itemsCombine != null;
    }

    private static void showCloseMenu(Player player, String text) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text, "Đóng");
    }
}
