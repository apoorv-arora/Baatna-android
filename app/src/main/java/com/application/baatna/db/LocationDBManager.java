package com.application.baatna.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.application.baatna.data.BLocation;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;

public class LocationDBManager extends SQLiteOpenHelper {

	private static final String ID = "ID";
	private static final String USERID = "UserID";
	private static final String CITYID = "CityID";
	private static final String TYPE = "Type";
	private static final String TIMESTAMP = "Timestamp";
	private static final String BUNDLE = "Bundle";
	SQLiteDatabase db;

	private static final int DATABASE_VERSION = 2;
	private static final String CACHE_TABLE_NAME = "LOCATIONS";
	private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "
			+ CACHE_TABLE_NAME + " (" + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + USERID
			+ " INTEGER, " + TIMESTAMP + " INTEGER, " + TYPE + " INTEGER, "
			+ CITYID + " INTEGER, " + BUNDLE + " BLOB);";
	private static final String DATABASE_NAME = "LOCATIONSDB";
	Context ctx;

	public LocationDBManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		ctx = context;
	}

	public LocationDBManager(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public int addLocation(BLocation location, int cityId, int userId,
			long timestamp) {

		ArrayList<BLocation> locations = getLocations(cityId, userId);
		int result = -1;

		try {

			this.getReadableDatabase();

			SQLiteDatabase db = ctx.openOrCreateDatabase(
					"/data/data/com.application.baatna/databases/"
							+ DATABASE_NAME, SQLiteDatabase.OPEN_READWRITE,
					null);
			ContentValues values = new ContentValues();
			values.put(TIMESTAMP, timestamp);

			if (locations.contains(location)) {
				result = (int) db.update(CACHE_TABLE_NAME, values, TYPE + "=?",
						new String[] { location.getEntityType() + "="
								+ location.getEntityId() });

				CommonLib.ZLog("zloc addlocations if ",
						userId + " : " + location.getEntityType() + "="
								+ location.getEntityId());

			} else {

				byte[] bundle = RequestWrapper.Serialize_Object(location);

				values.put(USERID, userId);
				values.put(TYPE,
						location.getEntityType() + "=" + location.getEntityId());
				values.put(BUNDLE, bundle);
				values.put(CITYID, cityId);

				// Inserting Row
				result = (int) db.insert(CACHE_TABLE_NAME, null, values);
				CommonLib.ZLog("zloc addlocations else ",
						userId + " . " + location.getEntityType() + "="
								+ location.getEntityId());
			}

			db.close();
			this.close();
		} catch (Exception E) {
			try {
				this.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			result = -1;
		}
		return result;
		// Closing database connection
	}

	public ArrayList<BLocation> getLocations(int cityId, int userId) {
		BLocation location;
		this.getReadableDatabase();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		ArrayList<BLocation> queries = new ArrayList<BLocation>();

		try {
			db = ctx.openOrCreateDatabase(
					"/data/data/com.application.baatna/databases/"
							+ DATABASE_NAME, SQLiteDatabase.OPEN_READONLY, null);
			cursor = db.query(
					CACHE_TABLE_NAME,
					new String[] { ID, USERID, TIMESTAMP, TYPE, BUNDLE },
					CITYID + "=? AND " + USERID + "=?",
					new String[] { Integer.toString(cityId),
							Integer.toString(userId) }, null, null, TIMESTAMP
							+ " DESC", "20");
			if (cursor != null)
				cursor.moveToFirst();

			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				location = (BLocation) RequestWrapper.Deserialize_Object(
						cursor.getBlob(4), "");
				queries.add(location);
			}

			cursor.close();
			db.close();
			this.close();
			return queries;
		} catch (SQLiteException e) {

			this.close();
		} catch (Exception E) {
			try {
				cursor.close();
				db.close();
				this.close();
			} catch (Exception ec) {
				try {
					db.close();
				} catch (Exception e) {
					this.close();
				}
				this.close();
			}
		}
		return queries;
	}

}