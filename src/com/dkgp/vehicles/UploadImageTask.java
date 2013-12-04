package com.dkgp.vehicles;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UploadImageTask extends AsyncTask<File, Object, String> {

	private Context _context;
	Handler _handler;
	
	
	public UploadImageTask(Context c, Handler h) {
		_context = c;
		_handler = h; // uses observer pattern to communicate to Main Activity
	}

	@Override
	protected String doInBackground(File... params) {
		File imageFile = params[0];
		try{
		    InventoryService inventoryService = new InventoryService(_context);
		    return inventoryService.uploadImage(imageFile);
		    
		} catch (Exception e) {
			Log.e("UploadImageTask", e.toString());
			e.printStackTrace();
		}
		 return "";
		
	}
	
	 protected void onPostExecute(String result) {
		 Log.d("UploadImageTask","onPostExecute started");
		 Message msg = Message.obtain();
		 Bundle bundle = new Bundle();
		 if(result.length() > 0){
			 bundle.putString("assetId", result);
			 msg.what = TaskStatus.SUCCESS.ordinal();
		 }
		 msg.setData(bundle);
		 _handler.sendMessage(msg);
     }
}
