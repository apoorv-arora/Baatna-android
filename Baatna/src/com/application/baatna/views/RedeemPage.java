package com.application.baatna.views;

import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class RedeemPage extends Activity implements UploadManagerCallback{

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
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

	@Override
	protected void onDestroy() {
		isDestroyed = true;
		UploadManager.removeCallback(this);
		super.onDestroy();
	}
	
	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (requestType == CommonLib.WISH_ADD) {
			if (!isDestroyed) {
				z_ProgressDialog = ProgressDialog.show(RedeemPage.this, null,
						getResources().getString(R.string.wish_post_dialog), true, false);
				z_ProgressDialog.setCancelable(false);
			}
		}
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

}
