package com.dkgp.vehicles;

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
	//private static String url = "https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles?inventoryOwner=gmps-kindred";
	private String payload;
	public UploadVehicleTask(MainActivity mainActivity) {
		_activity = mainActivity;

	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		payload=GetPayload();
		if (payload.length()==0){
			
			
		}
		
		pDialog = new ProgressDialog(_activity);
		pDialog.setMessage("Uploading Vehicle Info\nPlease wait ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(true);
		pDialog.show();

	}

	@Override
	protected JSONObject doInBackground(String... args) {


	    //barcode = "1HGCM82633A004352";
//		String request = "{\"vehicles\":[{\"vehicle\":{\"vin\":\""
//				+ barcode + "\"}}]}";
		Resources res = _activity.getResources();
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_activity);
		String apiUrl = sharedPref.getString(res.getString(R.string.api_url), "");
		String vinuploadApi = sharedPref.getString(res.getString(R.string.api_create_vehicle), "");
		String url=apiUrl +"?inventoryOwner="+ vinuploadApi;

		JSONParser jParser = new JSONParser();
		JSONObject json = jParser.getJSONFromUrl(url, payload);
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
	private String GetPayload()
	{

		EditText etMake = (EditText) _activity.findViewById(R.id.etMake);
		String make = etMake.getText().toString();
		
		EditText etModel = (EditText) _activity.findViewById(R.id.etModel);
		String model = etModel.getText().toString();
		
		EditText etYear = (EditText) _activity.findViewById(R.id.etYear);
		String year = etYear.getText().toString();
		
		EditText etVin = (EditText) _activity.findViewById(R.id.scannedVIN);
		String vin = etVin.getText().toString();
		
		String dealerPhotoId = ""; //activity.get_uploadedImageAssetId();

		if(make.isEmpty() || model.isEmpty()||year.isEmpty()|| dealerPhotoId.isEmpty())
		{
			return "";
		}
		String payload ="{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\""+make+"\"},\"model\":{\"label\":\""+model+"\"},\"year\":"+year+",\"vin\":\""+ vin +"\",\"assets\":{\"dealerPhotos\":[{\"id\":\""+dealerPhotoId+"\"}]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";
		payload ="{\"criteria\":{\"vehicleContexts\":[{\"vehicleContext\":{\"vehicle\":{\"make\":{\"label\":\"Volkswagen\"},\"model\":{\"label\":\"Jetta Sedan\"},\"year\":2009,\"vin\":\"3vwal71k99m128066\",\"assets\":{\"dealerPhotos\":[{\"id\":\"7242888004\"}]}},\"modifiedFields\":[\"make.label\",\"model.label\",\"vin\",\"year\",\"assets\"]}}],\"inventoryOwner\":\"gmps-kindred\"}}";
		
		return payload;
		
	}
}