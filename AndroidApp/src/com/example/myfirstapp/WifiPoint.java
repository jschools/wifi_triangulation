package com.example.myfirstapp;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class WifiPoint implements Parcelable {

	@SerializedName("mac")
	public final String macAddress;

	@SerializedName("rssi")
	public final int rssi;

	@SerializedName("room")
	public final int room;

	@SerializedName("time")
	public final long timestamp;

	public WifiPoint(ScanResult scanResult, int room, long time) {
		macAddress = scanResult.BSSID;
		rssi = scanResult.level;
		this.room = room;
		timestamp = time;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(macAddress);
		dest.writeInt(rssi);
		dest.writeInt(room);
		dest.writeLong(timestamp);
	}

	private WifiPoint(Parcel in) {
		macAddress = in.readString();
		rssi = in.readInt();
		room = in.readInt();
		timestamp = in.readLong();
	}

	public static final Creator<WifiPoint> CREATOR = new Creator<WifiPoint>() {
		@Override
		public WifiPoint createFromParcel(Parcel source) {
			return new WifiPoint(source);
		}

		@Override
		public WifiPoint[] newArray(int size) {
			return new WifiPoint[size];
		}

	};

}
