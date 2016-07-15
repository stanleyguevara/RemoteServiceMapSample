package com.example.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.example.service.LocationService;
import com.example.util.Point;
import com.example.util.PointListener;

/**
 * Singleton.
 */
public class ServiceManager implements PointListener {

    private static volatile ServiceManager INSTANCE;

    private final Context context;
    private PointListener listener;
    private Point current;

    private Messenger service = null;
    private boolean isBound = false;
    private final Messenger messenger = new Messenger(new IncomingHandler(this));
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ServiceManager.this.service = new Messenger(service);
            try {
                isBound = true;
                Message msg = Message.obtain(null, LocationService.MSG_REGISTER);
                msg.replyTo = messenger;
                ServiceManager.this.service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            ServiceManager.this.service = null;
            isBound = false;
        }
    };

    private ServiceManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static ServiceManager getInstance(Context context) {
        if(INSTANCE == null) {
            synchronized (ServiceManager.class) {
                if(INSTANCE == null) {
                    INSTANCE = new ServiceManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void bind(PointListener listener) {
        this.listener = listener;
        if(!isBound) {
            Intent intent = new Intent(context, LocationService.class);
            intent.setAction(LocationService.ACTION_START);
            context.startService(intent);
            context.bindService(intent, connection, Context.BIND_IMPORTANT | Context.BIND_DEBUG_UNBIND);
        }
    }

    @Override
    public void onNewPoint(Point point, long scheduled) {
        if(listener != null) {
            current = point;
            listener.onNewPoint(point, scheduled);
        }
    }

    @Override
    public void onPointUpdate(Point point) {
        if(listener != null) {
            if(current != null && point.index == current.index) {
                listener.onPointUpdate(point);
            }
        }
    }

    public void unbind() {
        listener = null;
        if(isBound) {
            if(service != null) {
                try {
                    Message msg = Message.obtain(null, LocationService.MSG_UNREGISTER);
                    msg.replyTo = messenger;
                    service.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            context.unbindService(connection);
            isBound = false;
        }
    }

    public void kill() {
        if(isBound) {
            if(service != null) {
                try {
                    Message msg = Message.obtain(null, LocationService.MSG_STOP);
                    msg.replyTo = messenger;
                    service.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            context.unbindService(connection);
            isBound = false;
        }
    }
}
