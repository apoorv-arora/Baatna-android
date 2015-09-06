package com.application.baatna.views;

import java.util.ArrayList;
import java.util.List;

import com.application.baatna.R;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.IconView;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.TypefaceSpan;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WishboxActivity extends Activity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private boolean destroyed = false;
	private AsyncTask mAsyncRunning;
	private Activity mContext;
	private WishesAdapter mAdapter;
	LayoutInflater inflater;
	private int width;

	// Load more part
	private ListView mListView;
	ArrayList<Wish> wishes;
	LinearLayout mListViewFooter;
	private int mWishesTotalCount;
	private boolean cancelled = false;
	private boolean loading = false;
	private int count = 10;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.wishbox_activity);
		width = getWindowManager().getDefaultDisplay().getWidth();

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

		SpannableString s = new SpannableString(getString(R.string.your_wishbox));
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
			Intent intent = new Intent(WishboxActivity.this, BaatnaActivity.class);
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
		mAsyncRunning = new GetWishes().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetWishes extends AsyncTask<Object, Void, Object> {

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
				url = CommonLib.SERVER + "wish/view?start=0&count=" + count;
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

			findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);

			if (result != null) {
				findViewById(R.id.content).setVisibility(View.VISIBLE);
				if (result instanceof Object[]) {
					Object[] arr = (Object[]) result;
					mWishesTotalCount = (Integer) arr[0];
					setWishes((ArrayList<Wish>) arr[1]);
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
			viewHolder.date.setText(CommonLib.getDateFromUTC(wish.getTimeOfPost()));
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
					ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
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
					new AlertDialog.Builder(mContext).setMessage(getResources().getString(R.string.wish_delete_text))
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
		mAdapter = new WishesAdapter(mContext, R.layout.new_request_fragment, this.wishes);
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

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.WISH_REMOVE) {
			if (!destroyed)
				refreshView();
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

	private class LoadModeWishes extends AsyncTask<Integer, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Integer... params) {
			int start = params[0];
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "wish/view?start=" + start + "&count=" + count;
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
}
