package com.example.myfirstapp;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class UploadService extends IntentService {

	public static final String SERVICE_NAME = UploadService.class.getSimpleName();

	private static final String ACTION_UPLOAD = "UploadService.ACTION_UPLOAD";
	private static final String EXTRA_POINTS = "points";
	public static final String ACTION_UPLOAD_COMPLETE = "UploadService.ACTION_UPLOAD_COMPLETE";
	public static final String EXTRA_NUM_POINTS = "numPoints";

	private static final String URL = "http://morning-wave-7971.herokuapp.com/";

	public static void startUpload(List<WifiPoint> points) {
		Context context = WifiScanApplication.getInstance();

		Intent intent = new Intent(context, UploadService.class);
		intent.setAction(ACTION_UPLOAD);

		ArrayList<WifiPoint> arrayList;
		if (points instanceof ArrayList) {
			arrayList = (ArrayList) points;
		}
		else {
			arrayList = new ArrayList<>(points);
		}
		intent.putParcelableArrayListExtra(EXTRA_POINTS, arrayList);

		context.startService(intent);
	}

	public UploadService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		switch (intent.getAction()) {
		case ACTION_UPLOAD:
			handleUpload(intent);
			break;
		default:
			break;
		}
	}

	private static void handleUpload(Intent intent) {
		final List<WifiPoint> points = intent.getParcelableArrayListExtra(EXTRA_POINTS);
		final String json = new Gson().toJson(points);

		RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

		Request.Builder builder = new Request.Builder();
		builder.url(URL).post(body);
		Request request = builder.build();

		OkHttpClient client = new OkHttpClient();
		try {
			Response response = client.newCall(request).execute();
			final int code = response.code();
			if (code == 200) {
				final String responseBody = response.body().string();
				int count = Integer.parseInt(responseBody);
				broadcastCompletion(count);
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		broadcastCompletion(-1);
	}

	private static void broadcastCompletion(int count) {
		Intent intent = new Intent(ACTION_UPLOAD_COMPLETE);
		intent.putExtra(EXTRA_NUM_POINTS, count);
		LocalBroadcastManager.getInstance(WifiScanApplication.getInstance()).sendBroadcast(intent);
	}

}
