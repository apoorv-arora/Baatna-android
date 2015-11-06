package com.application.baatna.data;

import java.io.Serializable;

public class Message implements Serializable{

	private User fromUser;
	private User toUser;
	private Wish wish;
	private boolean isGroupChat;
	private boolean fromTo;
	private String message;
	private long messageId;

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public User getFromUser() {
		return fromUser;
	}

	public void setFromUser(User fromUser) {
		this.fromUser = fromUser;
	}

	public User getToUser() {
		return toUser;
	}

	public void setToUser(User toUser) {
		this.toUser = toUser;
	}

	public boolean isGroupChat() {
		return isGroupChat;
	}

	public void setGroupChat(boolean isGroupChat) {
		this.isGroupChat = isGroupChat;
	}

	public boolean isFromTo() {
		return fromTo;
	}

	public void setFromTo(boolean fromTo) {
		this.fromTo = fromTo;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Wish getWish() {
		return wish;
	}

	public void setWish(Wish wish) {
		this.wish = wish;
	}

}
