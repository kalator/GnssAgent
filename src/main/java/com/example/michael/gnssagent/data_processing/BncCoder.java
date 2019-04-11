package com.example.michael.gnssagent.data_processing;

import android.location.GnssMeasurementsEvent;

import java.util.List;

public class BncCoder extends BaseCoder {

    @Override
    public boolean parseData(OneEpoch inputData) {
        List<OneObs> allObs = inputData.getOneEpochObs();
        if (allObs.size() > 0) {
            addItemToFile("> " + allObs.get(0).getTime());
            for (OneObs obs : allObs) {
                addItemToFile(obs.toString());
            }
            addItemToFile("");
        }
        return true;
    }

    @Override
    public boolean parseData(GnssMeasurementsEvent eventArgs) {
        return false;
    }
}
