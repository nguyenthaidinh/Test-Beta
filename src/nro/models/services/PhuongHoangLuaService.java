package nro.models.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.models.consts.ConstItem;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.utils.Util;

/** Quản lý cấp và các dòng chỉ số riêng của Phượng hoàng lửa (item 1144). */
public final class PhuongHoangLuaService {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 6;

    public static final int OPTION_LEVEL = 72;
    public static final int OPTION_KI = 103;
    public static final int OPTION_HP = 77;
    public static final int OPTION_DAMAGE = 50;
    public static final int OPTION_CRITICAL_DAMAGE = 5;
    public static final int OPTION_ARMOR_PENETRATION = HellWolfPetService.OPTION_ARMOR_PENETRATION;
    public static final int OPTION_SELF_DESTRUCT_DAMAGE = HellWolfPetService.OPTION_SELF_DESTRUCT_DAMAGE;
    public static final int OPTION_LAZE_DAMAGE = HellWolfPetService.OPTION_LAZE_DAMAGE;
    public static final int OPTION_QCKK_DAMAGE = HellWolfPetService.OPTION_QCKK_DAMAGE;
    public static final int OPTION_ARMOR = 94;
    public static final int OPTION_CRITICAL_CHANCE = 14;

    private static final int[] CONTROLLED_OPTIONS = {
        OPTION_LEVEL,
        OPTION_KI,
        OPTION_HP,
        OPTION_DAMAGE,
        OPTION_CRITICAL_DAMAGE,
        OPTION_SELF_DESTRUCT_DAMAGE,
        OPTION_LAZE_DAMAGE,
        OPTION_QCKK_DAMAGE,
        OPTION_ARMOR_PENETRATION,
        OPTION_ARMOR,
        OPTION_CRITICAL_CHANCE
    };

    private static final int[] LEVEL_FIVE_OPTIONS = {
        OPTION_QCKK_DAMAGE,
        OPTION_SELF_DESTRUCT_DAMAGE,
        OPTION_LAZE_DAMAGE
    };

    private static final int[] LEVEL_SIX_OPTIONS = {
        OPTION_ARMOR_PENETRATION,
        OPTION_ARMOR,
        OPTION_CRITICAL_CHANCE
    };

    private static final PhuongHoangLuaService INSTANCE = new PhuongHoangLuaService();

    private PhuongHoangLuaService() {
    }

    public static PhuongHoangLuaService gI() {
        return INSTANCE;
    }

    public boolean isPhuongHoangLua(Item item) {
        return item != null && item.isNotNullItem()
                && item.template.id == ConstItem.PHUONG_HOANG_LUA;
    }

    public boolean isInitialized(Item item) {
        return isPhuongHoangLua(item) && findOption(item, OPTION_LEVEL) != null;
    }

