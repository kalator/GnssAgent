package com.example.michael.gnssagent.data_processing;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.ArrayList;


/** Container for base satelite info for each constellation:
 * -number of visible satelites
 * -number of used satelites
 * -number of L1/L5 for gps and E1/E5 for galileo
 * -SVIDs and signal strengths in ArrayList of map
 */

public class VisibleUsableSatelites implements Parcelable {

    public Integer gpsVisible;
    public Integer galileoVisible;
    public Integer glonassVisible;
    public Integer sbasVisible;
    public Integer beidouVisible;
    public Integer qzssVisible;

    public Integer gpsUsable;
    public Integer galileoUsable;
    public Integer glonassUsable;
    public Integer sbasUsable;
    public Integer beidouUsable;
    public Integer qzssUsable;

    public Integer gpsL1;
    public Integer gpsL5;
    public Integer galileoE1;
    public Integer galileoE5;

    public Integer usableInTotal;

    public ArrayList<Pair<String, Double>> satStrengthList;


    VisibleUsableSatelites() {
        gpsUsable = 0;
        galileoUsable = 0;
        glonassUsable = 0;
        sbasUsable = 0;
        beidouUsable = 0;
        qzssUsable = 0;

        gpsVisible = 0;
        galileoVisible = 0;
        glonassVisible =0;
        sbasVisible = 0;
        beidouVisible = 0;
        qzssVisible = 0;

        gpsL1 = 0;
        gpsL5 = 0;
        galileoE1 = 0;
        galileoE5 = 0;

        usableInTotal = 0;

        satStrengthList = new ArrayList<>();
    }

    protected VisibleUsableSatelites(Parcel in) {
        gpsVisible = in.readInt();
        galileoVisible = in.readInt();
        glonassVisible = in.readInt();
        sbasVisible = in.readInt();
        beidouVisible = in.readInt();
        qzssVisible = in.readInt();

        gpsUsable = in.readInt();
        galileoUsable = in.readInt();
        glonassUsable = in.readInt();
        sbasUsable = in.readInt();
        beidouUsable = in.readInt();
        qzssUsable = in.readInt();

        gpsL1 = in.readInt();
        gpsL5 = in.readInt();
        galileoE1 = in.readInt();
        galileoE5 = in.readInt();

        usableInTotal = in.readInt();

        satStrengthList = in.readArrayList(null);
    }

    public static final Creator<VisibleUsableSatelites> CREATOR = new Creator<VisibleUsableSatelites>() {
        @Override
        public VisibleUsableSatelites createFromParcel(Parcel in) {
            return new VisibleUsableSatelites(in);
        }

        @Override
        public VisibleUsableSatelites[] newArray(int size) {
            return new VisibleUsableSatelites[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(gpsVisible);
        parcel.writeInt(galileoVisible);
        parcel.writeInt(glonassVisible);
        parcel.writeInt(sbasVisible);
        parcel.writeInt(beidouVisible);
        parcel.writeInt(qzssVisible);

        parcel.writeInt(gpsUsable);
        parcel.writeInt(galileoUsable);
        parcel.writeInt(glonassUsable);
        parcel.writeInt(sbasUsable);
        parcel.writeInt(beidouUsable);
        parcel.writeInt(qzssUsable);

        parcel.writeInt(gpsL1);
        parcel.writeInt(gpsL5);
        parcel.writeInt(galileoE1);
        parcel.writeInt(galileoE5);

        parcel.writeInt(usableInTotal);

        parcel.writeList(satStrengthList);
    }
}

