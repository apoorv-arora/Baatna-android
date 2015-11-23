package com.application.baatna.data;

import java.io.Serializable;
import java.util.ArrayList;

public class FriendsCollection implements Serializable {

	ArrayList<User> notFollowing;
	ArrayList<User> following;
	ArrayList<User> notSignedUp;

	String friendsCountString = "";

	public String getFriendsCountString() {
		return friendsCountString;
	}

	public void setFriendsCountString(String friendsCountString) {
		this.friendsCountString = friendsCountString;
	}

	public ArrayList<User> getNotFollowing() {
		return notFollowing;
	}

	public void setNotFollowing(ArrayList<User> notFollowing) {
		this.notFollowing = notFollowing;
	}

	public ArrayList<User> getFollowing() {
		return following;
	}

	public void setFollowing(ArrayList<User> following) {
		this.following = following;
	}

	public ArrayList<User> getNotSignedUp() {
		return notSignedUp;
	}

	public void setNotSignedUp(ArrayList<User> notSignedUp) {
		this.notSignedUp = notSignedUp;
	}

}