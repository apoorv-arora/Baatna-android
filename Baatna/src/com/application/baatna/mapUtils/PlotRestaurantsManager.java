package com.application.baatna.mapUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.application.baatna.data.User;
import com.application.baatna.utils.CommonLib;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlotRestaurantsManager {

	private ArrayList<User> restaurantList;
	private GoogleMap map;
	private Context mContext;
    private Algorithm<SimpleRestaurantPin> mAlgorithm;
    
    private final ReadWriteLock mAlgorithmLock = new ReentrantReadWriteLock();
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();
    private ClusterTask mClusterTask;

	public PlotRestaurantsManager(ArrayList<User> restList, Context c, GoogleMap map) {
		this.map = map;
		this.restaurantList = restList;
		this.mContext = c;
		
        mAlgorithm = new PreCachingAlgorithmDecorator<SimpleRestaurantPin>(new NonHierarchicalDistanceBasedAlgorithm<SimpleRestaurantPin>());
        mClusterTask = new ClusterTask();
	}
	
	public void setRestaurantList(ArrayList<User> resList){
		this.restaurantList = resList;
	}
	
	public void plotRestaurantsOnMap(){
		
		ArrayList<SimpleRestaurantPin> pins = new ArrayList<SimpleRestaurantPin>();
		for (User r : restaurantList){
			SimpleRestaurantPin pin = new SimpleRestaurantPin();
			pin.setRestaurant(r);
			pins.add(pin);
		}
		
		addItems(pins);
		cluster();
	}
	
	  public void addItems(Collection<SimpleRestaurantPin> items) {
	        mAlgorithmLock.writeLock().lock();
	        try {
	            mAlgorithm.addItems(items);
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
                mClusterTask.execute(map.getCameraPosition().zoom);
            } else {
                mClusterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map.getCameraPosition().zoom);
            }
        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
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
            //mRenderer.onClustersChanged(clusters);
        	for (Cluster<SimpleRestaurantPin> c : clusters){
        		
        		map.addMarker(new MarkerOptions()
    			.title(c.getSize() + "")
    			.position(new LatLng(c.getPosition().latitude, c.getPosition().longitude)));
        		
        		CommonLib.ZLog("PlotRestaurantonMap", "Cluster Size: " + c.getSize());
        	}
        }
    }
    
    public void clearItems() {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.clearItems();
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }
}
