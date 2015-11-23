package com.application.baatna.mapUtils;

import java.util.Collection;

import com.google.android.gms.maps.model.LatLng;

/**
 * A collection of ClusterItems that are nearby each other.
 */
public interface Cluster<T extends ClusterItem> {
    public LatLng getPosition();

    Collection<T> getItems();

    int getSize();
}