package com.example.michael.gnssagent.data_processing;

import android.location.GnssMeasurementsEvent;

public class GnssLoggerCoder extends BaseCoder {

    @Override
    public boolean parseData(GnssMeasurementsEvent eventArgs) {
        addItemToFile(eventArgs.toString());
        return true;
    }

    @Override
    public boolean parseData(OneEpoch oneEpoch) {
        return false;
    }
}
