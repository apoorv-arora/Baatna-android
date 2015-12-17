package com.application.baatna.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.CustomTypefaceSpan;
import com.application.baatna.utils.IconView;
import com.application.baatna.utils.RequestWrapper;
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

/**
 * Displays user name, bio and profile pic
 */
public class UserPageActivity extends AppCompatActivity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private int width;
	private boolean destroyed = false;
	private BaatnaApp zapp;
	private int userId;
	boolean isSecondProfile = false;
	ImageView imageView;
	AsyncTask mAsyncRunning;

	private AsyncTask mListAsyncRunning;
	private WishesAdapter mAdapter;
	private ListView mListView;
	ArrayList<Wish> wishes;
	LinearLayout mListViewFooter;
	private int mWishesTotalCount;
	private boolean cancelled = false;
	private boolean loading = false;
	private int count = 10;

	public static final int WISH_OWN = 2;

	User user;
	private ProgressDialog z_ProgressDialog;


	@Override
	protected void onCreate(Bundle arg0) {
		overridePendingTransition(R.anim.rotation,R.anim.rotation1);
		super.onCreate(arg0);
		setContentView(R.layout.user_page_activity);
		zapp = (BaatnaApp) getApplication();

		UploadManager.addCallback(this);
		prefs = getSharedPreferences("application_settings", 0);
		width = getWindowManager().getDefaultDisplay().getWidth();
		if (getIntent() != null && getIntent().getExtras() != null) {
			Bundle extras = getIntent().getExtras();
			if (extras.containsKey("uid")) {
				userId = extras.getInt("uid");
				isSecondProfile = true;
				// start the loader
				findViewById(R.id.userpage_progress_container).setVisibility(View.VISIBLE);
				findViewById(R.id.content_container).setVisibility(View.GONE);
				findViewById(R.id.empty_view).setVisibility(View.GONE);
				refreshView();
			}
		}
		if (!isSecondProfile)
			userId = prefs.getInt("uid", 0);
		mListView = (ListView) findViewById(R.id.wish_items);
		mListView.setDivider(new ColorDrawable(getResources().getColor(R.color.feed_bg)));
		mListView.setDividerHeight(width / 40);
		fixSizes();
		setListeners();
		loadWishes();
	}

	private void refreshView() {
		if (mAsyncRunning != null)
			mAsyncRunning.cancel(true);
		mAsyncRunning = new GetUserDetails().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetUserDetails extends AsyncTask<Object, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "user/details?user_id=" + userId;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.USER_INFO, RequestWrapper.FAV);
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
			findViewById(R.id.userpage_progress_container).setVisibility(View.GONE);

			if (result != null) {
				if (result instanceof User) {
					user = (User) result;
					findViewById(R.id.content_container).setVisibility(View.VISIBLE);
					setImageFromUrlOrDisk(user.getImageUrl(), imageView, "profile", width, width, false);
					((TextView) findViewById(R.id.name)).setText(user.getUserName());
					((TextView) findViewById(R.id.description)).setText(user.getBio());
				}
			} else {
				findViewById(R.id.empty_view).setVisibility(View.GONE);
				findViewById(R.id.content_container).setVisibility(View.GONE);
				if (CommonLib.isNetworkAvailable(UserPageActivity.this)) {
					Toast.makeText(UserPageActivity.this, getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(UserPageActivity.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

	private void setListeners() {
		findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (prefs.getInt("uid", 0) == 0) {
			Intent intent = new Intent(UserPageActivity.this, Splash.class);
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

	ImageView imageViewBlur;

	private void fixSizes() {
		findViewById(R.id.name).setPadding(width / 20, width / 20, width / 20, width / 20);
		findViewById(R.id.description).setPadding(width / 20, width / 20, width / 20, width / 20);
		imageView = ((ImageView) findViewById(R.id.user_image));
		imageViewBlur = (ImageView) findViewById(R.id.drawer_user_info_background_image);

		((RelativeLayout.LayoutParams) findViewById(R.id.back_icon).getLayoutParams()).setMargins(width / 20,
				width / 15, width / 20, width / 20);

		findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		if (!isSecondProfile) {
			setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageView, "profile", width, width, false);
			((TextView) findViewById(R.id.name)).setText(prefs.getString("username", ""));
			((TextView) findViewById(R.id.description)).setText(prefs.getString("description", ""));
		}
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		super.onDestroy();
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
				if (imageViewBlur != null) {
					blurBitmap = CommonLib.fastBlur(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 10);
				}
				if (imageViewBlur != null && blurBitmap != null) {
					imageViewBlur.setImageBitmap(blurBitmap);
				}
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
//					if(type.equalsIgnoreCase("profile"))
//					{
//						bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);
//					}
					synchronized (zapp.cache) {
						zapp.cache.put(url2, bitmap);
					}
					if (imageViewBlur != null) {
						blurBitmap = CommonLib.fastBlur(bitmap, 4);
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
				if (imageViewBlur != null && blurBitmap != null) {
					imageViewBlur.setImageBitmap(blurBitmap);
				}
			}
		}
	}

	// Post this only wishes code
	private void loadWishes() {
		if (mListAsyncRunning != null)
			mListAsyncRunning.cancel(true);
		mListAsyncRunning = new GetWishes().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetWishes extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.wish_items).setVisibility(View.GONE);
			findViewById(R.id.wish_items_progress).setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "wish/get?type=" + WISH_OWN + "&start=0&count=" + count + "&another_user="
						+ userId;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.WISH_LIST, RequestWrapper.FAV);
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

			findViewById(R.id.wish_items_progress).setVisibility(View.GONE);

			if (result != null) {
				findViewById(R.id.wish_items).setVisibility(View.VISIBLE);
				if (result instanceof Object[]) {
					Object[] arr = (Object[]) result;
					mWishesTotalCount = (Integer) arr[0];
					setWishes((ArrayList<Wish>) arr[1]);
				}
			} else {
				if (CommonLib.isNetworkAvailable(UserPageActivity.this)) {
					Toast.makeText(UserPageActivity.this,
							UserPageActivity.this.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(UserPageActivity.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

				}
			}

		}
	}

	public class WishesAdapter extends ArrayAdapter<Wish> {

		private List<Wish> wishes;
		private Activity mContext;
		private int width;

		public WishesAdapter(Activity context, int resourceId, List<Wish> wishes) {
			super(context.getApplicationContext(), resourceId, wishes);
			mContext = context;
			this.wishes = wishes;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (wishes == null) {
				return 0;
			} else {
				return wishes.size();
			}
		}

		protected class ViewHolder {
			TextView title;
			TextView date;
			IconView crossIcon;

			TextView userName;
			TextView time;
			TextView distance;
			View bar;
			TextView accept;
			TextView decline;
			LinearLayout action_container;
			ImageView imageView;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final Wish wish = wishes.get(position);
			if (isSecondProfile) {

				// from
				if (v == null || v.findViewById(R.id.feed_item_root) == null) {
					v = LayoutInflater.from(mContext).inflate(R.layout.feed_list_item_snippet, null);
				}

				ViewHolder viewHolder = (ViewHolder) v.getTag();
				if (viewHolder == null) {
					viewHolder = new ViewHolder();
					viewHolder.userName = (TextView) v.findViewById(R.id.user_name);
					// viewHolder.userImage = (RoundedImageView) v
					// .findViewById(R.id.user_image);
					viewHolder.time = (TextView) v.findViewById(R.id.time);
					viewHolder.distance = (TextView) v.findViewById(R.id.distance);
					viewHolder.bar = v.findViewById(R.id.left_bar);
					viewHolder.accept = (TextView) v.findViewById(R.id.accept_button);
					viewHolder.decline = (TextView) v.findViewById(R.id.decline_button);
					viewHolder.action_container = (LinearLayout) v.findViewById(R.id.action_container);
					viewHolder.imageView = (ImageView) v.findViewById(R.id.user_image);
					v.setTag(viewHolder);
				}

				((RelativeLayout.LayoutParams) v.findViewById(R.id.feed_item_container).getLayoutParams())
						.setMargins(width / 40, 0, width / 40, 0);
				viewHolder.accept.setPadding(width / 20, 0, width / 20, 0);
				viewHolder.decline.setPadding(width / 20, 0, width / 20, 0);

				//viewHolder.imageView.setVisibility(View.GONE);


				v.findViewById(R.id.feed_item).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (((LinearLayout) v.getParent()).getChildAt(1) == null) {
							if (wish != null && wish.getDescription() != null) {
								String description = wish.getDescription();
								TextView descriptionTextView = new TextView(UserPageActivity.this);
								LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
										LinearLayout.LayoutParams.WRAP_CONTENT);
								descriptionTextView.setLayoutParams(params);
								descriptionTextView.setGravity(Gravity.CENTER);
								descriptionTextView.setText(description);
								descriptionTextView.setBackgroundColor(getResources().getColor(R.color.white));
								((LinearLayout) v.getParent()).addView(descriptionTextView);
							}
						} else {
							if (((LinearLayout) v.getParent()).getChildAt(1) != null
									&& ((LinearLayout) v.getParent()).getChildAt(1) instanceof TextView)
								((LinearLayout) v.getParent()).removeViewAt(1);
						}

					}
				});

				int distance = CommonLib.distFrom(prefs.getFloat("lat", 0), prefs.getFloat("lon", 0),
						wish.getLatitude(), wish.getLongitude());

				//distance in km
				distance = (distance / 1000);
				//if(distance < 5)
				viewHolder.distance.setText(distance + "KM");
				//else
				//viewHolder.distance.setVisibility(View.GONE);

				if (user != null && wish != null) {
					String description = getResources().getString(R.string.feed_user_requested,
							user.getUserName() + " ", wish.getTitle().toUpperCase() + " ", wish.getRequiredFor());
					Spannable desc = new SpannableString(description);
					Pattern p = Pattern.compile(user.getUserName(), Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(description);
					while (m.find()) {
						desc.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					}
					p = Pattern.compile(wish.getTitle(), Pattern.CASE_INSENSITIVE);
					m = p.matcher(description);
					Typeface font=CommonLib.getTypeface(getContext(), CommonLib.Bold);
					while (m.find()) {

						desc.setSpan (new CustomTypefaceSpan(font), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
						desc.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.zomato_red)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

					}
					viewHolder.userName.setText(desc);
					setImageFromUrlOrDisk(user.getImageUrl(), viewHolder.imageView, "profile", position, width, false);


					viewHolder.bar
							.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));


					viewHolder.accept.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							z_ProgressDialog = ProgressDialog.show(UserPageActivity.this, null,
									getResources().getString(R.string.sending_request), true, false);
							z_ProgressDialog.setCancelable(false);
							UploadManager.updateRequestStatus(prefs.getString("access_token", ""),
									"" + wish.getWishId(), "1", new Object[] { user, wish });
						}
					});

					viewHolder.decline.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							UploadManager.updateRequestStatus(prefs.getString("access_token", ""),
									"" + wish.getWishId(), "2", new Object[] { user, wish });
						}
					});
					viewHolder.accept.setVisibility(View.VISIBLE);
					viewHolder.decline.setVisibility(View.VISIBLE);
				}
				// till
			} else {

				if (v == null || v.findViewById(R.id.wishbox_list_item_root) == null) {
					v = LayoutInflater.from(mContext).inflate(R.layout.wishbox_list_item, null);
				}

				ViewHolder viewHolder = (ViewHolder) v.getTag();
				if (viewHolder == null) {
					viewHolder = new ViewHolder();
					viewHolder.title = (TextView) v.findViewById(R.id.wish_title);
					viewHolder.date = (TextView) v.findViewById(R.id.wish_date);
					viewHolder.crossIcon = (IconView) v.findViewById(R.id.cross_icon);
					v.setTag(viewHolder);
				}

				((RelativeLayout.LayoutParams) v.findViewById(R.id.wishbox_list_item).getLayoutParams())
						.setMargins(width / 40, width / 40, width / 40, width / 40);

				viewHolder.date.setPadding(width / 20, width / 20, width / 20, width / 40);
				viewHolder.title.setPadding(width / 20, width / 40, width / 20, width / 40);
				((RelativeLayout.LayoutParams) viewHolder.crossIcon.getLayoutParams()).setMargins(0, 0, width / 20, 0);
				// set the date in hh:mm format
				viewHolder.date.setText(CommonLib.findDateDifference(wish.getTimeOfPost()));
				// set the span of title
				String title = mContext.getResources().getString(R.string.wish_title_hint) + wish.getTitle();
				SpannableStringBuilder finalSpanBuilderStr = new SpannableStringBuilder(title);

				ClickableSpan cs1 = new ClickableSpan() {
					@Override
					public void onClick(View widget) {
					}

					@Override
					public void updateDrawState(TextPaint ds) {
						super.updateDrawState(ds);
						ds.setUnderlineText(false);
						ds.setTypeface(
								CommonLib.getTypeface(UserPageActivity.this.getApplicationContext(), CommonLib.Bold));
						ds.setColor(getResources().getColor(R.color.bt_orange));
					}
				};
				finalSpanBuilderStr.setSpan(cs1, title.indexOf(wish.getTitle()),
						title.indexOf(wish.getTitle()) + wish.getTitle().length(),
						SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

				viewHolder.title.setText(finalSpanBuilderStr);

				viewHolder.crossIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(mContext)
								.setMessage(getResources().getString(R.string.wish_delete_text))
								.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								UploadManager.deleteRequest(prefs.getString("access_token", ""), wish.getWishId() + "");
								dialog.dismiss();
							}
						}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).show();
					}
				});
			}

			return v;
		}

	}

	// set all the wishes here
	private void setWishes(ArrayList<Wish> wishes) {
		this.wishes = wishes;
		if (wishes != null && wishes.size() > 0 && mWishesTotalCount > wishes.size()
				&& mListView.getFooterViewsCount() == 0) {
			mListViewFooter = new LinearLayout(getApplicationContext());
			mListViewFooter.setBackgroundResource(R.color.white);
			mListViewFooter.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, width / 5));
			mListViewFooter.setGravity(Gravity.CENTER);
			mListViewFooter.setOrientation(LinearLayout.HORIZONTAL);
			ProgressBar pbar = new ProgressBar(getApplicationContext(), null,
					android.R.attr.progressBarStyleSmallInverse);
			mListViewFooter.addView(pbar);
			pbar.setTag("progress");
			pbar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mListView.addFooterView(mListViewFooter);
		}
		mAdapter = new WishesAdapter(this, R.layout.new_request_fragment, this.wishes);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount - 1 < mWishesTotalCount
						&& !loading && mListViewFooter != null) {
					if (mListView.getFooterViewsCount() == 1) {
						loading = true;
						new LoadModeWishes().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, totalItemCount - 1);
					}
				} else if (totalItemCount - 1 == mWishesTotalCount && mListView.getFooterViewsCount() > 0) {
					mListView.removeFooterView(mListViewFooter);
				}
			}
		});
	}

	private class LoadModeWishes extends AsyncTask<Integer, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Integer... params) {
			int start = params[0];
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "wish/get?type=" + WISH_OWN + "&start=" + start + "&count=" + count
						+ "&another_user=" + userId;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.WISH_LIST, RequestWrapper.FAV);
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
			if (result != null && result instanceof Object[]) {
				Object[] arr = (Object[]) result;
				wishes.addAll((ArrayList<Wish>) arr[1]);
				mAdapter.notifyDataSetChanged();
			}
			loading = false;
		}
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (destroyed)
			return;
		if (requestType == CommonLib.WISH_UPDATE_STATUS) {
			if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
				z_ProgressDialog.dismiss();

			if (destroyed || !status)
				return;

			if (objectId != 1) // accept
				return;

			final User user = (User) ((Object[]) data)[0];
			final Wish wish = (Wish) ((Object[]) data)[1];

			final AlertDialog messageDialog;
			messageDialog = new AlertDialog.Builder(this)
					.setMessage(
							getResources().getString(R.string.thanks_wish_tick, user.getUserName(), wish.getTitle()))
					.setPositiveButton(getResources().getString(R.string.message),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// navigate to message with this user, how?
									Intent intent = new Intent(UserPageActivity.this, MessagesActivity.class);
									intent.putExtra("user", user);
									intent.putExtra("wish", wish);
									intent.putExtra("type", CommonLib.WISH_ACCEPTED_CURRENT_USER);
									startActivity(intent);
								}
							})
					.setNegativeButton(getResources().getString(R.string.later), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create();
			messageDialog.show();
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

}
