package com.axeda.monitor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.NameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.axeda.monitor.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AlarmsTab extends Activity {

	ImageView image;
	ListView list;
	AssetAlarmListViewAdapter adapter;
	AutoCompleteTextView actv;
	ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	ArrayAdapter<String> arradapter;
	ArrayList<String> deviceName = new ArrayList<String>();
	ArrayList<Asset> assets = new ArrayList<Asset>();
	String alarmresp;
	String alresp;
	String asset;
	String alarmname;
	Intent intent;

	String alarmpos = new String();
	String modelName;

	ProgressDialog myProgressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview);

		Bundle b = this.getIntent().getExtras();
		asset = b.getString("assetname");
		modelName = b.getString("modelname");
		// deviceName = b.getStringArrayList("AllAssets");
		alarmname = b.getString("alarmname");
		list = (ListView) findViewById(R.id.list);

		adapter = new AssetAlarmListViewAdapter(this, alarms);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View view,
					final int position, long id) {

				AlertDialog.Builder adb = new AlertDialog.Builder(
						AlarmsTab.this);

				adb.setTitle("Acknowledge Alarms");

				adb.setMessage("Do you want to Acknowledge "
						+ alarms.get(position).name + "?");
				alarmpos = alarms.get(position).name;

				adb.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								CloseAlarm(asset, modelName, alarmpos);
								adapter.removeItem(alarms.get(position).name);
							}
						});
				adb.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

				adb.show();

			}

		});

		getAlarmsForAsset(asset);

	}

	public OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			listener.onClick(view);
			adapter.notify();
		};
	};

	public void getAlarmsForAsset(String asset_name) {
		AxedaMonitor app = (AxedaMonitor) getApplication();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new NameValuePair("devicename", asset));
		params.add(new NameValuePair("modelname", modelName));
		alarmresp = app.callScripto("Android_getAlarmsForAsset", params);
		if (!alarmresp.contains("<Response />")) {
			parseAlarmForAsset(alarmresp);
		} else {
			alarms.add(new Alarm("There are no Alerts", null, null, null, null,
					null, null));
		}

	}

	public void parseAlarmForAsset(String xmlresp) {

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();

			StringReader reader = new StringReader(alarmresp);
			InputSource inputSource = new InputSource(reader);
			Document document = documentBuilder.parse(inputSource);

			Node responseNode = document.getDocumentElement();
			NodeList locChildNodes = responseNode.getChildNodes();
			NamedNodeMap locAttributes = null;

			for (int i = 0; i < locChildNodes.getLength(); i++) {

				if (locChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					locAttributes = locChildNodes.item(i).getAttributes();
					Alarm alarm = new Alarm(locAttributes.getNamedItem("name")
							.getNodeValue(), locAttributes.getNamedItem(
							"device").getNodeValue(), locAttributes
							.getNamedItem("severity").getNodeValue(),
							locAttributes.getNamedItem("date").getNodeValue(),
							locAttributes.getNamedItem("longdate")
									.getNodeValue(), locAttributes
									.getNamedItem("model").getNodeValue(),
							locAttributes.getNamedItem("cond").getNodeValue());
					this.alarms.add(alarm);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.map:
			Intent i = new Intent(AlarmsTab.this, MyCurrentLocation.class);
			Bundle b = new Bundle();
			b.putString("asset", asset);
			b.putString("modelname", modelName);
			b.putString("alarmname", alarmname);
			i.putExtras(b);
			myProgressDialog = ProgressDialog.show(this, "Locating you...",
					"Please wait ..", true);
			Handler handler = new Handler();
			new LoadingThread(handler).start();
			startActivity(i);
			break;

		case R.id.alarm:
			Intent myIntent = new Intent(this, Main.class);

			Bundle bund = new Bundle();
			bund.putString("assetName", asset);
			bund.putString("modelname", modelName);
			myIntent.putExtras(bund);
			startActivity(myIntent);
			finish();
			break;

		case R.id.logout:
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
			break;

		case R.id.search:

			setContentView(R.layout.autocomplete);
			actv = (AutoCompleteTextView) findViewById(R.id.txtAssets);
			actv.setThreshold(1);
			arradapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, deviceName);

			actv.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keycode, KeyEvent keyevent) {

					if (!TextUtils.isEmpty(actv.getText())) {

						getAllAssets(actv.getText().toString());
						actv.setAdapter(arradapter);
						return false;
					}

					return false;
				}
			});

			actv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Intent i = new Intent(AlarmsTab.this, TabBar.class);
					Bundle b = new Bundle();
					b.putString("assetname", actv.getText().toString());
					b.putString("modelname", modelName);
					i.putExtras(b);
					startActivity(i);
				}

			});
			break;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {

			onBackPressed();
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onBackPressed() {
		Intent myIntent = new Intent(AlarmsTab.this, Main.class);

		Bundle b = new Bundle();

		b.putString("asset", asset);

		myIntent.putExtras(b);
		startActivity(myIntent);
		finish();

		return;
	}

	public void CloseAlarm(String asset, String mod_num, String alarmName) {

		try {
			AxedaMonitor app = (AxedaMonitor) getApplication();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new NameValuePair("deviceName", asset));
			params.add(new NameValuePair("alarmName", alarmName));
			params.add(new NameValuePair("modelName", mod_num));

			alresp = app.callScripto("Android_CloseAlarm", params);

		} catch (NullPointerException npe) {
			Toast.makeText(this, "Session Timed Out. Please login",
					Toast.LENGTH_LONG).show();
			intent = new Intent(this, Login.class);
			startActivity(intent);
		}

	}

	class LoadingThread extends Thread {

		Handler handler;

		public LoadingThread(Handler handler) {
			super();
			this.handler = handler;
		}

		public void run() {

			try {

				sleep(5000);

			} catch (InterruptedException e) {

			}
			handler.post(new FinishThread());
		}
	}

	class FinishThread extends Thread {

		public void run() {

			if (myProgressDialog != null)
				myProgressDialog.dismiss();
		}
	}

	public void getAllAssets(String searchfilter) {
		AxedaMonitor app = (AxedaMonitor) getApplication();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new NameValuePair("searchfilter", searchfilter));

		try {
			String assetresp = app.callScripto("Android_searchAssets", params);
			Log.i("scripto", "resp " + assetresp);
			Log.i("scripto", "devices" + deviceName);
			deviceName.clear();
			if (!assetresp.contains("<Response />")) {
				parseAssets(assetresp);
			}
		} catch (NullPointerException npe) {
			Toast.makeText(this, "Session Timed Out. Please login",
					Toast.LENGTH_LONG).show();
			intent = new Intent(this, Login.class);
			startActivity(intent);
		}
	}

	public void parseAssets(String assetresp) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();

			StringReader reader = new StringReader(assetresp);
			InputSource inputSource = new InputSource(reader);
			Document document = documentBuilder.parse(inputSource);

			Node responseNode = document.getDocumentElement();
			NodeList locChildNodes = responseNode.getChildNodes();
			NamedNodeMap locAttributes = null;

			for (int i = 0; i < locChildNodes.getLength(); i++) {
				if (locChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					locAttributes = locChildNodes.item(i).getAttributes();
					Asset asset = new Asset(locAttributes.getNamedItem("name")
							.getNodeValue(), locAttributes
							.getNamedItem("model").getNodeValue());
					this.assets.add(asset);
					modelName = asset.model;
					deviceName.add(asset.name);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

}
