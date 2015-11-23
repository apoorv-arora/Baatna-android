package com.application.baatna.mapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.application.baatna.mapUtils.ClusterManager.OnClusterClickListener;
import com.application.baatna.mapUtils.ClusterManager.OnClusterInfoWindowClickListener;
import com.application.baatna.mapUtils.ClusterManager.OnClusterItemClickListener;
import com.application.baatna.mapUtils.ClusterManager.OnClusterItemInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyRenderer implements ClusterRenderer<SimpleRestaurantPin>{

	private Set<? extends Cluster<SimpleRestaurantPin>> drawnClusters;

	private final String TAG = "MyRenderer";

	private final GoogleMap mMap;
	private final ClusterManager mClusterManager;

	private ClusterManager.OnClusterClickListener<SimpleRestaurantPin> mClickListener;
	private ClusterManager.OnClusterInfoWindowClickListener<SimpleRestaurantPin> mInfoWindowClickListener;
	private ClusterManager.OnClusterItemClickListener<SimpleRestaurantPin> mItemClickListener;
	private ClusterManager.OnClusterItemInfoWindowClickListener<SimpleRestaurantPin> mItemInfoWindowClickListener;

	private static final int MIN_CLUSTER_SIZE = 2;

	// Restaurants
	private MarkerCache<SimpleRestaurantPin> mMarkerCache = new MarkerCache<SimpleRestaurantPin>();


	// Clustered Restaurants
	private Map<Marker, Cluster<SimpleRestaurantPin>> mMarkerToCluster = new HashMap<Marker, Cluster<SimpleRestaurantPin>>();
	private Map<Cluster<SimpleRestaurantPin>, Marker> mClusterToMarker = new HashMap<Cluster<SimpleRestaurantPin>, Marker>();

	public MyRenderer(GoogleMap map, ClusterManager clusterManager) {
		mMap = map;
		mClusterManager = clusterManager;
	}

	@Override
	public void onClustersChanged(Set<? extends Cluster<SimpleRestaurantPin>> clusters) {
		

		drawOnMap(clusters);
	}

	private void drawOnMap(Set<? extends Cluster<com.application.baatna.mapUtils.SimpleRestaurantPin>> clusters) {

		if (drawnClusters != null && drawnClusters.equals(clusters)){
			return;
		} 


		for (Cluster<SimpleRestaurantPin> cluster : clusters) {

			if (!shouldRenderAsCluster(cluster)){
				// Get Rest Pin
				for (SimpleRestaurantPin item : cluster.getItems()) {
					Marker marker = mMarkerCache.get(item);
					MarkerWithPosition markerWithPosition;
					if (marker == null) {
						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.position(item.getPosition());
						onBeforeClusterItemRendered(item, markerOptions);
						//marker = mClusterManager.getMarkerCollection().addMarker(markerOptions);
						marker = mMap.addMarker(markerOptions);
						onClusterItemRendered(item, marker);
						mMarkerCache.put(item, marker);
					} 
				}

			} else {
				// Get Cluster Pin

				Marker marker = mClusterToMarker.get(cluster);

				if (marker == null){
					MarkerOptions markerOptions = new MarkerOptions().position(cluster.getPosition());
					onBeforeClusterRendered(cluster, markerOptions);
					//Marker marker = mClusterManager.getClusterMarkerCollection().addMarker(markerOptions);
					marker = mMap.addMarker(markerOptions);
					mMarkerToCluster.put(marker, cluster);
					mClusterToMarker.put(cluster, marker);
					onClusterRendered(cluster, marker);
				}
				//MarkerWithPosition markerWithPosition = new MarkerWithPosition(marker);
			}
		}
	}

	@Override
	public void setOnClusterClickListener(OnClusterClickListener<SimpleRestaurantPin> listener) {
		mClickListener = listener;
	}

	@Override
	public void setOnClusterInfoWindowClickListener(OnClusterInfoWindowClickListener<SimpleRestaurantPin> listener) {
		mInfoWindowClickListener = listener;
	}

	@Override
	public void setOnClusterItemClickListener(OnClusterItemClickListener<SimpleRestaurantPin> listener) {
		mItemClickListener = listener;
	}

	@Override
	public void setOnClusterItemInfoWindowClickListener(OnClusterItemInfoWindowClickListener<SimpleRestaurantPin> listener) {
		mItemInfoWindowClickListener = listener;
	}

	@Override
	public void onAdd() {

		mClusterManager.getMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				return mItemClickListener != null && mItemClickListener.onClusterItemClick(mMarkerCache.get(marker));
			}
		});

		mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if (mItemInfoWindowClickListener != null) {
					mItemInfoWindowClickListener.onClusterItemInfoWindowClick(mMarkerCache.get(marker));
				}
			}
		});

		mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				return mClickListener != null && mClickListener.onClusterClick(mMarkerToCluster.get(marker));
			}
		});

		mClusterManager.getClusterMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if (mInfoWindowClickListener != null) {
					mInfoWindowClickListener.onClusterInfoWindowClick(mMarkerToCluster.get(marker));
				}
			}
		});

	}

	@Override
	public void onRemove() {
		mClusterManager.getMarkerCollection().setOnMarkerClickListener(null);
		mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(null);
	}

	private static class MarkerCache<SimpleRestaurantPin> {
		private Map<SimpleRestaurantPin, Marker> mCache = new HashMap<SimpleRestaurantPin, Marker>();
		private Map<Marker, SimpleRestaurantPin> mCacheReverse = new HashMap<Marker, SimpleRestaurantPin>();

		public Marker get(SimpleRestaurantPin item) {
			return mCache.get(item);
		}

		public SimpleRestaurantPin get(Marker m) {
			return mCacheReverse.get(m);
		}

		public void put(SimpleRestaurantPin item, Marker m) {
			mCache.put(item, m);
			mCacheReverse.put(m, item);
		}

		public void remove(Marker m) {
			SimpleRestaurantPin item = mCacheReverse.get(m);
			mCacheReverse.remove(m);
			mCache.remove(item);
		}
	}

	protected boolean shouldRenderAsCluster(Cluster<SimpleRestaurantPin> cluster) {
		return cluster.getSize() > MIN_CLUSTER_SIZE;
	}

	/**
	 * A Marker and its position. Marker.getPosition() must be called from the UI thread, so this
	 * object allows lookup from other threads.
	 */
	private static class MarkerWithPosition {
		private final Marker marker;
		private LatLng position;

		private MarkerWithPosition(Marker marker) {
			this.marker = marker;
			position = marker.getPosition();
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof MarkerWithPosition) {
				return marker.equals(((MarkerWithPosition) other).marker);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return marker.hashCode();
		}
	}

	/**
	 * Called before the marker for a ClusterItem is added to the map.
	 */
	protected void onBeforeClusterItemRendered(SimpleRestaurantPin item, MarkerOptions markerOptions) {
	}

	/**
	 * Called before the marker for a Cluster is added to the map.
	 * The default implementation draws a circle with a rough count of the number of items.
	 */
	protected void onBeforeClusterRendered(Cluster<SimpleRestaurantPin> cluster, MarkerOptions markerOptions) {
	}

	/**
	 * Called after the marker for a Cluster has been added to the map.
	 */
	protected void onClusterRendered(Cluster<SimpleRestaurantPin> cluster, Marker marker) {
	}

	/**
	 * Called after the marker for a ClusterItem has been added to the map.
	 */
	protected void onClusterItemRendered(SimpleRestaurantPin clusterItem, Marker marker) {
	}

}
