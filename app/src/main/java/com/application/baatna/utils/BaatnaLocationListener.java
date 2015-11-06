package com.application.baatna.utils;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.application.baatna.BaatnaApp;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class BaatnaLocationListener implements com.google.android.gms.location.LocationListener,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

	private ArrayList<BaatnaLocationCallback> callbacks;
	private BaatnaApp zapp;
	public boolean forced = false;

	private GoogleApiClient mGoogleApiClient;

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

	/**
	 * Called when the location has changed.
	 * <p/>
	 * <p> There are no restrictions on the use of the supplied Location object.
	 *
	 * @param loc The new location, as a Location object.
	 */
	@Override
	public void onLocationChanged(Location loc) {
		if(loc != null) {

			boolean callToBeFired = false;
			if(forced || CommonLib.distFrom(zapp.lat, zapp.lon, loc.getLatitude(), loc.getLongitude()) > .2) {
				zapp.lat = loc.getLatitude();
				zapp.lon = loc.getLongitude();
				callToBeFired = true;
			}

			zapp.interruptLocationTimeout();
			zapp.state = CommonLib.LOCATION_DETECTED;

			for (BaatnaLocationCallback callback : callbacks) {
				//zapp.currentCity = zapp.getCityIdFromLocation(loc);
				callback.onCoordinatesIdentified(loc);
			}

			CommonLib.ZLog("zll", "call to be fired " + forced);
//            if(zapp.locationManager != null) {
//                zapp.locationManager.removeUpdates(this);
//            }

		}

		if(zapp.locationManager != null) {
			zapp.locationManager.removeUpdates(this);
		}

		if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		}
	}

	/**
	 * Called when the provider status changes. This method is called when
	 * a provider is unable to fetch a location or if the provider has recently
	 * become available after a period of unavailability.
	 *
	 * @param provider the name of the location provider associated with this
	 *                 update.
	 * @param status   {@link android.location.LocationProvider#OUT_OF_SERVICE} if the
	 *                 provider is out of service, and this is not expected to change in the
	 *                 near future; {@link android.location.LocationProvider#TEMPORARILY_UNAVAILABLE} if
	 *                 the provider is temporarily unavailable but is expected to be available
	 *                 shortly; and {@link android.location.LocationProvider#AVAILABLE} if the
	 *                 provider is currently available.
	 * @param extras   an optional Bundle which will contain provider specific
	 *                 status variables.
	 *                 <p/>
	 *                 <p> A number of common key/value pairs for the extras Bundle are listed
	 *                 below. Providers that use any of the keys on this list must
	 *                 provide the corresponding value as described below.
	 *                 <p/>
	 *                 <ul>
	 *                 <li> satellites - the number of satellites used to derive the fix
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		CommonLib.ZLog("zll ", "onStatusChanged");
	}

	/**
	 * Called when the provider is enabled by the user.
	 *
	 * @param provider the name of the location provider associated with this
	 *                 update.
	 */
	@Override
	public void onProviderEnabled(String provider) {
		CommonLib.ZLog("zll ", "onProviderEnabled");
	}

	/**
	 * Called when the provider is disabled by the user. If requestLocationUpdates
	 * is called on an already disabled provider, this method is called
	 * immediately.
	 *
	 * @param provider the name of the location provider associated with this
	 *                 update.
	 */
	@Override
	public void onProviderDisabled(String provider) {
		CommonLib.ZLog("zll ", "onProviderDisabled");
	}

	public void getFusedLocation(BaatnaApp zapp) {
		PackageManager pm = zapp.getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)) {
			this.zapp = zapp;
			mGoogleApiClient = new GoogleApiClient.Builder(zapp)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onConnected(Bundle bundle) {

		CommonLib.ZLog("zll", "onConnected");
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

		if(mGoogleApiClient.isConnected()) {
			currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		}
		if(currentLocation != null) {
			onLocationChanged(currentLocation);
		} else {
			zapp.getAndroidLocation();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		CommonLib.ZLog("zll", "onConnectionFailed");
		zapp.getAndroidLocation();
	}

	public void locationNotEnabled() {
		CommonLib.ZLog("zll", "locationNotEnabled");
		if(forced) {
			zapp.state = CommonLib.LOCATION_NOT_ENABLED;
			zapp.lat = 0;
			zapp.lon = 0;
			zapp.location = "";
			for (BaatnaLocationCallback callback : callbacks)
				callback.locationNotEnabled();
		}
	}

	public void interruptProcess() {
		CommonLib.ZLog("zll", "interupptProcess");

		zapp.state = CommonLib.LOCATION_NOT_DETECTED;
		zapp.lat = 0;
		zapp.lon = 0;
		zapp.location = "";
		zapp.locationManager.removeUpdates(this);
		for (BaatnaLocationCallback callback : callbacks) {
			callback.onLocationTimedOut();
		}
	}

}