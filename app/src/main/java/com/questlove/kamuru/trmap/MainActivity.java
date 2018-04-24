package com.questlove.kamuru.trmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapMarkerItem2;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    private static final String TKey = "ad651374-6e25-4c59-b096-a83c089fb8b1";
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 1;

    private Toolbar toolbar;
    private TMapView tmapView;
    private boolean trackingMode = false;
    private Location lastLocation;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initMap();
    }

    private void initMap() {
        tmapView = new TMapView(this);
        tmapView.setSKTMapApiKey(TKey);
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.map_view);
        viewGroup.addView(tmapView);

        tmapView.setOnMarkerClickEvent(new TMapView.OnCalloutMarker2ClickCallback() {
            @Override
            public void onCalloutMarker2ClickEvent(String s, TMapMarkerItem2 tMapMarkerItem2) {

            }
        });

        tmapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> markerlist, ArrayList<TMapPOIItem> poilist, TMapPoint tMapPoint, PointF pointF) {
//                Toast.makeText(MainActivity.this, tMapPoint.getLatitude() + " " + tMapPoint.getLongitude(), Toast.LENGTH_SHORT).show();
                Log.d(null, tMapPoint.getLatitude() + " " + tMapPoint.getLongitude());
                hideIME();
                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> markerlist, ArrayList<TMapPOIItem> poilist, TMapPoint tMapPoint, PointF pointF) {
                return false;
            }
        });
        tmapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint) {
                Toast.makeText(MainActivity.this, "long pressed", Toast.LENGTH_SHORT).show();

                tmapView.removeAllMarkerItem();
            }
        });

        new TMapGpsManager(this).OpenGps();
    }

    private void hideIME() {
        EditText editText = (EditText) findViewById(R.id.search_text);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
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
        tmapView.MapZoomIn();
    }

    public void zoomOut(View view) {
        tmapView.MapZoomOut();
    }

    public void myLocation(View view) {
        tmapView.setCenterPoint(lastLocation.getLongitude(), lastLocation.getLatitude(), true);
        tmapView.setLocationPoint(lastLocation.getLongitude(), lastLocation.getLatitude());
        tmapView.setIconVisibility(true);
    }

    public void search(View view) {
        tmapView.removeAllMarkerItem();

        EditText editText = (EditText) findViewById(R.id.search_text);
        TMapData mapData = new TMapData();

        double minLat = 99999999;
        double minLon = 99999999;
        double maxLat = 0;
        double maxLon = 0;

        try {
            ArrayList<TMapPOIItem> poiList = mapData.findAllPOI(editText.getText().toString());
            for (TMapPOIItem poi : mapData.findAllPOI(editText.getText().toString())) {
                double lat = Double.parseDouble(poi.noorLat);
                double lon = Double.parseDouble(poi.noorLon);

                TMapPoint point = new TMapPoint(lat, lon);
                TMapMarkerItem markerItem = new TMapMarkerItem();

                Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.poi_dot);
                markerItem.setTMapPoint(point);
                markerItem.setName(poi.name);
                markerItem.setVisible(markerItem.VISIBLE);
                markerItem.setIcon(bitmap);

                bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.i_go);
                markerItem.setCalloutTitle(poi.name);
                markerItem.setCalloutSubTitle("sub");
                markerItem.setCanShowCallout(true);
//                markerItem.setAutoCalloutVisible(true);
                markerItem.setCalloutRightButtonImage(bitmap);

                tmapView.addMarkerItem(poi.name, markerItem);

                minLon = minLon < lon ? minLon : lon;
                minLat = minLat < lat ? minLat : lat;
                maxLon = maxLon > lon ? maxLon : lon;
                maxLat = maxLat > lat ? maxLat : lat;
            }

            if (poiList.size() == 0) {
                return;
            } else if (poiList.size() == 1) {
                tmapView.setCenterPoint(minLon, maxLat, true);
                tmapView.zoomToTMapPoint(new TMapPoint(maxLat, minLon), new TMapPoint(maxLat, minLon));
            } else {
                tmapView.setCenterPoint((minLon + maxLon) / 2, (minLat + maxLat) / 2, true);
                tmapView.zoomToTMapPoint(new TMapPoint(maxLat, minLon), new TMapPoint(minLat, maxLon));
            }

            hideIME();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChange(Location location) {
        lastLocation = location;
    }
}
