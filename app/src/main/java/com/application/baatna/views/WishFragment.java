package com.application.baatna.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.IconView;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import java.util.ArrayList;
import java.util.List;

public class WishFragment extends Fragment implements UploadManagerCallback {

	private BaatnaApp zapp;
	private Activity activity;
	private View getView;
	private SharedPreferences prefs;
	private int width, height;
	private LayoutInflater vi;
	private boolean destroyed = false;

	private AsyncTask mAsyncRunning;
	private WishesAdapter mAdapter;
	private ListView mListView;
	ArrayList<Wish> wishes;
	LinearLayout mListViewFooter;
	private int mWishesTotalCount;
	private boolean cancelled = false;
	private boolean loading = false;
	private int count = 10;
	
	public static final int WISH_OWN = 2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_wish_own, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();
		getView = getView();

		prefs = activity.getSharedPreferences("application_settings", 0);
		zapp = (BaatnaApp) activity.getApplication();
		width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
		vi = LayoutInflater.from(activity.getApplicationContext());

		mListView = (ListView) getView.findViewById(R.id.wish_list);
		mListView.setDivider(null);
		mListView.setDividerHeight(0);
		
		setListeners();
		refreshView();
		UploadManager.addCallback(this);

	}

	private void setListeners() {
		getView.findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

	}

	@Override
	public void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	private void refreshView() {
		if (mAsyncRunning != null)
			mAsyncRunning.cancel(true);
		mAsyncRunning = new GetWishes().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetWishes extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.VISIBLE);

			getView.findViewById(R.id.content).setAlpha(1f);

			getView.findViewById(R.id.content).setVisibility(View.GONE);

			getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "wish/get?type="+WISH_OWN+"&start=0&count=" + count;
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

			getView.findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);

			if (result != null) {
				getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
				if (result instanceof Object[]) {
					Object[] arr = (Object[]) result;
					mWishesTotalCount = (Integer) arr[0];
					setWishes((ArrayList<Wish>) arr[1]);
					if( ((ArrayList<Wish>) arr[1]).size()  == 0 ) {
						getView.findViewById(R.id.content).setVisibility(View.GONE);
						getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
						((TextView)getView.findViewById(R.id.empty_view_text)).setText("Nothing here yet");

					} else {
						getView.findViewById(R.id.content).setVisibility(View.VISIBLE);
						getView.findViewById(R.id.empty_view).setVisibility(View.GONE);
					}
				}
			} else {
				if (CommonLib.isNetworkAvailable(activity)) {
					Toast.makeText(activity, activity.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(activity, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
							.show();

					getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					getView.findViewById(R.id.content).setVisibility(View.GONE);
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
					ds.setTypeface(CommonLib.getTypeface(activity.getApplicationContext(), CommonLib.Bold));
					ds.setColor(getResources().getColor(R.color.bt_drawer_green));
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
			mListViewFooter = new LinearLayout(activity.getApplicationContext());
			mListViewFooter.setBackgroundResource(R.color.white);
			mListViewFooter.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, width / 5));
			mListViewFooter.setGravity(Gravity.CENTER);
			mListViewFooter.setOrientation(LinearLayout.HORIZONTAL);
			ProgressBar pbar = new ProgressBar(activity.getApplicationContext(), null,
					android.R.attr.progressBarStyleSmallInverse);
			mListViewFooter.addView(pbar);
			pbar.setTag("progress");
			pbar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mListView.addFooterView(mListViewFooter);
		}
		mAdapter = new WishesAdapter(activity, R.layout.new_request_fragment, this.wishes);
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
				url = CommonLib.SERVER + "wish/get?type="+WISH_OWN+"&start="+start+"&count=" + count;
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

	public void scrollToTop() {
		try {
			// if (getView.findViewById(R.id.home_fragment_scroll_root) != null)
			// {
			// ((ScrollView)
			// getView.findViewById(R.id.home_fragment_scroll_root)).smoothScrollTo(0,
			// 0);
			// }
		} catch (Exception e) {
		}
	}

}
