package com.application.baatna.views;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.FacebookConnect;
import com.application.baatna.utils.FacebookConnectCallback;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;
import com.facebook.Session;

public class BaatnaActivity extends Activity implements
		FacebookConnectCallback, UploadManagerCallback {

	int width;
	int height;
	private SharedPreferences prefs;
	private ProgressDialog z_ProgressDialog;
	private boolean dismissDialog = false;

	/** Constant, randomly selected */
	public final int RESULT_FACEBOOK_LOGIN_OK = 1432; // Random numbers
	public final int RESULT_GOOGLE_LOGIN_OK = 1434;

	// RelativeLayout loginPage;
	// RelativeLayout signUpPage;

	final int DEFAULT_SHOWN = 87;
	final int LOGIN_SHOWN = 88;
	final int SIGNUP_SHOWN = 89;
	int mState = DEFAULT_SHOWN;
	public static final int ANIMATION_LOGIN = 200;

	private int lastVisited = 0;

	private final int SIGN_IN = 201;
	private final int SIGN_UP = 202;

	private int nameLength = 0;
	private int emailLength = 0;
	private int pswdLength = 0;

	private String APPLICATION_ID;
	private boolean destroyed = false;

	private String error_responseCode = "";
	private String error_exception = "";
	private String error_stackTrace = "";
	private String login_type = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		prefs = getSharedPreferences("application_settings", 0);
		APPLICATION_ID = prefs.getString("app_id", "");

		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();
		// loginPage = (RelativeLayout) findViewById(R.id.main_login_container);
		// signUpPage = (RelativeLayout)
		// findViewById(R.id.main_signup_container);
		fixSizes();
		UploadManager.addCallback(this);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void fixSizes() {

		// setting the logo
		final ImageView img = (ImageView) findViewById(R.id.baatna_logo);

		try {
			int imageWidth = width;
			int imageHeight = height;

			Bitmap searchBitmap = CommonLib.getBitmap(this,
					R.drawable.splash_final, imageWidth, imageHeight);
			img.getLayoutParams().width = imageWidth;
			img.getLayoutParams().height = imageHeight;
			img.setImageBitmap(searchBitmap);
			
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		// ((RelativeLayout.LayoutParams) img.getLayoutParams()).setMargins(0,
		// height / 2 - height / 5, 0, width / 20);

		int buttonHeight = (11 * 9 * width) / (80 * 10);
		// fb button
		View fb_cont = findViewById(R.id.login_page_layout_connect_using_facebook);
		fb_cont.getLayoutParams().height = buttonHeight;
		((RelativeLayout.LayoutParams) fb_cont.getLayoutParams()).setMargins(
				width / 20, height / 2 + height / 4, width / 10, width / 80);
		((LinearLayout.LayoutParams) findViewById(
				R.id.login_page_facebook_icon_container).getLayoutParams())
				.setMargins(0, 0, width / 20, 0);
		findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().width = buttonHeight;
		findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().height = buttonHeight;
		((TextView) findViewById(R.id.login_page_layout_connect_using_facebook_text))
				.setText(getResources().getString(R.string.login_via_facebook));

		Paint textPaint = ((TextView) findViewById(R.id.login_page_layout_connect_using_facebook_text)).getPaint();
		float textWidth = textPaint.measureText(getResources().getString(R.string.login_via_facebook));
		findViewById(R.id.layout_login_separator).getLayoutParams().width = (int) textWidth + width / 20;
//		 findViewById(R.id.login_page_layout_connect_using_facebook_text).getWidth();
		// findViewById(R.id.layout_signup_text).setPadding(width / 20,
		// width / 20, width / 20, width / 20);
		// findViewById(R.id.layout_login_text).setPadding(width / 20, width /
		// 20,
		// width / 20, width / 20);

		// sign-up page

		// login page

	}

	@Override
	public void response(Bundle bundle) {

		login_type = "FBLogin";
		error_exception = "";
		error_responseCode = "";
		error_stackTrace = "";
		boolean regIdSent = false;

		if (bundle.containsKey("error_responseCode"))
			error_responseCode = bundle.getString("error_responseCode");

		if (bundle.containsKey("error_exception"))
			error_exception = bundle.getString("error_exception");

		if (bundle.containsKey("error_stackTrace"))
			error_stackTrace = bundle.getString("error_stackTrace");

		try {

			int status = bundle.getInt("status");

			if (status == 0) {

				if (!error_exception.equals("")
						|| !error_responseCode.equals("")
						|| !error_stackTrace.equals(""))
					;// BTODO
						// sendFailedLogsToServer();

				if (bundle.getString("errorMessage") != null) {
					String errorMessage = bundle.getString("errorMessage");
					Toast.makeText(getApplicationContext(), errorMessage,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.err_occurred, Toast.LENGTH_SHORT).show();
				}
				if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                    z_ProgressDialog.dismiss();
			} else {
				Editor editor = prefs.edit();
				editor.putInt("uid", bundle.getInt("uid"));
				if (bundle.containsKey("email"))
					editor.putString("email", bundle.getString("email"));
				if (bundle.containsKey("username"))
					editor.putString("username", bundle.getString("username"));
				if (bundle.containsKey("thumbUrl"))
					editor.putString("thumbUrl", bundle.getString("thumbUrl"));
				if (bundle.containsKey("profile_pic"))
					editor.putString("profile_pic", bundle.getString("profile_pic"));
				if (bundle.containsKey("user_name"))
					editor.putString("username", bundle.getString("username"));
				String token = bundle.getString("access_token");
				System.out.println(token);
				editor.putString("access_token",
						bundle.getString("access_token"));
				editor.putBoolean("verifiedUser",
						bundle.getBoolean("verifiedUser"));
				editor.commit();

				// ZTracker.logAppFlyersEvent(BaatnaActivity.this,
				// "Signup_Facebook");
				CommonLib.ZLog("login", "FACEBOOK");

				// if (REQUEST_CODE == START_LOGIN_INTENT
				// || REQUEST_CODE == START_ACTIVITY_LOGIN_INTENT) {
				navigateToHome();
				// setResult(CommonLib.RESULT_FACEBOOK_LOGIN_OK);
				// finish();

				// } else if (REQUEST_CODE ==
				// ZOMATO_LOGIN_CONFIRMATION_REQUIRED) {
				// navigateToHome();
				// // setResult(RESULT_OK);
				// // finish();
				//
				// } else {
				// navigateToHome();
				// // setResult(RESULT_OK);
				// // finish();
				// }

				// GCM
				// if (bundle.containsKey("reg_id_sent")) {
				// regIdSent = bundle.getBoolean("reg_id_sent");
				// if (!regIdSent) {
				// String regId = getRegistrationId(getApplicationContext());
				// if (regId.length() > 0 && prefs.getInt("uid", 0) > 0)
				// sendRegistrationIdToBackend();
				// }
				// }

				if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
					z_ProgressDialog.dismiss();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void facebookAction(View view) {
		z_ProgressDialog = ProgressDialog
				.show(BaatnaActivity.this, null,
						getResources().getString(R.string.verifying_creds),
						true, false);
		z_ProgressDialog.setCancelable(false);
		String regId = prefs.getString("registration_id", "");
		FacebookConnect facebookConnect = new FacebookConnect(
				BaatnaActivity.this, 1, APPLICATION_ID, true, regId);
		facebookConnect.execute();
	}

	@Override
	protected void onDestroy() {
		destroyed = true;
		UploadManager.removeCallback(this);
		ImageView img = (ImageView) findViewById(R.id.baatna_logo);
		img.setImageBitmap(null);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		try {
			super.onActivityResult(requestCode, resultCode, intent);
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, intent);

		} catch (Exception w) {

			w.printStackTrace();

			try {
				com.facebook.Session fbSession = com.facebook.Session
						.getActiveSession();
				if (fbSession != null) {
					fbSession.closeAndClearTokenInformation();
				}
				com.facebook.Session.setActiveSession(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (dismissDialog) {
			if (z_ProgressDialog != null) {
				z_ProgressDialog.dismiss();
			}
		}
	}

	@Override
	public void onBackPressed() {

		// if (mState == SIGNUP_SHOWN) {
		//
		// mState = DEFAULT_SHOWN;
		//
		// Animation animation = new TranslateAnimation(0, 0, 0, height);
		// animation.setDuration(ANIMATION_LOGIN);
		// animation.setAnimationListener(new AnimationListener() {
		//
		// @Override
		// public void onAnimationStart(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// signUpPage.setVisibility(View.GONE);
		//
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(findViewById(R.id.main_root)
		// .getWindowToken(), 0);
		// }
		// });
		// signUpPage.startAnimation(animation);
		//
		// } else if (mState == LOGIN_SHOWN) {
		//
		// mState = DEFAULT_SHOWN;
		//
		// Animation animation = new TranslateAnimation(0, 0, 0, height);
		// animation.setDuration(ANIMATION_LOGIN);
		// animation.setAnimationListener(new AnimationListener() {
		//
		// @Override
		// public void onAnimationStart(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animation animation) {
		// }
		//
		// @Override
		// public void onAnimationEnd(Animation animation) {
		// loginPage.setVisibility(View.GONE);
		//
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(findViewById(R.id.main_root)
		// .getWindowToken(), 0);
		// }
		// });
		// loginPage.startAnimation(animation);
		// }
		super.onBackPressed();
	}

	public void animateScreens(View view) {
		animateToScreen2(view, false);
	}

	public void goBack(View v) {
		onBackPressed();
	}

	private void animateToScreen2(View view, Boolean fromSignUp) {
		int id = view.getId();

		// if (id == R.id.layout_signup_text) {
		//
		// mState = SIGNUP_SHOWN;
		//
		// // animation up
		// signUpPage.setVisibility(View.VISIBLE);
		// Animation animation = new TranslateAnimation(0, 0, height, 0);
		// animation.setDuration(ANIMATION_LOGIN);
		// signUpPage.startAnimation(animation);
		//
		// signupSetup(signUpPage);
		//
		// signUpPage.findViewById(R.id.about_us_terms_conditions_container)
		// .setPadding(width / 20, width / 40, width / 20, width / 40);
		//
		// ((TextView) signUpPage.findViewById(R.id.about_us_terms_conditions))
		// .setText(getResources().getString(R.string.signup_terms));
		//
		// signUpPage.findViewById(R.id.page_header_close).setPadding(
		// width / 20, 0, width / 20, 0);
		// ((TextView) signUpPage.findViewById(R.id.page_header_text))
		// .setText(getResources().getString(R.string.sign_up));
		//
		// ((LinearLayout.LayoutParams) signUpPage.findViewById(
		// R.id.login_details).getLayoutParams()).setMargins(
		// width / 20, width / 40, width / 20, width / 40);
		//
		// final EditText usrnm = ((EditText) signUpPage
		// .findViewById(R.id.login_username));
		// final EditText email = ((EditText) signUpPage
		// .findViewById(R.id.login_email));
		// final EditText pswrd = ((EditText) signUpPage
		// .findViewById(R.id.login_password));
		//
		// usrnm.setPadding(width / 20, 0, width / 20, 0);
		// email.setPadding(width / 20, 0, width / 20, 0);
		// pswrd.setPadding(width / 20, 0, width / 20, 0);
		//
		// usrnm.getLayoutParams().height = width / 7;
		// email.getLayoutParams().height = width / 7;
		// pswrd.getLayoutParams().height = width / 7;
		//
		// // Pre-fill of sign-up credentials
		// try {
		// AccountManager am = AccountManager.get(getApplicationContext());
		// if (am != null) {
		// Account[] accounts = am.getAccounts();
		// Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		// for (Account account : accounts) {
		// if (account.type.equals("com.google")
		// && emailPattern.matcher(account.name).matches()) {
		// email.setText(account.name);
		// }
		// }
		// }
		// } catch (Exception e) {
		// Crashlytics.logException(e);
		// }
		//
		// signUpPage.findViewById(R.id.submit_button).getLayoutParams().height
		// = width / 7;
		// signUpPage.findViewById(R.id.forgot_pass_text).getLayoutParams().height
		// = width / 10;
		// signUpPage.findViewById(R.id.forgot_pass_text).setPadding(0,
		// width / 40, 0, 0);
		//
		// // sign up button
		// ((LinearLayout.LayoutParams) signUpPage.findViewById(
		// R.id.login_submit).getLayoutParams()).setMargins(
		// width / 20, 0, width / 20, 0);
		// ((TextView) signUpPage.findViewById(R.id.submit_button))
		// .setText(getResources().getString(R.string.Sign_up));
		//
		// ((LinearLayout.LayoutParams) signUpPage.findViewById(
		// R.id.login_email_details).getLayoutParams()).setMargins(
		// width / 20, width / 20, width / 20, 0);
		// ((RelativeLayout.LayoutParams) signUpPage.findViewById(
		// R.id.email_login_separator1).getLayoutParams()).setMargins(
		// width / 40, 0, 0, 0);
		// signUpPage.findViewById(R.id.email_login_separator1)
		// .getLayoutParams().width = (width - 2 * width / 40 - signUpPage
		// .findViewById(R.id.help_string).getLayoutParams().width) / 2;
		// ((RelativeLayout.LayoutParams) signUpPage.findViewById(
		// R.id.email_login_separator2).getLayoutParams()).setMargins(
		// 0, 0, width / 40, 0);
		// signUpPage.findViewById(R.id.email_login_separator2)
		// .getLayoutParams().width = (width - 2 * width / 40 - signUpPage
		// .findViewById(R.id.help_string).getLayoutParams().width) / 2;
		//
		// int buttonHeight = (11 * 9 * width) / (80 * 10);
		//
		// View fb_cont = signUpPage
		// .findViewById(R.id.login_page_layout_connect_using_facebook);
		// fb_cont.getLayoutParams().height = buttonHeight;
		// ((RelativeLayout.LayoutParams) fb_cont.getLayoutParams())
		// .setMargins(width / 20, width / 20, width / 20, width / 40);
		// ((LinearLayout.LayoutParams) signUpPage.findViewById(
		// R.id.login_page_facebook_icon_container).getLayoutParams())
		// .setMargins(0, 0, width / 20, 0);
		// signUpPage.findViewById(R.id.login_page_facebook_icon_container)
		// .getLayoutParams().width = buttonHeight;
		// signUpPage.findViewById(R.id.login_page_facebook_icon_container)
		// .getLayoutParams().height = buttonHeight;
		// ((TextView) signUpPage
		// .findViewById(R.id.login_page_layout_connect_using_facebook_text))
		// .setText(getResources().getString(R.string.signup_facebook));
		// ((TextView) signUpPage.findViewById(R.id.help_string))
		// .setText(getResources().getString(
		// R.string.signup_using_email));
		// signUpPage.findViewById(R.id.help_string).setPadding(width / 40, 0,
		// width / 40, 0);
		//
		// // already have an account
		// signUpPage.findViewById(R.id.login_page_already_have_an_account)
		// .setVisibility(View.VISIBLE);
		// signUpPage.findViewById(R.id.login_page_already_have_an_account)
		// .setPadding(0, width / 20, 0, 0);
		// setAlreadyHaveAnAccountText();
		//
		// // empty space at bottom
		// signUpPage.findViewById(R.id.login_blank_view).setVisibility(
		// View.VISIBLE);
		// signUpPage.findViewById(R.id.login_blank_view).getLayoutParams().height
		// = width / 20;
		//
		// usrnm.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start,
		// int before, int count) {
		// nameLength = s.toString().trim().length();
		//
		// int filled = 0;
		// filled = nameLength * pswdLength * emailLength;
		// if (filled > 0) {
		// signUpPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_green_button);
		// } else {
		// signUpPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_button_border);
		// }
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start,
		// int count, int after) {
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// }
		// });
		//
		// email.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start,
		// int before, int count) {
		// emailLength = s.toString().trim().length();
		//
		// int filled = emailLength * pswdLength;
		//
		// if (lastVisited == SIGN_UP) {
		// filled *= nameLength;
		// }
		//
		// if (filled > 0) {
		// signUpPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_green_button);
		// } else {
		// signUpPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_button_border);
		// }
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start,
		// int count, int after) {
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// }
		// });
		//
		// pswrd.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start,
		// int before, int count) {
		// pswdLength = s.toString().trim().length();
		// int filled = emailLength * pswdLength;
		//
		// if (lastVisited == SIGN_UP) {
		// filled *= nameLength;
		// }
		//
		// if (filled > 0) {
		// signUpPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_green_button);
		// } else {
		// signUpPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_button_border);
		// }
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start,
		// int count, int after) {
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// }
		// });
		//
		// ((EditText) findViewById(R.id.forgot_password_email))
		// .setOnEditorActionListener(new OnEditorActionListener() {
		//
		// @Override
		// public boolean onEditorAction(TextView v, int actionId,
		// KeyEvent event) {
		// if (actionId == EditorInfo.IME_ACTION_DONE) {
		// resetPassword(v);
		// return true;
		// }
		// return false;
		// }
		// });
		//
		// signUpPage.findViewById(R.id.login_submit).setOnClickListener(
		// new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// clickSubmitSignup(v);
		// }
		// });
		//
		// pswrd.setOnEditorActionListener(new OnEditorActionListener() {
		//
		// @Override
		// public boolean onEditorAction(TextView v, int keyCode,
		// KeyEvent event) {
		//
		// if (keyCode == EditorInfo.IME_ACTION_DONE) {
		//
		// View focusedView = v;
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(INPUT_METHOD_SERVICE);
		// if (focusedView != null) {
		// imm.hideSoftInputFromWindow(
		// focusedView.getWindowToken(), 0);
		// focusedView.clearFocus();
		//
		// } else
		// imm.hideSoftInputFromWindow(
		// findViewById(R.id.main_root)
		// .getWindowToken(), 0);
		//
		// clickSubmitSignup(findViewById(R.id.login_details));
		//
		// return true;
		// }
		// return false;
		// }
		// });
		//
		// } else if (id == R.id.layout_login_text) {
		// mState = LOGIN_SHOWN;
		//
		// // animations
		// loginPage.setVisibility(View.VISIBLE);
		// if (fromSignUp) {
		//
		// // animation in
		// Animation animation = new TranslateAnimation(width, 0, 0, 0);
		// animation.setDuration(ANIMATION_LOGIN);
		// loginPage.startAnimation(animation);
		//
		// // animation out
		// Animation animation2 = new TranslateAnimation(0, -width, 0, 0);
		// animation2.setDuration(ANIMATION_LOGIN);
		// signUpPage.startAnimation(animation2);
		//
		// signUpPage.setVisibility(View.GONE);
		//
		// } else {
		// Animation animation = new TranslateAnimation(0, 0, height, 0);
		// animation.setDuration(ANIMATION_LOGIN);
		// loginPage.startAnimation(animation);
		// }
		// loginPage.findViewById(R.id.page_header_close).setPadding(
		// width / 20, 0, width / 20, 0);
		// ((TextView) loginPage.findViewById(R.id.page_header_text))
		// .setText(getResources().getString(R.string.login));
		//
		// ((LinearLayout.LayoutParams) loginPage.findViewById(
		// R.id.login_details).getLayoutParams()).setMargins(
		// width / 20, width / 40, width / 20, width / 40);
		//
		// loginPage.findViewById(R.id.about_us_terms_conditions_container)
		// .setPadding(width / 20, width / 40, width / 20, width / 40);
		// ((TextView) loginPage.findViewById(R.id.about_us_terms_conditions))
		// .setText(getResources().getString(R.string.login_terms));
		//
		// int buttonHeight = (11 * 9 * width) / (80 * 10);
		// ((LinearLayout.LayoutParams) loginPage.findViewById(
		// R.id.login_email_details).getLayoutParams()).setMargins(
		// width / 20, width / 20, width / 20, 0);
		// loginPage.findViewById(R.id.email_login_separator1)
		// .getLayoutParams().width = (width - 2 * width / 40 - loginPage
		// .findViewById(R.id.help_string).getLayoutParams().width) / 2;
		// ((RelativeLayout.LayoutParams) loginPage.findViewById(
		// R.id.email_login_separator2).getLayoutParams()).setMargins(
		// 0, 0, width / 40, 0);
		// loginPage.findViewById(R.id.email_login_separator2)
		// .getLayoutParams().width = (width - 2 * width / 40 - loginPage
		// .findViewById(R.id.help_string).getLayoutParams().width) / 2;
		//
		// // fb button
		// View fb_cont = loginPage
		// .findViewById(R.id.login_page_layout_connect_using_facebook);
		// fb_cont.getLayoutParams().height = buttonHeight;
		// ((RelativeLayout.LayoutParams) fb_cont.getLayoutParams())
		// .setMargins(width / 20, width / 20, width / 20, width / 40);
		// ((LinearLayout.LayoutParams) loginPage.findViewById(
		// R.id.login_page_facebook_icon_container).getLayoutParams())
		// .setMargins(0, 0, width / 20, 0);
		// loginPage.findViewById(R.id.login_page_facebook_icon_container)
		// .getLayoutParams().width = buttonHeight;
		// loginPage.findViewById(R.id.login_page_facebook_icon_container)
		// .getLayoutParams().height = buttonHeight;
		// ((TextView) loginPage
		// .findViewById(R.id.login_page_layout_connect_using_facebook_text))
		// .setText(getResources().getString(
		// R.string.login_via_facebook));
		// ((TextView) loginPage.findViewById(R.id.help_string))
		// .setText(getResources().getString(
		// R.string.login_using_email));
		// loginPage.findViewById(R.id.help_string).setPadding(width / 40, 0,
		// width / 40, 0);
		//
		// final EditText usrnm = ((EditText) loginPage
		// .findViewById(R.id.login_username));
		// final EditText email = ((EditText) loginPage
		// .findViewById(R.id.login_email));
		// final EditText pswrd = ((EditText) loginPage
		// .findViewById(R.id.login_password));
		//
		// usrnm.getLayoutParams().height = width / 7;
		// email.getLayoutParams().height = width / 7;
		// pswrd.getLayoutParams().height = width / 7;
		//
		// usrnm.setPadding(width / 20, 0, width / 20, 0);
		// email.setPadding(width / 20, 0, width / 20, 0);
		// pswrd.setPadding(width / 20, 0, width / 20, 0);
		//
		// loginPage.findViewById(R.id.submit_button).getLayoutParams().height =
		// width / 7;
		// loginPage.findViewById(R.id.forgot_pass_text).getLayoutParams().height
		// = width / 10;
		// loginPage.findViewById(R.id.forgot_pass_text).setPadding(0,
		// width / 40, 0, 0);
		//
		// // log in button
		// ((LinearLayout.LayoutParams) loginPage.findViewById(
		// R.id.login_submit).getLayoutParams()).setMargins(
		// width / 20, 0, width / 20, 0);
		// ((TextView) loginPage.findViewById(R.id.submit_button))
		// .setText(getResources().getString(R.string.Login));
		//
		// loginPage.findViewById(R.id.login_blank_view).setVisibility(
		// View.VISIBLE);
		// loginPage.findViewById(R.id.login_blank_view).getLayoutParams().height
		// = width / 20;
		//
		// loginPage.findViewById(R.id.login_page_already_have_an_account)
		// .setVisibility(View.GONE);
		//
		// usrnm.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start,
		// int before, int count) {
		// nameLength = s.toString().trim().length();
		//
		// int filled = 0;
		// filled = nameLength * pswdLength * emailLength;
		// if (filled > 0) {
		// loginPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_green_button);
		// } else {
		// loginPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_button_border);
		// }
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start,
		// int count, int after) {
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// }
		// });
		//
		// email.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start,
		// int before, int count) {
		// emailLength = s.toString().trim().length();
		//
		// int filled = emailLength * pswdLength;
		//
		// if (lastVisited == SIGN_UP) {
		// filled *= nameLength;
		// }
		//
		// if (filled > 0) {
		// loginPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_green_button);
		// } else {
		// loginPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_button_border);
		// }
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start,
		// int count, int after) {
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// }
		// });
		//
		// pswrd.addTextChangedListener(new TextWatcher() {
		//
		// @Override
		// public void onTextChanged(CharSequence s, int start,
		// int before, int count) {
		// pswdLength = s.toString().trim().length();
		// int filled = emailLength * pswdLength;
		//
		// if (lastVisited == SIGN_UP) {
		// filled *= nameLength;
		// }
		//
		// if (filled > 0) {
		// loginPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_green_button);
		// } else {
		// loginPage.findViewById(R.id.login_submit)
		// .setBackgroundResource(
		// R.drawable.bottom_button_border);
		// }
		// }
		//
		// @Override
		// public void beforeTextChanged(CharSequence s, int start,
		// int count, int after) {
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// }
		// });
		//
		// ((EditText) findViewById(R.id.forgot_password_email))
		// .setOnEditorActionListener(new OnEditorActionListener() {
		//
		// @Override
		// public boolean onEditorAction(TextView v, int actionId,
		// KeyEvent event) {
		// if (actionId == EditorInfo.IME_ACTION_DONE) {
		// resetPassword(v);
		// return true;
		// }
		// return false;
		// }
		// });
		//
		// loginSetup(loginPage);
		//
		// loginPage.findViewById(R.id.login_submit).setOnClickListener(
		// new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// clickSubmitLogin(v);
		// }
		// });
		//
		// }
	}

	private void setAlreadyHaveAnAccountText() {

		// String firstString = getString(R.string.already_have_account);
		// String secondString = " " + getString(R.string.Login);
		//
		// SpannableStringBuilder finalStr = new SpannableStringBuilder();
		//
		// // first
		// SpannableString firstStr = new SpannableString(firstString);
		// ClickableSpan firstSpan = new ClickableSpan() {
		// @Override
		// public void onClick(View widget) {
		// mState = LOGIN_SHOWN;
		// animateToScreen2(findViewById(R.id.layout_login_text), true);
		// }
		//
		// @Override
		// public void updateDrawState(TextPaint ds) {
		// super.updateDrawState(ds);
		// ds.setUnderlineText(false);
		// ds.setTextSize(getResources().getDimension(R.dimen.size16));
		// ds.setTypeface(CommonLib.getTypeface(getApplicationContext(),
		// CommonLib.Regular));
		// ds.setColor(getResources()
		// .getColor(R.color.white_trans_seventy));
		// }
		// };
		// firstStr.setSpan(firstSpan, 0, firstString.length(),
		// SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
		//
		// SpannableString secondStr = new SpannableString(secondString);
		// ClickableSpan secondSpan = new ClickableSpan() {
		// @Override
		// public void onClick(View widget) {
		// mState = LOGIN_SHOWN;
		// animateToScreen2(findViewById(R.id.layout_login_text), true);
		// }
		//
		// @Override
		// public void updateDrawState(TextPaint ds) {
		// super.updateDrawState(ds);
		// ds.setUnderlineText(false);
		// ds.setTextSize(getResources().getDimension(R.dimen.size16));
		// ds.setTypeface(CommonLib.getTypeface(getApplicationContext(),
		// CommonLib.Bold));
		// ds.setColor(getResources()
		// .getColor(R.color.white_trans_seventy));
		// }
		// };
		// secondStr.setSpan(secondSpan, 0, secondString.length(),
		// SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
		//
		// finalStr.append(firstStr);
		// finalStr.append(secondStr);
		// ((TextView) signUpPage
		// .findViewById(R.id.login_page_already_have_an_account))
		// .setText(finalStr, TextView.BufferType.SPANNABLE);
		// makeLinksFocusable(((TextView) signUpPage
		// .findViewById(R.id.login_page_already_have_an_account)));
	}

	private void makeLinksFocusable(TextView tv) {
		MovementMethod m = tv.getMovementMethod();
		if ((m == null) || !(m instanceof LinkMovementMethod)) {
			if (tv.getLinksClickable()) {
				tv.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	private void loginSetup(View root) {
		root.findViewById(R.id.login_username).setVisibility(View.GONE);
		root.findViewById(R.id.login_email).setVisibility(View.VISIBLE);
		root.findViewById(R.id.login_password).setVisibility(View.VISIBLE);
		root.findViewById(R.id.view1).setVisibility(View.GONE);
		root.findViewById(R.id.forgot_pass_text).setVisibility(View.VISIBLE);

		((TextView) root.findViewById(R.id.login_email)).setHint(getResources()
				.getString(R.string.email_or_username));

		pswdLength = ((EditText) root.findViewById(R.id.login_password))
				.getText().toString().length();
		emailLength = ((EditText) root.findViewById(R.id.login_email))
				.getText().toString().length();

		int filled = pswdLength * emailLength;

		if (filled > 0) {
			root.findViewById(R.id.login_submit).setBackgroundResource(
					R.drawable.bottom_green_button);
		} else {
			root.findViewById(R.id.login_submit).setBackgroundResource(
					R.drawable.bottom_button_border);
		}
	}

	private void signupSetup(View root) {

		root.findViewById(R.id.view1).setVisibility(View.VISIBLE);
		root.findViewById(R.id.forgot_pass_text).setVisibility(View.GONE);
		root.findViewById(R.id.login_email).setVisibility(View.VISIBLE);
		root.findViewById(R.id.login_password).setVisibility(View.VISIBLE);
		TextView name = (TextView) root.findViewById(R.id.login_username);
		name.setVisibility(View.VISIBLE);

		((TextView) root.findViewById(R.id.login_username))
				.setHint(getResources().getString(R.string.edit_name_hint));
		((TextView) root.findViewById(R.id.login_email)).setHint(getResources()
				.getString(R.string.email));

		nameLength = name.getText().toString().length();
		pswdLength = ((EditText) root.findViewById(R.id.login_password))
				.getText().toString().length();
		emailLength = ((EditText) root.findViewById(R.id.login_email))
				.getText().toString().length();

		int filled = nameLength * pswdLength * emailLength;

		if (filled > 0) {
			root.findViewById(R.id.login_submit).setBackgroundResource(
					R.drawable.bottom_green_button);
		} else {
			root.findViewById(R.id.login_submit).setBackgroundResource(
					R.drawable.bottom_button_border);
		}

	}

	public void resetPassword(View v) {
		// EditText email = (EditText) findViewById(R.id.forgot_password_email);
		// String emailString = email.getText().toString();
		//
		// InputMethodManager keyboard = (InputMethodManager)
		// getSystemService(INPUT_METHOD_SERVICE);
		// keyboard.hideSoftInputFromWindow(findViewById(R.id.main_root)
		// .getWindowToken(), 0);
		//
		// if (emailString.equals("")) {
		// Toast.makeText(getApplicationContext(), R.string.email_blank_error,
		// Toast.LENGTH_LONG).show();
		// } else {
		// new forgotPassword().executeOnExecutor(
		// AsyncTask.THREAD_POOL_EXECUTOR, emailString);
		// }
	}

	private class forgotPassword extends AsyncTask<String, Void, String[]> {

		@Override
		protected String[] doInBackground(String... params) {

			try {

				String email = params[0].trim();

				InputStream is = RequestWrapper.fetchhttp(CommonLib.SERVER
						+ "forgot_password.xml?email="
						+ java.net.URLEncoder.encode(email, "UTF-8") + "&uuid="
						+ APPLICATION_ID
						+ CommonLib.getVersionString(getApplicationContext()));

				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(is);
				Element res = dom.getDocumentElement();

				Element statusEle = (Element) res
						.getElementsByTagName("status").item(0);
				String status = statusEle.getFirstChild().getNodeValue();

				Element messageEle = (Element) res.getElementsByTagName(
						"message").item(0);
				String message = messageEle.getFirstChild().getNodeValue();

				return new String[] { status, message };

			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			// if (result == null) {
			// Toast.makeText(getApplicationContext(), R.string.error_message,
			// Toast.LENGTH_LONG).show();
			// } else {
			//
			// if (result[0].equals("success")) {
			//
			// findViewById(R.id.reset_result).setVisibility(View.VISIBLE);
			// findViewById(R.id.reset_container).setVisibility(View.GONE);
			// findViewById(R.id.forgot_password_email).setVisibility(
			// View.GONE);
			// ((TextView) findViewById(R.id.reset_result))
			// .setText(result[1]);
			//
			// } else {
			// Toast.makeText(getApplicationContext(), result[1],
			// Toast.LENGTH_LONG).show();
			// }
			//
			// }
		}
	}

	protected void clickSubmitLogin(View v) {

		lastVisited = SIGN_IN;

		CommonLib.ZLog("clickSubmitLogin", "clickSubmitLogin");

		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.main_root)
				.getWindowToken(), 0);

		// login(loginPage.findViewById(R.id.login_details));
	}

	protected void clickSubmitSignup(View v) {

		lastVisited = SIGN_UP;

		CommonLib.ZLog("clickSubmitSignup", "clickSubmitSignup");

		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.main_root)
				.getWindowToken(), 0);

		// zomatoSignUp(signUpPage.findViewById(R.id.login_details));
	}

	public void login(View view) {
		// make the api call.
		// String email = ((TextView) loginPage.findViewById(R.id.login_email))
		// .getText().toString();
		// if (email == null || email.equals(""))
		// return;
		// String password = ((TextView) loginPage
		// .findViewById(R.id.login_password)).getText().toString();
		// if (password == null || password.equals(""))
		// return;
		// login_type = "baatna_login - auth";
		//
		// UploadManager.login(email, password);
	}

	public void zomatoSignUp(View view) {
		// String name = ((TextView)
		// signUpPage.findViewById(R.id.login_username))
		// .getText().toString();
		// if (name == null || name.equals(""))
		// return;
		// String email = ((TextView) signUpPage.findViewById(R.id.login_email))
		// .getText().toString();
		// if (email == null || email.equals(""))
		// return;
		// String password = ((TextView) signUpPage
		// .findViewById(R.id.login_password)).getText().toString();
		// if (password == null || password.equals(""))
		// return;
		// UploadManager.signUp(name, email, password);
	}

	@Override
	public void uploadFinished(int requestType, int userId, int objectId,
			Object data, int uploadId, boolean status, String stringId) {
		if (requestType == CommonLib.SIGNUP) {
			if (destroyed)
				return;
			if (status) {
				// make the login call now.
				// String name = ((TextView) signUpPage
				// .findViewById(R.id.login_username)).getText()
				// .toString();
				// if (name == null || name.equals(""))
				// return;
				// String email = ((TextView) signUpPage
				// .findViewById(R.id.login_email)).getText().toString();
				// if (email == null || email.equals(""))
				// return;
				// String password = ((TextView) signUpPage
				// .findViewById(R.id.login_password)).getText()
				// .toString();
				// if (password == null || password.equals(""))
				// return;
				// UploadManager.login(email, password);
				// } else {

			}
		} else if (requestType == CommonLib.LOGIN) {
			if (destroyed)
				return;
			if (status) {
				// save the access token.
				// {"access_token":"532460","user":{"user_id":0,"is_verified":0}}
				JSONObject responseJSON = null;
				try {
					responseJSON = new JSONObject(String.valueOf(data));
					SharedPreferences.Editor editor = prefs.edit();
					if (responseJSON.has("access_token")) {
						editor.putString("access_token",
								responseJSON.getString("access_token"));
					}
					if (responseJSON.has("HSLogin")
							&& responseJSON.get("HSLogin") instanceof Boolean) {
						editor.putBoolean("HSLogin",
								responseJSON.getBoolean("HSLogin"));
					}
					if (responseJSON.has("INSTITUTION_NAME")) {
						editor.putString("INSTITUTION_NAME",
								responseJSON.getString("INSTITUTION_NAME"));
					}
					if (responseJSON.has("STUDENT_ID")) {
						editor.putString("STUDENT_ID",
								responseJSON.getString("STUDENT_ID"));
					}
					if (responseJSON.has("user_id")
							&& responseJSON.get("user_id") instanceof Integer) {
						editor.putInt("uid", responseJSON.getInt("user_id"));
					}
					editor.commit();
					navigateToHome();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId,
			Object object) {
		if (requestType == CommonLib.SIGNUP) {
			if (destroyed)
				return;
		}
	}

	public void navigateToHome() {
		if (prefs.getInt("uid", 0) != 0) {
			if (prefs.getBoolean("instutionLogin", true) && prefs.getBoolean("HSLogin", true)) {
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

}