    /**
     * Khai mở vật phẩm cũ chưa có cấp và dọn các dòng trùng/sai giới hạn.
     * Vật phẩm thô được tính là cấp 1 và nhận KI ngẫu nhiên 1-70% đúng một lần.
     */
    public void normalize(Item item) {
        if (!isPhuongHoangLua(item)) {
            return;
        }
        if (item.itemOptions == null) {
            item.itemOptions = new ArrayList<>();
        }

        ItemOption oldLevel = findOption(item, OPTION_LEVEL);
        boolean firstInitialization = oldLevel == null;
        int level = firstInitialization ? MIN_LEVEL : clampLevel(oldLevel.param);

        List<ItemOption> preserved = new ArrayList<>();
        Map<Integer, Integer> values = new LinkedHashMap<>();
        for (ItemOption option : item.itemOptions) {
            if (option == null || option.optionTemplate == null) {
                continue;
            }
            int optionId = option.optionTemplate.id;
            if (isControlledOption(optionId)) {
                if (optionId != OPTION_LEVEL) {
                    values.merge(optionId, Math.max(0, option.param), Math::max);
                }
            } else {
                preserved.add(option);
            }
        }

        if (!values.containsKey(OPTION_KI)) {
            values.put(OPTION_KI, randomPercent(70));
        }
        if (level >= 2 && !values.containsKey(OPTION_HP)) {
            values.put(OPTION_HP, randomPercent(70));
        }
        if (level >= 3 && !values.containsKey(OPTION_DAMAGE)) {
            values.put(OPTION_DAMAGE, randomPercent(50));
        }
        if (level >= 4 && !values.containsKey(OPTION_CRITICAL_DAMAGE)) {
            values.put(OPTION_CRITICAL_DAMAGE, randomPercent(30));
        }

        int levelFiveOption = selectOneExisting(values, LEVEL_FIVE_OPTIONS);
        if (level >= 5 && levelFiveOption == -1) {
            levelFiveOption = randomOption(LEVEL_FIVE_OPTIONS);
            values.put(levelFiveOption, randomPercent(20));
        }
        int levelSixOption = selectOneExisting(values, LEVEL_SIX_OPTIONS);
        if (level >= 6 && levelSixOption == -1) {
            levelSixOption = randomOption(LEVEL_SIX_OPTIONS);
            values.put(levelSixOption, randomPercent(25));
        }

        List<ItemOption> normalized = new ArrayList<>(preserved);
        normalized.add(new ItemOption(OPTION_LEVEL, level));
        addIfAvailable(normalized, values, OPTION_KI, level >= 4 ? 100 : 70);
        if (level >= 2) {
            addIfAvailable(normalized, values, OPTION_HP, level >= 4 ? 100 : 70);
        }
        if (level >= 3) {
            addIfAvailable(normalized, values, OPTION_DAMAGE, level >= 4 ? 70 : 50);
        }
        if (level >= 4) {
            addIfAvailable(normalized, values, OPTION_CRITICAL_DAMAGE, 30);
        }
        if (level >= 5 && levelFiveOption != -1) {
            addIfAvailable(normalized, values, levelFiveOption, 20);
        }
        if (level >= 6 && levelSixOption != -1) {
            addIfAvailable(normalized, values, levelSixOption, 25);
        }

        item.itemOptions.clear();
        item.itemOptions.addAll(normalized);
        item.info = item.getInfo();
        item.content = item.getContent();
    }

    public int getLevel(Item item) {
        normalize(item);
        ItemOption level = findOption(item, OPTION_LEVEL);
        return level == null ? MIN_LEVEL : clampLevel(level.param);
    }

    /** Gọi duy nhất sau khi lần đập cấp đã thành công. */
    public boolean upgrade(Item item, int targetLevel) {
        normalize(item);
        if (!isPhuongHoangLua(item)) {
            return false;
        }
        int currentLevel = getLevel(item);
        if (targetLevel != currentLevel + 1 || targetLevel > MAX_LEVEL) {
            return false;
        }

        setOption(item, OPTION_LEVEL, targetLevel);
        switch (targetLevel) {
            case 2 -> setOption(item, OPTION_HP, randomPercent(70));
            case 3 -> setOption(item, OPTION_DAMAGE, randomPercent(50));
            case 4 -> {
                removeControlledStats(item);
                setOption(item, OPTION_LEVEL, targetLevel);
                setOption(item, OPTION_KI, randomPercent(100));
                setOption(item, OPTION_HP, randomPercent(100));
                setOption(item, OPTION_DAMAGE, randomPercent(70));
                setOption(item, OPTION_CRITICAL_DAMAGE, randomPercent(30));
            }
            case 5 -> setOption(item, randomOption(LEVEL_FIVE_OPTIONS), randomPercent(20));
            case 6 -> setOption(item, randomOption(LEVEL_SIX_OPTIONS), randomPercent(25));
            default -> {
                return false;
            }
        }
        normalize(item);
        return getLevel(item) == targetLevel;
    }

