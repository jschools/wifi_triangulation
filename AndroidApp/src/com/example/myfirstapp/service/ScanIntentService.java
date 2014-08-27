package com.example.myfirstapp.service;

import android.app.IntentService;
import android.content.Intent;

public class ScanIntentService extends IntentService {

	private static final String NAME = ScanIntentService.class.getSimpleName();

	public static final String ACTION_DATA_POINT_COLLECTED = "ACTION_DATA_POINT_COLLECTED";
	public static final String EXTRA_DATA_POINT_COUNT = "DATA_POINT_COUNT";

	public ScanIntentService() {
		super(NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

}
