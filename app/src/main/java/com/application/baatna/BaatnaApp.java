package com.application.baatna;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.AsyncTask;

import com.application.baatna.db.MessageDBWrapper;
import com.application.baatna.services.LocationUpdateService;
import com.application.baatna.utils.BaatnaLocationListener;
import com.application.baatna.utils.CacheCleanerService;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.CommonLib.TrackerName;
import com.application.baatna.utils.LruCache;
import com.application.baatna.utils.PostWrapper;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class BaatnaApp extends Application {

	private String APPLICATION_ID = "";
	public BaatnaLocationListener zll = new BaatnaLocationListener(this);
	public LocationManager locationManager = null;
	public String location = "";
	public double lat = 0;
	public double lon = 0;
	public LruCache<String, Bitmap> cache;
	public boolean isNetworkProviderEnabled = false;
	public boolean isGpsProviderEnabled = false;
	public boolean firstLaunch = false;
	public int state = CommonLib.LOCATION_DETECTION_RUNNING;

	private CheckLocationTimeoutAsync checkLocationTimeoutThread;

	public void onCreate() {
		super.onCreate();

		SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		try {
			lat = Double.parseDouble(prefs.getString("lat1", "0"));
			lon = Double.parseDouble(prefs.getString("lon1", "0"));
		} catch (ClassCastException e) {
		} catch (Exception e) {
		}
		APPLICATION_ID = prefs.getString("app_id", "");
		location = prefs.getString("location", "");

		if (prefs.getInt("version", 0) < CommonLib.VERSION) {

			// the logic in this block is used on Home.java, to determine
			// whether to show collection first run or not.
			if (prefs.getInt("version", 0) == 0) {
				prefs.edit().putBoolean("app_fresh_install", true).commit();
				prefs.edit().putBoolean("app_upgrade", false).commit();

			} else if (prefs.getInt("version", 0) > 0) {
				prefs.edit().putBoolean("app_upgrade", true).commit();
				prefs.edit().putBoolean("app_fresh_install", false).commit();
			}

			firstLaunch = true;
			Editor edit = prefs.edit();

			// logging out user with app version < 3.2
			if (prefs.getInt("version", 0) < 40) {
				edit.putInt("uid", 0);
			}

			edit.putBoolean("firstLaunch", true);
			edit.putInt("version", CommonLib.VERSION);
			edit.commit();

			deleteDatabase("CACHE");
			deleteDatabase("MESSAGESDB");

			startCacheCleanerService();
			startLocationUpdateService();

		} else {
			firstLaunch = prefs.getBoolean("firstLaunch", true);
		}

		try {
			if (!isMyServiceRunning(CacheCleanerService.class)) {
				boolean alarmUp = (PendingIntent.getService(this, 0, new Intent(this, CacheCleanerService.class),
						PendingIntent.FLAG_NO_CREATE) != null);

				if (!alarmUp)
					startCacheCleanerService();
			}

			if (!isMyServiceRunning(LocationUpdateService.class)) {
				boolean alarmUp = (PendingIntent.getService(this, 0, new Intent(this, LocationUpdateService.class),
						PendingIntent.FLAG_NO_CREATE) != null);

				if (!alarmUp)
					startLocationUpdateService();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// UploadManager Initialize
		UploadManager.setContext(getApplicationContext());
		PostWrapper.Initialize(getApplicationContext());
		RequestWrapper.Initialize(getApplicationContext());

		// DB Initialize
		MessageDBWrapper.Initialize(getApplicationContext());
		cache = new LruCache<String, Bitmap>(30);

		new ThirdPartyInitAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		getTracker(CommonLib.TrackerName.GLOBAL_TRACKER);

	}

	private class ThirdPartyInitAsync extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			try {
				// Crashlytics Initialize
				Fabric.with(getApplicationContext(), new Crashlytics());
//				Crashlytics.setString("version", CommonLib.CRASHLYTICS_VERSION_STRING);

				// AppsFlyerLib Initialize
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
		}
	}

	public boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void startCacheCleanerService() {

		Intent intent = new Intent(this, CacheCleanerService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 04);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 3 * 24 * 60 * 60 * 1000, pintent);
	}

	private void startLocationUpdateService() {

		Intent intent = new Intent(this, LocationUpdateService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 04);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pintent);
	}

//	public void onLowMemory() {
//		SharedPreferences prefs = getSharedPreferences("application_settings", 0);
//		Editor editor = prefs.edit();
//		editor.putString("lat1", lat + "");
//		editor.putString("lon1", lon + "");
//		editor.putString("location", location);
//		editor.commit();
//
//		cache.clear();
//
//	}

	public void onTrimLevel(int i) {
		SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		Editor editor = prefs.edit();
		editor.putString("lat1", lat + "");
		editor.putString("lon1", lon + "");
		editor.putString("location", location);
		editor.commit();

		cache.clear();
	}

	public void setLocationString(String lstr) {
		location = lstr;
		SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		Editor editor = prefs.edit();
		editor.putString("location", location);
		editor.commit();
	}

	public String getLocationString() {
		return location;
	}

	public void setApplicationID(String aid) {
		APPLICATION_ID = aid;
	}

	public String getApplicationID() {
		return APPLICATION_ID;
	}

	public void interruptLocationTimeout() {
		// checkLocationTimeoutThread.interrupt();
		if (checkLocationTimeoutThread != null)
			checkLocationTimeoutThread.interrupt = false;
	}

	public void startLocationCheck() {

		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		if (result == ConnectionResult.SUCCESS) {
			zll.getFusedLocation(this);
		} else {
			getAndroidLocation();
		}
	}

	public void getAndroidLocation() {

		CommonLib.ZLog("zll", "getAndroidLocation");

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);

		if (providers != null) {
			for (String providerName : providers) {
				if (providerName.equals(LocationManager.GPS_PROVIDER))
					isGpsProviderEnabled = true;
				if (providerName.equals(LocationManager.NETWORK_PROVIDER))
					isNetworkProviderEnabled = true;
			}
		}

		if (isNetworkProviderEnabled || isGpsProviderEnabled) {

			if (isGpsProviderEnabled)
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, zll);
			if (isNetworkProviderEnabled)
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 500.0f, zll);

			if (checkLocationTimeoutThread != null) {
				checkLocationTimeoutThread.interrupt = false;
			}

			checkLocationTimeoutThread = new CheckLocationTimeoutAsync();
			checkLocationTimeoutThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		} else {
			zll.locationNotEnabled();
		}
	}

	private class CheckLocationTimeoutAsync extends AsyncTask<Void, Void, Void> {
		boolean interrupt = true;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			if (interrupt) {
				zll.interruptProcess();
			}
		}
	}

	public boolean isLocationAvailable() {
		return (isNetworkProviderEnabled || isGpsProviderEnabled);
	}

	// GA
	private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	public synchronized Tracker getTracker(TrackerName trackerId) {

		if (!mTrackers.containsKey(trackerId)) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = trackerId == CommonLib.TrackerName.APPLICATION_TRACKER ? analytics.newTracker("UA-69383611-1")
					: analytics.newTracker(R.xml.global_tracker);
			mTrackers.put(trackerId, t);
		}
		return mTrackers.get(trackerId);
	}
}
