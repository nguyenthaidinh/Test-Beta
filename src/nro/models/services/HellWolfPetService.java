package nro.models.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.models.consts.ConstItem;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player_system.Template;
import nro.models.utils.Util;

/**
 * Luật chỉ số riêng của Sói Địa Ngục (item 1654) và các loại Hồn ma dùng để ép.
 */
public final class HellWolfPetService {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 6;
    private static final int OPTION_DEFAULT = 73;
    public static final int OPTION_LEVEL = 72;

    public static final int OPTION_DAMAGE = 50;
    public static final int OPTION_HP = 77;
    public static final int OPTION_ARMOR = 94;
    public static final int OPTION_KI = 103;
    public static final int OPTION_CRITICAL_DAMAGE = 5;
    public static final int OPTION_ARMOR_PENETRATION = 251;
    public static final int OPTION_SELF_DESTRUCT_DAMAGE = 252;
    public static final int OPTION_LAZE_DAMAGE = 253;
    public static final int OPTION_QCKK_DAMAGE = 254;

    private static final int[] SOUL_OPTION_IDS = {
        OPTION_HP,
        OPTION_KI,
        OPTION_DAMAGE,
        OPTION_CRITICAL_DAMAGE,
        OPTION_ARMOR,
        OPTION_ARMOR_PENETRATION,
        OPTION_SELF_DESTRUCT_DAMAGE,
        OPTION_LAZE_DAMAGE,
        OPTION_QCKK_DAMAGE
    };

    private static final int[][] INITIAL_PET_OPTIONS = {
        {OPTION_DEFAULT, 0},
        {OPTION_LEVEL, MIN_LEVEL},
        {106, 0}, // Không ảnh hưởng bởi cái lạnh.
        {30, 1} // Không thể giao dịch.
    };

    private static final HellWolfPetService INSTANCE = new HellWolfPetService();

    private HellWolfPetService() {
    }

    public static HellWolfPetService gI() {
        return INSTANCE;
    }

    /**
     * Bộ option khởi tạo cho Sói Địa Ngục mới. Pet bắt đầu ở cấp 1, giữ công
     * dụng chống lạnh và không giao dịch; không có sẵn chỉ số ép và không có
     * hạn sử dụng.
     */
    public List<ItemOption> createInitialPetOptions() {
        List<ItemOption> options = new ArrayList<>();
        for (int[] option : INITIAL_PET_OPTIONS) {
            options.add(new ItemOption(option[0], option[1]));
        }
        return options;
    }

    public void normalizeTemplate(Template.ItemTemplate template) {
        if (template == null || template.id != ConstItem.SOI_DIA_NGUC) {
            return;
        }
        template.name = "Sói Địa Ngục";
        template.description = "Pet 6 cấp, dùng Hồn ma để ép và tăng các chỉ số";
    }

