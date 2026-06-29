package nro.models.boss;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Quản lý tỉ lệ rơi đồ Thần Linh riêng cho từng loại boss.
 */
public final class BossDropRateManager {

    private static final Path CONFIG_PATH = Path.of("data", "boss_than_linh_rates.properties");
    private static final Map<String, Integer> DEFAULT_RATES = createDefaultRates();
    private static final BossDropRateManager INSTANCE = new BossDropRateManager();

    private final Map<String, Integer> configuredRates = new ConcurrentHashMap<>();

    private BossDropRateManager() {
        load();
    }

    public static BossDropRateManager gI() {
        return INSTANCE;
    }

    public boolean supports(Boss boss) {
        return boss != null && DEFAULT_RATES.containsKey(getKey(boss));
    }

    public int getDefaultRate(Boss boss) {
        return boss == null ? 0 : DEFAULT_RATES.getOrDefault(getKey(boss), 0);
    }

    public Integer getConfiguredRate(Boss boss) {
        return boss == null ? null : configuredRates.get(getKey(boss));
    }

    public int getEffectiveRate(Boss boss) {
        if (boss == null) {
            return 0;
        }
        return configuredRates.getOrDefault(getKey(boss), getDefaultRate(boss));
    }

    public boolean shouldDrop(Boss boss, int defaultRate) {
        int rate = clamp(defaultRate);
        if (boss != null) {
            String key = getKey(boss);
            int registeredDefault = DEFAULT_RATES.getOrDefault(key, rate);
            rate = configuredRates.getOrDefault(key, registeredDefault);
        }
        return rate > 0 && Util.isTrue(rate, 100);
    }

    /**
     * @param rate tỉ lệ 0-100; null để trả về tỉ lệ mặc định trong code
     */
    public synchronized boolean setConfiguredRate(Boss boss, Integer rate) {
        if (boss == null || (rate != null && (rate < 0 || rate > 100))) {
            return false;
        }

        String key = getKey(boss);
        Integer previous = configuredRates.get(key);
        if (rate == null) {
            configuredRates.remove(key);
        } else {
            configuredRates.put(key, rate);
        }

        if (save()) {
            return true;
        }

        if (previous == null) {
            configuredRates.remove(key);
        } else {
            configuredRates.put(key, previous);
        }
        return false;
    }

    private String getKey(Boss boss) {
        return boss.getClass().getName();
    }

    private void load() {
        if (!Files.exists(CONFIG_PATH)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                try {
                    int rate = Integer.parseInt(properties.getProperty(key).trim());
                    if (rate >= 0 && rate <= 100) {
                        configuredRates.put(key, rate);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            Logger.error("Không thể đọc cấu hình tỉ lệ Thần Linh boss: " + e.getMessage() + "\n");
        }
    }

    private boolean save() {
        Properties properties = new Properties();
        configuredRates.forEach((key, rate) -> properties.setProperty(key, String.valueOf(rate)));
        Path temporaryPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(temporaryPath,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                properties.store(output, "Boss Than Linh drop rates (0-100)");
            }
            try {
                Files.move(temporaryPath, CONFIG_PATH,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            Logger.error("Không thể lưu cấu hình tỉ lệ Thần Linh boss: " + e.getMessage() + "\n");
            return false;
        }
    }

    private static int clamp(int rate) {
        return Math.max(0, Math.min(100, rate));
    }

    private static Map<String, Integer> createDefaultRates() {
        Map<String, Integer> rates = new HashMap<>();

        add(rates, 10,
                "nro.models.boss.Android.Android13",
                "nro.models.boss.Android.Android14",
                "nro.models.boss.Android.Android15",
                "nro.models.boss.Android.Android19",
                "nro.models.boss.Android.DrKore",
                "nro.models.boss.Android.KingKong",
                "nro.models.boss.Android.Pic",
                "nro.models.boss.Android.Poc",
                "nro.models.boss.Cell.XenBoHung",
                "nro.models.boss.Cell.SieuBoHung");

        add(rates, 1,
                "nro.models.boss.MajinBuu_12h.BuiBui",
                "nro.models.boss.MajinBuu_12h.BuiBui2",
                "nro.models.boss.MajinBuu_12h.Cadic",
                "nro.models.boss.MajinBuu_12h.Drabura",
                "nro.models.boss.MajinBuu_12h.Drabura2",
                "nro.models.boss.MajinBuu_12h.Drabura3",
                "nro.models.boss.MajinBuu_12h.Goku",
                "nro.models.boss.MajinBuu_12h.Yacon");

        add(rates, 20, "nro.models.boss.Baby.Baby");
        add(rates, 35, "nro.models.boss.Black_Goku.BlackGoku");
        add(rates, 30,
                "nro.models.boss.Cold.Cooler",
                "nro.models.boss.cumber.Cumber",
                "nro.models.boss.Golden_fireza.GoldenFrieza",
                "nro.models.boss.ma_vuong_picolo_namek.Pocolo",
                "nro.models.boss.MajinBuu_12h.Mabu");
        add(rates, 70,
                "nro.models.boss.gokuvegeta.Goku",
                "nro.models.boss.gokuvegeta.Cadic",
                "nro.models.boss.gokuvegeta.BlackGoku");

        // Các lớp dưới đây dùng bảng thưởng Thần Linh 30% khi ở Thành Cổ.
        add(rates, 30,
                "nro.models.boss.doanh_trai.NinjaAoTim",
                "nro.models.boss.doanh_trai.RobotVeSi",
                "nro.models.boss.doanh_trai.TrungUyThep",
                "nro.models.boss.doanh_trai.TrungUyTrang",
                "nro.models.boss.doanh_trai.TrungUyXanhLo",
                "nro.models.boss.event_hung_vuong.SonTinh",
                "nro.models.boss.event_hung_vuong.ThuyTinh",
                "nro.models.boss.event_trung_thu.KhiDot",
                "nro.models.boss.nhan_ban.NhanBan");

        return Map.copyOf(rates);
    }

    private static void add(Map<String, Integer> rates, int rate, String... classNames) {
        for (String className : classNames) {
            rates.put(className, rate);
        }
    }
}
