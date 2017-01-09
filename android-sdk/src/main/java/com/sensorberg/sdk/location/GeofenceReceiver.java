package com.sensorberg.sdk.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.sensorberg.sdk.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.sensorberg.sdk.receiver.GEOFENCE";

    private Context context;
    private GeofenceManager manager;

    private List<GeofenceManager.GeofenceListener> listeners = new ArrayList<>();

    public GeofenceReceiver(Context context, GeofenceManager manager) {
        this.context = context;
        this.manager = manager;
        registerReceiver();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null) {
            Logger.log.geofenceError("GeofencingEvent is null", null);
            return;
        }
        if (event.hasError() && event.getErrorCode() == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            //This runs in a case of e.g. disabling location on the device.
            //If we've registred geofence before, Google Play Service lets us know about removal here.
            //(But we don't rely only on it, cause we're smart and listen to disabling location anyway)
            manager.setRegistred(false);
            return;
        }
        try {
            List<GeofenceData> geofenceDatas = GeofenceData.from(event);
            boolean entry = event.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER;
            for (GeofenceData geofenceData : geofenceDatas) {
                notifyListeners(geofenceData, entry);
            }
        } catch (IllegalArgumentException ex) {
            Logger.log.geofenceError("GeofencingEvent is invalid", ex);
        }
    }

    public void addListener(GeofenceManager.GeofenceListener listener) {
        for (GeofenceManager.GeofenceListener previous : listeners) {
            if(previous == listener) {
                return;
            }
        }
        if (listeners.size() == 0) {
            registerReceiver();
        }
        listeners.add(listener);
    }

    public void removeListener(GeofenceManager.GeofenceListener listener) {
        Iterator<GeofenceManager.GeofenceListener> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            GeofenceManager.GeofenceListener existing = iterator.next();
            if(existing == listener) {
                iterator.remove();
                if (listeners.size() == 0) {
                    context.unregisterReceiver(this);
                }
                return;
            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        context.registerReceiver(this, filter);
    }

    private void notifyListeners(GeofenceData geofenceData, boolean entry) {
        for (GeofenceManager.GeofenceListener listener : listeners) {
            listener.onGeofenceEvent(geofenceData, entry);
        }
    }
}