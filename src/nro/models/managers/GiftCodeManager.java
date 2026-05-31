package nro.models.managers;

import nro.models.data.LocalManager;
import nro.models.player_system.GiftCode;
import nro.models.player.Player;
import nro.models.item.Item;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import nro.models.services.InventoryService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GiftCodeManager {

    public String name;
    public final ArrayList<GiftCode> listGiftCode = new ArrayList<>();

    private static GiftCodeManager instance;

    public static GiftCodeManager gI() {
        if (instance == null) {
            instance = new GiftCodeManager();
        }
        return instance;
    }

    /**
     * Reload tất cả giftcode từ database mà không cần restart server.
     * Gọi bằng lệnh admin: reloadgc
     */
    public int reloadGiftCodes() {
        try {
            Connection con = LocalManager.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode");
            ResultSet rs = ps.executeQuery();
            ArrayList<GiftCode> newList = new ArrayList<>();
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                giftcode.code = rs.getString("code");
                giftcode.id = rs.getInt("id");
                giftcode.countLeft = rs.getInt("count_left");
                if (giftcode.countLeft == -1) {
                    giftcode.countLeft = 999999999;
                }
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);
                        int id = Integer.parseInt(jsonObj.get("id").toString());
                        int quantity = Integer.parseInt(jsonObj.get("quantity").toString());
                        JSONArray option = (JSONArray) jsonObj.get("options");
                        ArrayList<Item.ItemOption> optionList = new ArrayList<>();
                        if (option != null) {
                            for (int u = 0; u < option.size(); u++) {
                                JSONObject jsonobject = (JSONObject) option.get(u);
                                int optionId = Integer.parseInt(jsonobject.get("id").toString());
                                int param = Integer.parseInt(jsonobject.get("param").toString());
                                optionList.add(new Item.ItemOption(optionId, param));
                            }
                        }
                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }
                newList.add(giftcode);
            }
            rs.close();
            ps.close();
            // Thay thế list cũ
            listGiftCode.clear();
            listGiftCode.addAll(newList);
            Logger.success(Logger.RED + "Reloaded giftcode (" + listGiftCode.size() + ")\n");
            return listGiftCode.size();
        } catch (Exception e) {
            Logger.logException(GiftCodeManager.class, e, "Error reloading giftcode");
            return -1;
        }
    }

    public GiftCode checkUseGiftCode(Player player, String code) {
        for (GiftCode giftCode : listGiftCode) {
            if (giftCode.code.equals(code)) {
                if (giftCode.countLeft <= 0) {
                    Service.gI().sendThongBaoOK(player, "Giftcode đã hết");
                    return null;
                } else if (giftCode.isUsedGiftCode(player)) {
                    Service.gI().sendThongBaoOK(player, "Lio đẹp trai!");
                    return null;
                }
                if (InventoryService.gI().getCountEmptyBag(player) < giftCode.detail.size()) {
                    Service.gI().sendThongBaoOK(player,
                            "Cần tối thiểu " + giftCode.detail.size() + " ô hành trang trống");
                    return null;
                }
                giftCode.countLeft -= 1;
                player.giftCode.add(code);
                updateGiftCode(giftCode);
                return giftCode;
            }
        }
        return null;
    }

    public void updateGiftCode(GiftCode giftcode) {
        try {
            LocalManager.executeUpdate("update giftcode set count_left = ? where id = ?", giftcode.countLeft,
                    giftcode.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkInfomationGiftCode(Player p) {
        StringBuilder sb = new StringBuilder();
        for (GiftCode giftCode : listGiftCode) {
            sb.append("Code: ").append(giftCode.code).append(", Số lượng còn lại: ").append(giftCode.countLeft)
                    .append("\b")
                    .append("Ngày tạo: ")
                    .append(giftCode.datecreate).append(", Ngày hết hạn: ").append(giftCode.dateexpired)
                    .append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        NpcService.gI().createTutorial(p, 5073, sb.toString());
    }

}