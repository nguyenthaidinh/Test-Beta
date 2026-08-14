package nro.models.item;

import nro.models.consts.ConstItem;
import nro.models.player_system.Template;
import nro.models.player_system.Template.ItemTemplate;
import nro.models.services.ItemService;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Item {

    public ItemTemplate template;
    private static Map<String, String> OPTION_STRING = new HashMap<String, String>();

    public String info;

    public String content;

    public int quantity;

    public int quantityGD = 0;

    public List<ItemOption> itemOptions;

    public long createTime;
    public int id;
    public Object text;
    public long expire;
    public Iterable<ItemOption> options;

    public boolean isNotNullItem() {
        return this.template != null;
    }

    public String getName() {
        return template.name;
    }

    public Item() {
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public Item(short itemId) {
        this.template = ItemService.gI().getTemplate(itemId);
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public String getInfo() {
        String doThanThanhInfo = getDoThanThanhInfo();
        String strInfo = doThanThanhInfo == null ? "" : doThanThanhInfo + "\n";
        for (ItemOption itemOption : itemOptions) {
            if (itemOption == null) {
                continue;
            }
            if (itemOption.optionTemplate != null
                    && (ItemService.isDoThanThanhSetOption(itemOption.optionTemplate.id, itemOption.param)
                    || ItemService.isDoThanThanhDisplayOption(itemOption.optionTemplate.id))) {
                continue;
            }
            strInfo += itemOption.getOptionString();
        }
        return strInfo;
    }

    public String getContent() {
        String doThanThanhInfo = getDoThanThanhInfo();
        if (this.template != null
                && (this.template.id == ConstItem.KEO_NAO_NGUOI || this.template.id == ConstItem.KEO_BI_NGO
                || this.template.id == ConstItem.KEO_BAN_TAY || this.template.id == ConstItem.HOP_KEO_MA_QUY
                || this.template.id == ConstItem.MAY_DO_LINH_HON)
                && this.template.description != null && !this.template.description.isEmpty()) {
            return this.template.description;
        }
        if (doThanThanhInfo != null) {
            return doThanThanhInfo + "\n" + this.template.description;
        }
        return "Yêu cầu sức mạnh " + this.template.strRequire + " trở lên";
    }

    public List<ItemOption> getClientItemOptions() {
        List<ItemOption> clientOptions = new ArrayList<>();
        boolean addedDoThanThanhDisplay = false;
        for (ItemOption itemOption : itemOptions) {
            if (itemOption == null || itemOption.optionTemplate == null) {
                continue;
            }
            if (ItemService.isDoThanThanhSetOption(itemOption.optionTemplate.id, itemOption.param)) {
                if (!addedDoThanThanhDisplay) {
                    ItemOption displayOption = ItemService.getDoThanThanhDisplayOption(itemOption.param);
                    if (displayOption != null && displayOption.optionTemplate != null) {
                        clientOptions.add(displayOption);
                        addedDoThanThanhDisplay = true;
                    }
                }
                continue;
            }
            if (ItemService.isDoThanThanhDisplayOption(itemOption.optionTemplate.id)) {
                continue;
            }
            clientOptions.add(itemOption);
        }
        return clientOptions;
    }

    private String getDoThanThanhInfo() {
        for (ItemOption itemOption : itemOptions) {
            if (itemOption == null || itemOption.optionTemplate == null) {
                continue;
            }
            String optionString = ItemOption.getDoThanThanhOptionString(itemOption.optionTemplate.id, itemOption.param);
            if (optionString != null) {
                return optionString;
            }
        }
        return null;
    }

    public void dispose() {
        this.template = null;
        this.info = null;
        this.content = null;
        if (this.itemOptions != null) {
            for (ItemOption io : this.itemOptions) {
                if (io != null) {
                    io.dispose();
                }
            }
            this.itemOptions.clear();
        }
        this.itemOptions = null;
    }

    public static class ItemOption {

        public int param;

        public Template.ItemOptionTemplate optionTemplate;

        public ItemOption() {
        }

        public ItemOption(ItemOption io) {
            if (io == null) {
                return;
            }
            this.param = io.param;
            this.optionTemplate = io.optionTemplate;
        }

        public ItemOption(int tempId, int param) {
            this.optionTemplate = ItemService.gI().getItemOptionTemplate(tempId);
            this.param = param;
        }

        public ItemOption(Template.ItemOptionTemplate temp, int param) {
            this.optionTemplate = temp;
            this.param = param;
        }

        public String getOptionString() {
            if (this.optionTemplate == null) {
                return "";
            }
            String doThanThanhOption = getDoThanThanhOptionString(this.optionTemplate.id, this.param);
            if (doThanThanhOption != null) {
                return doThanThanhOption;
            }
            return Util.replace(this.optionTemplate.name, "#", String.valueOf(this.param));
        }

        public void dispose() {
            this.optionTemplate = null;
        }

        @Override
        public String toString() {
            final String n = "\"";
            int optionId = optionTemplate == null ? -1 : optionTemplate.id;
            return "{"
                    + n + "id" + n + ":" + n + optionId + n + ","
                    + n + "param" + n + ":" + n + param + n
                    + "}";
        }

        public String getOptionString(int param) {
            if (this.optionTemplate == null) {
                return "";
            }
            String doThanThanhOption = getDoThanThanhOptionString(this.optionTemplate.id, this.param);
            if (doThanThanhOption != null) {
                return doThanThanhOption;
            }
            String key = this.optionTemplate.name + "#" + param + "#";
            String value = OPTION_STRING.get(key);
            if (value == null) {
                value = Util.replace(this.optionTemplate.name, "#", String.valueOf(param));
                OPTION_STRING.put(key, value);
            }
            return value;
        }

        private static String getDoThanThanhOptionString(int optionId, int optionParam) {
            if (!ItemService.isDoThanThanhSetOption(optionId, optionParam)) {
                return null;
            }
            switch (optionParam) {
                case ItemService.DO_THAN_THANH_PARAM_SVK_CON:
                    return "Set SVK con\n(5 m\u00f3n +125% s\u00e1t th\u01b0\u01a1ng Kamejoko)";
                case ItemService.DO_THAN_THANH_PARAM_SON_CON:
                    return "Set S\u01a1n con\n(5 m\u00f3n +50% xuy\u00ean gi\u00e1p, +40% s\u00e1t th\u01b0\u01a1ng Kaioken)";
                case ItemService.DO_THAN_THANH_PARAM_KHANH_CON:
                    return "Set Kh\u00e1nh con\n(5 m\u00f3n x2 Th\u00e1i D\u01b0\u01a1ng H\u1ea1 San, -50% th\u1eddi gian Th\u00f4i Mi\u00ean)";
                case ItemService.DO_THAN_THANH_PARAM_SON_EM:
                    return "Set S\u01a1n em\n(5 m\u00f3n +150% KI)";
                case ItemService.DO_THAN_THANH_PARAM_NGAO_CON:
                    return "Set Ngao con\n(5 m\u00f3n +130% s\u00e1t th\u01b0\u01a1ng Li\u00ean Ho\u00e0n)";
                case ItemService.DO_THAN_THANH_PARAM_NGAO_EM:
                    return "Set Ngao em\n(5 m\u00f3n +150% s\u00e1t th\u01b0\u01a1ng \u0110\u1ebb Tr\u1ee9ng, x2 th\u1eddi gian h\u1ed3i)";
                case ItemService.DO_THAN_THANH_PARAM_BI_CON:
                    return "Set Bi con\n(5 m\u00f3n +120% HP)";
                case ItemService.DO_THAN_THANH_PARAM_CAY_CON:
                    return "Set C\u1ea7y con\n(5 m\u00f3n +50% HP, x2 ph\u1ea1m vi T\u1ef1 S\u00e1t)";
                case ItemService.DO_THAN_THANH_PARAM_BINH_CON:
                    return "Set B\u00ecnh con\n(5 m\u00f3n +60% HP, +30% gi\u00e1p)";
                default:
                    return null;
            }
        }
    }

    public short getId() {
        return template.id;
    }

    public byte getType() {
        return template.type;
    }

    public boolean isSKH() {
        for (ItemOption itemOption : itemOptions) {
            if (itemOption != null && itemOption.optionTemplate != null
                    && ((itemOption.optionTemplate.id >= 127 && itemOption.optionTemplate.id <= 135)
                    || (itemOption.optionTemplate.id == 233
                    && !ItemService.isDoThanThanhSetOption(itemOption.optionTemplate.id, itemOption.param))
                    || (itemOption.optionTemplate.id >= 237 && itemOption.optionTemplate.id <= 248))) {
                return true;
            }
        }
        return false;
    }

    public boolean isVaiTho() {
        if (this.template.id >= 0 && this.template.id <= 65) {
            return true;
        }
        return false;
    }

    public boolean isDTS() {
        if (this.template.id >= 1048 && this.template.id <= 1062) {
            return true;
        }
        return false;
    }

    public boolean isDTL() {
        if (this.template.id >= 555 && this.template.id <= 567) {
            return true;
        }
        return false;
    }

    public boolean isDHD() {
        if (this.template.id >= 650 && this.template.id <= 662) {
            return true;
        }
        return false;
    }

    public boolean isManhTS() {
        if (this.template.id >= 1066 && this.template.id <= 1070) {
            return true;
        }
        return false;
    }

    public boolean isGiayMau() {
        if (this.template.id == 1505) {
            return true;
        }
        return false;
    }

    public boolean isDaNangCap() {
        if (this.template.id >= 1074 && this.template.id <= 1078) {
            return true;
        } else if (this.template.id == -1) {
        }
        return false;
    }

    public int getOptionParam(int id) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate != null && itemOption.optionTemplate.id == id) {
                return itemOption.param;
            }
        }
        return 0;
    }

    public void addOptionParam(int id, int param) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate != null && itemOption.optionTemplate.id == id) {
                itemOption.param += param;
                return;
            }
        }
        this.itemOptions.add(new ItemOption(id, param));
    }

    public ItemOption getOptionDaPhaLe() {
        return switch (template.id) {
            case 20 ->
                new ItemOption(77, 5);
            case 19 ->
                new ItemOption(103, 5);
            case 18 ->
                new ItemOption(80, 5);
            case 17 ->
                new ItemOption(81, 5);
            case 16 ->
                new ItemOption(50, 3);
            case 15 ->
                new ItemOption(94, 2);
            case 14 ->
                new ItemOption(108, 2);

            case 441 ->
                new ItemOption(95, 5);
            case 442 ->
                new ItemOption(96, 5);
            case 443 ->
                new ItemOption(97, 5);
            case 444 ->
                new ItemOption(98, 5);
            case 445 ->
                new ItemOption(99, 5);
            case 446 ->
                new ItemOption(100, 5);
            case 447 ->
                new ItemOption(101, 5);

            case 1416 ->
                new ItemOption(95, 5);
            case 1417 ->
                new ItemOption(96, 5);
            case 1418 ->
                new ItemOption(97, 5);
            case 1419 ->
                new ItemOption(98, 5);
            case 1420 ->
                new ItemOption(99, 5);
            case 1421 ->
                new ItemOption(100, 5);
            case 1422 ->
                new ItemOption(101, 5);

            case 1426 ->
                new ItemOption(95, 5);
            case 1427 ->
                new ItemOption(96, 5);
            case 1428 ->
                new ItemOption(97, 5);
            case 1429 ->
                new ItemOption(98, 5);
            case 1430 ->
                new ItemOption(99, 5);
            case 1431 ->
                new ItemOption(100, 5);
            case 1432 ->
                new ItemOption(101, 5);
            case 1433 ->
                new ItemOption(153, 5);
            case 1434 ->
                new ItemOption(160, 5);
            default ->
                itemOptions == null || itemOptions.isEmpty() ? null : itemOptions.get(0);
        };
    }

    public boolean canPhaLeHoa() {
        return this.template != null && (this.template.type < 5 || ItemService.gI().isTrainArmor(this));
    }

    public Item cloneItem() {
        Item item = new Item();
        item.itemOptions = new ArrayList<>();
        item.template = this.template;
        item.info = this.info;
        item.content = this.content;
        item.quantity = this.quantity;
        item.createTime = this.createTime;
        for (Item.ItemOption io : this.itemOptions) {
            if (io == null) {
                continue;
            }
            item.itemOptions.add(new Item.ItemOption(io));
        }
        return item;
    }

    public String getOptionInfo() {
        StringJoiner optionInfo = new StringJoiner("\n");
        for (ItemOption io : this.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102
                    && io.optionTemplate.id != 107 && io.optionTemplate.id != 218
                    && !ItemService.isDoThanThanhDisplayOption(io.optionTemplate.id)) {
                optionInfo.add(io.getOptionString());
            }
        }
        return optionInfo.toString();
    }

    public boolean isThanLinh() {
        if (this.template.id >= 555 && this.template.id <= 567) {
            return true;
        }
        return false;
    }

    public boolean isThucAn() {
        if (this.template.id >= 663 && this.template.id <= 667) {
            return true;
        }
        return false;
    }

    public String getOptionInfo(Item item) {
        boolean haveOption = false;
        StringJoiner optionInfo = new StringJoiner("\n");
        ItemOption iodpl = item != null ? item.getOptionDaPhaLe() : null;
        if (iodpl == null || iodpl.optionTemplate == null) {
            return getOptionInfo();
        }
        Item itC = this.cloneItem();
        for (ItemOption io : itC.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            if (!haveOption && io.optionTemplate.id == iodpl.optionTemplate.id) {
                io.param += iodpl.param;
                haveOption = true;
            }
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102
                    && io.optionTemplate.id != 107
                    && !ItemService.isDoThanThanhDisplayOption(io.optionTemplate.id)) {
                optionInfo.add(io.getOptionString());
            }
        }
        if (!haveOption) {
            optionInfo.add(iodpl.getOptionString());
        }
        itC.dispose();
        return optionInfo.toString();
    }

    public String getOptionInfoCuongHoa(Item item) {
        StringJoiner optionInfo = new StringJoiner("\n");
        ItemOption iodpl = item != null ? item.getOptionDaPhaLe() : null;
        if (iodpl == null || iodpl.optionTemplate == null) {
            return getOptionInfo();
        }
        Item itC = this.cloneItem();
        for (ItemOption io : itC.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102
                    && io.optionTemplate.id != 107 && io.optionTemplate.id != 218
                    && !ItemService.isDoThanThanhDisplayOption(io.optionTemplate.id)) {
                optionInfo.add(io.getOptionString());
            }
        }
        optionInfo.add(iodpl.getOptionString());
        itC.dispose();
        return optionInfo.toString();
    }

    public void subOptionParam(int id, int param) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate != null && itemOption.optionTemplate.id == id) {
                itemOption.param -= param;
                return;
            }
        }
    }

    public boolean isDaNangCap1() {
        if (this.template.id >= 1074 && this.template.id <= 1078) {
            return true;
        } else if (this.template.id == -1) {
        }
        return false;
    }

    public String typeName() {
        switch (this.template.type) {
            case 0:
                return "Áo";
            case 1:
                return "Quần";
            case 2:
                return "Găng";
            case 3:
                return "Giày";
            case 4:
                return "Rada";
            default:
                return "";
        }
    }

    public String typeHanhTinh() {
        switch (this.template.id) {
            case 1071:
                return "Trái đất";
            case 1084:
                return "Trái đất";
            case 1072:
                return "Namếc";
            case 1085:
                return "Namếc";
            case 1073:
                return "Xayda";
            case 1086:
                return "Xayda";
            default:
                return "";
        }
    }

    public byte typeIdManh() {
        if (!isManhTS()) {
            return -1;
        }
        switch (this.template.id) {
            case 1066:
                return 0;
            case 1067:
                return 1;
            case 1070:
                return 2;
            case 1068:
                return 3;
            case 1069:
                return 4;
            default:
                return -1;
        }
    }

    public String typeNameManh() {
        switch (this.template.id) {
            case 1066:
                return "Áo";
            case 1067:
                return "Quần";
            case 1070:
                return "Găng";
            case 1068:
                return "Giày";
            case 1069:
                return "Nhẫn";
            default:
                return "";
        }
    }

    public String typeDanangcap() {
        switch (this.template.id) {
            case 1074:
                return "cấp 1";
            case 1075:
                return "cấp 2";
            case 1076:
                return "cấp 3";
            case 1077:
                return "cấp 4";
            case 1078:
                return "cấp 5";
            default:
                return "";
        }
    }

    public String typeDaMayman() {
        switch (this.template.id) {
            case 1079:
                return "cấp 1";
            case 1080:
                return "cấp 2";
            case 1081:
                return "cấp 3";
            case 1082:
                return "cấp 4";
            case 1083:
                return "cấp 5";
            default:
                return "";
        }

    }

    public boolean isDaMayMan() {
        return this.template.id >= 1079 && this.template.id <= 1083;
    }

    public boolean isCongThucVip() {
        return (this.template.id >= 1071 && this.template.id <= 1073) || (this.template.id >= 1084 && this.template.id <= 1086);
    }

    public boolean isDoKyGui() {
        return this.template != null && (this.itemOptions.stream().anyMatch(op -> op != null && op.optionTemplate != null && op.optionTemplate.id == 86)
                || this.itemOptions.stream().anyMatch(op -> op != null && op.optionTemplate != null && op.optionTemplate.id == 87)
                || this.template.type == 14 || this.template.type == 15 || this.template.type == 6 || this.template.id >= 14 && this.template.id <= 20);
    }

    public String getInfoItem() {
        String strInfo = "|1|" + template.name + "\n|0|";
        String doThanThanhInfo = getDoThanThanhInfo();
        if (doThanThanhInfo != null) {
            strInfo += doThanThanhInfo + "\n";
        }
        for (ItemOption itemOption : itemOptions) {
            if (itemOption == null) {
                continue;
            }
            if (itemOption.optionTemplate != null
                    && (ItemService.isDoThanThanhSetOption(itemOption.optionTemplate.id, itemOption.param)
                    || ItemService.isDoThanThanhDisplayOption(itemOption.optionTemplate.id))) {
                continue;
            }
            strInfo += itemOption.getOptionString() + "\n";
        }
        strInfo += "|2|" + template.description;
        return strInfo;
    }

    public boolean isHaveOption(int id) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate != null && itemOption.optionTemplate.id == id) {
                return true;
            }
        }
        return false;
    }

    public boolean isSachTuyetKy() {
        return template.id == 1044 || template.id == 1211 || template.id == 1212;
    }

    public boolean isSachTuyetKy2() {
        return template.id >= 1278 && template.id <= 1280;
    }

    public ItemOption getOptionById(int id) {
        if (this.itemOptions == null) {
            return null;
        }
        for (ItemOption option : this.itemOptions) {
            if (option != null && option.optionTemplate != null && option.optionTemplate.id == id) {
                return option;
            }
        }
        return null;
    }

    public boolean hasOption(int optionId, int minParam) {
        for (ItemOption option : this.itemOptions) {
            if (option != null && option.optionTemplate != null
                    && option.optionTemplate.id == optionId && option.param >= minParam) {
                return true;
            }
        }
        return false;
    }

}
