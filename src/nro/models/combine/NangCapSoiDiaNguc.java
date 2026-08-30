package nro.models.combine;

import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.HellWolfPetService;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/** Nâng cấp Sói Địa Ngục bằng Thịt tươi (item 1549). */
public final class NangCapSoiDiaNguc {

    private static final int GEM_COST = 5_000;

    private NangCapSoiDiaNguc() {
    }

    public static void showInfoCombine(Player player) {
        Item wolf = findWolf(player);
        Item meat = findFreshMeat(player);
        if (player.combineNew.itemsCombine.size() != 2 || wolf == null || meat == null) {
            showCloseMenu(player, "Cần chọn đúng 1 Sói Địa Ngục và Thịt tươi.");
            return;
        }

        HellWolfPetService wolfService = HellWolfPetService.gI();
        wolfService.normalizePet(wolf);
        int currentLevel = wolfService.getLevel(wolf);
        if (currentLevel < HellWolfPetService.MIN_LEVEL) {
            showCloseMenu(player, "Sói Địa Ngục đang thiếu dữ liệu cấp nhưng vẫn còn chỉ số.\n"
                    + "Hệ thống đã giữ nguyên toàn bộ option và tạm khóa nâng cấp.\n"
                    + "Hãy báo admin kiểm tra, không được bỏ hoặc thay đổi vật phẩm này.");
            return;
        }
        if (currentLevel >= HellWolfPetService.MAX_LEVEL) {
            showCloseMenu(player, "Sói Địa Ngục đã đạt cấp tối đa 6.");
            return;
        }

        int targetLevel = currentLevel + 1;
        int requiredMeat = getRequiredMeat(targetLevel);
        int successRate = getSuccessRate(targetLevel);
        player.combineNew.goldCombine = 0;
        player.combineNew.gemCombine = GEM_COST;
        player.combineNew.ratioCombine = successRate;

        String npcSay = "Nâng Sói Địa Ngục từ cấp " + currentLevel + " lên cấp " + targetLevel + "\n"
                + "Cần: " + Util.numberToMoney(requiredMeat) + " Thịt tươi\n"
                + "Phí nâng cấp: " + Util.numberToMoney(GEM_COST) + " ngọc\n"
                + "Hiện có trong ô nâng cấp: " + Util.numberToMoney(meat.quantity) + " Thịt tươi\n"
                + "Tỉ lệ thành công: " + successRate + "%\n"
                + "Chỉ số mở ở cấp " + targetLevel + ":\n" + getLevelBenefits(targetLevel) + "\n"
                + "Thành công hoặc thất bại đều tiêu hao Thịt tươi và ngọc.";
        if (meat.quantity < requiredMeat) {
            showCloseMenu(player, npcSay + "\nCòn thiếu "
                    + Util.numberToMoney(requiredMeat - meat.quantity) + " Thịt tươi.");
            return;
        }
        if (player.inventory.gem < GEM_COST) {
            showCloseMenu(player, npcSay + "\nCòn thiếu "
                    + Util.numberToMoney(GEM_COST - player.inventory.gem) + " ngọc.");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay, "Nâng cấp\n" + Util.numberToMoney(requiredMeat) + " Thịt tươi\n"
                        + Util.numberToMoney(GEM_COST) + " ngọc", "Từ chối");
    }

