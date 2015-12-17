package com.application.baatna.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.TypefaceSpan;
import com.google.android.gms.plus.PlusOneButton;

public class AboutUs extends AppCompatActivity {

	private int width;
	PlusOneButton mPlusOneButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		width = getWindowManager().getDefaultDisplay().getWidth();
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);
		setUpActionBar();
		fixsizes();
		setListeners();

		mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
		ImageView img = (ImageView) findViewById(R.id.zomato_logo);
		img.getLayoutParams().width = width / 3;
		img.getLayoutParams().height = width / 3;
		// setting image
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher,
					options);
			options.inSampleSize = CommonLib.calculateInSampleSize(options,
					width, width);
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Config.RGB_565;
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher, options);

			img.setImageBitmap(bitmap);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			img.setBackgroundColor(getResources().getColor(R.color.black));
		} catch (Exception e) {
			e.printStackTrace();
			img.setBackgroundColor(getResources().getColor(R.color.black));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			if (mPlusOneButton != null)
				mPlusOneButton.initialize(
						"https://market.android.com/details?id="
								+ getPackageName(), 0);
		} catch (Exception d) {

		}
	}

	private void setUpActionBar() {

		android.support.v7.app.ActionBar actionBar = getSupportActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setElevation(0);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.about_us));
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

		((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
				.setMargins(width / 40, 0, 0, 0);
		actionBarCustomView.findViewById(R.id.title).setPadding(width / 20, 0, width / 40, 0);
		title.setText(s);
		title.setAllCaps(true);
	}

	void fixsizes() {

		width = getWindowManager().getDefaultDisplay().getWidth();

		// About us main page layouts
		// findViewById(R.id.home_logo).getLayoutParams().height = 3 * width /
		// 10;
		// findViewById(R.id.home_logo).getLayoutParams().width = 3 * width /
		// 10;
		findViewById(R.id.home_version).setPadding(width / 20, 0, 0, 0);
		findViewById(R.id.home_logo_container).setPadding(width / 20,
				width / 20, width / 20, width / 20);
		findViewById(R.id.about_us_body).setPadding(width / 20, 0, width / 20,
				width / 20);
		((LinearLayout.LayoutParams) findViewById(R.id.plus_one_button)
				.getLayoutParams()).setMargins(width / 20, width / 20,
				width / 20, width / 20);

		RelativeLayout.LayoutParams relativeParams2 = new RelativeLayout.LayoutParams(
				width, 9 * width / 80);
		relativeParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		findViewById(R.id.about_us_privacy_policy_container).setLayoutParams(
				relativeParams2);
		((TextView) ((LinearLayout) findViewById(R.id.about_us_privacy_policy_container))
				.getChildAt(0)).setPadding(width / 20, 0, 0, 0);
		findViewById(R.id.about_us_privacy_policy).setPadding(width / 40, 0,
				width / 20, 0);

		RelativeLayout.LayoutParams relativeParams3 = new RelativeLayout.LayoutParams(
				width, 9 * width / 80);
		relativeParams3.addRule(RelativeLayout.ABOVE, R.id.separator3);
		findViewById(R.id.about_us_terms_conditions_container).setLayoutParams(
				relativeParams3);
		((TextView) ((LinearLayout) findViewById(R.id.about_us_terms_conditions_container))
				.getChildAt(0)).setPadding(width / 20, 0, 0, 0);
		findViewById(R.id.about_us_terms_conditions).setPadding(width / 40, 0,
				width / 20, 0);
	}

	public void setListeners() {

		LinearLayout btnTermsAndConditons = (LinearLayout) findViewById(R.id.about_us_terms_conditions_container);
		btnTermsAndConditons.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// tracker.trackPageView("/android/About_TC/"+zapp.city_id);
				// trackerAll.trackPageView("/android/About_TC/"+zapp.city_id);

				Intent intent = new Intent(AboutUs.this, BWebView.class);
				intent.putExtra("title",
						getResources()
								.getString(R.string.about_us_terms_of_use));
				// intent.putExtra("url",
				// "https://www.zomato.com/terms_mobile.html");
				// startActivity(intent);
				// ////overridePendingTransition(R.anim.slide_in_right,
				// R.anim.slide_out_left);

			}
		});

		LinearLayout btnPrivacyPolicy = (LinearLayout) findViewById(R.id.about_us_privacy_policy_container);
		btnPrivacyPolicy.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// tracker.trackPageView("/android/About_Privacy/"+zapp.city_id);
				// trackerAll.trackPageView("/android/About_Privacy/"+zapp.city_id);

				Intent intent = new Intent(AboutUs.this, BWebView.class);
				intent.putExtra(
						"title",
						getResources().getString(
								R.string.about_us_privacypolicy));
				// intent.putExtra("url",
				// "https://www.zomato.com/privacy_mobile.html");
				// startActivity(intent);
				// //overridePendingTransition(R.anim.slide_in_right,
				// R.anim.slide_out_left);

			}
		});
	}

	public void goBack(View view) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void actionBarSelected(View v) {

		switch (v.getId()) {

		case R.id.home_icon_container:
			onBackPressed();

		default:
			break;
		}

	}

}
