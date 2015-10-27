package com.application.baatna.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.PostWrapper;
import com.application.baatna.utils.TypefaceSpan;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackPage extends Activity implements UploadManagerCallback {

	int screenWidth;
	int screenHeight;

	private BaatnaApp zapp;
	private SharedPreferences prefs;

	final Context context = this;

	private TextView feedbackEmailText;
	private final int EMAIL_FEEDBACK = 1500;
	private View actionBarCustomView;

	private boolean destroyed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.feedback_page);

		UploadManager.addCallback(this);
		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();

		feedbackEmailText = (TextView) findViewById(R.id.feedback_email);
		feedbackEmailText.setTextColor(getResources().getColor(R.color.black));
		feedbackEmailText.setText(getFeedbackEmailSpannableText(), TextView.BufferType.SPANNABLE);
		feedbackEmailText.setMovementMethod(LinkMovementMethod.getInstance());
		feedbackEmailText.setPadding(0, screenWidth / 20, 0, 0);

		zapp = (BaatnaApp) getApplication();
		prefs = getSharedPreferences("application_settings", 0);

		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		setUpActionBar();
		fixSizes();
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	private void setUpActionBar() {
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.feedback_title));
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

		((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
				.setMargins(screenWidth / 40, 0, screenWidth / 40, 0);
		actionBarCustomView.findViewById(R.id.title).setPadding(screenWidth / 40, 0, screenWidth / 40, 0);
		title.setText(s);
	}

	public void actionBarSelected(View v) {

		switch (v.getId()) {

		case R.id.home_icon_container:
			onBackPressed();
			break;

		default:
			break;
		}

	}

	private SpannableString getFeedbackEmailSpannableText() {

		String feedbackText = getResources().getString(R.string.feedback_email);
		String email = "android@baatna.com";
		SpannableString ss = new SpannableString(feedbackText);
		ClickableSpan clickableSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				feedbackEmailText.setEnabled(false);
				sendFeedbackEmail();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setUnderlineText(false);
				ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Regular));
				ds.setTextSize(getResources().getDimension(R.dimen.size12));
				ds.setColor(getResources().getColor(R.color.z_red_feedback));
			}
		};

		if (feedbackText.indexOf(email) > -1)
			ss.setSpan(clickableSpan, feedbackText.indexOf(email), feedbackText.indexOf(email) + email.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return ss;
	}

	private void sendFeedbackEmail() {
		Intent i = new Intent(Intent.ACTION_SEND);

		i.setType("application/octet-stream");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "android@baatna.com" });
		i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.feedback_email_subject));

		try {
			final String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
					+ CommonLib.getNetworkState(this) + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
					+ "Location     : " + zapp.lat + " , " + zapp.lon + "\n" + "User Id     	: "
					+ prefs.getInt("uid", 0) + "\n" + "User Agent   : "
					+ CommonLib.getVersionString(getApplicationContext()) + "&device=" + Build.MANUFACTURER + ", "
					+ Build.BRAND + ", " + Build.MODEL);

			FileOutputStream fOut = openFileOutput("log.txt", MODE_WORLD_READABLE);
			File file = getFileStreamPath("log.txt");
			Uri uri = Uri.fromFile(file);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			osw.write(LogString);
			osw.flush();
			osw.close();
			i.putExtra(Intent.EXTRA_STREAM, uri);
		} catch (Exception e) {
		}

		try {
			startActivityForResult(Intent.createChooser(i, getResources().getString(R.string.send_mail)),
					EMAIL_FEEDBACK);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_email_clients),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EMAIL_FEEDBACK) {
			deleteFile("log.txt");
			feedbackEmailText.setEnabled(true);
			// onBackPressed();
		}
	}

	private void fixSizes() {

		if (actionBarCustomView != null) {
			actionBarCustomView.findViewById(R.id.tick_container).setVisibility(View.GONE);
		}

		findViewById(R.id.submit_button).getLayoutParams().height = screenWidth / 10;
		findViewById(R.id.submit_button).setEnabled(false);
		findViewById(R.id.submit_button).setClickable(false);

		findViewById(R.id.feedback_content).getLayoutParams().height = screenHeight / 2;
		EditText feedbackContent = (EditText) findViewById(R.id.feedback_content);
		feedbackContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equals("") || s.toString().trim().length() < 1) {
					actionBarCustomView.findViewById(R.id.tick_container).setVisibility(View.GONE);
					/*
					 * findViewById(R.id.submit_button).setEnabled(false);
					 * findViewById(R.id.submit_button).setClickable(false);
					 * findViewById(R.id.submit_button).setBackgroundColor(
					 * getResources().getColor(R.color.zhl_dark));
					 */
				} else {/*
						 * findViewById(R.id.submit_button).setEnabled(true);
						 * findViewById(R.id.submit_button).setClickable(true);
						 * findViewById(R.id.submit_button).
						 * setBackgroundDrawable(getResources().getDrawable(R.
						 * drawable.greenbuttonfeedback));
						 */
					actionBarCustomView.findViewById(R.id.tick_container).setVisibility(View.VISIBLE);
				}
			}
		});

		feedbackContent.setPadding(screenWidth / 40, screenWidth / 40, screenWidth / 40, 0);
		findViewById(R.id.feedback_container).setPadding(screenWidth / 20, screenWidth / 20, screenWidth / 20,
				screenWidth / 20);
	}

	private String getLogString() {
		String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
				+ CommonLib.getNetworkState(this) + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
				+ "Location     : " + zapp.lat + " , " + zapp.lon + "\n" + "User Id      : " + prefs.getInt("uid", 0)
				+ "\n" + "User Agent   : " + CommonLib.getVersionString(getApplicationContext()) + "&device="
				+ Build.MANUFACTURER + "," + Build.BRAND + "," + Build.MODEL);

		return LogString;
	}

	@Override
	public void onBackPressed() {
		try {
			InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(findViewById(R.id.feedback_content).getWindowToken(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		FeedbackPage.this.finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public void submit(View v) {

		if (!((EditText) findViewById(R.id.feedback_content)).getText().toString().equals("")) {
			String message = ((EditText) findViewById(R.id.feedback_content)).getText().toString();
			String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
					+ CommonLib.getNetworkState(this) + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
					+ "Location     : " + zapp.lat + " , " + zapp.lon + "\n" + "User Id      : "
					+ prefs.getInt("uid", 0) + "\n" + "User Agent   : "
					+ CommonLib.getVersionString(getApplicationContext()) + "&device=" + Build.MANUFACTURER + ","
					+ Build.BRAND + "," + Build.MODEL);
			UploadManager.sendFeedback(message, LogString);
			// new
			// sendFeedback().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
			// message);
		}
	}

	public void proceed(View v) {
		if (!((EditText) findViewById(R.id.feedback_content)).getText().toString().equals("")
				&& ((EditText) findViewById(R.id.feedback_content)).getText().toString().trim().length() > 0) {
			String message = ((EditText) findViewById(R.id.feedback_content)).getText().toString();
			String LogString = new String("App Version  : " + CommonLib.VERSION_STRING + "\n" + "Connection   : "
					+ CommonLib.getNetworkState(this) + "\n" + "Identifier   : " + prefs.getString("app_id", "") + "\n"
					+ "Location     : " + zapp.lat + " , " + zapp.lon + "\n" + "User Id      : "
					+ prefs.getInt("uid", 0) + "\n" + "User Agent   : "
					+ CommonLib.getVersionString(getApplicationContext()) + "&device=" + Build.MANUFACTURER + ","
					+ Build.BRAND + "," + Build.MODEL);

			UploadManager.sendFeedback(message, LogString);
			// new
			// sendFeedback().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
			// message);
		}
	}

	private class sendFeedback extends AsyncTask<String, Void, Element> {

		@Override
		protected void onPreExecute() {
			if (CommonLib.isNetworkAvailable(context))
				onBackPressed();
			else
				Toast.makeText(context, getResources().getString(R.string.no_internet_message), Toast.LENGTH_LONG)
						.show();

			super.onPreExecute();
		}

		@Override
		protected Element doInBackground(String... params) {

			try {

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("message", params[0]));
				nameValuePairs.add(new BasicNameValuePair("attachment", getLogString()));

				HttpResponse response = PostWrapper.getPostResponse(
						CommonLib.SERVER + "feedback.xml?" + CommonLib.getVersionString(getApplicationContext()),
						nameValuePairs, FeedbackPage.this);
				InputStream is = CommonLib.getStream(response);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(is);
				Element res = dom.getDocumentElement();
				is.close();
				return res;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Element res) {

			if (res != null) {
				try {
					boolean status = (res.getElementsByTagName("status").item(0).getFirstChild().getNodeValue()
							.equals("1")) ? true : false;
					CommonLib.ZLog("FeedBack Result",
							"status : " + (res.getElementsByTagName("status").item(0).getFirstChild().getNodeValue()));
					if (!status) {
						// Toast.makeText(context,
						// getResources().getString(R.string.feedback_failed),
						// Toast.LENGTH_LONG).show();
					} else {
						// onBackPressed(); //COMMENTED
					}
				} catch (Exception e) {
					// Toast.makeText(context,
					// getResources().getString(R.string.feedback_failed),
					// Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			} else {
				// Toast.makeText(context,
				// getResources().getString(R.string.could_not_connect),
				// Toast.LENGTH_LONG).show();
			}

		}

	}

	public void goBack(View view) {
		onBackPressed();
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
			String stringId) {
		if (requestType == CommonLib.SEND_FEEDBACK) {
			if (!destroyed && status) {
				Toast.makeText(FeedbackPage.this, "Thank you for your feeback, it means a lot to us!",
						Toast.LENGTH_LONG).show();
				onBackPressed();
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

	}
}