package com.application.baatna.utils;

public interface UploadManagerCallback {
	
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId);
	
	public void uploadStarted(int requestType, int objectId, String stringId, Object object);
	
}