package com.arm.sensinode.gateway.resources;

import com.sensinode.coap.Code;
import com.sensinode.coap.MediaTypes;
import com.sensinode.coap.exception.CoapCodeException;
import com.sensinode.coap.server.CoapExchange;
import com.sensinode.coap.server.CoapServer;
import com.sensinode.coap.utils.AbstractObservableResource;

public class StringObservableResource extends AbstractObservableResource {
	private String mResourceName = null;
	private String mResourceValue = null;
        
    // defaulted content type: plain text
	public StringObservableResource(String resourceName,String resourceValue,CoapServer server) {
    	super(server);
    	this.mResourceName = resourceName;
    	this.mResourceValue = resourceValue;
 
        // Set the link attributes for this resource
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);
        this.getLink().set(this.mResourceValue, this.mResourceValue);
    }

    @Override
    public void get(CoapExchange ex) throws CoapCodeException {
        ex.setResponseBody(this.mResourceValue);
        ex.setResponseCode(Code.C205_CONTENT);
        ex.sendResponse();
    }
    
    // getters and setters
    public String name() { return this.mResourceName; }
    public String value() { return this.mResourceValue; }
}
