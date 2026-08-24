package nro.models.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import nro.models.data.LocalManager;

/** Truy cập database của BXH săn Hồn Ma. */
public final class GhostHuntLeaderboardDAO {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS ghost_hunt_leaderboard (
                player_id BIGINT NOT NULL,
                point BIGINT UNSIGNED NOT NULL DEFAULT 0,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (player_id),
                INDEX idx_ghost_hunt_point (point, player_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;

    private static volatile boolean schemaReady;

    private GhostHuntLeaderboardDAO() {
    }

    public static void addPoint(long playerId, long point) throws SQLException {
        if (playerId <= 0 || point <= 0) {
            return;
        }
        try (Connection con = LocalManager.getConnection()) {
            ensureSchema(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO ghost_hunt_leaderboard (player_id, point) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE point = point + ?, updated_at = CURRENT_TIMESTAMP")) {
                ps.setLong(1, playerId);
                ps.setLong(2, point);
                ps.setLong(3, point);
                ps.executeUpdate();
            }
        }
    }

    public static List<Row> loadTop(int limit) throws SQLException {
        List<Row> rows = new ArrayList<>();
        try (Connection con = LocalManager.getConnection()) {
            ensureSchema(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT g.player_id, g.point, p.name, p.gender, p.head, p.items_body "
                    + "FROM ghost_hunt_leaderboard g "
                    + "INNER JOIN player p ON p.id = g.player_id "
                    + "WHERE g.point > 0 "
                    + "ORDER BY g.point DESC, g.player_id ASC LIMIT ?")) {
                ps.setInt(1, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new Row(
                                rs.getLong("player_id"),
                                rs.getLong("point"),
                                rs.getString("name"),
                                rs.getByte("gender"),
                                rs.getShort("head"),
                                rs.getString("items_body")));
                    }
                }
            }
        }
        return rows;
    }

    public static Standing getStanding(long playerId) throws SQLException {
        try (Connection con = LocalManager.getConnection()) {
            ensureSchema(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT me.point, (SELECT COUNT(*) + 1 FROM ghost_hunt_leaderboard ranked "
                    + "INNER JOIN player ranked_player ON ranked_player.id = ranked.player_id "
                    + "WHERE ranked.point > me.point "
                    + "OR (ranked.point = me.point AND ranked.player_id < me.player_id)) AS rank_position "
                    + "FROM ghost_hunt_leaderboard me "
                    + "INNER JOIN player current_player ON current_player.id = me.player_id "
                    + "WHERE me.player_id = ? AND me.point > 0")) {
                ps.setLong(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Standing(rs.getLong("point"), rs.getInt("rank_position"));
                    }
                }
            }
        }
        return new Standing(0, -1);
    }

    public static int clearAll() throws SQLException {
        try (Connection con = LocalManager.getConnection()) {
            ensureSchema(con);
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ghost_hunt_leaderboard")) {
                return ps.executeUpdate();
            }
        }
    }

    private static void ensureSchema(Connection con) throws SQLException {
        if (schemaReady) {
            return;
        }
        synchronized (GhostHuntLeaderboardDAO.class) {
            if (schemaReady) {
                return;
            }
            // Production normally creates the table through the migration file. Probe it
            // first so the game account only needs SELECT/INSERT/DELETE permissions and
            // is not forced to have CREATE TABLE permission on every fresh process.
            try (Statement statement = con.createStatement()) {
                statement.executeQuery("SELECT 1 FROM ghost_hunt_leaderboard LIMIT 0");
                schemaReady = true;
                return;
            } catch (SQLException tableNotReady) {
                // Local/development databases may not have run the migration yet.
            }
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate(CREATE_TABLE_SQL);
            }
            schemaReady = true;
        }
    }

    public record Row(long playerId, long point, String name, byte gender, short head, String itemsBody) {
    }

    public record Standing(long point, int rank) {
    }
}
