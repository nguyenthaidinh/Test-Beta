package nro.models.boss.ghost;

import java.time.Duration;
import java.time.ZonedDateTime;
import nro.models.utils.TimeUtil;

/** Chia đều một số lượt cố định vào các khung thời gian trong ngày Việt Nam. */
final class GhostDailySchedule {

    static final long DAY_MILLIS = Duration.ofDays(1).toMillis();

    private GhostDailySchedule() {
    }

    static int currentSlot(int dailySlots) {
        ZonedDateTime now = ZonedDateTime.now(TimeUtil.VIETNAM_ZONE);
        long millisSinceStartOfDay = Duration.between(
                now.toLocalDate().atStartOfDay(TimeUtil.VIETNAM_ZONE), now).toMillis();
        return slotAt(millisSinceStartOfDay, dailySlots);
    }

    static int slotAt(long millisSinceStartOfDay, int dailySlots) {
        if (dailySlots <= 0) {
            throw new IllegalArgumentException("Số khung trong ngày phải lớn hơn 0");
        }
        long safeMillis = Math.max(0L, Math.min(DAY_MILLIS - 1L, millisSinceStartOfDay));
        return (int) Math.min(dailySlots - 1L, safeMillis * dailySlots / DAY_MILLIS);
    }
}
