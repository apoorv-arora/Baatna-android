package com.application.baatna.views;

import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewRequestActivity extends Activity implements UploadManagerCallback {

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
		setupActionBar();
		mFragment = new NewRequestFragment();
		mFragment.setArguments(getIntent().getExtras());
		getFragmentManager().beginTransaction().add(R.id.fragment_container, mFragment, "request_fragment_container")
				.commit();
		UploadManager.addCallback(this);
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		View v = inflater.inflate(R.layout.green_action_bar, null);

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
}
