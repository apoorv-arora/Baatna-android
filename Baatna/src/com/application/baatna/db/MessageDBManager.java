package com.application.baatna.db;

import java.util.ArrayList;

import com.application.baatna.data.Message;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDBManager extends SQLiteOpenHelper {

	private static final String ID = "ID";
	private static final String MESSAGEID = "MessageID";
	private static final String TYPE = "Type";
	private static final String TIMESTAMP = "Timestamp";
	private static final String BUNDLE = "Bundle";
	SQLiteDatabase db;

	private static final int DATABASE_VERSION = 2;
	private static final String CACHE_TABLE_NAME = "MESSAGES";
	private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE " + CACHE_TABLE_NAME + " (" + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + MESSAGEID + " INTEGER, " + TIMESTAMP + " INTEGER, "
			+ TYPE + " INTEGER, " + BUNDLE + " BLOB);";
	private static final String DATABASE_NAME = "MESSAGESDB";
	Context ctx;

	public MessageDBManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		ctx = context;
	}

	public MessageDBManager(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public int addMessage(Message location, int userId, long timestamp) {

		ArrayList<Message> locations = getMessages(userId);
		int result = -1;

		try {

			this.getReadableDatabase();

			SQLiteDatabase db = ctx.openOrCreateDatabase("/data/data/com.application.baatna/databases/" + DATABASE_NAME,
					SQLiteDatabase.OPEN_READWRITE, null);
			ContentValues values = new ContentValues();
			values.put(TIMESTAMP, timestamp);

			if (locations.contains(location)) {
				result = (int) db.update(CACHE_TABLE_NAME, values, TYPE + "=?",
						new String[] { location.getMessageId() + "" });

				CommonLib.ZLog("zloc addlocations if ", userId + " : " + location.getMessageId() + "");

			} else {

				byte[] bundle = RequestWrapper.Serialize_Object(location);

				values.put(MESSAGEID, userId);
				values.put(TYPE, location.getMessageId());
				values.put(BUNDLE, bundle);

				// Inserting Row
				result = (int) db.insert(CACHE_TABLE_NAME, null, values);
				CommonLib.ZLog("zloc addlocations else ", userId + " . " + location.getMessageId());
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

	public ArrayList<Message> getMessages(int userId) {
		Message location;
		this.getReadableDatabase();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		ArrayList<Message> queries = new ArrayList<Message>();

		try {
			db = ctx.openOrCreateDatabase("/data/data/com.application.baatna/databases/" + DATABASE_NAME,
					SQLiteDatabase.OPEN_READONLY, null);
			cursor = db.query(CACHE_TABLE_NAME, new String[] { ID, MESSAGEID, TIMESTAMP, TYPE, BUNDLE },
					MESSAGEID + "=?",
					new String[] { Integer.toString(userId) }, null, null,
					TIMESTAMP + " DESC", "20");
			if (cursor != null)
				cursor.moveToFirst();

			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				location = (Message) RequestWrapper.Deserialize_Object(cursor.getBlob(4), "");
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