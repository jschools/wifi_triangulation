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

	private boolean mScanning;
	private Handler mHandler;
	private List<WifiPoint> mPoints;
	private int mRoom;
	private int mDisplayedCount;

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
				Toast.makeText(MainActivity.this, "Complete: " + count, Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(false);

		mHandler = new Handler();
		mPoints = new ArrayList<WifiPoint>();
		mDisplayedCount = 0;

		Spinner roomSpinner = (Spinner) findViewById(R.id.spinner_location);
		roomSpinner.setOnItemSelectedListener(this);
		mRoom = roomSpinner.getSelectedItemPosition();

		findViewById(R.id.btn_start).setOnClickListener(this);
		findViewById(R.id.btn_stop).setOnClickListener(this);
		findViewById(R.id.btn_upload).setOnClickListener(this);
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
		default:
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mRoom = position;
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

	private void uploadResults() {
		stopScanning();

		UploadService.startUpload(new ArrayList<>(mPoints));

		mDisplayedCount = 0;
		mPoints.clear();
		updateProgress(0);
	}

	private void updateUiState() {
		setProgressBarIndeterminateVisibility(mScanning);
		findViewById(R.id.spinner_location).setEnabled(!mScanning);
		findViewById(R.id.btn_start).setEnabled(!mScanning);
		findViewById(R.id.btn_stop).setEnabled(mScanning);
		findViewById(R.id.btn_upload).setEnabled(!mScanning);
	}

	private void appendScanResults() {
		if (!mScanning) {
			return;
		}

		WifiManager mgr = (WifiManager) getSystemService(WIFI_SERVICE);
		List<ScanResult> results = mgr.getScanResults();

		long time = System.currentTimeMillis() / 1000;
		for (ScanResult scanResult : results) {
			mPoints.add(new WifiPoint(scanResult, mRoom, time));
		}

		mHandler.post(mChangeCounterRunnable);
	}

	private void updateProgress(int dataPointCount) {
		String text = getString(R.string.fmt_data_points_collected, Integer.valueOf(dataPointCount));
		((TextView) findViewById(R.id.collected_count)).setText(text);
	}

	private final Runnable mChangeCounterRunnable = new Runnable() {
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
					mDisplayedCount--;
				}
				mHandler.postDelayed(this, 30);
			}
		}
	};

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
