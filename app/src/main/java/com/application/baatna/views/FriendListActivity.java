package com.application.baatna.views;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.data.User;
import com.application.baatna.data.UserComactMessage;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.TypefaceSpan;
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

public class FriendListActivity extends AppCompatActivity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private boolean destroyed = false;
	private AsyncTask mAsyncRunning;
	private Activity mContext;
	private FriendsAdapter mAdapter;
	private ListView feedListView;
	private int width;

	private BaatnaApp zapp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.friend_list_activity);

		width = getWindowManager().getDefaultDisplay().getWidth();
		zapp = (BaatnaApp) getApplication();

		prefs = getSharedPreferences("application_settings", 0);
		mContext = this;
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);
		setupActionBar();
		setListeners();
		refreshView();


		UploadManager.addCallback(this);

		feedListView = (ListView) findViewById(R.id.feedListView);
		feedListView.setDivider(new ColorDrawable(getResources().getColor(R.color.feed_bg)));
		feedListView.setDividerHeight(width / 40);
		feedListView.setTextFilterEnabled(true);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.option_menu, menu);
		final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.animate();
		searchView.setIconified(true);
		searchView.setVisibility(View.GONE);
		SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextChange(String newText)
			{
				// this is your adapter that will be filtered
				mAdapter.getFilter().filter(newText);
				return true;
			}
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				// this is your adapter that will be filtered
				mAdapter.getFilter().filter(query);
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				return true;
			}




		};
		searchView.setOnQueryTextListener(textChangeListener);

		return super.onCreateOptionsMenu(menu);

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

	private void setupActionBar() {

		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.messages));
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

		((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
				.setMargins(width / 40, 0, 0, 0);
		actionBarCustomView.findViewById(R.id.title).setPadding(width / 20, 0, width / 40, 0);
		title.setText(s);
		title.setAllCaps(true);
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
			Intent intent = new Intent(FriendListActivity.this, Splash.class);
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
		mAsyncRunning = new GetMessagesFeedItems().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private void setListeners() {
		findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

	}

	private class GetMessagesFeedItems extends AsyncTask<Object, Void, Object> {

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
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.MESSAGES_COMPACT, RequestWrapper.FAV);
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
					mAdapter = new FriendsAdapter(mContext, R.layout.friend_list_item_snippet,
							(ArrayList<UserComactMessage>) result);
					feedListView.setAdapter(mAdapter);
					if( ((ArrayList<UserComactMessage>) result).size()  == 0 ) {
						findViewById(R.id.feedListView).setVisibility(View.GONE);
						findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
						((TextView)findViewById(R.id.empty_view_text)).setText("Nothing here yet");

					} else {
						findViewById(R.id.feedListView).setVisibility(View.VISIBLE);
						findViewById(R.id.empty_view).setVisibility(View.GONE);
					}
				}
			} else {
				findViewById(R.id.empty_view).setVisibility(View.GONE);
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.feedListView).setVisibility(View.GONE);
				}
			}

		}
	}

	public class FriendsAdapter extends ArrayAdapter<UserComactMessage> implements Filterable{

		public List<UserComactMessage> messageItems;
		public List<UserComactMessage> orig;

		private Activity mContext;
		private int width;

		public FriendsAdapter(Activity context, int resourceId, List<UserComactMessage> feedItems) {
			super(context.getApplicationContext(), resourceId, feedItems);
			mContext = context;
			this.messageItems = feedItems;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (messageItems == null) {
				return 0;
			} else {
				return messageItems.size();
			}
		}

		public Filter getFilter() {
			return new Filter() {

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {

					constraint = constraint.toString().toLowerCase();
					FilterResults oReturn = new FilterResults();
					if (orig == null)
						orig = messageItems;
					if (constraint != null && constraint.toString().length() > 0) {


						List<UserComactMessage> results = new ArrayList<UserComactMessage>();
						for (UserComactMessage g : orig) {
							if (g.getWish().getTitle().toLowerCase().contains(constraint)||g.getUser().getUserName().toLowerCase().contains(constraint))
							{
								results.add(g);
							Log.e("filter",g.getWish().getTitle());}
						}
						oReturn.values = results;
						oReturn.count = results.size();
					} else {
						oReturn.values = orig;
						oReturn.count = orig.size();
					}
					return oReturn;
				}



				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint,
											  FilterResults results) {
					messageItems = (List<UserComactMessage>) results.values;
					notifyDataSetChanged();
				}
			};
		}


		protected class ViewHolder {
			TextView userName;
			TextView time;
			TextView distance;
			View bar;
			ImageView imageView;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final UserComactMessage feedItem = messageItems.get(position);

			if (v == null || v.findViewById(R.id.feed_item_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.friend_list_item_snippet, null);
			}
			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.userName = (TextView) v.findViewById(R.id.user_name);
				viewHolder.time = (TextView) v.findViewById(R.id.time);
				viewHolder.distance = (TextView) v.findViewById(R.id.distance);
				viewHolder.bar = v.findViewById(R.id.left_bar);
				viewHolder.imageView = (ImageView) v.findViewById(R.id.user_image);
				v.setTag(viewHolder);
			}

			((RelativeLayout.LayoutParams) v.findViewById(R.id.feed_item_container).getLayoutParams())
					.setMargins(width / 40, 0, width / 40, 0);

			final User user = feedItem.getUser();

			final Wish wish = feedItem.getWish();
			//setting text to display time
			viewHolder.time.setText(CommonLib.findDateDifference(feedItem.getTimestamp()));
			//displaying distance

			int distance = CommonLib.distFrom(prefs.getFloat("lat", 0), prefs.getFloat("lon", 0),
					feedItem.getLatitude(), feedItem.getLongitude());
			//distance in km
			distance=(distance/1000);
			//if(distance < 5)
				viewHolder.distance.setText(distance+" KM");
			//else
				//viewHolder.distance.setVisibility(View.GONE);

			viewHolder.imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FriendListActivity.this, UserPageActivity.class);
					if (user != null && user.getUserId() != prefs.getInt("uid", 0))
						intent.putExtra("uid", user.getUserId());
					startActivity(intent);
				}
			});

			v.findViewById(R.id.feed_item).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FriendListActivity.this, MessagesActivity.class);
					intent.putExtra("user", user);
					intent.putExtra("wish", wish);
					intent.putExtra("type", feedItem.getType());
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			});

			switch (feedItem.getType()) {

			case CommonLib.CURRENT_USER_WISH_ACCEPTED:
				if (user != null) {
					setImageFromUrlOrDisk(user.getImageUrl(), viewHolder.imageView, "", width, width, false);
					String description = getResources().getString(R.string.message_user_requested,
							user.getUserName() + " ", wish.getTitle().toUpperCase());
					//making sapnnable string to make changes to color and format of description
					Spannable desc=new SpannableString(description);
					Pattern p = Pattern.compile(user.getUserName(), Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(description);
					while (m.find()){
						desc.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					}
					p=Pattern.compile(wish.getTitle(), Pattern.CASE_INSENSITIVE);
					m = p.matcher(description);
					while (m.find()){


						desc.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.item_received_color)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						desc.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					}
					viewHolder.userName.setText(desc);
					viewHolder.bar
							.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.item_received_color)));

				}
				break;
			case CommonLib.WISH_ACCEPTED_CURRENT_USER:
				if (user != null && wish != null) {
					String description = getResources().getString(R.string.message_requested_fulfilled,
							wish.getTitle().toUpperCase() + " ", user.getUserName() + " ");
					//making sapnnable string to make changes to color and format of description
					Spannable desc=new SpannableString(description);
					Pattern p = Pattern.compile(user.getUserName(), Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(description);
					while (m.find()){

						desc.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					}
					p=Pattern.compile(wish.getTitle(), Pattern.CASE_INSENSITIVE);
					m = p.matcher(description);
					while (m.find()){

						desc.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.item_offered_color)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						desc.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					}


					setImageFromUrlOrDisk(user.getImageUrl(), viewHolder.imageView, "", position, width, false);

					viewHolder.userName.setText(desc);
					viewHolder.bar
							.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.item_offered_color)));

				}
				break;
			}
			return v;
		}
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
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

}
