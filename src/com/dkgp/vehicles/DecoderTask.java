package com.dkgp.vehicles;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.EditText;

public class DecoderTask extends AsyncTask<String, String, JSONObject> {
	private ProgressDialog pDialog;
	private MainActivity _activity;
	private static String url = "https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles/detail?inventoryOwner=gmps-kindred&locale=en_us";

	public DecoderTask(MainActivity mainActivity) {
		_activity = mainActivity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(_activity);
		pDialog.setMessage("Decoding\nPlease wait ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();

	}

	@Override
	protected JSONObject doInBackground(String... args) {

		EditText vin = (EditText) _activity.findViewById(R.id.scannedVIN);
		String barcode = vin.getText().toString();
	    //barcode = "1HGCM82633A004352";
		String request = "{\"vehicles\":[{\"vehicle\":{\"vin\":\""
				+ barcode + "\"}}]}";

		JSONParser jParser = new JSONParser();
		JSONObject json = jParser.getJSONFromUrl(url, request);
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		pDialog.dismiss();
		try {

			EditText make = (EditText) _activity.findViewById(R.id.etMake);
			EditText model = (EditText) _activity.findViewById(R.id.etModel);
			EditText year = (EditText) _activity.findViewById(R.id.etYear);
			JSONObject vehicle = json.getJSONArray("vehicles")
					.getJSONObject(0).getJSONObject("vehicle");

			make.setText(vehicle.getJSONObject("make").getString("label"));
			model.setText(vehicle.getJSONObject("model").getString("label"));
			year.setText(vehicle.getString("year"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}