    /**
     * Pet cũ chưa có option cấp được chuyển về cấp 1 và xóa các chỉ số tặng sẵn.
     * Pet đã có option cấp chỉ được gộp dòng trùng và chặn đúng trần của cấp hiện tại.
     */
    public void normalizePet(Item pet) {
        if (!isHellWolf(pet) || pet.itemOptions == null) {
            return;
        }
        normalizeTemplate(pet.template);

        ItemOption levelOption = findOption(pet, OPTION_LEVEL);
        boolean missingLevel = levelOption == null;
        int level = missingLevel ? MIN_LEVEL : clampLevel(levelOption.param);

        List<ItemOption> preservedOptions = new ArrayList<>();
        Map<Integer, Integer> controlledValues = new LinkedHashMap<>();
        boolean hasNonTradeOption = false;
        for (ItemOption option : pet.itemOptions) {
            if (option == null || option.optionTemplate == null) {
                continue;
            }
            int optionId = option.optionTemplate.id;
            if (optionId == OPTION_LEVEL) {
                continue;
            }
            if (isSoulOption(optionId)) {
                controlledValues.merge(optionId, Math.max(0, option.param), Math::max);
                continue;
            }
            hasNonTradeOption |= optionId == 30;
            preservedOptions.add(option);
        }

        /*
         * Không được coi một Sói đang có chỉ số ép nhưng bị thiếu option cấp là
         * pet cũ. Nếu tiếp tục chuẩn hóa, toàn bộ chỉ số sẽ bị xóa và Sói bị đưa
         * về cấp 1. Giữ nguyên dữ liệu để admin có thể phục hồi cấp; các chức
         * năng nâng/ép sẽ khóa vật phẩm này qua getLevel() trả về 0.
         */
        if (missingLevel && !controlledValues.isEmpty()) {
            if (!hasNonTradeOption) {
                pet.itemOptions.add(new ItemOption(30, 1));
            }
            pet.info = pet.getInfo();
            pet.content = pet.getContent();
            return;
        }

        if (!hasNonTradeOption) {
            preservedOptions.add(new ItemOption(30, 1));
        }
        preservedOptions.add(new ItemOption(OPTION_LEVEL, level));
        for (int optionId : SOUL_OPTION_IDS) {
            int cap = getCap(level, optionId);
            int value = Math.min(cap, controlledValues.getOrDefault(optionId, 0));
            if (value > 0) {
                preservedOptions.add(new ItemOption(optionId, value));
            }
        }

        pet.itemOptions.clear();
        pet.itemOptions.addAll(preservedOptions);
        pet.info = pet.getInfo();
        pet.content = pet.getContent();
    }

    /** Gán một loại ngẫu nhiên cho Hồn ma cũ hoặc Hồn ma mới chưa có loại. */
    public void normalizeSoul(Item soul) {
        if (!isGhostSoul(soul) || soul.itemOptions == null) {
            return;
        }
        ItemOption selected = null;
        boolean hasDefaultOption = false;
        List<ItemOption> normalized = new ArrayList<>();
        for (ItemOption option : soul.itemOptions) {
            if (option == null || option.optionTemplate == null) {
                continue;
            }
            if (isSoulOption(option.optionTemplate.id)) {
                if (selected == null) {
                    selected = new ItemOption(option.optionTemplate.id, getSoulIncrement(option.optionTemplate.id));
                }
            } else {
                normalized.add(option);
                hasDefaultOption |= option.optionTemplate.id == OPTION_DEFAULT;
            }
        }
        if (!hasDefaultOption) {
            normalized.add(new ItemOption(OPTION_DEFAULT, 0));
        }
        if (selected == null) {
            selected = createRandomSoulOption();
        }
        normalized.add(selected);
        soul.itemOptions.clear();
        soul.itemOptions.addAll(normalized);
        soul.info = soul.getInfo();
        soul.content = soul.getContent();
    }

    public ItemOption createRandomSoulOption() {
        int optionId = SOUL_OPTION_IDS[Util.nextInt(0, SOUL_OPTION_IDS.length - 1)];
        return new ItemOption(optionId, getSoulIncrement(optionId));
    }

    public ItemOption getSoulStatOption(Item soul) {
        normalizeSoul(soul);
        if (!isGhostSoul(soul)) {
            return null;
        }
        for (ItemOption option : soul.itemOptions) {
            if (option != null && option.optionTemplate != null
                    && isSoulOption(option.optionTemplate.id)) {
                return option;
            }
        }
        return null;
    }

    public int getLevel(Item pet) {
        normalizePet(pet);
        ItemOption levelOption = findOption(pet, OPTION_LEVEL);
        return levelOption == null ? 0 : clampLevel(levelOption.param);
    }

    public int getOptionValue(Item pet, int optionId) {
        normalizePet(pet);
        ItemOption option = findOption(pet, optionId);
        return option == null ? 0 : Math.max(0, option.param);
    }

    /** Nâng Sói đúng một cấp, giữ nguyên toàn bộ chỉ số đã ép. */
    public boolean upgradeLevel(Item pet) {
        normalizePet(pet);
        if (!isHellWolf(pet)) {
            return false;
        }
        ItemOption levelOption = findOption(pet, OPTION_LEVEL);
        if (levelOption == null || levelOption.param >= MAX_LEVEL) {
            return false;
        }
        levelOption.param++;
        normalizePet(pet);
        return true;
    }

