package com.application.baatna.views;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

public class HSLoginActivity extends Activity implements UploadManagerCallback {

	int width;
	int height;
	private SharedPreferences prefs;
	private boolean destroyed = false;
	private GetCategoriesList mAsyncTaskRunning;
	private LayoutInflater inflater;
	private BaatnaApp zapp;
	private Activity mActivity;
	private ListView mSubzoneSearchListView;
	SimpleListAdapter adapter1;

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
		refreshView();
		fixSizes();
		setListeners();
		setUpActionBar();
		UploadManager.addCallback(this);

	}

	private void setUpActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		try {
			int width = getWindowManager().getDefaultDisplay().getWidth();
			findViewById(android.R.id.home).setPadding(width / 80, 0,
					width / 40, 0);
			ViewGroup home = (ViewGroup) findViewById(android.R.id.home)
					.getParent();
			home.getChildAt(0).setPadding(width / 80, 0, width / 80, 0);
		} catch (Exception e) {
		}
		actionBar.setTitle(getResources().getString(R.string.login));
	}

	@Override
	public void onBackPressed() {
		// disconnect facebook
		try {

			com.facebook.Session fbSession = com.facebook.Session
					.getActiveSession();
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
			Intent intent = new Intent(zapp, BaatnaActivity.class);
			startActivity(intent);
			finish();
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
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
		findViewById(R.id.institutionEt).setPadding(width / 20, width / 10,
				width / 20, width / 40);
		findViewById(R.id.studentIdEt).setPadding(width / 20, width / 40,
				width / 20, width / 20);
		findViewById(R.id.submit_button).setPadding(0, width / 20, 0,
				width / 20);

		((LinearLayout.LayoutParams) findViewById(R.id.institutionEt)
				.getLayoutParams()).setMargins(width / 20, width / 20,
				width / 20, width / 40);
		((LinearLayout.LayoutParams) findViewById(R.id.studentIdEt)
				.getLayoutParams()).setMargins(width / 20, width / 40,
				width / 20, width / 20);
		((LinearLayout.LayoutParams) findViewById(R.id.submit_button)
				.getLayoutParams()).setMargins(width / 20, width / 40,
				width / 20, 0);
	}

	private void setListeners() {
		findViewById(R.id.submit_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String institutionName = ((TextView) findViewById(R.id.institutionEt))
								.getText().toString();
						String studentId = ((TextView) findViewById(R.id.studentIdEt))
								.getText().toString();
						UploadManager.updateInstitution(
								prefs.getString("access_token", ""),
								institutionName, studentId);
					}
				});
		findViewById(R.id.empty_view_retry_container).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						refreshView();
					}
				});

		((TextView) findViewById(R.id.institutionEt))
				.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						adapter1.getFilter().filter(arg0);
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void afterTextChanged(Editable arg0) {
						// TODO Auto-generated method stub
						if(arg0.length() > 0)
							findViewById(R.id.subzone_search_list_view_container).setVisibility(View.VISIBLE);
						else
							findViewById(R.id.subzone_search_list_view_container).setVisibility(View.GONE);
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
		(mAsyncTaskRunning = new GetCategoriesList())
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
	public void uploadFinished(int requestType, int userId, int objectId,
			Object data, int uploadId, boolean status, String stringId) {
		if (requestType == CommonLib.UPDATE_INSTITUTION) {
			if (status && !destroyed) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("HSLogin", false).commit();
				navigateToHome();
			}
		}

	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId,
			Object object) {

	}

	// get institution name list...
	private class GetCategoriesList extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.login_progress_container).setVisibility(
					View.VISIBLE);

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
				Object info = RequestWrapper.RequestHttp(url,
						RequestWrapper.INSTITUTIONS_LIST, RequestWrapper.FAV);
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

			findViewById(R.id.login_progress_container).setVisibility(
					View.GONE);

			if (result != null) {
				findViewById(R.id.content).setVisibility(
						View.VISIBLE);
				if (result instanceof ArrayList<?>) {
					setInstitutions((ArrayList<String>)result);
				}
			} else {
				if (CommonLib.isNetworkAvailable(HSLoginActivity.this)) {
					Toast.makeText(HSLoginActivity.this,
							getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							HSLoginActivity.this,
							getResources().getString(
									R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					findViewById(R.id.content).setVisibility(View.GONE);
				}
			}

		}
	}

	private void setInstitutions(List<String> categories) {
		adapter1 = new SimpleListAdapter(mActivity,
				R.layout.simple_list_item, categories);
		mSubzoneSearchListView.setAdapter(adapter1);
	}
	
	private class SimpleListAdapter  extends ArrayAdapter<String> {

		private List<String> wishes;
		private Activity mContext;
		private int width;

		public SimpleListAdapter(Activity context, int resourceId, List<String> wishes) {
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
			if (v == null
					|| v.findViewById(R.id.list_root) == null) {
				v = LayoutInflater.from(mContext).inflate(
						R.layout.simple_list_item, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.text = (TextView) v.findViewById(R.id.text1);
				v.setTag(viewHolder);
			}

			viewHolder.text.setText(wish);
			viewHolder.text.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((TextView)findViewById(R.id.institutionEt)).setText(((TextView)v).getText().toString());
					findViewById(R.id.subzone_search_list_view_container).setVisibility(View.GONE);
				}
			});
			return v;
		}
	}
	
}
