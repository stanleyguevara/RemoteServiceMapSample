package com.example.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.R;
import com.example.util.Point;
import com.example.util.PointListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, OnStreetViewPanoramaReadyCallback, PointListener {

    private static final float ZOOM = 15f;
    private GoogleMap map;
    private StreetViewPanorama street;
    private Marker marker;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        StreetViewPanoramaFragment streetViewFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.streetview);
        streetViewFragment.getStreetViewPanoramaAsync(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setTitle("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        ServiceManager manager = ServiceManager.getInstance(this);
        manager.bind(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ServiceManager manager = ServiceManager.getInstance(this);
        manager.unbind();
    }

    @Override
    public void onNewPoint(Point point, long scheduled) {
        showPoint(point);
        countdown(scheduled);
    }

    @Override
    public void onPointUpdate(Point point) {
        if(map != null && marker != null) {
            marker.setTitle(point.info);
            marker.showInfoWindow();
        }
    }

    private void showPoint(Point point) {
        LatLng place = new LatLng(point.lat, point.lng);
        if(map != null) {
            if(marker != null) marker.remove();
            marker = map.addMarker(new MarkerOptions().position(place).title(point.info));
            if(!Point.NO_INFO.equals(point.info)) marker.showInfoWindow();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, ZOOM));
        }
        if(street != null) street.setPosition(place);
    }

    private void countdown(final long scheduled) {
        ActionBar bar = getSupportActionBar();
        if(bar != null) {
            long now = System.currentTimeMillis();
            long diff = scheduled - now;
            if(diff > 0) {
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff+500);
                bar.setTitle(String.format("%s s to next", seconds));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countdown(scheduled);
                    }
                }, 1000);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetView) {
        street = streetView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                ServiceManager manager = ServiceManager.getInstance(this);
                manager.kill();
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
