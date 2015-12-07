package com.application.baatna.data;

import java.io.Serializable;

public class FeedItem implements Serializable{

	private int feedId;
	/**
	 * Type 0: User joined near you Type 1: User X posted a new request of wish
	 * item Y Type 2: User X gave Y to user Z
	 * */
	private int type;
	/**
	 * Time when the item is posted
	 * */
	private long timestamp;
	/**
	 * User X
	 * */
	private User userFirst;
	/**
	 * User Z
	 * */
	private User userSecond;
	/**
	 * Wish Y
	 * */
	private Wish wish;

	/**
	 * Location of the feed item
	 * */
	private double latitude;
	private double longitude;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public User getUserIdFirst() {
		return userFirst;
	}

	public void setUserFirst(User userFirst) {
		this.userFirst = userFirst;
	}

	public User getUserSecond() {
		return userSecond;
	}

	public void setUserSecond(User userSecond) {
		this.userSecond = userSecond;
	}

	public Wish getWish() {
		return wish;
	}

	public void setWish(Wish wish) {
		this.wish = wish;
	}

	public int getFeedId() {
		return feedId;
	}

	public void setFeedId(int feedId) {
		this.feedId = feedId;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
