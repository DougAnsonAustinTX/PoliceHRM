package com.arm.sensinode.gateway.resources;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocManager {
	private Timer timer1 = null;
	private LocationManager lm = null;
	private LocationResultInterface locationResult = null;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;
    
    public boolean getLocation(Context context, LocationResultInterface result)
    {
    	boolean status = true;
    	
        // record our caller for later use
        this.locationResult=result;
        
        // get the location manager
        if (this.lm == null) lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try { this.gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); } catch(Exception ex) { this.gps_enabled = false; }
        try { this.network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER); } catch(Exception ex) { this.network_enabled = false; }

        // if neither provider is enabled, return
        if (!this.gps_enabled && !this.network_enabled) {
        	status = false;
        }
        else {
	        // request location from GPS if enabled
	        if(this.gps_enabled) this.lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.locationListenerGps);
	        
	        // request location from Network if enabled
	        if(this.network_enabled) this.lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this.locationListenerNetwork);
	        
	        // setup the timer to query
	        this.timer1 = new Timer();
	        this.timer1.schedule(new GetLastLocation(), 0, 2000);
        }
        
        // return our status
        return status;
    }

    // GPS listener
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
            locationResult.gotLocation(location);
        }
        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };

    // Network listener
    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);
            locationResult.gotLocation(location);
        }
        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };
    
    // Inner class that utilizes a timer to query location providers for current location
    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
        	 lm.removeUpdates(locationListenerGps);
             lm.removeUpdates(locationListenerNetwork);

             Location net_loc = null;
             Location gps_loc = null;
             
             // get last known location from GPS
             if(gps_enabled) gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             
             // get last known location from Network
             if(network_enabled) net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

             //if there are both values use the latest one
             if (gps_loc != null && net_loc!= null) {
                 if(gps_loc.getTime() > net_loc.getTime()) {
                	 this.cancel();
                     locationResult.gotLocation(gps_loc);
                 }
                 else {
                	 this.cancel();
                     locationResult.gotLocation(net_loc);
                 }
                 return;
             }
             else if (gps_loc != null) {
                 this.cancel();
                 locationResult.gotLocation(gps_loc);
                 return;
             }
             else if (net_loc != null) {
            	 this.cancel();
                 locationResult.gotLocation(net_loc);
                 return;
             }
             else {
	             this.cancel();
	             locationResult.gotLocation(null);
	             return;
             }
         }
     }
 }
