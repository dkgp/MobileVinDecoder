package com.dkgp.vehicles;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;

public class UploadImageTask extends AsyncTask<File, Object, String> {
	private ProgressDialog dialog;
	private Context _context;
	Handler _handler;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		dialog = new ProgressDialog(_context);
		dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
		dialog.setMessage("Uploading\nPlease wait ...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.show();

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
		 dialog.dismiss();
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
