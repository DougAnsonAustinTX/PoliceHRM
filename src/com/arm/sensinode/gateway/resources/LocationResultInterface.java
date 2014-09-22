package com.arm.sensinode.gateway.resources;

import android.content.Context;
import android.location.Location;

public interface LocationResultInterface {
    public void gotLocation(Location location);
    public Context getMyContext();
}
