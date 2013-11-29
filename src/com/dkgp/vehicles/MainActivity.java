package com.dkgp.vehicles;

import java.io.File;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.dkgp.mobilevindecoder.R;

//test after fetch
public class MainActivity extends Activity {

	private final int _scanRequest = 0;
	private final int _cameraRequest = 1;
	private final int _selectRequest = 2;

	private final int _getImageDialog = 1;

	private String _uploadedImageAssetId;
	private File _imageFile;
	private static String url = "https://api.dev-2.cobalt.com/inventory/rest/v1.0/vehicles/detail?inventoryOwner=gmps-kindred&locale=en_us";

	private OnClickListener getImageListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			showDialog(_getImageDialog);

		}
	};
	private OnClickListener getDecodeVINListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			new JSONParse().execute();

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ImageButton takePicButton = (ImageButton) findViewById(R.id.btnTakePicure);
		takePicButton.setOnClickListener(getImageListener);

		Button decodeVINButton = (Button) findViewById(R.id.decodeVIN);
		decodeVINButton.setOnClickListener(getDecodeVINListener);

	}

	private class JSONParse extends AsyncTask<String, String, JSONObject> {
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Decoding ... Please wait ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();

		}

		@Override
		protected JSONObject doInBackground(String... args) {

			EditText vin = (EditText) findViewById(R.id.scannedVIN);
			String barcode = vin.getText().toString();
//		    barcode = "1HGCM82633A004352";
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

				EditText make = (EditText) findViewById(R.id.etMake);
				EditText model = (EditText) findViewById(R.id.etModel);
				EditText year = (EditText) findViewById(R.id.etYear);
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

	protected android.app.Dialog onCreateDialog(int id) {
		switch (id) {
		case _getImageDialog:
			AlertDialog.Builder builder = new Builder(this);
			return builder
					.setTitle("Select source of image")
					.setNegativeButton("Take new",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(MainActivity.this,
											"Taking new picture", 5).show();

								}
							})
					.setPositiveButton("Select existing",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(MainActivity.this,
											"Selecting existing picture", 5)
											.show();

								}
							}).setIcon(R.drawable.ic_launcher).create();
		}

		return null;

	};

	// test after rename
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void scanVIN(View view) {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "ONE_D_MODE");
		startActivityForResult(intent, 0);
	}

	public void selectPicture(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		// _imageFile = getImageFile();
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(_imageFile));

		startActivityForResult(intent, _selectRequest);

	}

	public void takePicture(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		_imageFile = getImageFile();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(_imageFile)); // to
																			// get
																			// high
																			// resolution
																			// picture
		startActivityForResult(intent, _cameraRequest);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case _scanRequest:
				String contents = data.getStringExtra("SCAN_RESULT");
				String format = data.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				if (format.contains("CODE_39")) {
					EditText editText = (EditText) findViewById(R.id.scannedVIN);
					editText.setText(contents);
					new JSONParse().execute();

				} else {
					Toast.makeText(MainActivity.this,
							"Invalid VIN barcode.  Please rescan!", 8).show();
					
				}
				break;
			case _cameraRequest:
				handleCameraRequest(requestCode, resultCode, data);
				break;
			case _selectRequest:
				Uri selectedImage = data.getData();
				_imageFile = new File(selectedImage.getPath());
				ImageView imageViewer = (ImageView) findViewById(R.id.imageView1);
				imageViewer.setImageURI(selectedImage);

				break;
			default:
				Log.e("onActivityResult", String.format(
						"Unrecognized request code: %d", requestCode));
			}
		} else {
			Log.e("onActivityResult", String.format(
					"Request Failed - resultCode: %d, requestCode: %d",
					resultCode, requestCode));
		}
	}

	protected void handleCameraRequest(int requestCode, int resultCode,
			Intent data) {
		Bitmap image;

		if (data != null) {
			image = (Bitmap) data.getParcelableExtra("data");
		} else {
			// File imageFile = getImageFile();
			String imageFileName = _imageFile.getAbsolutePath();
			image = BitmapFactory.decodeFile(imageFileName);
		}
		// int height = image.getHeight();
		// int width = image.getWidth();
		// Log.e("handleCameraRequest",
		// String.format("Image size ..  height:%d width:%d", height, width));

		// display image
		ImageView imageViewer = (ImageView) findViewById(R.id.imageView1);
		imageViewer.setImageBitmap(image);

	}

	private File getImageFile() {
		File targetDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		assureThatDirectoryExist(targetDir);
		File imageFile = new File(targetDir, "MyPicture.jpg");

		return imageFile;
	}

	private void assureThatDirectoryExist(File directory) {
		if (!directory.exists()) {
			directory.mkdirs();
		}

	}

	public void saveVehicle(View view) {
		try {
			new UploadImageTask(this).execute(_imageFile);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void set_uploadedImageUrl(String url) {
		EditText editText = (EditText) findViewById(R.id.uploadedImageFilePath);
		editText.setText(url);
	}

	public String get_uploadedImageAssetId() {
		return _uploadedImageAssetId;
	}

	public void set_uploadedImageAssetId(String uploadedImageAssetId) {
		Log.e("set_uploadedImageAssetId", uploadedImageAssetId);
		_uploadedImageAssetId = uploadedImageAssetId;
	}

}
