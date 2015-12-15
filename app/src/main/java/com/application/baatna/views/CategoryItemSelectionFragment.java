package com.application.baatna.views;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.application.baatna.R;
import com.application.baatna.data.CategoryItems;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.IconView;

import java.util.List;

public class CategoryItemSelectionFragment extends AppCompatActivity {

	private SharedPreferences prefs;
	private boolean isDestroyed = false;
	private GridView mGridView;
	private CategoryItemsAdapter mAdapter;

	public static final int requestCode = 100;
	public static final int RESULT_ITEM_SELECTED = 101;
	public static final int RESULT_ITEM_UNSELECTED = 102;
	
	LayoutInflater inflater;
	private int width;
	private String mCategoryName;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.category_item_selection);
		width = getWindowManager().getDefaultDisplay().getWidth();
		inflater = LayoutInflater.from(this);
		mGridView = (GridView) findViewById(R.id.gridView);
		if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("category_name")) {
			mCategoryName = String.valueOf(getIntent().getExtras().get("category_name"));
		}
		if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("category_id")) {
			mAdapter = new CategoryItemsAdapter(this, R.layout.category_item_holder,
					CommonLib.getCategoryItems(getIntent().getIntExtra("category_id", 0)));
			mGridView.setAdapter(mAdapter);
		}
		prefs = getSharedPreferences("application_settings", 0);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);
		setupActionBar();
	}

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		View v = inflater.inflate(R.layout.green_action_bar, null);

		((TextView)v.findViewById(R.id.back_icon)).setText("D");
		((TextView)v.findViewById(R.id.title)).setText(mCategoryName);
		
		v.findViewById(R.id.back_icon).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					InputMethodManager imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				onBackPressed();
			}
		});
		actionBar.setCustomView(v);

		v.findViewById(R.id.back_icon).setPadding(width / 20, 0, width / 20, 0);

		// user handle
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setPadding(width / 80, 0, width / 40, 0);
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

			viewHolder.category_bg.getLayoutParams().height = width / 3;
			
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
