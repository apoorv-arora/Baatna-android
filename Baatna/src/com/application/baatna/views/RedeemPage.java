package com.application.baatna.views;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.data.Coupon;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.TypefaceSpan;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RedeemPage extends Activity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private boolean destroyed = false;
	private AsyncTask mAsyncRunning;
	private Activity mContext;
	private CouponsAdapter mAdapter;
	LayoutInflater inflater;
	private int width;
	private ListView mListView;
	ArrayList<Coupon> coupons;
	private BaatnaApp zapp;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.redeem_page);
		width = getWindowManager().getDefaultDisplay().getWidth();
		zapp = (BaatnaApp) getApplication();
		prefs = getSharedPreferences("application_settings", 0);
		mListView = (ListView) findViewById(R.id.wish_list);
		mListView.setDivider(null);
		mListView.setDividerHeight(0);
		mContext = this;
		inflater = LayoutInflater.from(this);
		setupActionBar();
		setListeners();
		refreshView();
		UploadManager.addCallback(this);
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.redeem));
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

		((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
				.setMargins(width / 40, 0, 0, 0);
		actionBarCustomView.findViewById(R.id.title).setPadding(width / 20, 0, width / 40, 0);
		title.setText(s);
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
			Intent intent = new Intent(RedeemPage.this, Splash.class);
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
		mAsyncRunning = new GetCoupons().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetCoupons extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.wishbox_progress_container).setVisibility(View.VISIBLE);

			findViewById(R.id.content).setAlpha(1f);

			findViewById(R.id.content).setVisibility(View.GONE);

			findViewById(R.id.empty_view).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "redeem/get?";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_REDEEM_COUPONS, RequestWrapper.FAV);
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

			findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);

			if (result != null) {
				findViewById(R.id.content).setVisibility(View.VISIBLE);
				if (result instanceof Object[]) {
					Object[] arr = (Object[]) result;
					setWishes((ArrayList<Coupon>) arr[0]);
				}
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
							.show();

					findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					findViewById(R.id.content).setVisibility(View.GONE);
				}
			}

		}
	}

	public class CouponsAdapter extends ArrayAdapter<Coupon> {

		private List<Coupon> wishes;
		private Activity mContext;
		private int width;

		public CouponsAdapter(Activity context, int resourceId, List<Coupon> wishes) {
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
			ImageView logo;
			TextView validity;
			TextView offer;
			TextView terms;
			RelativeLayout request_container;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final Coupon coupon = wishes.get(position);
			if (v == null || v.findViewById(R.id.wishbox_list_item_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.redeem_list_snippet, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.logo = (ImageView) v.findViewById(R.id.logo);
				viewHolder.validity = (TextView) v.findViewById(R.id.validity);
				viewHolder.offer = (TextView) v.findViewById(R.id.offer);
				viewHolder.terms = (TextView) v.findViewById(R.id.terms);
				viewHolder.request_container = (RelativeLayout) v.findViewById(R.id.request_container);

				v.setTag(viewHolder);
			}

			viewHolder.validity.setText(coupon.getValidity());
			viewHolder.offer.setText(coupon.getName());
			viewHolder.terms.setText(coupon.getTerms());

			setImageFromUrlOrDisk(coupon.getImage(), viewHolder.logo, "user", width / 10, width / 10, false);

			viewHolder.request_container.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					UploadManager.getCoupon(coupon.getId() + "");
				}
			});
			return v;
		}

	}

	// set all the wishes here
	private void setWishes(ArrayList<Coupon> wishes) {
		this.coupons = wishes;
		mAdapter = new CouponsAdapter(mContext, R.layout.new_request_fragment, this.coupons);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.COUPON_UPDATE) {
			if (!destroyed && status) {
				Toast.makeText(mContext, "Success", Toast.LENGTH_LONG).show();
				;
				refreshView();
			}
		}
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

}
