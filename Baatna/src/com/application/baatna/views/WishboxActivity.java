package com.application.baatna.views;

import java.util.ArrayList;
import java.util.List;

import com.application.baatna.R;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.IconView;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WishboxActivity extends Activity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private boolean destroyed = false;
	private ListView mListView;
	private AsyncTask mAsyncRunning;
	private Activity mContext;
	private WishesAdapter mAdapter;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.wishbox_activity);

		prefs = getSharedPreferences("application_settings", 0);
		mListView = (ListView) findViewById(R.id.wish_list);
		mListView.setDivider(null);
		mListView.setDividerHeight(0);
		mContext = this;
		setupActionBar();
		setListeners();
		refreshView();
		UploadManager.addCallback(this);
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		actionBar.setTitle(getResources().getString(R.string.your_wishbox));

		try {
			int width = getWindowManager().getDefaultDisplay().getWidth();
			findViewById(android.R.id.home).setPadding(width / 40, 0,
					width / 40, 0);
			ViewGroup home = (ViewGroup) findViewById(android.R.id.home)
					.getParent();
			home.getChildAt(0).setPadding(width / 40, 0, width / 80, 0);
		} catch (Exception e) {
		}
	}

	private void setListeners() {
		findViewById(R.id.empty_view_retry_container).setOnClickListener(
				new View.OnClickListener() {
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
			Intent intent = new Intent(WishboxActivity.this,
					BaatnaActivity.class);
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
		mAsyncRunning = new GetWishes()
				.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetWishes extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.wishbox_progress_container).setVisibility(
					View.VISIBLE);

			findViewById(R.id.wish_list).setAlpha(1f);

			findViewById(R.id.wish_list).setVisibility(View.GONE);

			findViewById(R.id.empty_view).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "wish/view?";
				Object info = RequestWrapper.RequestHttp(url,
						RequestWrapper.WISH_LIST, RequestWrapper.FAV);
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

			findViewById(R.id.wishbox_progress_container).setVisibility(
					View.GONE);

			if (result != null) {
				findViewById(R.id.wish_list).setVisibility(View.VISIBLE);
				if (result instanceof ArrayList<?>)
					setWishes((ArrayList<Wish>) result);
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							mContext,
							getResources().getString(
									R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					findViewById(R.id.wish_list).setVisibility(View.GONE);
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
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final Wish wish = wishes.get(position);
			if (v == null
					|| v.findViewById(R.id.wishbox_list_item_root) == null) {
				v = LayoutInflater.from(mContext).inflate(
						R.layout.wishbox_list_item, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.title = (TextView) v.findViewById(R.id.wish_title);
				viewHolder.date = (TextView) v.findViewById(R.id.wish_date);
				viewHolder.crossIcon = (IconView) v
						.findViewById(R.id.cross_icon);
				v.setTag(viewHolder);
			}

			((RelativeLayout.LayoutParams) v.findViewById(
					R.id.wishbox_list_item).getLayoutParams()).setMargins(
					width / 20, width / 40, width / 20, width / 40);

			viewHolder.date.setPadding(width / 20, width / 20, width / 20,
					width / 40);
			viewHolder.title.setPadding(width / 20, width / 20, width / 20,
					width / 40);
			viewHolder.crossIcon.setPadding(width / 20, width / 20, width / 20,
					width / 40);
			// set the date in hh:mm format
			viewHolder.date.setText(CommonLib.getDateFromUTC(wish
					.getTimeOfPost()));
			// set the span of title
			String title = mContext.getResources().getString(
					R.string.wish_title_hint)
					+ wish.getTitle();
			SpannableStringBuilder finalSpanBuilderStr = new SpannableStringBuilder(
					title);

			ClickableSpan cs1 = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					ds.setUnderlineText(false);
					ds.setTypeface(CommonLib.getTypeface(
							getApplicationContext(), CommonLib.Bold));
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
							.setMessage(
									getResources().getString(
											R.string.wish_delete_text))
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											UploadManager.deleteRequest(prefs
													.getString("access_token",
															""),
													wish.getWishId() + "");
											dialog.dismiss();
										}
									})
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).show();
				}
			});
			return v;
		}

	}

	// set all the wishes here
	private void setWishes(ArrayList<Wish> categories) {
		mAdapter = new WishesAdapter(mContext, R.layout.new_request_fragment,
				categories);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId,
			Object data, int uploadId, boolean status, String stringId) {
		if (requestType == CommonLib.WISH_REMOVE) {
			if (!destroyed)
				refreshView();
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId,
			Object object) {
	}

}