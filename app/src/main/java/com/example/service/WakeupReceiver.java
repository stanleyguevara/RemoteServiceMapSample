package com.example.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WakeupReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent wakeful = new Intent(context, LocationService.class);
        String action = intent.getAction();
        wakeful.setAction(action);
        switch (action) {
            case LocationService.ACTION_WAKEUP:
                startWakefulService(context, wakeful);
                break;
        }
    }

}
