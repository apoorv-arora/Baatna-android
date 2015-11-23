package com.application.baatna.utils;

import android.location.Location;

public interface BaatnaLocationCallback {
	public void onCoordinatesIdentified(Location loc);
	public void onLocationIdentified();
	public void onLocationNotIdentified();
	public void onDifferentCityIdentified();
	public void locationNotEnabled();
	public void onLocationTimedOut();
	public void onNetworkError();
}
