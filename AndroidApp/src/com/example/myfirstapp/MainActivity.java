package com.example.myfirstapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.example.myfirstapp.service.ScanIntentService;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume() {
		super.onResume();

		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, FILTER);
	}

	@Override
	protected void onPause() {
		super.onPause();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
	}

	private void updateProgress(int dataPointCount) {
		String text = getString(R.string.fmt_data_points_collected, Integer.valueOf(dataPointCount));
		((TextView) findViewById(R.id.collected_count)).setText(text);
	}

	private static final IntentFilter FILTER = new IntentFilter(ScanIntentService.ACTION_DATA_POINT_COLLECTED);

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int dataPoints = intent.getIntExtra(ScanIntentService.EXTRA_DATA_POINT_COUNT, 0);
			updateProgress(dataPoints);
		}
	};
}
