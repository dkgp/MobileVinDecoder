package com.dkgp.vehicles;

import java.io.File;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

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
		
		// initialize
		initialize();
		
	}

	private void initialize() {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, _timeout);
		HttpConnectionParams.setSoTimeout(httpParameters, _timeout);

		_httpClient = new DefaultHttpClient(httpParameters);

		_sharedPref = PreferenceManager.getDefaultSharedPreferences(_context);
		_apiUrl = _sharedPref.getString(_context.getString(R.string.api_url), "");
		_inventoryOwner = _sharedPref.getString(_context.getString(R.string.inventory_owner), "");
	}

	public String uploadImage(File imageFile){
		String assetId = "";
		try {
			
			String uploadImageApi = _sharedPref.getString(_context.getString(R.string.api_image_upload), "");
			String url = _apiUrl + uploadImageApi +"?inventoryOwner="+_inventoryOwner;
			Log.i("UploadImageUrl",url);

			HttpPost post = new HttpPost(url);

			MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();  
			multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart("images", new FileBody(imageFile));
			multipartEntity.addTextBody("assetType", "image");

			post.setEntity(multipartEntity.build());


			HttpResponse response = _httpClient.execute(post);

			String responseBody = EntityUtils.toString(response.getEntity());
			JsonParser jsonParser = new JsonParser();
			JsonElement ele = jsonParser.parse(responseBody);
			assetId = ele.getAsJsonObject().getAsJsonArray("uploadStatus").get(0).getAsJsonObject().get("assetId").getAsString();
			String uploadedImagePath = ele.getAsJsonObject().getAsJsonArray("uploadStatus").get(0).getAsJsonObject().get("path").getAsString();
			String imageUrl = ele.getAsJsonObject().get("imageServer").getAsString() + uploadedImagePath;

			Log.d("imageUrl",imageUrl);

			_httpClient.getConnectionManager().shutdown(); 


		} catch (Exception e) {
			Log.e("UploadImage", e.toString());
			e.printStackTrace();
		}
		return assetId;
	}
	
	
}
