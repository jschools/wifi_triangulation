package com.example.myfirstapp;

import android.app.Application;

public class WifiScanApplication extends Application {

	private static WifiScanApplication sInstance;

	@Override
	public void onCreate() {
		super.onCreate();

		sInstance = this;
	}

	public static WifiScanApplication getInstance() {
		return sInstance;
	}
}
