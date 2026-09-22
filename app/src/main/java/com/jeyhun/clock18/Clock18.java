package com.jeyhun.clock18;

import java.util.Calendar;
import java.util.Locale;

public final class Clock18 {
    public static final double BASE = 69.28203230275509174;
    public static final double CUSTOM_HOUR_REAL_SECONDS = BASE * BASE; // 4800
    public static final double REAL_DAY_SECONDS = 86400.0;

    private Clock18() {}

    public static Parts now() {
        Calendar c = Calendar.getInstance();
        double elapsed = c.get(Calendar.HOUR_OF_DAY) * 3600.0
                + c.get(Calendar.MINUTE) * 60.0
                + c.get(Calendar.SECOND)
                + c.get(Calendar.MILLISECOND) / 1000.0;
        return fromElapsed(elapsed);
    }

    public static Parts fromElapsed(double elapsed) {
        elapsed = ((elapsed % REAL_DAY_SECONDS) + REAL_DAY_SECONDS) % REAL_DAY_SECONDS;
        int hour = (int)Math.floor(elapsed / CUSTOM_HOUR_REAL_SECONDS);
        double remHour = elapsed - hour * CUSTOM_HOUR_REAL_SECONDS;
        double minuteFloat = remHour / BASE;
        int minute = (int)Math.floor(minuteFloat);
        double second = remHour - minute * BASE;
        return new Parts(hour, minute, second, minuteFloat);
    }

    public static String hhmm(Parts p) {
        return String.format(Locale.US, "%02d:%02d", p.hour, p.minute);
    }

    public static String hhmmss(Parts p) {
        return String.format(Locale.US, "%02d:%02d:%02d", p.hour, p.minute, (int)Math.floor(p.second));
    }

    public static final class Parts {
        public final int hour;
        public final int minute;
        public final double second;
        public final double minuteFloat;

        public Parts(int hour, int minute, double second, double minuteFloat) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.minuteFloat = minuteFloat;
        }
    }
}
