package nro.models.services;

import nro.models.data.LocalManager;
import nro.models.player.Player;
import nro.models.server.Manager;
import nro.models.utils.Logger;

public class LuckyRoundTopService {

    private static LuckyRoundTopService instance;
    private static boolean loggedMissingColumn;

    public static LuckyRoundTopService gI() {
        if (instance == null) {
            instance = new LuckyRoundTopService();
        }
        return instance;
    }

    public void addPoint(Player player, long point) {
        if (player == null || point <= 0) {
            return;
        }
        try {
            LocalManager.executeUpdate(
                    "INSERT INTO lucky_round_top (player_id, point) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE point = point + VALUES(point)",
                    player.id,
                    point);
            Manager.isTopLuckyRoundChanged = true;
        } catch (Exception e) {
            if (!loggedMissingColumn) {
                loggedMissingColumn = true;
                Logger.logException(LuckyRoundTopService.class, e,
                        "Khong the cong diem top vong quay. Hay tao bang lucky_round_top.");
            }
        }
    }
}
