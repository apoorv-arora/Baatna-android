package com.application.baatna.utils;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Parser;

import com.application.baatna.data.Categories;
import com.application.baatna.data.FeedItem;
import com.application.baatna.data.Institution;
import com.application.baatna.data.Message;
import com.application.baatna.data.User;
import com.application.baatna.data.UserComactMessage;
import com.application.baatna.data.Wish;
import com.google.android.gms.maps.model.LatLng;

public class ParserJson {

	@SuppressWarnings("resource")
	public static JSONObject convertInputStreamToJSON(InputStream is) throws JSONException {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		String responseJSON = s.hasNext() ? s.next() : "";

		CommonLib.ZLog("response", responseJSON);
		JSONObject map = new JSONObject(responseJSON);
		CommonLib.ZLog("RESPONSE", map.toString(2));
		return map;
	}

	public static Object[] parseSignupResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseInstitutionResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseSendMessageResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject object = responseObject.getJSONObject("response");
					output[1] = parse_Message(object);
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseLoginResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static JSONObject parseFBLoginResponse(InputStream is) throws JSONException {

		JSONObject output = new JSONObject();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			String out = responseObject.getString("status");
			if (out.equals("success")) {
				if (responseObject.has("response")) {
					output = new JSONObject(String.valueOf(responseObject.get("response")));
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output.put("error", responseObject.getString("errorMessage"));
				}
			}
		}
		return output;
	}

	public static Object[] parseLogoutResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseWishPostResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseWishDeletePostResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static ArrayList<Categories> parse_Categories(InputStream is) throws JSONException {

		ArrayList<Categories> categories = new ArrayList<Categories>();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject categoriesObject = responseObject.getJSONObject("response");
					if (categoriesObject.has("categories") && categoriesObject.get("categories") instanceof JSONArray) {
						JSONArray categoriesArr = categoriesObject.getJSONArray("categories");
						for (int i = 0; i < categoriesArr.length(); i++) {
							Categories category = new Categories();
							JSONObject categoryJson = categoriesArr.getJSONObject(i);
							if (categoryJson.has("category") && categoryJson.get("category") instanceof JSONObject) {
								categoryJson = categoryJson.getJSONObject("category");
								if (categoryJson.has("category_id")
										&& categoryJson.get("category_id") instanceof Integer)
									category.setCategoryId(categoryJson.getInt("category_id"));
								if (categoryJson.has("category_name"))
									category.setCategory(String.valueOf(categoryJson.get("category_name")));
								if (categoryJson.has("category_icon"))
									category.setCategoryIcon(String.valueOf(categoryJson.get("category_icon")));
								categories.add(category);
							}
						}
					}
				}
			}
		}
		return categories;
	}

	public static Object[] parse_Wishes(InputStream is) throws JSONException {

		Object[] objects = new Object[2];
		ArrayList<Wish> wishes = new ArrayList<Wish>();
		int size = 0;
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject categoriesObject = responseObject.getJSONObject("response");
					if (categoriesObject.has("total") && categoriesObject.get("total") instanceof Integer)
						size = categoriesObject.getInt("total");
					if (categoriesObject.has("wishes") && categoriesObject.get("wishes") instanceof JSONArray) {
						JSONArray categoriesArr = categoriesObject.getJSONArray("wishes");
						for (int i = 0; i < categoriesArr.length(); i++) {
							Wish category = new Wish();
							JSONObject categoryJson = categoriesArr.getJSONObject(i);
							if (categoryJson.has("wish") && categoryJson.get("wish") instanceof JSONObject) {
								categoryJson = categoryJson.getJSONObject("wish");
								if (categoryJson.has("title"))
									category.setTitle(String.valueOf(categoryJson.get("title")));
								if (categoryJson.has("description"))
									category.setDescription(String.valueOf(categoryJson.get("description")));
								if (categoryJson.has("time_post") && categoryJson.get("time_post") instanceof Long)
									category.setTimeOfPost(categoryJson.getLong("time_post"));
								if (categoryJson.has("user_id") && categoryJson.get("user_id") instanceof Integer)
									category.setUserId(categoryJson.getInt("user_id"));
								if (categoryJson.has("wish_id") && categoryJson.get("wish_id") instanceof Integer)
									category.setWishId(categoryJson.getInt("wish_id"));
								wishes.add(category);
							}
						}
					}
				}
			}
		}
		objects[0] = size;
		objects[1] = wishes;
		return objects;
	}

	public static ArrayList<Institution> parse_Institutions(InputStream is) throws JSONException {

		ArrayList<Institution> wishes = new ArrayList<Institution>();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject categoriesObject = responseObject.getJSONObject("response");
					if (categoriesObject.has("institutions")
							&& categoriesObject.get("institutions") instanceof JSONArray) {
						JSONArray categoriesArr = categoriesObject.getJSONArray("institutions");
						for (int i = 0; i < categoriesArr.length(); i++) {
							Institution institution = new Institution();
							JSONObject categoryJson = categoriesArr.getJSONObject(i);
							if (categoryJson.has("institution")
									&& categoryJson.get("institution") instanceof JSONObject) {
								categoryJson = categoryJson.getJSONObject("institution");
								if (categoryJson.has("name")) {
									institution.setInstitutionName(String.valueOf(categoryJson.get("name")));
								}
								if (categoryJson.has("branches") && categoryJson.get("branches") instanceof JSONArray) {
									JSONArray branchArr = categoryJson.getJSONArray("branches");
									ArrayList<String> branches = new ArrayList<String>();
									for(int j=0 ;j<branchArr.length();j++) {
										branches.add( String.valueOf(branchArr.get(j)) );
									}
									institution.setBranches(branches);
								}
								wishes.add(institution);
							}
						}
					}
				}
			}
		}
		return wishes;
	}

	public static ArrayList<User> parse_NearbyUsers(InputStream is) throws JSONException {

		ArrayList<User> wishes = new ArrayList<User>();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject categoriesObject = responseObject.getJSONObject("response");
					if (categoriesObject.has("users") && categoriesObject.get("users") instanceof JSONArray) {
						JSONArray categoriesArr = categoriesObject.getJSONArray("users");
						for (int i = 0; i < categoriesArr.length(); i++) {
							JSONObject categoryJson = categoriesArr.getJSONObject(i);
							if (categoryJson.has("session") && categoryJson.get("session") instanceof JSONObject) {
								categoryJson = categoryJson.getJSONObject("session");
								double latitude = 0, longitude = 0;
								if (categoryJson.has("latitude") && categoryJson.get("latitude") instanceof Double) {
									latitude = categoryJson.getDouble("latitude");
								}
								if (categoryJson.has("longitude") && categoryJson.get("longitude") instanceof Double) {
									longitude = categoryJson.getDouble("longitude");
								}
								User location = new User();
								location.setLatitude(latitude);
								location.setLongitude(longitude);
								wishes.add(location);
							}
						}
					}
				}
			}
		}
		return wishes;
	}

	public static Wish parse_Wish(JSONObject wishObject) {
		if (wishObject == null)
			return null;

		Wish wish = new Wish();

		try {
			if (wishObject.has("title"))
				wish.setTitle(String.valueOf(wishObject.get("title")));

			if (wishObject.has("description"))
				wish.setDescription(String.valueOf(wishObject.get("description")));
			if (wishObject.has("time_post") && wishObject.get("time_post") instanceof Long)
				wish.setTimeOfPost(wishObject.getLong("time_post"));
			if (wishObject.has("user_id") && wishObject.get("user_id") instanceof Integer)
				wish.setUserId(wishObject.getInt("user_id"));
			if (wishObject.has("wish_id") && wishObject.get("wish_id") instanceof Integer)
				wish.setWishId(wishObject.getInt("wish_id"));
			if (wishObject.has("status") && wishObject.get("status") instanceof Integer)
				wish.setStatus(wishObject.getInt("status"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return wish;
	}

	public static User parse_User(JSONObject userObject) {
		if (userObject == null)
			return null;

		User returnUser = new User();
		try {
			if (userObject.has("user_id") && userObject.get("user_id") instanceof Integer) {
				returnUser.setUserId(userObject.getInt("user_id"));
			}

			if (userObject.has("email")) {
				returnUser.setEmail(String.valueOf(userObject.get("email")));
			}

			if (userObject.has("profile_pic")) {
				returnUser.setImageUrl(String.valueOf(userObject.get("profile_pic")));
			}

			if (userObject.has("user_name")) {
				returnUser.setUserName(String.valueOf(userObject.get("user_name")));
			}
			
			if (userObject.has("fbId")) {
				returnUser.setFbId(String.valueOf(userObject.get("fbId")));
			}
			
			if (userObject.has("contact")) {
				returnUser.setContact(String.valueOf(userObject.get("contact")));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return returnUser;
	}

	public static Message parse_Message(JSONObject messageObject) {
		if (messageObject == null)
			return null;

		Message returnMessage = new Message();
		try {
			if (messageObject.has("to_user") && messageObject.get("to_user") instanceof JSONObject) {
				JSONObject object = messageObject.getJSONObject("to_user");
				if (object.has("user")) {
					returnMessage.setToUser(parse_User(object.getJSONObject("user")));
				}
			}

			if (messageObject.has("from_user") && messageObject.get("from_user") instanceof JSONObject) {
				JSONObject object = messageObject.getJSONObject("from_user");
				if (object.has("user")) {
					returnMessage.setFromUser(parse_User(object.getJSONObject("user")));
				}
			}

			if (messageObject.has("message_id") && messageObject.get("message_id") instanceof Integer) {
				returnMessage.setMessageId(messageObject.getInt("message_id"));
			}

			if (messageObject.has("from_to") && messageObject.get("from_to") instanceof Boolean) {
				returnMessage.setFromTo(messageObject.getBoolean("from_to"));
			}

			if (messageObject.has("message")) {
				returnMessage.setMessage(String.valueOf(messageObject.get("message")));
			}

			if (messageObject.has("wish")) {
				JSONObject object = messageObject.getJSONObject("wish");
				if (object.has("wish"))
					returnMessage.setWish(parse_Wish(object.getJSONObject("wish")));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return returnMessage;
	}

	public static Object[] parse_NewsFeedResponse(InputStream is) throws JSONException {
		Object[] response = new Object[2];
		ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {

			if (responseObject.getString("status").equals("success")) {

				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {

					JSONObject newsFeedObject = responseObject.getJSONObject("response");
					if (newsFeedObject.has("total") && newsFeedObject.get("total") instanceof Integer)
						response[0] = newsFeedObject.getInt("total");
					if (newsFeedObject.has("newsFeed") && newsFeedObject.get("newsFeed") instanceof JSONArray) {
						JSONArray categoriesArr = newsFeedObject.getJSONArray("newsFeed");

						for (int i = 0; i < categoriesArr.length(); i++) {

							JSONObject categoryJson = categoriesArr.getJSONObject(i);

							FeedItem feedItem = new FeedItem();

							if (categoryJson.has("userFirst") && categoryJson.get("userFirst") instanceof JSONObject) {

								JSONObject userFirstJSONObject = new JSONObject();
								userFirstJSONObject = categoryJson.getJSONObject("userFirst");

								if (userFirstJSONObject.has("user")
										&& userFirstJSONObject.get("user") instanceof JSONObject) {
									User userFirst = parse_User(userFirstJSONObject.getJSONObject("user"));
									feedItem.setUserFirst(userFirst);
								}
							}

							if (categoryJson.has("userSecond")
									&& categoryJson.get("userSecond") instanceof JSONObject) {

								JSONObject userSecondJSONObject = new JSONObject();
								userSecondJSONObject = categoryJson.getJSONObject("userSecond");

								if (userSecondJSONObject.has("user")
										&& userSecondJSONObject.get("user") instanceof JSONObject) {
									User userSecond = parse_User(userSecondJSONObject.getJSONObject("user"));
									feedItem.setUserSecond(userSecond);
								}
							}

							if (categoryJson.has("type") && categoryJson.get("type") instanceof Integer) {
								feedItem.setType(categoryJson.getInt("type"));
							}

							if (categoryJson.has("latitude") && categoryJson.get("latitude") instanceof Double) {
								feedItem.setLatitude(categoryJson.getDouble("latitude"));
							}

							if (categoryJson.has("longitude") && categoryJson.get("longitude") instanceof Double) {
								feedItem.setLongitude(categoryJson.getDouble("longitude"));
							}

							if (categoryJson.has("wish") && categoryJson.get("wish") instanceof JSONObject) {

								JSONObject userSecondJSONObject = new JSONObject();
								userSecondJSONObject = categoryJson.getJSONObject("wish");

								if (userSecondJSONObject.has("wish")
										&& userSecondJSONObject.get("wish") instanceof JSONObject) {
									Wish wish = parse_Wish(userSecondJSONObject.getJSONObject("wish"));
									feedItem.setWish(wish);
								}
							}

							feedItems.add(feedItem);
						}
					}
				}
			}
		}
		response[1] = feedItems;
		return response;
	}

	public static User parse_UserResponse(InputStream is) throws JSONException {
		User response = new User();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {

			if (responseObject.getString("status").equals("success")) {

				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {

					JSONObject responseObj = responseObject.getJSONObject("response");
					if (responseObj != null && responseObj.has("user")
							&& responseObj.get("user") instanceof JSONObject) {
						response = ParserJson.parse_User(responseObj.getJSONObject("user"));
					}
				}
			}
		}
		return response;
	}

	public static ArrayList<UserComactMessage> parse_UserCompactMessageResponse(InputStream is) throws JSONException {
		ArrayList<UserComactMessage> response = new ArrayList<UserComactMessage>();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {

			if (responseObject.getString("status").equals("success")) {

				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {

					JSONObject responseObj = responseObject.getJSONObject("response");
					if (responseObj != null && responseObj.has("messages")
							&& responseObj.get("messages") instanceof JSONArray) {
						JSONArray messagesArr = new JSONArray();
						messagesArr = responseObj.getJSONArray("messages");
						for (int i = 0; i < messagesArr.length(); i++) {
							JSONObject object = messagesArr.getJSONObject(i);
							if (object.has("message")) {
								UserComactMessage messageObj = new UserComactMessage();
								object = object.getJSONObject("message");
								if (object.has("wish"))
									if (object.getJSONObject("wish").has("wish"))
										messageObj.setWish(
												parse_Wish(object.getJSONObject("wish").getJSONObject("wish")));
								if (object.has("user")) {
									if (object.getJSONObject("user").has("user"))
										messageObj.setUser(
												parse_User(object.getJSONObject("user").getJSONObject("user")));
								}
								if (object.has("type") && object.get("type") instanceof Integer)
									messageObj.setType(object.getInt("type"));
								response.add(messageObj);
							}
						}
					}
				}
			}
		}
		return response;
	}
}
