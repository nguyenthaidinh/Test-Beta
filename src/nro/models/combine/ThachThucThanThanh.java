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

public class ThachThucThanThanh {

    private static final int GOLD_COST = 2_000_000_000;
    private static final int SUCCESS_RATE = 50;
    private static final int DA_NGU_SAC_ID = 674;
    private static final int DA_NGU_SAC_QUANTITY = 10;
    private static final int REQUIRED_DO_HUY_DIET = 5;
    private static final int REQUIRED_SELECTED_ITEMS = REQUIRED_DO_HUY_DIET + 2;
    private static final int GENDER_NEUTRAL = -1;
    private static final int GENDER_UNKNOWN = -2;

    public static void showInfoCombine(Player player) {
        if (!hasFullAngelSet(player)) {
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "C\u1ea7n m\u1eb7c \u0111\u1ee7 5 m\u00f3n trang b\u1ecb Thi\u00ean S\u1ee9 \u0111\u1ec3 th\u00e1ch th\u1ee9c Th\u1ea7n Th\u00e1nh.", "\u0110\u00f3ng");
            return;
        }

        MaterialCount count = countMaterials(player.combineNew.itemsCombine);
        if (!count.isValid(player.gender)) {
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "C\u00f4ng th\u1ee9c kh\u00f4ng h\u1ee3p l\u1ec7!\n"
                    + "C\u1ea7n: 1 \u0111\u1ed3 Th\u1ea7n Linh K\u00edch Ho\u1ea1t\n"
                    + "C\u1ea7n: 1 s\u00e9t H\u1ee7y Di\u1ec7t (5 m\u00f3n) c\u00f9ng h\u00e0nh tinh\n"
                    + "C\u1ea7n: " + DA_NGU_SAC_QUANTITY + " \u0110\u00e1 ng\u0169 s\u1eafc\n"
                    + "C\u1ea7n: m\u1eb7c full s\u00e9t Thi\u00ean S\u1ee9.", "\u0110\u00f3ng");
            return;
        }

        player.combineNew.goldCombine = GOLD_COST;
        player.combineNew.ratioCombine = SUCCESS_RATE;

        int gender = count.resolveGender(player.gender);
        String npcSay = "|2|Th\u00e1ch th\u1ee9c Th\u1ea7n Th\u00e1nh\n"
                + "|2|H\u00e0nh tinh: " + getGenderName(gender) + "\n"
                + "|2|T\u1ec9 l\u1ec7 th\u00e0nh c\u00f4ng: " + SUCCESS_RATE + "%\n"
                + "|2|C\u1ea7n: 1 \u0111\u1ed3 Th\u1ea7n Linh K\u00edch Ho\u1ea1t\n"
                + "|2|C\u1ea7n: 1 s\u00e9t H\u1ee7y Di\u1ec7t (5 m\u00f3n) c\u00f9ng h\u00e0nh tinh\n"
                + "|2|C\u1ea7n: " + DA_NGU_SAC_QUANTITY + " \u0110\u00e1 ng\u0169 s\u1eafc\n"
                + "|2|C\u1ea7n: " + Util.numberToMoney(GOLD_COST) + " v\u00e0ng\n"
                + "|2|Nh\u1eadn: 1 \u0111\u1ed3 Th\u1ea7n Th\u00e1nh random c\u00f9ng h\u00e0nh tinh\n"
                + "|7|Th\u1ea5t b\u1ea1i v\u1eabn m\u1ea5t v\u00e0ng v\u00e0 nguy\u00ean li\u1ec7u";

        if (player.inventory.gold < GOLD_COST) {
            npcSay += "\n|7|C\u00f2n thi\u1ebfu " + Util.powerToString(GOLD_COST - player.inventory.gold) + " v\u00e0ng";
            CombineService.gI().whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "\u0110\u00f3ng");
        } else {
            CombineService.gI().whis.createOtherMenu(player, CombineService.THACH_THUC_THAN_THANH, npcSay,
                    "Ch\u1ebf t\u1ea1o\n" + Util.numberToMoney(GOLD_COST) + " v\u00e0ng", "T\u1eeb ch\u1ed1i");
        }
    }

    public static void thachThuc(Player player) {
        if (!hasFullAngelSet(player)) {
            Service.gI().sendThongBao(player, "C\u1ea7n m\u1eb7c \u0111\u1ee7 5 m\u00f3n trang b\u1ecb Thi\u00ean S\u1ee9 \u0111\u1ec3 th\u00e1ch th\u1ee9c Th\u1ea7n Th\u00e1nh.");
            return;
        }

        MaterialCount count = countMaterials(player.combineNew.itemsCombine);
        if (!count.isValid(player.gender)) {
            Service.gI().sendThongBao(player, "C\u00f4ng th\u1ee9c kh\u00f4ng h\u1ee3p l\u1ec7.");
            return;
        }
        if (player.inventory.gold < GOLD_COST) {
            Service.gI().sendThongBao(player, "Kh\u00f4ng \u0111\u1ee7 v\u00e0ng \u0111\u1ec3 th\u00e1ch th\u1ee9c Th\u1ea7n Th\u00e1nh.");
            return;
        }

        int gender = count.resolveGender(player.gender);
        player.inventory.gold -= GOLD_COST;
        removeMaterials(player, count);

        if (Util.isTrue(SUCCESS_RATE, 100)) {
            Item item = createRandomDoThanThanh(gender);
            if (item != null && InventoryService.gI().addItemBag(player, item)) {
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Ch\u1ebf t\u1ea1o th\u00e0nh c\u00f4ng! Nh\u1eadn \u0111\u01b0\u1ee3c " + item.template.name);
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Ch\u1ebf t\u1ea1o th\u1ea5t b\u1ea1i do h\u00e0nh trang kh\u00f4ng \u0111\u1ee7 ch\u1ed7.");
            }
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Ch\u1ebf t\u1ea1o th\u1ea5t b\u1ea1i!");
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
        if (items == null || items.size() != REQUIRED_SELECTED_ITEMS) {
            count.invalid++;
            return count;
        }

        for (Item item : items) {
            if (item == null || !item.isNotNullItem()) {
                count.invalid++;
            } else if (isDoThanLinhKichHoat(item)) {
                count.acceptDoThanLinhKichHoat(item);
            } else if (item.isDHD()) {
                count.acceptDoHuyDiet(item);
            } else if (item.template.id == DA_NGU_SAC_ID) {
                count.daNguSacItems.add(item);
                count.totalDaNguSac += item.quantity;
            } else {
                count.invalid++;
            }
        }
        return count;
    }

    private static boolean isDoThanLinhKichHoat(Item item) {
        return item != null && item.isNotNullItem() && item.isDTL() && hasOldKichHoatOption(item);
    }

    private static boolean hasOldKichHoatOption(Item item) {
        if (item.itemOptions == null) {
            return false;
        }
        for (Item.ItemOption option : item.itemOptions) {
            if (option == null || option.optionTemplate == null) {
                continue;
            }
            int optionId = option.optionTemplate.id;
            if ((optionId >= 127 && optionId <= 135)
                    || optionId == 233
                    || (optionId >= 237 && optionId <= 248)) {
                return true;
            }
        }
        return false;
    }

    private static Item createRandomDoThanThanh(int gender) {
        switch (gender) {
            case 0:
                return ItemService.gI().createDoThanLinhKichHoat(0, 251, Util.nextInt(1, 3));
            case 1:
                return ItemService.gI().createDoThanLinhKichHoat(1, 251, Util.nextInt(4, 6));
            case 2: {
                int[] options = {252, 253, 254};
                return ItemService.gI().createDoThanLinhKichHoat(2, options[Util.nextInt(options.length)], 1);
            }
            default:
                return null;
        }
    }

    private static void removeMaterials(Player player, MaterialCount count) {
        InventoryService.gI().subQuantityItemsBag(player, count.doThanLinhKichHoat, 1);
        for (Item item : new ArrayList<>(count.doHuyDietItems)) {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        }

        int remaining = DA_NGU_SAC_QUANTITY;
        for (Item item : new ArrayList<>(count.daNguSacItems)) {
            if (remaining <= 0) {
                break;
            }
            int remove = Math.min(remaining, item.quantity);
            InventoryService.gI().subQuantityItemsBag(player, item, remove);
            remaining -= remove;
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

    private static int getGenderFromDoThanLinh(Item item) {
        int optionGender = getGenderFromKichHoatOptions(item);
        int templateGender = getGenderFromDoThanLinhTemplate(item);
        if (optionGender >= 0 && templateGender >= 0 && optionGender != templateGender) {
            return GENDER_UNKNOWN;
        }
        if (optionGender >= 0) {
            return optionGender;
        }
        return templateGender;
    }

    private static int getGenderFromDoThanLinhTemplate(Item item) {
        switch (item.template.id) {
            case 555:
            case 556:
            case 562:
            case 563:
                return 0;
            case 557:
            case 558:
            case 564:
            case 565:
                return 1;
            case 559:
            case 560:
            case 566:
            case 567:
                return 2;
            case 561:
                return GENDER_NEUTRAL;
            default:
                return GENDER_UNKNOWN;
        }
    }

    private static int getGenderFromKichHoatOptions(Item item) {
        int gender = GENDER_UNKNOWN;
        if (item.itemOptions == null) {
            return gender;
        }
        for (Item.ItemOption option : item.itemOptions) {
            int optionGender = getGenderFromKichHoatOption(option);
            if (optionGender == GENDER_UNKNOWN) {
                continue;
            }
            if (optionGender == GENDER_NEUTRAL) {
                if (gender == GENDER_UNKNOWN) {
                    gender = GENDER_NEUTRAL;
                }
                continue;
            }
            if (gender >= 0 && gender != optionGender) {
                return GENDER_UNKNOWN;
            }
            gender = optionGender;
        }
        return gender;
    }

    private static int getGenderFromKichHoatOption(Item.ItemOption option) {
        if (option == null || option.optionTemplate == null) {
            return GENDER_UNKNOWN;
        }
        switch (option.optionTemplate.id) {
            case 127:
            case 128:
            case 129:
            case 139:
            case 140:
            case 141:
            case 245:
            case 246:
            case 247:
            case 248:
                return 0;
            case 130:
            case 131:
            case 132:
            case 142:
            case 143:
            case 144:
            case 237:
            case 238:
            case 239:
            case 240:
                return 1;
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 241:
            case 242:
            case 243:
            case 244:
            case 252:
            case 253:
            case 254:
                return 2;
            case 251:
                if (option.param >= 1 && option.param <= 3) {
                    return 0;
                }
                if (option.param >= 4 && option.param <= 6) {
                    return 1;
                }
                return GENDER_UNKNOWN;
            case 233:
            case 234:
                return GENDER_NEUTRAL;
            default:
                return GENDER_UNKNOWN;
        }
    }

    private static String getGenderName(int gender) {
        switch (gender) {
            case 0:
                return "Tr\u00e1i \u0110\u1ea5t";
            case 1:
                return "Namek";
            case 2:
                return "Xayda";
            default:
                return "Kh\u00f4ng r\u00f5";
        }
    }

    private static class MaterialCount {

        private Item doThanLinhKichHoat;
        private final List<Item> doHuyDietItems = new ArrayList<>();
        private final boolean[] doHuyDietTypes = new boolean[5];
        private final List<Item> daNguSacItems = new ArrayList<>();
        private int totalDaNguSac;
        private int invalid;
        private int gender = GENDER_UNKNOWN;
        private boolean genderMismatch;

        private void acceptDoThanLinhKichHoat(Item item) {
            if (doThanLinhKichHoat != null) {
                invalid++;
                return;
            }
            doThanLinhKichHoat = item;
            acceptGender(getGenderFromDoThanLinh(item));
        }

        private void acceptDoHuyDiet(Item item) {
            if (doHuyDietItems.size() >= REQUIRED_DO_HUY_DIET) {
                invalid++;
                return;
            }
            int type = item.template.type;
            if (type < 0 || type >= doHuyDietTypes.length || doHuyDietTypes[type]) {
                invalid++;
                return;
            }
            doHuyDietTypes[type] = true;
            doHuyDietItems.add(item);
            acceptGender(getGenderFromDoHuyDiet(item));
        }

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

        private boolean isValid(int fallbackGender) {
            return doThanLinhKichHoat != null
                    && doHuyDietItems.size() == REQUIRED_DO_HUY_DIET
                    && totalDaNguSac >= DA_NGU_SAC_QUANTITY
                    && invalid == 0
                    && !genderMismatch
                    && resolveGender(fallbackGender) >= 0;
        }

        private int resolveGender(int fallbackGender) {
            if (gender >= 0) {
                return gender;
            }
            if (fallbackGender >= 0 && fallbackGender <= 2) {
                return fallbackGender;
            }
            return GENDER_UNKNOWN;
        }
    }
}
