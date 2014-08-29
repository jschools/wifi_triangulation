package com.zackaryscholl.wifilocation.scanner;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ViewResultsActivity extends ListActivity {

	public static final String EXTA_POINTS = ViewResultsActivity.class.getName() + ".EXTRA_POINTS";

	private List<WifiPoint> mPoints;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPoints = getIntent().getParcelableArrayListExtra(EXTA_POINTS);
		setListAdapter(new WifiPointAdapter(this, mPoints));
	}

	private static class WifiPointAdapter extends ArrayAdapter<WifiPoint> {

		public WifiPointAdapter(Context context, List<WifiPoint> objects) {
			super(context, R.layout.list_item_wifi, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ViewHolder holder;
			if (v == null) {
				v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_wifi, parent, false);
				holder = new ViewHolder(v);
				v.setTag(holder);
			}
			else {
				holder = (ViewHolder) v.getTag();
			}

			final WifiPoint point = getItem(position);
			holder.position.setText(Integer.toString(position));
			holder.mac.setText("MAC: " + point.macAddress);
			holder.rssi.setText("RSSI: " + point.rssi);
			holder.location.setText("Location: " + point.location);

			return v;
		}

		private static class ViewHolder {
			public final TextView position;
			public final TextView mac;
			public final TextView rssi;
			public final TextView location;

			public ViewHolder(View v) {
				position = (TextView) v.findViewById(R.id.position);
				mac = (TextView) v.findViewById(R.id.mac);
				rssi = (TextView) v.findViewById(R.id.rssi);
				location = (TextView) v.findViewById(R.id.location);
			}
		}

	}
}
