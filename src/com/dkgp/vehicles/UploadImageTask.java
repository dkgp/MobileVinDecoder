package com.dkgp.vehicles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class UploadImageTask extends AsyncTask<File, Object, List<String>> {

	private final int _timeout = 3000; 
	private MainActivity _activity;
	private String _imageUrl;

	public UploadImageTask(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
		_activity = mainActivity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected List<String> doInBackground(File... params) {
		File imageFile = params[0];
		List<String> myData = new ArrayList<String>(); 
		Resources res = _activity.getResources();
		try{
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, _timeout);
			HttpConnectionParams.setSoTimeout(httpParameters, _timeout);
			
			HttpClient client = new DefaultHttpClient(httpParameters);
			
			// create image upload api url from settings
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_activity);
			String apiUrl = sharedPref.getString(res.getString(R.string.api_url), "");
			String uploadImageApi = sharedPref.getString(res.getString(R.string.api_image_upload), "");
			
		    HttpPost post = new HttpPost(apiUrl + uploadImageApi);
		    
		    MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();  
		    multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		    multipartEntity.addPart("images", new FileBody(imageFile));
		    multipartEntity.addTextBody("assetType", "image");
		    
		    post.setEntity(multipartEntity.build());
		    
		    HttpResponse response = client.execute(post);
		    //HttpEntity entity = response.getEntity();
		    
		    String responseBody = EntityUtils.toString(response.getEntity());
		    JsonParser jsonParser = new JsonParser();
		    JsonElement ele = jsonParser.parse(responseBody);
		    String uploadedImageAssetId = ele.getAsJsonObject().getAsJsonArray("uploadStatus").get(0).getAsJsonObject().get("assetId").getAsString();
		    String uploadedImagePath = ele.getAsJsonObject().getAsJsonArray("uploadStatus").get(0).getAsJsonObject().get("path").getAsString();
		    _imageUrl = ele.getAsJsonObject().get("imageServer").getAsString() + uploadedImagePath;
		    
		    Log.d("imageUrl",_imageUrl);
		    
		    client.getConnectionManager().shutdown(); 
			myData.add("Image uploaded successfully");
			myData.add(uploadedImageAssetId);
		    
		} catch (Exception e) {
			//Toast.makeText(_activity, "Error uploading picture.", 5);
			//_activity.show_message("error uploading picture");
			Log.e("doInBackground", e.toString());
			e.printStackTrace();
			myData.add("Error uploading image file");
			myData.add("");
		}
		 return myData;
		
	}
	
	 protected void onPostExecute(List<String> result) {
		 Log.e("UploadImageTask","onPostExecute started");
		 _activity.OnUploadImageTaskComplete(result);
     }
}
