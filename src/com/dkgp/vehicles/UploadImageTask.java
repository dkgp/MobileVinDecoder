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

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class UploadImageTask extends AsyncTask<File, Object, String> {

	private final int _timeout = 5000; 
	private MainActivity _activity;
	private String _imageUrl;

	public UploadImageTask(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
		_activity = mainActivity;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String doInBackground(File... params) {
		File imageFile = params[0];
		try{
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, _timeout);
			HttpConnectionParams.setSoTimeout(httpParameters, _timeout);
			
			HttpClient client = new DefaultHttpClient(httpParameters);
			
		    HttpPost post = new HttpPost("https://api.dev-2.cobalt.com/inventoryAssetService/rest/v1.0/assets/upload");
		    
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
		    _imageUrl = ele.getAsJsonObject().get("imageServer").getAsString() + uploadedImageAssetId;
		    
		    client.getConnectionManager().shutdown(); 
			
		    Log.e("postFile",String.format("Image uploaded successfully. File Name: %s", uploadedImageAssetId));
		    return uploadedImageAssetId;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error uploading image file";
		}
		
	}
	
	 protected void onPostExecute(String result) {
		 Log.e("UploadImageTask","onPostExecute started");
         Log.e("UploadImageTask","uploaded file as " + result);
         _activity.set_uploadedImageAssetId(result);
         _activity.set_uploadedImageUrl(_imageUrl != null ? _imageUrl : result);
     }
}