    public static void upgrade(Player player) {
        synchronized (player) {
            Item wolf = findWolf(player);
            Item meat = findFreshMeat(player);
            if (player.combineNew.itemsCombine.size() != 2 || wolf == null || meat == null
                    || InventoryService.gI().getIndexItemBag(player, wolf) < 0
                    || InventoryService.gI().getIndexItemBag(player, meat) < 0) {
                Service.gI().sendThongBao(player, "Vật phẩm nâng cấp không còn hợp lệ trong hành trang.");
                return;
            }

            HellWolfPetService wolfService = HellWolfPetService.gI();
            int currentLevel = wolfService.getLevel(wolf);
            if (currentLevel < HellWolfPetService.MIN_LEVEL) {
                Service.gI().sendThongBao(player,
                        "Sói Địa Ngục đang thiếu dữ liệu cấp. Option đã được bảo vệ, hãy báo admin kiểm tra.");
                return;
            }
            if (currentLevel >= HellWolfPetService.MAX_LEVEL) {
                Service.gI().sendThongBao(player, "Sói Địa Ngục đã đạt cấp tối đa 6.");
                return;
            }

            int targetLevel = currentLevel + 1;
            int requiredMeat = getRequiredMeat(targetLevel);
            int successRate = getActualSuccessRate(targetLevel);
            if (requiredMeat <= 0 || meat.quantity < requiredMeat) {
                Service.gI().sendThongBao(player, "Không đủ "
                        + Util.numberToMoney(requiredMeat) + " Thịt tươi để nâng lên cấp " + targetLevel + ".");
                return;
            }
            if (player.inventory.gem < GEM_COST) {
                Service.gI().sendThongBao(player, "Không đủ "
                        + Util.numberToMoney(GEM_COST) + " ngọc để nâng cấp Sói Địa Ngục.");
                return;
            }

            boolean success = Util.isTrue(successRate, 100);
            if (success && !wolfService.upgradeLevel(wolf)) {
                Service.gI().sendThongBao(player, "Không thể nâng cấp Sói Địa Ngục.");
                return;
            }

            player.inventory.gem -= GEM_COST;
            InventoryService.gI().subQuantityItemsBag(player, meat, requiredMeat);
            if (InventoryService.gI().getIndexItemBag(player, meat) < 0) {
                player.combineNew.itemsCombine.remove(meat);
            }
            boolean savedImmediately = PlayerDAO.updateInventoryAndBag(player);
            if (success) {
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Nâng cấp thành công Sói Địa Ngục lên cấp " + targetLevel
                        + ", đã dùng " + Util.numberToMoney(requiredMeat) + " Thịt tươi và "
                        + Util.numberToMoney(GEM_COST) + " ngọc.");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Nâng cấp thất bại, đã tiêu hao "
                        + Util.numberToMoney(requiredMeat) + " Thịt tươi và "
                        + Util.numberToMoney(GEM_COST) + " ngọc.");
            }
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
            if (!savedImmediately) {
                Service.gI().sendThongBao(player,
                        "Cảnh báo: chưa thể lưu ngay dữ liệu nâng Sói, vui lòng không thoát game và báo admin.");
            }
        }
    }

    public static int getRequiredMeat(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> 100;
            case 3 -> 300;
            case 4 -> 1_000;
            case 5 -> 3_000;
            case 6 -> 5_000;
            default -> 0;
        };
    }

    public static int getSuccessRate(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> 50;
            case 3 -> 40;
            case 4 -> 30;
            case 5 -> 10;
            case 6 -> 5;
            default -> 0;
        };
    }

    private static int getActualSuccessRate(int targetLevel) {
        return switch (targetLevel) {
            case 2 -> 50;
            case 3 -> 30;
            case 4 -> 15;
            case 5 -> 7;
            case 6 -> 3;
            default -> 0;
        };
    }

    public static String getUpgradeGuide() {
        return "Cấp 2: 100 Thịt tươi\n"
                + "Cấp 3: 300 Thịt tươi\n"
                + "Cấp 4: 1.000 Thịt tươi\n"
                + "Cấp 5: 3.000 Thịt tươi\n"
                + "Cấp 6: 5.000 Thịt tươi\n"
                + "Mỗi lượt: 5.000 ngọc\n"
                + "Tỉ lệ lên cấp 2/3/4/5/6: 50%/40%/30%/10%/5%";
    }

    public static String getLevelBenefits(int level) {
        return switch (level) {
            case 1 -> "HP, KI tối đa 70%; Sức đánh tối đa 40%";
            case 2 -> "HP, KI tối đa 100%; Sức đánh tối đa 70%";
            case 3 -> "HP, KI 100%; Sức đánh 70%; Giáp và ST chí mạng 10%";
            case 4 -> "HP, KI 100%; Sức đánh 70%; Giáp và ST chí mạng 25%";
            case 5 -> "HP, KI 100%; Sức đánh 70%; Giáp và ST chí mạng 25%; Xuyên giáp, Tự sát, Laze và QCKK 20%";
            case 6 -> "HP, KI 100%; Sức đánh 70%; các chỉ số còn lại tối đa 30%";
            default -> "Không xác định";
        };
    }

    private static Item findWolf(Player player) {
        if (!hasCombineItems(player)) {
            return null;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (HellWolfPetService.gI().isHellWolf(item)) {
                return item;
            }
        }
        return null;
    }

    private static Item findFreshMeat(Player player) {
        if (!hasCombineItems(player)) {
            return null;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem()
                    && item.template.id == ConstItem.THIT_TUOI_NANG_CAP_SOI) {
                return item;
            }
        }
        return null;
    }

    private static boolean hasCombineItems(Player player) {
        return player != null && player.combineNew != null && player.combineNew.itemsCombine != null;
    }

    private static void showCloseMenu(Player player, String text) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text, "Đóng");
    }
}
