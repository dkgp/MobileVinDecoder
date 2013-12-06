package com.dkgp.vehicles;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class DecoderTask extends AsyncTask<String, String, JSONObject> {
	private ProgressDialog pDialog;
	
	// private static String url =
	// "https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles/detail?inventoryOwner=gmps-kindred&locale=en_us";

	private Context _context;
	private Handler _handler;

	public DecoderTask(Context context, Handler handler) {
		_context = context;
		_handler = handler; 

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(_context);
		pDialog.setMessage("Decoding\nPlease wait ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();


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
			pDialog.dismiss();
			
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
			e.printStackTrace();
		}

	}
}