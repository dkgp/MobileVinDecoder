package com.dkgp.vehicles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class InventoryService {
	private final int _timeout = 3000;
	private Context _context;
	private HttpClient _httpClient;
	private String _apiUrl;
	private SharedPreferences _sharedPref;
	private String _inventoryOwner;

	// constructor
	public InventoryService(Context c) {
		_context = c;
		initialize();

	}

	private void initialize() {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, _timeout);
		HttpConnectionParams.setSoTimeout(httpParameters, _timeout);

		_httpClient = new DefaultHttpClient(httpParameters);

		_sharedPref = PreferenceManager.getDefaultSharedPreferences(_context);
		_apiUrl = _sharedPref.getString(_context.getString(R.string.api_url),
				"");
		_inventoryOwner = _sharedPref.getString(
				_context.getString(R.string.inventory_owner), "");
	}

	public JSONObject uploadVehicle(Vehicle vehicle) {

		InputStream inputstream = null;
		JSONObject jObj = null;
		String json = "";
		// Making HTTP request
		try {
			String payload = getPayload(vehicle);
			Log.i("payload", payload);
			String vinuploadApi = _sharedPref.getString(
					_context.getString(R.string.api_create_vehicle), "");
			String url = _apiUrl + vinuploadApi + "?inventoryOwner="
					+ _inventoryOwner;
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity(payload);
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, _timeout);
			HttpConnectionParams.setSoTimeout(httpParameters, _timeout);

			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setParams(httpParameters);
			httpPost.setEntity(params);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			inputstream = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputstream, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			inputstream.close();
			json = sb.toString();
			Log.i("UploadVehicleTask Url return", json);
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}

	public JSONObject decodeVin(String vin) {

		InputStream inputstream = null;
		JSONObject jObj = null;
		String json = "";
		// Making HTTP request
		try {

			String vinuploadApi = _sharedPref.getString(
					_context.getString(R.string.api_vin_decode), "");
			String url = _apiUrl + vinuploadApi + "?inventoryOwner="
					+ _inventoryOwner;

			// defaultHttpClient
			String request = "{\"vehicles\":[{\"vehicle\":{\"vin\":\"" + vin
					+ "\"}}]}";

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity(request);
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, _timeout);
			HttpConnectionParams.setSoTimeout(httpParameters, _timeout);

			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setParams(httpParameters);
			httpPost.setEntity(params);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			inputstream = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputstream, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			inputstream.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;

	}

	public String uploadImage(File imageFile) {
		String assetId = "";
		try {

			String uploadImageApi = _sharedPref.getString(
					_context.getString(R.string.api_image_upload), "");
			String url = _apiUrl + uploadImageApi + "?inventoryOwner="
					+ _inventoryOwner;
			Log.i("UploadImageUrl", url);

			HttpPost post = new HttpPost(url);

			MultipartEntityBuilder multipartEntity = MultipartEntityBuilder
					.create();
			multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart("images", new FileBody(imageFile));
			multipartEntity.addTextBody("assetType", "image");

			post.setEntity(multipartEntity.build());

			HttpResponse response = _httpClient.execute(post);

			String responseBody = EntityUtils.toString(response.getEntity());
			JsonParser jsonParser = new JsonParser();
			JsonElement ele = jsonParser.parse(responseBody);
			assetId = ele.getAsJsonObject().getAsJsonArray("uploadStatus")
					.get(0).getAsJsonObject().get("assetId").getAsString();
			String uploadedImagePath = ele.getAsJsonObject()
					.getAsJsonArray("uploadStatus").get(0).getAsJsonObject()
					.get("path").getAsString();
			String imageUrl = ele.getAsJsonObject().get("imageServer")
					.getAsString()
					+ uploadedImagePath;

			Log.d("imageUrl", imageUrl);

			_httpClient.getConnectionManager().shutdown();

		} catch (Exception e) {
			Log.e("UploadImage", e.toString());
			e.printStackTrace();
		}
		return assetId;
	}

	private String getPayload(Vehicle vehicle) {

		String make = vehicle.getMake();
		String model = vehicle.getModel();
		String year = vehicle.getYear();
		String vin = vehicle.getVIN();
		String styleid = vehicle.getStyleId();
		Log.i("car ", make + model + year + vin + "styleid: " + styleid + " ");

		List<String> dealerPhotoIds = vehicle.getDealerPhotoIds();
		String photoIds = "";

		StringBuilder sb = new StringBuilder();
		if (dealerPhotoIds != null && dealerPhotoIds.size() > 0) {
			for (String photoId : dealerPhotoIds) {
				sb.append("{\"id\":\"" + photoId + "\"},");
			}
			sb.deleteCharAt(sb.length() - 1);
			photoIds = sb.toString();
		}
		Log.i("photoIds",photoIds);
		String payload = "{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\""
				+ make
				+ "\"},\"model\":{\"label\":\""
				+ model
				+ "\"},\"year\":"
				+ year
				+ ",\"style\":{\"id\":\""
				+ styleid
				+ "\"},\"source\":\"M\",\"vin\":\""
				+ vin
				+ "\",\"assets\":{\"dealerPhotos\":["
				+ photoIds
				+ "]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\", \"source\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";
		// payload
		// ="{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\"Volkswagen\"},\"model\":{\"label\":\"Jetta Sedan\"},\"year\":2009,\"vin\":\"3vwal71k99m128066\",\"assets\":{\"dealerPhotos\":[{\"id\":\"7242888004\"}]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";
		Log.i("payload", payload);

		return payload;

	}

}
