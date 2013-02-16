package com.axeda.monitor;

import java.util.ArrayList;

import com.axeda.monitor.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AssetAlarmListViewAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<Alarm> alarms;

	private static LayoutInflater inflater = null;

	public AssetAlarmListViewAdapter(Activity a, ArrayList<Alarm> alarms) {
		activity = a;
		this.alarms = alarms;

		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public Object getItem(int position) {
		return position;
	}

	public int getCount() {
		return alarms.size();
	}

	public long getItemId(int position) {
		return position;
	}

	public void removeItem(String item) {
		alarms.remove(item);
		this.notifyDataSetChanged();
	}

	public static class ViewHolder {
		public TextView text;
		public TextView text2;
		public TextView text3;
		public TextView text4;
		public ImageView image;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;
		Alarm alarm = alarms.get(position);
		if (convertView == null) {
			vi = inflater.inflate(R.layout.listitem, null);
			holder = new ViewHolder();
			holder.text = (TextView) vi.findViewById(R.id.alarmname);
			holder.text2 = (TextView) vi.findViewById(R.id.date);
			holder.text3 = (TextView) vi.findViewById(R.id.asset);
			holder.text4 = (TextView) vi.findViewById(R.id.AssDesc);
			holder.image = (ImageView) vi.findViewById(R.id.image);
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();
		holder.text.setText(alarm.name);
		holder.text3.setText(alarm.date);
		holder.text2.setText(alarm.deviceName);

		if (alarm.severity != null) {
			if (alarm.severity.equals("Med")) {
				holder.image.setImageResource(R.drawable.medium_icon);
			}
			if (alarm.severity.equals("Lo")) {
				holder.image.setImageResource(R.drawable.low_icon);
			}
			if (alarm.severity.equals("Hi")) {
				holder.image.setImageResource(R.drawable.high_icon);
			}
		} else {
			holder.text.setText("There are no Alarms for your Assets");
			vi.setClickable(false);
		}

		return vi;

	}

}
