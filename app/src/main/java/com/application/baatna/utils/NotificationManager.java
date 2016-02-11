package com.application.baatna.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.data.FeedItem;
import com.application.baatna.data.Message;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.db.MessageDBWrapper;
import com.application.baatna.utils.facebook.FriendChecker;
import com.application.baatna.views.MessagesActivity;
import com.application.baatna.views.WishActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.zip.Inflater;

/**
 * Created by yogeshmadaan on 10/02/16.
 */
public class NotificationManager {
    private static NotificationManager _instance;
    private Context context;
    private SharedPreferences prefs;
    private android.app.NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    public static int notificationId ;
    public static final int NOTIFICATION_ID_MESSAGE = 1;
    public static final int NOTIFICATION_ID_FEED = 2;
    private NotificationManager(Context context) {
        this.context = context;
    }

    public static NotificationManager getInstance(Context context)
    {
        if(null== _instance)
            _instance = new NotificationManager(context);
        return _instance;
    }

    public void sendNotification(Bundle extras) {

        String msg = extras.getString("Notification");
        String type = extras.getString("type");
        Intent notificationActivity = null;
        boolean showNotification = true;
        if (type != null && type.equals("message")) {
            notificationId = NOTIFICATION_ID_MESSAGE;
            JSONObject message = null;
            try {
                message = new JSONObject(msg);
                Message messageObj = ParserJson.parse_Message(message);
                if (messageObj != null && messageObj.getFromUser() != null && messageObj.getWish() != null) {
                    MessageDBWrapper.addMessage(messageObj, messageObj.getFromUser().getUserId(),
                            messageObj.getWish().getWishId(), System.currentTimeMillis());
                    msg = messageObj.getFromUser().getUserName() + " has sent you a message";
                    notificationActivity = new Intent(context, MessagesActivity.class);
                    notificationActivity.putExtra("user", messageObj.getFromUser());
                    notificationActivity.putExtra("wish", messageObj.getWish());
                    if(messageObj.getWish().getUserId()==messageObj.getFromUser().getUserId())
                        notificationActivity.putExtra("type", CommonLib.WISH_ACCEPTED_CURRENT_USER);
                    else if(messageObj.getWish().getUserId()==context.getSharedPreferences(CommonLib.APP_SETTINGS, 0).getInt("uid",0))
                        notificationActivity.putExtra("type", CommonLib.CURRENT_USER_WISH_ACCEPTED);
                    else
                        notificationActivity.putExtra("type", type);

                    notificationActivity.putExtra("message", messageObj);

                    boolean object = CommonLib.getCurrentActiveActivity(context);
                    if (object) {
                        showNotification = true;
                        Intent mIntent = new Intent(CommonLib.LOCAL_BROADCAST_NOTIFICATION);
                        mIntent.putExtra("user", messageObj.getFromUser());
                        mIntent.putExtra("wish", messageObj.getWish());
                        mIntent.putExtra("type", type);
                        mIntent.putExtra("message", messageObj);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(mIntent);
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type != null && type.equals("newWish")) {
            notificationId = NOTIFICATION_ID_FEED;
            JSONObject message = null;
            try {
                message = new JSONObject(msg);
                notificationActivity = new Intent(context, WishActivity.class);
                User mUser = null;
                Wish mWish = null;
                if (message.has("message")) {
                    msg = String.valueOf(message.get("message"));
                }
                if (message.has("wish") && message.get("wish") instanceof JSONObject
                        && message.getJSONObject("wish").has("wish")) {
                    mWish = ParserJson.parse_Wish(message.getJSONObject("wish").getJSONObject("wish"));
                }

                if (message.has("user") && message.getJSONObject("user").has("user")) {
                    mUser = ParserJson.parse_User(message.getJSONObject("user").getJSONObject("user"));
                }

                notificationActivity.putExtra("user", mUser);
                notificationActivity.putExtra("wish", mWish);
                if(mUser!=null && mWish!=null)
                {
                    Intent intent = new Intent(CommonLib.LOCAL_FEED_BROADCAST_NOTIFICATION);
                    FeedItem feedItem = new FeedItem();
                    feedItem.setFeedId(-1);
                    feedItem.setLatitude(0);
                    feedItem.setLongitude(0);
                    feedItem.setTimestamp(System.currentTimeMillis());
                    feedItem.setType(CommonLib.FEED_TYPE_NEW_REQUEST);
                    feedItem.setUserFirst(mUser);
                    feedItem.setWish(mWish);
                    intent.putExtra("feed",feedItem);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (type != null && type.equals("wish")) {
            notificationId = NOTIFICATION_ID_FEED;
            JSONObject message = null;

            try {
                message = new JSONObject(msg);
                if (message.has("wish") && message.getJSONObject("wish").has("wish")) {
                    Wish messageObj = ParserJson.parse_Wish(message.getJSONObject("wish").getJSONObject("wish"));
                    if (messageObj != null) {
                        if (message.has("from_user") && message.getJSONObject("from_user").has("user")) {
                            User user = ParserJson.parse_User(message.getJSONObject("from_user").getJSONObject("user"));
                            boolean isFriendOnFacebook = FriendChecker.isFriendOnFacebook(prefs.getString("fbId", ""),
                                    user.getFbId(), prefs.getString("fb_token", ""));
                            if (isFriendOnFacebook) {
                                msg = user.getUserName() + " offered you a " + messageObj.getTitle()
                                        + ". Start chatting!";
                                notificationActivity = new Intent(context, MessagesActivity.class);
                                notificationActivity.putExtra("user", user);
                                notificationActivity.putExtra("wish", messageObj);
                                notificationActivity.putExtra("type", CommonLib.CURRENT_USER_WISH_ACCEPTED);
                            } else if (CommonLib.hasContact(context, user.getContact())) {
                                msg = user.getUserName() + " offered you a " + messageObj.getTitle()
                                        + ". Start chatting!";
                                notificationActivity = new Intent(context, MessagesActivity.class);
                                notificationActivity.putExtra("user", user);
                                notificationActivity.putExtra("wish", messageObj);
                                notificationActivity.putExtra("type", CommonLib.WISH_ACCEPTED_CURRENT_USER);
                            } else
                                showNotification = false;
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // check if app is alive, do not push the message notifiication then
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationActivity == null)
            notificationActivity = new Intent(context, Splash.class);
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationActivity, flags);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Baatna").setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setAutoCancel(true).setContentText(msg).setSound(soundUri);
        mBuilder.setContentIntent(contentIntent);
        if (showNotification)
            mNotificationManager.notify(notificationId, mBuilder.build());
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

    public void cancelNotification(int notificationId)
    {
        mNotificationManager.cancel(notificationId);
    }
}
