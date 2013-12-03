package com.dkgp.vehicles;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

public class DecoderTask extends AsyncTask<String, String, JSONObject> {
	private ProgressDialog pDialog;
	private MainActivity _activity;
	//private static String url = "https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles/detail?inventoryOwner=gmps-kindred&locale=en_us";

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

		Resources res = _activity.getResources();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_activity);
		
		String apiUrl = sharedPref.getString(res.getString(R.string.api_url), "");
		String vinuploadApi = sharedPref.getString(res.getString(R.string.api_vin_decode), "");
		String inventoryOwner = sharedPref.getString(res.getString(R.string.inventory_owner), "");
		String url=apiUrl +vinuploadApi+"?inventoryOwner="+inventoryOwner ;

		HttpConnection jParser = new HttpConnection();
		JSONObject json = jParser.getJSONFromUrl(url, request, 5000);
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		pDialog.dismiss();
		try {
			JSONObject jsonVehicle = json.getJSONArray("vehicles")
					.getJSONObject(0).getJSONObject("vehicle");
			
			Vehicle vehicle =new Vehicle();
			String make = jsonVehicle.getJSONObject("make").getString("label");
			String model = jsonVehicle.getJSONObject("model").getString("label");
			String year = jsonVehicle.getString("year");
			String vin = jsonVehicle.getString("vin");
			String styleId = jsonVehicle.getJSONObject("style").getString("id");
			vehicle.setMake(make);
			vehicle.setModel(model);
			vehicle.setYear(year);
			vehicle.setVIN(vin);
			vehicle.setStyleId(styleId);
			
			EditText etMake = (EditText) _activity.findViewById(R.id.etMake);
            EditText etModel = (EditText) _activity.findViewById(R.id.etModel);
            EditText etYear = (EditText) _activity.findViewById(R.id.etYear);
            
            etMake.setText(make);
            etModel.setText(model);
            etYear.setText(year);
            
			_activity.OnDecoderTaskComplete(vehicle);
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}