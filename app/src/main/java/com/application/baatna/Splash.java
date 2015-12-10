package com.application.baatna;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.utils.BaatnaLocationCallback;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.FacebookConnect;
import com.application.baatna.utils.FacebookConnectCallback;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;
import com.application.baatna.views.Home;
import com.application.baatna.views.UserLoginActivity;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class Splash extends Activity implements FacebookConnectCallback, UploadManagerCallback, BaatnaLocationCallback {

	int width;
	int height;
	private SharedPreferences prefs;
	private ProgressDialog z_ProgressDialog;
	private boolean dismissDialog = false;

	/** Constant, randomly selected */
	public final int RESULT_FACEBOOK_LOGIN_OK = 1432; // Random numbers
	public final int RESULT_GOOGLE_LOGIN_OK = 1434;

	// RelativeLayout loginPage;
	// RelativeLayout signUpPage;

	final int DEFAULT_SHOWN = 87;
	final int LOGIN_SHOWN = 88;
	final int SIGNUP_SHOWN = 89;
	int mState = DEFAULT_SHOWN;
	public static final int ANIMATION_LOGIN = 200;

	private String APPLICATION_ID;
	private boolean destroyed = false;

	private String error_responseCode = "";
	private String error_exception = "";
	private String error_stackTrace = "";

	private BaatnaApp zapp;
	Context context;
	private boolean windowHasFocus = false;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	String regId;
	int hardwareRegistered = 0;

	ImageView imgBg, imgLogo, imgText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		prefs = getSharedPreferences("application_settings", 0);
		context = getApplicationContext();
		zapp = (BaatnaApp) getApplication();
		APPLICATION_ID = prefs.getString("app_id", "");

		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();

		imgBg = (ImageView) findViewById(R.id.baatna_background);
		imgLogo = (ImageView) findViewById(R.id.baatna_logo);
		imgText = (ImageView) findViewById(R.id.baatna_text);

		try {
			int imageWidth = width;
			int imageHeight = height;

			Bitmap bgBitmap = CommonLib.getBitmap(this, R.drawable.baatna_background, imageWidth, imageHeight);
			imgBg.getLayoutParams().width = imageWidth;
			imgBg.getLayoutParams().height = imageHeight;
			imgBg.setImageBitmap(bgBitmap);

			Bitmap logoBitmap = CommonLib.getBitmap(this, R.drawable.baatna_splash_logo, imageWidth / 2,
					imageWidth / 2);
			imgLogo.getLayoutParams().width = imageWidth / 2;
			imgLogo.getLayoutParams().height = imageWidth / 2;
			imgLogo.setImageBitmap(logoBitmap);

			Bitmap textBitmap = CommonLib.getBitmap(this, R.drawable.baatna_splash_text, imageWidth / 2, imageWidth / 10);
			imgText.getLayoutParams().width = imageWidth / 2;
			imgText.getLayoutParams().height = imageWidth / 10;
			imgText.setImageBitmap(textBitmap);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}


		imgBg.postDelayed(new Runnable() {
			@Override
			public void run() {
				startLocationCheck();
			}
		}, 2000);
		animate();
		fixSizes();
		UploadManager.addCallback(this);
	}

	private Animation animation1, animation2;

	private void animate() {
		try {
			imgBg.setVisibility(View.VISIBLE);

			animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
			animation2.setDuration(700);
			animation2.restrictDuration(700);
			animation2.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					imgText.setVisibility(View.VISIBLE);
					if (prefs.getInt("uid", 0) == 0) {
						findViewById(R.id.login_page_layout_connect_using_facebook).setVisibility(View.VISIBLE);
						findViewById(R.id.layout_login_separator).setVisibility(View.VISIBLE);
					} else {
						checkPlayServices();
					}
				}
			});
			animation2.scaleCurrentDuration(1);

			animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_center);
			animation1.setDuration(700);
			animation1.restrictDuration(700);
			animation1.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					imgLogo.setVisibility(View.VISIBLE);
					imgText.setVisibility(View.VISIBLE);
					imgText.startAnimation(animation2);
				}
			});
			animation1.scaleCurrentDuration(1);
			imgLogo.startAnimation(animation1);
			imgLogo.setVisibility(View.VISIBLE);

		} catch (Exception e) {
			imgBg.setVisibility(View.VISIBLE);
			imgLogo.setVisibility(View.VISIBLE);
			imgText.setVisibility(View.VISIBLE);
			findViewById(R.id.login_page_layout_connect_using_facebook).setVisibility(View.VISIBLE);
			findViewById(R.id.layout_login_separator).setVisibility(View.VISIBLE);
		}
	}

	private void checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode != ConnectionResult.SUCCESS) {

			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				CommonLib.ZLog("google-play-resultcode", resultCode);
				if (resultCode == 2 && !isFinishing()) {

					if (windowHasFocus)
						showDialog(PLAY_SERVICES_RESOLUTION_REQUEST);
				} else {
					navigateToHome();
				}

			} else {
				navigateToHome();
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
			navigateToHome();
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

		final SharedPreferences prefs = getSharedPreferences("application_settings", 0);
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
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);
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
		UploadManager.updateRegistrationId(prefs.getString("access_token", ""), regId);
	}

	public void onCoordinatesIdentified(Location loc) {
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void fixSizes() {

		// setting the logo
		// final ImageView img = (ImageView) findViewById(R.id.baatna_logo);
		//
		// try {
		// int imageWidth = width;
		// int imageHeight = height;
		//
		// Bitmap searchBitmap = CommonLib.getBitmap(this,
		// R.drawable.splash_final, imageWidth, imageHeight);
		// img.getLayoutParams().width = imageWidth;
		// img.getLayoutParams().height = imageHeight;
		// img.setImageBitmap(searchBitmap);
		//
		// } catch (OutOfMemoryError e) {
		// e.printStackTrace();
		// }
		((RelativeLayout.LayoutParams) imgLogo.getLayoutParams()).setMargins(0, height / 5, 0, width / 20);

		int buttonHeight = (11 * 9 * width) / (80 * 10);
		// fb button
		View fb_cont = findViewById(R.id.login_page_layout_connect_using_facebook);
		fb_cont.getLayoutParams().height = buttonHeight;
		((RelativeLayout.LayoutParams) fb_cont.getLayoutParams()).setMargins(width / 20, height / 2 + height / 4,
				width / 10, width / 80);
		((LinearLayout.LayoutParams) findViewById(R.id.login_page_facebook_icon_container).getLayoutParams())
				.setMargins(0, 0, width / 20, 0);
		findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().width = buttonHeight;
		findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().height = buttonHeight;
		((TextView) findViewById(R.id.login_page_layout_connect_using_facebook_text))
				.setText(getResources().getString(R.string.login_via_facebook));

		Paint textPaint = ((TextView) findViewById(R.id.login_page_layout_connect_using_facebook_text)).getPaint();
		float textWidth = textPaint.measureText(getResources().getString(R.string.login_via_facebook));
		findViewById(R.id.layout_login_separator).getLayoutParams().width = (int) textWidth + width / 20;
		// findViewById(R.id.login_page_layout_connect_using_facebook_text).getWidth();
		// findViewById(R.id.layout_signup_text).setPadding(width / 20,
		// width / 20, width / 20, width / 20);
		// findViewById(R.id.layout_login_text).setPadding(width / 20, width /
		// 20,
		// width / 20, width / 20);

		// sign-up page

		// login page

	}

	@Override
	public void response(Bundle bundle) {

		error_exception = "";
		error_responseCode = "";
		error_stackTrace = "";
		boolean regIdSent = false;

		if (bundle.containsKey("error_responseCode"))
			error_responseCode = bundle.getString("error_responseCode");

		if (bundle.containsKey("error_exception"))
			error_exception = bundle.getString("error_exception");

		if (bundle.containsKey("error_stackTrace"))
			error_stackTrace = bundle.getString("error_stackTrace");

		try {

			int status = bundle.getInt("status");

			if (status == 0) {

				if (!error_exception.equals("") || !error_responseCode.equals("") || !error_stackTrace.equals(""))
					;// BTODO
						// sendFailedLogsToServer();

				if (bundle.getString("errorMessage") != null) {
					String errorMessage = bundle.getString("errorMessage");
					Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.err_occurred, Toast.LENGTH_SHORT).show();
				}
				if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
					z_ProgressDialog.dismiss();
			} else {
				Editor editor = prefs.edit();
				editor.putInt("uid", bundle.getInt("uid"));
				if (bundle.containsKey("email"))
					editor.putString("email", bundle.getString("email"));
				if (bundle.containsKey("username"))
					editor.putString("username", bundle.getString("username"));
				if (bundle.containsKey("thumbUrl"))
					editor.putString("thumbUrl", bundle.getString("thumbUrl"));
				if (bundle.containsKey("profile_pic"))
					editor.putString("profile_pic", bundle.getString("profile_pic"));
				if (bundle.containsKey("user_name"))
					editor.putString("username", bundle.getString("username"));
				String token = bundle.getString("access_token");
				System.out.println(token);
				editor.putString("access_token", bundle.getString("access_token"));
				editor.putBoolean("verifiedUser", bundle.getBoolean("verifiedUser"));
				editor.commit();

				// ZTracker.logAppFlyersEvent(BaatnaActivity.this,
				// "Signup_Facebook");
				CommonLib.ZLog("login", "FACEBOOK");

				// if (REQUEST_CODE == START_LOGIN_INTENT
				// || REQUEST_CODE == START_ACTIVITY_LOGIN_INTENT) {
				checkPlayServices();
				// setResult(CommonLib.RESULT_FACEBOOK_LOGIN_OK);
				// finish();

				// } else if (REQUEST_CODE ==
				// ZOMATO_LOGIN_CONFIRMATION_REQUIRED) {
				// navigateToHome();
				// // setResult(RESULT_OK);
				// // finish();
				//
				// } else {
				// navigateToHome();
				// // setResult(RESULT_OK);
				// // finish();
				// }

				// GCM
				// if (bundle.containsKey("reg_id_sent")) {
				// regIdSent = bundle.getBoolean("reg_id_sent");
				// if (!regIdSent) {
				// String regId = getRegistrationId(getApplicationContext());
				// if (regId.length() > 0 && prefs.getInt("uid", 0) > 0)
				// sendRegistrationIdToBackend();
				// }
				// }

				if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
					z_ProgressDialog.dismiss();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void facebookAction(View view) {
		z_ProgressDialog = ProgressDialog.show(Splash.this, null, getResources().getString(R.string.verifying_creds),
				true, false);
		z_ProgressDialog.setCancelable(false);
		String regId = prefs.getString("registration_id", "");
		FacebookConnect facebookConnect = new FacebookConnect(Splash.this, 1, APPLICATION_ID, true, regId);
		facebookConnect.execute();
	}

	@Override
	protected void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		ImageView img = (ImageView) findViewById(R.id.baatna_logo);
		img.setImageBitmap(null);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		try {
			super.onActivityResult(requestCode, resultCode, intent);
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, intent);

		} catch (Exception w) {

			w.printStackTrace();

			try {
				com.facebook.Session fbSession = com.facebook.Session.getActiveSession();
				if (fbSession != null) {
					fbSession.closeAndClearTokenInformation();
				}
				com.facebook.Session.setActiveSession(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (dismissDialog) {
			if (z_ProgressDialog != null) {
				z_ProgressDialog.dismiss();
			}
		}
	}

	public void onBackPressed() {
		super.onBackPressed();
	}

	public void goBack(View v) {
		onBackPressed();
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.SIGNUP) {
			if (destroyed)
				return;
			if (status) {
				// make the login call now.
				// String name = ((TextView) signUpPage
				// .findViewById(R.id.login_username)).getText()
				// .toString();
				// if (name == null || name.equals(""))
				// return;
				// String email = ((TextView) signUpPage
				// .findViewById(R.id.login_email)).getText().toString();
				// if (email == null || email.equals(""))
				// return;
				// String password = ((TextView) signUpPage
				// .findViewById(R.id.login_password)).getText()
				// .toString();
				// if (password == null || password.equals(""))
				// return;
				// UploadManager.login(email, password);
				// } else {

			}
		} else if (requestType == CommonLib.LOGIN) {
			if (destroyed)
				return;
			if (status) {
				// save the access token.
				// {"access_token":"532460","user":{"user_id":0,"is_verified":0}}
				JSONObject responseJSON = null;
				try {
					responseJSON = new JSONObject(String.valueOf(data));
					Editor editor = prefs.edit();
					if (responseJSON.has("access_token")) {
						editor.putString("access_token", responseJSON.getString("access_token"));
					}
					if (responseJSON.has("HSLogin") && responseJSON.get("HSLogin") instanceof Boolean) {
						editor.putBoolean("HSLogin", responseJSON.getBoolean("HSLogin"));
					}
					if (responseJSON.has("INSTITUTION_NAME")) {
						editor.putString("INSTITUTION_NAME", responseJSON.getString("INSTITUTION_NAME"));
					}
					if (responseJSON.has("STUDENT_ID")) {
						editor.putString("STUDENT_ID", responseJSON.getString("STUDENT_ID"));
					}
					if (responseJSON.has("user_id") && responseJSON.get("user_id") instanceof Integer) {
						editor.putInt("uid", responseJSON.getInt("user_id"));
					}
					editor.commit();
					checkPlayServices();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (requestType == CommonLib.SIGNUP) {
			if (destroyed)
				return;
		}
	}

	public void navigateToHome() {
		if (prefs.getInt("uid", 0) != 0) {
			if (prefs.getBoolean("instutionLogin", true) && prefs.getBoolean("HSLogin", true)) {
				//did not login using hslogin before, navigate to user details page
				Intent intent = new Intent(this, UserLoginActivity.class);
//				Intent intent = new Intent(this, HSLoginActivity.class);
				startActivity(intent);
				finish();
			} else {
				Intent intent = new Intent(this, Home.class);
				startActivity(intent);
				finish();
			}
		}
	}

}