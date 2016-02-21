package com.application.baatna.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.SplashScreen;
import com.application.baatna.data.FeedItem;
import com.application.baatna.data.User;
import com.application.baatna.data.UserComactMessage;
import com.application.baatna.data.Wish;
import com.application.baatna.utils.BTracker;
import com.application.baatna.utils.BaatnaLocationCallback;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.CustomTypefaceSpan;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;
import com.application.baatna.utils.fab.FABControl.OnFloatingActionsMenuUpdateListener;
import com.facebook.Session;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.plus.PlusOneButton;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Home extends AppCompatActivity
		implements OnFloatingActionsMenuUpdateListener, UploadManagerCallback, BaatnaLocationCallback {

	private BaatnaApp zapp;
	private SharedPreferences prefs;
	private int width;
	public DrawerLayout mDrawerLayout;
	LayoutInflater inflater;
	// rate us on the play store
	boolean rateDialogShow = false;
	private boolean destroyed = false;

	private ListView feedListView;
	private NewsFeedAdapter feedListAdapter;
	private List<FeedItem> feedItems;

	private Activity mContext;

	// FAB Stuff
	private View mFABOverlay;
	private boolean mFABExpanded = false;
	private boolean mFABVisible = false;

	private GetNewsFeedItems mAsyncTaskRunning;
	View headerView;

	private int mScrollState;
	ArrayList<GetImage> getImageArray = new ArrayList<GetImage>();
	private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

	ArrayList<FeedItem> wishes;
	LinearLayout mListViewFooter;
	private int mWishesTotalCount;
	private boolean cancelled = false;
	private boolean loading = false;
	private int count = 10;
	private ProgressDialog z_ProgressDialog;

	// rate us on the play store
	private PlusOneButton mPlusOneButton;

	private SwipeRefreshLayout swipeRefreshLayout;
	Location loc;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.home_activity);

		getWindow().setBackgroundDrawable(null);
		mContext = this;
		inflater = LayoutInflater.from(this);
		prefs = getSharedPreferences(CommonLib.APP_SETTINGS, 0);
		zapp = (BaatnaApp) getApplication();

		width = getWindowManager().getDefaultDisplay().getWidth();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);

		setupActionBar();
		setUpDrawer();
		// initialize list view
		setUpFAB();
		LocationCheck(mContext);

		headerView = View.inflate(this, R.layout.map_header_view, null);

		feedListView = (ListView) findViewById(R.id.feedListView);
		feedListView.addHeaderView(headerView);
		feedListView.setDivider(new ColorDrawable(getResources().getColor(R.color.feed_bg)));
		feedListView.setDividerHeight(width / 40);

		((RelativeLayout.LayoutParams) headerView.findViewById(R.id.request_icon).getLayoutParams())
				.setMargins(width / 20 + width / 80 + width / 100, 0, 0, 0);
		headerView.findViewById(R.id.make_request_container).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_MAKE_NEW_REQUEST_PRESSED, "");
				Intent intent = new Intent(Home.this, NewRequestActivity.class);
				startActivity(intent);

			}
		});

		findViewById(R.id.feedListView).setVisibility(View.GONE);
		refreshFeed();

		feedListView.setOnScrollListener(new OnScrollListener() {


			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mScrollState = scrollState;

				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

					worker.schedule(new Runnable() {
						@Override
						public void run() {
							if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
											ArrayList<GetImage> arr2 = new ArrayList<GetImage>();

											boolean urlConflict = false;

											for (GetImage task : getImageArray) {
												if (!task.url.equals("")) {

													if (task.imageViewReference != null
															&& task.imageViewReference.get() != null) {
														GetImage getImg = getBitmapWorkerTask(
																task.imageViewReference.get());
														if (getImg != null && getImg.url2.equals(task.url)) {

															GetImage gi = new GetImage(task.url,
																	task.imageViewReference.get(), task.width,
																	task.height, task.useDiskCache, task.type, false);
															// gi.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
															try {
																gi.executeOnExecutor(
																		CommonLib.THREAD_POOL_EXECUTOR_IMAGE);
															} catch (RejectedExecutionException e) {
																CommonLib.sPoolWorkQueueImage.clear();
															}
															arr2.add(0, gi);

														} else {
															urlConflict = true;
															// break;
														}
													}
												} else if (!task.url2.equals("")) {

													if (task.imageViewReference != null
															&& task.imageViewReference.get() != null) {
														GetImage getImg = getBitmapWorkerTask(
																task.imageViewReference.get());
														if (getImg != null && getImg.url2.equals(task.url2)) {

															GetImage gi = new GetImage(task.url2,
																	task.imageViewReference.get(), task.width,
																	task.height, task.useDiskCache, task.type, false);
															// gi.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
															try {
																gi.executeOnExecutor(
																		CommonLib.THREAD_POOL_EXECUTOR_IMAGE);
															} catch (RejectedExecutionException e) {
																CommonLib.sPoolWorkQueueImage.clear();
															}
															arr2.add(0, gi);

														} else {
															urlConflict = true;
															// break;
														}
													}
												}
											}

											getImageArray.clear();

											if (urlConflict) {
												feedListAdapter.notifyDataSetChanged();
											} else {
												getImageArray.addAll(arr2);
											}

										}
									}
								});
							}
						}
					}, 50, TimeUnit.MILLISECONDS);
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
		rateDialogShow = prefs.getBoolean("rate_dialog_show", true);
		if (rateDialogShow) {
			int rateDialogCounter = prefs.getInt("rate_dialog", 1);
			int rateDialogCounterTrigger = prefs.getInt("rate_dialog_trigger", 3);

			if (rateDialogCounter == rateDialogCounterTrigger) {
				showRateUsDialog();
				CommonLib.ZLog("rate dialog", "show dialog rateDialogCounterTrigger = " + rateDialogCounterTrigger);
			}

			rateDialogCounter++;
			Editor edit = prefs.edit();
			edit.putInt("rate_dialog", rateDialogCounter);
			edit.commit();

			CommonLib.ZLog("rate dialog", "rate_dialog is now  " + rateDialogCounter);
			CommonLib.ZLog("rate dialog", "rate_dialog_trigger is now " + rateDialogCounterTrigger);
		}
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				CommonLib.ZLog("onRefresh", "called");
				refreshFeed();
			}

		});
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);


		((TextView) findViewById(R.id.txt_new_feeds)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				feedListAdapter.notifyDataSetChanged();
				feedListView.smoothScrollToPosition(0);

				((TextView) findViewById(R.id.txt_new_feeds)).setVisibility(View.GONE);

			}
		});
		UploadManager.addCallback(this);

		zapp.lat=prefs.getFloat("lat1",0);
		zapp.lon=prefs.getFloat("lon1",0);
		Editor editor = prefs.edit();
		editor.putFloat("lat", (float)zapp.lat);
		editor.putFloat("lon",(float)zapp.lon);
		Log.e("zapp", zapp.lat + " " + zapp.lon);
		displayAddressMap((ImageView) headerView.findViewById(R.id.search_map), zapp.lat, zapp.lon);
		new AppConfigandRating().execute(null, null, null);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mFeedReceived,
				new IntentFilter(CommonLib.LOCAL_FEED_BROADCAST_NOTIFICATION));
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

		new CheckToken().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}



	private BroadcastReceiver mFeedReceived = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				if (intent != null && intent.getExtras() != null && intent.hasExtra("feed")) {
					FeedItem feedItem = (FeedItem) intent.getSerializableExtra("feed");
					wishes.add(0, feedItem);
					if (feedListView != null && feedListView.getFirstVisiblePosition() >= 2)
						((TextView) findViewById(R.id.txt_new_feeds)).setVisibility(View.VISIBLE);
					else if (feedListView != null && feedListView.getFirstVisiblePosition() < 2)
						feedListAdapter.notifyDataSetChanged();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void setUpFAB() {

		mFABOverlay = findViewById(R.id.fab_overlay);
		mFABOverlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// toggleFAB();
			}
		});
		mFABOverlay.setClickable(false);
	}

	private void toggleFAB() {
		// ((FABControl) findViewById(R.id.multiple_actions)).toggle();
	}

	// makes FAB visible.
	public void showFAB(boolean delayed) {


		if (!mFABVisible) {
			mFABVisible = true;

			if (delayed) {
				ViewPropertyAnimator animator = findViewById(R.id.fab_post_request).animate().scaleX(1).scaleY(1)
						.setDuration(250).setInterpolator(new AccelerateInterpolator()).setStartDelay(200);

				findViewById(R.id.fab_post_request).setVisibility(View.VISIBLE);
				animator.setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						try {
							// showCashlessInFab();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}
				});

			} else {

				ViewPropertyAnimator animator = findViewById(R.id.fab_post_request).animate().scaleX(1).scaleY(1)
						.setDuration(200).setInterpolator(new AccelerateInterpolator());
				// required | dont remove
				animator.setListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {

					}

					@Override
					public void onAnimationRepeat(Animator animation) {

					}

					@Override
					public void onAnimationEnd(Animator animation) {
						try {
							// showCashlessInFab();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {

					}
				});
			}
		}
	}

	public void logoutConfirm(View V) {
		BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_DRAWER_SIGN_OUT_PRESSED, "");
		final AlertDialog logoutDialog;
		logoutDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.logout))
				.setMessage(getResources().getString(R.string.logout_confirm))
				.setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						// disconnect facebook
						try {

							Session fbSession = Session.getActiveSession();
							if (fbSession != null) {
								fbSession.closeAndClearTokenInformation();
							}
							Session.setActiveSession(null);

						} catch (Exception e) {
						}

						String accessToken = prefs.getString("access_token", "");
						UploadManager.logout(accessToken);

						Editor editor = prefs.edit();
						editor.putInt("uid", 0);
						editor.putString("thumbUrl", "");
						editor.putString("access_token", "");
						editor.remove("username");
						editor.remove("profile_pic");
						editor.remove("HSLogin");
						editor.remove("INSTITUTION_NAME");
						editor.remove("STUDENT_ID");
						editor.putBoolean("facebook_post_permission", false);
						editor.putBoolean("post_to_facebook_flag", false);
						editor.putBoolean("facebook_connect_flag", false);
						editor.putBoolean("twitter_status", false);

						editor.commit();
						//To stop getting chat notification after uninstall
						try {
							GoogleCloudMessaging.getInstance(getApplicationContext()).unregister();
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (prefs.getInt("uid", 0) == 0) {
							Intent intent = new Intent(zapp, SplashScreen.class);
							startActivity(intent);
							finish();
						}
					}
				}).setNegativeButton(getResources().getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
				.create();
		logoutDialog.show();
	}

	// makes FAB gone.
	public void hideFAB() {

		if (mFABVisible) {
			mFABVisible = false;

			ViewPropertyAnimator animator = findViewById(R.id.fab_post_request).animate().scaleX(0).scaleY(0)
					.setDuration(50).setStartDelay(0).setInterpolator(new AccelerateInterpolator());
			animator.setListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					findViewById(R.id.fab_post_request).setVisibility(View.GONE);
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
		}
	}

	// Controls visibility/scale of FAB on drawer open/close
	private void scaleFAB(float input) {
		if (input < .7f) {

			if (findViewById(R.id.fab_post_request).getVisibility() != View.VISIBLE)
				findViewById(R.id.fab_post_request).setVisibility(View.VISIBLE);

			findViewById(R.id.fab_post_request).setScaleX(1 - input);
			findViewById(R.id.fab_post_request).setScaleY(1 - input);

		} else {

			if (findViewById(R.id.fab_post_request).getScaleX() != 0)
				findViewById(R.id.fab_post_request).setScaleX(0);

			if (findViewById(R.id.fab_post_request).getScaleY() != 0)
				findViewById(R.id.fab_post_request).setScaleY(0);

			if (findViewById(R.id.fab_post_request).getVisibility() != View.GONE)
				findViewById(R.id.fab_post_request).setVisibility(View.GONE);
		}

	}

	private void setupActionBar() {

		ActionBar actionBar = getSupportActionBar();
		if(Build.VERSION.SDK_INT > 20)
			actionBar.setElevation(0);

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		View v = inflater.inflate(R.layout.baatna_action_bar, null);

		v.findViewById(R.id.action_buttons).setVisibility(View.VISIBLE);
		v.findViewById(R.id.home_icon_zomato).setVisibility(View.VISIBLE);

		v.findViewById(R.id.open_messages).setPadding(width / 20, width / 40, width / 20, width / 40);

		v.findViewById(R.id.open_messages).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_MESSAGES_ICON_PRESSED, "");
				Intent intent = new Intent(Home.this, FriendListActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			}
		});

		v.findViewById(R.id.home_icon_container).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openOrCloseDrawer();
			}
		});

		try {
			v.findViewById(R.id.home_icon_zomato).setPadding(width / 80, width / 80, width / 80, width / 80);
		} catch (Exception e) {

		}

		// user handle
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setPadding(width / 80, 0, width / 40, 0);
		actionBar.setCustomView(v);

		// setImageOnActionBar();
	}

	public void openOrCloseDrawer() {

		if (!mFABExpanded) {
			if (mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
				InputMethodManager mgr = (InputMethodManager) getApplicationContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));

			} else {
				mDrawerLayout.openDrawer(findViewById(R.id.left_drawer));
			}
		}
	}

	public void actionBarSelected(View view) {
		switch (view.getId()) {
			case R.id.fab_post_request:
				if (view.getAlpha() == 1) {
					if(Build.VERSION.SDK_INT >= 21) {

						// previously invisible view
						View view1 = findViewById(R.id.drawer_layout);

// get the center for the clipping circle
						int centerX = view1.getRight();
						int centerY = view1.getBottom();

						int startRadius = 0;
// get the final radius for the clipping circle
						int endRadius = Math.max(view1.getWidth(), view1.getHeight());

// create the animator for this view (the start radius is zero)
						Animator anim =
								ViewAnimationUtils.createCircularReveal(view1, centerX, centerY, startRadius, endRadius);

// make the view visible and start the animation

						anim.start();
						BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_FAB_ICON_PRESSED, "");
						Intent intent = new Intent(Home.this, NewRequestActivity.class);
						startActivityForResult(intent, CommonLib.NEW_REQUEST);
						overridePendingTransition(0, 0);
					} else  {
						BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_FAB_ICON_PRESSED, "");
						Intent intent = new Intent(Home.this, NewRequestActivity.class);
						startActivity(intent);
					}
					break;
				}
		}
	}

	private void setUpDrawer() {

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		final View drawerIcon = findViewById(R.id.ic_drawer);
		drawerIcon.measure(0, 0);
		drawerIcon.setPivotX(0);
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {
			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				drawerIcon.setScaleX((1 - arg1 / 2));
				scaleFAB(arg1);
			}

			@Override
			public void onDrawerOpened(View arg0) {
			}

			@Override
			public void onDrawerClosed(View arg0) {
			}
		});

		setUpUserSettingsInDrawer();
	}

	@Override
	public void onBackPressed() {
		if (mFABExpanded) {
			toggleFAB();
			return;
		}

		if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
			mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
			return;
		}

		super.onBackPressed();
	}

	@Override
	public void onResume() {
		getSupportActionBar().show();
		if(CommonLib.isAndroidL()) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(getResources().getColor(R.color.action_bar_default));
		}
		try {
			super.onResume();

			if (mFABExpanded)
				toggleFAB();

		} catch (Exception e) {
		}

	}

	private void setUpUserSettingsInDrawer() {

		findViewById(R.id.drawer_user_stat_cont).setPadding(3 * width / 80, 0, 0, 0);
		findViewById(R.id.drawer_user_gradient_bottom).getLayoutParams().height = (12 * width / 90);

		// user click
		findViewById(R.id.drawer_user_container).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_OWN_PROFILE_PRESSED, "");
				Intent intent = new Intent(Home.this, UserPageActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.rotation, R.anim.rotation1);
			}
		});

		setImageInDrawer();
	}

	// called from
	// 1. Home when drawer is being set.
	// 2. Me fragment after EDIT PROFILE.
	public void setImageInDrawer() {
		// Blurred user image
		ImageView imageBackground = (ImageView) findViewById(R.id.drawer_user_info_background_image);
		setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageBackground, "profile_pic", width, width, false, false);

		ImageView imageBlur = (ImageView) findViewById(R.id.drawer_user_info_blur_background_image);
		setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageBlur, "blur", width / 20, width / 20, false, true);
	}

	public void rate(View v) {

		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=com.application.baatna"));
			startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {

		} catch (Exception e) {

		}
	}

	public void inviteFriends(View v) {
		String userId = "";
		String shortUrl = "https://play.google.com/store/apps/details?id=com.application.baatna";
		String shareText = getResources().getString(R.string.share_description) + shortUrl;

		List<Intent> targetedShareIntents = new ArrayList<Intent>();
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);
		if (!resInfo.isEmpty()) {
			for (ResolveInfo resolveInfo : resInfo) {
				String packageName = resolveInfo.activityInfo.packageName;
				Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
				targetedShareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
				if (TextUtils.equals(packageName, "com.facebook.katana")) {
					String shareFacebookText = "<p>" + shareText + "</p>";
					targetedShareIntent.setType("text/plain");
					targetedShareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareFacebookText));
					targetedShareIntent.putExtra(Intent.EXTRA_TEXT,
							"https://play.google.com/store/apps/details?id=com.application.baatna");
				} else {
					targetedShareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
				}
				if (packageName.equalsIgnoreCase("com.whatsapp") || packageName.equalsIgnoreCase("com.facebook.katana")
						|| packageName.equalsIgnoreCase("com.facebook.orca")
						|| packageName.equalsIgnoreCase("com.twitter.android")
						|| packageName.equalsIgnoreCase("com.tencent.mm")
						|| packageName.equalsIgnoreCase("com.viber.voip")
						|| packageName.equalsIgnoreCase("com.skype.raider") ||
						packageName.equalsIgnoreCase("com.google.android.apps.messaging")) {
					targetedShareIntent.setPackage(packageName);
					targetedShareIntents.add(targetedShareIntent);
				}
			}
			if (!targetedShareIntents.isEmpty()) //app crash if no targeted app found
			{
				Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Invite using");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));

				startActivity(chooserIntent);
			}
		}


	}

	public void openWishbox(View v) {
		BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_DRAWER_WISH_HISTORY_PRESSED, "");
		Intent intent = new Intent(this, WishHistoryActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		mDrawerLayout = null;
		zapp.zll.removeCallback(this);
		zapp.cache.clear();
		UploadManager.removeCallback(this);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mFeedReceived);
		super.onDestroy();
	}

	public void LocationCheck(Context context)
	{
		GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
				.addApi(LocationServices.API).build();
		googleApiClient.connect();

		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(10000);
		locationRequest.setFastestInterval(10000 / 2);

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
		builder.setAlwaysShow(true);

		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
		result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
			@Override
			public void onResult(LocationSettingsResult result) {
				final Status status = result.getStatus();
				switch (status.getStatusCode()) {
					case LocationSettingsStatusCodes.SUCCESS:
						Log.i("a", "All location settings are satisfied.");
						break;
					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						Log.i("b", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

						try {
							// Show the dialog by calling startResolutionForResult(), and check the result
							// in onActivityResult().
							status.startResolutionForResult(Home.this, 1000);
						} catch (IntentSender.SendIntentException e) {
							Log.i("c", "PendingIntent unable to execute request.");
						}
						break;
					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						Log.i("d", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
						break;
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
			case 1000:
				switch (resultCode) {
					case Activity.RESULT_OK:
						zapp.zll.forced = true;
						zapp.startLocationCheck();
						break;
					case Activity.RESULT_CANCELED:
//						LocationCheck(mContext);
//						Toast.makeText(mContext, "Please enable location services",
//								Toast.LENGTH_LONG).show();//keep asking if imp or do whatever
						break;
				}
				break;
		}
	}

	public void onCoordinatesIdentified(Location loc) {
		if(loc!=null) {
			UploadManager.updateLocation(prefs.getString("access_token", ""),loc.getLatitude(), loc.getLongitude());
			float lat = (float) loc.getLatitude();
			float lon = (float)loc.getLongitude();
			Log.e("latinhome loninhome",lat+" "+lon);
			Editor editor = prefs.edit();
			editor.putFloat("lat", lat);
			editor.putFloat("lon", lon);
			editor.commit();
			displayAddressMap((ImageView) headerView.findViewById(R.id.search_map), loc.getLatitude(), loc.getLongitude());
		}
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

	public void aboutus(View view) {
		BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_DRAWER_ABOUT_PRESSED, "");
		Intent intent = new Intent(this, AboutUs.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public void feedback(View v) {
		BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_DRAWER_FEEDBACK_PRESSED, "");
		startActivity(new Intent(this, FeedbackPage.class));
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	public void redeem(View v) {
		startActivity(new Intent(this, RedeemPage.class));
	}

	@Override
	public void onMenuExpanded() {
		mFABExpanded = true;
		mFABOverlay.animate().alpha(1).setDuration(100);
		mFABOverlay.setClickable(true);
	}

	// CALLBACK from FABControl
	@Override
	public void onMenuCollapsed() {
		mFABExpanded = false;
		mFABOverlay.animate().alpha(0).setDuration(100);
		mFABOverlay.setClickable(false);
	}

	private double lat;
	private double lon;

	public class NewsFeedAdapter extends ArrayAdapter<FeedItem> {

		private List<FeedItem> feedItems;
		private Activity mContext;
		private int width;
		private int descriptionPos = -1;

		public NewsFeedAdapter(Activity context, int resourceId, List<FeedItem> feedItems) {
			super(context.getApplicationContext(), resourceId, feedItems);
			mContext = context;
			this.feedItems = feedItems;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (feedItems == null) {
				return 0;
			} else {
				return feedItems.size();
			}
		}

		protected class ViewHolder {
			TextView userName;
			TextView time;
			TextView distance;
			View bar;
			TextView accept;
			TextView decline;
			LinearLayout action_container;
			ImageView imageView;
			RelativeLayout feed_item_container;
			RelativeLayout feed_item;
			TextView descriptiontextview;
			// RoundedImageView userImage;
		}
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		@Override
		public int getItemViewType(int position) {
			return (feedItems.get(position).getType() == CommonLib.FEED_TYPE_NEW_REQUEST) ? 0 : 1;
		}

		@Override
		public View getView(final int position, View v, ViewGroup parent) {
			final FeedItem feedItem = feedItems.get(position);
			int type = getItemViewType(position);
			if(v==null)
			{
				if (type==0)
					v = LayoutInflater.from(mContext).inflate(R.layout.feed_list_item_snippet, null);


				else
					v = LayoutInflater.from(mContext).inflate(R.layout.feed_list_item_snippet1, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.userName = (TextView) v.findViewById(R.id.user_name);
				viewHolder.feed_item_container=(RelativeLayout)v.findViewById(R.id.feed_item_container);
				viewHolder.feed_item=(RelativeLayout)v.findViewById(R.id.feed_item);
				// viewHolder.userImage = (RoundedImageView) v
				// .findViewById(R.id.user_image);
				viewHolder.time = (TextView) v.findViewById(R.id.time);
				viewHolder.distance = (TextView) v.findViewById(R.id.distance);
				viewHolder.bar = v.findViewById(R.id.left_bar);
				viewHolder.accept = (TextView) v.findViewById(R.id.accept_button);
				viewHolder.decline = (TextView) v.findViewById(R.id.decline_button);
				viewHolder.action_container = (LinearLayout) v.findViewById(R.id.action_container);
				viewHolder.imageView = (ImageView) v.findViewById(R.id.user_image);
				if(type==0)
					viewHolder.descriptiontextview=(TextView)v.findViewById(R.id.description_text_view);
				v.setTag(viewHolder);
			}
			if (position == 0) {
				if (((TextView) mContext.findViewById(R.id.txt_new_feeds)).getVisibility() == View.VISIBLE)
					((TextView) mContext.findViewById(R.id.txt_new_feeds)).setVisibility(View.GONE);
			}
			((RelativeLayout.LayoutParams) v.findViewById(R.id.feed_item_container).getLayoutParams())
					.setMargins(width / 40, 0, width / 40, 0);

			viewHolder.accept.getLayoutParams().width = getResources().getDimensionPixelOffset(R.dimen.height125) / 2;
			viewHolder.decline.getLayoutParams().width = getResources().getDimensionPixelOffset(R.dimen.height125) / 2;

			final User user = feedItem.getUserIdFirst();


			List<User> users = feedItem.getUsers();

			final Wish wish = feedItem.getWish();

			viewHolder.time.setText(CommonLib.findDateDifference(feedItem.getTimestamp()));
			if(type==0)
				viewHolder.descriptiontextview.setVisibility(View.GONE);
			if(feedItems.get(position).getType()==CommonLib.FEED_TYPE_NEW_USER) {
				v.findViewById(R.id.feed_item).setOnClickListener(null);
			}
			viewHolder.imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_USER_PROFILE_PRESSED, "");
					Intent intent = new Intent(Home.this, UserPageActivity.class);
					if (user != null && user.getUserId() != prefs.getInt("uid", 0))
						intent.putExtra("uid", user.getUserId());
					startActivity(intent);
					overridePendingTransition(R.anim.rotation, R.anim.rotation1);
				}
			});


			float distance = CommonLib.distFrom(prefs.getFloat("lat", 0), prefs.getFloat("lon", 0),
					feedItem.getLongitude(), feedItem.getLatitude());

			final TextView descriptionTextView = viewHolder.descriptiontextview;

			//distance in kmi
			if(distance>1)
			{distance = (distance / 1000);
				viewHolder.distance.setText((int)distance + "KM");	}
			//if(distance < 5)
			else if(distance==0)
				viewHolder.distance.setText("NEAR YOU");
			else
				viewHolder.distance.setText((int)distance + "M");
			//viewHolder.distance.setVisibility(View.GONE);


			int value = getResources().getDimensionPixelSize(R.dimen.size60);
			switch (feedItem.getType()) {

				case CommonLib.FEED_TYPE_NEW_USER:
					if (user != null) {
						String description = getResources().getString(R.string.feed_user_joined, user.getUserName() + " ");

						setImageFromUrlOrDisk(user.getImageUrl(), viewHolder.imageView, "user", value, value, false, false);
						Spannable desc = new SpannableString(description);
						Pattern p = Pattern.compile(user.getUserName(), Pattern.CASE_INSENSITIVE);
						Matcher m = p.matcher(description);
						while (m.find()) {
							desc.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						}

						viewHolder.userName.setText(desc);
//

//						viewHolder.accept.setVisibility(View.GONE);
//						viewHolder.decline.setVisibility(View.G);

						viewHolder.bar
								.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.feed_joined)));
					}
					break;

				case CommonLib.FEED_TYPE_NEW_REQUEST:

					//viewHolder.feed_item.setLayoutParams(new LinearLayout.LayoutParams(width/80, getResources().getDimensionPixelOffset(R.dimen.height125)));
					//v.getLayoutParams().height= R.dimen.height125;
					//v.setLayoutParams(new LinearLayout.LayoutParams(width,getResources().getDimensionPixelOffset(R.dimen.height125)));
					//viewHolder.feed_item_container.setLayoutParams(new RelativeLayout.LayoutParams(width/80, getResources().getDimensionPixelOffset(R.dimen.height125)));

					if (user != null && wish != null) {
						String description = "";
						try {
							description = getResources().getString(R.string.feed_user_requested,
									user.getUserName() + " ", wish.getTitle().toUpperCase() + " ", wish.getRequiredFor());
						} catch (Exception e) {
							e.printStackTrace();
						}

						setImageFromUrlOrDisk(user.getImageUrl(), viewHolder.imageView, "user", value, value, false, false);
						Spannable desc = new SpannableString(description);
						Pattern p = Pattern.compile(user.getUserName(), Pattern.CASE_INSENSITIVE);
						Matcher m = p.matcher(description);
						while (m.find()) {
							desc.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						}
						p = Pattern.compile(wish.getTitle(), Pattern.CASE_INSENSITIVE);
						m = p.matcher(description);
						Typeface font=CommonLib.getTypeface(getContext(), CommonLib.Bold);
						while (m.find()) {
							desc.setSpan (new CustomTypefaceSpan(font), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
							desc.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green_gradient)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

						}
						if(feedItems.get(position).getType()==CommonLib.FEED_TYPE_NEW_REQUEST)
							v.findViewById(R.id.feed_item).setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									if (((LinearLayout) v.getParent()).getChildAt(1).getVisibility()==View.GONE) {
										if (feedItem != null && feedItem.getWish() != null
												&& feedItem.getWish().getDescription() != null) {

											String description = feedItem.getWish().getDescription();


											LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
													LinearLayout.LayoutParams.WRAP_CONTENT);



											params.setMargins(width / 40, 0, width / 40, 0);
											descriptionTextView.setVisibility(View.VISIBLE);
											descriptionTextView.setText(description);
											descriptionTextView.setTypeface(CommonLib.getTypeface(getContext(), CommonLib.Regular));
											descriptionTextView.setPadding(width / 20, width / 20, width / 20, width / 20);
											descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.height14));
											descriptionTextView.setLayoutParams(params);

											descriptionTextView.setBackgroundColor(getResources().getColor(R.color.white));
											//((LinearLayout) v.getParent()).addView(descriptionTextView);


											descriptionPos = position;
										}
									} else {
										if (((LinearLayout) v.getParent()).getChildAt(1).getVisibility() == View.VISIBLE
												&& ((LinearLayout) v.getParent()).getChildAt(1) instanceof TextView)
											((LinearLayout) v.getParent()).getChildAt(1).setVisibility(View.GONE);
									}

								}
							});

						viewHolder.userName.setText(desc);


						viewHolder.bar
								.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green_gradient)));

						viewHolder.accept.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								z_ProgressDialog = ProgressDialog.show(Home.this, null,
										getResources().getString(R.string.sending_request), true, false);
								z_ProgressDialog.setCancelable(false);
								UploadManager.updateRequestStatus(prefs.getString("access_token", ""),
										"" + wish.getWishId(), "1", new Object[]{user, wish});
								feedListAdapter.remove(feedItem);
								feedListAdapter.notifyDataSetChanged();
							}
						});

						viewHolder.decline.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								UploadManager.updateRequestStatus(prefs.getString("access_token", ""),
										"" + wish.getWishId(), "2", new Object[]{user, wish});
								feedListAdapter.remove(feedItem);
								feedListAdapter.notifyDataSetChanged();
							}
						});


						viewHolder.accept.setVisibility(View.VISIBLE);
						viewHolder.decline.setVisibility(View.VISIBLE);

						//v.setMinimumHeight(getResources().getDimensionPixelOffset(R.dimen.height125));
						//viewHolder.feed_item.setMinimumHeight(getResources().getDimensionPixelOffset(R.dimen.height125));
						//viewHolder.feed_item_container.setMinimumHeight(getResources().getDimensionPixelOffset(R.dimen.height125));

					}
					break;

				case CommonLib.FEED_TYPE_REQUEST_FULFILLED:
					StringBuilder builder = new StringBuilder();
					builder.append(getResources().getString(R.string.feed_requested_fulfilled,
							user.getUserName() + " ", wish.getTitle().toUpperCase() + " ", users.get(0).getUserName()));
					if(users.size() > 2)
						builder.append(" "+getResources().getString(R.string.and_more, users.size() - 2));
					String description = builder.toString();
					setImageFromUrlOrDisk(user.getImageUrl(), viewHolder.imageView, "user", value, value, false, false);
					Spannable desc = new SpannableString(description);
					Pattern p = Pattern.compile(user.getUserName(), Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(description);
					while (m.find()) {
						desc.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					}
					p = Pattern.compile(wish.getTitle(), Pattern.CASE_INSENSITIVE);
					m = p.matcher(description);
					Typeface font=CommonLib.getTypeface(getContext(), CommonLib.Bold);
					while (m.find()) {

						desc.setSpan (new CustomTypefaceSpan(font), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
						desc.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.bt_orange_2)), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

					}
					if(feedItems.get(position).getType()==CommonLib.FEED_TYPE_REQUEST_FULFILLED)
						v.findViewById(R.id.feed_item).setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								if (((LinearLayout) v.getParent()).getChildAt(1) == null) {
									if (feedItem != null && feedItem.getWish() != null
											&& feedItem.getWish().getDescription() != null) {



										LinearLayout descriptionimageView = new LinearLayout(Home.this);
										LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
												getResources().getDimensionPixelOffset(R.dimen.height75));


										//coversion of px to dp
							/*int padding_in_dp = 10;
							final float scale = getResources().getDisplayMetrics().density;
							int padding_in_px = (int) (padding_in_dp * scale + 0.5f);*/


										params.setMargins(width / 40, 0, width / 40, 0);
										descriptionimageView.setLayoutParams(params);


										//ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(descriptionTextView.getLayoutParams());
										//marginParams.setMargins(50, 50, 50, 50);


										//descriptionTextView.setTextSize(pxFromDp(14, Home.this));

							/*int fontsize = (int) (14 * scale + 0.5f);
							descriptionTextView.setTextSize(fontsize);
							int linespacing = 21;
							descriptionTextView.setLineSpacing(linespacing,1);

							int pixels = (int) (125 * scale + 0.5f);
							descriptionTextView.setMinHeight(pixels);*/
										descriptionimageView.setBackgroundColor(getResources().getColor(R.color.white));
										((LinearLayout) v.getParent()).addView(descriptionimageView);
										descriptionPos = position;
									}
								} else {
									if (((LinearLayout) v.getParent()).getChildAt(1) != null
											&& ((LinearLayout) v.getParent()).getChildAt(1) instanceof LinearLayout)
										((LinearLayout) v.getParent()).removeViewAt(1);
								}

							}
						});

					viewHolder.userName.setText(desc);

					viewHolder.bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bt_orange_2)));
