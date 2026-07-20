package nro.models.shop_lio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import nro.models.utils.Util;
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
 * - Bán: Player bán đồ TL → nhận 50 thỏi vàng, đồ vào shop
 * - Mua: Player mua đồ TL từ shop → trả 100 thỏi vàng
 *
 * @author Lio
 */
public class LioShopService {

    private static final int MAX_STACK_QUANTITY = 100_000_000;
    public static final int DAILY_SELL_LIMIT = 80;
    private static final int SHOP_PAGE_SIZE = 20;
    private static final int SHOP_TAB_COUNT = LioShopManager.MAX_ITEMS / SHOP_PAGE_SIZE;
    public static final String SHOP_TAG = "LIO_DEP_TRAI_SHOP";
    public static final String BE_TAC_SHOP_TAG = "LIO_BE_TAC_SHOP";
    private static final int PRICE_BE_TAC = 150;
    private static final int PRICE_BE_TAC_SET = 800;
    private static final int BE_TAC_SET_ITEM_COUNT = 5;
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
    private final Map<Long, DailySellCounter> dailySellCounters = new ConcurrentHashMap<>();

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
     * Nhận 50 thỏi vàng, đồ được đưa vào shop
     */
    public synchronized void sellItem(Player player, Item item) {
        synchronized (player.inventory) {
            if (!isDoThanLinh(item)) {
                Service.gI().sendThongBao(player, "Chỉ có thể bán đồ Thần Linh!");
                return;
            }

            int bagIndex = InventoryService.gI().getIndexBag(player, item);
            if (bagIndex < 0 || item.quantity <= 0) {
                Service.gI().sendThongBao(player, "Vật phẩm không còn trong hành trang!");
                return;
            }

            if (LioShopManager.gI().getAvailableCount() >= LioShopManager.MAX_ITEMS) {
                Service.gI().sendThongBao(player, "Shop đã đầy (" + LioShopManager.MAX_ITEMS + " món), vui lòng quay lại sau!");
                return;
            }

            int dailySellCount = getDailySellCount(player.id);
            if (dailySellCount >= DAILY_SELL_LIMIT) {
                Service.gI().sendThongBao(player, "Hôm nay bạn đã bán đủ " + DAILY_SELL_LIMIT + " món, vui lòng quay lại ngày mai!");
                return;
            }

            boolean canReceiveThoiVang = hasStackableThoiVang(player, LioShopManager.PRICE_BUY_IN)
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
            if (!removeOneItemFromBag(player, item, bagIndex)) {
                Service.gI().sendThongBao(player, "Vật phẩm không còn trong hành trang!");
                return;
            }

            // Trả 100 thỏi vàng cho player
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
            increaseDailySellCount(player.id);

            Service.gI().sendThongBao(player, "Bán thành công! Nhận được " + LioShopManager.PRICE_BUY_IN + " thỏi vàng.");
        }
    }

