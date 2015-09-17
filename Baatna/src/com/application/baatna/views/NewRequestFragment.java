package com.application.baatna.views;

import java.util.ArrayList;
import java.util.List;

import com.application.baatna.R;
import com.application.baatna.data.Categories;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.IconView;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class NewRequestFragment extends Fragment {

	private View rootView;
	private LayoutInflater mInflater;
	private int width;
	private Activity mContext;
	private ListView mCategoriesListView;
	private RequestCategoryAdapter mCategoriesAdapter;
	private GetCategoriesList mAsyncTaskRunning;
	private SharedPreferences prefs;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		rootView = mInflater.inflate(R.layout.new_request_fragment, null);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = getActivity();
		width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		prefs = mContext.getSharedPreferences(CommonLib.APP_SETTINGS, 0);

		mCategoriesListView = (ListView) rootView.findViewById(R.id.category_list_view);
		// refreshView();
		setCategories(CommonLib.getCategoriesList());
		fixSizes();
		setListeners();

	}

	private void fixSizes() {

		rootView.findViewById(R.id.category_et).setPadding(width / 20, width / 20, width / 20, width / 40);

		((LinearLayout.LayoutParams) rootView.findViewById(R.id.category_separator).getLayoutParams())
				.setMargins(width / 20, 0, width / 20, 0);

		rootView.findViewById(R.id.description_et).setPadding(width / 20, width / 10, width / 20, width / 40);

		((LinearLayout.LayoutParams) rootView.findViewById(R.id.post).getLayoutParams()).setMargins(width / 20,
				width / 20, 0, width / 20);

		rootView.findViewById(R.id.post).setPadding(width / 20, width / 40, width / 20, width / 40);

		rootView.findViewById(R.id.category_selection_label).setPadding(width / 20, width / 40, width / 20, width / 20);

		mCategoriesListView.setDivider(null);

		rootView.findViewById(R.id.new_request_progress_container).setVisibility(View.GONE);
	}

	private void setListeners() {
		rootView.findViewById(R.id.description_et).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (rootView != null) {
					View scrollView = rootView.findViewById(R.id.new_request_scroll_container);
					((ScrollView) scrollView).requestDisallowInterceptTouchEvent(true);
				}
				v.onTouchEvent(event);
				return false;
			}
		});

		rootView.findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

		rootView.findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				
				UploadManager.postNewRequest(prefs.getString("access_token", ""),
						((TextView) rootView.findViewById(R.id.category_et)).getText().toString(),
						((TextView) rootView.findViewById(R.id.description_et)).getText().toString());
				try {
					InputMethodManager imm = (InputMethodManager) mContext
							.getSystemService(Service.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}

	private void refreshView() {
		if (mAsyncTaskRunning != null)
			mAsyncTaskRunning.cancel(true);
		(mAsyncTaskRunning = new GetCategoriesList()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class GetCategoriesList extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			rootView.findViewById(R.id.new_request_progress_container).setVisibility(View.VISIBLE);

			rootView.findViewById(R.id.new_request_scroll_container).setAlpha(1f);

			rootView.findViewById(R.id.new_request_scroll_container).setVisibility(View.GONE);

			rootView.findViewById(R.id.empty_view).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "wish/categories?";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.CATEGORIES_LIST, RequestWrapper.FAV);
				CommonLib.ZLog("url", url);
				return info;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (!isAdded())
				return;

			rootView.findViewById(R.id.new_request_progress_container).setVisibility(View.GONE);

			if (result != null) {
				rootView.findViewById(R.id.new_request_scroll_container).setVisibility(View.VISIBLE);
				if (result instanceof ArrayList<?>)
					setCategories((ArrayList<Categories>) result);
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					rootView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					rootView.findViewById(R.id.new_request_scroll_container).setVisibility(View.GONE);
				}
			}

		}
	}

	private void setCategories(ArrayList<Categories> categories) {
		mCategoriesAdapter = new RequestCategoryAdapter(mContext, R.layout.new_request_fragment, categories);
		mCategoriesListView.setAdapter(mCategoriesAdapter);
		setListViewHeightBasedOnChildren(mCategoriesListView);
	}

	private void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public class RequestCategoryAdapter extends ArrayAdapter<Categories> {

		private List<Categories> categories;
		private Activity mContext;
		private int width;

		public RequestCategoryAdapter(Activity context, int resourceId, List<Categories> categories) {
			super(context.getApplicationContext(), resourceId, categories);
			mContext = context;
			this.categories = categories;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (categories == null) {
				return 0;
			} else {
				return categories.size();
			}
		}

		protected class ViewHolder {
			TextView category_label;
			IconView category_image;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final Categories categoryName = categories.get(position);
			if (v == null || v.findViewById(R.id.request_category_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.request_category_adapter, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.category_label = (TextView) v.findViewById(R.id.category_title);
				viewHolder.category_image = (IconView) v.findViewById(R.id.category_image);
				v.setTag(viewHolder);
			}

			if(position % 2 == 0) {
				v.findViewById(R.id.request_category_root).setBackgroundResource(R.color.zhl_light);
			} else {
				v.findViewById(R.id.request_category_root).setBackgroundResource(R.color.white);
			}
			
			v.findViewById(R.id.proceed_icon).setPadding(width / 20, 0, width / 20, 0);
			
			viewHolder.category_label.setText(categoryName.getCategory());
			viewHolder.category_image.setText(categoryName.getCategoryIcon());
			viewHolder.category_label.setPadding(width / 40, width / 40, width / 20, width / 40);
			viewHolder.category_image.setPadding(width / 20, width / 40, width / 40, width / 40);
			viewHolder.category_label.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView view = (TextView) v;
					if (view != null && view.getText() != null) {
						setSelectedCategory(view.getText().toString());
						Intent intent = new Intent(mContext, CategoryItemSelectionFragment.class);
						intent.putExtra("category_id", categoryName.getCategoryId());
						mContext.startActivityForResult(intent, CategoryItemSelectionFragment.requestCode);
					}
				}
			});

			return v;
		}

	}

	private void setSelectedCategory(String category) {
		if (rootView != null) {
			((TextView) rootView.findViewById(R.id.category_et)).setText(category);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CategoryItemSelectionFragment.requestCode) {
			if (resultCode == CategoryItemSelectionFragment.RESULT_ITEM_SELECTED && data != null) {
				if (data.hasExtra("title")) {
					((TextView) rootView.findViewById(R.id.category_et))
							.setText(String.valueOf(data.getStringExtra("title")));
				}
			}
		}
	}

	public String getSelectedCategory() {
		return ((TextView) rootView.findViewById(R.id.category_et)).getText().toString();
	}

}
