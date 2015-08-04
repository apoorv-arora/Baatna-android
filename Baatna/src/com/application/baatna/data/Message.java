package com.application.baatna.data;

public class Message {

	private User fromUser;
	private User toUser;
	private boolean isGroupChat;
	private boolean fromTo;

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

}
