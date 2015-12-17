package com.application.baatna.views;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.TypefaceSpan;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

public class NewRequestActivity extends AppCompatActivity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private NewRequestFragment mFragment;
	private boolean isChecked = true;
	private boolean isDestroyed = false;
	private ProgressDialog z_ProgressDialog;
	LayoutInflater inflater;
	private int width;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.single_fragment_container);

		prefs = getSharedPreferences("application_settings", 0);
		width = getWindowManager().getDefaultDisplay().getWidth();
		inflater = LayoutInflater.from(this);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);
		setupActionBar();
		mFragment = new NewRequestFragment();
		mFragment.setArguments(getIntent().getExtras());
		getFragmentManager().beginTransaction().add(R.id.fragment_container, mFragment, "request_fragment_container")
				.commit();
		UploadManager.addCallback(this);
	}

	private void setupActionBar() {
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		if(Build.VERSION.SDK_INT > 20)
			actionBar.setElevation(0);
		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.green_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.make_new_request));
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

		((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
				.setMargins(width / 20 + width / 80 + width / 100, 0, 0, 0);
		actionBarCustomView.findViewById(R.id.title).setPadding(width / 20, 0, width / 40, 0);
		title.setText(s);
		title.setAllCaps(true);

	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getFragmentManager().findFragmentByTag("request_fragment_container");
		if (fragment != null) {
			if (isChecked && !((NewRequestFragment) fragment).getSelectedCategory().equals("")) {
				new AlertDialog.Builder(this).setMessage(getResources().getString(R.string.back_pressed_confimation))
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								isChecked = false;
								onBackPressed();
							}
						}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								isChecked = true;
							}
						}).show();
			} else
				super.onBackPressed();

		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

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
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.WISH_ADD) {
			if (!isDestroyed && status) {
				Toast.makeText(this, "Wish added successfully", Toast.LENGTH_LONG).show();
				isChecked = false;
				onBackPressed();
			}
			if (!isDestroyed && z_ProgressDialog != null) {
				z_ProgressDialog.dismiss();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Fragment fragment = getFragmentManager().findFragmentByTag("request_fragment_container");
		if (fragment != null) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (requestType == CommonLib.WISH_ADD) {
			if (!isDestroyed) {
				z_ProgressDialog = ProgressDialog.show(NewRequestActivity.this, null,
						getResources().getString(R.string.wish_post_dialog), true, false);
				z_ProgressDialog.setCancelable(false);
			}
		}
	}
	public void actionBarSelected(View v) {

		switch (v.getId()) {

			case R.id.home_icon_container:
				onBackPressed();
			default:
				break;
		}

	}
	public void postRequest(View v){

		LayoutInflater mInflater = inflater;
		Context mContext=getBaseContext();
		View rootView = mInflater.inflate(R.layout.new_request_fragment, null);
		String timeduration= ((TextView) findViewById(R.id.time_duration)).getText().toString();
		String title = ((TextView) findViewById(R.id.category_et)).getText().toString();
		String description = ((TextView) findViewById(R.id.description_et)).getText().toString();

		if (title == null || title.length() < 1) {
			Toast.makeText(mContext, "Please enter title of the request", Toast.LENGTH_SHORT).show();
			((TextView) rootView.findViewById(R.id.category_et)).requestFocus();
			return;
		}

		if (timeduration== null ) {
			Toast.makeText(mContext, "Please enter time duration of the request", Toast.LENGTH_SHORT).show();
			((TextView) rootView.findViewById(R.id.time_duration)).requestFocus();
			return;
		}
		if (description == null || description.length() < 30) {
			Toast.makeText(mContext, "Please enter description of the request of at least 30 characters", Toast.LENGTH_SHORT).show();
			((TextView) rootView.findViewById(R.id.description_et)).requestFocus();
			return;
		}

		UploadManager.postNewRequest(prefs.getString("access_token", ""), title, description);
		try {
			InputMethodManager imm = (InputMethodManager) mContext
					.getSystemService(Service.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
