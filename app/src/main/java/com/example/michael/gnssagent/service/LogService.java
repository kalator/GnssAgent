package com.example.michael.gnssagent.service;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.michael.gnssagent.R;
import com.example.michael.gnssagent.data_processing.BaseCoder;
import com.example.michael.gnssagent.data_processing.BncCoder;
import com.example.michael.gnssagent.data_processing.ChipsetPositionCoder;
import com.example.michael.gnssagent.data_processing.Constants;
import com.example.michael.gnssagent.data_processing.RawCoder;
import com.example.michael.gnssagent.data_processing.OneEpoch;
import com.example.michael.gnssagent.data_processing.VisibleUsableSatelites;
import com.example.michael.gnssagent.ui.main.MainActivity;

import java.sql.Timestamp;
import java.util.Calendar;

import static com.example.michael.gnssagent.service.App.CHANNEL_ID;

public class LogService extends Service {

    private boolean logRawFlag; // flag to log or not to log (comes from activity)
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GnssStatus.Callback gnssStatusCallback;
    private BaseCoder obsCoder;
    private ChipsetPositionCoder posCoder;
    private String selectedObsCoder;
    private GnssMeasurementsEvent.Callback myGnssMeas;
    private GnssNavigationMessage.Callback myGnssNav;

    private int timeFormat;

    private boolean integerizeTime;

    private Timestamp logStartTimestamp;

    private Location loc;

    private String satelites = "";

    @Override
    public void onCreate() {
        Log.i("Service", "Created");
        super.onCreate();
        OneEpoch.resetClass();

        logRawFlag = false;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (logRawFlag) {
                    processPosition(location);
                }

                loc = location;
                updateMapActivity(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        gnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                String c;
                satelites = "";
                for (int i = 0; i < status.getSatelliteCount(); i++) {

                    String s;
                    if (status.getSvid(i) < 10) {
                        s = "0"+status.getSvid(i);
                    } else {
                        s = ""+status.getSvid(i);
                    }

                    if (status.getConstellationType(i) == GnssStatus.CONSTELLATION_GPS) {
                        if (Math.abs(status.getCarrierFrequencyHz(i) - 1.57544998E9) < Constants.EPS_HZ_GPS) {
                            c = "G"+s+"_L1";
                        } else {
                            c = "G"+s+"_L5";
                        }
                    } else if (status.getConstellationType(i) == GnssStatus.CONSTELLATION_GLONASS) {
                        c = "R"+s;
                    } else if (status.getConstellationType(i) == GnssStatus.CONSTELLATION_GALILEO) {
                        if (Math.abs(status.getCarrierFrequencyHz(i) - 1.57544998E9) < Constants.EPS_HZ_GPS) {
                            c = "E"+s+"_E1";
                        } else {
                            c = "E"+s+"_E5";
                        }
                    } else if (status.getConstellationType(i) == GnssStatus.CONSTELLATION_BEIDOU) {
                        c = "C"+s;
                    } else if (status.getConstellationType(i) == GnssStatus.CONSTELLATION_QZSS) {
                        c = "J"+s;
                    } else if (status.getConstellationType(i) == GnssStatus.CONSTELLATION_SBAS) {
                        c = "S"+s;
                    } else {
                        c = "U"+s;
                    }

                        satelites += c + " ";
                }
                Log.i("SATS", satelites);
                super.onSatelliteStatusChanged(status);
            }
        };

/*        myGnssNav = new GnssNavigationMessage.Callback() {
            @Override
            public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                Log.i("Service", event.toString());
                super.onGnssNavigationMessageReceived(event);

            }
        };*/

        myGnssMeas = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived(eventArgs);

                // observations
                processRawMeas(eventArgs);

                // position from chipset - wrong solution
             //   if (logRawFlag) {
            //        processPosition(loc);
             //   }
            }
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Service", "Resumed");
        // TODO: Check permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return START_NOT_STICKY;
        }
        locationManager.registerGnssMeasurementsCallback(myGnssMeas);
        locationManager.registerGnssStatusCallback(gnssStatusCallback);
