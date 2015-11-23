package com.application.baatna.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.Splash;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by apoorvarora on 16/11/15.
 */
public class UserLoginActivity extends Activity implements UploadManagerCallback {

    private SharedPreferences prefs;
    private int width;
    private boolean destroyed = false;
    private BaatnaApp zapp;
    private int userId;
    ImageView imageView;
    AsyncTask mAsyncRunning;

    User user;
    private ProgressDialog z_ProgressDialog;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.user_login_activity);

        UploadManager.addCallback(this);
        zapp = (BaatnaApp) getApplication();

        prefs = getSharedPreferences("application_settings", 0);
        width = getWindowManager().getDefaultDisplay().getWidth();
        userId = prefs.getInt("uid", 0);
        fixSizes();
        setListeners();

    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);
        mAsyncRunning = new GetUserDetails().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private class GetUserDetails extends AsyncTask<Object, Void, Object> {

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "user/details?user_id=" + userId;
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.USER_INFO, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;
            findViewById(R.id.userpage_progress_container).setVisibility(View.GONE);

            if (result != null) {
                if (result instanceof User) {
                    user = (User) result;
                    findViewById(R.id.content_container).setVisibility(View.VISIBLE);
                    setImageFromUrlOrDisk(user.getImageUrl(), imageView, "", width / 20, width / 20, false);
                    ((TextView) findViewById(R.id.name)).setText(user.getUserName());
                }
            } else {
                findViewById(R.id.empty_view).setVisibility(View.GONE);
                findViewById(R.id.content_container).setVisibility(View.GONE);
                if (CommonLib.isNetworkAvailable(UserLoginActivity.this)) {
                    Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserLoginActivity.this, getResources().getString(R.string.no_internet_message),
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void setListeners() {
        findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView();
            }
        });

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = ((TextView) findViewById(R.id.phone_number)).getText().toString();
                if(phoneNumber == null || phoneNumber.length() < 1) {
                    Toast.makeText(UserLoginActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    ((TextView) findViewById(R.id.phone_number)).requestFocus();
                    return;
                }
                z_ProgressDialog = ProgressDialog.show(UserLoginActivity.this, null, "Registering. Please wait...");
                UploadManager.updateInstitution(prefs.getString("access_token", ""), "-1", "", -1, "-1",
                        phoneNumber);
            }
        });

        findViewById(R.id.join_college).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLoginActivity.this, HSLoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        // disconnect facebook
        try {

            com.facebook.Session fbSession = com.facebook.Session.getActiveSession();
            if (fbSession != null) {
                fbSession.closeAndClearTokenInformation();
            }
            com.facebook.Session.setActiveSession(null);

        } catch (Exception e) {
        }

        String accessToken = prefs.getString("access_token", "");
        UploadManager.logout(accessToken);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("uid", 0);
        editor.putString("thumbUrl", "");
        editor.putString("access_token", "");
        editor.remove("username");
        editor.putBoolean("facebook_post_permission", false);
        editor.putBoolean("post_to_facebook_flag", false);
        editor.putBoolean("facebook_connect_flag", false);
        editor.putBoolean("twitter_status", false);

        editor.commit();

        if (prefs.getInt("uid", 0) == 0) {
            Intent intent = new Intent(zapp, Splash.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getInt("uid", 0) == 0) {
            Intent intent = new Intent(UserLoginActivity.this, Splash.class);
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

    ImageView imageViewBlur;

    private void fixSizes() {
        findViewById(R.id.name).setPadding(width / 20, width / 20, width / 20, width / 20);
        imageView = ((ImageView) findViewById(R.id.user_image));
        imageViewBlur = (ImageView) findViewById(R.id.drawer_user_info_background_image);

        ((RelativeLayout.LayoutParams) findViewById(R.id.back_icon).getLayoutParams()).setMargins(width / 20,
                width / 20, width / 20, width / 20);

        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageView, "", width, width, false);
        ((TextView) findViewById(R.id.name)).setText(prefs.getString("username", ""));
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
                                       int height, boolean useDiskCache) {

        if (cancelPotentialWork(url, imageView)) {

            GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), zapp.cache.get(url + type), task);
            imageView.setImageDrawable(asyncDrawable);
            if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
            }
            if (zapp.cache.get(url + type) == null) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1L);
            } else if (imageView != null && imageView.getDrawable() != null
                    && ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
                imageView.setBackgroundResource(0);
                Bitmap blurBitmap = null;
                if (imageViewBlur != null) {
                    blurBitmap = CommonLib.fastBlur(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 10);
                }
                if (imageViewBlur != null && blurBitmap != null) {
                    imageViewBlur.setImageBitmap(blurBitmap);
                }
                if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                }
            }
        }
    }

    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<GetImage> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<GetImage>(bitmapWorkerTask);
        }

        public GetImage getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public boolean cancelPotentialWork(String data, ImageView imageView) {
        final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.url;
            if (!bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    private GetImage getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class GetImage extends AsyncTask<Object, Void, Bitmap> {

        String url = "";
        private WeakReference<ImageView> imageViewReference;
        private int width;
        private int height;
        boolean useDiskCache;
        String type;
        Bitmap blurBitmap;

        public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type) {
            this.url = url;
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
            this.useDiskCache = true;// useDiskCache;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar)
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            try {

                String url2 = url + type;

                if (destroyed && (imageViewReference.get() != findViewById(R.id.user_image))) {
                    return null;
                }

                if (useDiskCache) {
                    bitmap = CommonLib.getBitmapFromDisk(url2, getApplicationContext());
                }

                if (bitmap == null) {
                    try {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
                        opts.inJustDecodeBounds = false;

                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        if (useDiskCache) {
                            CommonLib.writeBitmapToDisk(url2, bitmap, getApplicationContext(),
                                    Bitmap.CompressFormat.JPEG);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error e) {

                    }
                }

                if (bitmap != null) {

                    bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);
                    synchronized (zapp.cache) {
                        zapp.cache.put(url2, bitmap);
                    }
                    if (imageViewBlur != null) {
                        blurBitmap = CommonLib.fastBlur(bitmap, 4);
                    }
                }

            } catch (Exception e) {
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (!destroyed) {
                if (isCancelled()) {
                    bitmap = null;
                }
                if (imageViewReference != null && bitmap != null) {
                    final ImageView imageView = imageViewReference.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                        if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                            ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                        }
                    }
                }
                if (imageViewBlur != null && blurBitmap != null) {
                    imageViewBlur.setImageBitmap(blurBitmap);
                }
            }
        }
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
                               String stringId) {
        if (destroyed)
            return;
        if (requestType == CommonLib.WISH_UPDATE_STATUS) {
            if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                z_ProgressDialog.dismiss();

            if (destroyed || !status)
                return;

            if (objectId != 1) // accept
                return;

            final User user = (User) ((Object[]) data)[0];
            final Wish wish = (Wish) ((Object[]) data)[1];

            final AlertDialog messageDialog;
            messageDialog = new AlertDialog.Builder(this)
                    .setMessage(
                            getResources().getString(R.string.thanks_wish_tick, user.getUserName(), wish.getTitle()))
                    .setPositiveButton(getResources().getString(R.string.message),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // navigate to message with this user, how?
                                    Intent intent = new Intent(UserLoginActivity.this, MessagesActivity.class);
                                    intent.putExtra("user", user);
                                    intent.putExtra("wish", wish);
                                    intent.putExtra("type", CommonLib.WISH_ACCEPTED_CURRENT_USER);
                                    startActivity(intent);
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.later), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            messageDialog.show();
        } else if (requestType == CommonLib.UPDATE_INSTITUTION) {
            if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                z_ProgressDialog.dismiss();
            if (status && !destroyed) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("HSLogin", false);
                editor.putBoolean("instutionLogin", false);
                editor.commit();
                navigateToHome();
            }
        }

    }

    public void navigateToHome() {
        if (prefs.getInt("uid", 0) != 0) {
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
    }

}

