package nro.models.shop_lio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống để nhận thỏi vàng!");
            return;
        }

        // Copy item data trước khi xóa
        short itemId = item.template.id;
        int qty = item.quantity;
        List<ItemOption> ops = new ArrayList<>(item.itemOptions);

        // Xóa đồ TL khỏi hành trang
        InventoryService.gI().subQuantityItemsBag(player, item, 1);

        // Thêm vào shop
        int newId = LioShopManager.gI().getMaxId() + 1;
        LioShopItem shopItem = new LioShopItem(
                newId, itemId, (int) player.id, player.name,
                LioShopManager.PRICE_SELL_OUT, 1, ops, false);
        LioShopManager.gI().listItem.add(shopItem);

        // Trả 25 thỏi vàng cho player
        Item thoiVang = ItemService.gI().createNewItem((short) 457);
        thoiVang.quantity = LioShopManager.PRICE_BUY_IN;
        InventoryService.gI().addItemBag(player, thoiVang);

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

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang không còn chỗ trống!");
            openShop(player);
            return;
        }

        // Kiểm tra đủ thỏi vàng
        Item thoiVang = findThoiVang(player);
        if (thoiVang == null || thoiVang.quantity < LioShopManager.PRICE_SELL_OUT) {
            Service.gI().sendThongBao(player, "Bạn cần " + LioShopManager.PRICE_SELL_OUT + " thỏi vàng để mua!");
            openShop(player);
            return;
        }

        // Trừ thỏi vàng
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, LioShopManager.PRICE_SELL_OUT);

        // Tạo item và trả cho player
        Item newItem = ItemService.gI().createNewItem(shopItem.itemId);
        newItem.itemOptions.clear();
        newItem.itemOptions.addAll(shopItem.options);

        InventoryService.gI().addItemBag(player, newItem);

        // Xóa khỏi shop
        LioShopManager.gI().listItem.remove(shopItem);

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        LioShopManager.gI().save();

        Service.gI().sendThongBao(player, "Mua thành công " + newItem.template.name + "!");
        openShop(player);
    }

    /**
     * Mở giao diện shop (sử dụng protocol ConsignShop -100)
     */
    public void openShop(Player player) {
        if (LioShopManager.gI() == null) {
            Service.gI().sendThongBao(player, "Shop đang bảo trì, vui lòng quay lại sau!");
            return;
        }
        List<LioShopItem> available = LioShopManager.gI().listItem.stream()
                .filter(it -> it != null && !it.isSold)
                .collect(Collectors.toList());

        Message msg = null;
        try {
            msg = new Message(-100);
            msg.writer().writeByte(0); // tab index
            msg.writer().writeByte(1); // 1 tab
            // Tab "Đồ Thần Linh"
            msg.writer().writeUTF("Đồ Thần Linh");
            msg.writer().writeByte(1); // max page
            msg.writer().writeByte(Math.min(available.size(), 20)); // items count

            int count = 0;
            for (LioShopItem shopItem : available) {
                if (count >= 20) break;

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
                if (player.getSession().version >= 222) {
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
                count++;
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
    private Item findThoiVang(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457) {
                return item;
            }
        }
        return null;
    }
}
