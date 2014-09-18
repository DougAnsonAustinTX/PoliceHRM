package com.arm.sensinode.gateway.resources;

import android.content.Context;

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

public class HRMResource extends AbstractObservableResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(HRMResource.class);
    
    public static String HRM_RESOURCE_NAME = "/dev/hrm";

    private String HRM_value = "0";       	 // HRM value - default is OFFLINE
     
    // HACK
    private SensinodeService m_service = null;

    public HRMResource(Context context, CoapServer server) {
        super(server);
 
        // Set the link attributes for this resource
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);
    }
    
    public void setService(SensinodeService service) { this.m_service = service; }

    // HACK force NSP to accept the new HRM value immediately... notifyChange() is way to slow... takes almost 60 seconds.
    public void publishHRMValue(String hrm_value) {
    	if (this.m_service != null) {
    		LOGGER.debug("Sending HRM value through PUT directly...");
    		String url = this.m_service.buildResourceURL(this.m_service.getEndpoint(), HRMResource.HRM_RESOURCE_NAME);
            boolean success = this.m_service.putResourceValue(url,hrm_value);
            if (success) LOGGER.debug("HRM value PUT successful...");
            else LOGGER.debug("HRM value PUT FAULED...");
    	}
    }
    
    private void nt() {
        Thread nt = new Thread(new Runnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                try {
                    notifyChange(HRM_value.getBytes(), MediaTypes.CT_TEXT_PLAIN, true);
                    LOGGER.debug("HRM value: " + HRM_value);
                    publishHRMValue(HRM_value);
                } catch (CoapException ex) {
                    LoggerFactory.getLogger(HRMResource.class).error(ex.getMessage(), ex);
                }
            }
        });
        nt.start();
    }

    @Override
    public void get(CoapExchange ex) throws CoapCodeException {
        ex.setResponseBody(HRM_value);
        ex.setResponseCode(Code.C205_CONTENT);
        ex.sendResponse();
    }
    
    // this gets called whenver the HRM is pressed
    public void updateHRMValue(int hrm_value) {
       HRM_value = "" + hrm_value;
       nt();
    }

    public void stopHRMReceiver() {
        // not used
        ;
    }

    public boolean isReceiverRunning() {
        // not used
        return false;
    }

}