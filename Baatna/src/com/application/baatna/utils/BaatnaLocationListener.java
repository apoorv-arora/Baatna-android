package com.application.baatna.utils;

import java.util.ArrayList;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.application.baatna.BaatnaApp;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class BaatnaLocationListener implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

	private ArrayList<BaatnaLocationCallback> callbacks;
	private BaatnaApp zapp;
	public boolean forced = false;
	
	// A request to connect to Location Services
    //private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
	
	public BaatnaLocationListener(BaatnaApp zapp) {
		callbacks = new ArrayList<BaatnaLocationCallback>();
		this.zapp = zapp;
	}

	public void addCallback(BaatnaLocationCallback callback) {
		callbacks.add(callback);
	}
	
	public void removeCallback(BaatnaLocationCallback callback) {
		if(callbacks.contains(callback))
			callbacks.remove(callback);
	}

	public void locationNotEnabled() {				
		
		if(forced) {
			zapp.lat = 0;
			zapp.lon = 0;
			zapp.location = "";
			for (BaatnaLocationCallback callback : callbacks)
				callback.locationNotEnabled();
		}
	}

	public void interruptProcess() {				
		
			zapp.lat = 0;
			zapp.lon = 0;
			zapp.location = "";
			zapp.locationManager.removeUpdates(this);
			for (BaatnaLocationCallback callback : callbacks) {
				callback.onLocationTimedOut();
			}
	}
	
	public void onLocationChanged(Location loc) {				
		
		if(loc != null) {

			if(forced || CommonLib.distFrom(zapp.lat, zapp.lon, loc.getLatitude(), loc.getLongitude()) > .2) {
				zapp.lat = loc.getLatitude();
				zapp.lon = loc.getLongitude();
			}
			zapp.interruptLocationTimeout();

			for (BaatnaLocationCallback callback : callbacks) {
				//	if(forced || (!forced && callToBeFired)) 
				callback.onCoordinatesIdentified(loc);
			}
			
		}
		
		if(zapp.locationManager != null) {
			zapp.locationManager.removeUpdates(this);
		}
		
		if(mLocationClient != null && mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
	}
	
	public void onProviderDisabled(String arg0) {
		CommonLib.ZLog("zll ", "onProviderDisabled");
		
	}

	public void onProviderEnabled(String arg0) {
		
		CommonLib.ZLog("zll ", "onProviderEnabled");
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
		CommonLib.ZLog("zll ", "onStatusChanged");
	}

	/**
	 * Get the current zone and city given the coordinates from the server in the background. 
	 */
	
	
	public void getFusedLocation(BaatnaApp zapp) {
		PackageManager pm = zapp.getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
			this.zapp = zapp;
			mLocationClient = new LocationClient(zapp.getApplicationContext(), this, this);
			mLocationClient.connect();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		
		zapp.getAndroidLocation();
		
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
		
		/*
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);


            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // ZTODO: If no resolution is available, display a dialog to the user with the error.
            
        }
		*/
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Location currentLocation = null;
		
		try {
			PackageManager pm = zapp.getPackageManager();
			if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION) 
					|| pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) 
					|| pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)) {
			} else {
				return;
			}
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
			
		if(mLocationClient.isConnected())
			currentLocation = mLocationClient.getLastLocation();
		
		if(currentLocation != null) {
			onLocationChanged(currentLocation);
		
		} else {
			zapp.getAndroidLocation();
			
		}
	}

	@Override
	public void onDisconnected() {
		
		
	}
	
}	