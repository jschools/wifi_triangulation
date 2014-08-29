package com.example.myfirstapp;

import android.net.wifi.ScanResult;

public class WifiPoint {

	public final String macAddress;
	public final int rssi;
	public final int room;
	public final long timestamp;

	public WifiPoint(ScanResult scanResult, int room, long time) {
		macAddress = scanResult.BSSID;
		rssi = scanResult.level;
		this.room = room;
		timestamp = time;
	}

}
