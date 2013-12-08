package com.dkgp.vehicles;

//test 002

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final int _scanRequest = 0;
	private final int _cameraRequest = 1;
	private final int _selectRequest = 2;
	private final int _getImageDialog = 1;

	private static List<String> _uploadedImageAssetIds;
	private static Vehicle _vehicle;
	private File _imageFile;
	private Button saveButton;

	private OnClickListener getImageListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			showDialog(_getImageDialog);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ImageButton takePicButton = (ImageButton) findViewById(R.id.btnTakePicure);
		takePicButton.setOnClickListener(getImageListener);
		Bitmap img = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.ic_launcher);
		img = Bitmap.createScaledBitmap(img, 80, 50, true);
		takePicButton.setImageBitmap(img);

		saveButton = (Button) findViewById(R.id.buttonSave);
	}

	protected android.app.Dialog onCreateDialog(int id) {
		switch (id) {
		case _getImageDialog:
			AlertDialog.Builder builder = new Builder(this);
			return builder
					.setTitle(R.string.title_image_source)
					.setNegativeButton("Take new",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									MainActivity.this.takePicture();
								}
							})
					.setPositiveButton("Select existing",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									MainActivity.this.selectPicture();

								}
							}).setIcon(R.drawable.ic_launcher).create();
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
			Intent intent = new Intent(MainActivity.this,
					MainPreferenceActivity.class);
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

	public void decodeVIN(View view) {
		EditText editText = (EditText) findViewById(R.id.scannedVIN);
		String vin = editText.getText().toString();
		int count = vin.length();
		if (count != 17) {
			Toast.makeText(MainActivity.this,
					"Invalid VIN!\nThe entered VIN must be 17 characters long",
					5).show();
			return;
		}

		callDecodeTask(vin);

	}

	private void callDecodeTask(String vin) {
		Handler asyncHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == TaskStatus.SUCCESS.ordinal()) {

					_vehicle = (Vehicle) msg.obj;
					updateFields(_vehicle);
					Toast.makeText(MainActivity.this,
							"VIN Decoding Completed!", 5).show();
				} else {
					Toast.makeText(MainActivity.this,
							"Cannot Connect to API.\nPlease try again!", 10)
							.show();

				}

			}
		};
		new DecoderTask(this, asyncHandler).execute(vin);
	}

	public void selectPicture() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		startActivityForResult(intent, _selectRequest);

	}

	public void takePicture() {
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
					clearAllFields();
					EditText editText = (EditText) findViewById(R.id.scannedVIN);
					editText.setText(contents);

					callDecodeTask(contents);

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
		Cursor cursor = getContentResolver().query(contentURI, null, null,
				null, null);
		if (cursor == null) { // Source is Dropbox or other similar local file
								// path
			return contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			return cursor.getString(idx);
		}
	}

	protected void handleCameraRequest(int requestCode, int resultCode,
			Intent data) {
		Bitmap image;
		String imageFileName;

		if (data != null) {
			Uri selectedImage = data.getData();
			imageFileName = getRealPathFromURI(selectedImage);
			_imageFile = new File(imageFileName);
		} else {
			imageFileName = _imageFile.getAbsolutePath();
		}
		Log.d("handleCameraRequest",
				String.format("Image File Name: %s", imageFileName));

		// display image
		image = BitmapFactory.decodeFile(imageFileName);

		LinearLayout myGallery = (LinearLayout) findViewById(R.id.mygallery);

		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setLayoutParams(new LayoutParams(250, 250));
		layout.setGravity(Gravity.CENTER);

		ImageView imageView = new ImageView(getApplicationContext());
		imageView.setLayoutParams(new LayoutParams(220, 220));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setImageBitmap(image);

		layout.addView(imageView);
		myGallery.addView(layout);

		// scroll to the right where new image is loaded
		final HorizontalScrollView s = (HorizontalScrollView) findViewById(R.id.horizontalayout1);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				s.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		}, 100L);

		uploadImageToServer();
	}

	private File getImageFile() {
		File targetDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		assureThatDirectoryExist(targetDir);
		File imageFile = new File(targetDir, new SimpleDateFormat(
				"yyyy-MM-dd-hh-mm-ss'.jpg'").format(new Date()));
		Log.d("File name", imageFile.getAbsolutePath());
		return imageFile;
	}

	private void assureThatDirectoryExist(File directory) {
		if (!directory.exists()) {
			directory.mkdirs();
		}

	}

	private void uploadImageToServer() {
		Log.i("saveVehicle", "saveVehicle start");
		saveButton.setEnabled(false);
		Handler asyncHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1: // success
					if (_uploadedImageAssetIds == null) {
						_uploadedImageAssetIds = new ArrayList<String>();
					}
					_uploadedImageAssetIds.add(msg.getData().getString(
							"assetId"));

					Toast.makeText(MainActivity.this,
							"Image uploaded successfully", 5).show();
					break;
				default:
					LinearLayout gallery = (LinearLayout) findViewById(R.id.mygallery);
					gallery.removeAllViews();

					Toast.makeText(MainActivity.this,
							"Error uploading image file", 5).show();
					break;
				}
				saveButton.setEnabled(true);
			}
		};
		new UploadImageTask(this, asyncHandler).execute(_imageFile);
	}

	public void saveVehicle(View view) {
		try {

			if (_vehicle == null || _vehicle.getMake().isEmpty()
					|| _vehicle.getModel().isEmpty()
					|| _vehicle.getYear().isEmpty()) {
				Toast.makeText(this,
						"Error: Vehicle Info Missing. Cannot Save!", 5).show();
				throw new RuntimeException();
			}
			_vehicle.setDealerPhotoIds(_uploadedImageAssetIds);
			Handler asyncHandler = new Handler() {
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					if (msg.what == TaskStatus.SUCCESS.ordinal()) {
						_vehicle = (Vehicle) msg.obj;
						Toast.makeText(MainActivity.this,
								"Vehicle Save Completed!", 10).show();
						clearAllFields();
					} else {
						Toast.makeText(MainActivity.this,
								"Cannot Connect to API.\nPlease try again!", 10)
								.show();
					}

				}
			};
			new UploadVehicleTask(this, asyncHandler, _vehicle).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void show_message(String message) {
		Toast.makeText(this, message, 5).show();
	}

	void updateFields(Vehicle vehicle) {

		// _vehicle =vehicle;

		EditText etMake = (EditText) findViewById(R.id.etMake);
		EditText etModel = (EditText) findViewById(R.id.etModel);
		EditText etYear = (EditText) findViewById(R.id.etYear);

		etMake.setText(vehicle.getMake());
		etModel.setText(vehicle.getModel());
		etYear.setText(vehicle.getYear());

	}

	private void clearAllFields() {
		EditText etMake = (EditText) findViewById(R.id.etMake);
		etMake.setText("");

		EditText etModel = (EditText) findViewById(R.id.etModel);
		etModel.setText("");

		EditText etYear = (EditText) findViewById(R.id.etYear);
		etYear.setText("");

		EditText etVin = (EditText) findViewById(R.id.scannedVIN);
		etVin.setText("");

		LinearLayout gallery = (LinearLayout) findViewById(R.id.mygallery);
		gallery.removeAllViews();
		_vehicle = null;
		_uploadedImageAssetIds = null;
	}

}
