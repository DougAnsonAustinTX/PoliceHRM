package com.arm.sensinode.gateway.resources;

import android.content.Context;
import android.os.Build;

import com.sensinode.coap.MediaTypes;
import com.sensinode.coap.Code;
import com.sensinode.coap.exception.CoapCodeException;
import com.sensinode.coap.server.CoapExchange;
import com.sensinode.coap.utils.CoapResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelResource extends CoapResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(ModelResource.class);
    Context context;

    public ModelResource() {

        // Set the link attributes for this resource
        //this.getLink().setResourceType("ucum:s");
        this.getLink().setObservable(Boolean.TRUE);
        this.getLink().setContentType(MediaTypes.CT_TEXT_PLAIN);
    }

    @Override
    public void get(CoapExchange ex) throws CoapCodeException {
        ex.setResponseBody("Panic Button Gateway");
        ex.setResponseCode(Code.C205_CONTENT);
        ex.getResponseHeaders().setContentType(MediaTypes.CT_TEXT_PLAIN);
        ex.sendResponse();
    }

}