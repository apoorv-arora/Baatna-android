package com.application.baatna.data;

public class UserComactMessage {
	
	/**
	 * Group Id helps in fetching the chat for this particular group.
	 */
	int groupId;

	/**
	 * Last Message.
	 */
	String lastMessage;

	/**
	 * Timestamp of the last message.
	 */
	long timestamp;

	/**
	 * Current user can chat to this user.
	 */
	User user;

	/**
	 * Type = 1 signifies the current user has accepted a wish for the user Type
	 * = 2 signifies the user has accepted a wish for the current user.
	 */
	int type;

	/**
	 * Wish signifies the wish in picture.
	 */
	Wish wish;

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Wish getWish() {
		return wish;
	}

	public void setWish(Wish wish) {
		this.wish = wish;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