    /**
     * Player mua đồ Thần Linh từ shop
     * Trả 100 thỏi vàng
     */
    public synchronized void buyItem(Player player, int shopItemId) {
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

    public synchronized void buyBeTacItem(Player player, int shopItemId) {
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

    public synchronized void buyBeTacSet(Player player, int planetIndex) {
        if (planetIndex < 0 || planetIndex > 2
                || BE_TAC_ITEMS[planetIndex].length != BE_TAC_SET_ITEM_COUNT) {
            Service.gI().sendThongBao(player, "Hành tinh không hợp lệ!");
            return;
        }

        synchronized (player.inventory) {
            buyBeTacSetLocked(player, planetIndex);
        }
    }

    private void buyBeTacSetLocked(Player player, int planetIndex) {
        if (getTotalThoiVang(player) < PRICE_BE_TAC_SET) {
            Service.gI().sendThongBao(player,
                    "Bạn cần " + PRICE_BE_TAC_SET + " thỏi vàng để mua!");
            return;
        }

        int slotsFreedByPayment = countGoldBarSlotsFreed(player, PRICE_BE_TAC_SET);
        int availableSlots = InventoryService.gI().getCountEmptyBag(player) + slotsFreedByPayment;
        if (availableSlots < BE_TAC_SET_ITEM_COUNT) {
            Service.gI().sendThongBao(player, "Cần ít nhất "
                    + (BE_TAC_SET_ITEM_COUNT - slotsFreedByPayment)
                    + " ô hành trang trống để nhận đủ Set Thần Linh!");
            return;
        }

        List<Item> setItems = new ArrayList<>();
        for (int slot = 0; slot < BE_TAC_SET_ITEM_COUNT; slot++) {
            Item item = createBeTacItemForBuy(getBeTacShopItemId(planetIndex, slot));
            if (item == null || item.template == null) {
                Service.gI().sendThongBao(player, "Không thể tạo đủ Set Thần Linh, giao dịch đã hủy.");
                return;
            }
            setItems.add(item);
        }

        subtractThoiVang(player, PRICE_BE_TAC_SET);
        List<Integer> emptySlots = getEmptyBagSlots(player, BE_TAC_SET_ITEM_COUNT);
        if (emptySlots.size() < BE_TAC_SET_ITEM_COUNT) {
            Item refund = ItemService.gI().createNewItem((short) ConstItem.THOI_VANG, PRICE_BE_TAC_SET);
            InventoryService.gI().addItemBag(player, refund);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ, đã hoàn lại thỏi vàng.");
            return;
        }

        for (int i = 0; i < BE_TAC_SET_ITEM_COUNT; i++) {
            player.inventory.itemsBag.set(emptySlots.get(i), setItems.get(i));
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Mua nhanh thành công Set Thần Linh "
                + BE_TAC_TAB_NAMES[planetIndex] + " gồm đủ 5 món với giá "
                + PRICE_BE_TAC_SET + " thỏi vàng!");
    }

    private void openShop(Player player, int page, boolean firstOpen) {
        if (LioShopManager.gI() == null) {
            Service.gI().sendThongBao(player, "Shop đang bảo trì, vui lòng quay lại sau!");
            return;
        }
        List<LioShopItem> available = LioShopManager.gI().listItem.stream()
                .filter(it -> it != null && !it.isSold)
                .collect(Collectors.toList());
        int totalPage = Math.max(1, (available.size() + SHOP_PAGE_SIZE - 1) / SHOP_PAGE_SIZE);
        if (page >= totalPage) {
            page = totalPage - 1;
        }
        int from = Math.min(page * SHOP_PAGE_SIZE, available.size());
        int to = Math.min(from + SHOP_PAGE_SIZE, available.size());
        List<LioShopItem> itemsSend = available.subList(from, to);

        Message msg = null;
        try {
            player.idMark.setTagNameShop(SHOP_TAG);
            msg = new Message(firstOpen ? -44 : -100);
            if (firstOpen) {
                msg.writer().writeByte(2);
                msg.writer().writeByte(SHOP_TAB_COUNT);
                for (int tab = 0; tab < SHOP_TAB_COUNT; tab++) {
                    msg.writer().writeUTF(tab < totalPage ? "Trang " + (tab + 1) : "");
                    msg.writer().writeByte(1); // mỗi tab là một trang
                    int tabFrom = Math.min(tab * SHOP_PAGE_SIZE, available.size());
                    int tabTo = Math.min(tabFrom + SHOP_PAGE_SIZE, available.size());
                    int tabCount = tab < totalPage ? tabTo - tabFrom : 0;
                    msg.writer().writeByte(tabCount);
                    for (int i = tabFrom; i < tabTo && tab < totalPage; i++) {
                        writeShopItem(msg, player, available.get(i), true);
                    }
                }
            } else {
                msg.writer().writeByte(Math.min(page, SHOP_TAB_COUNT - 1)); // tab index
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

    private boolean hasStackableThoiVang(Player player, int quantity) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == ConstItem.THOI_VANG
                    && item.quantity <= MAX_STACK_QUANTITY - quantity) {
                return true;
            }
        }
        return false;
    }

    private int getDailySellCount(long playerId) {
        DailySellCounter counter = dailySellCounters.get(playerId);
        if (counter == null) {
            return 0;
        }
        if (Util.isAfterMidnight(counter.lastTime)) {
            counter.count = 0;
            counter.lastTime = System.currentTimeMillis();
        }
        return counter.count;
    }

    private void increaseDailySellCount(long playerId) {
        DailySellCounter counter = dailySellCounters.computeIfAbsent(playerId, id -> new DailySellCounter());
        if (Util.isAfterMidnight(counter.lastTime)) {
            counter.count = 0;
        }
        counter.count++;
        counter.lastTime = System.currentTimeMillis();
    }

    private int getTotalThoiVang(Player player) {
        long total = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.isNotNullItem()
                    && item.template.id == ConstItem.THOI_VANG && item.quantity > 0) {
                total += item.quantity;
                if (total >= Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        return (int) total;
    }

    private int countGoldBarSlotsFreed(Player player, int quantity) {
        int remaining = quantity;
        int freedSlots = 0;
        for (Item item : player.inventory.itemsBag) {
            if (remaining <= 0) {
                break;
            }
            if (item != null && item.isNotNullItem()
                    && item.template.id == ConstItem.THOI_VANG && item.quantity > 0) {
                if (item.quantity <= remaining) {
                    remaining -= item.quantity;
                    freedSlots++;
                } else {
                    remaining = 0;
                }
            }
        }
        return freedSlots;
    }

    private void subtractThoiVang(Player player, int quantity) {
        int remaining = quantity;
        for (int i = 0; i < player.inventory.itemsBag.size() && remaining > 0; i++) {
            Item item = player.inventory.itemsBag.get(i);
            if (item == null || !item.isNotNullItem()
                    || item.template.id != ConstItem.THOI_VANG || item.quantity <= 0) {
                continue;
            }
            int subtract = Math.min(item.quantity, remaining);
            remaining -= subtract;
            InventoryService.gI().subQuantityItemsBag(player, item, subtract);
        }
    }

    private List<Integer> getEmptyBagSlots(Player player, int maxSlots) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < player.inventory.itemsBag.size() && slots.size() < maxSlots; i++) {
            Item item = player.inventory.itemsBag.get(i);
            if (item == null || !item.isNotNullItem()) {
                slots.add(i);
            }
        }
        return slots;
    }

    private boolean removeOneItemFromBag(Player player, Item item, int bagIndex) {
        if (bagIndex < 0 || bagIndex >= player.inventory.itemsBag.size()
                || player.inventory.itemsBag.get(bagIndex) != item || item.quantity <= 0) {
            return false;
        }
        if (item.quantity <= 1) {
            player.inventory.itemsBag.set(bagIndex, ItemService.gI().createItemNull());
            item.dispose();
        } else {
            item.quantity--;
        }
        return true;
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

    private static class DailySellCounter {

        private int count;
        private long lastTime;
    }
}
