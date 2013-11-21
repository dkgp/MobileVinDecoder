package com.dkgp.mobilevindecoder;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private final int _scanRequest = 0;
	private final int _cameraRequest = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
 	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void scanVIN(View view)
	{
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "ONE_D_MODE");
		startActivityForResult(intent, 0);
	}

	public void takePicture(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		File imageFile = getImageFile();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
		startActivityForResult(intent, _cameraRequest);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			switch (requestCode){
			case _scanRequest:
				String contents = data.getStringExtra("SCAN_RESULT");
		        String format = data.getStringExtra("SCAN_RESULT_FORMAT");
		        // Handle successful scan
		        EditText editText = (EditText)findViewById(R.id.scannedVIN);
		        editText.setText("VIN: "+contents +" format: " + format);
				break;
			case _cameraRequest:
				handleCameraRequest(requestCode, resultCode, data);
				break;
				
				default:
					Log.e("onActivityResult", String.format("Unrecognized request code: %d", requestCode));
			}
		} else {
			Log.e("onActivityResult", String.format("Request Failed - resultCode: %d, requestCode: %d", resultCode, requestCode));
		}
	}
	
	protected void handleCameraRequest(int requestCode, int resultCode, Intent data){
		Bitmap image;
		
		if (data != null){
			image = (Bitmap)data.getParcelableExtra("data");
		}
		else {
			File imageFile = getImageFile();
			String imageFileName = imageFile.getAbsolutePath();
			image = BitmapFactory.decodeFile(imageFileName);
		}
		int height = image.getHeight();
		int width = image.getWidth();
		Log.e("handleCameraRequest", String.format("Image size ..  height:%d width:%d", height, width));
		
	}
	
	private File getImageFile() {
		File targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		assureThatDirectoryExist(targetDir);
		File imageFile = new File(targetDir, "MyPicture.jpg");
		
		return imageFile;
	}
	
	private void assureThatDirectoryExist(File directory) {
		if (!directory.exists()){
			directory.mkdirs();
		}
		
	}

}
