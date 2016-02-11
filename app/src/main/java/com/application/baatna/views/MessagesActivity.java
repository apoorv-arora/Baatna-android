package com.application.baatna.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.data.Message;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.db.MessageDBWrapper;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagesActivity extends AppCompatActivity implements UploadManagerCallback {

	private int width;

	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	private EditText messageText;
	private View sendMessageButton;
	LayoutInflater inflater;
	private MessagesAdapter mAdapter;
	private boolean destroyed = false;
	private User currentUser;
	private Wish currentWish;

	private SharedPreferences prefs;
	private Activity mContext;
	private ListView messageList;
	private ArrayList<Message> messages = new ArrayList<Message>();
	private BaatnaApp zapp;
	private int type;
	private ProgressDialog zProgressDialog;

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent != null && intent.getExtras() != null && intent.hasExtra("message")) {
			// new GetMessages().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			mAdapter.add((Message) intent.getSerializableExtra("message"));
			mAdapter.notifyDataSetChanged();
			messageList.setSelection(mAdapter.getCount() - 1);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_messages, menu);
		return true;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.message_activity);

		mContext = this;
		zapp = (BaatnaApp) getApplication();
		prefs = getSharedPreferences(CommonLib.APP_SETTINGS, 0);
		inflater = LayoutInflater.from(this);
		messageList = (ListView) findViewById(R.id.messageList);
		width = getWindowManager().getDefaultDisplay().getWidth();

		currentUser = (User) getIntent().getExtras().getSerializable("user");
		currentWish = (Wish) getIntent().getExtras().getSerializable("wish");
		if (getIntent().getExtras().containsKey("type") && getIntent().getExtras().get("type") instanceof Integer)
			type = getIntent().getExtras().getInt("type");
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);
		setUpActionBar();

		messageText = (EditText) findViewById(R.id.message);

		messageText.setPadding(width / 20, width / 20, width / 20, width / 20);

		// findViewById(R.id.send_container).setPadding(width / 20, width / 20,
		// width / 20, width / 20);

		// sendMessageButton = ;

		findViewById(R.id.send_icon).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				long objectId = System.currentTimeMillis();
				String message = messageText.getText().toString();
				if (message == null || message.length() < 1)
					return;
				UploadManager.sendMessage(currentUser.getUserId() + "", message, currentWish.getWishId() + "",
						objectId);
				messageText.setText("");

				Message msg = new Message();
				msg.setFromTo(true);
				msg.setMessage(message);
				msg.setToUser(currentUser);
				msg.setWish(currentWish);
				msg.setMessageId(objectId);

				messages.add(msg);
				mAdapter.notifyDataSetChanged();
				messageList.setSelection(messages.size() - 1);
			}
		});

		messageText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == 66) {
					long objectId = System.currentTimeMillis();
					String message = messageText.getText().toString();
					UploadManager.sendMessage(currentUser.getUserId() + "", message, currentWish.getWishId() + "",
							objectId);
					messageText.setText("");

					Message msg = new Message();
					msg.setFromTo(true);
					msg.setMessage(message);
					msg.setToUser(currentUser);
					msg.setWish(currentWish);
					msg.setMessageId(objectId);

					messages.add(msg);
					mAdapter.notifyDataSetChanged();
					messageList.setSelection(messages.size() - 1);
					return true;
				}
				return false;
			}
		});
		UploadManager.addCallback(this);

		new GetMessages().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

		messageList.setDividerHeight(width / 20);
		messageList.setHeaderDividersEnabled(true);
		messageList.setFooterDividersEnabled(true);
		setListeners();

		if (type == CommonLib.CURRENT_USER_WISH_ACCEPTED) {
			// subtitle.setText("Offered YOU A "+ currentWish.getTitle());
			//set background
			setFragmentBackground();
			String Text=" Product Received";

			if (currentWish.getStatus() == CommonLib.STATUS_OFFERED) {
				((TextView) findViewById(R.id.product_status)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.messages_tick_icon)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.product_status)).setText(Text.toUpperCase());
				((TextView) findViewById(R.id.product_status)).setTextColor(getResources().getColor(R.color.item_received_color));
				((TextView) findViewById(R.id.messages_tick_icon)).setTextColor(getResources().getColor(R.color.item_received_color));

			} else if (currentWish.getStatus() == CommonLib.STATUS_RECEIVED) {
				// check if the product is received
			} else if (currentWish.getStatus() == CommonLib.STATUS_ACTIVE) {
				((TextView) findViewById(R.id.product_status)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.messages_tick_icon)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.product_status)).setText(Text);
				((TextView) findViewById(R.id.product_status)).setTextColor(getResources().getColor(R.color.item_received_color));
				((TextView)findViewById(R.id.messages_tick_icon)).setTextColor(getResources().getColor(R.color.item_received_color));
			} else {
				((TextView) findViewById(R.id.product_status)).setVisibility(View.GONE);
				((TextView) findViewById(R.id.messages_tick_icon)).setVisibility(View.GONE);
			}
		} else if (type == CommonLib.WISH_ACCEPTED_CURRENT_USER) {
			// subtitle.setText("REQUESTED FOR A "+ currentWish.getTitle());
			String Text="Product Offered";

			if (currentWish.getStatus() == CommonLib.STATUS_OFFERED) {
				// check if this user is in the accepted list, if it is, make
				// the view gone, else visible
			} else if (currentWish.getStatus() == CommonLib.STATUS_RECEIVED) {
				((TextView) findViewById(R.id.product_status)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.messages_tick_icon)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.product_status)).setText(Text.toUpperCase());
				((TextView) findViewById(R.id.product_status)).setTextColor(getResources().getColor(R.color.item_offered_color));
				((TextView)findViewById(R.id.messages_tick_icon)).setTextColor(getResources().getColor(R.color.item_offered_color));


			} else if (currentWish.getStatus() == CommonLib.STATUS_ACTIVE) {
				((TextView) findViewById(R.id.product_status)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.messages_tick_icon)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.product_status)).setText(Text.toUpperCase());
				((TextView) findViewById(R.id.product_status)).setTextColor(getResources().getColor(R.color.item_offered_color));
				((TextView)findViewById(R.id.messages_tick_icon)).setTextColor(getResources().getColor(R.color.item_offered_color));


			} else {
				((TextView) findViewById(R.id.product_status)).setVisibility(View.GONE);
				((TextView) findViewById(R.id.messages_tick_icon)).setVisibility(View.GONE);
			}
		}

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mNotificationReceived,
				new IntentFilter(CommonLib.LOCAL_BROADCAST_NOTIFICATION));

	}

	private BroadcastReceiver mNotificationReceived = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				if (intent != null && intent.getExtras() != null && intent.hasExtra("message")) {
					// new
					// GetMessages().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
					// check for user and wish

					User user = (User) intent.getSerializableExtra("user");
					Wish wish = (Wish) intent.getSerializableExtra("wish");

					if (wish.getWishId() == currentWish.getWishId() && user.getUserId() == currentUser.getUserId()) {
						mAdapter.add((Message) intent.getSerializableExtra("message"));
						mAdapter.notifyDataSetChanged();
						messageList.setSelection(mAdapter.getCount() - 1);
						NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						notificationManager.cancel(com.application.baatna.utils.NotificationManager.NOTIFICATION_ID_MESSAGE);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void setListeners() {

		findViewById(R.id.dropdown_setting).setVisibility(View.VISIBLE);

		findViewById(R.id.product_status).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int action = -1;
				if (type == CommonLib.CURRENT_USER_WISH_ACCEPTED) {
					action = CommonLib.ACTION_WISH_RECEIVED;
					UploadManager.updateWishOfferedStatus(prefs.getString("access_token", ""),
							"" + currentWish.getWishId(), action + "");
				} else if (type == CommonLib.WISH_ACCEPTED_CURRENT_USER) {
					action = CommonLib.ACTION_WISH_OFFERED;
					UploadManager.updateWishOfferedStatus(prefs.getString("access_token", ""),
							"" + currentWish.getWishId(), action + "");
				}
			}
		});
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
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		View v = inflater.inflate(R.layout.messages_action_bar, null);
		v.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(v);
		v.findViewById(R.id.back_icon).setPadding(width / 20, 0, width / 20, 0);

		// user handle
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setPadding(width / 80, 0, width / 40, 0);

		TextView subtitle = (TextView) v.findViewById(R.id.subtitle);
		subtitle.setPadding(width / 80, 0, width / 40, 0);

		title.setText(currentUser.getUserName());
		if (type == CommonLib.CURRENT_USER_WISH_ACCEPTED) {
			if (Build.VERSION.SDK_INT >= 21) {
				Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(getResources().getColor(R.color.item_received_color));
			}

			String temp="OFFERED YOU A " + currentWish.getTitle().toUpperCase();
			Spannable subtext=new SpannableString(temp);
			Pattern p= Pattern.compile(currentWish.getTitle(), Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(subtext);
			while (m.find()){


				subtext.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.item_received_color)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				subtext.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}
			subtitle.setText(subtext);
		}
		else if (type == CommonLib.WISH_ACCEPTED_CURRENT_USER) {
			if (Build.VERSION.SDK_INT >= 21) {
				Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(getResources().getColor(R.color.item_offered_color));
			}

			String temp="REQUESTED FOR A " + currentWish.getTitle().toUpperCase();

			Spannable subtext=new SpannableString(temp);
			Pattern p= Pattern.compile(currentWish.getTitle(), Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(subtext);
			while (m.find()){
				subtext.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.item_offered_color)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				subtext.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			}
			subtitle.setText(subtext);
		}

		ImageView imageView = (ImageView) v.findViewById(R.id.user_chat_head);
		setImageFromUrlOrDisk(currentUser.getImageUrl(), imageView, "", width, width, false);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		destroyed = true;
		if (zProgressDialog != null && zProgressDialog.isShowing())
			zProgressDialog.dismiss();
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mNotificationReceived);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();

		try {
			InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(findViewById(R.id.message).getWindowToken(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public void actionBarSelected(View v) {

		switch (v.getId()) {

			case R.id.home_icon_container:
				onBackPressed();
				break;
			default:
				break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}

		else if (item.getItemId() == R.id.block) {
			zProgressDialog = ProgressDialog.show(MessagesActivity.this, null,
					getResources().getString(R.string.sending_request), true, false);

			UploadManager.blockUser(currentUser.getUserId()+"");

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
						MessageDBWrapper.addMessage((Message) data, ((Message) data).getToUser().getUserId(),
								((Message) data).getWish().getWishId(), System.currentTimeMillis());
						// messages.add((Message) data);
						// mAdapter.notifyDataSetChanged();
						// messageList.setSelection(messages.size()-1);
					}
				} else {// show retry button
					Toast.makeText(mContext, "Something went wrong.", Toast.LENGTH_SHORT).show();
				}
			}
		} else if (requestType == CommonLib.WISH_OFFERED_STATUS && !destroyed) {
			if (zProgressDialog != null && zProgressDialog.isShowing())
				zProgressDialog.dismiss();
			findViewById(R.id.product_status).setVisibility(View.GONE);
			findViewById(R.id.messages_tick_icon).setVisibility(View.GONE);
		} else if (requestType == CommonLib.BLOCK_USER && !destroyed) {
			if (zProgressDialog != null && zProgressDialog.isShowing())
				zProgressDialog.dismiss();

			if(status)
				finish();
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (requestType == CommonLib.WISH_OFFERED_STATUS && !destroyed) {
			zProgressDialog = ProgressDialog.show(MessagesActivity.this, null,
					getResources().getString(R.string.hang_on), true, false);
			zProgressDialog.setCancelable(true);
		}
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
					messageList.setSelection(messages.size() - 1);
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

	private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
									   int height, boolean useDiskCache) {

		if (cancelPotentialWork(url, imageView)) {

			GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), zapp.cache.get(url + type), task);
			imageView.setImageDrawable(asyncDrawable);
			if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
					&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
					&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
				((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
			}
			if (zapp.cache.get(url + type) == null) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1L);
			} else if (imageView != null && imageView.getDrawable() != null
					&& ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
				imageView.setBackgroundResource(0);
				Bitmap blurBitmap = null;
				if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
					((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
				}
			}
		}
	}

	private class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<GetImage> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<GetImage>(bitmapWorkerTask);
		}

		public GetImage getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public boolean cancelPotentialWork(String data, ImageView imageView) {
		final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final String bitmapData = bitmapWorkerTask.url;
			if (!bitmapData.equals(data)) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}

	private GetImage getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	private class GetImage extends AsyncTask<Object, Void, Bitmap> {

		String url = "";
		private WeakReference<ImageView> imageViewReference;
		private int width;
		private int height;
		boolean useDiskCache;
		String type;
		Bitmap blurBitmap;

		public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type) {
			this.url = url;
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.width = width;
			this.height = height;
			this.useDiskCache = true;// useDiskCache;
			this.type = type;
		}

		@Override
		protected void onPreExecute() {
			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar)
					((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
			}
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;
			try {

				String url2 = url + type;

				if (destroyed && (imageViewReference.get() != findViewById(R.id.user_image))) {
					return null;
				}

				if (useDiskCache) {
					bitmap = CommonLib.getBitmapFromDisk(url2, getApplicationContext());
				}

				if (bitmap == null) {
					try {
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inJustDecodeBounds = true;
						BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

						opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
						opts.inJustDecodeBounds = false;

						bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

						if (useDiskCache) {
							CommonLib.writeBitmapToDisk(url2, bitmap, getApplicationContext(),
									Bitmap.CompressFormat.JPEG);
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Error e) {

					}
				}

				if (bitmap != null) {

					bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);
					synchronized (zapp.cache) {
						zapp.cache.put(url2, bitmap);
					}
				}

			} catch (Exception e) {
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (!destroyed) {
				if (isCancelled()) {
					bitmap = null;
				}
				if (imageViewReference != null && bitmap != null) {
					final ImageView imageView = imageViewReference.get();
					if (imageView != null) {
						imageView.setImageBitmap(bitmap);
						if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
								&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
								&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
							((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
						}
					}
				}
			}
		}
	}
	public void setFragmentBackground()
	{
		if(type == CommonLib.WISH_ACCEPTED_CURRENT_USER)
		{
			messageList.setBackgroundColor(getResources().getColor(R.color.item_offered_color));
			((RelativeLayout) findViewById(R.id.parent_layout)).setBackgroundColor(getResources().getColor(R.color.item_offered_color));
			Log.e("orange color","set");
		}
	}

}
