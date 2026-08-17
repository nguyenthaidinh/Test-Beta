package nro.models.services;

import nro.models.data.LocalManager;
import nro.models.data.LocalResultSet;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.utils.Logger;

public class GoldBarSpendService {

    private static GoldBarSpendService instance;
    private static boolean loggedUpdateError;
    private static boolean loggedReadError;

    public static GoldBarSpendService gI() {
        if (instance == null) {
            instance = new GoldBarSpendService();
        }
        return instance;
    }

    public void addPoint(Player player, long spentGoldBars) {
        if (player == null || spentGoldBars <= 0) {
            return;
        }
        try {
            LocalManager.executeUpdate(
                    "INSERT INTO gold_bar_spend_top (player_id, point) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE point = point + VALUES(point)",
                    player.id,
                    spentGoldBars);
            Manager.isTopGoldBarSpendChanged = true;
        } catch (Exception e) {
            if (!loggedUpdateError) {
                loggedUpdateError = true;
                Logger.logException(GoldBarSpendService.class, e,
                        "Khong the cap nhat gold_bar_spend_top. Hay chay sql/gold_bar_spend_top.sql.");
            }
        }
    }

    public String getPoint(long playerId) {
        LocalResultSet rs = null;
        try {
            rs = LocalManager.executeQuery(
                    "SELECT point FROM gold_bar_spend_top WHERE player_id = ? LIMIT 1",
                    playerId);
            if (rs.next()) {
                String point = rs.getString("point");
                return point == null || point.isEmpty() ? "0" : point;
            }
        } catch (Exception e) {
            if (!loggedReadError) {
                loggedReadError = true;
                Logger.logException(GoldBarSpendService.class, e,
                        "Khong the doc diem tu gold_bar_spend_top.");
            }
            return null;
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return "0";
    }
}
