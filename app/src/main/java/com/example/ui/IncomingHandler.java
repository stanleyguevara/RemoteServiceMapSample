package com.example.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.service.LocationService;
import com.example.util.Point;

public class IncomingHandler extends Handler {
    private ServiceManager manager;

    public IncomingHandler(ServiceManager manager) {
        this.manager = manager;
    }

    @Override
    public void handleMessage(Message msg) {
        if(manager != null) {
            Bundle incoming;
            Point point;
            switch (msg.what) {
                case LocationService.RESPONSE_POINT:
                    incoming = msg.getData();
                    point = (Point) incoming.getSerializable(LocationService.BUNDLE_POINT);
                    long scheduled = incoming.getLong(LocationService.BUNDLE_SCHEDULE);
                    manager.onNewPoint(point, scheduled);
                    break;
                case LocationService.RESPONSE_POINT_UPDATE:
                    incoming = msg.getData();
                    point = (Point) incoming.getSerializable(LocationService.BUNDLE_POINT);
                    manager.onPointUpdate(point);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
