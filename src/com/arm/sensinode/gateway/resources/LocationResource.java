package com.arm.sensinode.gateway.resources;

import android.content.Context;
import android.location.Location;

import com.sensinode.coap.Code;
import com.sensinode.coap.MediaTypes;
import com.sensinode.coap.exception.CoapCodeException;
import com.sensinode.coap.exception.CoapException;
import com.sensinode.coap.server.CoapExchange;
import com.sensinode.coap.server.CoapServer;
import com.sensinode.coap.utils.AbstractObservableResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arm.sensinode.gateway.SensinodeService;
import com.google.android.gms.location.LocationListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocationResource extends AbstractObservableResource implements LocationListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(LocationResource.class);
    
    // Location Resource
    private String last_location = SensinodeService.DEFAULT_LOCATION_COORDS; 
 
    // Minimum update interval (in seconds)
    private final int UPDATE_INTERVAL = 10;

    public LocationResource(Context context, CoapServer server) {
        super(server);

        // Set the link attributes for this resource
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                nt();
            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    private void nt() {
        Thread nt = new Thread(new Runnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                try {
                    notifyChange(last_location.getBytes(), MediaTypes.CT_TEXT_PLAIN, true);
                    LOGGER.debug("Last location update: " + last_location);
                } catch (CoapException ex) {
                    LoggerFactory.getLogger(LocationResource.class).error(ex.getMessage(), ex);
                }
            }
        });
        nt.start();
    }

    @Override
    public void get(CoapExchange coapExchange) throws CoapCodeException {
        coapExchange.setResponseBody(String.valueOf(last_location).getBytes());
        coapExchange.setResponseCode(Code.C205_CONTENT);
        coapExchange.sendResponse();
    }

	@Override
	public void onLocationChanged(Location arg0) {
		// fixed
		;
	}
}
