package com.application.baatna.views;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.application.baatna.R;

public class UserPageActivity extends Activity {

	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.user_page_activity);

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
			home.getChildAt(0).setPadding(width/80, 0, width/80, 0);	
		} catch (Exception e) {
		}
	}
	
	@Override
	public void onBackPressed() {
		
		super.onBackPressed();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(prefs.getInt("uid", 0) == 0 ){
			Intent intent = new Intent(UserPageActivity.this, BaatnaActivity.class);
			startActivity(intent);	
			finish();
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
}
