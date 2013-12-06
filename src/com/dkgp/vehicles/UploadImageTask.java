package com.dkgp.vehicles;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UploadImageTask extends AsyncTask<File, Object, String> {
	private ProgressDialog pDialog;
	private Context _context;
	Handler _handler;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(_context);
		pDialog.setMessage("Uploading\nPlease wait ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();

	}
	public UploadImageTask(Context context, Handler handler) {
		_context = context;
		_handler = handler; // uses observer pattern to communicate to Main Activity
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
		 pDialog.dismiss();
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
