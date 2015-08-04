package com.application.baatna.db;

import java.util.ArrayList;

import android.content.Context;

import com.application.baatna.data.BLocation;

public class LocationDBWrapper {

	public static LocationDBManager helper;

	public static void Initialize(Context context) {
		helper = new LocationDBManager(context);
	}

	public static int addLocation(BLocation location, int cityId, int userId,
			long timestamp) {
		return helper.addLocation(location, cityId, userId, timestamp);
	}

	public static ArrayList<BLocation> getLocations(int cityId, int userId) {
		return helper.getLocations(cityId, userId);
	}
}