//					viewHolder.accept.setVisibility(View.INVISIBLE);
//					viewHolder.decline.setVisibility(View.INVISIBLE);
					break;

			}
			return v;
		}

	}

	private void refreshFeed() {
		if (mAsyncTaskRunning != null)
			mAsyncTaskRunning.cancel(true);
		(mAsyncTaskRunning = new GetNewsFeedItems()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class GetNewsFeedItems extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "newsfeed/get/?start=0&count=" + count;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.NEWS_FEED, RequestWrapper.FAV);
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

			if (result != null) {
				if (result instanceof Object[]) {
					findViewById(R.id.feedListView).setVisibility(View.VISIBLE);
					Object[] arr = (Object[]) result;
					if (arr[0] != null)
						mWishesTotalCount = (Integer) arr[0];
					setWishes((ArrayList<FeedItem>) arr[1]);
				}
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.feedListView).setVisibility(View.GONE);
				}
			}
			swipeRefreshLayout.setRefreshing(false);

		}
	}

	private void setWishes(ArrayList<FeedItem> wishes) {
		this.wishes = wishes;
		if (wishes != null && wishes.size() > 0 && mWishesTotalCount > wishes.size()
				&& feedListView.getFooterViewsCount() == 0) {
			mListViewFooter = new LinearLayout(getApplicationContext());
			mListViewFooter.setBackgroundResource(R.color.white);
			mListViewFooter.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, width / 5));
			mListViewFooter.setGravity(Gravity.CENTER);
			mListViewFooter.setOrientation(LinearLayout.HORIZONTAL);
			ProgressBar pbar = new ProgressBar(getApplicationContext(), null,
					android.R.attr.progressBarStyleSmallInverse);
			mListViewFooter.addView(pbar);
			pbar.setTag("progress");
			pbar.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			feedListView.addFooterView(mListViewFooter);
		}
		feedListAdapter = new NewsFeedAdapter(mContext, R.layout.new_request_fragment, this.wishes);
		feedListView.setAdapter(feedListAdapter);
		feedListView.setOnScrollListener(new OnScrollListener() {



											 public void onScrollStateChanged(AbsListView view, int scrollState) {
											 }

											 public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
												 if(feedListView.canScrollVertically(-1))
													 showFAB(true);
												 else hideFAB();
												 if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount - 1 < mWishesTotalCount
														 && !loading && mListViewFooter != null) {

													 if (feedListView.getFooterViewsCount() == 1) {
														 loading = true;
														 new LoadModeFeed().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, totalItemCount - 1);
													 }
												 } else if (totalItemCount - 1 == mWishesTotalCount) {
													 feedListView.removeFooterView(mListViewFooter);
												 }
											 }


										 }
		);
	}

	private class LoadModeFeed extends AsyncTask<Integer, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Integer... params) {
			int start = params[0];
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "newsfeed/get?start=" + start + "&count=" + count;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.NEWS_FEED, RequestWrapper.FAV);
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
			if (result != null && result instanceof Object[]) {
				Object[] arr = (Object[]) result;
				wishes.addAll((ArrayList<FeedItem>) arr[1]);
				feedListAdapter.notifyDataSetChanged();
			}
			loading = false;
		}
	}

	private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
									   int height, boolean useDiskCache, boolean fastBlur) {

		if (cancelPotentialWork(url, imageView)) {

			GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type, fastBlur);

			final AsyncDrawable asyncDrawable = new AsyncDrawable(Home.this.getResources(), zapp.cache.get(url), task);
			imageView.setImageDrawable(asyncDrawable);
			if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
				((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
			}
			if (zapp.cache.get(url) == null) {
				try {
					task.executeOnExecutor(CommonLib.THREAD_POOL_EXECUTOR_IMAGE);
				} catch (RejectedExecutionException e) {
					CommonLib.sPoolWorkQueueImage.clear();
				}
				getImageArray.add(task);
			} else {
				imageView.setBackgroundResource(0);
				Bitmap blurBitmap = null;
				if (type != null && type.equalsIgnoreCase("blur") && imageView != null) {
					blurBitmap = CommonLib.fastBlur(((BitmapDrawable) imageView.getDrawable()).getBitmap(), 10);
				}
				if (imageView != null && blurBitmap != null) {
					imageView.setImageBitmap(blurBitmap);
				}
				if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
					((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
				}
			}
		} else if (imageView != null && imageView.getDrawable() != null
				&& ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
			imageView.setBackgroundResource(0);
			if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
					&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
				((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
			}
		}
	}

	private class GetImage extends AsyncTask<Object, Void, Bitmap> {

		String url = "";
		private WeakReference<ImageView> imageViewReference;
		private int width;
		private int height;
		boolean useDiskCache;
		String type;
		String url2 = "";
		boolean fastBlur = false;

		public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type,
						boolean fastBlur) {
			this.url = url;
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.width = width;
			this.height = height;
			this.useDiskCache = true;// useDiskCache;
			this.type = type;
			this.fastBlur = fastBlur;
		}

		@Override
		protected void onPreExecute() {
			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
						&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
						&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar)
					((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
			}
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			Bitmap bitmap = null;
			try {
				if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
					if (!destroyed) {
						if (useDiskCache) {
							bitmap = CommonLib.getBitmapFromDisk(url, getApplicationContext());
						}

						if (bitmap == null) {
							try {
								BitmapFactory.Options opts = new BitmapFactory.Options();
								opts.inJustDecodeBounds = true;
								opts.inPreferredConfig = Config.RGB_565;
								BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

								opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
								opts.inJustDecodeBounds = false;
								opts.inPreferredConfig = Config.RGB_565;

								bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null,
										opts);

//								if (fastBlur)
//									bitmap = CommonLib.fastBlur(bitmap, 4);
								if (useDiskCache) {
									// if
									// (CommonLib.shouldScaleDownBitmap(Home.this,
									// bitmap)) {
									// bitmap =
									// Bitmap.createScaledBitmap(bitmap, width,
									// height, false);
									// }
									CommonLib.writeBitmapToDisk(url, bitmap, Home.this.getApplicationContext(),
											Bitmap.CompressFormat.JPEG);
								}
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							} catch (Error e) {
								zapp.cache.clear();
							}
						}

					} else {
						this.cancel(true);
					}
				}
			} catch (Exception e) {
			}

			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {

			if (isCancelled()) {
				bitmap = null;
			}

			if (bitmap != null) {
//				if (this.type.equalsIgnoreCase("user"))
//					bitmap = CommonLib.getRoundedCornerBitmap(bitmap, width);

				zapp.cache.put(url, bitmap);

			} else if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				GetImage task = getBitmapWorkerTask(imageView);
				if (task != null) {
					if (task.url2.equals("")) {
						task.url2 = new String(task.url);
					}
					task.url = "";
				}
			}

			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();

				if (imageView != null && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					GetImage currentTask = getBitmapWorkerTask(imageView);

					if ((!url.equals("")) && currentTask != null
							&& (currentTask.url.equals(url) || currentTask.url2.equals(url))) {
						GetImage task = new GetImage(url, imageView, width, height, true, type, fastBlur);
                        if (fastBlur)
                            bitmap = CommonLib.fastBlur(bitmap, 4);
						final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), bitmap, task);
						imageView.setImageDrawable(asyncDrawable);
						imageView.setBackgroundResource(0);
						if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
								&& ((ViewGroup) imageView.getParent()).getChildAt(1) != null
								&& ((ViewGroup) imageView.getParent()).getChildAt(1) instanceof ProgressBar) {
							((ViewGroup) imageView.getParent()).getChildAt(1).setVisibility(View.GONE);
						}
					} else {
						CommonLib.ZLog("getimagearray-imageview", "wrong bitmap");
					}
					getImageArray.remove(this);

				} else if (imageView != null) {
					GetImage task = getBitmapWorkerTask(imageView);
					if (task != null) {
						// if(task.url2.equals("")) {
						task.url2 = new String(task.url);
						// }
						task.url = "";
					}
				} else if (imageView == null) {
					CommonLib.ZLog("getimagearray-imageview", "null");
				}
			}

			/*
			 * if (imageViewReference != null && bitmap != null) { final
			 * ImageView imageView = imageViewReference.get(); if (imageView !=
			 * null) { imageView.setImageBitmap(bitmap);
			 * imageView.setBackgroundResource(0); getImageArray.remove(this); }
			 * }
			 */
		}
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

	private class AsyncDrawable extends BitmapDrawable {
		// private final SoftReference<GetImage> bitmapWorkerTaskReference;
		private final GetImage bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
			super(res, bitmap);
			// bitmapWorkerTaskReference = new
			// SoftReference<GetImage>(bitmapWorkerTask);
			bitmapWorkerTaskReference = bitmapWorkerTask;
		}

		public GetImage getBitmapWorkerTask() {
			return bitmapWorkerTaskReference;
			// return bitmapWorkerTaskReference.get();
		}
	}

	public boolean cancelPotentialWork(String data, ImageView imageView) {
		final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {

			final String bitmapData = bitmapWorkerTask.url;
			if (!bitmapData.equals(data)) {
				if (bitmapWorkerTask.url2.equals("")) {
					bitmapWorkerTask.url2 = new String(bitmapWorkerTask.url);
				}
				// Cancel previous task
				bitmapWorkerTask.url = "";
				bitmapWorkerTask.cancel(true);
				// getImageArray.clear();
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
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
									Intent intent = new Intent(Home.this, MessagesActivity.class);
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
		} else if (requestType == CommonLib.UPDATE_RATING) {
			if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
				z_ProgressDialog.dismiss();

			if (destroyed || !status)
				return;
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
	}

	private void showRateUsDialog() {
		try {
			View customView = inflater.inflate(R.layout.rate_dialog, null);
			mPlusOneButton = (PlusOneButton) customView.findViewById(R.id.plus_one_button);
			mPlusOneButton.initialize("https://market.android.com/details?id=" + getPackageName(), 0);

			final AlertDialog dialog = new AlertDialog.Builder(Home.this, AlertDialog.THEME_HOLO_LIGHT)
					.setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							CommonLib.ZLog("rate dialog", "onCancel");
							int rateDialogCounterTrigger = prefs.getInt("rate_dialog_trigger", 3);
							CommonLib.ZLog("rate dialog",
									"onCancel() rateDialogCounterTrigger = " + rateDialogCounterTrigger);

							if (rateDialogCounterTrigger == 3)
								rateDialogCounterTrigger = 8;
							else if (rateDialogCounterTrigger == 8)
								rateDialogCounterTrigger = 13;
							else if (rateDialogCounterTrigger == 13)
								rateDialogCounterTrigger = 20;
							else if (rateDialogCounterTrigger > 13)
								rateDialogCounterTrigger = rateDialogCounterTrigger + 10;

							Editor edit = prefs.edit();
							edit.putInt("rate_dialog_trigger", rateDialogCounterTrigger);
							edit.commit();

							CommonLib.ZLog("rate dialog",
									"onCancel() rateDialogCounterTrigger is now " + rateDialogCounterTrigger);
						}
					})

					.setView(customView)
					.create();

			dialog.setCanceledOnTouchOutside(true);
			dialog.show();

			customView.findViewById(R.id.rate_dialog_rate_now).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					CommonLib.ZLog("rate dialog", "rate now");

					try {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse("market://details?id=" + getPackageName()));
						startActivity(browserIntent);

					} catch (ActivityNotFoundException e) {
					} catch (Exception e) {
					}

					Editor edit = prefs.edit();
					edit.putBoolean("rate_dialog_show", false);
					edit.commit();
					dialog.dismiss();
				}
			});

			customView.findViewById(R.id.rate_dialog_remind).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					int rateDialogCounterTrigger = prefs.getInt("rate_dialog_trigger", 3);
					CommonLib.ZLog("rate dialog", "remind rateDialogCounterTrigger = " + rateDialogCounterTrigger);

					if (rateDialogCounterTrigger == 3)
						rateDialogCounterTrigger = 8;
					else if (rateDialogCounterTrigger == 8)
						rateDialogCounterTrigger = 13;
					else if (rateDialogCounterTrigger == 13)
						rateDialogCounterTrigger = 20;
					else if (rateDialogCounterTrigger > 13)
						rateDialogCounterTrigger = rateDialogCounterTrigger + 10;

					Editor edit = prefs.edit();
					edit.putInt("rate_dialog_trigger", rateDialogCounterTrigger);
					edit.commit();
					dialog.dismiss();
					CommonLib.ZLog("rate dialog", "rateDialogCounterTrigger is now at " + rateDialogCounterTrigger);
				}
			});

			customView.findViewById(R.id.rate_dialog_never).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CommonLib.ZLog("rate dialog", "never");

					Editor edit = prefs.edit();
					edit.putBoolean("rate_dialog_show", false);
					edit.commit();

					dialog.dismiss();
				}
			});

		} catch (Exception e) {

		}
	}

	// get institution name list...
	private class AppConfigandRating extends AsyncTask<Object, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "appConfig/version?";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.APP_CONFIG_VERSION_AND_RATING, RequestWrapper.FAV);
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


			if (result != null && result instanceof Object[]) {
				Object[] response = (Object[]) result;
				Boolean asd=(Boolean) response[1];
				if (!(Boolean) response[1]) {
					// if true show the dialog, else do not show the dialog
					new AlertDialog.Builder(mContext).setMessage(getResources().getString(R.string.update_message))
							.setCancelable(false).setPositiveButton(getResources().getString(R.string.update),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									CommonLib.ZLog("Update", "update now");
									try {
										Intent browserIntent = new Intent(Intent.ACTION_VIEW,
												Uri.parse("market://details?id=" + getPackageName()));
										startActivity(browserIntent);
									} catch (ActivityNotFoundException e) {
									} catch (Exception e) {
									}
									dialog.dismiss();
								}
							})
							.show();
				}

				if (response[0] instanceof ArrayList<?>) {
					ArrayList<UserComactMessage> userlist = (ArrayList<UserComactMessage>) response[0];

					if (!userlist.isEmpty()) {
						for (UserComactMessage user : userlist) {
							showRatingDialog(user);
							break;
						}

					}

				}


			}

			else {


			}

		}
	}

	private void displayAddressMap(ImageView addressMap, double lot, double lon) {

		addressMap.getLayoutParams().width = width;

		((FrameLayout) addressMap.getParent()).getLayoutParams().width = width;
		addressMap.getLayoutParams().width = width;

		String mapUrl = "http://maps.googleapis.com/maps/api/staticmap?center=" + lot + "," + lon + "&zoom=18&size="
				+ width + "x" + getResources().getDimensionPixelSize(R.dimen.height125)
				+ "&maptype=terrain&scale=2&markers=icon:http://i.imgur.com/Kn5aI2q.png?1"+"|scale:2|"
				+ lot + "," + lon;

		setImageFromUrlOrDisk(mapUrl, addressMap, "static_map", width,
				getResources().getDimensionPixelSize(R.dimen.height125), false, false);

		// click
		((FrameLayout) addressMap.getParent()).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BTracker.logGAEvent(Home.this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_MAP_IMAGE_PRESSED, "");
				Intent intent = new Intent(Home.this, MapActivity.class);
				startActivity(intent);

			}
		});

	}

	public void showRatingDialog(UserComactMessage user)
	{
		if(user == null || user.getUser() == null)
			return;

		final UserComactMessage temp=user;

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.rating_popup, null);
		dialogBuilder.setView(dialogView);
		((TextView)dialogView.findViewById(R.id.name)).setText(user.getUser().getUserName());
		ImageView userratingimage =((ImageView)dialogView.findViewById(R.id.image));
		setImageFromUrlOrDisk(user.getUser().getImageUrl(), userratingimage, "profile_pic", width, width, false, false);
		RatingBar ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar);
		final String ratedValue=ratingBar.getRating()+"";
		Drawable progress = ratingBar.getProgressDrawable();
		DrawableCompat.setTintMode(progress, PorterDuff.Mode.SRC_ATOP);
		DrawableCompat.setTint(progress, R.color.green_gradient);
		dialogBuilder.setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				z_ProgressDialog = ProgressDialog.show(Home.this, null,
						getResources().getString(R.string.update_rating_dialog), true, false);

				UploadManager.updateRating(prefs.getString("access_token", ""),temp.getUser().getUserId()+"",ratedValue,temp.getWish().getWishId()+"",temp.getWish().getUserId()+"");
			}
		});
		dialogBuilder.setNegativeButton("LATER", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		AlertDialog b = dialogBuilder.create();
		b.show();


	}

	private class CheckToken extends AsyncTask<Object, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "appConfig/token";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.APP_CONFIG_TOKEN, RequestWrapper.FAV);
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

			if (result != null) {
				if (result instanceof Object[]) {
				try {
					String status = (String) ((Object[]) result)[0];
					String response = (String) ((Object[]) result)[1];
					if(status != null && status.equalsIgnoreCase("success") && response != null && response.equalsIgnoreCase("1")) {

					} if(status != null && status.equalsIgnoreCase("success") && response.equalsIgnoreCase("2")) {
						// disconnect facebook
						try {

							Session fbSession = Session.getActiveSession();
							if (fbSession != null) {
								fbSession.closeAndClearTokenInformation();
							}
							Session.setActiveSession(null);

						} catch (Exception e) {
						}

						String accessToken = prefs.getString("access_token", "");
						UploadManager.logout(accessToken);

						Editor editor = prefs.edit();
						editor.putInt("uid", 0);
						editor.putString("thumbUrl", "");
						editor.putString("access_token", "");
						editor.remove("username");
						editor.remove("profile_pic");
						editor.remove("HSLogin");
						editor.remove("INSTITUTION_NAME");
						editor.remove("STUDENT_ID");
						editor.putBoolean("facebook_post_permission", false);
						editor.putBoolean("post_to_facebook_flag", false);
						editor.putBoolean("facebook_connect_flag", false);
						editor.putBoolean("twitter_status", false);

						editor.commit();
						//To stop getting chat notification after uninstall
						try {
							GoogleCloudMessaging.getInstance(getApplicationContext()).unregister();
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (prefs.getInt("uid", 0) == 0) {
							Intent intent = new Intent(zapp, SplashScreen.class);
							startActivity(intent);
							finish();
						}
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				}
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.feedListView).setVisibility(View.GONE);
				}
			}
		}
	}


}
