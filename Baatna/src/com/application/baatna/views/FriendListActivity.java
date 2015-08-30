package com.application.baatna.views;

import java.util.ArrayList;
import java.util.List;

import com.application.baatna.R;
import com.application.baatna.data.FeedItem;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.FriendInfo;
import com.application.baatna.utils.IconView;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;
import com.application.baatna.views.Home.NewsFeedAdapter;
import com.application.baatna.views.Home.NewsFeedAdapter.ViewHolder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FriendListActivity extends Activity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private boolean destroyed = false;
	private AsyncTask mAsyncRunning;
	private Activity mContext;
	private FriendsAdapter mAdapter;
	private ListView feedListView;
	private int width;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.friend_list_activity);
		
		width = getWindowManager().getDefaultDisplay().getWidth();
		
		prefs = getSharedPreferences("application_settings", 0);
		mContext = this;
		setupActionBar();
		setListeners();
		refreshView();
		UploadManager.addCallback(this);
		
		feedListView = (ListView) findViewById(R.id.feedListView);
		feedListView.setDivider(new ColorDrawable(getResources().getColor(R.color.feed_bg)));
		feedListView.setDividerHeight(width / 40);
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		actionBar.setTitle(getResources().getString(R.string.messages));

		try {
			int width = getWindowManager().getDefaultDisplay().getWidth();
			findViewById(android.R.id.home).setPadding(width / 40, 0, width / 40, 0);
			ViewGroup home = (ViewGroup) findViewById(android.R.id.home).getParent();
			home.getChildAt(0).setPadding(width / 40, 0, width / 80, 0);
		} catch (Exception e) {
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// should be handled at ever activity
		if (prefs.getInt("uid", 0) == 0) {
			Intent intent = new Intent(FriendListActivity.this, BaatnaActivity.class);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			onBackPressed();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshView() {
		if (mAsyncRunning != null)
			mAsyncRunning.cancel(true);
		mAsyncRunning = new GetNewsFeedItems().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private void setListeners() {
		findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

	}
	
	private class GetNewsFeedItems extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.feedListView).setVisibility(View.GONE);
			findViewById(R.id.friend_list_progress_container).setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "messaging/get?";
				Object info = RequestWrapper.RequestHttp(url,
						RequestWrapper.NEWS_FEED, RequestWrapper.FAV);
				CommonLib.ZLog("url", url);
				return info;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (destroyed)
				return;
			findViewById(R.id.friend_list_progress_container).setVisibility(View.GONE);
			if (result != null) {
				if (result instanceof ArrayList<?>) {
					findViewById(R.id.feedListView).setVisibility(View.VISIBLE);
					mAdapter = new FriendsAdapter(mContext,
							R.layout.feed_list_item_snippet,
							(ArrayList<FeedItem>) result);
					feedListView.setAdapter(mAdapter);
				}
			} else {
				findViewById(R.id.empty_view).setVisibility(View.GONE);
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.feedListView).setVisibility(View.GONE);
				}
			}

		}
	}


	public class FriendsAdapter extends ArrayAdapter<FeedItem> {

		private List<FeedItem> feedItems;
		private Activity mContext;
		private int width;

		public FriendsAdapter(Activity context, int resourceId,
				List<FeedItem> feedItems) {
			super(context.getApplicationContext(), resourceId, feedItems);
			mContext = context;
			this.feedItems = feedItems;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (feedItems == null) {
				return 0;
			} else {
				return feedItems.size();
			}
		}

		protected class ViewHolder {
			TextView userName;
			TextView time;
			TextView distance;
			View bar;
			// RoundedImageView userImage;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final FeedItem feedItem = feedItems.get(position);

			if (v == null || v.findViewById(R.id.feed_item_root) == null) {
				v = LayoutInflater.from(mContext).inflate(
						R.layout.friend_list_item_snippet, null);
			}
			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.userName = (TextView) v.findViewById(R.id.user_name);
				// viewHolder.userImage = (RoundedImageView) v
				// .findViewById(R.id.user_image);
				viewHolder.time = (TextView) v.findViewById(R.id.time);
				// viewHolder.distance = (TextView)
				// v.findViewById(R.id.distance);
				viewHolder.bar = v.findViewById(R.id.left_bar);
				v.setTag(viewHolder);
			}

			((RelativeLayout.LayoutParams)v.findViewById(R.id.feed_item_container).getLayoutParams())
			.setMargins(width / 40, 0, width / 40, 0);
			User user = feedItem.getUserIdFirst();

			User user2 = feedItem.getUserSecond();

			final Wish wish = feedItem.getWish();

			viewHolder.time.setText(CommonLib.getDateFromUTC(feedItem
					.getTimestamp()));

			switch (feedItem.getType()) {

			case CommonLib.FEED_TYPE_NEW_USER:
				if (user != null) {
					String description = getResources()
							.getString(R.string.feed_user_joined,
									user.getUserName() + " ");
					viewHolder.userName.setText(description);
					viewHolder.bar.setBackgroundDrawable(new ColorDrawable(
							getResources().getColor(R.color.zomato_red)));
					viewHolder.bar
					.setBackgroundDrawable(new ColorDrawable(
							getResources().getColor(
									R.color.feed_joined)));
				}
				break;
			case CommonLib.FEED_TYPE_NEW_REQUEST:
				if (user != null && wish != null) {
					String description = getResources().getString(
							R.string.feed_user_requested,
							user.getUserName() + " ", wish.getTitle() + " ");
					viewHolder.userName.setText(description);
					viewHolder.bar
							.setBackgroundDrawable(new ColorDrawable(
									getResources().getColor(
											R.color.zomato_red)));

				}
				break;
			case CommonLib.FEED_TYPE_REQUEST_FULFILLED:
				String description = getResources().getString(
						R.string.feed_requested_fulfilled,
						user.getUserName() + " ", wish.getTitle() + " ",
						user2.getUserName());
				viewHolder.userName.setText(description);
				viewHolder.bar.setBackgroundDrawable(new ColorDrawable(
						getResources().getColor(R.color.bt_orange)));
				viewHolder.bar
				.setBackgroundDrawable(new ColorDrawable(
						getResources().getColor(
								R.color.feed_offered)));
				break;
				
			}
			v.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FriendListActivity.this, MessagesActivity.class);
					intent.putExtra(FriendInfo.USERNAME, "apoorv");
					intent.putExtra(FriendInfo.IP, "192.168.43.86");
					intent.putExtra(FriendInfo.PORT, "8080");
					intent.putExtra(FriendInfo.MESSAGE, "hello");
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			});
			return v;
		}


	}

	// set all the wishes here
	private void setWishes(ArrayList<FeedItem> categories) {
		mAdapter = new FriendsAdapter(mContext, R.layout.new_request_fragment, categories);
		feedListView.setAdapter(mAdapter);
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

}