//        locationManager.registerGnssNavigationMessageCallback(myGnssNav);


        // get location
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }

        // check if we are already logging, then do not do anything
        if  (!logRawFlag) {

            // flag indicating whether to log
            logRawFlag = intent.getBooleanExtra("logRawFlag", false);

            selectedObsCoder = intent.getStringExtra("selectedCoder");

            timeFormat = intent.getIntExtra("timeFormat", 0);

            integerizeTime = intent.getBooleanExtra("integerizeTime", true);

            // if clicked on notification, MainActivity will be opened
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);


            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.log_service))
                    .setContentText(getString(R.string.log_in_progress))
                    .setSmallIcon(R.drawable.ic_gps_fixed_black_24dp)
                    .setContentIntent(pendingIntent)
                    .setWhen(Calendar.getInstance().getTimeInMillis())
                    .setShowWhen(true)
    //                .addAction(R.drawable.ic_log_service, "Stop Log", pendingIntent)
                    .build();

            // push notification if we want to log
            if (logRawFlag) {
                // decide which coder to use
                // bnc style
                if (selectedObsCoder.equals(getResources().getStringArray(R.array.coders)[0])) {
                    obsCoder = new BncCoder();
                }
                // as is
                else if (selectedObsCoder.equals(getResources().getStringArray(R.array.coders)[1])) {
                    obsCoder = new RawCoder();
                }

                // position coder
                posCoder = new ChipsetPositionCoder();

                logStartTimestamp = new Timestamp(System.currentTimeMillis());

                if (!obsCoder.createFile(logStartTimestamp, Constants.OBS_FILES) ||
                        !posCoder.createFile(logStartTimestamp, Constants.POS_FILES)) {
                    pushMsg(getString(R.string.cannot_start_log));
                } else {
                    pushMsg(getString(R.string.log_started));
                    startForeground(1, notification);
                }

            }
        }

        // check if we want to stop the logging
        if (logRawFlag) {
            if (!intent.getBooleanExtra("logRawFlag", false)) {
                // received signal from clicking on stop button
                stopLogging();
                stopForeground(true);
            }

        }
        // do heavy work on background thread
        return START_NOT_STICKY;
    }

    private void stopLogging() {
        logRawFlag = false;
        obsCoder.closeFile();
        posCoder.closeFile();
        pushMsg(getString(R.string.log_saved));
    }

    @Override
    public void onDestroy() {
        OneEpoch.resetClass();
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        Log.i("Service", "Stopped");
    }

    // for bound services, we do not need it
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Parse received GNSS Raw Measurement to selected format.
     *  Parse position from chipset at the same time. */
    // TODO: control whether to save data or not
    private void processRawMeas(GnssMeasurementsEvent eventArgs) {
        //preprocess data
        OneEpoch oneEpoch = new OneEpoch(eventArgs, timeFormat, integerizeTime); // preprocessing of gnss data
        sendDataToMainActivity(oneEpoch.getSats());
        if (logRawFlag) {
         //   Toast.makeText(this, eventArgs.getClock().hasLeapSecond()+"",Toast.LENGTH_LONG).show();

            // bnc, send oneEpoch
            if (selectedObsCoder.equals(getResources().getStringArray(R.array.coders)[0])) {
                if (!obsCoder.parseData(oneEpoch)) {
                    pushMsg(getString(R.string.cannot_log));
                    logRawFlag = false;
                    obsCoder.closeFile();
                    posCoder.closeFile();
                }
            }

            // as is, send eventArgs
            else if (selectedObsCoder.equals(getResources().getStringArray(R.array.coders)[1])) {
                if (!obsCoder.parseData(eventArgs)) {
                    pushMsg(getString(R.string.cannot_log));
                    logRawFlag = false;
                    obsCoder.closeFile();
                    posCoder.closeFile();
                }

            }
/*
            // position from chipset
            if (!posCoder.parseData(loc, timeFormat, satelites)) {
                pushMsg(getString(R.string.cannot_log));
                logRawFlag = false;
                obsCoder.closeFile();
                posCoder.closeFile();
            }*/


        }
    }

    /** Toast */
    private void pushMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /** Update main activity with sat stats. */
    private void sendDataToMainActivity(VisibleUsableSatelites sats) {
        Intent intent = new Intent();
        intent.putExtra("Satellite status", sats);
        intent.setAction("UPDATE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /** Update maps activity with location. */
    private void updateMapActivity(Location location) {
        Intent intent = new Intent();
        intent.putExtra("location", location);
        intent.setAction("LOCATION");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /** Save position from chipset to file.*/
    public void processPosition(Location location) {
        posCoder.parseData(location, timeFormat, satelites);
    }
}
