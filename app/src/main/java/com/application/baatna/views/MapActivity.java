package com.application.baatna.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.data.User;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.TypefaceSpan;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Created by apoorvarora on 17/01/16.
 */
public class MapActivity extends AppCompatActivity {

	private BaatnaApp zapp;
	private SharedPreferences prefs;
	private int width;
	private LayoutInflater inflater;
	private AsyncTask mAsyncRunning;

	/** Map Object */
	private GoogleMap mMap;
	private MapView mMapView;
	private float defaultMapZoomLevel = 12.5f + 1.75f;
	// private LatLng recentLatLng;
	private ArrayList<LatLng> mapCoords;
	private final float MIN_MAP_ZOOM = 13.0f;

	private double lat;
	private double lon;

	private ArrayList<User> mapResultList;
	private boolean destroyed = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.google_map_view_layout);

		width = getWindowManager().getDefaultDisplay().getWidth();

		inflater = LayoutInflater.from(this);

		try {
			MapsInitializer.initialize(this);
		} catch (Exception e) {
			Crashlytics.logException(e);
		}

		mMapView = (MapView) findViewById(R.id.search_map);
		mMapView.onCreate(savedInstanceState);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);
		setupActionBar();

		prefs = getSharedPreferences("application_settings", 0);
		zapp = (BaatnaApp) getApplication();
		lat = zapp.lat;
		lon = zapp.lon;
		refreshView();

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private void setupActionBar() {
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.your_neighbourhood));
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
				cameraPosition = new CameraPosition.Builder().target(targetCoords) // Sets
						// the
						// center
						// of
						// the
						// map
						// to
						// Mountain
						// View
						.zoom(defaultMapZoomLevel) // Sets the zoom
						.build(); // Creates a CameraPosition from the builder

				try {
					mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				} catch (Exception e) {
					MapsInitializer.initialize(MapActivity.this);
					mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

	@Override
	public void onDestroy() {

		if (mMapView != null)
			mMapView.onDestroy();

		destroyed = true;
		// if (zapp != null && zapp.cacheForMarkerImages != null)
		// zapp.cacheForMarkerImages.clear();
		super.onDestroy();
	}

	private void refreshMap() {

		if (lat != 0.0 && lon != 0.0) {

			if (mMap == null)
				setUpMapIfNeeded();

			// Clearing The current clustering restaurant dataset
			if (mMap != null && mapResultList != null && !mapResultList.isEmpty()) {

				for (User r : mapResultList) {
					if (r.getLatitude() != 0.0 || r.getLongitude() != 0.0) {
						BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_icon);
						mMap.addMarker(new MarkerOptions().position(new LatLng(r.getLatitude(), r.getLongitude())).icon(icon));
					}
				}

			}
		}
	}

	private boolean displayed = false;

	@Override
	public void onResume() {

		super.onResume();
		displayed = true;
		if (mMapView != null) {
			mMapView.onResume();

			if (mMap == null && (lat != 0.0 || lon != 0.0))
				setUpMapIfNeeded();
		}

	}

	private void dynamicZoom(LatLngBounds bounds) throws IllegalStateException {

		if (mapResultList != null && mapResultList.size() == 1) {
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width / 40 + width / 15 + width / 15));
		} else if (mMap != null)
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width / 40 + width / 15));
	}

	private void refreshView() {
		if (mAsyncRunning != null)
			mAsyncRunning.cancel(true);
		mAsyncRunning = new GetCategoriesList().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	private class GetCategoriesList extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			findViewById(R.id.wishbox_progress_container).setVisibility(View.VISIBLE);

			findViewById(R.id.content).setAlpha(1f);

			findViewById(R.id.content).setVisibility(View.GONE);

			findViewById(R.id.empty_view).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "user/nearbyusers/?latitude="+zapp.lat+"&longitude="+zapp.lon;
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.NEARBY_USERS, RequestWrapper.FAV);
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
			findViewById(R.id.wishbox_progress_container).setVisibility(View.GONE);
			if (result != null) {
				findViewById(R.id.content).setVisibility(View.VISIBLE);
				if (result instanceof ArrayList<?>) {
					mapResultList = (ArrayList<User>) result;
					refreshMap();
				}
			} else {
				if (CommonLib.isNetworkAvailable(MapActivity.this)) {
					Toast.makeText(MapActivity.this, getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MapActivity.this, getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();
					findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					findViewById(R.id.content).setVisibility(View.GONE);

				}
			}

		}
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

}

