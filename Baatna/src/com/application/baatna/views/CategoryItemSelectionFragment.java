package com.application.baatna.views;

import java.util.List;

import com.application.baatna.R;
import com.application.baatna.data.CategoryItems;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.IconView;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class CategoryItemSelectionFragment extends Activity {

	private SharedPreferences prefs;
	private boolean isDestroyed = false;
	private GridView mGridView;
	private CategoryItemsAdapter mAdapter;

	public static final int requestCode = 100;
	public static final int RESULT_ITEM_SELECTED = 101;
	public static final int RESULT_ITEM_UNSELECTED = 102;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.category_item_selection);
		mGridView = (GridView) findViewById(R.id.gridView);
		if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("category_id")) {
			mAdapter = new CategoryItemsAdapter(this, R.layout.category_item_holder,
					CommonLib.getCategoryItems(getIntent().getIntExtra("category_id", 0)));
			mGridView.setAdapter(mAdapter);
		}
		prefs = getSharedPreferences("application_settings", 0);
		setupActionBar();
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setLogo(R.drawable.ic_launcher);

		try {
			int width = getWindowManager().getDefaultDisplay().getWidth();
			findViewById(android.R.id.home).setPadding(width / 80, 0, width / 40, 0);
			ViewGroup home = (ViewGroup) findViewById(android.R.id.home).getParent();
			home.getChildAt(0).setPadding(width / 80, 0, width / 80, 0);
		} catch (Exception e) {
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

	@Override
	protected void onDestroy() {
		isDestroyed = true;
		super.onDestroy();
	}

	public class CategoryItemsAdapter extends ArrayAdapter<CategoryItems> {

		private List<CategoryItems> categories;
		private Activity mContext;
		private int width;

		public CategoryItemsAdapter(Activity context, int resourceId, List<CategoryItems> categories) {
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
			TextView category_title;
			IconView category_bg;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final CategoryItems categoryItem = categories.get(position);
			if (v == null || v.findViewById(R.id.category_item_holder_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.category_item_holder, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.category_title = (TextView) v.findViewById(R.id.category_title);
				viewHolder.category_bg = (IconView) v.findViewById(R.id.category_bg);
				v.setTag(viewHolder);
			}
			viewHolder.category_bg.setText(categoryItem.getResId());
			viewHolder.category_title.setText(categoryItem.getName());

			v.findViewById(R.id.category_item_holder_root).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent resultIntent = new Intent();
					resultIntent.putExtra("title",
							((TextView) v.findViewById(R.id.category_title)).getText().toString());
					setResult(RESULT_ITEM_SELECTED, resultIntent);
					finish();
				}
			});
			return v;
		}

	}

}
