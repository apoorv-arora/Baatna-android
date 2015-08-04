package com.application.baatna.mapUtils;

import com.application.baatna.data.User;
import com.google.android.gms.maps.model.LatLng;

public class SimpleRestaurantPin implements ClusterItem{
	
	private User user;
	
	public SimpleRestaurantPin() {
		user = new User();
	}
	
	public void setRestaurant(User r) {
		user = r;
	}
	
	public User getRestaurant() {
		return user;
	}

	@Override
	public LatLng getPosition() {
		return new LatLng(user.getLatitude(), user.getLongitude());
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof SimpleRestaurantPin){
			return ((SimpleRestaurantPin) o).getRestaurant().equals(user);
		}
		
		return super.equals(o);
	}
	
	
	
}
