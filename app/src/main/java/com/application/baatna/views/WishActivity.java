package com.application.baatna.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.TypefaceSpan;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

public class WishActivity extends AppCompatActivity implements UploadManagerCallback {

	private BaatnaApp zapp;
	private SharedPreferences prefs;
	private int width;
	private boolean destroyed = false;
	private ProgressDialog z_ProgressDialog;
	private Wish mWish;
	private User mUser;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.wish_activity);
		prefs = getSharedPreferences(CommonLib.APP_SETTINGS, 0);
		zapp = (BaatnaApp) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();

		mWish = (Wish) getIntent().getSerializableExtra("wish");
		mUser = (User) getIntent().getSerializableExtra("user");

		UploadManager.addCallback(this);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);
		setupActionBar();
		fixSizes();
		setValues();

	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (destroyed)
			return;
		if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
			z_ProgressDialog.dismiss();
		if (requestType == CommonLib.WISH_UPDATE_STATUS) {
			if(destroyed || !status)
				return;
			
			if(objectId != 1 )//accept
				return;
			
			final User user = (User) ((Object[])data)[0];
			final Wish wish = (Wish) ((Object[])data)[1];
				
			final AlertDialog messageDialog;
			messageDialog = new AlertDialog.Builder(this)
					.setMessage(getResources().getString(R.string.thanks_wish_tick, user.getUserName(), wish.getTitle()))
					.setPositiveButton(getResources().getString(R.string.message), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//navigate to message with this user, how?
							Intent intent = new Intent(WishActivity.this, MessagesActivity.class);
							intent.putExtra("user", user);
							intent.putExtra("wish", wish);
							intent.putExtra("type", CommonLib.WISH_ACCEPTED_CURRENT_USER);
							startActivity(intent);
						}
					}).setNegativeButton(getResources().getString(R.string.later),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									onBackPressed();
								}
							})
					.create();
			messageDialog.show();
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (destroyed)
			return;
		if (requestType == CommonLib.WISH_UPDATE_STATUS) {
		}
	}

	private void fixSizes() {
		((RelativeLayout.LayoutParams) findViewById(R.id.feed_item_container).getLayoutParams()).setMargins(width / 40,
				0, width / 40, 0);
		findViewById(R.id.accept_button).setPadding(width / 20, 0, width / 20, 0);
		findViewById(R.id.decline_button).setPadding(width / 20, 0, width / 20, 0);
	}

	private void setValues() {
		String description = getResources().getString(R.string.feed_user_requested, mUser.getUserName() + " ",
				mWish.getTitle() + " ", mWish.getRequiredFor());

		String details = mWish.getDescription();
		TextView descriptionTextView = new TextView(WishActivity.this);
		LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		descriptionTextView.setLayoutParams(params);
		descriptionTextView.setGravity(Gravity.CENTER);
		descriptionTextView.setText(details);
		descriptionTextView.setBackgroundColor(getResources().getColor(R.color.white));
		((LinearLayout) findViewById(R.id.feed_item).getParent()).addView(descriptionTextView);

		setImageFromUrlOrDisk(mUser.getImageUrl(), (ImageView) findViewById(R.id.user_image), "", width, width, false);

		((TextView) findViewById(R.id.user_name)).setText(description);
		findViewById(R.id.left_bar)
				.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.zomato_red)));

		findViewById(R.id.accept_button).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				z_ProgressDialog = ProgressDialog.show(WishActivity.this, null,
						getResources().getString(R.string.sending_request), true, false);
				z_ProgressDialog.setCancelable(false);
				UploadManager.updateRequestStatus(prefs.getString("access_token", ""), "" + mWish.getWishId(), "1", new Object[] {mUser, mWish});
			}
		});

		findViewById(R.id.decline_button).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				UploadManager.updateRequestStatus(prefs.getString("access_token", ""), "" + mWish.getWishId(), "2", new Object[] {mUser, mWish});
			}
		});
		findViewById(R.id.accept_button).setVisibility(View.VISIBLE);
		findViewById(R.id.decline_button).setVisibility(View.VISIBLE);
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

		SpannableString s = new SpannableString(getString(R.string.help_out, mUser.getUserName()));
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
		Intent intent = new Intent(WishActivity.this, Home.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		super.onDestroy();
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
