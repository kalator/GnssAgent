package com.example.michael.gnssagent.data_processing;

import android.location.GnssMeasurementsEvent;
import android.os.Environment;
import android.util.Log;

import com.example.michael.gnssagent.data_processing.OneEpoch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public abstract class BaseCoder {

    // variables
    protected File file;
    private FileOutputStream stream;
    protected OneEpoch data;
    private double fullBiasNanos, biasNanos;
    private boolean pseudoRangeCompIsOn = false;


    // ==============================================
    // methods
    public abstract boolean parseData(OneEpoch oneEpoch);

    public abstract boolean parseData(GnssMeasurementsEvent eventArgs);

    /** Create file with name derived from input timestamp and file type*/
    public boolean createFile(Timestamp timestamp, String fileType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String fileName = String.format("log_%s_%s.txt", sdf.format(timestamp), fileType);

        File folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS),"GnssAgent");
        Log.i("folder", folder.toString());

        if(!folder.exists()) {
            if(!folder.mkdirs()) {
                return false;
            }
        }

        // TODO: Catch if file cannot be created
        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath(), "GnssAgent/" +fileName);
        Log.i("file", file.toString());

        try {
            stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /** write one line to file */
    protected boolean addItemToFile(String outputText) {
        try {
            stream.write(outputText.getBytes());
            stream.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeFile() {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Get name of file. */
    public String getFileName() {
        return file.getName();
    }
}
