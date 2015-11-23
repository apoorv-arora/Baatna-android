package com.application.baatna.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.application.baatna.R;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class GoogleMapFragment extends Fragment {

	// create a map object
	private GoogleMap googleMap;

	private View rootView;

	private Activity mContext;

	// Position global variable of store
	Double oldLatitude = 0.0, oldLongitude = 0.0, newLatitude = 0.0,
			newLongitude = 0.0;

	// Restaurant details
	String resName = "Store location marker";
	int resId = -1;

	ProgressDialog dialogMapLoader, progressDialog = null;

	private int width;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_map,
				container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();

		width = mContext.getWindowManager().getDefaultDisplay().getWidth();

		Bundle args = getArguments();
		if (args != null) {// put default check
			oldLatitude = args.getDouble("latitude");
			oldLongitude = args.getDouble("longitude");
			resName = args.getString("resName");
			resId = args.getInt("resId");
		}
		// load data from activity
		try {// Loading map
			initilizeMap();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */

	private void initilizeMap() {
		if (googleMap == null) {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (!destroyed) {
						try {
							googleMap = ((MapFragment) getFragmentManager()
									.findFragmentById(R.id.map)).getMap();
							googleMap.getUiSettings().setAllGesturesEnabled(
									true);
							googleMap.setMyLocationEnabled(true);
							googleMap.setBuildingsEnabled(true);
							// create a position
							LatLng location = new LatLng(oldLatitude,
									oldLongitude);
							googleMap.animateCamera(CameraUpdateFactory
									.newLatLngZoom(location, (float) 15.0));
						} catch (Exception e) {
							Crashlytics.logException(e);
							e.printStackTrace();
						}
					}
				}
			}, 1000);
		}

	}

	private boolean destroyed = false;

	@Override
	public void onDestroy() {
		destroyed = true;
		super.onDestroy();
	}

}