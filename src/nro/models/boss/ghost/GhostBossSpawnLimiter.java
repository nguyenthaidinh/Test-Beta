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
import nro.models.boss.BossID;
import nro.models.utils.Logger;
import nro.models.utils.TimeUtil;

/** Giới hạn và chia đều tối đa 500 lượt xuất hiện boss hồn ma trong một ngày. */
public final class GhostBossSpawnLimiter {

    public static final int DAILY_LIMIT = 500;

    private static final int[] GHOST_TYPES = {
        BossID.GHOST_SVK,
        BossID.GHOST_CAY_CON,
        BossID.GHOST_NGAO_CON
    };
    private static final Path STATE_FILE = Path.of("data", "ghost_boss_daily_spawn.properties");
    private static final GhostBossSpawnLimiter INSTANCE = new GhostBossSpawnLimiter();

    private LocalDate spawnDate;
    private int spawnedCount;
    private int lastSpawnSlot;

    private GhostBossSpawnLimiter() {
        loadState();
    }

    public static GhostBossSpawnLimiter gI() {
        return INSTANCE;
    }

    /**
     * Mỗi khung chỉ cho đúng một boss xuất hiện. Loại boss được luân phiên để ba hành tinh
     * nhận số lượng gần bằng nhau; các khung bị lỡ không được dồn lại thành một đợt lớn.
     */
    public synchronized boolean tryReserveSpawn(int ghostType) {
        resetForNewDayIfNeeded();
        if (spawnedCount >= DAILY_LIMIT) {
            return false;
        }

        int currentSlot = GhostDailySchedule.currentSlot(DAILY_LIMIT);
        if (currentSlot <= lastSpawnSlot || ghostType != getGhostTypeForSlot(currentSlot)) {
            return false;
        }

        int oldSlot = lastSpawnSlot;
        lastSpawnSlot = currentSlot;
        spawnedCount++;
        if (saveState()) {
            return true;
        }
        spawnedCount--;
        lastSpawnSlot = oldSlot;
        return false;
    }

    public synchronized int getSpawnedToday() {
        resetForNewDayIfNeeded();
        return spawnedCount;
    }

    private static int getGhostTypeForSlot(int slot) {
        return GHOST_TYPES[Math.floorMod(slot, GHOST_TYPES.length)];
    }

    private void resetForNewDayIfNeeded() {
        LocalDate today = today();
        if (!today.equals(spawnDate)) {
            spawnDate = today;
            spawnedCount = 0;
            lastSpawnSlot = -1;
        }
    }

    private void loadState() {
        spawnDate = today();
        spawnedCount = 0;
        lastSpawnSlot = -1;
        if (!Files.exists(STATE_FILE)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(STATE_FILE)) {
            properties.load(input);
            LocalDate savedDate = LocalDate.parse(properties.getProperty("date", ""));
            if (savedDate.equals(spawnDate)) {
                spawnedCount = Math.max(0, Math.min(DAILY_LIMIT,
                        Integer.parseInt(properties.getProperty("count", "0"))));
                lastSpawnSlot = Math.max(-1, Math.min(DAILY_LIMIT - 1,
                        Integer.parseInt(properties.getProperty("slot", "-1"))));
            }
        } catch (Exception e) {
            Logger.error("Không thể đọc lịch xuất hiện boss hồn ma: " + e.getMessage() + "\n");
        }
    }

    private boolean saveState() {
        Properties properties = new Properties();
        properties.setProperty("date", spawnDate.toString());
        properties.setProperty("count", Integer.toString(spawnedCount));
        properties.setProperty("slot", Integer.toString(lastSpawnSlot));

        Path temporaryFile = STATE_FILE.resolveSibling(STATE_FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(STATE_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(temporaryFile,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                properties.store(output, "Ghost boss daily spawn schedule");
            }
            try {
                Files.move(temporaryFile, STATE_FILE,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, STATE_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            Logger.error("Không thể lưu lịch xuất hiện boss hồn ma: " + e.getMessage() + "\n");
            return false;
        }
    }

    private static LocalDate today() {
        return LocalDate.now(TimeUtil.VIETNAM_ZONE);
    }
}
