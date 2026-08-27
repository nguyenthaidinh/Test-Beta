package nro.models.boss.ghost;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.Properties;
import nro.models.utils.Logger;
import nro.models.utils.TimeUtil;

/**
 * Giới hạn 500 vật phẩm Hồn ma mỗi ngày và chia thành 500 khung đều trong 24 giờ.
 * Trạng thái được lưu ra đĩa để không thể làm mới giới hạn bằng cách khởi động lại server.
 */
public final class GhostSoulDropLimiter {

    public static final int DAILY_LIMIT = 500;

    private static final Path STATE_FILE = Path.of("data", "ghost_soul_daily_drop.properties");
    private static final GhostSoulDropLimiter INSTANCE = new GhostSoulDropLimiter();

    private LocalDate dropDate;
    private int droppedCount;

    private GhostSoulDropLimiter() {
        loadState();
    }

    public static GhostSoulDropLimiter gI() {
        return INSTANCE;
    }

    /**
     * Giữ trước một lượt rơi trong phần hạn mức đã được mở tới thời điểm hiện tại.
     *
     * Hạn mức 500 cái được mở dần đều trong ngày. Các boss thường bị hạ theo từng đợt,
     * nên cho phép dùng bù những lượt của các khung trước chưa rơi; nếu chỉ cho đúng một
     * cái ở khung hiện tại thì phần lớn lượt trong một đợt boss sẽ bị mất.
     * Chỉ trả về true sau khi trạng thái mới được lưu thành công.
     */
    public synchronized boolean tryReserveDrop() {
        resetForNewDayIfNeeded();
        int releasedDropCount = GhostDailySchedule.currentSlot(DAILY_LIMIT) + 1;
        if (droppedCount >= DAILY_LIMIT || droppedCount >= releasedDropCount) {
            return false;
        }

        droppedCount++;
        if (saveState()) {
            return true;
        }

        // Không tạo vật phẩm nếu không lưu được số đếm, tránh vượt giới hạn sau khi restart.
        droppedCount--;
        return false;
    }

    public synchronized int getDroppedToday() {
        resetForNewDayIfNeeded();
        return droppedCount;
    }

    public synchronized int getRemainingToday() {
        return DAILY_LIMIT - getDroppedToday();
    }

    private void resetForNewDayIfNeeded() {
        LocalDate today = today();
        if (!today.equals(dropDate)) {
            dropDate = today;
            droppedCount = 0;
        }
    }

    private void loadState() {
        dropDate = today();
        droppedCount = 0;
        if (!Files.exists(STATE_FILE)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(STATE_FILE)) {
            properties.load(input);
            LocalDate savedDate = LocalDate.parse(properties.getProperty("date", ""));
            int savedCount = Integer.parseInt(properties.getProperty("count", "0"));
            if (savedDate.equals(dropDate)) {
                droppedCount = Math.max(0, Math.min(DAILY_LIMIT, savedCount));
            }
        } catch (Exception e) {
            Logger.error("Không thể đọc giới hạn rơi Hồn ma: " + e.getMessage() + "\n");
        }
    }

    private boolean saveState() {
        Properties properties = new Properties();
        properties.setProperty("date", dropDate.toString());
        properties.setProperty("count", Integer.toString(droppedCount));

        Path temporaryFile = STATE_FILE.resolveSibling(STATE_FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(STATE_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(temporaryFile,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                properties.store(output, "Ghost soul global daily drop limit");
            }
            try {
                Files.move(temporaryFile, STATE_FILE,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, STATE_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            Logger.error("Không thể lưu giới hạn rơi Hồn ma: " + e.getMessage() + "\n");
            return false;
        }
    }

    private static LocalDate today() {
        return LocalDate.now(TimeUtil.VIETNAM_ZONE);
    }
}
