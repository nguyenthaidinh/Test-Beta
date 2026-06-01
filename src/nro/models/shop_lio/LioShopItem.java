package nro.models.shop_lio;

import java.util.ArrayList;
import java.util.List;
import nro.models.item.Item.ItemOption;

/**
 * Data model cho 1 item trong shop Lio Đẹp Trai
 * Đồ Thần Linh được người chơi bán vào shop
 *
 * @author Lio
 */
public class LioShopItem {

    public int id;
    public short itemId;         // template ID đồ Thần Linh (555-567)
    public int sellerPlayerId;   // ID người bán
    public String sellerName;    // Tên người bán
    public int priceThoiVang;    // Giá bán ra = 100 TV
    public int quantity;
    public List<ItemOption> options = new ArrayList<>();
    public boolean isSold;

    public LioShopItem() {
    }

    public LioShopItem(int id, short itemId, int sellerPlayerId, String sellerName,
                       int priceThoiVang, int quantity, List<ItemOption> options, boolean isSold) {
        this.id = id;
        this.itemId = itemId;
        this.sellerPlayerId = sellerPlayerId;
        this.sellerName = sellerName;
        this.priceThoiVang = priceThoiVang;
        this.quantity = quantity;
        this.options = options;
        this.isSold = isSold;
    }
}
