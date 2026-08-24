package nro.models.services;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.utils.Logger;

/**
 * Giữ trạng thái bầu trời tối trên toàn server theo điều khiển của admin.
 * Dùng gói gọi Rồng Thần nhưng đặt map, khu và người gọi ở các ID không tồn tại,
 * nhờ đó mọi client chỉ nhận phần bầu trời tối mà không hiện rồng giả.
 */
public final class GlobalSkyService {

    private static final byte DRAGON_APPEAR = 0;
    private static final byte DRAGON_LEAVE = 1;
    private static final short HIDDEN_DRAGON_MAP = Short.MIN_VALUE;
    private static final short HIDDEN_DRAGON_BG = 0;
    private static final byte HIDDEN_DRAGON_ZONE = -1;
    private static final int HIDDEN_DRAGON_PLAYER = Integer.MIN_VALUE;
    private static final short HIDDEN_DRAGON_X = 0;
    private static final short HIDDEN_DRAGON_Y = 0;
    private static final byte SHENRON_TYPE = 0;
    private static final long DARK_RESTORE_DELAY_MS = 3_000L;
    private static final Path STATE_FILE = Path.of("data", "global_sky_state.dat");
    private static final ScheduledExecutorService RESTORE_EXECUTOR
            = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "global-sky-restorer");
                thread.setDaemon(true);
                return thread;
            });

    private static final GlobalSkyService INSTANCE = new GlobalSkyService();

    private boolean dark = loadState();
    private long stateVersion;
    private ScheduledFuture<?> pendingRestore;

    private GlobalSkyService() {
    }

    public static GlobalSkyService gI() {
        return INSTANCE;
    }

    public synchronized boolean isDark() {
        return dark;
    }

    public synchronized void toggle(Player admin) {
        setDark(admin, !dark);
    }

    public synchronized void setDark(Player admin, boolean enable) {
        if (admin == null || !admin.isAdmin()) {
            return;
        }
        if (dark != enable) {
            stateVersion++;
        }
        dark = enable;
        if (enable) {
            saveState();
            sendDarkToAllPlayers();
            scheduleDarkRestore();
            Service.gI().sendThongBao(admin,
                    "Đã làm tối bầu trời trên toàn server. Trạng thái sẽ giữ nguyên đến khi admin bật sáng.");
        } else {
            cancelPendingRestore();
            saveState();
            sendLightToAllPlayers();
            Service.gI().sendThongBao(admin, "Đã bật sáng bầu trời trên toàn server.");
        }
    }

    public synchronized void syncPlayer(Player player) {
        if (dark && player != null && player.isPl()) {
            sendDark(player);
        }
    }

    /**
     * Gọi sau khi Rồng Thần thật rời đi vì gói rời rồng làm client
     * sáng trở lại. Nếu admin vẫn bật chế độ tối thì phải áp lại ngay.
     */
    public synchronized void restoreAfterDragonLeaves() {
        if (dark) {
            scheduleDarkRestore();
        }
    }

    private void scheduleDarkRestore() {
        cancelPendingRestore();
        long expectedVersion = stateVersion;
        pendingRestore = RESTORE_EXECUTOR.schedule(() -> {
            synchronized (GlobalSkyService.this) {
                pendingRestore = null;
                if (dark && stateVersion == expectedVersion) {
                    sendDarkToAllPlayers();
                }
            }
        }, DARK_RESTORE_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void cancelPendingRestore() {
        if (pendingRestore != null) {
            pendingRestore.cancel(false);
            pendingRestore = null;
        }
    }

    private void sendDarkToAllPlayers() {
        Message message = null;
        try {
            message = createDarkMessage();
            Service.gI().sendMessAllPlayer(message);
            message = null;
        } catch (Exception e) {
            Logger.logException(GlobalSkyService.class, e);
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    private void sendDark(Player player) {
        Message message = null;
        try {
            message = createDarkMessage();
            player.sendMessage(message);
        } catch (Exception e) {
            Logger.logException(GlobalSkyService.class, e);
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    private Message createDarkMessage() throws Exception {
        Message message = new Message(-83);
        message.writer().writeByte(DRAGON_APPEAR);
        message.writer().writeShort(HIDDEN_DRAGON_MAP);
        message.writer().writeShort(HIDDEN_DRAGON_BG);
        message.writer().writeByte(HIDDEN_DRAGON_ZONE);
        message.writer().writeInt(HIDDEN_DRAGON_PLAYER);
        message.writer().writeUTF("null");
        message.writer().writeShort(HIDDEN_DRAGON_X);
        message.writer().writeShort(HIDDEN_DRAGON_Y);
        message.writer().writeByte(SHENRON_TYPE);
        return message;
    }

    private void sendLightToAllPlayers() {
        Message message = null;
        try {
            message = new Message(-83);
            message.writer().writeByte(DRAGON_LEAVE);
            Service.gI().sendMessAllPlayer(message);
            message = null;
        } catch (Exception e) {
            Logger.logException(GlobalSkyService.class, e);
        } finally {
            if (message != null) {
                message.cleanup();
            }
        }
    }

    private static boolean loadState() {
        try {
            return Files.exists(STATE_FILE)
                    && Boolean.parseBoolean(Files.readString(STATE_FILE, StandardCharsets.UTF_8).trim());
        } catch (Exception e) {
            Logger.logException(GlobalSkyService.class, e);
            return false;
        }
    }

    private void saveState() {
        try {
            Files.createDirectories(STATE_FILE.getParent());
            Files.writeString(STATE_FILE, Boolean.toString(dark), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Logger.logException(GlobalSkyService.class, e);
        }
    }
}
