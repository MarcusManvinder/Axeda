package com.axeda.monitor;

import java.util.ArrayList;

import com.axeda.monitor.R;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabBar extends TabActivity {
	/** Called when the activity is first created. */

	String assetName;
	String modelName;
	String alarmname;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab);
		Bundle b = this.getIntent().getExtras();

		alarmname = b.getString("alarmname");
		assetName = b.getString("assetname");
		modelName = b.getString("modelname");
		setTitle("Monitoring: " + assetName);

		/* TabHost will have Tabs */
		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, DetailsTab.class);

		Bundle bund1 = new Bundle();

		bund1.putString("assetname", assetName);
		bund1.putString("modelname", modelName);
		bund1.putString("alarmname", alarmname);
		intent.putExtras(bund1);

		spec = tabHost
				.newTabSpec("Details")
				.setIndicator("Asset Details",
						res.getDrawable(R.drawable.details)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, AlarmsTab.class);
		Bundle bund2 = new Bundle();

		bund2.putString("assetname", assetName);
		bund2.putString("modelname", modelName);
		bund2.putString("alarmname", alarmname);

		intent.putExtras(bund2);
		spec = tabHost
				.newTabSpec("Alerts")
				.setIndicator("Asset Alerts", res.getDrawable(R.drawable.alarm))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, DataTab.class);
		Bundle bund3 = new Bundle();

		bund3.putString("assetname", assetName);
		bund3.putString("modelname", modelName);
		bund3.putString("alarmname", alarmname);
		intent.putExtras(bund3);
		spec = tabHost.newTabSpec("Data")
				.setIndicator("Asset Data", res.getDrawable(R.drawable.data))
				.setContent(intent);
		tabHost.addTab(spec);

	}
}
