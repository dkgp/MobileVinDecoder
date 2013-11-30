package com.dkgp.vehicles;

//test 002

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

//test 001
public class MainActivity extends Activity {

	private final int _scanRequest = 0;
	private final int _cameraRequest = 1;
	private final int _selectRequest = 2;

	private final int _getImageDialog = 1;

	private String _uploadedImageAssetId;
	private File _imageFile;
	
	private Button saveButton;
	
	private OnClickListener getImageListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			showDialog(_getImageDialog);

		}
	};
	private OnClickListener getDecodeVINListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			new DecoderTask(MainActivity.this).execute();
		}
	};
	
//	private OnClickListener getUploadVehicleListener = new OnClickListener() {
//
//		@Override
//		public void onClick(View view) {
//			new UploadVehicleTask(MainActivity.this).execute();
//		}
//	};	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ImageButton takePicButton = (ImageButton) findViewById(R.id.btnTakePicure);
		takePicButton.setOnClickListener(getImageListener);

		Button decodeVINButton = (Button) findViewById(R.id.decodeVIN);
		decodeVINButton.setOnClickListener(getDecodeVINListener);
		
//		Button uploadVehicleButton = (Button) findViewById(R.id.btnUploadVehicle);
//		uploadVehicleButton.setOnClickListener(getUploadVehicleListener);	

		saveButton = (Button)findViewById(R.id.buttonSave);
	}


	protected android.app.Dialog onCreateDialog(int id) {
		switch(id) {
		case _getImageDialog:
			AlertDialog.Builder builder = new Builder(this);
			return builder
					.setTitle(R.string.title_image_source)
					.setNegativeButton("Take new", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.this.takePicture();
						}
					})
					.setPositiveButton("Select existing", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.this.selectPicture();
							
						}
					})
					.setIcon(R.drawable.ic_launcher)
					.create();
		}
		return null;
	};
 	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_settings:
	        	Intent intent = new Intent(MainActivity.this, MainPreferenceAcitivity.class);
	    		startActivity(intent);
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void scanVIN(View view) {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "ONE_D_MODE");
		startActivityForResult(intent, 0);
	}

	public void selectPicture() {
		Intent intent = new Intent(Intent.ACTION_PICK,
		           android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		startActivityForResult(intent , _selectRequest);
		
	}
	
	private void initializeImage() {
		_imageFile = null;
		set_uploadedImageAssetId(null);
		ImageView imageView = (ImageView) findViewById(R.id.imageView1);
		//imageView.setImageResource(R.drawable.no_image);
		imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.no_image));
	}

	public void takePicture() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		_imageFile = getImageFile();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(_imageFile)); // to get high resolution picture
		
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
					new DecoderTask(MainActivity.this).execute();

				} else {
					Toast.makeText(MainActivity.this,
							"Invalid VIN barcode.  Please rescan!", 8).show();
					
				}
				break;
			case _cameraRequest:
			case _selectRequest:
				handleCameraRequest(requestCode, resultCode, data);

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

	private String getRealPathFromURI(Uri contentURI) {
	    Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
	    if (cursor == null) { // Source is Dropbox or other similar local file path
	        return contentURI.getPath();
	    } else { 
	        cursor.moveToFirst(); 
	        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	        return cursor.getString(idx); 
	    }
	}
	
	protected void handleCameraRequest(int requestCode, int resultCode, Intent data){
		Bitmap image;
		String imageFileName;
		
		if (data != null){
			Uri selectedImage = data.getData();
			imageFileName = getRealPathFromURI(selectedImage);
			_imageFile = new File(imageFileName);
		}
		else {
			imageFileName = _imageFile.getAbsolutePath();
		}
		Log.d("handleCameraRequest",String.format("Image File Name: %s", imageFileName));
		// display image
		image = BitmapFactory.decodeFile(imageFileName);
		ImageView imageViewer = (ImageView) findViewById(R.id.imageView1);
		imageViewer.setImageBitmap(image);
		
		uploadImageToServer();
	}
	
	private File getImageFile() {
		File targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		assureThatDirectoryExist(targetDir);
		File imageFile = new File(targetDir, new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss'.jpg'").format(new Date()));
		Log.d("File name", imageFile.getAbsolutePath());
		return imageFile;
	}
	
	private void assureThatDirectoryExist(File directory) {
		if (!directory.exists()){
			directory.mkdirs();
		}
		
	}

	private void uploadImageToServer() {
		Log.e("saveVehicle","saveVehicle start");
		saveButton.setEnabled(false);
		new UploadImageTask(this).execute(_imageFile);
	}

	public void saveVehicle(View view) {
		try {
			// TODO:  add save code here.....
			
			Toast.makeText(this, "Vehicle successfully saved", 5).show();
			initializeImage();
			new UploadVehicleTask(MainActivity.this).execute();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public String get_uploadedImageAssetId() {
		return _uploadedImageAssetId;
	}
	public void set_uploadedImageAssetId(String uploadedImageAssetId) {
		Log.e("set_uploadedImageAssetId", uploadedImageAssetId);
		_uploadedImageAssetId = uploadedImageAssetId;
	}
	
	public void show_message(String message){
		Toast.makeText(this, message, 5).show();
	}
	
	void OnUploadImageTaskComplete(List<String> results){
		Toast.makeText(this, results.get(0), 5).show();
		_uploadedImageAssetId = results.get(1);
		
		saveButton.setEnabled(true);
	}

}
