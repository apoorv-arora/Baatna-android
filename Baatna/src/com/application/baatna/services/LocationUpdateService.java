package com.application.baatna.services;

import com.application.baatna.BaatnaApp;
import com.application.baatna.utils.BaatnaLocationCallback;
import com.application.baatna.utils.UploadManager;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

public class LocationUpdateService extends IntentService implements BaatnaLocationCallback {

	private BaatnaApp zapp;
	public Context context;
	private SharedPreferences prefs;

	public LocationUpdateService() {
		super("LocationUpdateService");
		context = this;
		zapp = (BaatnaApp) getApplication();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//start location check in background
		if(zapp == null)
			zapp = (BaatnaApp) getApplication();
		zapp.zll.addCallback(this);
	}
	
	@Override
	public void onCoordinatesIdentified(Location loc) {
		if (loc != null) {
			UploadManager.updateLocation(prefs.getString("access_token", ""), loc.getLatitude(), loc.getLongitude());
		}
	}

	@Override
	public void onLocationIdentified() {
	}

	@Override
	public void onLocationNotIdentified() {
	}

	@Override
	public void onDifferentCityIdentified() {
	}

	@Override
	public void locationNotEnabled() {
	}

	@Override
	public void onLocationTimedOut() {
	}

	@Override
	public void onNetworkError() {
	}
	
	@Override
	public void onDestroy() {
		zapp.zll.removeCallback(this);
		super.onDestroy();
	}
}