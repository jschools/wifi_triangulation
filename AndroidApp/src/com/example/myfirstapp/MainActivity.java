package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener {

	private static final String KEY_POINTS = "points";
	private static final String KEY_LOCATION = "location";

	private static final int SOFT_UPLOAD_LIMIT_KB = 2 * 1024; // cap out around 2MB
	private static final int APPROX_MEASUREMENT_SIZE_B = 90;
	private static final int SOFT_COUNT_LIMIT = (SOFT_UPLOAD_LIMIT_KB * 1024) / APPROX_MEASUREMENT_SIZE_B;

	private boolean mScanning;
	private boolean mUploading;

	private int mLocation;

	private Handler mHandler;
	private ArrayList<WifiPoint> mPoints;
	private final CountAnimator mCountAnimator = new CountAnimator();

	private static final IntentFilter WIFI_FILTER = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
	private static final IntentFilter UPLOAD_FILTER = new IntentFilter(UploadService.ACTION_UPLOAD_COMPLETE);
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
			case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
				appendScanResults();
				break;
			case UploadService.ACTION_UPLOAD_COMPLETE:
				int count = intent.getIntExtra(UploadService.EXTRA_NUM_POINTS, -1);
				Toast.makeText(MainActivity.this, "Server processed " + count + " points", Toast.LENGTH_SHORT).show();
				mUploading = false;
				updateUiState();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(KEY_POINTS, mPoints);
		outState.putInt(KEY_LOCATION, mLocation);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(false);

		if (savedInstanceState != null) {
			mPoints = savedInstanceState.getParcelableArrayList(KEY_POINTS);
			mLocation = savedInstanceState.getInt(KEY_LOCATION, 0);
		}

		if (mPoints == null) {
			mPoints = new ArrayList<WifiPoint>();
		}
		mCountAnimator.setDisplayedCount(mPoints.size());

		mHandler = new Handler();

		Spinner locationSpinner = (Spinner) findViewById(R.id.spinner_location);
		locationSpinner.setSelection(Math.min(mLocation, locationSpinner.getCount()));
		locationSpinner.setOnItemSelectedListener(this);

		findViewById(R.id.btn_start).setOnClickListener(this);
		findViewById(R.id.btn_stop).setOnClickListener(this);
		findViewById(R.id.btn_upload).setOnClickListener(this);
		findViewById(R.id.btn_clear).setOnClickListener(this);
		findViewById(R.id.btn_view).setOnClickListener(this);

		mScanning = false;
		mUploading = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateUiState();

		registerReceiver(mReceiver, WIFI_FILTER);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, UPLOAD_FILTER);
	}

	@Override
	protected void onPause() {
		super.onPause();

		stopScanning();
		mHandler.removeCallbacksAndMessages(null);

		unregisterReceiver(mReceiver);
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_start:
			startScanning();
			break;
		case R.id.btn_stop:
			stopScanning();
			break;
		case R.id.btn_upload:
			uploadResults();
			break;
		case R.id.btn_clear:
			clearResults();
			break;
		case R.id.btn_view:
			viewResults();
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mLocation = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// don't care
	}

	private void startScanning() {
		mScanning = true;
		mHandler.post(mScanRunnable);

		updateUiState();
	}

	private void stopScanning() {
		mScanning = false;
		mHandler.removeCallbacks(mScanRunnable);

		updateUiState();
	}

	private void clearResults() {
		stopScanning();
		mPoints.clear();

		updateUiState();
	}

	private void uploadResults() {
		stopScanning();

		UploadService.startUpload(new ArrayList<>(mPoints));
		mUploading = true;
		TextView uploadedCountView = (TextView) findViewById(R.id.uploaded_count);
		String message = "Uploading " + mPoints.size() + " data points...";
		uploadedCountView.setText(message);

		clearResults();

		updateUiState();
	}

	private void viewResults() {
		Intent intent = new Intent(this, ViewResultsActivity.class);
		intent.putExtra(ViewResultsActivity.EXTA_POINTS, mPoints);
		startActivity(intent);
	}

	private void updateUiState() {
		setProgressBarIndeterminateVisibility(mScanning || mUploading);
		findViewById(R.id.spinner_location).setEnabled(!mScanning);
		findViewById(R.id.btn_start).setEnabled(!mScanning && !mUploading);
		findViewById(R.id.btn_stop).setEnabled(mScanning);
		findViewById(R.id.btn_upload).setEnabled(!mScanning && !mUploading && !mPoints.isEmpty());
		findViewById(R.id.btn_clear).setEnabled(!mScanning && !mUploading && !mPoints.isEmpty());
		findViewById(R.id.uploaded_count).setVisibility(mUploading ? View.VISIBLE : View.GONE);
		findViewById(R.id.btn_view).setEnabled(!mScanning && !mUploading && !mPoints.isEmpty());
		startAnimatedProgressUpdate();
	}

	private void appendScanResults() {
		if (!mScanning) {
			return;
		}

		WifiManager mgr = (WifiManager) getSystemService(WIFI_SERVICE);
		List<ScanResult> results = mgr.getScanResults();

		long time = System.currentTimeMillis() / 1000;
		for (ScanResult scanResult : results) {
			mPoints.add(new WifiPoint(scanResult, mLocation, time));
		}

		if (mPoints.size() > SOFT_COUNT_LIMIT) {
			Toast.makeText(this, "Upload size of " + SOFT_UPLOAD_LIMIT_KB + "kB reached", Toast.LENGTH_SHORT).show();
			stopScanning();
		}

		startAnimatedProgressUpdate();
	}

	private void startAnimatedProgressUpdate() {
		mCountAnimator.run();
	}

	private class CountAnimator implements Runnable {
		private int mDisplayedCount = 0;

		public void setDisplayedCount(int displayedCount) {
			mDisplayedCount = displayedCount;
			updateProgress(mDisplayedCount);
		}

		@Override
		public void run() {
			mHandler.removeCallbacks(this);

			updateProgress(mDisplayedCount);
			final int mPointCount = mPoints.size();
			if (mDisplayedCount != mPointCount) {
				if (mDisplayedCount < mPoints.size()) {
					mDisplayedCount++;
				}
				else {
					mDisplayedCount = Math.max(mPointCount, mDisplayedCount / 2);
				}
				mHandler.postDelayed(this, 30);
			}
		}

		private void updateProgress(int dataPointCount) {
			String text = getString(R.string.fmt_data_points_collected, Integer.valueOf(dataPointCount));
			((TextView) findViewById(R.id.collected_count)).setText(text);
		}
	}

	private final Runnable mScanRunnable = new Runnable() {
		@Override
		public void run() {
			mHandler.removeCallbacks(this);

			WifiManager mgr = (WifiManager) getSystemService(WIFI_SERVICE);
			boolean started = mgr.startScan();

			if (!started) {
				Toast.makeText(MainActivity.this, "WiFi scan failed to start", Toast.LENGTH_SHORT).show();
				stopScanning();
			}
			else {
				mHandler.postDelayed(this, 500);
			}
		}
	};

}
