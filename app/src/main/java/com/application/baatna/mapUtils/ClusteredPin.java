package com.application.baatna.mapUtils;

public class ClusteredPin {

	private int count = 0;
	private double lat;
	private double lon;
	private String resIds;
	
	public void setCount(int count) {
		this.count = count;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public int getCount() {
		return count;
	}
	public double getLat() {
		return lat;
	}
	public double getLon() {
		return lon;
	}
	public String getResIds() {
		return resIds;
	}
	public void setResIds(String resIds) {
		this.resIds = resIds;
	}
	
}
