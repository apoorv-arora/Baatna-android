package com.application.baatna.utils;

import android.content.Context;

import com.application.baatna.BaatnaApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class BTracker {

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