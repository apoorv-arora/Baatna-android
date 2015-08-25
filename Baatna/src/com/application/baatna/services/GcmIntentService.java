package com.application.baatna.services;

import java.util.zip.Inflater;

import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.receivers.GcmBroadcastReceiver;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.views.BaatnaActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class GcmIntentService extends IntentService {

	public static boolean notificationDismissed = false;

	public Context context;
	private SharedPreferences prefs;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	public static final int NOTIFICATION_ID = 1;

	public GcmIntentService() {
		super("GcmIntentService");
		context = this;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		prefs = getSharedPreferences("application_settings", 0);

		if (extras != null && !extras.isEmpty()) {
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */

			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				CommonLib.ZLog("Send error:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				CommonLib.ZLog("Deleted messages on server:", extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

				if (extras.containsKey("Notification"))
					sendNotification(extras);

			}
		}

		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(Bundle extras) {

		String msg = extras.getString("Notification");
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationActivity = new Intent(this, Splash.class);
		notificationActivity.putExtra("", "");
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationActivity, 0);
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Baatna").setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg)
				.setSound(soundUri);
		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		// String len = extras.getString("length");
		// String command = extras.getString("command");
		//
		// CommonLib.ZLog("sendNotification", msg);
		// int l = Integer.parseInt(len);
		// JSONObject response = null;
		//
		// try {
		// // 1. base 64 decode
		// byte[] data = Base64.decode(msg, Base64.DEFAULT);
		//
		// // 2. G unzip
		// String decom = decompress(data, l);
		//
		// CommonLib.ZLog("GCM sendNotification after g unzip ", decom + ".");
		// CommonLib.ZLog("command", command + ".");
		//
		// response = new JSONObject(decom);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public static String decompress(byte[] compressed, int len) {
		String outputStr = null;
		try {
			Inflater decompresor = new Inflater();
			decompresor.setInput(compressed, 0, compressed.length);
			byte[] result = new byte[len];
			int resultLength = decompresor.inflate(result);
			decompresor.end();

			outputStr = new String(result, 0, resultLength, "UTF-8");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputStr;
	}
}