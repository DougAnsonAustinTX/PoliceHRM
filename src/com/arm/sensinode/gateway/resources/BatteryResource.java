package com.arm.sensinode.gateway.resources;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.sensinode.coap.MediaTypes;
import com.sensinode.coap.exception.CoapException;
import com.sensinode.coap.server.CoapServer;
import com.sensinode.coap.utils.AbstractObservableResource;
import com.sensinode.coap.Code;
import com.sensinode.coap.exception.CoapCodeException;
import com.sensinode.coap.server.CoapExchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatteryResource extends AbstractObservableResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(BatteryResource.class);

    private final Context context;
    private String current_level = "-1%";
    private BroadcastReceiver battery_receiver;
    private boolean receiver_enabled = false;

    public BatteryResource(Context context, CoapServer server) {
        super(server);
        this.context = context;

        // Set the link attributes for this resource
        //this.getLink().setResourceType("ucum:s");
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);

        // Turn on the battery receiver
        startBatteryReceiver();

        /*ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                nt();
            }
        }, 10, 10, TimeUnit.SECONDS);*/
    }

    private void nt() {
        Thread nt = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyChange(current_level.getBytes(), MediaTypes.CT_TEXT_PLAIN, true);
                    LOGGER.debug("Battery level update: " + current_level);
                } catch (CoapException ex) {
                    LoggerFactory.getLogger(BatteryResource.class).error(ex.getMessage(), ex);
                }
            }
        });
        nt.start();
    }

    @Override
    public void get(CoapExchange ex) throws CoapCodeException {
        ex.setResponseBody(current_level);
        ex.setResponseCode(Code.C205_CONTENT);
        ex.sendResponse();
    }

    private void startBatteryReceiver() {
        if (!isReceiverRunning()) {
            receiver_enabled = true;
            battery_receiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    int raw_level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int level = -1;
                    if (raw_level >= 0 && scale > 0) {
                        level = (raw_level * 100) / scale;
                    }
                    current_level = String.format("%d%%", level);
                    // Push update to Sensinode
                    nt();
                }
            };
            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(battery_receiver, batteryLevelFilter);
        }
    }

    public void stopBatteryReceiver() {
        if (isReceiverRunning()) {
            context.unregisterReceiver(battery_receiver);
            receiver_enabled = false;
        }
    }

    public boolean isReceiverRunning() {
        return receiver_enabled;
    }

}