    public int addSoulStat(Item pet, int optionId) {
        normalizePet(pet);
        if (!isSoulOption(optionId)) {
            return 0;
        }
        int cap = getCap(getLevel(pet), optionId);
        int current = getOptionValue(pet, optionId);
        int added = Math.min(getSoulIncrement(optionId), Math.max(0, cap - current));
        if (added <= 0) {
            return 0;
        }

        ItemOption option = findOption(pet, optionId);
        if (option == null) {
            pet.itemOptions.add(new ItemOption(optionId, added));
        } else {
            option.param += added;
        }
        normalizePet(pet);
        return added;
    }

    public int getCap(int level, int optionId) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            return 0;
        }
        level = clampLevel(level);
        return switch (optionId) {
            case OPTION_HP, OPTION_KI -> level == 1 ? 70 : 100;
            case OPTION_DAMAGE -> level == 1 ? 40 : 70;
            case OPTION_CRITICAL_DAMAGE, OPTION_ARMOR -> switch (level) {
                case 1, 2 -> 0;
                case 3 -> 10;
                case 4, 5 -> 25;
                default -> 30;
            };
            case OPTION_ARMOR_PENETRATION, OPTION_SELF_DESTRUCT_DAMAGE,
                    OPTION_LAZE_DAMAGE, OPTION_QCKK_DAMAGE -> switch (level) {
                case 1, 2, 3, 4 -> 0;
                case 5 -> 20;
                default -> 30;
            };
            default -> 0;
        };
    }

    public int getRequiredLevel(int optionId) {
        return switch (optionId) {
            case OPTION_HP, OPTION_KI, OPTION_DAMAGE -> 1;
            case OPTION_CRITICAL_DAMAGE, OPTION_ARMOR -> 3;
            case OPTION_ARMOR_PENETRATION, OPTION_SELF_DESTRUCT_DAMAGE,
                    OPTION_LAZE_DAMAGE, OPTION_QCKK_DAMAGE -> 5;
            default -> MAX_LEVEL + 1;
        };
    }

    public int getSoulIncrement(int optionId) {
        return switch (optionId) {
            case OPTION_HP, OPTION_KI -> 5;
            case OPTION_DAMAGE -> 2;
            case OPTION_CRITICAL_DAMAGE, OPTION_ARMOR, OPTION_ARMOR_PENETRATION,
                    OPTION_SELF_DESTRUCT_DAMAGE, OPTION_LAZE_DAMAGE, OPTION_QCKK_DAMAGE -> 2;
            default -> 0;
        };
    }

    public String getOptionName(int optionId) {
        return switch (optionId) {
            case OPTION_HP -> "HP";
            case OPTION_KI -> "KI";
            case OPTION_DAMAGE -> "Sức đánh";
            case OPTION_CRITICAL_DAMAGE -> "Sát thương chí mạng";
            case OPTION_ARMOR -> "Giáp";
            case OPTION_ARMOR_PENETRATION -> "Xuyên giáp";
            case OPTION_SELF_DESTRUCT_DAMAGE -> "Sát thương Tự sát";
            case OPTION_LAZE_DAMAGE -> "Sát thương Laze";
            case OPTION_QCKK_DAMAGE -> "Sát thương Quả cầu Kênh Khi";
            default -> "Không xác định";
        };
    }

    public boolean isHellWolf(Item item) {
        return item != null && item.isNotNullItem()
                && item.template.id == ConstItem.SOI_DIA_NGUC;
    }

    public boolean isGhostSoul(Item item) {
        return item != null && item.isNotNullItem()
                && item.template.id == ConstItem.HON_MA;
    }

    public boolean isSoulOption(int optionId) {
        for (int supportedId : SOUL_OPTION_IDS) {
            if (supportedId == optionId) {
                return true;
            }
        }
        return false;
    }

    private ItemOption findOption(Item item, int optionId) {
        if (item == null || item.itemOptions == null) {
            return null;
        }
        for (ItemOption option : item.itemOptions) {
            if (option != null && option.optionTemplate != null
                    && option.optionTemplate.id == optionId) {
                return option;
            }
        }
        return null;
    }

    private int clampLevel(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }
}
