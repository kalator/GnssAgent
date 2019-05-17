package com.example.michael.gnssagent.ui.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.michael.gnssagent.service.LogService;
import com.example.michael.gnssagent.data_processing.OneEpoch;
import com.example.michael.gnssagent.R;
import com.example.michael.gnssagent.data_processing.VisibleUsableSatelites;
import com.example.michael.gnssagent.ui.BaseActivity;
import com.example.michael.gnssagent.ui.MapsActivity;
import com.example.michael.gnssagent.ui.files_managing.FilesActivity;


public class MainActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private SatStatusFragment t1;
    private SatSignalStrengthFragment t2;

    private static boolean serviceOn; // for setting state of start/stop log buttons (opening
                                      // activity from notification is resetting it)
    private static long stopWatchEllapsed;  // save time, when stopwatch was started for restarting
                                            // the activity while log is on

    private BroadcastReceiver receiveUpdateFromLogService;
    private BroadcastReceiver gnssStatus;

    private boolean logRawFlag; // flag whether user wants to log

    private Button startButton; // start logging button
    private Button stopButton;  // stop logging button
    private Spinner coderChooser; // drop down with coders
    private String selectedCoder; // for LogService
    private Chronometer stopWatch;
    private TextView locationOff;

    float x1, x2, y1, y2; // for motionevent - swiping between activities


    public static final int PERMISSIONS_ALL= 99;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Activity", "onCreate");
        super.onCreate(savedInstanceState);
/*        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        Toast.makeText(this, getDeviceName() + " " + manufacturer + " " + model, Toast.LENGTH_SHORT).show();
*/

        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        logRawFlag = false; // set up

        setUpWidgets();

        // tabbed fragments
        t1 = new SatStatusFragment();
        t2 = new SatSignalStrengthFragment();

        // set up coder Chooser
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.coders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coderChooser.setAdapter(adapter);
        coderChooser.setOnItemSelectedListener(this);

        // set up buttons if service is on (see declaration of serviceOn)
        if(serviceOn) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            // reset OneEpoch class to get time fix
            OneEpoch.resetClass();
        }

        checkPermissions();
        setUpBroadcastReceivers();

        statusCheck();
        startService(); // start location service

/*        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());*/
    }

    public void setUpBroadcastReceivers() {
        // set up broadcast receiver receiving updates from LogService
        receiveUpdateFromLogService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // show statistics of visible, usable and L1/L5, E1/E5 signals
                // disable/enable start log button according to number of usable satellites
                // (now at least 1 must be usable)
                // TODO: change number of mandatory satellites
                showStatisticsToUser(intent);
            }
        };

        // set up broadcast receiver to detect if user has toggled location off
        gnssStatus = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    // check if location was toggled off
                    statusCheck();
                }
            }
        };

        // register broadcast receiver receiving updates from LogService
        LocalBroadcastManager
                .getInstance(MainActivity.this)
                .registerReceiver(receiveUpdateFromLogService, new IntentFilter("UPDATE"));

        // register broadcast receiver detecting if user has toggled location off
        registerReceiver(gnssStatus,
                new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

    }

    public void setUpWidgets() {
        stopWatch = findViewById(R.id.chronometer);

        startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false); // set false for no satellite is online at the beginning
        stopButton = findViewById(R.id.stopButton);

        coderChooser = findViewById(R.id.coderChooser);

        locationOff = findViewById(R.id.locationOff);

    }

    @Override
    protected void onResume() {
        if (serviceOn) {
            stopWatch.setBase(stopWatchEllapsed);
        } else {
            // reset OneEpoch for getting time fix
            OneEpoch.resetClass();
        }
        Log.i("Activity", "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("Activity", "onPause");
        if (serviceOn) {
            stopWatchEllapsed = stopWatch.getBase();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i("Activity","onDestroy");
        OneEpoch.resetClass();
        stopService();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiveUpdateFromLogService);
        unregisterReceiver(gnssStatus);
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.i("Activity", "onRestart");
        super.onRestart();
    }

    /** Start logging on button click */
    // TODO: check, if writing to file is enabled
    public void startLogging(View v) {
        // start stop watch
        if(!serviceOn) {
            stopWatch.setBase(SystemClock.elapsedRealtime());
            stopWatch.start();
        }
        logRawFlag = true;
        serviceOn = true;
        stopButton.setEnabled(true);
        startButton.setEnabled(false);
        startService();
    }

    public void startService() {

        Intent serviceIntent = new Intent(this, LogService.class);
        serviceIntent.putExtra("logRawFlag", logRawFlag);
        serviceIntent.putExtra("selectedCoder", selectedCoder);
        serviceIntent.putExtra("timeFormat", log_time_format);
        serviceIntent.putExtra("integerizeTime", integerize_time);

        startService(serviceIntent);
    }

    /** Stop logging on button click */
    public void stopLogging(View v) {

        stopWatch.stop();
        stopWatch.setBase(SystemClock.elapsedRealtime());

        stopServiceHelpMeta();

        Intent intent = new Intent(this, LogService.class);
        intent.putExtra("logRawFlag", false);
        startService(intent); // we do not want to stop service, we just want to stop logging
    }

    /** Stop logging on destroying activity. */
    public void stopService() {
        stopServiceHelpMeta();
        Intent serviceIntent = new Intent(this, LogService.class);
        stopService(serviceIntent);
    }

    /** Method implementing common things for stopLogging and stopService methods. */
    public void stopServiceHelpMeta() {
        serviceOn = false;
        logRawFlag = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public void checkPermissions() {
        final String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL);
        }
    }

    // TODO: rework
    public static boolean hasPermissions(Context context, String[] permissions) {

        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // External storage
        if (requestCode == PERMISSIONS_ALL) {
            if (grantResults.length > 0 &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                startService();
            }

        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI() {};

    // listener for drop down
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedCoder = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationOff.setText(R.string.locationOff);
            startButton.setEnabled(false);
            t1.setDefaultSateliteStatus();
        }

        else {
            locationOff.setText(R.string.emptyString);
        }
    }

    public void showStatisticsToUser(Intent intent) {
        // set satelite status
        VisibleUsableSatelites sats = intent.getParcelableExtra("Satellite status");
        t1.setSateliteStatus(sats);
        t2.updateChart(sats);

        // enable/disable start log button according to number of usable satellites
        // only when log is not on
        if (!serviceOn) {
            if (sats.usableInTotal < 0) {
                startButton.setEnabled(false);
            }
            else {
                startButton.setEnabled(true);
            }
        }
    }


    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    /** Slide to other activities. */
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchevent.getX();
                y2 = touchevent.getY();

                //swipe right
                if (x1 > x2){
                    Intent i = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }

                //swipe left
                if (x1 < x2) {
                    Intent i = new Intent(MainActivity.this, FilesActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        onPause();
        moveTaskToBack(true);
    }

    public void goToFiles(View view) {
        Intent i = new Intent(MainActivity.this, FilesActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void goToMap(View view) {
        Intent i = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return t1;
                case 1:
                    return t2;

                default:
                    return null;

            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}