    /** Giữ nguyên cấp và tạo lại toàn bộ các dòng đã mở của cấp hiện tại. */
    public boolean rerollCurrentLevel(Item item) {
        normalize(item);
        if (!isPhuongHoangLua(item)) {
            return false;
        }
        int level = getLevel(item);
        removeControlledStats(item);
        setOption(item, OPTION_LEVEL, level);
        setOption(item, OPTION_KI, randomPercent(level >= 4 ? 100 : 70));
        if (level >= 2) {
            setOption(item, OPTION_HP, randomPercent(level >= 4 ? 100 : 70));
        }
        if (level >= 3) {
            setOption(item, OPTION_DAMAGE, randomPercent(level >= 4 ? 70 : 50));
        }
        if (level >= 4) {
            setOption(item, OPTION_CRITICAL_DAMAGE, randomPercent(30));
        }
        if (level >= 5) {
            setOption(item, randomOption(LEVEL_FIVE_OPTIONS), randomPercent(20));
        }
        if (level >= 6) {
            setOption(item, randomOption(LEVEL_SIX_OPTIONS), randomPercent(25));
        }
        normalize(item);
        return getLevel(item) == level;
    }

    public String getStatSummary(Item item) {
        normalize(item);
        StringBuilder result = new StringBuilder();
        for (ItemOption option : item.itemOptions) {
            if (option == null || option.optionTemplate == null
                    || option.optionTemplate.id == OPTION_LEVEL
                    || !isControlledOption(option.optionTemplate.id)) {
                continue;
            }
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(getOptionName(option.optionTemplate.id))
                    .append(' ').append(option.param).append('%');
        }
        return result.length() == 0 ? "Chưa có chỉ số" : result.toString();
    }

    public String getOptionName(int optionId) {
        return switch (optionId) {
            case OPTION_KI -> "KI";
            case OPTION_HP -> "HP";
            case OPTION_DAMAGE -> "Sức đánh";
            case OPTION_CRITICAL_DAMAGE -> "Sát thương chí mạng";
            case OPTION_QCKK_DAMAGE -> "Sát thương QCKK";
            case OPTION_SELF_DESTRUCT_DAMAGE -> "Sát thương Tự sát";
            case OPTION_LAZE_DAMAGE -> "Sát thương Laze";
            case OPTION_ARMOR_PENETRATION -> "Xuyên giáp";
            case OPTION_ARMOR -> "Giáp";
            case OPTION_CRITICAL_CHANCE -> "Chí mạng";
            default -> "Chỉ số";
        };
    }

    private void removeControlledStats(Item item) {
        item.itemOptions.removeIf(option -> option != null && option.optionTemplate != null
                && option.optionTemplate.id != OPTION_LEVEL
                && isControlledOption(option.optionTemplate.id));
    }

    private void setOption(Item item, int optionId, int value) {
        ItemOption option = findOption(item, optionId);
        if (option == null) {
            item.itemOptions.add(new ItemOption(optionId, value));
        } else {
            option.param = value;
        }
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

    private boolean isControlledOption(int optionId) {
        for (int id : CONTROLLED_OPTIONS) {
            if (id == optionId) {
                return true;
            }
        }
        return false;
    }

    private int selectOneExisting(Map<Integer, Integer> values, int[] optionIds) {
        for (int optionId : optionIds) {
            if (values.getOrDefault(optionId, 0) > 0) {
                return optionId;
            }
        }
        return -1;
    }

    private int randomOption(int[] optionIds) {
        return optionIds[Util.nextInt(0, optionIds.length - 1)];
    }

    private int randomPercent(int max) {
        return Util.nextInt(1, max);
    }

    private void addIfAvailable(List<ItemOption> options, Map<Integer, Integer> values,
            int optionId, int maxValue) {
        int value = Math.max(1, Math.min(maxValue, values.getOrDefault(optionId, 1)));
        options.add(new ItemOption(optionId, value));
    }

    private int clampLevel(int level) {
        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }
}
