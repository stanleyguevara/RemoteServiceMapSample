package com.example.service;

import android.os.Handler;
import android.os.Message;

public class IncomingHandler extends Handler {
    private LocationService service;

    IncomingHandler(LocationService service) {
        this.service = service;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case LocationService.MSG_REGISTER:
                service.client = msg.replyTo;
                service.sendPoint();
                break;
            case LocationService.MSG_UNREGISTER:
                service.client = null;
                break;
            case LocationService.MSG_STOP:
                service.stop();
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
