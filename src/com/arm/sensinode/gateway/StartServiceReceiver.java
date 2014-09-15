package com.arm.sensinode.gateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SensinodeService.class);
        service.putExtra("enabled", true);
        context.startService(service);
    }
}