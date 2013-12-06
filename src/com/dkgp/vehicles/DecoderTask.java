package com.dkgp.vehicles;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;

public class DecoderTask extends AsyncTask<String, String, JSONObject> {
	private ProgressDialog dialog;
	private Context _context;
	private Handler _handler;

	public DecoderTask(Context context, Handler handler) {
		_context = context;
		_handler = handler;

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		dialog = new ProgressDialog(_context);
		dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
		dialog.setMessage("Decoding\nPlease wait ...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(true);
		dialog.show();

	}

	@Override
	protected JSONObject doInBackground(String... params) {

		String vin = params[0];
		InventoryService inventoryService = new InventoryService(_context);
		JSONObject json = inventoryService.decodeVin(vin);

		return json;

	}

	@Override
	protected void onPostExecute(JSONObject json) {

		try {
			dialog.dismiss();

			JSONObject jsonVehicle = json.getJSONArray("vehicles")
					.getJSONObject(0).getJSONObject("vehicle");

			Vehicle vehicle = new Vehicle();
			String make = jsonVehicle.getJSONObject("make").getString("label");
			String model = jsonVehicle.getJSONObject("model")
					.getString("label");
			String year = jsonVehicle.getString("year");
			String vin = jsonVehicle.getString("vin");
			String styleId = jsonVehicle.getJSONObject("style").getString("id");
			vehicle.setMake(make);
			vehicle.setModel(model);
			vehicle.setYear(year);
			vehicle.setVIN(vin);
			vehicle.setStyleId(styleId);

			Message msg = Message.obtain();
			msg.obj = vehicle;

			_handler.sendMessage(msg);

		} catch (Exception e) {
			_handler.sendMessage(null);
			e.printStackTrace();
		}

	}
}