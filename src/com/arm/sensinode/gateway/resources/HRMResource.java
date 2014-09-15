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

    private final Context context;
    private String HRM_state = "0";       // not pressed
    private String previous_state = null;    // initialized
    
    // HACK
    private SensinodeService m_service = null;

    public HRMResource(Context context, CoapServer server) {
        super(server);
        this.context = context;
 
        // Set the link attributes for this resource
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);
    }
    
    public void setService(SensinodeService service) { this.m_service = service; }

    // HACK force NSP to accept the new HRM state immediately... notifyChange() is way to slow... takes almost 60 seconds.
    public void publishHRMState(String HRM_state) {
    	if (this.m_service != null) {
    		LOGGER.debug("Sending HRM state through PUT directly...");
    		String url = this.m_service.buildResourceURL(this.m_service.getEndpoint(), "/dev/panic");
            boolean success = this.m_service.putResourceValue(url,HRM_state);
            if (success) LOGGER.debug("HRM state PUT successful...");
            else LOGGER.debug("HRM state PUT FAULED...");
    	}
    }
    
    private void nt() {
        Thread nt = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    notifyChange(HRM_state.getBytes(), MediaTypes.CT_TEXT_PLAIN, true);
                    LOGGER.debug("HRM state: " + HRM_state);
                    publishHRMState(HRM_state);
                } catch (CoapException ex) {
                    LoggerFactory.getLogger(HRMResource.class).error(ex.getMessage(), ex);
                }
            }
        });
        nt.start();
    }

    @Override
    public void get(CoapExchange ex) throws CoapCodeException {
        ex.setResponseBody(HRM_state);
        ex.setResponseCode(Code.C205_CONTENT);
        ex.sendResponse();
    }
    
    // this gets called whenver the HRM is pressed
    public void HRMPressed() {
      // HRM pressed
      HRM_state = "1";
      
      // Push current state to Sensinode(only if changed)
      if (previous_state == null || previous_state.equalsIgnoreCase(HRM_state) == false) {
        nt();
        previous_state = HRM_state;
      }
    }
    
    // this gets called whenver the HRM is un-pressed
    public void HRMReset() {
      // HRM pressed
      HRM_state = "0";
      
      // Push current state to Sensinode
      if (previous_state == null || previous_state.equalsIgnoreCase(HRM_state) == false) {
        nt();
        previous_state = HRM_state;
      }
    }

    private void startHRMReceiver() {
        // not used
        ;
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