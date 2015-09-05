package com.application.baatna.views;

import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

public class NewRequestActivity extends Activity implements UploadManagerCallback {

	private SharedPreferences prefs;
	private NewRequestFragment mFragment;
	private boolean isChecked = true;
	private boolean isDestroyed = false;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.single_fragment_container);

		prefs = getSharedPreferences("application_settings", 0);
		setupActionBar();
		mFragment = new NewRequestFragment();
		mFragment.setArguments(getIntent().getExtras());
		getFragmentManager().beginTransaction().add(R.id.fragment_container, mFragment, "request_fragment_container")
				.commit();
		UploadManager.addCallback(this);
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
	}
}
