package com.example.michael.gnssagent.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.example.michael.gnssagent.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends BaseActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        PopupMenu.OnMenuItemClickListener {

    private GoogleMap mMap;

    private static int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private static float currentZoom = 15;

    private Marker currentMarker = null;

    private static LatLng currentLocation = new LatLng(49.9142392, 14.7871981);

    private ImageButton centerButton;
    private ImageButton layerButton;

    private BroadcastReceiver receiveUpdateFromLogService;

    private boolean userMapMoveFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // disable back arrow on the action bar
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } catch (NullPointerException e) {

        }

        centerButton = findViewById(R.id.centeringButton);
        layerButton = findViewById(R.id.layersButton);

        centerButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    centerButton.setBackgroundResource(R.drawable.img_button_clicked);
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    centerMap(view);
                    centerButton.setBackgroundResource(R.drawable.img_button_border);
                }
                return true;
            }
        });

        layerButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    layerButton.setBackgroundResource(R.drawable.img_button_clicked);
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    setMapLayer(view);
                    layerButton.setBackgroundResource(R.drawable.img_button_border);
                }
                return true;
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpBroadcastReceiver();
    }

    public void setUpBroadcastReceiver(){
        // set up broadcast receiver receiving updates from LogService
        receiveUpdateFromLogService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // get location updates
                Location loc = intent.getExtras().getParcelable("location");
                if (loc != null) {
                    updateLocation(new LatLng(loc.getLatitude(), loc.getLongitude()));
                }
            }
        };

        // register broadcast receiver receiving updates from LogService
        LocalBroadcastManager
                .getInstance(MapsActivity.this)
                .registerReceiver(receiveUpdateFromLogService, new IntentFilter("LOCATION"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveCanceledListener(this);

        BitmapDescriptor blueDot = BitmapDescriptorFactory.fromResource(R.drawable.bullet_blue);
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(blueDot)
                .position(currentLocation)
                .anchor(0.5f,0.5f);
        currentMarker = mMap.addMarker(markerOptions);
        mMap.setMapType(mapType);

        // Move the camera to Pecny
        CameraUpdate myPos = CameraUpdateFactory.newLatLngZoom(currentLocation, currentZoom);
        mMap.moveCamera(myPos);

//        updateLocation(currentLocation);

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (userMapMoveFlag) {
                    currentZoom = mMap.getCameraPosition().zoom;
                }
//                Toast.makeText(getApplicationContext(),"agih",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void updateLocation(LatLng loc) {

        currentLocation = loc;
        // put marker on position
        try {
            currentMarker.setPosition(currentLocation);
        } catch (NullPointerException e) {
            // do nothing
        }

        // move camera if map is centered
        if (!userMapMoveFlag) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));

            CameraUpdate myPos = CameraUpdateFactory.newLatLngZoom(currentLocation, currentZoom);
            mMap.animateCamera(myPos);

        }
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
//            Toast.makeText(this, "GESTURE", Toast.LENGTH_LONG).show();
            userMapMoveFlag = true;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {
//            Toast.makeText(this, "API", Toast.LENGTH_LONG).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
//            Toast.makeText(this, "DEVELOPER",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCameraMove() {
    }

    @Override
    public void onCameraMoveCanceled() {
    }

    @Override
    public void onCameraIdle() {
    }

    public void centerMap(View view) {
        userMapMoveFlag = false;
        if (currentZoom < 10)
        {
            currentZoom = 15;
        }
        updateLocation(currentLocation);
    }

    public void setMapLayer(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.layer_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.default_map:
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                mMap.setMapType(mapType);
                return true;
            case R.id.satellite_map:
                mapType = GoogleMap.MAP_TYPE_SATELLITE;
                mMap.setMapType(mapType);
                return true;
            case R.id.terrain_map:
                mapType = GoogleMap.MAP_TYPE_TERRAIN;
                mMap.setMapType(mapType);
            case R.id.hybrid_map:
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                mMap.setMapType(mapType);
            default:
                return false;
        }
    }

    public void goToMain(View view) {
        onBackPressed();
    }
}
