package nro.models.combine;

import java.util.ArrayList;
import java.util.List;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class TaiHienThanhThan {

    private static final int GOLD_TAI_HIEN = 2_000_000_000;
    private static final int RATIO_TAI_HIEN = 50;
    private static final int REQUIRED_DO_HUY_DIET = 5;
    private static final int REQUIRED_DO_KICH_HOAT = 5;
    private static final int REQUIRED_TOTAL_ITEMS = REQUIRED_DO_HUY_DIET + REQUIRED_DO_KICH_HOAT;
    private static final int GENDER_NEUTRAL = -1;
    private static final int GENDER_UNKNOWN = -2;

    public static void showInfoCombine(Player player) {
        if (!hasFullAngelSet(player)) {
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần mặc đủ 5 món trang bị Thiên Sứ để tái hiện Thánh Thần.", "Đóng");
            return;
        }
        if (player.combineNew.itemsCombine.size() != REQUIRED_TOTAL_ITEMS) {
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần đặt đúng 5 món Hủy Diệt và 5 món Kích Hoạt cùng hành tinh.", "Đóng");
            return;
        }

        MaterialCount count = countMaterials(player.combineNew.itemsCombine);
        if (!count.isValid()) {
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Công thức không hợp lệ!\n"
                    + "Cần: 5 món Hủy Diệt cùng hành tinh\n"
                    + "Cần: 5 món Kích Hoạt cùng hành tinh\n"
                    + "Cần ít nhất 1 dòng Kích Hoạt có bonus đang hoạt động\n"
                    + "Đồ Hủy Diệt và đồ Kích Hoạt phải cùng hành tinh.", "Đóng");
            return;
        }

        player.combineNew.goldCombine = GOLD_TAI_HIEN;
        player.combineNew.ratioCombine = RATIO_TAI_HIEN;

        String npcSay = "|2|Tái hiện Thánh Thần\n"
                + "|2|Hành tinh: " + getGenderName(count.gender) + "\n"
                + "|2|Tỉ lệ thành công: " + RATIO_TAI_HIEN + "%\n"
                + "|2|Cần: 5 món Hủy Diệt cùng hành tinh\n"
                + "|2|Cần: 5 món Kích Hoạt cùng hành tinh\n"
                + "|2|Nhận: 1 trang bị Thần Linh Kích Hoạt cùng hành tinh\n"
                + "|2|Cần: " + Util.numberToMoney(GOLD_TAI_HIEN) + " vàng\n"
                + "|7|Thất bại vẫn mất vàng và nguyên liệu";

        if (player.inventory.gold < GOLD_TAI_HIEN) {
            npcSay += "\n|7|Còn thiếu " + Util.powerToString(GOLD_TAI_HIEN - player.inventory.gold) + " vàng";
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().whis.createOtherMenu(player, CombineService.TAI_HIEN_THANH_THAN, npcSay,
                    "Tái hiện\n" + Util.numberToMoney(GOLD_TAI_HIEN) + " vàng", "Từ chối");
        }
    }

    public static void taiHien(Player player) {
        if (!hasFullAngelSet(player)) {
            Service.gI().sendThongBao(player, "Cần mặc đủ 5 món trang bị Thiên Sứ để tái hiện Thánh Thần.");
            return;
        }
        if (player.combineNew.itemsCombine.size() != REQUIRED_TOTAL_ITEMS) {
            Service.gI().sendThongBao(player, "Cần đặt đúng 5 món Hủy Diệt và 5 món Kích Hoạt cùng hành tinh.");
            return;
        }

        MaterialCount count = countMaterials(player.combineNew.itemsCombine);
        if (!count.isValid()) {
            Service.gI().sendThongBao(player, "Công thức không hợp lệ, cần cùng hành tinh và có ít nhất 1 dòng Kích Hoạt đang hoạt động.");
            return;
        }
        if (player.inventory.gold < GOLD_TAI_HIEN) {
            Service.gI().sendThongBao(player, "Không đủ vàng để tái hiện Thánh Thần.");
            return;
        }

        int gender = count.gender;
        int kichHoatOption = count.randomKichHoatOption();

        player.inventory.gold -= GOLD_TAI_HIEN;
        removeMaterials(player);

        if (Util.isTrue(RATIO_TAI_HIEN, 100)) {
            Item item = ItemService.gI().createDoThanLinhKichHoat(gender, kichHoatOption);
            if (item != null && InventoryService.gI().addItemBag(player, item)) {
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Tái hiện thành công! Nhận được " + item.template.name);
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Tái hiện thất bại do hành trang không đủ chỗ.");
            }
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Tái hiện thất bại!");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static boolean hasFullAngelSet(Player player) {
        return player != null && player.setClothes != null && player.setClothes.checkSetAngel();
    }

    private static MaterialCount countMaterials(List<Item> items) {
        MaterialCount count = new MaterialCount();
        for (Item item : items) {
            if (isDoHuyDiet(item)) {
                count.doHuyDiet++;
                count.acceptGender(getGenderFromDoHuyDiet(item));
            } else {
                int kichHoatOption = getMainKichHoatOption(item);
                if (kichHoatOption != -1) {
                    count.doKichHoat++;
                    count.acceptGender(getGenderFromDoKichHoat(item, kichHoatOption));
                    if (isRuntimeKichHoatOption(kichHoatOption)) {
                        count.kichHoatOptions.add(kichHoatOption);
                    }
                } else {
                    count.invalid++;
                }
            }
        }
        return count;
    }

    private static boolean isDoHuyDiet(Item item) {
        return item != null && item.isNotNullItem() && item.isDHD();
    }

    private static int getMainKichHoatOption(Item item) {
        if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
            return -1;
        }
        for (Item.ItemOption option : item.itemOptions) {
            int optionId = getOptionId(option);
            if (isMainKichHoatOption(optionId)) {
                return optionId;
            }
        }
        for (Item.ItemOption option : item.itemOptions) {
            int mainOption = normalizeKichHoatOption(getOptionId(option));
            if (mainOption != -1) {
                return mainOption;
            }
        }
        return -1;
    }

    private static int getOptionId(Item.ItemOption option) {
        if (option == null || option.optionTemplate == null) {
            return -1;
        }
        return option.optionTemplate.id;
    }

    private static boolean isMainKichHoatOption(int optionId) {
        return optionId >= 127 && optionId <= 135
                || optionId == 233
                || optionId == 237
                || optionId == 241
                || optionId == 245;
    }

    private static boolean isRuntimeKichHoatOption(int optionId) {
        return optionId != 233;
    }

    private static int normalizeKichHoatOption(int optionId) {
        switch (optionId) {
            case 127:
            case 139:
                return 127;
            case 128:
            case 140:
                return 128;
            case 129:
            case 141:
                return 129;
            case 130:
            case 142:
                return 130;
            case 131:
            case 143:
                return 131;
            case 132:
            case 144:
                return 132;
            case 133:
            case 136:
                return 133;
            case 134:
            case 137:
                return 134;
            case 135:
            case 138:
                return 135;
            case 233:
            case 234:
                return 233;
            case 237:
            case 238:
            case 239:
            case 240:
                return 237;
            case 241:
            case 242:
            case 243:
            case 244:
                return 241;
            case 245:
            case 246:
            case 247:
            case 248:
                return 245;
            default:
                return -1;
        }
    }

    private static int getGenderFromDoHuyDiet(Item item) {
        switch (item.template.id) {
            case 650:
            case 651:
            case 657:
            case 658:
                return 0;
            case 652:
            case 653:
            case 659:
            case 660:
                return 1;
            case 654:
            case 655:
            case 661:
            case 662:
                return 2;
            case 656:
                return GENDER_NEUTRAL;
            default:
                return GENDER_UNKNOWN;
        }
    }

    private static int getGenderFromDoKichHoat(Item item, int kichHoatOption) {
        int optionGender = getGenderFromKichHoatOption(kichHoatOption);
        int templateGender = getGenderFromTemplate(item);
        if (optionGender >= 0 && templateGender >= 0 && optionGender != templateGender) {
            return GENDER_UNKNOWN;
        }
        if (optionGender >= 0) {
            return optionGender;
        }
        if (templateGender >= 0) {
            return templateGender;
        }
        return optionGender;
    }

    private static int getGenderFromTemplate(Item item) {
        if (item == null || item.template == null) {
            return GENDER_UNKNOWN;
        }
        if (item.template.gender >= 0 && item.template.gender <= 2) {
            return item.template.gender;
        }
        return GENDER_NEUTRAL;
    }

    private static int getGenderFromKichHoatOption(int optionId) {
        switch (optionId) {
            case 127:
            case 128:
            case 129:
            case 245:
                return 0;
            case 130:
            case 131:
            case 132:
            case 237:
                return 1;
            case 133:
            case 134:
            case 135:
            case 241:
                return 2;
            case 233:
                return GENDER_NEUTRAL;
            default:
                return GENDER_UNKNOWN;
        }
    }

    private static String getGenderName(int gender) {
        switch (gender) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namek";
            case 2:
                return "Xayda";
            default:
                return "Không rõ";
        }
    }

    private static void removeMaterials(Player player) {
        int removedDoHuyDiet = 0;
        int removedDoKichHoat = 0;
        for (Item item : new ArrayList<>(player.combineNew.itemsCombine)) {
            if (removedDoHuyDiet < REQUIRED_DO_HUY_DIET && isDoHuyDiet(item)) {
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                removedDoHuyDiet++;
            } else if (removedDoKichHoat < REQUIRED_DO_KICH_HOAT && getMainKichHoatOption(item) != -1) {
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                removedDoKichHoat++;
            }
        }
    }

    private static class MaterialCount {

        private int doHuyDiet;
        private int doKichHoat;
        private int invalid;
        private int gender = GENDER_UNKNOWN;
        private boolean genderMismatch;
        private final List<Integer> kichHoatOptions = new ArrayList<>();

        private void acceptGender(int itemGender) {
            if (itemGender == GENDER_NEUTRAL) {
                return;
            }
            if (itemGender == GENDER_UNKNOWN) {
                invalid++;
                return;
            }
            if (gender == GENDER_UNKNOWN) {
                gender = itemGender;
            } else if (gender != itemGender) {
                genderMismatch = true;
            }
        }

        private boolean isValid() {
            return doHuyDiet == REQUIRED_DO_HUY_DIET
                    && doKichHoat == REQUIRED_DO_KICH_HOAT
                    && invalid == 0
                    && !genderMismatch
                    && gender >= 0
                    && !kichHoatOptions.isEmpty();
        }

        private int randomKichHoatOption() {
            return kichHoatOptions.get(Util.nextInt(kichHoatOptions.size()));
        }
    }
}
