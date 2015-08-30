package com.application.baatna.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class CommonLib {

	public static final boolean isTestBuild = false;

	// public static final String SERVER_PREFIX = "https://";
	// public static final String SERVER_BODY = "api.baatna.com/";
	// public static String SERVER_WITHOUT_VERSION = "https://1api.baatna.com/";
	public static final String SERVER_PREFIX = "http://";
//	public static final String SERVER_BODY = "52.76.6.41:8080/BaatnaServer/rest/";
//	public static String SERVER_WITHOUT_VERSION = "http://52.76.6.41:8080/BaatnaServer/rest/";
	public static final String SERVER_BODY = "192.168.1.42:8080/BaatnaServer/rest/";
	public static String SERVER_WITHOUT_VERSION = "http://192.168.1.42:8080/BaatnaServer/rest/";
	
	public static final boolean enableHSLogin = true;

	// public static String API_VERSION = "v2/";
	public static String API_VERSION = "";
	public static String SERVER = SERVER_WITHOUT_VERSION + API_VERSION;

	/** to keep the url same for all cities.xml requests */
	// public static String STATIC_SERVER = "https://1api.baatna.com/v2/";
	/** API key */
	public static final String APIKEY = "AIzaSyC1Zbn_ROSWO-l4IJYTDaeyBTEit3fn9FI";

	/** GCM Sender ID */
	public static final String GCM_SENDER_ID = "531855430941";

	public final static boolean BaatnaLog = true;
	private static SharedPreferences prefs;

	/** Application version */
	public static final int VERSION = 1;
	public static final String VERSION_STRING = "1.0.1";

	/** Preferences */
	public final static String APP_SETTINGS = "application_settings";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";

	public static String BOLD_FONT_FILENAME = "BOLD_FONT_FILENAME";

	/** Authorization params */
	public static final String SOURCE = "&source=android_market&version="
			+ android.os.Build.VERSION.RELEASE + "&app_version=" + VERSION;
	public static final String CLIENT_ID = "bt_android_client";
	public static final String APP_TYPE = "bt_android";

	// AppsFlyer
	public static final String APPSFLYER_KEY = "ZWajvvuaGUAqqD83ZNauKW";

	public static final String CRASHLYTICS_VERSION_STRING = "6.4.3 Live";

	public static String LightItalic = "fonts/Glober_Light_Italic.otf";
	public static String Regular = "fonts/Glober_Light.otf";
	public static String Bold = "fonts/Glober_SemiBold.otf";
	public static String Icons = "fonts/baats-app.ttf";

	public enum TrackerName {
		GLOBAL_TRACKER, APPLICATION_TRACKER
	}

	/** Activity result */
	public static final int NEW_REQUEST = 32;

	/** Upload status tracker */
	public static final int SIGNUP = 201;
	public static final int LOGIN = 202;
	public static final int LOGOUT = 203;
	public static final int WISH_ADD = 204;
	public static final int WISH_REMOVE = 205;
	public static final int HARDWARE_REGISTER = 206;
	public static final int UPDATE_INSTITUTION = 207;
	public static final int WISH_UPDATE_STATUS = 208;
	public static final int SEND_MESSAGE = 209;
	
	
	/**
	 * Feed types
	 * */
	public static final int FEED_TYPE_NEW_USER = 1;
	public static final int FEED_TYPE_NEW_REQUEST = 2;
	public static final int FEED_TYPE_REQUEST_FULFILLED = 3;

	// Calculate the sample size of bitmaps
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		int inSampleSize = 1;
		double ratioH = (double) options.outHeight / reqHeight;
		double ratioW = (double) options.outWidth / reqWidth;

		int h = (int) Math.round(ratioH);
		int w = (int) Math.round(ratioW);

		if (h > 1 || w > 1) {
			if (h > w) {
				inSampleSize = h >= 2 ? h : 2;

			} else {
				inSampleSize = w >= 2 ? w : 2;
			}
		}
		return inSampleSize;
	}

	public static final Hashtable<String, Typeface> typefaces = new Hashtable<String, Typeface>();

	public static Typeface getTypeface(Context c, String name) {
		synchronized (typefaces) {
			if (!typefaces.containsKey(name)) {
				try {
					InputStream inputStream = c.getAssets().open(name);
					File file = createFileFromInputStream(inputStream, name);
					if (file == null) {
						return Typeface.DEFAULT;
					}
					Typeface t = Typeface.createFromFile(file);
					typefaces.put(name, t);
				} catch (Exception e) {
					e.printStackTrace();
					return Typeface.DEFAULT;
				}
			}
			return typefaces.get(name);
		}
	}

	private static File createFileFromInputStream(InputStream inputStream,
			String name) {

		try {
			File f = File.createTempFile("font", null);
			OutputStream outputStream = new FileOutputStream(f);
			byte buffer[] = new byte[1024];
			int length = 0;

			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}

			outputStream.close();
			inputStream.close();
			return f;
		} catch (Exception e) {
			// Logging exception
			e.printStackTrace();
		}

		return null;
	}

	// Baatna Logging end points
	public static void ZLog(String Tag, String Message) {
		if (BaatnaLog && Message != null)
			Log.i(Tag, Message);
	}

	public static void ZLog(String Tag, float Message) {
		if (BaatnaLog)
			Log.i(Tag, Message + "");
	}

	public static void ZLog(String Tag, boolean Message) {
		if (BaatnaLog)
			Log.i(Tag, Message + "");
	}

	public static void ZLog(String Tag, int Message) {
		if (BaatnaLog)
			Log.i(Tag, Message + "");
	}

	// Return this string for every call
	public static String getVersionString(Context context) {
		String language = Locale.getDefault().getLanguage();
		String languageLog = Locale.getDefault().getLanguage();
		String country = Locale.getDefault().getCountry();
		String uuidString = "";

		if (prefs == null && context != null)
			prefs = context.getSharedPreferences(APP_SETTINGS, 0);

		if (prefs != null)
			uuidString = "&uuid=" + prefs.getString("app_id", "");

		if (language.equalsIgnoreCase("pt") && country.equalsIgnoreCase("PT"))
			language = "pt";

		else if (language.equalsIgnoreCase("pt")
				&& country.equalsIgnoreCase("BR"))
			language = "por";

		else if (language.equalsIgnoreCase("in"))
			language = "id";

		else if (language.equalsIgnoreCase("es")
				&& country.equalsIgnoreCase("CL"))
			language = "es_cl";

		else if (language.equalsIgnoreCase("cs"))
			language = "cs";

		else if (language.equalsIgnoreCase("sk"))
			language = "sk";

		else if (language.equalsIgnoreCase("pl"))
			language = "pl";

		else if (language.equalsIgnoreCase("it"))
			language = "it";

		return SOURCE + uuidString + "&lang=" + language + "&android_language="
				+ languageLog + "&android_country=" + country;
	}

	public static InputStream getStream(HttpResponse response)
			throws IllegalStateException, IOException {
		InputStream instream = response.getEntity().getContent();
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null
				&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			instream = new GZIPInputStream(instream);
		}
		return instream;
	}

	// Checks if network is available
	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return distance in km
	 */

	public static double distFrom(double lat1, double lng1, double lat2,
			double lng2) {
		double earthRadius = 6371;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1)
				* Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		return dist;
	}

	// Returns the Network State
	public static String getNetworkState(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		String returnValue = "";
		if (null != activeNetwork) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
				returnValue = "wifi";
			else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
				returnValue = "mobile" + "_" + getNetworkType(context);
			else
				returnValue = "Unknown";
		} else
			returnValue = "Not connected";
		return returnValue;
	}

	// Returns the Data Network type
	public static String getNetworkType(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		switch (telephonyManager.getNetworkType()) {

		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xRTT";

		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";

		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE ";

		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return "EHRPD ";

		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "EVDO_0 ";

		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "EVDO_A ";

		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return "EVDO_B ";

		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS ";

		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA ";

		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA ";

		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return "HSPAP ";

		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA ";

		case TelephonyManager.NETWORK_TYPE_IDEN:
			return "IDEN ";

		case TelephonyManager.NETWORK_TYPE_LTE:
			return "LTE ";

		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS ";

		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return "UNKNOWN ";

		default:
			return "UNKNOWN ";
		}
	}

	// check done before storing the bitmap in the memory
	public static boolean shouldScaleDownBitmap(Context context, Bitmap bitmap) {
		if (context != null && bitmap != null && bitmap.getWidth() > 0
				&& bitmap.getHeight() > 0) {
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			int width = metrics.widthPixels;
			int height = metrics.heightPixels;
			return ((width != 0 && width / bitmap.getWidth() < 1) || (height != 0 && height
					/ bitmap.getHeight() < 1));
		}
		return false;
	}

	public static boolean isAndroidL() {
		return android.os.Build.VERSION.SDK_INT >= 21;
	}

	public static String getDateFromUTC(long timestamp) {
		Date date = new Date(timestamp);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        return (cal.get(Calendar.MONTH)
                + "/" + cal.get(Calendar.DATE)
                + " " + cal.get(Calendar.HOUR)
                + ":" + cal.get(Calendar.MINUTE)
                + (cal.get(Calendar.AM_PM)==0?"AM":"PM")
                );
	}
	
	 /**
     * Returns the bitmap associated
     * */
    public static Bitmap getBitmap(Context mContext, int resId, int width , int height) throws OutOfMemoryError{
        if(mContext == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(mContext.getResources(), resId, options);
        options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        if(!CommonLib.isAndroidL())
            options.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);

        return bitmap;
    }

}
