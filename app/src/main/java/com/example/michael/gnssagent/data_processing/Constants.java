package com.example.michael.gnssagent.data_processing;

public final class Constants {

    // TODO: to be changes on new leap second
    public static final int UTC_TAI_LEAP_SECONDS = 37;
    public static final int GPS_UTC_LEAP_SECONDS = -UTC_TAI_LEAP_SECONDS + 19;



    public static final long NUMBER_MILLI_SECONDS_DAY = 86400000L;
    public static final double NUMBER_NANO_SECONDS_WEEK = 604800e9;
    public static final double NUMBER_NANO_SECONDS_DAY = 864000e8;
    public static final double NUMBER_NANO_SECONDS_100_MILLI = 1e8;
    public static final double SPEED_OF_LIGHT = 299792458.0; // m/s
    public static final double GPS_L1_FREQUENCY_HZ = 1.57542003e9;
    public static final double GPS_L5_FREQUENCY_HZ = 1.17645005e9;
    public static final double GALILEO_L1_FREQUENCY_HZ = 1.57542003e9;
    public static final double GALILEO_L5_FREQUENCY_HZ = 1.17645005e9;
    public static final double GLONASS_L1_FREQUENCY_HZ_AVG = 1.59721875e9; // +-0.00815625e9
    public static final double EPS_HZ_GPS = 10;
    public static final String ALL_FILES = "*";
    public static final String OBS_FILES = "obs";
    public static final String NAV_FILES = "nav";
    public static final String POS_FILES = "pos";

    public static final int GPS_STANDARD = 0;
    public static final int GPS_WEEK_SECONDS = 1;

    public static final long GPS_UTC_DIFF_MILLIS = 315964800000L + GPS_UTC_LEAP_SECONDS*1000;
}
