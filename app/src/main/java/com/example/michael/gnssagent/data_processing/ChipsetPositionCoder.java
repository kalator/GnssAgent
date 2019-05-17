package com.example.michael.gnssagent.data_processing;

import android.location.GnssMeasurementsEvent;
import android.location.Location;

import com.example.michael.gnssagent.data_processing.Constants;
import com.example.michael.gnssagent.data_processing.OneEpoch;
import com.example.michael.gnssagent.data_processing.TimeConverter;
import com.example.michael.gnssagent.data_processing.BaseCoder;

public class ChipsetPositionCoder extends BaseCoder {


    @Override
    public boolean parseData(OneEpoch oneEpoch) {
        return false;
    }

    @Override
    public boolean parseData(GnssMeasurementsEvent eventArgs) { return false; }

    public boolean parseData(Location location, int timeFormat, String sats) {
        addItemToFile(convertTime(
                location.getTime(), timeFormat)+"\t"+
                location.getLongitude()+"\t"+
                location.getLatitude()+"\t" +
                location.getAltitude() +"\t ±"+
                location.getAccuracy() + "\t " +
                sats);
        return true;
    }

    private String convertTime(long utcMillis, int timeFormat) {
        if (timeFormat == Constants.GPS_WEEK_SECONDS) {
            return TimeConverter.utcMillis2gpsWeekSeconds(utcMillis, 4);
        } else {
            if (timeFormat == Constants.GPS_STANDARD) {
                return TimeConverter.utcMillis2gpsStandard(utcMillis, 3);
            }
        }

        return "0000";
    }

}
