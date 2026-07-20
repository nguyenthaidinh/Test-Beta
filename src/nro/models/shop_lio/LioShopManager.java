package nro.models.shop_lio;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nro.models.data.LocalManager;
import org.json.simple.JSONValue;

/**
 * Quản lý data shop Lio Đẹp Trai
 * Persist vào DB table shop_lio
 *
 * @author Lio
 */
public class LioShopManager {

    private static LioShopManager instance;

    public static LioShopManager gI() {
        if (instance == null) {
            instance = new LioShopManager();
        }
        return instance;
    }

    // Giới hạn tối đa 200 món trong shop
    public static final int MAX_ITEMS = 200;

    // Giá mua vào (player bán cho NPC) = 50 thỏi vàng
    public static final int PRICE_BUY_IN = 50;

    // Giá bán ra (player mua từ NPC) = 100 thỏi vàng
    public static final int PRICE_SELL_OUT = 100;

    public List<LioShopItem> listItem = new ArrayList<>();

    public int getMaxId() {
        try {
            List<Integer> ids = new ArrayList<>();
            for (LioShopItem it : listItem) {
                if (it != null) {
                    ids.add(it.id);
                }
            }
            if (ids.isEmpty()) {
                return 0;
            }
            return Collections.max(ids);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getAvailableCount() {
        int count = 0;
        for (LioShopItem it : listItem) {
            if (it != null && !it.isSold) {
                count++;
            }
        }
        return count;
    }

    public synchronized void resetForMaintenance() {
        try (Connection con = LocalManager.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate("DELETE FROM `shop_lio`");
            }
            listItem.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (Connection con = LocalManager.getConnection()) {
            Statement s = con.createStatement();
            s.execute("DELETE FROM shop_lio");
            for (LioShopItem it : this.listItem) {
                if (it != null && !it.isSold) {
                    s.execute(String.format(
                            "INSERT INTO `shop_lio`(`id`, `player_id`, `seller_name`, `item_id`, `price`, `quantity`, `itemOption`, `isSold`) "
                            + "VALUES ('%s','%s','%s','%s','%s','%s','%s','%s')",
                            it.id, it.sellerPlayerId, it.sellerName,
                            it.itemId, it.priceThoiVang, it.quantity,
                            JSONValue.toJSONString(it.options).equals("null") ? "[]" : JSONValue.toJSONString(it.options),
                            it.isSold ? 1 : 0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
