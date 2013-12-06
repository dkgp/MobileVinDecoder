package com.dkgp.vehicles;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class UploadVehicleTask extends AsyncTask<String, String, JSONObject> {
	private ProgressDialog dialog;
	private Vehicle _vehicle;
	private Context _context;
	private Handler _handler;

	public UploadVehicleTask(Context context, Handler handler, Vehicle vehicle) {
		_context = context;
		_handler = handler;
		_vehicle = vehicle;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		dialog = new ProgressDialog(_context);
		dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
		dialog.setMessage("Uploading Vehicle Info\nPlease wait ...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.show();
	}

	@Override
	protected JSONObject doInBackground(String... args) {
		
		InventoryService inventoryService = new InventoryService(_context);
		JSONObject json = inventoryService.uploadVehicle(_vehicle);
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		dialog.dismiss();
		try {

			Log.i("return-json", "json");
			JSONObject result = json.getJSONObject("result")
					.getJSONArray("status").getJSONObject(0);

			String message = result.getString("message");
			Boolean status = message.contains("Vehicle created successfully");

			Message msg = Message.obtain();
			Bundle bundle = new Bundle();

			if (status == true) {
				Toast.makeText(_context, "Successfully Uploaded.", 8).show();
				msg.what = TaskStatus.SUCCESS.ordinal();
			} else {
				Toast.makeText(_context, "Failed to Upload.", 8).show();
				msg.what = TaskStatus.FAIL.ordinal();
			}
			msg.setData(bundle);
			_handler.sendMessage(msg);

			Log.i("return message", message);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}