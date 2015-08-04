package com.application.baatna.views;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.data.User;
import com.application.baatna.mapUtils.Cluster;
import com.application.baatna.mapUtils.ClusterManager;
import com.application.baatna.mapUtils.GoogleMapRenderer;
import com.application.baatna.mapUtils.SimpleRestaurantPin;
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

public class MapActivity extends Activity {

	private BaatnaApp zapp;
	private SharedPreferences prefs;
	private int width;
	private LayoutInflater inflater;

	/** Map Object */
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

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.google_map_view_layout);

		inflater = LayoutInflater.from(this);

		try {
			MapsInitializer.initialize(this);
		} catch (Exception e) {
			Crashlytics.logException(e);
		}

		View inflatedView = inflater.inflate(R.layout.google_map_view_layout,
				null);
		mMapView = (MapView) findViewById(R.id.search_map);
		mMapView.onCreate(savedInstanceState);

		prefs = getSharedPreferences("application_settings", 0);
		zapp = (BaatnaApp) getApplication();
		init();

	}

	private void init() {
		lat = zapp.lat;
		lon = zapp.lon;

		double lat = 51.5145160;
		double lng = -0.1270060;
		this.lat = lat;
		this.lon = lng;

		mapResultList = new ArrayList<User>();
		// Add ten cluster items in close proximity, for purposes of this
		// example.
		for (int i = 0; i < 10; i++) {
			double offset = i / 60d;
			lat = lat + offset;
			lng = lng + offset;
			User offsetItem = new User();
			offsetItem.setLatitude(lat);
			offsetItem.setLongitude(lon);
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
					MapsInitializer.initialize(MapActivity.this);
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

	@Override
	public void onDestroy() {

		if (mMapView != null)
			mMapView.onDestroy();

		// if (zapp != null && zapp.cacheForMarkerImages != null)
		// zapp.cacheForMarkerImages.clear();
		super.onDestroy();
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

}
