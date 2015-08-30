package com.application.baatna.services;

import java.util.zip.Inflater;

import org.json.JSONException;
import org.json.JSONObject;

import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.data.Message;
import com.application.baatna.db.MessageDBWrapper;
import com.application.baatna.receivers.GcmBroadcastReceiver;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.ParserJson;
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
		String type = extras.getString("type");
		
		if( type.equals("message")) {
			JSONObject message = null;
			try {
				message = new JSONObject(msg);
				Message messageObj = ParserJson.parse_Message(message);
				MessageDBWrapper.addMessage(messageObj, prefs.getInt("uid", 0), System.currentTimeMillis());
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
		//check if app is alive, do not push the message notifiication then
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