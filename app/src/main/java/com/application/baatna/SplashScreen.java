package com.application.baatna;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.utils.BTracker;
import com.application.baatna.utils.BaatnaLocationCallback;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.FacebookConnect;
import com.application.baatna.utils.FacebookConnectCallback;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;
import com.application.baatna.views.Home;
import com.application.baatna.views.UserLoginActivity;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by apoorvarora on 20/02/16.
 */
public class SplashScreen extends Activity implements FacebookConnectCallback, UploadManagerCallback, BaatnaLocationCallback {

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

    private String APPLICATION_ID;
    private boolean destroyed = false;
    Location loc;

    private String error_responseCode = "";
    private String error_exception = "";
    private String error_stackTrace = "";

    private BaatnaApp zapp;
    private Activity context;
    private boolean windowHasFocus = false;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regId;
    int hardwareRegistered = 0;

    ImageView imgBg;

    private Animation animation1, animation2, animation3;
    private TranslateAnimation translation;

    private ViewPager mViewPager;
    //    private RelativeLayout mSignupContainer;
    private boolean firstBackground = true;

    private boolean hasSwipedPager = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        prefs = getSharedPreferences("application_settings", 0);
        context = this;
        zapp = (BaatnaApp) getApplication();
        APPLICATION_ID = prefs.getString("app_id", "");

        //hacking zomato
//        final ArrayList<HashMap<String,String>> LIST = new ArrayList<HashMap<String,String>>();
//        try {
//            Context myContext = createPackageContext("in.amazon.mShop.android.shopping", Context.CONTEXT_IGNORE_SECURITY); // where com.example is the owning  app containing the preferences
//            SharedPreferences testPrefs = myContext.getSharedPreferences
//                    ("application_settings", Context.MODE_WORLD_READABLE);
//            String test = testPrefs.getString("access_token", "");
//            CommonLib.ZLog("test", test);
//        } catch(PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }

        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        imgBg = (ImageView) findViewById(R.id.baatna_background);

        try {
            int imageWidth = width;
            int imageHeight = height;

            Bitmap bgBitmap = CommonLib.getBitmap(this, R.drawable.bg, imageWidth, imageHeight);
            imgBg.getLayoutParams().width = imageWidth;
            imgBg.getLayoutParams().height = imageHeight;
            imgBg.setImageBitmap(bgBitmap);

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        imgBg.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationCheck();
            }
        }, 2000);

        onCoordinatesIdentified(loc);

        //stuffs
        mViewPager = (ViewPager) findViewById(R.id.tour_view_pager);
        mViewPager.setOffscreenPageLimit(4);
