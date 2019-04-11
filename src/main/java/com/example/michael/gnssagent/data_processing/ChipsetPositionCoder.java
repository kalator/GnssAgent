package com.example.michael.gnssagent.data_processing;

import android.app.Application;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.example.michael.gnssagent.service.App;
import com.example.michael.gnssagent.ui.main.MainActivity;

public class ChipsetPositionCoder extends BaseCoder {


    @Override
    public boolean parseData(OneEpoch oneEpoch) {
        return false;
    }

    @Override
    public boolean parseData(GnssMeasurementsEvent eventArgs) { return false; }

    public boolean parseData(Location location, int timeFormat) {
        addItemToFile(convertTime(
                location.getTime()+Constants.GPS_UTC_LEAP_SECONDS, timeFormat)+"\t"+
                location.getLongitude()+"\t"+
                location.getLatitude()+"\t ±"+
                location.getAccuracy());
        return true;
    }

    private String convertTime(long utcMillis, int timeFormat) {
        if (timeFormat == Constants.GPS_WEEK_SECONDS) {
            return TimeConverter.utcMillis2gpsWeekSeconds(utcMillis, 3);
        } else {
            if (timeFormat == Constants.GPS_STANDARD) {
                return TimeConverter.utcMillis2gpsStandard(utcMillis, 3);
            }
        }

        return "0000";
    }

}
