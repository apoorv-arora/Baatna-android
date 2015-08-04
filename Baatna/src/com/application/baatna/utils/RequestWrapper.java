package com.application.baatna.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

import com.application.baatna.BaatnaApp;
import com.application.baatna.data.Categories;
import com.application.baatna.data.FeedItem;
import com.application.baatna.data.Message;
import com.application.baatna.data.Wish;
import com.google.android.gms.maps.model.LatLng;

public class RequestWrapper {
	private static SharedPreferences prefs;
	private static BaatnaApp zapp;
	
	//cache time
	public static final int FAV = -1;
	public static final int TEMP = 86400;
	public static final int CONSTANT = 1209600;
	public static final int ONE_HOUR = 3600;
	public static final int THREE_HOURS = 3600 * 3;
	
	//contant identifiers
	public static final String USER_MESSAGES = "user_messages";
	public static final String CATEGORIES_LIST = "categories_list";
	public static final String WISH_LIST = "wish_list";
	public static final String INSTITUTIONS_LIST = "institutions_list";
	public static final String NEARBY_USERS = "nearby_users";
	public static final String NEWS_FEED = "news_feed";

	public static void Initialize(Context context) {
		prefs = context.getSharedPreferences("application_settings", 0);
	}
	
	public static InputStream fetchhttp(String urlstring) {

		String value = null;
		try {

			CommonLib.ZLog("RW url", urlstring + ".");
			HttpPost httpPost = new HttpPost(urlstring);
			httpPost.addHeader(new BasicHeader("access_token", prefs.getString("access_token", "")));
			httpPost.addHeader(new BasicHeader("client_id", CommonLib.CLIENT_ID));
			httpPost.addHeader(new BasicHeader("app_type", CommonLib.APP_TYPE));
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
			
			nameValuePairs.add(new BasicNameValuePair("latitude", prefs.getString("latitude", "0")));
			nameValuePairs.add(new BasicNameValuePair("longitude", prefs.getString("longitude", "0")));
			
			if (nameValuePairs != null)
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			CommonLib.ZLog("AccessToken: ", prefs.getString("access_token", ""));

//			if (CommonLib.isTestBuild)
//				httpPost.addHeader(new BasicHeader("Authorization", "Basic emRldjpvSnU0Rm9oY2hvb20zY2hhPWcmbw==")); // ZDEV (new)
//			else
//				httpPost.addHeader(new BasicHeader("Accept-Encoding", "gzip"));

			long timeBeforeApiCall = System.currentTimeMillis();
			HttpResponse response = HttpManager.execute(httpPost);
			CommonLib.ZLog("fetchhttp(); Response Time: ", System.currentTimeMillis() - timeBeforeApiCall);

			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = CommonLib.getStream(response);
				return in;

			} else {
				CommonLib.ZLog("fetchhttp(); Response Code: ", responseCode + "-------" + urlstring);
			}
		} catch (Exception e) {
			CommonLib.ZLog("Error fetching http url", e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object RequestHttp(String url, String Object_Type, int status) {
		Object o = null;
		InputStream http_result;

		http_result = fetchhttp(url);		
		o = parse(http_result, Object_Type);
		return o;
	}
	
	public static Object parse(InputStream result, String Type) {

		Object o = null;

		if (Type == CATEGORIES_LIST) {
			ArrayList<Categories> categories = null;
			try{
				categories = (ArrayList<Categories>) ParserJson.parse_Categories(result);
			} catch(JSONException e) {
				e.printStackTrace();
			}
			return categories;
		} else if(Type == WISH_LIST) {
			ArrayList<Wish> categories = null;
			try{
				categories = (ArrayList<Wish>) ParserJson.parse_Wishes(result);
			} catch(JSONException e) {
				e.printStackTrace();
			}
			return categories;
		} else if(Type == INSTITUTIONS_LIST) {
			ArrayList<String> categories = null;
			try{
				categories = (ArrayList<String>) ParserJson.parse_Institutions(result);
			} catch(JSONException e) {
				e.printStackTrace();
			}
			return categories;
		} else if(Type == NEARBY_USERS) {
			ArrayList<LatLng> categories = null;
			try{
				categories = (ArrayList<LatLng>) ParserJson.parse_NearbyUsers(result);
			} catch(JSONException e) {
				e.printStackTrace();
			}
			return categories;
		} else if(Type == NEWS_FEED) {
			ArrayList<FeedItem> feedItems = null;
			try{
				feedItems = (ArrayList<FeedItem>) ParserJson.parse_NewsFeedResponse(result);
			} catch(JSONException e) {
				e.printStackTrace();
			}
			return feedItems;
		} 
		return o;
	}
	
	public static byte[] Serialize_Object(Object O) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(O);
		out.close();

		// Get the bytes of the serialized object
		byte[] buf = bos.toByteArray();
		return buf;
	}

	public static Object Deserialize_Object(byte[] input, String Type) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(input));

		if (Type == USER_MESSAGES) {
			Message result = (Message) in.readObject();
			in.close();
			return result;
		} else {
			in.close();
			return null;
		}
		
	}

}
