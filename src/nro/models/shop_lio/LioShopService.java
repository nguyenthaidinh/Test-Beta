package nro.models.shop_lio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nro.models.consts.ConstItem;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;

/**
 * Service xử lý logic mua bán đồ Thần Linh tại NPC Lio Đẹp Trai
 * - Bán: Player bán đồ TL → nhận 25 thỏi vàng, đồ vào shop
 * - Mua: Player mua đồ TL từ shop → trả 100 thỏi vàng
 *
 * @author Lio
 */
public class LioShopService {

    private static final int MAX_STACK_QUANTITY = 100_000_000;
    public static final String SHOP_TAG = "LIO_DEP_TRAI_SHOP";
    public static final String BE_TAC_SHOP_TAG = "LIO_BE_TAC_SHOP";
    private static final int PRICE_BE_TAC = 120;
    private static final int BE_TAC_ITEM_ID_BASE = 30000;
    private static final String[] BE_TAC_TAB_NAMES = {"Trái Đất", "Namek", "Xayda", "", ""};
    private static final short[][] BE_TAC_ITEMS = {
        {555, 556, 562, 563, 561},
        {557, 558, 564, 565, 561},
        {559, 560, 566, 567, 561},
        {},
        {}
    };
    private static LioShopService instance;
    private final Map<Integer, Item> beTacShopItems = new HashMap<>();

    public static LioShopService gI() {
        if (instance == null) {
            instance = new LioShopService();
        }
        return instance;
    }

    /**
     * Kiểm tra item có phải đồ Thần Linh (ID 555-567) không
     */
    public boolean isDoThanLinh(Item item) {
        if (item == null || !item.isNotNullItem()) {
            return false;
        }
        return item.template.id >= 555 && item.template.id <= 567;
    }

    /**
     * Player bán đồ Thần Linh cho NPC
     * Nhận 25 thỏi vàng, đồ được đưa vào shop
     */
    public void sellItem(Player player, Item item) {
        if (!isDoThanLinh(item)) {
            Service.gI().sendThongBao(player, "Chỉ có thể bán đồ Thần Linh!");
            return;
        }

        if (LioShopManager.gI().getAvailableCount() >= LioShopManager.MAX_ITEMS) {
            Service.gI().sendThongBao(player, "Shop đã đầy (" + LioShopManager.MAX_ITEMS + " món), vui lòng quay lại sau!");
            return;
        }

        boolean canReceiveThoiVang = hasStackableThoiVang(player)
                || item.quantity <= 1
                || InventoryService.gI().getCountEmptyBag(player) > 0;
        if (!canReceiveThoiVang) {
            Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống để nhận thỏi vàng!");
            return;
        }

        // Copy item data trước khi xóa
        short itemId = item.template.id;
        List<ItemOption> ops = copyOptions(item.itemOptions);

        // Xóa đồ TL khỏi hành trang
        InventoryService.gI().subQuantityItemsBag(player, item, 1);

        // Trả 25 thỏi vàng cho player
        Item thoiVang = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, LioShopManager.PRICE_BUY_IN);
        if (!InventoryService.gI().addItemBag(player, thoiVang)) {
            Item rollbackItem = ItemService.gI().createNewItem(itemId);
            rollbackItem.itemOptions.clear();
            rollbackItem.itemOptions.addAll(copyOptions(ops));
            InventoryService.gI().addItemBag(player, rollbackItem);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Không thể trả thỏi vàng, đã hoàn lại đồ Thần Linh.");
            return;
        }

        // Thêm vào shop sau khi trả thưởng thành công
        int newId = LioShopManager.gI().getMaxId() + 1;
        LioShopItem shopItem = new LioShopItem(
                newId, itemId, (int) player.id, player.name,
                LioShopManager.PRICE_SELL_OUT, 1, ops, false);
        LioShopManager.gI().listItem.add(shopItem);

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        LioShopManager.gI().save();

