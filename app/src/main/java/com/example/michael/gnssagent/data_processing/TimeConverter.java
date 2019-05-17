package com.example.michael.gnssagent.data_processing;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class TimeConverter {
    // Location does not take leap seconds to account!!

    public static String utcMillis2gpsWeekSeconds(long utcMillis, int precision) {
        return gpsNanos2gpsWeekSeconds(utcMillis2gpsNanos(utcMillis, precision), precision);
    }

    public static String utcMillis2gpsStandard(long utcMillis, int precision) {
        return gpsNanos2gpsStandard(utcMillis2gpsNanos(utcMillis, precision), precision);

    }

    // Date class is computing from 1st Jan 1970, GPS time is from 6th Jan 1980
    public static String gpsNanos2gpsStandard(long gpsNanos, int precision) {
        long currentMillis = System.currentTimeMillis();
        long currentDayMillis = currentMillis - currentMillis % Constants.NUMBER_MILLI_SECONDS_DAY;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
        long nanosInDay = (gpsNanos+Constants.GPS_UTC_DIFF_MILLIS*1000000)
                - currentDayMillis*1000000;
        double secondsInDay = (nanosInDay/1e9)%60;
        double minutesInDay = Math.floor((nanosInDay/1e9)/60)%60;
        double hoursInDay = Math.floor(((nanosInDay/1e9)/60)/60);
        return sdf.format(new Date(currentDayMillis))
                +String.format(" %.0f %.0f %."+precision+"f", hoursInDay, minutesInDay, secondsInDay);
    }

    public static long utcMillis2gpsNanos(long utcMillis, int precision) {
        return (utcMillis - Constants.GPS_UTC_DIFF_MILLIS)*1000000;
    }

    public static String gpsNanos2gpsWeekSeconds(long gpsNanos, int precision) {
        double seconds = gpsNanos/1e9;
        double weeks = Math.floor(seconds/(3600*24*7));
        return String.format("%.0f %."+precision+"f",weeks,(seconds-weeks*7*24*3600));
    }

}
