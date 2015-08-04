package com.application.baatna.mapUtils;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

public class ClusterManager implements GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
    private final GoogleMarkerManager mMarkerManager;
    private final GoogleMarkerManager.Collection mMarkers;
    private final GoogleMarkerManager.Collection mClusterMarkers;

    private Algorithm<SimpleRestaurantPin> mAlgorithm;
    private final ReadWriteLock mAlgorithmLock = new ReentrantReadWriteLock();
    private ClusterRenderer<SimpleRestaurantPin> mRenderer;

    private GoogleMap mMap;
    private CameraPosition mPreviousCameraPosition;
    private ClusterTask mClusterTask;
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();

    private OnClusterItemClickListener<SimpleRestaurantPin> mOnClusterItemClickListener;
    private OnClusterInfoWindowClickListener<SimpleRestaurantPin> mOnClusterInfoWindowClickListener;
    private OnClusterItemInfoWindowClickListener<SimpleRestaurantPin> mOnClusterItemInfoWindowClickListener;
    private OnClusterClickListener<SimpleRestaurantPin> mOnClusterClickListener;
    
    private int distance = 0;
    
    public ClusterManager(Context context, GoogleMap map) {
        this(context, map, new GoogleMarkerManager(map));
        //distance = (int) context.getResources().getDimension(R.dimen.height35);
        distance = 28;
    }

    public ClusterManager(Context context, GoogleMap map, GoogleMarkerManager markerManager) {
        mMap = map;
        mMarkerManager = markerManager;
        mClusterMarkers = markerManager.newCollection();
        mMarkers = markerManager.newCollection();
        mRenderer = new GoogleMapRenderer(map, this);
        mAlgorithm = new PreCachingAlgorithmDecorator<SimpleRestaurantPin>(new NonHierarchicalDistanceBasedAlgorithm<SimpleRestaurantPin>());
//        distance = (int) context.getResources().getDimension(R.dimen.height55);
        distance = 28;
        mAlgorithm.setMaxZoomDistance(distance);
        mClusterTask = new ClusterTask();
        mRenderer.onAdd();
    }

    public GoogleMarkerManager.Collection getMarkerCollection() {
        return mMarkers;
    }

    public GoogleMarkerManager.Collection getClusterMarkerCollection() {
        return mClusterMarkers;
    }

    public GoogleMarkerManager getMarkerManager() {
        return mMarkerManager;
    }

    public void setRenderer(ClusterRenderer<SimpleRestaurantPin> view) {
        mRenderer.setOnClusterClickListener(null);
        mRenderer.setOnClusterItemClickListener(null);
        mClusterMarkers.clear();
        mMarkers.clear();
        mRenderer.onRemove();
        mRenderer = view;
        mRenderer.onAdd();
        mRenderer.setOnClusterClickListener(mOnClusterClickListener);
        mRenderer.setOnClusterInfoWindowClickListener(mOnClusterInfoWindowClickListener);
        mRenderer.setOnClusterItemClickListener(mOnClusterItemClickListener);
        mRenderer.setOnClusterItemInfoWindowClickListener(mOnClusterItemInfoWindowClickListener);
        cluster();
    }

    public void setAlgorithm(Algorithm<SimpleRestaurantPin> algorithm) {
        mAlgorithmLock.writeLock().lock();
        try {
            if (mAlgorithm != null) {
                algorithm.addItems(mAlgorithm.getItems());
            }
            mAlgorithm = new PreCachingAlgorithmDecorator<SimpleRestaurantPin>(algorithm);
            if (distance != 0)
            	mAlgorithm.setMaxZoomDistance(distance);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
        cluster();
    }

    public void clearItems() {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.clearItems();
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    public void addItems(Collection<SimpleRestaurantPin> items) {
        mAlgorithmLock.writeLock().lock();
        try {
        	mAlgorithm.addItems(items);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }
    
    public void addItem(SimpleRestaurantPin myItem) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.addItem(myItem);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    public void removeItem(SimpleRestaurantPin item) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.removeItem(item);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    /**
     * Force a re-cluster. You may want to call this after adding new item(s).
     */
    public void cluster() {
        mClusterTaskLock.writeLock().lock();
        try {
            // Attempt to cancel the in-flight request.
            mClusterTask.cancel(true);
            mClusterTask = new ClusterTask();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mClusterTask.execute(mMap.getCameraPosition().zoom);
            } else {
                mClusterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMap.getCameraPosition().zoom);
            }
        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
    }

    /**
     * Might re-cluster.
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mRenderer instanceof GoogleMap.OnCameraChangeListener) {
            ((GoogleMap.OnCameraChangeListener) mRenderer).onCameraChange(cameraPosition);
        }

        // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        CameraPosition position = mMap.getCameraPosition();
        if (mPreviousCameraPosition != null ) {
            
        	int z1 = (int) mPreviousCameraPosition.zoom;
        	int z2 = (int) position.zoom;

        	if (z1 == z2)
        		return;
        }
        mPreviousCameraPosition = mMap.getCameraPosition();
        cluster();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return getMarkerManager().onMarkerClick(marker);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getMarkerManager().onInfoWindowClick(marker);
    }
    
    /**
     * Runs the clustering algorithm in a background thread, then re-paints when results come back.
     */
    private class ClusterTask extends AsyncTask<Float, Void, Set<? extends Cluster<SimpleRestaurantPin>>> {
        @Override
        protected Set<? extends Cluster<SimpleRestaurantPin>> doInBackground(Float... zoom) {
            mAlgorithmLock.readLock().lock();
            try {
                return mAlgorithm.getClusters(zoom[0]);
            } finally {
                mAlgorithmLock.readLock().unlock();
            }
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<SimpleRestaurantPin>> clusters) {
        	mRenderer.onClustersChanged(clusters);
        }
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a click listener to the map.
     */
    public void setOnClusterClickListener(OnClusterClickListener<SimpleRestaurantPin> listener) {
        mOnClusterClickListener = listener;
        mRenderer.setOnClusterClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a info window click listener to the map.
     */
    public void setOnClusterInfoWindowClickListener(OnClusterInfoWindowClickListener<SimpleRestaurantPin> listener) {
        mOnClusterInfoWindowClickListener = listener;
        mRenderer.setOnClusterInfoWindowClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a click listener to the map.
     */
    public void setOnClusterItemClickListener(OnClusterItemClickListener<SimpleRestaurantPin> listener) {
        mOnClusterItemClickListener = listener;
        mRenderer.setOnClusterItemClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem's Info Window is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a info window click listener to the map.
     */
    public void setOnClusterItemInfoWindowClickListener(OnClusterItemInfoWindowClickListener<SimpleRestaurantPin> listener) {
        mOnClusterItemInfoWindowClickListener = listener;
        mRenderer.setOnClusterItemInfoWindowClickListener(listener);
    }

    /**
     * Called when a Cluster is clicked.
     */
    public interface OnClusterClickListener<T extends ClusterItem> {
        public boolean onClusterClick(Cluster<T> cluster);
    }

    /**
     * Called when a Cluster's Info Window is clicked.
     */
    public interface OnClusterInfoWindowClickListener<T extends ClusterItem> {
        public void onClusterInfoWindowClick(Cluster<T> cluster);
    }

    /**
     * Called when an individual ClusterItem is clicked.
     */
    public interface OnClusterItemClickListener<T extends ClusterItem> {
        public boolean onClusterItemClick(T item);
    }

    /**
     * Called when an individual ClusterItem's Info Window is clicked.
     */
    public interface OnClusterItemInfoWindowClickListener<T extends ClusterItem> {
        public void onClusterItemInfoWindowClick(T item);
    }
}
