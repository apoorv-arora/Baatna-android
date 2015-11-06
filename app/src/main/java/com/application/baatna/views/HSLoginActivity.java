package com.application.baatna.views;

import java.util.ArrayList;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.data.Institution;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.TypefaceSpan;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class HSLoginActivity extends Activity implements UploadManagerCallback {

	int width;
	int height;
	private SharedPreferences prefs;
	private boolean destroyed = false;
	private GetCategoriesList mAsyncTaskRunning;
	private LayoutInflater inflater;
	private BaatnaApp zapp;
	private Activity mActivity;
	private ListView mSubzoneSearchListView, mBranchListView;
	SimpleListAdapter adapter1;
	BranchListAdapter branchAdapter;
	private ProgressDialog z_ProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hs_login_activity);
		mActivity = this;
		prefs = getSharedPreferences("application_settings", 0);
		inflater = LayoutInflater.from(getApplicationContext());
		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();
		zapp = (BaatnaApp) getApplication();
		mSubzoneSearchListView = (ListView) findViewById(R.id.subzone_search_list_view);
		mBranchListView = (ListView) findViewById(R.id.branch_list_view);

		refreshView();
		fixSizes();
		setListeners();
		setUpActionBar();
		UploadManager.addCallback(this);
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

	private void setUpActionBar() {
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.login));
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

	@Override
	public void onBackPressed() {
		// disconnect facebook
		try {

			com.facebook.Session fbSession = com.facebook.Session.getActiveSession();
			if (fbSession != null) {
				fbSession.closeAndClearTokenInformation();
			}
			com.facebook.Session.setActiveSession(null);

		} catch (Exception e) {
		}

		String accessToken = prefs.getString("access_token", "");
		UploadManager.logout(accessToken);

		Editor editor = prefs.edit();
		editor.putInt("uid", 0);
		editor.putString("thumbUrl", "");
		editor.putString("access_token", "");
		editor.remove("username");
		editor.putBoolean("facebook_post_permission", false);
		editor.putBoolean("post_to_facebook_flag", false);
		editor.putBoolean("facebook_connect_flag", false);
		editor.putBoolean("twitter_status", false);

		editor.commit();

		if (prefs.getInt("uid", 0) == 0) {
			Intent intent = new Intent(zapp, Splash.class);
			startActivity(intent);
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		} else
			super.onBackPressed();
	}

	public void goBack(View v) {
		onBackPressed();
	}

	public void actionBarSelected(View v) {

		switch (v.getId()) {

		case R.id.home_icon_container:
			onBackPressed();

		default:
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	private void fixSizes() {
		findViewById(R.id.institutionEt).setPadding(width / 20, width / 10, width / 20, width / 40);
		findViewById(R.id.branch_Et).setPadding(width / 20, width / 40, width / 20, width / 40);
		findViewById(R.id.phone_number).setPadding(width / 20, width / 40, width / 20, width / 40);
		findViewById(R.id.year_selector).setPadding(width / 20, width / 40, width / 20, width / 40);
		findViewById(R.id.submit_button).setPadding(0, width / 20, 0, width / 20);

		((LinearLayout.LayoutParams) findViewById(R.id.institutionEt).getLayoutParams()).setMargins(width / 20,
				width / 20, width / 20, width / 40);
		((LinearLayout.LayoutParams) findViewById(R.id.branch_Et).getLayoutParams()).setMargins(width / 20, width / 20,
				width / 20, width / 40);
		((LinearLayout.LayoutParams) findViewById(R.id.year_selector).getLayoutParams()).setMargins(width / 20,
				width / 20, width / 20, width / 40);
		((LinearLayout.LayoutParams) findViewById(R.id.phone_number).getLayoutParams()).setMargins(width / 20,
				width / 20, width / 20, width / 20);

		((LinearLayout.LayoutParams) findViewById(R.id.submit_button).getLayoutParams()).setMargins(width / 20,
				width / 20, width / 20, 0);
	}

	private void setListeners() {
		findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeLoginCall();
			}
		});
		findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

		((TextView) findViewById(R.id.institutionEt)).setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (((TextView) v).length() >= 0)
						findViewById(R.id.subzone_search_list_view_container).setVisibility(View.VISIBLE);
					else
						findViewById(R.id.subzone_search_list_view_container).setVisibility(View.GONE);
				} else {
					findViewById(R.id.subzone_search_list_view_container).setVisibility(View.GONE);
				}
			}
		});

		((TextView) findViewById(R.id.branch_Et)).setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (((TextView) v).length() >= 0)
						findViewById(R.id.branch_list_container).setVisibility(View.VISIBLE);
					else
						findViewById(R.id.branch_list_container).setVisibility(View.GONE);
				} else {
					findViewById(R.id.branch_list_container).setVisibility(View.GONE);
				}
			}
		});

		((TextView) findViewById(R.id.institutionEt)).addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				adapter1.getFilter().filter(arg0);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (arg0.length() >= 0)
					findViewById(R.id.subzone_search_list_view_container).setVisibility(View.VISIBLE);
				else
					findViewById(R.id.subzone_search_list_view_container).setVisibility(View.GONE);
			}
		});

		((TextView) findViewById(R.id.branch_Et)).addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				adapter1.getFilter().filter(arg0);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (arg0.length() >= 0)
					findViewById(R.id.branch_list_container).setVisibility(View.VISIBLE);
				else
					findViewById(R.id.branch_list_container).setVisibility(View.GONE);
			}
		});

		((EditText)findViewById(R.id.phone_number)).setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
					makeLoginCall();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	protected void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	private void refreshView() {
		if (mAsyncTaskRunning != null)
			mAsyncTaskRunning.cancel(true);
		(mAsyncTaskRunning = new GetCategoriesList()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void navigateToHome() {
		if (prefs.getInt("uid", 0) != 0) {
			if (prefs.getBoolean("HSLogin", true)) {
				Intent intent = new Intent(this, HSLoginActivity.class);
				startActivity(intent);
				finish();
			} else {
				Intent intent = new Intent(this, Home.class);
				startActivity(intent);
				finish();
			}
		}
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.UPDATE_INSTITUTION) {
			if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
				z_ProgressDialog.dismiss();
			if (status && !destroyed) {
				Editor editor = prefs.edit();
				editor.putBoolean("HSLogin", false).commit();
				navigateToHome();
			}
		}

	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

	}

	// get institution name list...
	private class GetCategoriesList extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.login_progress_container).setVisibility(View.VISIBLE);

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
				url = CommonLib.SERVER + "user/institutions?";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.INSTITUTIONS_LIST, RequestWrapper.FAV);
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

			findViewById(R.id.login_progress_container).setVisibility(View.GONE);

			if (result != null) {
				findViewById(R.id.content).setVisibility(View.VISIBLE);
				if (result instanceof ArrayList<?>) {
					setInstitutions((ArrayList<Institution>) result);
				}
			} else {
				if (CommonLib.isNetworkAvailable(HSLoginActivity.this)) {
					Toast.makeText(HSLoginActivity.this, getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(HSLoginActivity.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					findViewById(R.id.content).setVisibility(View.GONE);
				}
			}

		}
	}

	private void setInstitutions(ArrayList<Institution> categories) {
		adapter1 = new SimpleListAdapter(mActivity, R.layout.simple_list_item, categories);
		mSubzoneSearchListView.setAdapter(adapter1);
		setListViewHeightBasedOnChildren(mSubzoneSearchListView);

	}

	private class SimpleListAdapter extends ArrayAdapter<Institution> {

		private ArrayList<Institution> wishes;
		private Activity mContext;
		private int width;

		public SimpleListAdapter(Activity context, int resourceId, ArrayList<Institution> wishes) {
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
			TextView text;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final Institution wish = wishes.get(position);
			if (v == null || v.findViewById(R.id.list_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.simple_list_item, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.text = (TextView) v.findViewById(R.id.text1);
				v.setTag(viewHolder);
			}

			viewHolder.text.setPadding(width / 20, width / 20, width / 20, width / 20);

			viewHolder.text.setText(wish.getInstitutionName());
			viewHolder.text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((TextView) findViewById(R.id.institutionEt)).setText(((TextView) v).getText().toString());
					findViewById(R.id.subzone_search_list_view_container).setVisibility(View.GONE);

					branchAdapter = new BranchListAdapter(mActivity, R.layout.simple_list_item, wish.getBranches());
					mBranchListView.setAdapter(branchAdapter);
					setListViewHeightBasedOnChildren(mBranchListView);

					((TextView)findViewById(R.id.branch_Et)).requestFocus();
				}
			});
			return v;
		}

	}

	private class BranchListAdapter extends ArrayAdapter<String> {

		private ArrayList<String> wishes;
		private Activity mContext;
		private int width;

		public BranchListAdapter(Activity context, int resourceId, ArrayList<String> wishes) {
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
			TextView text;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final String wish = wishes.get(position);
			if (v == null || v.findViewById(R.id.list_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.simple_list_item, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.text = (TextView) v.findViewById(R.id.text1);
				v.setTag(viewHolder);
			}

			viewHolder.text.setPadding(width / 20, width / 20, width / 20, width / 20);

			viewHolder.text.setText(wish);
			viewHolder.text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((TextView) findViewById(R.id.branch_Et)).setText(((TextView) v).getText().toString());
					findViewById(R.id.branch_list_container).setVisibility(View.GONE);

					((TextView)findViewById(R.id.year_selector)).requestFocus();
				}
			});
			return v;
		}

	}

	private void makeLoginCall() {
		String institutionName = ((TextView) findViewById(R.id.institutionEt)).getText().toString();
		String branchName = ((TextView) findViewById(R.id.branch_Et)).getText().toString();
		String phoneNumber = ((TextView) findViewById(R.id.phone_number)).getText().toString();
		
		if(institutionName == null || institutionName.length() < 1) {
			Toast.makeText(mActivity, "Invalid institution name", Toast.LENGTH_SHORT).show();
			((TextView) findViewById(R.id.institutionEt)).requestFocus();
			return;
		}
		
		if(branchName == null || branchName.length() < 1) {
			Toast.makeText(mActivity, "Invalid branch name", Toast.LENGTH_SHORT).show();
			((TextView) findViewById(R.id.branch_Et)).requestFocus();
			return;
		}
		
		int year = -1;
		try {
			year = Integer.parseInt(((TextView) findViewById(R.id.year_selector)).getText().toString());
		} catch( NumberFormatException e) {
			e.printStackTrace();
			Toast.makeText(mActivity, "Invalid year", Toast.LENGTH_SHORT).show();
			((TextView) findViewById(R.id.year_selector)).requestFocus();
			return;
		}
		
		if(year == -1 || ((TextView) findViewById(R.id.year_selector)).getText().toString().length() == 0) {
			Toast.makeText(mActivity, "Invalid year", Toast.LENGTH_SHORT).show();
			((TextView) findViewById(R.id.year_selector)).requestFocus();
			return;
		}
		
		if(phoneNumber == null || phoneNumber.length() < 1) {
			Toast.makeText(mActivity, "Invalid phone number", Toast.LENGTH_SHORT).show();
			((TextView) findViewById(R.id.phone_number)).requestFocus();
			return;
		}
		
		
		z_ProgressDialog = ProgressDialog.show(HSLoginActivity.this, null,
				getResources().getString(R.string.verifying_creds), true, false);
		z_ProgressDialog.setCancelable(false);
		UploadManager.updateInstitution(prefs.getString("access_token", ""), institutionName, "", year, branchName,
				phoneNumber);
	}

}
