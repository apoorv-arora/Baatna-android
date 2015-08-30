package com.application.baatna.db;

import java.util.ArrayList;

import com.application.baatna.data.Message;

import android.content.Context;

public class MessageDBWrapper {

	public static MessageDBManager helper;

	public static void Initialize(Context context) {
		helper = new MessageDBManager(context);
	}

	public static int addMessage(Message location, int userId,
			long timestamp) {
		return helper.addMessage(location, userId, timestamp);
	}

	public static ArrayList<Message> getMessages( int userId) {
		return helper.getMessages(userId);
	}
}