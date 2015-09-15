package com.application.baatna.views;

import java.util.ArrayList;
import java.util.List;

import com.application.baatna.R;
import com.application.baatna.data.Message;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.db.MessageDBWrapper;
import com.application.baatna.services.IMService;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.FriendInfo;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MessagesActivity extends Activity implements UploadManagerCallback {

	private int width;

	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	private EditText messageText;
	private Button sendMessageButton;
	LayoutInflater inflater;
	private MessagesAdapter mAdapter;
	private boolean destroyed = false;
	private User currentUser;
	private Wish currentWish;

	private SharedPreferences prefs;
	private Activity mContext;
	private ListView messageList;
	private ArrayList<Message> messages = new ArrayList<Message>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.message_activity);
		mContext = this;
		prefs = getSharedPreferences(CommonLib.APP_SETTINGS, 0);
		inflater = LayoutInflater.from(this);
		messageList = (ListView) findViewById(R.id.messageList);
		width = getWindowManager().getDefaultDisplay().getWidth();

		currentUser = (User) getIntent().getExtras().getSerializable("user");
		currentWish = (Wish) getIntent().getExtras().getSerializable("wish");

		setUpActionBar();

		messageText = (EditText) findViewById(R.id.message);

		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);

		sendMessageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				UploadManager.sendMessage(currentUser.getUserId() + "", messageText.getText().toString(),
						currentWish.getWishId() + "");
			}
		});

		messageText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == 66) {
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
		});
		UploadManager.addCallback(this);

		new GetMessages().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		int message = -1;
		switch (id) {
		case MESSAGE_CANNOT_BE_SENT:
			message = R.string.about_text;
			break;
		}

		if (message == -1) {
			return null;
		} else {
			return new AlertDialog.Builder(MessagesActivity.this).setMessage(message)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							/* User clicked OK so do some stuff */
						}
					}).create();
		}
	}

	private void setUpActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		View v = inflater.inflate(R.layout.white_action_bar, null);

		v.findViewById(R.id.home_icon_container).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		try {
			v.findViewById(R.id.home_icon_zomato).setPadding(width / 80, width / 80, width / 80, width / 80);
		} catch (Exception e) {

		}

		// user handle
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setPadding(width / 80, 0, width / 40, 0);
		actionBar.setCustomView(v);

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		destroyed = true;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public void actionBarSelected(View v) {

		switch (v.getId()) {

		case R.id.home_icon_container:
			onBackPressed();

		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	public class MessagesAdapter extends ArrayAdapter<Message> {

		private List<Message> messagesList;
		private Activity mContext;
		private int width;

		public MessagesAdapter(Activity context, int resourceId, List<Message> wishes) {
			super(context.getApplicationContext(), resourceId, wishes);
			mContext = context;
			messagesList = wishes;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (messagesList == null) {
				return 0;
			} else {
				return messagesList.size();
			}
		}

		protected class ViewHolder {
			TextView messageFrom;
			TextView messageTo;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final Message message = messagesList.get(position);
			if (v == null || v.findViewById(R.id.chat_snippet) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.chat_item_snippet, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.messageFrom = (TextView) v.findViewById(R.id.message_preview_left);
				viewHolder.messageTo = (TextView) v.findViewById(R.id.message_preview_right);
				v.setTag(viewHolder);
			}

			if (message.isFromTo()) {
				viewHolder.messageFrom.setVisibility(View.GONE);
				viewHolder.messageTo.setVisibility(View.VISIBLE);
				viewHolder.messageTo.setText(message.getMessage());
			} else {
				viewHolder.messageFrom.setVisibility(View.VISIBLE);
				viewHolder.messageTo.setVisibility(View.GONE);
				viewHolder.messageFrom.setText(message.getMessage());
			}

			return v;
		}

	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.SEND_MESSAGE) {
			if (!destroyed) {
				if (status) { // add to DB with the message Id
					if (data != null && data instanceof Message) {
						MessageDBWrapper.addMessage((Message) data, ((Message) data).getFromUser().getUserId(),
								((Message) data).getWish().getWishId(), System.currentTimeMillis());
					}
				} else {// show retry button
					Toast.makeText(mContext, "Something went wrong.", Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

	private class GetMessages extends AsyncTask<Object, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				messages = MessageDBWrapper.getMessages(currentUser.getUserId(), currentWish.getWishId());
				return messages;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (destroyed)
				return;

			if (messages != null) {
				if (messages instanceof ArrayList<?>) {
					mAdapter = new MessagesAdapter(mContext, R.layout.chat_item_snippet, messages);
					messageList.setAdapter(mAdapter);
					messageList.setSelection(messages.size()-1);
				}
			} else {
				if (CommonLib.isNetworkAvailable(MessagesActivity.this)) {
					Toast.makeText(MessagesActivity.this, getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MessagesActivity.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

}
