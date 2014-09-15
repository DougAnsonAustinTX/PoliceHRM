package com.arm.sensinode.gateway.resources;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.arm.sensinode.gateway.SensinodeService;
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
	public static String BATTERY_RESOURCE_NAME = "/dev/bat";
	 
    private final static Logger LOGGER = LoggerFactory.getLogger(BatteryResource.class);

    private final Context context;
    private String current_level = "-1%";
    private BroadcastReceiver battery_receiver;
    private boolean receiver_enabled = false;
    
    // HACK
    private SensinodeService m_service = null;

    public BatteryResource(Context context, CoapServer server) {
    	super(server);
        this.context = context;
 
        // Set the link attributes for this resource
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);
    }
    
    public void setService(SensinodeService service) { this.m_service = service; }

    // HACK force NSP to accept the new Battery value immediately... notifyChange() is way to slow... takes almost 60 seconds.
    public void publishBatteryValue(String hrm_value) {
    	if (this.m_service != null) {
    		LOGGER.debug("Sending Battery value through PUT directly...");
    		String url = this.m_service.buildResourceURL(this.m_service.getEndpoint(), BatteryResource.BATTERY_RESOURCE_NAME);
            boolean success = this.m_service.putResourceValue(url,current_level);
            if (success) LOGGER.debug("Battery value PUT successful...");
            else LOGGER.debug("Battery value PUT FAULED...");
    	}
    }
    
    private void nt() {
        Thread nt = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyChange(current_level.getBytes(), MediaTypes.CT_TEXT_PLAIN, true);
                    LOGGER.debug("Battery value: " + current_level);
                    publishBatteryValue(current_level);
                } catch (CoapException ex) {
                    LoggerFactory.getLogger(HRMResource.class).error(ex.getMessage(), ex);
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