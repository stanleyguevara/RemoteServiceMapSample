package com.example.service;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.example.util.Point;
import com.example.util.PointListener;

import java.io.IOException;
import java.util.List;

/**
 * Geocoder happens to have timeout well below 30s so we're not implementing timeout for this task.
 */
public class GeocodeTask extends AsyncTask<Point, Integer, Point> {

    private PointListener listener;
    private Geocoder geocoder;

    public GeocodeTask(PointListener listener, Geocoder geocoder) {
        this.listener = listener;
        this.geocoder = geocoder;
    }

    @Override
    protected Point doInBackground(Point... points) {
        Point point = points[0];
        try {
            List<Address> addresses = geocoder.getFromLocation(point.lat, point.lng, 1);
            String address = Point.NO_INFO;
            if(addresses.size() > 0) address = addresses.get(0).getAddressLine(0);
            point.info = address;
            return point;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Point point) {
        super.onPostExecute(point);
        if(listener != null && point != null) listener.onPointUpdate(point);
    }
}
