package com.application.baatna.utils.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FriendChecker {

	private static final String FACEBOOK_HOST = "https://graph.facebook.com/v2.2/";

	private static final String CHECK_FRIEND_API = "friends/";

	public static boolean isFriendOnFacebook(String userA, String userB, String accessToken) {
		String url = FACEBOOK_HOST + userA + "/" + CHECK_FRIEND_API + userB + "/?access_token=" + accessToken;

		URL obj;
		try {
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());

			JSONObject responseJson = new JSONObject(response.toString());
			if (responseJson.has("data") && responseJson.get("data") instanceof JSONArray
					&& responseJson.getJSONArray("data").length() > 0)
				return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}

}