        Service.gI().sendThongBao(player, "Bán thành công! Nhận được " + LioShopManager.PRICE_BUY_IN + " thỏi vàng.");
    }

    /**
     * Player mua đồ Thần Linh từ shop
     * Trả 100 thỏi vàng
     */
    public void buyItem(Player player, int shopItemId) {
        LioShopItem shopItem = null;
        for (LioShopItem it : LioShopManager.gI().listItem) {
            if (it != null && it.id == shopItemId && !it.isSold) {
                shopItem = it;
                break;
            }
        }

        if (shopItem == null) {
            Service.gI().sendThongBao(player, "Vật phẩm không tồn tại hoặc đã được bán!");
            openShop(player);
            return;
        }

        // Kiểm tra đủ thỏi vàng
        Item thoiVang = findThoiVang(player, LioShopManager.PRICE_SELL_OUT);
        if (thoiVang == null) {
            Service.gI().sendThongBao(player, "Bạn cần " + LioShopManager.PRICE_SELL_OUT + " thỏi vàng để mua!");
            openShop(player);
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0 && thoiVang.quantity > LioShopManager.PRICE_SELL_OUT) {
            Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống!");
            openShop(player);
            return;
        }

        // Trừ thỏi vàng
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, LioShopManager.PRICE_SELL_OUT);

        // Tạo item và trả cho player
        Item newItem = ItemService.gI().createNewItem(shopItem.itemId);
        newItem.itemOptions.clear();
        newItem.itemOptions.addAll(copyOptions(shopItem.options));

        if (!InventoryService.gI().addItemBag(player, newItem)) {
            Item refund = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, LioShopManager.PRICE_SELL_OUT);
            InventoryService.gI().addItemBag(player, refund);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống!");
            openShop(player);
            return;
        }

        // Xóa khỏi shop
        LioShopManager.gI().listItem.remove(shopItem);

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        LioShopManager.gI().save();

        Service.gI().sendThongBao(player, "Mua thành công " + newItem.template.name + "!");
        openShop(player);
    }

    /**
     * Mở giao diện shop (mở lần đầu bằng -44, chuyển trang bằng -100)
     */
    public void openShop(Player player) {
        openShop(player, 0, true);
    }

    public void openShopPage(Player player, int page) {
        openShop(player, Math.max(0, page), false);
    }

    public boolean hasAvailableItem(int shopItemId) {
        for (LioShopItem it : LioShopManager.gI().listItem) {
            if (it != null && it.id == shopItemId && !it.isSold) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBeTacItem(int shopItemId) {
        return getBeTacItemId(shopItemId) != -1;
    }

    public void buyBeTacItem(Player player, int shopItemId) {
        short itemId = getBeTacItemId(shopItemId);
        if (itemId == -1) {
            Service.gI().sendThongBao(player, "Vat pham khong ton tai!");
            openBeTacShop(player);
            return;
        }

        Item thoiVang = findThoiVang(player, PRICE_BE_TAC);
        if (thoiVang == null) {
            Service.gI().sendThongBao(player, "Ban can " + PRICE_BE_TAC + " thoi vang de mua!");
            openBeTacShop(player);
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0 && thoiVang.quantity > PRICE_BE_TAC) {
            Service.gI().sendThongBao(player, "Hanh trang khong con cho trong!");
            openBeTacShop(player);
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, thoiVang, PRICE_BE_TAC);
        Item item = createBeTacItemForBuy(shopItemId);
        if (item == null || !InventoryService.gI().addItemBag(player, item)) {
            Item refund = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, PRICE_BE_TAC);
            InventoryService.gI().addItemBag(player, refund);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            Service.gI().sendThongBao(player, "Khong the mua vat pham, da hoan lai thoi vang.");
            openBeTacShop(player);
            return;
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Mua thanh cong " + item.template.name + "!");
        openBeTacShop(player);
    }

    private void openShop(Player player, int page, boolean firstOpen) {
        if (LioShopManager.gI() == null) {
            Service.gI().sendThongBao(player, "Shop đang bảo trì, vui lòng quay lại sau!");
            return;
        }
        List<LioShopItem> available = LioShopManager.gI().listItem.stream()
                .filter(it -> it != null && !it.isSold)
                .collect(Collectors.toList());
        int totalPage = Math.max(1, (available.size() + 19) / 20);
        if (page >= totalPage) {
            page = totalPage - 1;
        }
        int from = Math.min(page * 20, available.size());
        int to = Math.min(from + 20, available.size());
        List<LioShopItem> itemsSend = available.subList(from, to);

        Message msg = null;
        try {
            player.idMark.setTagNameShop(SHOP_TAG);
            msg = new Message(firstOpen ? -44 : -100);
            if (firstOpen) {
                msg.writer().writeByte(2);
                msg.writer().writeByte(5); // Consign UI expects 5 tabs
                for (int tab = 0; tab < 5; tab++) {
                    msg.writer().writeUTF(tab < totalPage ? "Trang " + (tab + 1) : "");
                    msg.writer().writeByte(1); // mỗi tab là một trang
                    int tabFrom = Math.min(tab * 20, available.size());
                    int tabTo = Math.min(tabFrom + 20, available.size());
                    int tabCount = tab < totalPage ? tabTo - tabFrom : 0;
                    msg.writer().writeByte(tabCount);
                    for (int i = tabFrom; i < tabTo && tab < totalPage; i++) {
                        writeShopItem(msg, player, available.get(i), true);
                    }
                }
            } else {
                msg.writer().writeByte(Math.min(page, 4)); // tab index
                msg.writer().writeByte(1); // max page trong tab
                msg.writer().writeByte(0); // page trong tab
                msg.writer().writeByte(itemsSend.size()); // items count
                for (LioShopItem shopItem : itemsSend) {
                    writeShopItem(msg, player, shopItem, false);
                }
            }

            player.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void openBeTacShop(Player player) {
        openBeTacShop(player, 0, true);
    }

    public void openBeTacShopPage(Player player, int tab) {
        openBeTacShop(player, Math.max(0, Math.min(tab, BE_TAC_ITEMS.length - 1)), false);
    }

    private void openBeTacShop(Player player, int tabIndex, boolean firstOpen) {
        Message msg = null;
        try {
            player.idMark.setTagNameShop(BE_TAC_SHOP_TAG);
            msg = new Message(firstOpen ? -44 : -100);
            if (firstOpen) {
                msg.writer().writeByte(2);
                msg.writer().writeByte(5);
                for (int tab = 0; tab < 5; tab++) {
                    writeBeTacTab(msg, player, tab, true);
                }
            } else {
                msg.writer().writeByte(tabIndex);
                msg.writer().writeByte(1);
                msg.writer().writeByte(0);
                msg.writer().writeByte(BE_TAC_ITEMS[tabIndex].length);
                for (int slot = 0; slot < BE_TAC_ITEMS[tabIndex].length; slot++) {
                    writeBeTacShopItem(msg, player, tabIndex, slot, false);
                }
            }
            player.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void writeBeTacTab(Message msg, Player player, int tab, boolean firstOpen) throws IOException {
        msg.writer().writeUTF(BE_TAC_TAB_NAMES[tab]);
        msg.writer().writeByte(1);
        msg.writer().writeByte(BE_TAC_ITEMS[tab].length);
        for (int slot = 0; slot < BE_TAC_ITEMS[tab].length; slot++) {
            writeBeTacShopItem(msg, player, tab, slot, firstOpen);
        }
    }

    private void writeBeTacShopItem(Message msg, Player player, int tab, int slot, boolean firstOpen) throws IOException {
        Item it = getOrCreateBeTacShopItem(tab, slot);
        if (it == null) {
            return;
        }
        msg.writer().writeShort(it.template.id);
        msg.writer().writeShort(getBeTacShopItemId(tab, slot));
        msg.writer().writeInt(PRICE_BE_TAC);
        msg.writer().writeInt(-1);
        msg.writer().writeByte(0);
        if (firstOpen || player.getSession().version >= 222) {
            msg.writer().writeInt(1);
        } else {
            msg.writer().writeByte(1);
        }
        msg.writer().writeByte(0);
        msg.writer().writeByte(it.itemOptions.size());
        for (int i = 0; i < it.itemOptions.size(); i++) {
            msg.writer().writeByte(it.itemOptions.get(i).optionTemplate.id);
            msg.writer().writeShort(it.itemOptions.get(i).param);
        }
        msg.writer().writeByte(0);
        if (firstOpen) {
            msg.writer().writeByte(0);
        }
    }

    private Item getOrCreateBeTacShopItem(int tab, int slot) {
        int shopItemId = getBeTacShopItemId(tab, slot);
        synchronized (beTacShopItems) {
            Item item = beTacShopItems.get(shopItemId);
            if (item == null) {
                item = ItemService.gI().createDoThanLinh(BE_TAC_ITEMS[tab][slot]);
                if (item != null) {
                    beTacShopItems.put(shopItemId, item);
                }
            }
            return item;
        }
    }

    private Item createBeTacItemForBuy(int shopItemId) {
        int raw = shopItemId - BE_TAC_ITEM_ID_BASE;
        int tab = raw / 10;
        int slot = raw % 10;
        if (raw < 0 || tab < 0 || tab >= BE_TAC_ITEMS.length || slot < 0 || slot >= BE_TAC_ITEMS[tab].length) {
            return null;
        }
        Item source = getOrCreateBeTacShopItem(tab, slot);
        if (source == null || source.template == null) {
            return null;
        }
        Item item = ItemService.gI().createNewItem(source.template.id);
        item.itemOptions.clear();
        item.itemOptions.addAll(copyOptions(source.itemOptions));
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    private int getBeTacShopItemId(int tab, int slot) {
        return BE_TAC_ITEM_ID_BASE + tab * 10 + slot;
    }

    private short getBeTacItemId(int shopItemId) {
        int raw = shopItemId - BE_TAC_ITEM_ID_BASE;
        int tab = raw / 10;
        int slot = raw % 10;
        if (raw < 0 || tab < 0 || tab >= BE_TAC_ITEMS.length || slot < 0 || slot >= BE_TAC_ITEMS[tab].length) {
            return -1;
        }
        return BE_TAC_ITEMS[tab][slot];
    }

    private void writeShopItem(Message msg, Player player, LioShopItem shopItem, boolean firstOpen) throws IOException {
        Item it = ItemService.gI().createNewItem(shopItem.itemId);
        it.itemOptions.clear();
        if (shopItem.options.isEmpty()) {
            it.itemOptions.add(new ItemOption(73, 0));
        } else {
            it.itemOptions.addAll(shopItem.options);
        }

        msg.writer().writeShort(it.template.id);
        msg.writer().writeShort(shopItem.id);
        msg.writer().writeInt(shopItem.priceThoiVang); // gold (thỏi vàng)
        msg.writer().writeInt(-1); // gem
        msg.writer().writeByte(0); // buy type
        if (firstOpen || player.getSession().version >= 222) {
            msg.writer().writeInt(shopItem.quantity);
        } else {
            msg.writer().writeByte(shopItem.quantity);
        }
        msg.writer().writeByte(0); // isMe
        msg.writer().writeByte(it.itemOptions.size());
        for (int a = 0; a < it.itemOptions.size(); a++) {
            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
            msg.writer().writeShort(it.itemOptions.get(a).param);
        }
        msg.writer().writeByte(0);
        if (firstOpen) {
            msg.writer().writeByte(0);
        }
    }

    /**
     * Tìm thỏi vàng (ID 457) trong hành trang
     */
    private Item findThoiVang(Player player, int minQuantity) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == ConstItem.THOI_VANG && item.quantity >= minQuantity) {
                return item;
            }
        }
        return null;
    }

    private boolean hasStackableThoiVang(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == ConstItem.THOI_VANG
                    && item.quantity < MAX_STACK_QUANTITY) {
                return true;
            }
        }
        return false;
    }

    private List<ItemOption> copyOptions(List<ItemOption> options) {
        List<ItemOption> copies = new ArrayList<>();
        if (options == null) {
            return copies;
        }
        for (ItemOption option : options) {
            if (option != null && option.optionTemplate != null) {
                copies.add(new ItemOption(option));
            }
        }
        return copies;
    }
}
