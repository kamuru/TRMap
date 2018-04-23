package com.questlove.kamuru.trmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapView;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    private static final String TKey = "ad651374-6e25-4c59-b096-a83c089fb8b1";
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;

    private TMapView mapView;
    private boolean trackingMode = false;
    private Location lastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initMap();
    }

    private void initMap() {
        mapView = new TMapView(this);
        mapView.setSKTMapApiKey(TKey);

        new TMapGpsManager(this).OpenGps();

        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.map_view);
        viewGroup.addView(mapView);
    }

    private void requestPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            boolean shouldShowRequest = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (shouldShowRequest) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
            }
        }
    }

    public void zoomIn(View view) {
        mapView.MapZoomIn();
    }

    public void zoomOut(View view) {
        mapView.MapZoomOut();
    }

    public void myLocation(View view) {
        mapView.setLocationPoint(lastLocation.getLongitude(), lastLocation.getLatitude());
        mapView.setCenterPoint(lastLocation.getLongitude(), lastLocation.getLatitude(), true);
        mapView.setIconVisibility(true);
    }

    public void search(View view) {
        EditText editText = (EditText) findViewById(R.id.search_text);
        TMapData mapData = new TMapData();

        try {
            for (TMapPOIItem item : mapData.findAllPOI(editText.getText().toString())) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChange(Location location) {
        lastLocation = location;
    }
}
