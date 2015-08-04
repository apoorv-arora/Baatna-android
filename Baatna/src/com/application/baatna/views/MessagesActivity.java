package com.application.baatna.views;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.application.baatna.R;

public class MessagesActivity extends Activity {

	private int width;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.message_activity);

		width = getWindowManager().getDefaultDisplay().getWidth();

		setUpActionBar();
	}

	private void setUpActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setTitle(getResources().getString(R.string.messages));

		try {
			ViewGroup home = (ViewGroup) findViewById(android.R.id.home)
					.getParent();
			home.getChildAt(0).setPadding(width / 80, 0, width / 80, 0);
		} catch (Exception e) {
		}

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
}
