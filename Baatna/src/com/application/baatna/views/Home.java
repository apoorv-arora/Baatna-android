package com.application.baatna.views;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.data.FeedItem;
import com.application.baatna.data.User;
import com.application.baatna.data.Wish;
import com.application.baatna.mapUtils.Cluster;
import com.application.baatna.mapUtils.ClusterManager;
import com.application.baatna.mapUtils.GoogleMapRenderer;
import com.application.baatna.mapUtils.SimpleRestaurantPin;
import com.application.baatna.utils.BaatnaLocationCallback;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.FacebookConnectCallback;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.fab.FABControl;
import com.application.baatna.utils.fab.FABControl.OnFloatingActionsMenuUpdateListener;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Home extends FragmentActivity implements FacebookConnectCallback,
		BaatnaLocationCallback, OnFloatingActionsMenuUpdateListener {

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

	private GoogleMapFragment maps;
	private Activity mContext;

	// FAB Stuff
	private View mFABOverlay;
	private boolean mFABExpanded = false;
	private boolean mFABVisible = false;

	private GetNewsFeedItems mAsyncTaskRunning;
	View headerView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.home_activity);
		getWindow().setBackgroundDrawable(null);
		mContext = this;
		inflater = LayoutInflater.from(this);
		prefs = getSharedPreferences(CommonLib.APP_SETTINGS, 0);
		zapp = (BaatnaApp) getApplication();
		zapp.zll.addCallback(this);
		width = getWindowManager().getDefaultDisplay().getWidth();

		setupActionBar();
		setUpDrawer();
		// initialize list view
		setUpFAB();

		headerView = View.inflate(this, R.layout.map_header_view, null);

		feedListView = (ListView) findViewById(R.id.feedListView);
		feedListView.addHeaderView(headerView);
		feedListView.setDividerHeight(width / 40);

		headerView.findViewById(R.id.search_map).getLayoutParams().width = width;
		headerView.findViewById(R.id.search_map).getLayoutParams().height = width;

		headerView.findViewById(R.id.search_map).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Home.this, MapActivity.class);
				startActivity(intent);
			}
		});

		setMapActivity(savedInstanceState);
		refreshFeed();
	}

	private void setUpFAB() {

		((FABControl) findViewById(R.id.multiple_actions))
				.setOnFloatingActionsMenuUpdateListener(this);
		// overlay behind FAB
		mFABOverlay = findViewById(R.id.fab_overlay);
		mFABOverlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggleFAB();
			}
		});
		mFABOverlay.setClickable(false);

		showFAB(true);
	}

	private void toggleFAB() {
		((FABControl) findViewById(R.id.multiple_actions)).toggle();
	}

	// makes FAB visible.
	public void showFAB(boolean delayed) {

		if (!mFABVisible) {
			mFABVisible = true;

			findViewById(R.id.multiple_actions).setVisibility(View.VISIBLE);

			if (delayed) {
				ViewPropertyAnimator animator = findViewById(
						R.id.fab_expand_menu_button).animate().scaleX(1)
						.scaleY(1).setDuration(250)
						.setInterpolator(new AccelerateInterpolator())
						.setStartDelay(700);
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

			} else {

				ViewPropertyAnimator animator = findViewById(
						R.id.fab_expand_menu_button).animate().scaleX(1)
						.scaleY(1).setDuration(200)
						.setInterpolator(new AccelerateInterpolator());
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
		final AlertDialog logoutDialog;
		logoutDialog = new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.logout))
				.setMessage(getResources().getString(R.string.logout_confirm))
				.setPositiveButton(getResources().getString(R.string.logout),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// disconnect facebook
								try {

									com.facebook.Session fbSession = com.facebook.Session
											.getActiveSession();
									if (fbSession != null) {
										fbSession
												.closeAndClearTokenInformation();
									}
									com.facebook.Session.setActiveSession(null);

								} catch (Exception e) {
								}

								String accessToken = prefs.getString(
										"access_token", "");
								UploadManager.logout(accessToken);

								Editor editor = prefs.edit();
								editor.putInt("uid", 0);
								editor.putString("thumbUrl", "");
								editor.putString("access_token", "");
								editor.remove("username");
								editor.putBoolean("facebook_post_permission",
										false);
								editor.putBoolean("post_to_facebook_flag",
										false);
								editor.putBoolean("facebook_connect_flag",
										false);
								editor.putBoolean("twitter_status", false);

								editor.commit();

								if (prefs.getInt("uid", 0) == 0) {
									Intent intent = new Intent(zapp,
											BaatnaActivity.class);
									startActivity(intent);
									finish();
								}
							}
						})
				.setNegativeButton(
						getResources().getString(R.string.dialog_cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		logoutDialog.show();
	}

	// makes FAB gone.
	public void hideFAB() {

		if (mFABVisible) {
			mFABVisible = false;

			ViewPropertyAnimator animator = findViewById(
					R.id.fab_expand_menu_button).animate().scaleX(0).scaleY(0)
					.setDuration(50).setStartDelay(0)
					.setInterpolator(new AccelerateInterpolator());

			animator.setListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					findViewById(R.id.multiple_actions)
							.setVisibility(View.GONE);
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

			if (findViewById(R.id.multiple_actions).getVisibility() != View.VISIBLE)
				findViewById(R.id.multiple_actions).setVisibility(View.VISIBLE);

			findViewById(R.id.fab_expand_menu_button).setScaleX(1 - input);
			findViewById(R.id.fab_expand_menu_button).setScaleY(1 - input);

		} else {

			if (findViewById(R.id.fab_expand_menu_button).getScaleX() != 0)
				findViewById(R.id.fab_expand_menu_button).setScaleX(0);

			if (findViewById(R.id.fab_expand_menu_button).getScaleY() != 0)
				findViewById(R.id.fab_expand_menu_button).setScaleY(0);

			if (findViewById(R.id.multiple_actions).getVisibility() != View.GONE)
				findViewById(R.id.multiple_actions).setVisibility(View.GONE);
		}

	}

	private void setupActionBar() {

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		
		View v = inflater.inflate(R.layout.baatna_action_bar, null);

		v.findViewById(R.id.action_buttons).setVisibility(View.VISIBLE);
		v.findViewById(R.id.home_icon_zomato).setVisibility(View.VISIBLE);
		
		v.findViewById(R.id.open_messages).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Home.this,
								MessagesActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_right,
								R.anim.slide_out_left);

					}
				});

		v.findViewById(R.id.home_icon_container).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						openOrCloseDrawer();
					}
				});

		try {
			v.findViewById(R.id.home_icon_zomato).setPadding(width / 80,
					width / 80, width / 80, width / 80);
		} catch (Exception e) {

		}

		// user handle
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setPadding(width / 80, 0, width / 40, 0);
		actionBar.setCustomView(v);

		// setImageOnActionBar();
	}

	private void initializeMap() {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		maps = new GoogleMapFragment();
		Bundle bundle = new Bundle();
		bundle.putDouble("latitude", 0);
		bundle.putDouble("longitude", 0);
		maps.setArguments(bundle);
		fragmentTransaction.add(R.id.fragment_container, maps, "map_container");
		// fragmentTransaction.setCustomAnimations(R.anim.slide_in_right,
		// R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		fragmentTransaction.commit();
	}

	public void openOrCloseDrawer() {

		if (!mFABExpanded) {
			if (mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
				InputMethodManager mgr = (InputMethodManager) getApplicationContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
						0);
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
				Intent intent = new Intent(Home.this, NewRequestActivity.class);
				startActivityForResult(intent, CommonLib.NEW_REQUEST);
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

		if (mDrawerLayout != null
				&& mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
			mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
			return;
		}
	}

	@Override
	public void onResume() {
		try {
			super.onResume();

			displayed = true;
			if (mMapView != null) {
				mMapView.onResume();

				if (mMap == null && (lat != 0.0 || lon != 0.0))
					setUpMapIfNeeded();
			}
			if (mFABExpanded)
				toggleFAB();

			// logging out user
			// if (prefs.getInt("uid", 0) == 0) {
			// Intent intent = new Intent(zapp, BaatnaActivity.class);
			// startActivity(intent);
			// finish();
			// }

		} catch (Exception e) {
		}

	}

	private void setUpUserSettingsInDrawer() {

		// user snippet in drawer
		findViewById(R.id.drawer_user_container).getLayoutParams().height = width / 3;
		findViewById(R.id.drawer_user_stat_cont).setPadding(3 * width / 80, 0,
				0, 0);
		findViewById(R.id.drawer_user_gradient_bottom).getLayoutParams().height = (12 * width / 90);
		findViewById(R.id.drawer_user_info_background_image).getLayoutParams().height = width / 3;
		findViewById(R.id.seperator).getLayoutParams().height = width / 30;

		// user click
		findViewById(R.id.drawer_user_container).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// navigateToMyProfile();
					}
				});

		setImageInDrawer();
		updateUserInfoInDrawer();

	}

	// called from
	// 1. Home when drawer is being set.
	// 2. Me fragment after EDIT PROFILE.
	public void setImageInDrawer() {
		// Blurred user image
		ImageView imageBackground = (ImageView) findViewById(R.id.drawer_user_info_background_image);
		// imageBackground.getLayoutParams().height = width / 3;
		//
		// CommonLib.ZLog("thumbUrl blurred", prefs.getString("thumbUrl", "")
		// + ".");
		// if (!prefs.getString("thumbUrl", "").equals("")
		// && prefs.getString("thumbUrl", "").length() > 0) {
		// FetchBackgroundImage task = new FetchBackgroundImage(
		// imageBackground, prefs.getString("thumbUrl", ""), width,
		// width / 3);
		// task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		//
		// } else {
		// setBackgroundColor(imageBackground);
		// }

	}

	// called from
	// 1. Home when drawer is being set.
	// 2. Me fragment after the user object is populated.
	public void updateUserInfoInDrawer() {
		//
		//
		// String userNameString = prefs.getString("username", "");
		//
		// // user name, followers, reviews
		// if ((!userNameString.equals("") && userNameString.length() > 0)
		// && prefs.contains("numReviews")
		// && prefs.contains("numFollowers")) {
		//
		// TextView userName = (TextView) findViewById(R.id.drawer_user_name);
		// userName.setText(userNameString);
		//
		// TextView userStats = (TextView) findViewById(R.id.drawer_user_stats);
		// int followerCount = prefs.getInt("numFollowers", 0);
		// int reviewCount = prefs.getInt("numReviews", 0);
		//
		// if ((followerCount > 0 && reviewCount > 0)
		// || (reviewCount == 0 && followerCount == 0)
		// || (reviewCount == 0 && followerCount > 0)
		// || (reviewCount > 0 && followerCount == 0))
		// userStats.setText(String.format(
		// getResources().getString(R.string.user_stats),
		// reviewCount, followerCount));
		// else if (reviewCount > 0 && followerCount == 1)
		// userStats.setText(String.format(
		// getResources().getString(
		// R.string.user_stats_single_follower),
		// reviewCount, followerCount));
		// else if (reviewCount == 1 && followerCount > 0)
		// userStats.setText(String.format(
		// getResources().getString(
		// R.string.user_stats_single_review),
		// reviewCount, followerCount));
		// }
	}

	// public void feedback(View v) {
	// startActivity(new Intent(this, FeedbackPage.class));
	// }

	public void rate(View v) {

		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=com.application.zomato"));
			startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {

		} catch (Exception e) {

		}
	}

	public void openWishbox(View v) {
		Intent intent = new Intent(this, WishboxActivity.class);
		startActivity(intent);
	}

	@Override
	public void onDestroy() {
		if (mMapView != null)
			mMapView.onDestroy();
		destroyed = true;
		mDrawerLayout = null;
		zapp.zll.removeCallback(this);
		zapp.cache.clear();
		super.onDestroy();
	}

	@Override
	public void onCoordinatesIdentified(Location loc) {
	}

	@Override
	public void onLocationIdentified() {
	}

	@Override
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
	}

	@Override
	public void onNetworkError() {
	}

	@Override
	public void response(Bundle bundle) {

	}

	public void aboutus(View view) {
		Intent intent = new Intent(this, AboutUs.class);
		startActivity(intent);
	}

	public void feedback(View v) {
		startActivity(new Intent(this, FeedbackPage.class));
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

	private boolean mapRefreshed = false;
	private GoogleMap mMap;
	private MapView mMapView;
	private float defaultMapZoomLevel = 12.5f + 1.75f;
	private boolean mMapSearchAnimating = false;
	public boolean mapSearchVisible = false;
	private boolean mapOptionsVisible = true;
	// private LatLng recentLatLng;
	private ArrayList<LatLng> mapCoords;
	private final float MIN_MAP_ZOOM = 13.0f;

	private View restMarker;
	private View clusterMarker;
	private View landmarkMarker;

	private ClusterManager plotManager;
	public boolean drawOverlayActive = false;

	private double lat;
	private double lon;

	private ArrayList<User> mapResultList;

	private void setMapActivity(Bundle savedInstanceState) {
		try {
			MapsInitializer.initialize(this);
		} catch (Exception e) {
			Crashlytics.logException(e);
		}

		View inflatedView = inflater.inflate(R.layout.google_map_view_layout,
				null);
		mMapView = (MapView) headerView.findViewById(R.id.search_map);
		mMapView.onCreate(savedInstanceState);

		prefs = getSharedPreferences("application_settings", 0);
		zapp = (BaatnaApp) getApplication();
		init();
	}

	private void init() {
		lat = zapp.lat;
		lon = zapp.lon;

		refreshMap();
		new GetCategoriesList().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private void updateMapPoints(ArrayList<LatLng> locations) {

		mapResultList = new ArrayList<User>();
		for (LatLng location : locations) {
			User offsetItem = new User();
			offsetItem.setLatitude(location.latitude);
			offsetItem.setLongitude(location.longitude);
			mapResultList.add(offsetItem);
		}

		refreshMap();
	}

	private void setUpMapIfNeeded() {
		if (mMap == null && mMapView != null)
			mMap = mMapView.getMap();
		if (mMap != null) {

			LatLng targetCoords = null;

			if (lat != 0.0 || lon != 0.0)
				targetCoords = new LatLng(lat, lon);
			else {
				// target the current city
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.getUiSettings().setAllGesturesEnabled(true);
			mMap.getUiSettings().setZoomControlsEnabled(false);
			mMap.getUiSettings().setTiltGesturesEnabled(false);
			mMap.getUiSettings().setCompassEnabled(false);
			mMap.setMyLocationEnabled(true);
			mMap.setBuildingsEnabled(true);

			CameraPosition cameraPosition;
			if (targetCoords != null) {
				cameraPosition = new CameraPosition.Builder()
						.target(targetCoords) // Sets the center of the map to
												// Mountain View
						.zoom(defaultMapZoomLevel) // Sets the zoom
						.build(); // Creates a CameraPosition from the builder

				try {
					mMap.animateCamera(CameraUpdateFactory
							.newCameraPosition(cameraPosition));
				} catch (Exception e) {
					MapsInitializer.initialize(Home.this);
					mMap.animateCamera(CameraUpdateFactory
							.newCameraPosition(cameraPosition));
				}
			}

		}

	}

	private void disableMap() {
		if (mMap != null)
			mMap.getUiSettings().setAllGesturesEnabled(false);
	}

	private void enableMap() {
		if (mMap != null) {
			mMap.getUiSettings().setAllGesturesEnabled(true);
			mMap.getUiSettings().setZoomControlsEnabled(false);
			mMap.getUiSettings().setTiltGesturesEnabled(false);
			mMap.getUiSettings().setCompassEnabled(false);
			mMap.getUiSettings().setMyLocationButtonEnabled(false);
		}

	}

	@Override
	public void onPause() {
		displayed = false;
		if (mMapView != null)
			mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onLowMemory() {

		try {
			if (mMapView != null) {
				mMapView.onLowMemory();
			}
			// clearMapCache();
		} catch (Exception e) {
			Crashlytics.logException(e);
		}

		super.onLowMemory();
	}

	private void refreshMap() {

		if (lat != 0.0 && lon != 0.0) {

			if (mMap == null)
				setUpMapIfNeeded();

			if (plotManager == null)
				plotManager = new ClusterManager(this, mMap);

			// Clearing The current clustering restaurant dataset

			if (mMap != null && mapResultList != null
					&& !mapResultList.isEmpty()) {

				LatLngBounds.Builder builder = new LatLngBounds.Builder();

				ArrayList<SimpleRestaurantPin> clusterPins = new ArrayList<SimpleRestaurantPin>();
				for (User r : mapResultList) {
					if (r.getLatitude() != 0.0 || r.getLongitude() != 0.0) {
						// if (type.equals(MAP_LOCATION_SEARCH)
						// || (!type.equals(MAP_DRAW_SEARCH)))
						builder.include(new LatLng(r.getLatitude(), r
								.getLongitude()));
						SimpleRestaurantPin pin = new SimpleRestaurantPin();
						pin.setRestaurant(r);
						clusterPins.add(pin);
					}
				}

				if (clusterPins != null && clusterPins.size() == 1)
					showSingleRestaurant(clusterPins.get(0));

				// Adding new restaurant set to clustering algorithm
				plotManager.addItems(clusterPins);
				if (mMap != null) {
					mMap.setOnCameraChangeListener(plotManager);
					mMap.setOnMarkerClickListener(plotManager);
				}
			}
		}
	}

	private void showSingleRestaurant(SimpleRestaurantPin item) {

		String resIds = "";
		ArrayList<User> resList = new ArrayList<User>();
		if (item != null && item.getRestaurant() != null) {
			resList.add(item.getRestaurant());
		}
	}

	private boolean displayed = false;

	private void dynamicZoom(LatLngBounds bounds) throws IllegalStateException {

		if (mapResultList != null && mapResultList.size() == 1) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
					width / 40 + width / 15 + width / 15));
		} else if (mMap != null)
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,
					width / 40 + width / 15));
	}

	private class mRenderer extends GoogleMapRenderer implements
			GoogleMap.OnCameraChangeListener {

		public mRenderer() {
			super(mMap, plotManager);
		}

		@Override
		public void onCameraChange(CameraPosition cameraPosition) {
		}

		@Override
		protected void onBeforeClusterItemRendered(SimpleRestaurantPin item,
				MarkerOptions markerOptions) {

		}

		@Override
		protected void onBeforeClusterRendered(
				Cluster<SimpleRestaurantPin> cluster,
				MarkerOptions markerOptions) {

		}

		@Override
		protected void onClusterRendered(Cluster<SimpleRestaurantPin> cluster,
				Marker marker) {
		}

		@Override
		protected void onClusterItemRendered(SimpleRestaurantPin clusterItem,
				Marker marker) {
		}

	}

	// get institution name list...
	private class GetCategoriesList extends AsyncTask<Object, Void, Object> {

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "user/nearbyusers?";
				Object info = RequestWrapper.RequestHttp(url,
						RequestWrapper.NEARBY_USERS, RequestWrapper.FAV);
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
				if (result instanceof ArrayList<?>) {
					updateMapPoints((ArrayList<LatLng>) result);
				}
			} else {
				if (CommonLib.isNetworkAvailable(Home.this)) {
					Toast.makeText(Home.this,
							getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							Home.this,
							getResources().getString(
									R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();
				}
			}

		}
	}

	public class NewsFeedAdapter extends ArrayAdapter<FeedItem> {

		private List<FeedItem> feedItems;
		private Activity mContext;
		private int width;

		public NewsFeedAdapter(Activity context, int resourceId,
				List<FeedItem> feedItems) {
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
			// RoundedImageView userImage;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final FeedItem feedItem = feedItems.get(position);

			if (v == null || v.findViewById(R.id.feed_item_root) == null) {
				v = LayoutInflater.from(mContext).inflate(
						R.layout.feed_list_item_snippet, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.userName = (TextView) v.findViewById(R.id.user_name);
				// viewHolder.userImage = (RoundedImageView) v
				// .findViewById(R.id.user_image);
				viewHolder.time = (TextView) v.findViewById(R.id.time);
				// viewHolder.distance = (TextView)
				// v.findViewById(R.id.distance);
				viewHolder.bar = v.findViewById(R.id.left_bar);
				viewHolder.accept = (TextView) v
						.findViewById(R.id.accept_button);
				viewHolder.decline = (TextView) v
						.findViewById(R.id.decline_button);
				viewHolder.action_container = (LinearLayout) v
						.findViewById(R.id.action_container);
				v.setTag(viewHolder);
			}

			// fix sizes
			viewHolder.bar.getLayoutParams().width = width / 10;
			((RelativeLayout.LayoutParams) viewHolder.bar.getLayoutParams())
					.setMargins(width / 40, 0, 0, 0);
			((RelativeLayout.LayoutParams) viewHolder.action_container
					.getLayoutParams()).setMargins(0, 0, width / 40, 0);
				
			viewHolder.accept.setPadding(width / 40, width / 80, width / 40,
					width / 80);
			viewHolder.decline.setPadding(width / 40, width / 80, width / 40,
					width / 80);

			User user = feedItem.getUserIdFirst();

			User user2 = feedItem.getUserSecond();

			final Wish wish = feedItem.getWish();

			viewHolder.time.setText(CommonLib.getDateFromUTC(feedItem
					.getTimestamp()));

			switch (feedItem.getType()) {

			case CommonLib.FEED_TYPE_NEW_USER:
				if (user != null) {
					String description = getResources()
							.getString(R.string.feed_user_joined,
									user.getUserName() + " ");
					viewHolder.userName.setText(description);
					viewHolder.bar.setBackgroundDrawable(new ColorDrawable(
							getResources().getColor(R.color.zomato_red)));
					viewHolder.accept.setVisibility(View.INVISIBLE);
					viewHolder.decline.setVisibility(View.INVISIBLE);
				}
				break;
			case CommonLib.FEED_TYPE_NEW_REQUEST:
				if (user != null && wish != null) {
					String description = getResources().getString(
							R.string.feed_user_requested,
							user.getUserName() + " ", wish.getTitle() + " ");
					viewHolder.userName.setText(description);
					viewHolder.bar
							.setBackgroundDrawable(new ColorDrawable(
									getResources().getColor(
											R.color.black_trans_ninety)));

					viewHolder.accept
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									UploadManager.updateRequestStatus(
											prefs.getString("access_token", ""),
											"" + wish.getWishId(), "1");
								}
							});

					viewHolder.decline
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									UploadManager.updateRequestStatus(
											prefs.getString("access_token", ""),
											"" + wish.getWishId(), "2");
								}
							});
					viewHolder.accept.setVisibility(View.VISIBLE);
					viewHolder.decline.setVisibility(View.VISIBLE);
				}

				break;
			case CommonLib.FEED_TYPE_REQUEST_FULFILLED:
				String description = getResources().getString(
						R.string.feed_requested_fulfilled,
						user.getUserName() + " ", wish.getTitle() + " ",
						user2.getUserName());
				viewHolder.userName.setText(description);
				viewHolder.bar.setBackgroundDrawable(new ColorDrawable(
						getResources().getColor(R.color.bt_orange)));
				viewHolder.accept.setVisibility(View.INVISIBLE);
				viewHolder.decline.setVisibility(View.INVISIBLE);
				break;
			}

			return v;
		}

	}

	private void refreshFeed() {
		if (mAsyncTaskRunning != null)
			mAsyncTaskRunning.cancel(true);
		(mAsyncTaskRunning = new GetNewsFeedItems())
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class GetNewsFeedItems extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.feedListView).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "newsfeed/get?";
				Object info = RequestWrapper.RequestHttp(url,
						RequestWrapper.NEWS_FEED, RequestWrapper.FAV);
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
				if (result instanceof ArrayList<?>) {
					findViewById(R.id.feedListView).setVisibility(View.VISIBLE);
					feedListAdapter = new NewsFeedAdapter(mContext,
							R.layout.feed_list_item_snippet,
							(ArrayList<FeedItem>) result);
					feedListView.setAdapter(feedListAdapter);
				}
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					findViewById(R.id.feedListView).setVisibility(View.GONE);
				}
			}

		}
	}

}