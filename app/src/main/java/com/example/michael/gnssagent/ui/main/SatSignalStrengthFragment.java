package com.example.michael.gnssagent.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.michael.gnssagent.R;
import com.example.michael.gnssagent.data_processing.VisibleUsableSatelites;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SatSignalStrengthFragment extends Fragment {

    BarChart chart;
    YAxis yAxis;
    XAxis xAxis;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_sat_strength, container, false);



        chart = myView.findViewById(R.id.chart);

        setUpChart();

        return myView;
    }

    public void setUpChart(){
        chart.setTouchEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets( 0, 0, 0, 5);


        yAxis = chart.getAxisLeft();

        yAxis.setAxisMaximum(50f);
        yAxis.setAxisMinimum(0f);

        xAxis = chart.getXAxis();
        xAxis.setGranularity(1);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(290);
        xAxis.setDrawGridLines(false);

    }

    public void updateChart(VisibleUsableSatelites sats) {
        try {

            sortSats(sats.satStrengthList);


            List<BarEntry> entries = new ArrayList<>();
            ArrayList<String> xLabel = new ArrayList<>();

            float i = 0;
            for (Pair<String, Double> s : sats.satStrengthList) {
                entries.add(new BarEntry(i, s.second.floatValue()));
                xLabel.add(s.first);
                i = i + 1;
            }


            xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));

            xAxis.setLabelCount(xLabel.size());

            BarDataSet set = new BarDataSet(entries, "BarDataSet");

            set.setColors(ColorTemplate.COLORFUL_COLORS);

            BarData data = new BarData(set);

            chart.setData(data);

            data.setBarWidth(0.6f);

            chart.invalidate();

        } catch (NullPointerException e) {

        }
    }

    public void sortSats(ArrayList<Pair<String, Double>> sats) {

        sats.sort(new Comparator<Pair<String, Double>>() {

            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {

                if (o1.first.compareTo(o2.first) < 0) {
                    return -1;
                } else if (o1.first.compareTo(o2.first) > 0){
                    return 1;
                } else {
                    return 0;
                }
            }
        });


    }
}
