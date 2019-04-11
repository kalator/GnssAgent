package com.example.michael.gnssagent.ui.main;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.michael.gnssagent.R;
import com.example.michael.gnssagent.data_processing.VisibleUsableSatelites;

public class SatStatusFragment extends Fragment {

    private TextView gpsUsable;
    private TextView gpsVisible;
    private TextView galileoUsable;
    private TextView galileoVisible;
    private TextView glonassUsable;
    private TextView glonassVisible;
    private TextView sbasUsable;
    private TextView sbasVisible;
    private TextView beidouUsable;
    private TextView beidouVisible;
    private TextView qzssUsable;
    private TextView qzssVisible;
    private TextView gpsFq;
    private TextView galileoFq;

    private View myView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_sat_status, container, false);

//        setUpWidgets();

        return myView;
    }

    @Override
    public void onResume() {
        setUpWidgets();
        super.onResume();
    }

    public void setUpWidgets() {

        gpsUsable = myView.findViewById(R.id.gpsUsable);
        gpsVisible = myView.findViewById(R.id.gpsVisible);

        galileoUsable = myView.findViewById(R.id.galileoUsable);
        galileoVisible = myView.findViewById(R.id.galileoVisible);

        glonassUsable = myView.findViewById(R.id.glonassUsable);
        glonassVisible = myView.findViewById(R.id.glonassVisible);

        sbasUsable = myView.findViewById(R.id.sbasUsable);
        sbasVisible = myView.findViewById(R.id.sbasVisible);

        beidouUsable = myView.findViewById(R.id.beidouUsable);
        beidouVisible = myView.findViewById(R.id.beidouVisible);

        qzssUsable = myView.findViewById(R.id.qzssUsable);
        qzssVisible = myView.findViewById(R.id.qzssVisible);

        gpsFq = myView.findViewById(R.id.gpsFq);
        galileoFq = myView.findViewById(R.id.galileoFq);

        setDefaultSateliteStatus();
    }

    public void setDefaultSateliteStatus() {

        try {
            //gps
            gpsUsable.setText("0");
            gpsVisible.setText("0");

            // Galileo
            galileoUsable.setText("0");
            galileoVisible.setText("0");

            // Glonnas
            glonassUsable.setText("0");
            glonassVisible.setText("0");

            // Beidou
            beidouUsable.setText("0");
            beidouVisible.setText("0");

            // SBAS
            sbasUsable.setText("0");
            sbasVisible.setText("0");

            // QZSS
            qzssUsable.setText("0");
            qzssVisible.setText("0");

            // frequencies
            gpsFq.setText("0/0");
            galileoFq.setText("0/0");
        } catch (java.lang.NullPointerException e) {
        }
    }

    public void setSateliteStatus(VisibleUsableSatelites sats) {

        try {
            //gps
            if (sats.gpsUsable.toString() != null) gpsUsable.setText(sats.gpsUsable.toString());
            if (sats.gpsVisible.toString() != null) gpsVisible.setText(sats.gpsVisible.toString());

            // Galileo
            if (sats.galileoUsable.toString() != null)
                galileoUsable.setText(sats.galileoUsable.toString());
            if (sats.galileoVisible.toString() != null)
                galileoVisible.setText(sats.galileoVisible.toString());

            // Glonnas
            if (sats.glonassUsable.toString() != null)
                glonassUsable.setText(sats.glonassUsable.toString());
            if (sats.glonassVisible.toString() != null)
                glonassVisible.setText(sats.glonassVisible.toString());

            // Beidou
            if (sats.beidouUsable.toString() != null)
                beidouUsable.setText(sats.beidouUsable.toString());
            if (sats.beidouVisible.toString() != null)
                beidouVisible.setText(sats.beidouVisible.toString());

            // SBAS
            if (sats.sbasUsable.toString() != null) sbasUsable.setText(sats.sbasUsable.toString());
            if (sats.sbasVisible.toString() != null)
                sbasVisible.setText(sats.sbasVisible.toString());

            // QZSS
            if (sats.qzssUsable.toString() != null) qzssUsable.setText(sats.qzssUsable.toString());
            if (sats.qzssVisible.toString() != null)
                qzssVisible.setText(sats.qzssVisible.toString());

            // frequencies
            if (sats.gpsL1.toString() != null && sats.gpsL5.toString() != null) {
                gpsFq.setText(sats.gpsL1.toString() + "/" + sats.gpsL5.toString());
            }

            if (sats.galileoE1.toString() != null && sats.galileoE5.toString() != null) {
                galileoFq.setText(sats.galileoE1.toString() + "/" + sats.galileoE5.toString());
            }
        } catch (NullPointerException e){

        }
    }
}
