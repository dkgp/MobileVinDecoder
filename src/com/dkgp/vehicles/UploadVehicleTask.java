package com.dkgp.vehicles;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class UploadVehicleTask extends AsyncTask<String, String, JSONObject> {
	private ProgressDialog pDialog;
	private MainActivity _activity;
	private static String url = "https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles?inventoryOwner=gmps-kindred";

	public UploadVehicleTask(MainActivity mainActivity) {
		_activity = mainActivity;

	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(_activity);
		pDialog.setMessage("Uploading Vehicle Info\nPlease wait ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();

	}

	@Override
	protected JSONObject doInBackground(String... args) {

//		EditText vin = (EditText) _activity.findViewById(R.id.scannedVIN);
//		String barcode = vin.getText().toString();
	    //barcode = "1HGCM82633A004352";
//		String request = "{\"vehicles\":[{\"vehicle\":{\"vin\":\""
//				+ barcode + "\"}}]}";
		String make ="";
		String model="";
		String year="";
		String vin="";
		String dealerPhotoId ="";
		String request ="{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\""+make+"\"},\"model\":{\"label\":\""+model+"\"},\"year\":"+year+",\"vin\":\""+ vin +"\",\"assets\":{\"dealerPhotos\":[{\"id\":\""+dealerPhotoId+"\"}]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";
		request ="{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\"Volkswagen\"},\"model\":{\"label\":\"Jetta Sedan\"},\"year\":2009,\"vin\":\"3vwal71k99m128066\",\"assets\":{\"dealerPhotos\":[{\"id\":\"7242888004\"}]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";

		JSONParser jParser = new JSONParser();
		JSONObject json = jParser.getJSONFromUrl(url, request);
		return json;
	}

	@Override
	protected void onPostExecute(JSONObject json) {
		pDialog.dismiss();
		try {

			
			JSONObject result = json.getJSONObject("result").getJSONArray("status").getJSONObject(0);
					
			String message = result.getString("message");
			Boolean status =message.contains("Vehicle created successfully");
			
			if (status==true){
				Toast.makeText(_activity,"Successfully Uploaded.", 8).show();
			}else{
				Toast.makeText(_activity,"Failed to Upload.", 8).show();
			}
			//Log.i("test", message);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}