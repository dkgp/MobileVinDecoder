package com.dkgp.vehicles;

import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class UploadVehicleTask extends AsyncTask<String, String, JSONObject> {
	private ProgressDialog pDialog;
	private MainActivity _activity;
	private Vehicle _vehicle;
	// private static String url =
	// "https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles?inventoryOwner=gmps-kindred";
	private String payload;

	public UploadVehicleTask(MainActivity mainActivity, Vehicle vehicle) {
		_activity = mainActivity;
		_vehicle = vehicle;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		payload = GetPayload();

		pDialog = new ProgressDialog(_activity);
		pDialog.setMessage("Uploading Vehicle Info\nPlease wait ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();

	}

	@Override
	protected JSONObject doInBackground(String... args) {
		Resources res = _activity.getResources();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(_activity);
		String apiUrl = sharedPref.getString(res.getString(R.string.api_url),
				"");
		String vinuploadApi = sharedPref.getString(
				res.getString(R.string.api_create_vehicle), "");
		String inventoryOwner = sharedPref.getString(
				res.getString(R.string.inventory_owner), "");
		String url = apiUrl + vinuploadApi + "?inventoryOwner="
				+ inventoryOwner;
		Log.i("UploadVehicleTask Url", url);
		HttpConnection jParser = new HttpConnection();
		JSONObject json = jParser.getJSONFromUrl(url, payload, 5000);
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		pDialog.dismiss();
		try {

			Log.i("result", json.toString());
			JSONObject result = json.getJSONObject("result")
					.getJSONArray("status").getJSONObject(0);

			String message = result.getString("message");
			Boolean status = message.contains("Vehicle created successfully");

			if (status == true) {
				Toast.makeText(_activity, "Successfully Uploaded.", 8).show();
			} else {
				Toast.makeText(_activity, "Failed to Upload.", 8).show();
			}
			Log.i("test", message);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String GetPayload() {

		String make = _vehicle.getMake();
		String model = _vehicle.getModel();
		String year = _vehicle.getYear();
		String vin = _vehicle.getVIN();
		String styleid = _vehicle.getStyleId();
		List<String> dealerPhotoIds = _vehicle.getDealerPhotoIds();
		String photoIds ="";
		Log.i("test",make +model+ year+vin+"styleid: "+ styleid);
		StringBuilder sb = new StringBuilder();
		if (dealerPhotoIds.size()>0) {
			for (String photoId : dealerPhotoIds) {
				sb.append("{\"id\":\"" + photoId + "\"},");
			}
			sb.deleteCharAt(sb.length() - 1);
			photoIds = sb.toString();
		}
		
		Log.i("photoIds", photoIds);
		String payload = "{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\""
				+ make
				+ "\"},\"model\":{\"label\":\""
				+ model
				+ "\"},\"year\":"
				+ year
				+ ",\"style\":{\"id\":\""
				+ styleid
				+ "\"},\"source\":\"M\",\"vin\":\""
				+ vin
				+ "\",\"assets\":{\"dealerPhotos\":["
				+ photoIds
				+ "]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\", \"source\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";
		// payload
		// ="{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\"Volkswagen\"},\"model\":{\"label\":\"Jetta Sedan\"},\"year\":2009,\"vin\":\"3vwal71k99m128066\",\"assets\":{\"dealerPhotos\":[{\"id\":\"7242888004\"}]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";
		Log.i("payload", payload);
		return payload;

	}
}