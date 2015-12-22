package com.application.baatna.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.application.baatna.data.Categories;
import com.application.baatna.data.CategoryItems;
import com.application.baatna.views.MessagesActivity;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class CommonLib {

	public static final boolean isTestBuild = false;

	public static final String SERVER_PREFIX = "http://";
	public static final String SERVER_BODY = "52.76.14.6:8080/BaatnaServer/rest/";
	public static String SERVER_WITHOUT_VERSION = "http://52.76.14.6:8080/BaatnaServer/rest/";
	//public static final String SERVER_BODY = "192.168.2.50:8080/BaatnaServer/rest/";
	//public static String SERVER_WITHOUT_VERSION = "http://192.168.2.50:8080/BaatnaServer/rest/";

	public static final boolean enableHSLogin = true;

	public static String API_VERSION = "";
	public static String SERVER = SERVER_WITHOUT_VERSION + API_VERSION;
	
	public static final String LOCAL_BROADCAST_NOTIFICATIONS = "new_push_notification";
	public static final String LOCAL_BROADCAST_NOTIFICATION = "gcm-push-notification";
	public static final String LOCAL_FEED_BROADCAST_NOTIFICATION = "new-feed-notification";


	/** to keep the url same for all cities.xml requests */
	// public static String STATIC_SERVER = "https://1api.baatna.com/v2/";
	/** API key */
	public static final String APIKEY = "AIzaSyC1Zbn_ROSWO-l4IJYTDaeyBTEit3fn9FI";

	/** GCM Sender ID */
	public static final String GCM_SENDER_ID = "531855430941";

	public final static boolean BaatnaLog = false;
	private static SharedPreferences prefs;

	/** Application version */
	public static final int VERSION = 6;
	public static final String VERSION_STRING = "1.1";

	/** Preferences */
	public final static String APP_SETTINGS = "application_settings";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";

	public static final int CURRENT_USER_WISH_ACCEPTED = 1;
	public static final int WISH_ACCEPTED_CURRENT_USER = 2;

	public static String BOLD_FONT_FILENAME = "BOLD_FONT_FILENAME";

	/** Authorization params */
	public static final String SOURCE = "&source=android_market&version=" + android.os.Build.VERSION.RELEASE
			+ "&app_version=" + VERSION;
	public static final String CLIENT_ID = "bt_android_client";
	public static final String APP_TYPE = "bt_android";

	/**
	 * Wish status
	 */
	public static final int STATUS_DELETED = 0;
	public static final int STATUS_ACTIVE = 1;
	public static final int STATUS_ACCEPTED = 2;
	public static final int STATUS_OFFERED = 3;
	public static final int STATUS_RECEIVED = 4;
	public static final int STATUS_FULLFILLED = 5;

	/**
	 * Wish actions
	 */
	public static final int ACTION_WISH_OFFERED = 3;
	public static final int ACTION_WISH_RECEIVED = 4;

	/**
	 * Thread pool executors
	 */
	private static final int mImageAsyncsMaxSize = 4;
	public static final BlockingQueue<Runnable> sPoolWorkQueueImage = new LinkedBlockingQueue<Runnable>(128);
	private static ThreadFactory sThreadFactoryImage = new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r);
		}
	};
	public static final Executor THREAD_POOL_EXECUTOR_IMAGE = new ThreadPoolExecutor(mImageAsyncsMaxSize,
			mImageAsyncsMaxSize, 1, TimeUnit.SECONDS, sPoolWorkQueueImage, sThreadFactoryImage);

	// AppsFlyer
	public static final String APPSFLYER_KEY = "ZWajvvuaGUAqqD83ZNauKW";

	public static final String CRASHLYTICS_VERSION_STRING = "1.1 Live";

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
	public static final int LOCATION_UPDATE = 210;
	public static final int WISH_OFFERED_STATUS = 211;
	public static final int COUPON_UPDATE = 212;
	public static final int SEND_FEEDBACK = 213;


	/** Constant to track location identification progress */
	public static final int LOCATION_NOT_ENABLED = 0;
	/** Constant to track location identification progress */
	public static final int LOCATION_NOT_DETECTED = 1;
	/** Constant to track location identification progress */
	public static final int LOCATION_DETECTED = 2;
	/** Constant to track location identification progress */
	public static final int GETZONE_CALLED = 3;
	/** Constant to track location identification progress */
	public static final int CITY_IDENTIFIED = 4;
	/** Constant to track location identification progress */
	public static final int CITY_NOT_IDENTIFIED = 5;
	public static final int LOCATION_DETECTION_RUNNING = 6;
	public static final int DIFFERENT_CITY_IDENTIFIED = 7;

	/**
	 * Feed types
	 */
	public static final int FEED_TYPE_NEW_USER = 1;
	public static final int FEED_TYPE_NEW_REQUEST = 2;
	public static final int FEED_TYPE_REQUEST_FULFILLED = 3;

	// Calculate the sample size of bitmaps
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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

	private static File createFileFromInputStream(InputStream inputStream, String name) {

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

		else if (language.equalsIgnoreCase("pt") && country.equalsIgnoreCase("BR"))
			language = "por";

		else if (language.equalsIgnoreCase("in"))
			language = "id";

		else if (language.equalsIgnoreCase("es") && country.equalsIgnoreCase("CL"))
			language = "es_cl";

		else if (language.equalsIgnoreCase("cs"))
			language = "cs";

		else if (language.equalsIgnoreCase("sk"))
			language = "sk";

		else if (language.equalsIgnoreCase("pl"))
			language = "pl";

		else if (language.equalsIgnoreCase("it"))
			language = "it";

		return SOURCE + uuidString + "&lang=" + language + "&android_language=" + languageLog + "&android_country="
				+ country;
	}

	public static InputStream getStream(HttpResponse response) throws IllegalStateException, IOException {
		InputStream instream = response.getEntity().getContent();
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			instream = new GZIPInputStream(instream);
		}
		return instream;
	}

	// Checks if network is available
	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivityManager = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
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

	public static int distFrom(double lat1, double lng1, double lat2, double lng2) {
		Log.e("lat1"+lat1+"  long1"+lng1,"lat 2"+lat2 +"  long2"+lng2);
		/*double earthRadius = 6371;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;
		Log.e("difference in distance",""+dist);
		return distance(lat1, lng1, lat2, lng2);*/
		float[] result=new float[3];
		Location.distanceBetween(lat1,lng1,lat2,lng2,result);
		//conversion to m
		result[0]=result[0]/1000;
		return (int)result[0];

	}
	/*private static double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;

			dist = dist * 1.609344;
		return (dist);
	}*/

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	//private static double rad2deg(double rad) {return (rad * 180 / Math.PI);}

	// Returns the Network State
	public static String getNetworkState(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

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
		if (context != null && bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			int width = metrics.widthPixels;
			int height = metrics.heightPixels;
			return ((width != 0 && width / bitmap.getWidth() < 1) || (height != 0 && height / bitmap.getHeight() < 1));
		}
		return false;
	}

	public static boolean isAndroidL() {
		return android.os.Build.VERSION.SDK_INT >= 21;
	}

	public static String getDateFromUTC(long timestamp) {
		if(timestamp == 0)
			timestamp = System.currentTimeMillis();
		Date date = new Date(timestamp);
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		cal.setTime(date);
		String val = (cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE) + " " + cal.get(Calendar.HOUR) + ":"
				+ cal.get(Calendar.MINUTE) + (cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM"));
		
//		final Date currentTime = new Date();
//		final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");
//		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//		String ret = "UTC time: " + sdf.format(currentTime);
//		System.out.println(ret);
		
		return val;
	}

	/**
	 * Returns the bitmap associated
	 */
	public static Bitmap getBitmap(Context mContext, int resId, int width, int height) throws OutOfMemoryError {
		if (mContext == null)
			return null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeResource(mContext.getResources(), resId, options);
		options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.RGB_565;

		if (!CommonLib.isAndroidL())
			options.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);

		return bitmap;
	}

	/**
	 * Blur a bitmap with the radius associated
	 */
	public static Bitmap fastBlur(Bitmap bitmap, int radius) {
		try {
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();

			int[] pix = new int[w * h];
			CommonLib.ZLog("pix", w + " " + h + " " + pix.length);
			bitmap.getPixels(pix, 0, w, 0, 0, w, h);

			Bitmap blurBitmap = bitmap.copy(bitmap.getConfig(), true);

			int wm = w - 1;
			int hm = h - 1;
			int wh = w * h;
			int div = radius + radius + 1;

			int r[] = new int[wh];
			int g[] = new int[wh];
			int b[] = new int[wh];
			int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
			int vmin[] = new int[Math.max(w, h)];

			int divsum = (div + 1) >> 1;
			divsum *= divsum;
			int dv[] = new int[256 * divsum];
			for (i = 0; i < 256 * divsum; i++) {
				dv[i] = (i / divsum);
			}

			yw = yi = 0;

			int[][] stack = new int[div][3];
			int stackpointer;
			int stackstart;
			int[] sir;
			int rbs;
			int r1 = radius + 1;
			int routsum, goutsum, boutsum;
			int rinsum, ginsum, binsum;

			for (y = 0; y < h; y++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				for (i = -radius; i <= radius; i++) {
					p = pix[yi + Math.min(wm, Math.max(i, 0))];
					sir = stack[i + radius];
					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);
					rbs = r1 - Math.abs(i);
					rsum += sir[0] * rbs;
					gsum += sir[1] * rbs;
					bsum += sir[2] * rbs;
					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}
				}
				stackpointer = radius;

				for (x = 0; x < w; x++) {

					r[yi] = dv[rsum];
					g[yi] = dv[gsum];
					b[yi] = dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (y == 0) {
						vmin[x] = Math.min(x + radius + 1, wm);
					}
					p = pix[yw + vmin[x]];

					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[(stackpointer) % div];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi++;
				}
				yw += w;
			}
			for (x = 0; x < w; x++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				yp = -radius * w;
				for (i = -radius; i <= radius; i++) {
					yi = Math.max(0, yp) + x;

					sir = stack[i + radius];

					sir[0] = r[yi];
					sir[1] = g[yi];
					sir[2] = b[yi];

					rbs = r1 - Math.abs(i);

					rsum += r[yi] * rbs;
					gsum += g[yi] * rbs;
					bsum += b[yi] * rbs;

					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}

					if (i < hm) {
						yp += w;
					}
				}
				yi = x;
				stackpointer = radius;
				for (y = 0; y < h; y++) {
					// Preserve alpha channel: ( 0xff000000 & pix[yi] )
					pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (x == 0) {
						vmin[y] = Math.min(y + r1, hm) * w;
					}
					p = x + vmin[y];

					sir[0] = r[p];
					sir[1] = g[p];
					sir[2] = b[p];

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[stackpointer];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi += w;
				}
			}

			CommonLib.ZLog("pix", w + " " + h + " " + pix.length);
			blurBitmap.setPixels(pix, 0, w, 0, 0, w, h);
			return blurBitmap;

		} catch (OutOfMemoryError e) {
			return bitmap;
		} catch (Exception e) {
			return bitmap;
		}
	}

	public static Bitmap getBitmapFromDisk(String url, Context ctx) {

		Bitmap defautBitmap = null;
		try {
			String filename = constructFileName(url);
			File filePath = new File(ctx.getCacheDir(), filename);

			if (filePath.exists() && filePath.isFile() && !filePath.isDirectory()) {
				FileInputStream fi;
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPreferredConfig = Config.RGB_565;
				fi = new FileInputStream(filePath);
				defautBitmap = BitmapFactory.decodeStream(fi, null, opts);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (Exception e) {

		} catch (OutOfMemoryError e) {

		}

		return defautBitmap;
	}

	public static String constructFileName(String url) {
		return url.replaceAll("/", "_");
	}

	public static void writeBitmapToDisk(String url, Bitmap bmp, Context ctx, CompressFormat format) {
		FileOutputStream fos;
		String fileName = constructFileName(url);
		try {
			if (bmp != null) {
				fos = new FileOutputStream(new File(ctx.getCacheDir(), fileName));
				bmp.compress(format, 75, fos);
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap getRoundedCornerBitmap(final Bitmap bitmap, final float roundPx) {

		if (bitmap != null) {
			try {
				final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
				Canvas canvas = new Canvas(output);

				final Paint paint = new Paint();
				final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
				final RectF rectF = new RectF(rect);

				paint.setAntiAlias(true);
				canvas.drawARGB(0, 0, 0, 0);
				canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

				paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
				canvas.drawBitmap(bitmap, rect, rect, paint);

				return output;

			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	public static ArrayList<Categories> getCategoriesList() {

		ArrayList<Categories> finalCategoryList = new ArrayList<Categories>();

		Categories categoryBookSports = new Categories();
		categoryBookSports.setCategoryId(1);
		categoryBookSports.setCategory("Books and Sports");
		categoryBookSports.setCategoryIcon("a");

		ArrayList<CategoryItems> categoryItemsList1 = new ArrayList<CategoryItems>();
		// categoryItemsList1.add(new CategoryItems(1, "Book", -1));
		// categoryItemsList1.add(new CategoryItems(2, "Cricket Bat", -1));
		// categoryItemsList1.add(new CategoryItems(3, "Football", -1));
		// categoryItemsList1.add(new CategoryItems(4, "Tennis Racquet", -1));
		// categoryItemsList1.add(new CategoryItems(5, "Badminton Racquet",
		// -1));
		// categoryItemsList1.add(new CategoryItems(6, "Golf Kit", -1));
		// categoryItemsList1.add(new CategoryItems(7, "BasketBall", -1));
		categoryBookSports.setCategoryItems(categoryItemsList1);

		finalCategoryList.add(categoryBookSports);

		Categories categorygames = new Categories();
		categorygames.setCategoryId(2);
		categorygames.setCategory("Games");
		categorygames.setCategoryIcon("a");

		ArrayList<CategoryItems> categoryItemsList2 = new ArrayList<CategoryItems>();
		// categoryItemsList2.add(new CategoryItems(1, "Play-station", -1));
		// categoryItemsList2.add(new CategoryItems(2, "Playing cards", -1));
		// categoryItemsList2.add(new CategoryItems(3, "Scrabble", -1));
		// categoryItemsList2.add(new CategoryItems(4, "Chess", -1));
		// categoryItemsList2.add(new CategoryItems(5, "Badminton Racquet",
		// -1));
		// categoryItemsList2.add(new CategoryItems(6, "Scotland Yard", -1));
		categorygames.setCategoryItems(categoryItemsList2);

		finalCategoryList.add(categorygames);

		Categories categoryMusic = new Categories();
		categoryMusic.setCategoryId(4);
		categoryMusic.setCategory("Music");
		categoryMusic.setCategoryIcon("a");

		ArrayList<CategoryItems> categoryItemsList3 = new ArrayList<CategoryItems>();
		// categoryItemsList3.add(new CategoryItems(1, "Acoustic Guitar", -1));
		// categoryItemsList3.add(new CategoryItems(2, "Electric Guitar", -1));
		// categoryItemsList3.add(new CategoryItems(3, "Drum-kit", -1));
		// categoryItemsList3.add(new CategoryItems(4, "Snares", -1));
		// categoryItemsList3.add(new CategoryItems(5, "Peddle", -1));
		// categoryItemsList3.add(new CategoryItems(6, "Microphone", -1));
		// categoryItemsList3.add(new CategoryItems(7, "Amplifier", -1));
		// categoryItemsList3.add(new CategoryItems(8, "Keyboard", -1));
		// categoryItemsList3.add(new CategoryItems(9, "Guitar pedals", -1));
		categoryMusic.setCategoryItems(categoryItemsList3);

		finalCategoryList.add(categoryMusic);

		Categories categoryTravelHoliday = new Categories();
		categoryTravelHoliday.setCategoryId(4);
		categoryTravelHoliday.setCategory("Travel and Holiday");
		categoryTravelHoliday.setCategoryIcon("a");

		ArrayList<CategoryItems> categoryItemsList4 = new ArrayList<CategoryItems>();
		// categoryItemsList4.add(new CategoryItems(1, "Helmet", -1));
		// categoryItemsList4.add(new CategoryItems(2, "Tavel Bag", -1));
		// categoryItemsList4.add(new CategoryItems(3, "go pro", -1));
		// categoryItemsList4.add(new CategoryItems(4, "Camping Chair", -1));
		// categoryItemsList4.add(new CategoryItems(5, "Tent", -1));
		// categoryItemsList4.add(new CategoryItems(6, "Suitcase", -1));
		// categoryItemsList4.add(new CategoryItems(7, "Sleeping bag", -1));
		// categoryItemsList4.add(new CategoryItems(8, "Inflatable Bed", -1));
		categoryTravelHoliday.setCategoryItems(categoryItemsList4);

		finalCategoryList.add(categoryTravelHoliday);

		Categories categoryParty = new Categories();
		categoryParty.setCategoryId(1);
		categoryParty.setCategory("Party");
		categoryParty.setCategoryIcon("a");

		ArrayList<CategoryItems> categoryItemsList5 = new ArrayList<CategoryItems>();
		// categoryItemsList5.add(new CategoryItems(1, "Disco Ball", -1));
		// categoryItemsList5.add(new CategoryItems(2, "Poker Set", -1));
		// categoryItemsList5.add(new CategoryItems(3, "Dj Set", -1));
		// categoryItemsList5.add(new CategoryItems(4, "Lights", -1));
		// categoryItemsList5.add(new CategoryItems(5, "Speakers", -1));
		// categoryItemsList5.add(new CategoryItems(6, "Fancy Chairs", -1));
		categoryParty.setCategoryItems(categoryItemsList5);

		finalCategoryList.add(categoryParty);

		Categories categoryBakingCooking = new Categories();
		categoryBakingCooking.setCategoryId(1);
		categoryBakingCooking.setCategory("Baking and cooking");
		categoryBakingCooking.setCategoryIcon("a");

		ArrayList<CategoryItems> categoryItemsList6 = new ArrayList<CategoryItems>();
		// categoryItemsList6.add(new CategoryItems(1, "Blender", -1));
		// categoryItemsList6.add(new CategoryItems(2, "Hand blender", -1));
		// categoryItemsList6.add(new CategoryItems(3, "Microwave", -1));
		// categoryItemsList6.add(new CategoryItems(4, "Cooking Pot", -1));
		// categoryItemsList6.add(new CategoryItems(5, "Ice bucket", -1));
		// categoryItemsList6.add(new CategoryItems(6, "Cutlery", -1));
		// categoryItemsList6.add(new CategoryItems(7, "Cakepan", -1));
		categoryBakingCooking.setCategoryItems(categoryItemsList6);

		finalCategoryList.add(categoryBakingCooking);

		Categories categoryHomeImprovement = new Categories();
		categoryHomeImprovement.setCategoryId(1);
		categoryHomeImprovement.setCategory("Home Improvement");
		categoryHomeImprovement.setCategoryIcon("a");

		ArrayList<CategoryItems> categoryItemsList7 = new ArrayList<CategoryItems>();
		// categoryItemsList7.add(new CategoryItems(1, "Drill", -1));
		// categoryItemsList7.add(new CategoryItems(2, "Hammer", -1));
		// categoryItemsList7.add(new CategoryItems(3, "Pincers", -1));
		// categoryItemsList7.add(new CategoryItems(4, "Iron (Press)", -1));
		// categoryItemsList7.add(new CategoryItems(5, "Screwdriver", -1));
		// categoryItemsList7.add(new CategoryItems(6, "Step Ladder", -1));
		categoryHomeImprovement.setCategoryItems(categoryItemsList1);

		finalCategoryList.add(categoryHomeImprovement);
		return finalCategoryList;
	}

	public static ArrayList<CategoryItems> getCategoryItems(int catId) {

		ArrayList<CategoryItems> finalCategoryList = new ArrayList<CategoryItems>();

		switch (catId) {
		case 1:
			finalCategoryList.add(new CategoryItems(1, "Book", ""));
			finalCategoryList.add(new CategoryItems(2, "Cricket Bat", ""));
			finalCategoryList.add(new CategoryItems(3, "Football", ""));
			finalCategoryList.add(new CategoryItems(4, "Tennis Racquet", ""));
			finalCategoryList.add(new CategoryItems(5, "Badminton Racquet", ""));
			finalCategoryList.add(new CategoryItems(6, "Golf Kit", ""));
			finalCategoryList.add(new CategoryItems(7, "BasketBall", ""));
			break;
		case 2:
			finalCategoryList.add(new CategoryItems(1, "Play-station", ""));
			finalCategoryList.add(new CategoryItems(2, "Playing cards", ""));
			finalCategoryList.add(new CategoryItems(3, "Scrabble", ""));
			finalCategoryList.add(new CategoryItems(4, "Chess", ""));
			finalCategoryList.add(new CategoryItems(5, "Badminton Racquet", ""));
			finalCategoryList.add(new CategoryItems(6, "Scotland Yard", ""));
			break;
		case 3:
			finalCategoryList.add(new CategoryItems(1, "Acoustic Guitar", ""));
			finalCategoryList.add(new CategoryItems(2, "Electric Guitar", ""));
			finalCategoryList.add(new CategoryItems(3, "Drum-kit", ""));
			finalCategoryList.add(new CategoryItems(4, "Snares", ""));
			finalCategoryList.add(new CategoryItems(5, "Peddle", ""));
			finalCategoryList.add(new CategoryItems(6, "Microphone", ""));
			finalCategoryList.add(new CategoryItems(7, "Amplifier", ""));
			finalCategoryList.add(new CategoryItems(8, "Keyboard", ""));
			finalCategoryList.add(new CategoryItems(9, "Guitar pedals", ""));
			break;
		case 4:
			finalCategoryList.add(new CategoryItems(1, "Helmet", ""));
			finalCategoryList.add(new CategoryItems(2, "Tavel Bag", ""));
			finalCategoryList.add(new CategoryItems(3, "go pro", ""));
			finalCategoryList.add(new CategoryItems(4, "Camping Chair", ""));
			finalCategoryList.add(new CategoryItems(5, "Tent", ""));
			finalCategoryList.add(new CategoryItems(6, "Suitcase", ""));
			finalCategoryList.add(new CategoryItems(7, "Sleeping bag", ""));
			finalCategoryList.add(new CategoryItems(8, "Inflatable Bed", ""));
			break;
		case 5:
			finalCategoryList.add(new CategoryItems(1, "Disco Ball", ""));
			finalCategoryList.add(new CategoryItems(2, "Poker Set", ""));
			finalCategoryList.add(new CategoryItems(3, "Dj Set", ""));
			finalCategoryList.add(new CategoryItems(4, "Lights", ""));
			finalCategoryList.add(new CategoryItems(5, "Speakers", ""));
			finalCategoryList.add(new CategoryItems(6, "Fancy Chairs", ""));
			break;
		case 6:
			finalCategoryList.add(new CategoryItems(1, "Blender", ""));
			finalCategoryList.add(new CategoryItems(2, "Hand blender", ""));
			finalCategoryList.add(new CategoryItems(3, "Microwave", ""));
			finalCategoryList.add(new CategoryItems(4, "Cooking Pot", ""));
			finalCategoryList.add(new CategoryItems(5, "Ice bucket", ""));
			finalCategoryList.add(new CategoryItems(6, "Cutlery", ""));
			finalCategoryList.add(new CategoryItems(7, "Cakepan", ""));
			break;
		case 7:
			finalCategoryList.add(new CategoryItems(1, "Drill", ""));
			finalCategoryList.add(new CategoryItems(2, "Hammer", ""));
			finalCategoryList.add(new CategoryItems(3, "Pincers", ""));
			finalCategoryList.add(new CategoryItems(4, "Iron (Press)", ""));
			finalCategoryList.add(new CategoryItems(5, "Screwdriver", ""));
			finalCategoryList.add(new CategoryItems(6, "Step Ladder", ""));
			break;
		}
		return finalCategoryList;
	}

	public static boolean getCurrentActiveActivity(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		boolean returnvalue= componentInfo.getClassName().equals(MessagesActivity.class.getName());
		Object o = MessagesActivity.class;
		System.out.println(o);
		return (returnvalue);
	}

	public static boolean hasContact(Context mContext, String contact) {
		ContentResolver cr = mContext.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer
						.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						if(phoneNo.contains(contact) || contact.contains(phoneNo)) {
							return true;
						}
					}
					pCur.close();
				}
			}
		} 
		return false;
	}


	public static String findDateDifference(Long timestamp) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm a");
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTimeInMillis(timestamp);
		calendar2.setTimeInMillis(System.currentTimeMillis());
		try {


			//in milliseconds
			long diff = calendar2.getTimeInMillis() - calendar1.getTimeInMillis();

			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);
			long diffYears = diffDays / 365;
//            System.out.print(diffDays + " days, ");
//            System.out.print(diffHours + " hours, ");
//            System.out.print(diffMinutes + " minutes, ");
//            System.out.print(diffSeconds + " seconds.");
//            Log.e("difference is", diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, ");
			if (diffDays == 0) {
				if (diffMinutes > 0) {
					return diffMinutes+ " MINS AGO";
					//return timeFormat.format(calendar1.getTime());
				} else return "Just Now";
			} else if (diffDays == 1) {
				return "Yesterday";
			} else if (diffDays <= 30) {
				return "" + diffDays + " DAYS AGO";
			} //else
				//return format.format(calendar1.getTime());


		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean validatePhone(String phone)
	{

		try{
			if(phone.trim().length()==0)
			{
				return false;
			}
			double n = Double.parseDouble(phone);
			if(!(phone.trim().toString().startsWith("7")||phone.trim().toString().startsWith("8")||phone.trim().toString().startsWith("9")))
				return false;
			if(phone.trim().length()==10)
				return true;

		}catch(Exception e)
		{
			return false;
		}
		return false;
	}

}
