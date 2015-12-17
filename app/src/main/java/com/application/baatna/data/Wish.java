package com.application.baatna.data;

import java.io.Serializable;

public class Wish implements Serializable {

	private String title;
	private String description;
	private long timeOfPost;
	private int wishId;
	private int userId;
	private int status;
	private double latitude;
	private double longitude;
	private int requiredFor;

	public Wish() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getTimeOfPost() {
		return timeOfPost;
	}

	public void setTimeOfPost(long timeOfPost) {
		this.timeOfPost = timeOfPost;
	}

	public int getWishId() {
		return wishId;
	}

	public void setWishId(int wishId) {
		this.wishId = wishId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public int getRequiredFor() {
		return requiredFor;
	}

	public void setRequiredFor(int requiredFor) {
		this.requiredFor = requiredFor;
	}
}
