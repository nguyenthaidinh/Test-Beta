package nro.models.shop_lio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private static LioShopService instance;

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
                msg.writer().writeByte(1); // 1 tab
                msg.writer().writeUTF("Đồ Thần Linh");
                msg.writer().writeByte(totalPage); // max page
            } else {
                msg.writer().writeByte(0); // tab index
                msg.writer().writeByte(totalPage); // max page
                msg.writer().writeByte(page);
            }
            msg.writer().writeByte(itemsSend.size()); // items count
            for (LioShopItem shopItem : itemsSend) {
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
