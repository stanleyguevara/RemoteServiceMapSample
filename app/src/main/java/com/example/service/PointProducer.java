package com.example.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.preference.PreferenceManager;

import com.example.util.Point;
import com.example.util.PointListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PointProducer {
    private static final long INTERVAL = 30000;
    private static final String KEY_CURRENT_POINT = "current_point";
    private static final int DEFAULT_CURRENT_POINT = 0;
    private Context context;
    private Geocoder geocoder;
    private SharedPreferences prefs;
    private AlarmManager alarm;
    private Point current;
    private int index;
    private long scheduled;

    static ArrayList<Point> places = new ArrayList<>();
    static {
        places.add(new Point(0, 52.400506, 16.906778, Point.NO_INFO));
        places.add(new Point(1, 52.394585, 16.927707, Point.NO_INFO));
        places.add(new Point(2, 52.408312, 16.934639, Point.NO_INFO));
        places.add(new Point(3, 52.410791, 16.934626, Point.NO_INFO));
        places.add(new Point(4, 52.421027, 16.935104, Point.NO_INFO));
        places.add(new Point(5, 52.400597, 16.993422, Point.NO_INFO));
    }

    private PointListener listener;

    public PointProducer(Context context, PointListener listener) {
        this.listener = listener;
        this.context = context;
        geocoder = new Geocoder(context, Locale.getDefault());
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        index = prefs.getInt(KEY_CURRENT_POINT, DEFAULT_CURRENT_POINT);
        current = places.get(index);
    }

    public void wakeup() {
        schedule();
        Point point = fetch();
        if(listener != null) listener.onNewPoint(point, scheduled);
        new GeocodeTask(listener, geocoder).execute(point);
    }

    public void cancel() {
        alarm.cancel(getWakeupIntent());
    }

    public Point getCurrentPoint() {
        return current;
    }

    public long getScheduledTime() {
        return scheduled;
    }

    private Point fetch() {
        if(index >= places.size()) index = 0;
        prefs.edit().putInt(KEY_CURRENT_POINT, index).apply();
        Point point = places.get(index);
        index++;
        current = point;
        return point;
    }

    private void schedule() {
        alarm.cancel(getWakeupIntent());
        PendingIntent pending = getWakeupIntent();
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long drift = 0;
        if(scheduled != 0) drift = now - scheduled;
        if(Math.abs(drift) < INTERVAL / 2 && scheduled != 0) {
            scheduled = scheduled + INTERVAL;
        } else {
            scheduled = now + INTERVAL;
        }
        cal.setTimeInMillis(scheduled);
        alarm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pending);
    }

    private PendingIntent getWakeupIntent() {
        Intent intent = new Intent(context, WakeupReceiver.class);
        intent.setAction(LocationService.ACTION_WAKEUP);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}