//        mSignupContainer = (RelativeLayout) findViewById(R.id.signup_container);

        TourPagerAdapter mTourPagerAdpater = new TourPagerAdapter();
        ((ViewPager) mViewPager).setAdapter(mTourPagerAdpater);

        ((ViewPager) mViewPager).setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int position = ((ViewPager) mViewPager).getOffscreenPageLimit();

            @Override
            public void onPageSelected(int arg0) {

                final int pos = arg0;
                // startPosition = arg0;

                LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);

                int index = 5;
                for (int count = 0; count < index; count++) {
                    ImageView dots = (ImageView) dotsContainer.getChildAt(count);

                    if (count == arg0)
                        dots.setImageResource(R.drawable.tour_image_dots_selected);
                    else
                        dots.setImageResource(R.drawable.tour_image_dots_unselected);
                }


                if (arg0 == 0 || arg0 == 1 || arg0 == 2) {
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);
                    findViewById(R.id.skip_container).setVisibility(View.VISIBLE);

                    if(arg0 == 0 && !firstBackground) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!destroyed && pos == 0 && !firstBackground) {
                                    int imageWidth = width;
                                    int imageHeight = height;
                                    Bitmap bgBitmap = CommonLib.getBitmap(context, R.drawable.bg, imageWidth, imageHeight);
                                    imgBg.getLayoutParams().width = imageWidth;
                                    imgBg.getLayoutParams().height = imageHeight;
                                    imgBg.setImageBitmap(bgBitmap);
                                    firstBackground = true;
                                }
                            }
                        }, 500);

                    } else if(firstBackground && arg0 != 0) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!destroyed && firstBackground && pos != 0) {
                                    int imageWidth = width;
                                    int imageHeight = height;

                                    Bitmap bgBitmap = CommonLib.getBitmap(context, R.drawable.bg2, imageWidth, imageHeight);
                                    imgBg.getLayoutParams().width = imageWidth;
                                    imgBg.getLayoutParams().height = imageHeight;
                                    imgBg.setImageBitmap(bgBitmap);
                                    firstBackground = false;
                                }
                            }
                        }, 500);

                    }
                } else if (arg0 == 3 || arg0 == 4) {
                    findViewById(R.id.signup_container).setVisibility(View.VISIBLE);
                    findViewById(R.id.skip_container).setVisibility(View.GONE);
                    if(firstBackground && mViewPager.getCurrentItem() != 0) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!destroyed && firstBackground && pos != 0) {
                                    int imageWidth = width;
                                    int imageHeight = height;

                                    Bitmap bgBitmap = CommonLib.getBitmap(context, R.drawable.bg2, imageWidth, imageHeight);
                                    imgBg.getLayoutParams().width = imageWidth;
                                    imgBg.getLayoutParams().height = imageHeight;
                                    imgBg.setImageBitmap(bgBitmap);
                                    firstBackground = false;
                                }
                            }
                        }, 500);

                    }
                }



            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        ((ViewPager) mViewPager).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hasSwipedPager = true;
                return false;
            }
        });
        fixSizes();
        animate();
        UploadManager.addCallback(this);
        updateDotsContainer();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!destroyed)
                    startTimer();
            }
        }, 1000);

        findViewById(R.id.skip_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewPager != null) {
                    hasSwipedPager = true;
                    mViewPager.setCurrentItem(4, true);
                }
            }
        });
    }
    View description;

    private void animate() {
        try {
            final View tourDots = findViewById(R.id.tour_dots);

            if(mViewPager != null && mViewPager.getCurrentItem() == 0 && mViewPager.getChildAt(mViewPager.getCurrentItem()) != null)
                description = mViewPager.getChildAt(mViewPager.getCurrentItem()).findViewById(R.id.description);

//            mSignupContainer.setVisibility(View.INVISIBLE);
            tourDots.setVisibility(View.INVISIBLE);
            if(description != null)
                description.setVisibility(View.INVISIBLE);
            translation = new TranslateAnimation(10f, 0F, 0f, 0F);
            translation.setDuration(200);
            translation.setFillAfter(true);
            translation.setInterpolator(new BounceInterpolator());

            animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            animation2.setDuration(500);
            animation2.restrictDuration(700);
            animation2.scaleCurrentDuration(1);
            animation2.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
//                    tourDots.startAnimation(translation);
                }
            });

            animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);
            animation3.setInterpolator(new DecelerateInterpolator());
            animation3.restrictDuration(700);
            animation3.scaleCurrentDuration(1);
            animation3.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tourDots.setVisibility(View.VISIBLE);
                    if (description != null) {
                        description.setVisibility(View.VISIBLE);
                        description.startAnimation(animation2);
                    }
                    tourDots.startAnimation(animation2);
                }
            });

            animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_center);
            animation1.setDuration(700);
            animation1.restrictDuration(700);
            animation1.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (prefs.getInt("uid", 0) == 0) {
                        tourDots.setVisibility(View.VISIBLE);
                        tourDots.startAnimation(animation2);
//                        mSignupContainer.setVisibility(View.VISIBLE);
//                        mSignupContainer.startAnimation(animation3);
                    } else {
                        checkPlayServices();
                    }
                }
            });
            animation1.scaleCurrentDuration(1);
            mViewPager.startAnimation(animation1);
        } catch (Exception e) {
            imgBg.setVisibility(View.VISIBLE);
//            findViewById(R.id.layout_login_separator).setVisibility(View.VISIBLE);
        }
    }

    private void checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {


            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                CommonLib.ZLog("google-play-resultcode", resultCode);
                if (resultCode == 2 && !isFinishing()) {

                    if (windowHasFocus)
                        showDialog(PLAY_SERVICES_RESOLUTION_REQUEST);
                } else {
                    navigateToHome();
                }

            } else {
                navigateToHome();
            }

        } else {

            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

            regId = getRegistrationId(context);

            if (hardwareRegistered == 0) {
                // Call
                if (prefs.getInt("uid", 0) != 0 && !regId.equals("")) {
                    sendRegistrationIdToBackend();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("HARDWARE_REGISTERED", 1);
                    editor.commit();
                    hardwareRegistered=1;
                }
            }

            if (regId.isEmpty()) {
                CommonLib.ZLog("GCM", "RegID is empty");
                registerInBackground();
            } else {
                CommonLib.ZLog("GCM", "already registered : " + regId);
            }
            navigateToHome();
        }
    }

    public void startLocationCheck() {
        zapp.zll.forced = true;
        zapp.zll.addCallback(this);
        zapp.startLocationCheck();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {

        final SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        String registrationId = prefs.getString(CommonLib.PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            CommonLib.ZLog("GCM", "Registration not found.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);
            return packageInfo.versionCode;

        } catch (Exception e) {
            CommonLib.ZLog("GCM", "EXCEPTION OCCURED" + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }

                    regId = gcm.register(CommonLib.GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regId;
                    storeRegistrationId(context, regId);

                    if (prefs.getInt("uid", 0) != 0 && !regId.equals(""))
                        sendRegistrationIdToBackend();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                CommonLib.ZLog("GCM msg", msg);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void storeRegistrationId(Context context, String regId) {

        prefs = getSharedPreferences("application_settings", 0);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommonLib.PROPERTY_REG_ID, regId);
        editor.putInt(CommonLib.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {
        // new registerDeviceAtZomato().execute();
        UploadManager.updateRegistrationId(prefs.getString("access_token", ""), regId);
    }

    public void onCoordinatesIdentified(Location loc) {
        if(loc!=null){
            UploadManager.updateLocation(prefs.getString("access_token", ""),loc.getLatitude(), loc.getLongitude());

            float lat = (float) loc.getLatitude();
            float lon = (float)loc.getLongitude();
            Log.e("lat lon", lat + " " + lon);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("lat1", lat);
            editor.putFloat("lon1", lon);
            editor.commit();}
    }

    public void onLocationIdentified() {
    }

    public void onLocationNotIdentified() {
    }

    @Override
    public void onDifferentCityIdentified() {
    }

    @Override
    public void locationNotEnabled() {
    }

    @Override
    public void onLocationTimedOut() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNetworkError() {
    }

    private void fixSizes() {

        mViewPager.getLayoutParams().height = 2 * height / 3;
        ((RelativeLayout.LayoutParams)mViewPager.getLayoutParams()).setMargins(0, height / 5, 0, width / 20);
//        mSignupContainer.getLayoutParams().height = height / 3 - width / 10 - width / 40;
    }

    @Override
    public void response(Bundle bundle) {

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

                if (!error_exception.equals("") || !error_responseCode.equals("") || !error_stackTrace.equals(""))
                    ;// BTODO
                // sendFailedLogsToServer();

                if (bundle.getString("errorMessage") != null) {
                    String errorMessage = bundle.getString("errorMessage");
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.err_occurred, Toast.LENGTH_SHORT).show();
                }
                if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                    z_ProgressDialog.dismiss();
            } else {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("uid", bundle.getInt("uid"));
                if (bundle.containsKey("email"))
                    editor.putString("email", bundle.getString("email"));
                if (bundle.containsKey("description"))
                    editor.putString("description", bundle.getString("description"));
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
                editor.putString("access_token", bundle.getString("access_token"));
                editor.putBoolean("verifiedUser", bundle.getBoolean("verifiedUser"));
                editor.commit();

                CommonLib.ZLog("login", "FACEBOOK");

                checkPlayServices();

                if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                    z_ProgressDialog.dismiss();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void facebookAction(View view) {
        BTracker.logGAEvent(this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_FACEBOOK_LOGIN_PRESSED, "");
//		z_ProgressDialog = ProgressDialog.show(Splash.this, null, getResources().getString(R.string.verifying_creds),
//				true, false);
        z_ProgressDialog = new ProgressDialog(SplashScreen.this,R.style.StyledDialog);
        z_ProgressDialog.setMessage(getResources().getString(R.string.verifying_creds));
        z_ProgressDialog.setCancelable(false);
        z_ProgressDialog.setIndeterminate(true);
        z_ProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        z_ProgressDialog.setCancelable(false);
        z_ProgressDialog.show();
        String regId = prefs.getString("registration_id", "");
        FacebookConnect facebookConnect = new FacebookConnect(SplashScreen.this, 1, APPLICATION_ID, true, regId);
        facebookConnect.execute();
        checkPlayServices();

    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            super.onActivityResult(requestCode, resultCode, intent);
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, intent);

        } catch (Exception w) {

            w.printStackTrace();

            try {
                com.facebook.Session fbSession = com.facebook.Session.getActiveSession();
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
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
                               String stringId) {
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
                        editor.putString("access_token", responseJSON.getString("access_token"));
                    }
                    if (responseJSON.has("HSLogin") && responseJSON.get("HSLogin") instanceof Boolean) {
                        editor.putBoolean("HSLogin", responseJSON.getBoolean("HSLogin"));
                    }
                    if (responseJSON.has("INSTITUTION_NAME")) {
                        editor.putString("INSTITUTION_NAME", responseJSON.getString("INSTITUTION_NAME"));
                    }
                    if (responseJSON.has("STUDENT_ID")) {
                        editor.putString("STUDENT_ID", responseJSON.getString("STUDENT_ID"));
                    }
                    if (responseJSON.has("user_id") && responseJSON.get("user_id") instanceof Integer) {
                        editor.putInt("uid", responseJSON.getInt("user_id"));
                    }
                    editor.commit();
                    checkPlayServices();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
        if (requestType == CommonLib.SIGNUP) {
            if (destroyed)
                return;
        }
    }

    private class TourPagerAdapter extends PagerAdapter {

        View layout;

        protected View getView(){
            return layout;
        }
        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.splash_screen_pager_snippet, null);
            this.layout = layout;

            if (position == 0) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.VISIBLE);
                tour_text.setVisibility(View.VISIBLE);

                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.logo, width, width);
                    tour_logo.getLayoutParams().width = (int) ( 0.56 * width);
                    tour_logo.getLayoutParams().height = (int) (0.43 * width);
                    tour_logo.setImageBitmap(logoBitmap);

                    Bitmap splashTextBitmap = CommonLib.getBitmap(context, R.drawable.baatna_splash_text, width / 2, width / 10);
                    tour_text_logo.getLayoutParams().width = width / 2;
                    tour_text_logo.getLayoutParams().height = width / 10;
                    tour_text_logo.setImageBitmap(splashTextBitmap);

                    tour_text.setText(getResources().getString(R.string.splash_description_1));
                    int margin = (int) ((1.2 * 566 * width / 1266) - (0.43 * width));
                    if(margin > 0)
                        ((RelativeLayout.LayoutParams)tour_text.getLayoutParams()).setMargins(0, margin, 0, 0);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 0 )
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);

            } else if (position == 1) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.INVISIBLE);
                tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_2));
                tour_text_logo.getLayoutParams().width = width / 2;
                tour_text_logo.getLayoutParams().height = width / 10;
                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.tour_1, width, height);
                    tour_logo.getLayoutParams().width = 50 * width  / 89;//50
                    tour_logo.getLayoutParams().height = 39 * width / 89;//39, 89
                    tour_logo.setImageBitmap(logoBitmap);
                    int margin = (int) ((1.2 * 566 * width / 1266) - (39 * width / 89));
                    if(margin > 0)
                        ((RelativeLayout.LayoutParams)tour_text.getLayoutParams()).setMargins(0, margin, 0, 0);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 1 )
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);

            } else if (position == 2) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.INVISIBLE);
                tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_3));
                tour_text_logo.getLayoutParams().width = width / 2;
                tour_text_logo.getLayoutParams().height = width / 10;
                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.tour_2,  width, height);
                    tour_logo.getLayoutParams().width = (int) (1.2 * 700 * width / 1266);//700
                    tour_logo.getLayoutParams().height = (int) (1.2 * 566 * width / 1266);//566
                    tour_logo.setImageBitmap(logoBitmap);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 2 )
                    findViewById(R.id.signup_container).setVisibility(View.INVISIBLE);

            } else if (position == 3) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.INVISIBLE);
                  tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_4));
                tour_text_logo.getLayoutParams().width = width / 2;
                tour_text_logo.getLayoutParams().height = width / 10;
                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.tour_3,  width, height);
                    tour_logo.getLayoutParams().width = 600 * width / 1147;//600
                    tour_logo.getLayoutParams().height = 547 * width / 1147;//547
                    tour_logo.setImageBitmap(logoBitmap);
                    int margin = (int) ((1.2 * 566 * width / 1266) - (547 * width / 1147));
                    if(margin > 0)
                        ((RelativeLayout.LayoutParams)tour_text.getLayoutParams()).setMargins(0, margin, 0, 0);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }

                if( mViewPager.getCurrentItem() == 3 )
                    findViewById(R.id.signup_container).setVisibility(View.VISIBLE);

            } else if (position == 4) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.VISIBLE);
                tour_text.setVisibility(View.GONE);

                // setting image
                try {

                    Bitmap logoBitmap = CommonLib.getBitmap(context, R.drawable.logo, width / 2, width / 2);
                    tour_logo.getLayoutParams().width = (int) ( 0.56 * width);
                    tour_logo.getLayoutParams().height = (int) (0.43 * width);
                    tour_logo.setImageBitmap(logoBitmap);

                    Bitmap splashTextBitmap = CommonLib.getBitmap(context, R.drawable.baatna_splash_text, width / 2, width / 10);
                    tour_text_logo.getLayoutParams().width = width / 2;
                    tour_text_logo.getLayoutParams().height = width / 10;
                    tour_text_logo.setImageBitmap(splashTextBitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                if( mViewPager.getCurrentItem() == 4 )
                    findViewById(R.id.signup_container).setVisibility(View.VISIBLE);

            }
            collection.addView(layout, 0);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void finishUpdate(ViewGroup arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup arg0) {
        }

    }

    private void updateDotsContainer() {

        LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);
        dotsContainer.removeAllViews();

        int index = 5;

        for (int count = 0; count < index; count++) {
            ImageView dots = new ImageView(getApplicationContext());

            if (count == 0) {
                dots.setImageResource(R.drawable.tour_image_dots_selected);
                dots.setPadding(width / 80, 0, width / 80, 0);

            } else {
                dots.setImageResource(R.drawable.tour_image_dots_unselected);
                dots.setPadding(0, 0, width / 80, 0);
            }

            final int c = count;
            dots.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        ((ViewPager) mViewPager).setCurrentItem(c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            dotsContainer.addView(dots);
        }
    }

    public void navigateToHome() {
        if (prefs.getInt("uid", 0) != 0) {
            if (prefs.getBoolean("instutionLogin", true) && prefs.getBoolean("HSLogin", true)) {
                //did not login using hslogin before, navigate to user details page
                Intent intent = new Intent(this, UserLoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, Home.class);
                startActivity(intent);
                finish();
            }
        }
    }

    int seconds = 12;
    Timer timer;
    private int mCurrentItem = 0;

    private void startTimer() {
        if (context == null || destroyed)
            return;

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!destroyed) {

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!destroyed) {
                                seconds -= 1;
                                if (seconds <= 0) {
                                    seconds = 12;
                                    timer.cancel();
                                } else {
                                    mCurrentItem++;
                                    if(mCurrentItem < 5 && !hasSwipedPager)
                                        mViewPager.setCurrentItem(mCurrentItem);
                                }

                            }
                        }
                    });
                } else {
                    timer.cancel();
                }
            }
        }, 3000, 3000);
    }
}
