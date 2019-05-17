package com.example.michael.gnssagent.data_processing;

import android.location.GnssMeasurementsEvent;

import com.example.michael.gnssagent.data_processing.OneEpoch;
import com.example.michael.gnssagent.data_processing.BaseCoder;

public class RawCoder extends BaseCoder {

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
