package com.application.baatna.mapUtils;

import java.util.Collection;
import java.util.Set;

/**
 * Logic for computing clusters
 */

public interface Algorithm<T extends ClusterItem> {
	
	void setMaxZoomDistance(int distance);
	
    void addItem(T item);

    void addItems(Collection<T> items);

    void clearItems();

    void removeItem(T item);

    Set<? extends Cluster<T>> getClusters(double zoom);

    Collection<T> getItems();
    
}
