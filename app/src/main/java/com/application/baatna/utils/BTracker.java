package com.application.baatna.utils;

import android.content.Context;

import com.application.baatna.BaatnaApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class BTracker {

	// Custom Categories
	public  final static String CATEGORY_WIDGET_ACTION = "CATEGORY_WIDGET_ACTION";

	// Custom Screen Names
	public final static String SCREEN_NAME_ONLINE_ORDERING_ADD_ADDRESS_ME_TAB = "Online Ordering-Add Address From Me Tab";

	// Custom Widget Actions
	public final static String ACTION_FACEBOOK_LOGIN_PRESSED = "ACTION_FACEBOOK_LOGIN_PRESSED";
	public final static String ACTION_FAB_ICON_PRESSED = "ACTION_FAB_ICON_PRESSED";
	public final static String ACTION_MAKE_NEW_REQUEST_PRESSED = "ACTION_MAKE_NEW_REQUEST_PRESSED";
	public final static String ACTION_MAP_IMAGE_PRESSED = "ACTION_MAP_IMAGE_PRESSED";
	public final static String ACTION_WISH_POST_PRESSED = "ACTION_WISH_POST_PRESSED";
	public final static String ACTION_WISH_TICK_PRESSED = "ACTION_WISH_TICK_PRESSED";
	public final static String ACTION_MESSAGES_ICON_PRESSED = "ACTION_MESSAGES_ICON_PRESSED";
	public final static String ACTION_DRAWER_WISH_HISTORY_PRESSED = "ACTION_DRAWER_WISH_HISTORY_PRESSED";
	public final static String ACTION_DRAWER_FEEDBACK_PRESSED = "ACTION_DRAWER_FEEDBACK_PRESSED";
	public final static String ACTION_DRAWER_ABOUT_PRESSED = "ACTION_DRAWER_ABOUT_PRESSED";
	public final static String ACTION_DRAWER_SIGN_OUT_PRESSED = "ACTION_DRAWER_SIGN_OUT_PRESSED";
	public final static String ACTION_OWN_PROFILE_PRESSED = "ACTION_OWN_PROFILE_PRESSED";
	public final static String ACTION_USER_PROFILE_PRESSED = "ACTION_USER_PROFILE_PRESSED";

	// Google Analytics Event
	public static void logGAEvent(Context ctx, String categoryStr, String actionStr, String labelStr) {

		try {

			// Get tracker.
			Tracker tracker = ((BaatnaApp) ctx.getApplicationContext()).getTracker(CommonLib.TrackerName.APPLICATION_TRACKER);

			// Build and send an Event.
			tracker.send(new HitBuilders
					.EventBuilder()
					.setCategory(categoryStr)
					.setAction(actionStr)
					.setLabel(labelStr)
					.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Google Analytics Screen View
	public static void logGAScreen(Context ctx, String screenName) {

		try {
			// Get tracker.
			Tracker tracker = ((BaatnaApp) ctx.getApplicationContext()).getTracker(CommonLib.TrackerName.APPLICATION_TRACKER);

			// Set screen name.
			tracker.setScreenName(screenName);

			// Send a screen view.
			tracker.send(new HitBuilders.AppViewBuilder().build());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}