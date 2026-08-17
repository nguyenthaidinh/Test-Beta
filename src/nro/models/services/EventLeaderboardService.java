package nro.models.services;

import nro.models.data.LocalManager;
import nro.models.data.LocalResultSet;
import nro.models.player.Player;
import nro.models.utils.Logger;

public class EventLeaderboardService {

    public static final String HALLOWEEN_BOX = "halloween_box";
    public static final String HALLOWEEN_CAPSULE = "halloween_capsule";
    public static final String HALLOWEEN_CANDY_BOX = "halloween_candy_box";
    private static EventLeaderboardService instance;
    private static boolean loggedUpdateError;

    public static EventLeaderboardService gI() {
        if (instance == null) {
            instance = new EventLeaderboardService();
        }
        return instance;
    }

    public int addPoint(Player player, String eventKey, int currentPoint, int point) {
        if (player == null || eventKey == null || eventKey.isEmpty() || point <= 0) {
            return currentPoint;
        }
        int fallbackPoint = safeAdd(currentPoint, point);
        try {
            LocalManager.executeUpdate(
                    "INSERT INTO event_leaderboard (event_key, player_id, point) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE point = GREATEST(point + ?, VALUES(point))",
                    eventKey,
                    player.id,
                    fallbackPoint,
                    point);
            return Math.max(fallbackPoint, getPoint(eventKey, player.id));
        } catch (Exception e) {
            if (!loggedUpdateError) {
                loggedUpdateError = true;
                Logger.logException(EventLeaderboardService.class, e,
                        "Không thể cập nhật event_leaderboard. Hãy chạy sql/event_leaderboard.sql.");
            }
            return fallbackPoint;
        }
    }

    public int getPoint(String eventKey, long playerId) {
        LocalResultSet rs = null;
        try {
            rs = LocalManager.executeQuery(
                    "SELECT point FROM event_leaderboard WHERE event_key = ? AND player_id = ? LIMIT 1",
                    eventKey,
                    playerId);
            if (rs.next()) {
                long point = rs.getLong("point");
                return point > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) point;
            }
        } catch (Exception e) {
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return 0;
    }

    private int safeAdd(int currentPoint, int point) {
        long result = (long) Math.max(0, currentPoint) + point;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
    }
}
