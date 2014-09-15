package com.arm.sensinode.gateway.resources;

import com.arm.sensinode.gateway.SensinodeService;
import com.sensinode.coap.MediaTypes;
import com.sensinode.coap.Code;
import com.sensinode.coap.exception.CoapCodeException;
import com.sensinode.coap.server.CoapExchange;
import com.sensinode.coap.utils.CoapResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufacturerResource extends CoapResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(ModelResource.class);

    public ManufacturerResource() {
        // Set the link attributes for this resource
        //this.getLink().setResourceType("ucum:s");
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);
    }

    @Override
    public void get(CoapExchange ex) throws CoapCodeException {
    	ex.setResponseBody(SensinodeService.DEFAULT_MFG_INFO);
        ex.setResponseCode(Code.C205_CONTENT);
        ex.getResponseHeaders().setContentType(MediaTypes.CT_TEXT_PLAIN);
        ex.sendResponse();
    }

}