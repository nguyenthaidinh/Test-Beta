package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.HellWolfPetService;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/** Ép đúng một Hồn ma vào pet Sói Địa Ngục. */
public final class EpOptionSoiDiaNguc {

    private static final int SUCCESS_RATE = 80;

    private EpOptionSoiDiaNguc() {
    }

    public static void showInfoCombine(Player player) {
        Item pet = findHellWolf(player);
        Item soul = findGhostSoul(player);
        if (player.combineNew.itemsCombine.size() != 2 || pet == null || soul == null) {
            showCloseMenu(player, "Cần chọn đúng 1 Sói Địa Ngục và 1 loại Hồn ma.");
            return;
        }

        HellWolfPetService wolfService = HellWolfPetService.gI();
        wolfService.normalizePet(pet);
        ItemOption soulOption = wolfService.getSoulStatOption(soul);
        if (soulOption == null) {
            showCloseMenu(player, "Hồn ma này không có loại chỉ số hợp lệ.");
            return;
        }

        int optionId = soulOption.optionTemplate.id;
        int level = wolfService.getLevel(pet);
        int requiredLevel = wolfService.getRequiredLevel(optionId);
        int cap = wolfService.getCap(level, optionId);
        int current = wolfService.getOptionValue(pet, optionId);
        String optionName = wolfService.getOptionName(optionId);

        if (level < requiredLevel || cap <= 0) {
            showCloseMenu(player, optionName + " chỉ được mở từ cấp " + requiredLevel + ".\n"
                    + "Sói Địa Ngục hiện đang ở cấp " + level + ".");
            return;
        }
        if (current >= cap) {
            showCloseMenu(player, optionName + " đã đạt giới hạn " + cap + "% ở cấp " + level + ".");
            return;
        }

        int added = Math.min(wolfService.getSoulIncrement(optionId), cap - current);
        player.combineNew.goldCombine = 0;
        player.combineNew.gemCombine = 0;
        player.combineNew.ratioCombine = SUCCESS_RATE;
        String npcSay = "Sói Địa Ngục cấp " + level + "\n"
                + optionName + ": " + current + "% → " + (current + added) + "%\n"
                + "Giới hạn hiện tại: " + cap + "%\n"
                + "Tỉ lệ thành công: " + SUCCESS_RATE + "%\n"
                + "Không tốn vàng, không tốn ngọc.\n"
                + "Thành công hoặc thất bại đều tiêu hao 1 Hồn ma.";
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay, "Ép option\nMiễn phí", "Từ chối");
    }

    public static void epOption(Player player) {
        synchronized (player) {
            Item pet = findHellWolf(player);
            Item soul = findGhostSoul(player);
            if (player.combineNew.itemsCombine.size() != 2 || pet == null || soul == null
                    || soul.quantity < 1
                    || InventoryService.gI().getIndexItemBag(player, pet) < 0
                    || InventoryService.gI().getIndexItemBag(player, soul) < 0) {
                Service.gI().sendThongBao(player, "Vật phẩm ép không còn hợp lệ trong hành trang.");
                return;
            }

            HellWolfPetService wolfService = HellWolfPetService.gI();
            ItemOption soulOption = wolfService.getSoulStatOption(soul);
            if (soulOption == null) {
                Service.gI().sendThongBao(player, "Hồn ma này không có loại chỉ số hợp lệ.");
                return;
            }

            int optionId = soulOption.optionTemplate.id;
            int level = wolfService.getLevel(pet);
            int cap = wolfService.getCap(level, optionId);
            int current = wolfService.getOptionValue(pet, optionId);
            if (cap <= 0 || current >= cap) {
                Service.gI().sendThongBao(player, "Chỉ số này chưa mở hoặc đã đạt giới hạn của cấp hiện tại.");
                return;
            }
            boolean success = Util.isTrue(SUCCESS_RATE, 100);
            int added = 0;
            if (success) {
                added = wolfService.addSoulStat(pet, optionId);
                if (added <= 0) {
                    Service.gI().sendThongBao(player, "Không thể tăng thêm chỉ số này.");
                    return;
                }
            }

            InventoryService.gI().subQuantityItemsBag(player, soul, 1);
            if (InventoryService.gI().getIndexItemBag(player, soul) < 0) {
                player.combineNew.itemsCombine.remove(soul);
            }
            if (success) {
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Ép thành công: "
                        + wolfService.getOptionName(optionId) + " +" + added + "%.");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Ép thất bại, đã tiêu hao 1 Hồn ma.");
            }
            InventoryService.gI().sendItemBags(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }

    private static Item findHellWolf(Player player) {
        if (player == null || player.combineNew == null || player.combineNew.itemsCombine == null) {
            return null;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (HellWolfPetService.gI().isHellWolf(item)) {
                return item;
            }
        }
        return null;
    }

    private static Item findGhostSoul(Player player) {
        if (player == null || player.combineNew == null || player.combineNew.itemsCombine == null) {
            return null;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (HellWolfPetService.gI().isGhostSoul(item)) {
                return item;
            }
        }
        return null;
    }

    private static void showCloseMenu(Player player, String text) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text, "Đóng");
    }
}
