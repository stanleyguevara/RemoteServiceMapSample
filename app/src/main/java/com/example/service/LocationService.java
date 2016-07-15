package com.example.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.example.R;
import com.example.ui.MapActivity;
import com.example.util.Point;
import com.example.util.PointListener;

public class LocationService extends Service implements PointListener {
    public static final String ACTION_START = "com.example.START";
    public static final String ACTION_WAKEUP = "com.example.WAKEUP";

    public static final int MSG_REGISTER = 1;
    public static final int MSG_UNREGISTER = 2;
    public static final int MSG_STOP = 3;

    public static final int RESPONSE_POINT = 10;
    public static final int RESPONSE_POINT_UPDATE = 11;

    public static final String BUNDLE_POINT = "point";
    public static final String BUNDLE_SCHEDULE = "schedule";

    private static final int NOTIF_ID = 1;

    private final String TAG = getClass().getName();

    final IncomingHandler handler = new IncomingHandler(this);
    final Messenger messenger = new Messenger(handler);
    private PowerManager.WakeLock lock;
    private PointProducer producer;
    Messenger client;

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        lock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        producer = new PointProducer(this, this);
        startAsForeground();
    }

    private void startAsForeground() {
        Notification.Builder builder = new Notification.Builder(this);
        Intent intent = new Intent(this, MapActivity.class);
        PendingIntent content = PendingIntent
                .getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(content)
                .setSmallIcon(R.drawable.ic_media_play)
                .setContentTitle("Foreground service")
                .setContentText("To elevate priority");
        Notification notification = builder.build();
        startForeground(NOTIF_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        producer.wakeup();
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            lock.acquire();
            if(intent == null) {
                //Resurrected by the system after low memory
                producer.wakeup();
            } else if (intent.getAction() != null) {
                String action = intent.getAction();
                switch (action) {
                    case LocationService.ACTION_WAKEUP:
                        producer.wakeup();
                        break;
                    case LocationService.ACTION_START:
                        //Nothing. Called to put service in started state.
                        break;
                }
            }
        } finally {
            lock.release();
        }
        return START_STICKY;
    }

    public void sendPoint() {
        onNewPoint(producer.getCurrentPoint(), producer.getScheduledTime());
    }

    @Override
    public void onNewPoint(Point point, long scheduled) {
        if(client != null) {
            try {
                Bundle bundle = new Bundle(1);
                bundle.putSerializable(BUNDLE_POINT, point);
                bundle.putLong(BUNDLE_SCHEDULE, scheduled);
                Message msg = Message.obtain(null, RESPONSE_POINT);
                msg.setData(bundle);
                client.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                client = null;
            }
        }
    }

    @Override
    public void onPointUpdate(Point point) {
        if(client != null) {
            try {
                Bundle bundle = new Bundle(1);
                bundle.putSerializable(BUNDLE_POINT, point);
                Message msg = Message.obtain(null, RESPONSE_POINT_UPDATE);
                msg.setData(bundle);
                client.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                client = null;
            }
        }
    }

    public void stop() {
        client = null;
        producer.cancel();
        stopForeground(true);
        stopSelf();
    }
}
