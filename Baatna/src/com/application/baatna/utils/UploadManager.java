package com.application.baatna.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Debug;
import android.widget.Toast;

import com.application.baatna.BaatnaApp;

public class UploadManager {

	public static Hashtable<Integer, AsyncTask> asyncs = new Hashtable<Integer, AsyncTask>();
	public static Context context;
	private static SharedPreferences prefs;
	private static ArrayList<UploadManagerCallback> callbacks = new ArrayList<UploadManagerCallback>();
	private static BaatnaApp zapp;

	public static void setContext(Context context) {
		UploadManager.context = context;
		prefs = context.getSharedPreferences("application_settings", 0);

		if (context instanceof BaatnaApp) {
			zapp = (BaatnaApp) context;
		}
	}

	public static void addCallback(UploadManagerCallback callback) {
		if (!callbacks.contains(callback)) {
			callbacks.add(callback);
		}

		// this is here because its called from a lot of places.
		if ((double) Debug.getNativeHeapAllocatedSize()
				/ Runtime.getRuntime().maxMemory() > .70) {
			if (zapp != null) {

				if (zapp.cache != null)
					zapp.cache.clear();
			}
		}
	}

	public static void removeCallback(UploadManagerCallback callback) {
		if (callbacks.contains(callback)) {
			callbacks.remove(callback);
		}
	}

	public static void signUp(String name, String email, String password) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.SIGNUP, 0, password, null);
		}

		new SignUp().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { name, email, password});

	}
	
	public static void login(String email, String password) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.LOGIN, 0, password, null);
		}

		new Login().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { email, password});

	}

	public static void logout(String accessToken) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.LOGOUT, 0, accessToken, null);
		}

		new Logout().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { accessToken});

	}
	
	public static void postNewRequest(String accessToken, String title, String description) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.WISH_ADD, 0, accessToken, null);
		}

		new NewRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { accessToken, title, description});

	}
	
	public static void deleteRequest(String accessToken, String wishId) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.WISH_REMOVE, 0, accessToken, null);
		}

		new DeleteRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { accessToken, wishId});

	}
	
	public static void updateRegistrationId(String accessToken, String regId) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.HARDWARE_REGISTER, 0, accessToken, null);
		}

		new UpdateRegistrationId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { accessToken, regId});

	}
	
	public static void updateInstitution(String accessToken, String institutionId, String studentId) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.UPDATE_INSTITUTION, 0, accessToken, null);
		}

		new UpdateInstitutionId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { accessToken, institutionId, studentId});

	}
	
	public static void updateRequestStatus(String accessToken, String wishId, String action) {
		for (UploadManagerCallback callback : callbacks) {
			callback.uploadStarted(CommonLib.WISH_UPDATE_STATUS, 0, accessToken, null);
		}

		new UpdateWishStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				new Object[] { accessToken, wishId, action});

	}

	private static class SignUp extends AsyncTask<Object, Void, Object[]> {

		private String userName;
		private String email;
		private String password;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			userName = (String) params[0];
			email = (String) params[1];
			password = (String) params[2];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("user_name", userName));
			nameValuePairs.add(new BasicNameValuePair("email", email));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "user/signup?", nameValuePairs, PostWrapper.REGISTER, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.SIGNUP, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}

	private static class Login extends AsyncTask<Object, Void, Object[]> {

		private String email;
		private String password;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			email = (String) params[0];
			password = (String) params[1];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("email", email));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "auth/login?", nameValuePairs, PostWrapper.LOGIN, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.LOGIN, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}
	
	private static class Logout extends AsyncTask<Object, Void, Object[]> {

		private String email;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			email = (String) params[0];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", email));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "auth/logout?", nameValuePairs, PostWrapper.LOGOUT, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.LOGOUT, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}


	private static class NewRequest extends AsyncTask<Object, Void, Object[]> {

		private String accessToken;
		private String title;
		private String description;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			accessToken = (String) params[0];
			title = (String) params[1];
			description = (String) params[2];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("title", title));
			nameValuePairs.add(new BasicNameValuePair("description", description));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "wish/add?", nameValuePairs, PostWrapper.WISH_POST, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.WISH_ADD, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}

	private static class DeleteRequest extends AsyncTask<Object, Void, Object[]> {

		private String accessToken;
		private String wishId;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			accessToken = (String) params[0];
			wishId = (String) params[1];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("wishId", wishId));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "wish/delete?", nameValuePairs, PostWrapper.WISH_DELETE, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.WISH_REMOVE, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateWishStatus extends AsyncTask<Object, Void, Object[]> {

		private String accessToken;
		private String wishId;
		private String action;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			accessToken = (String) params[0];
			wishId = (String) params[1];
			action = (String) params[2];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("wishId", wishId));
			nameValuePairs.add(new BasicNameValuePair("action", action));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "wish/update?", nameValuePairs, PostWrapper.WISH_STATUS_UPDATE, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.WISH_UPDATE_STATUS, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}

	private static class UpdateRegistrationId extends AsyncTask<Object, Void, Object[]> {

		private String accessToken;
		private String regId;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			accessToken = (String) params[0];
			regId = (String) params[1];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
//			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
//			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("pushId", regId));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "user/registrationId?", nameValuePairs, PostWrapper.HARDWARE_REGISTER, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.HARDWARE_REGISTER, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}
	
	private static class UpdateInstitutionId extends AsyncTask<Object, Void, Object[]> {

		private String accessToken;
		private String institutionId;
		private String studentId;

		@Override
		protected Object[] doInBackground(Object... params) {

			Object result[] = null;
			accessToken = (String) params[0];
			institutionId = (String) params[1];
			studentId = (String) params[2];
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			nameValuePairs.add(new BasicNameValuePair("institution_id", institutionId));
			nameValuePairs.add(new BasicNameValuePair("student_id", studentId));
			
			try {
				result = PostWrapper.postRequest(CommonLib.SERVER + "user/institution?", nameValuePairs, PostWrapper.INSTITUTION_ID, context);
			} catch (Exception e) {
				e.printStackTrace();
				return result;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

			for (UploadManagerCallback callback : callbacks) {
				callback.uploadFinished(CommonLib.UPDATE_INSTITUTION, prefs.getInt("uid", 0), 0, arg[1], 0, arg[0].equals("success"), "");
			}
		}
	}


}