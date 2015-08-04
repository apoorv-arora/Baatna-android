package com.application.baatna;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import com.application.baatna.utils.BaatnaLocationCallback;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.views.BaatnaActivity;
import com.application.baatna.views.HSLoginActivity;
import com.application.baatna.views.Home;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class Splash extends Activity implements BaatnaLocationCallback {

	private BaatnaApp zapp;
	private SharedPreferences prefs;
	Context context;
	private boolean windowHasFocus = false;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	String regId;
	int hardwareRegistered = 0;

	int width;
	int height;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);

		prefs = getSharedPreferences(CommonLib.APP_SETTINGS, 0);
		context = getApplicationContext();
		zapp = (BaatnaApp) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();

		final ImageView img = (ImageView) findViewById(R.id.baatna_logo);
		
		try {
			int imageWidth = width;
			int imageHeight = height;

			Bitmap searchBitmap = CommonLib.getBitmap(this,
					R.drawable.splash_final, imageWidth, imageHeight);
			img.getLayoutParams().width = imageWidth;
			img.getLayoutParams().height = imageHeight;
			img.setImageBitmap(searchBitmap);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		img.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkPlayServices();
				startLocationCheck();
			}
		}, 2000);

	}

	private void checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode != ConnectionResult.SUCCESS) {

			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				CommonLib.ZLog("google-play-resultcode", resultCode);
				if (resultCode == 2 && !isFinishing()) {

					if (windowHasFocus)
						showDialog(PLAY_SERVICES_RESOLUTION_REQUEST);

				} else {
					navigateToHomeOrLogin();
				}

			} else {
				navigateToHomeOrLogin();
			}

		} else {

			gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
			regId = getRegistrationId(context);

			if (hardwareRegistered == 0) {
				// Call
				if (prefs.getInt("uid", 0) != 0 && !regId.equals("")) {
					sendRegistrationIdToBackend();
					Editor editor = prefs.edit();
					editor.putInt("HARDWARE_REGISTERED", 1);
					editor.commit();
				}
			}

			if (regId.isEmpty()) {
				CommonLib.ZLog("GCM", "RegID is empty");
				registerInBackground();
			} else {
				CommonLib.ZLog("GCM", "already registered : " + regId);
			}
			navigateToHomeOrLogin();
		}
	}

	public void startLocationCheck() {
		zapp.zll.forced = true;
		zapp.zll.addCallback(this);
		zapp.startLocationCheck();
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p/>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {

		final SharedPreferences prefs = getSharedPreferences(
				"application_settings", 0);
		String registrationId = prefs.getString(CommonLib.PROPERTY_REG_ID, "");

		if (registrationId.isEmpty()) {
			CommonLib.ZLog("GCM", "Registration not found.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 1);
			return packageInfo.versionCode;

		} catch (Exception e) {
			CommonLib.ZLog("GCM", "EXCEPTION OCCURED" + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p/>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {

				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}

					regId = gcm.register(CommonLib.GCM_SENDER_ID);
					msg = "Device registered, registration ID=" + regId;
					storeRegistrationId(context, regId);

					if (prefs.getInt("uid", 0) != 0 && !regId.equals(""))
						sendRegistrationIdToBackend();

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				CommonLib.ZLog("GCM msg", msg);
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void storeRegistrationId(Context context, String regId) {

		prefs = getSharedPreferences("application_settings", 0);
		int appVersion = getAppVersion(context);
		Editor editor = prefs.edit();
		editor.putString(CommonLib.PROPERTY_REG_ID, regId);
		editor.putInt(CommonLib.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private void sendRegistrationIdToBackend() {
		// new registerDeviceAtZomato().execute();
		UploadManager.updateRegistrationId(prefs.getString("access_token", ""),
				regId);
	}

	public void onCoordinatesIdentified(Location loc) {
		checkPlayServices();
	}

	public void onLocationIdentified() {
	}

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onNetworkError() {
	}
	
	@Override
	protected void onDestroy() {
		ImageView img = (ImageView) findViewById(R.id.baatna_logo);
        img.setImageBitmap(null);
		super.onDestroy();
	}

	private void navigateToHomeOrLogin() {
		if (prefs.getInt("uid", 0) != 0) {
			if (prefs.getBoolean("HSLogin", true)) {
				Intent intent = new Intent(this, HSLoginActivity.class);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, Home.class);
				startActivity(intent);
			}
		} else {
			Intent startIntent = new Intent(Splash.this, BaatnaActivity.class);
			startActivity(startIntent);
			finish();
			overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}
		finish();
	}

}
