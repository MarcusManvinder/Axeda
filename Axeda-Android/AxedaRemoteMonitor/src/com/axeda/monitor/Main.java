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
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class Main extends Activity {

	ImageView image;
	ListView list;
	AlarmsListViewAdapter adapter;
	ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	ArrayList<Asset> assets = new ArrayList<Asset>();
	AutoCompleteTextView actv;
	ArrayAdapter<String> arradapter;
	ArrayList<String> deviceName = new ArrayList<String>(); // used for device
	Intent intent;
	String alarmresp;
	String modelName;
	String device;

	ProgressDialog myProgressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listview);
		setTitle("List of Active Alerts for All Assets");
		list = (ListView) findViewById(R.id.list);

		adapter = new AlarmsListViewAdapter(this, alarms);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View view,
					int position, long id) {

				Intent i = new Intent(Main.this, TabBar.class);
				Bundle b = new Bundle();

				b.putString("assetname", alarms.get(position).deviceName);
				b.putString("modelname", alarms.get(position).model);
				b.putString("alarmname", alarms.get(position).name);

				i.putExtras(b);
				startActivity(i);
				finish();
			}
		});
		
		getAlarms();

	}

	public void getAlarms() {
		AxedaMonitor app = (AxedaMonitor) getApplication();
		try {
			alarmresp = app.callScripto("Android_getAlarms", null);

			if (!alarmresp.contains("<Response />")) {
				parseAlarm(alarmresp);
			} else {
				alarms.add(new Alarm("There are no Alerts", null, null, null,
						null, null, null));
			}
		} catch (NullPointerException npe) {
			Toast.makeText(this, "Session Timed Out. Please login",
					Toast.LENGTH_LONG).show();
			intent = new Intent(this, Login.class);
			startActivity(intent);
		}
	}

	public void parseAlarm(String xmlresp) {
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

					Alarm alarm = new Alarm(
							locAttributes.getNamedItem("name").getNodeValue(), 
							locAttributes.getNamedItem("device").getNodeValue(), 
							locAttributes.getNamedItem("severity").getNodeValue(),
							locAttributes.getNamedItem("date").getNodeValue(),
							locAttributes.getNamedItem("longdate").getNodeValue(), 
							locAttributes.getNamedItem("model").getNodeValue(),
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

	public void getAllAssets(String searchfilter) {
		AxedaMonitor app = (AxedaMonitor) getApplication();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		
			params.add(new NameValuePair("searchfilter", searchfilter));

		
		try {
			String assetresp = app.callScripto("Android_searchAssets", params);
			Log.i("scripto","resp " + assetresp);
			Log.i("scripto" ,"devices" + deviceName);
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
					Asset asset = new Asset(
						locAttributes.getNamedItem("name").getNodeValue(), 
						locAttributes.getNamedItem("model").getNodeValue()
						);
				this.assets.add(asset);
				modelName = asset.model;
				deviceName.add(asset.name );
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
			Intent i = new Intent(Main.this, MyCurrentLocation.class);
			startActivity(i);
			break;

		case R.id.alarm:

			break;

		case R.id.logout:
			Intent intent = new Intent(Main.this, Login.class);
			startActivity(intent);
			break;

		case R.id.search:
		
			setContentView(R.layout.autocomplete);
			actv = (AutoCompleteTextView) findViewById(R.id.txtAssets);
			actv.setThreshold(1);
			arradapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, deviceName);
			
			actv.setOnKeyListener(new View.OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keycode, KeyEvent keyevent) {
				
					if(!TextUtils.isEmpty(actv.getText())){
				
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
					Intent i = new Intent(Main.this, TabBar.class);
					Bundle b = new Bundle();
					b.putString("assetname", assets.get(arg2).name);
					b.putString("modelname", assets.get(arg2).model);
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

	@Override
	public void onBackPressed() {

		return;
	}

	class LoadingThread extends Thread {

		Handler handler;

		public LoadingThread(Handler handler) {
			super();
			this.handler = handler;
		}

	}

}
