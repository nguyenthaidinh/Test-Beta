package nro.models.database;

import java.sql.Timestamp;
import nro.models.data.LocalManager;
import nro.models.player.Player;
import nro.models.utils.Logger;

public class GoldBarSpendHistoryDAO {

    private static volatile boolean schemaReady;
    private static volatile boolean schemaFailed;

    public static void insert(Player player, int amount, int balanceBefore, int balanceAfter,
            String actionCode, String reason, String details, String referenceId) {
        if (player == null || player.isBot || amount <= 0) {
            return;
        }
        try {
            ensureSchema();
            if (!schemaReady) {
                return;
            }
            Integer accountId = player.getSession() != null ? player.getSession().userId : null;
            String username = player.getSession() != null ? player.getSession().uu : null;
            LocalManager.executeUpdate(
                    "INSERT INTO gold_bar_spend_history "
                    + "(player_id, account_id, player_name, account_username, amount, "
                    + "balance_before, balance_after, action_code, reason, details, reference_id, created_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    player.id,
                    accountId,
                    safeText(player.name, 50),
                    safeText(username, 50),
                    amount,
                    balanceBefore,
                    balanceAfter,
                    safeText(actionCode, 64),
                    safeText(reason, 255),
                    details,
                    safeText(referenceId, 100),
                    new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            Logger.logException(GoldBarSpendHistoryDAO.class, e, "Khong the ghi lich su tieu Thoi Vang");
        }
    }

    private static void ensureSchema() {
        if (schemaReady || schemaFailed) {
            return;
        }
        synchronized (GoldBarSpendHistoryDAO.class) {
            if (schemaReady || schemaFailed) {
                return;
            }
            try {
                LocalManager.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS `gold_bar_spend_history` ("
                        + "`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
                        + "`player_id` BIGINT NOT NULL,"
                        + "`account_id` INT DEFAULT NULL,"
                        + "`player_name` VARCHAR(50) NOT NULL,"
                        + "`account_username` VARCHAR(50) DEFAULT NULL,"
                        + "`amount` INT UNSIGNED NOT NULL,"
                        + "`balance_before` INT UNSIGNED DEFAULT NULL,"
                        + "`balance_after` INT UNSIGNED DEFAULT NULL,"
                        + "`action_code` VARCHAR(64) NOT NULL,"
                        + "`reason` VARCHAR(255) NOT NULL,"
                        + "`details` TEXT DEFAULT NULL,"
                        + "`reference_id` VARCHAR(100) DEFAULT NULL,"
                        + "`created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),"
                        + "PRIMARY KEY (`id`),"
                        + "KEY `idx_gbsh_created` (`created_at`, `id`),"
                        + "KEY `idx_gbsh_player` (`player_id`, `created_at`, `id`),"
                        + "KEY `idx_gbsh_account` (`account_id`, `created_at`, `id`),"
                        + "KEY `idx_gbsh_player_name` (`player_name`, `created_at`, `id`),"
                        + "KEY `idx_gbsh_username` (`account_username`, `created_at`, `id`),"
                        + "KEY `idx_gbsh_action` (`action_code`, `created_at`, `id`),"
                        + "KEY `idx_gbsh_reference` (`reference_id`)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                schemaReady = true;
            } catch (Exception e) {
                schemaFailed = true;
                Logger.logException(GoldBarSpendHistoryDAO.class, e, "Khong the khoi tao bang gold_bar_spend_history");
            }
        }
    }

    private static String safeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
