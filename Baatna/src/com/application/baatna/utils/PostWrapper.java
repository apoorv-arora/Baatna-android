package com.application.baatna.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.application.baatna.R;
import com.application.baatna.data.User;
import com.crashlytics.android.Crashlytics;

public class PostWrapper {

	private static SharedPreferences prefs;

	/** Constants */
	public static String REGISTER = "register";
	public static String LOGOUT = "logout";
	public static String LOGIN = "register";
	public static String WISH_POST = "wish_post";
	public static String WISH_DELETE = "wish_delete";
	public static String HARDWARE_REGISTER = "hardware_register";
	public static String INSTITUTION_ID = "update_institution_id";
	public static String WISH_STATUS_UPDATE = "wish_status_update";
	public static String SEND_MESSAGE = "send_message";
	public static String LOCATION_UPDATE = "update_location";

	public static void Initialize(Context context) {
		// helper = new ResponseCacheManager(context);
		prefs = context.getSharedPreferences("application_settings", 0);
	}

	public static String convertStreamToString(java.io.InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	public static Object[] postRequest(String Url,
			List<NameValuePair> nameValuePairs, String type, Context appContext) {

		Object[] resp = new Object[] {
				"failed",
				appContext.getResources().getString(R.string.could_not_connect),
				new User() };

		try {

			HttpResponse response = getPostResponse(Url, nameValuePairs,
					appContext);
			int responseCode = response.getStatusLine().getStatusCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream is = CommonLib.getStream(response);
				if (type.equals(REGISTER)) {
					resp = ParserJson.parseSignupResponse(is);
				} else if(type.equals(LOGIN)) {
					resp = ParserJson.parseLoginResponse(is);
				} else if(type.equals(LOGOUT)) {
					resp = ParserJson.parseLogoutResponse(is);
				} else if(type.equals(WISH_POST)) {
					resp = ParserJson.parseWishPostResponse(is);
				} else if(type.equals(WISH_DELETE)) {
					resp = ParserJson.parseWishDeletePostResponse(is);
				} else if(type.equals(INSTITUTION_ID)) {
					resp = ParserJson.parseInstitutionResponse(is);
				}

			} 
//			else {
//				logErrorResponse(url, response);
//			}

		} catch (Exception E) {
			E.printStackTrace();
			return resp;
		}
		return resp;
	}

	public static HttpResponse getPostResponse(String Url,
			List<NameValuePair> nameValuePairs, Context appContext)
			throws Exception {

		HttpPost httpPost = new HttpPost(Url
				+ CommonLib.getVersionString(appContext));
		// httpPost.addHeader(new BasicHeader("X-Zomato-API-Key",
		// CommonLib.APIKEY));
		// httpPost.addHeader(new BasicHeader("X-Access-Token",
		// prefs.getString("access_token", "")));
		httpPost.addHeader(new BasicHeader("client_id", CommonLib.CLIENT_ID));
		httpPost.addHeader(new BasicHeader("app_type", CommonLib.APP_TYPE));
		// if (CommonLib.isTestBuild)
		// httpPost.addHeader(new BasicHeader("Authorization",
		// "Basic emRldjpvSnU0Rm9oY2hvb20zY2hhPWcmbw==")); // ZDEV(new)
		// else
		// httpPost.addHeader(new BasicHeader("Accept-Encoding", "gzip"));

		if (nameValuePairs != null)
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

		return HttpManager.execute(httpPost);
	}

}
