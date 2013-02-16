package com.axeda.monitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DataTab extends ListActivity {
	/** Called when the activity is first created. */
	String diresp;
	String dival;
	String diname;
	String modname;
	String asset;
	int pos;
	String dataItemName = new String();
	ArrayList<Asset> assets = new ArrayList<Asset>();
	ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	ArrayAdapter<String> adapter;
	public String dataItempos;
	AutoCompleteTextView actv;
	ArrayList<String> displayData = new ArrayList<String>();
	ArrayList<String> deviceName = new ArrayList<String>();
	ArrayAdapter<String> arradapter;
	String dataitemname;
	String dataitemval;
	String modelName;
	String modelType;
	String alarmname;
	Intent intent;
	ProgressDialog myProgressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent myIntent = new Intent();
		myIntent = this.getIntent();
		asset = myIntent.getStringExtra("assetname");
		modelType = myIntent.getStringExtra("modelname");
		alarmname = myIntent.getStringExtra("alarmname");

		setContentView(R.layout.datattemview);
		getDataItemValues(asset);

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_checked, displayData);
		setListAdapter(adapter);

	}

	public void onListItemClick(ListView parent, final View v, int position,
			long id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(DataTab.this);

		alert.setTitle("Set DataItem");
		alert.setMessage("Set Value for " + displayData.get(position));

		dataItempos = displayData.get(position);
		pos = position;

		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Set", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();

				StringTokenizer st = new StringTokenizer(dataItempos, ":");
				if (st.hasMoreElements()) {
					dataItemName = st.nextToken();

				}
				String v = new String();
				v = dataItemName + ": " + value.toString();

				SetDataitem(asset, dataItemName.trim(), value.toString(),
						modelName);

				adapter.remove(dataItempos);
				adapter.add(v);
				adapter.notifyDataSetChanged();
			}

		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		alert.show();

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
			Intent i = new Intent(DataTab.this, MyCurrentLocation.class);
			Bundle b = new Bundle();
			b.putString("asset", asset);
			b.putString("alarmname", alarmname);
			b.putStringArrayList("AllAssets", deviceName);
			b.putString("modelname", modelType);
			i.putExtras(b);
			final ProgressDialog myProgressDialog = ProgressDialog.show(this,
					"Locating you...", "Please wait ..", true);
			Handler handler = new Handler();
			new LoadingThread(handler).start();
			startActivity(i);
			break;

		case R.id.alarm:

			Intent myIntent = new Intent(DataTab.this, Main.class);

			Bundle bund = new Bundle();
			bund.putString("asset", asset);
			bund.putString("modelname", modelType);

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
				public void onItemClick(AdapterView<?> arg0, View ap0tat0rg1,
						int arg2, long arg3) {
					Intent i = new Intent(DataTab.this, TabBar.class);
					Bundle b = new Bundle();
					b.putString("assetname", actv.getText().toString());
					b.putString("modelname", assets.get(arg2).model);
					i.putExtras(b);
					startActivity(i);
				}

			});
			break;

		}
		return true;
	}

	public void getDataItemValues(String asset_name) {
		try {
			AxedaMonitor app = (AxedaMonitor) getApplication();

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new NameValuePair("devicename", asset));
			diresp = app.callScripto("Android_getDataItemValues", params);

			parseDataItems(diname, dival, modname, diresp);
		} catch (NullPointerException npe) {
			Toast.makeText(this, "Session Timed Out. Please login",
					Toast.LENGTH_LONG).show();
			intent = new Intent(this, Login.class);
			startActivity(intent);
		}

	}

	public void parseDataItems(String diname, String dival, String modname,
			String xmlresp) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setCoalescing(true);
			dbf.setExpandEntityReferences(true);

			String xml = diresp.trim();
			ByteArrayInputStream stream = new ByteArrayInputStream(
					xml.getBytes());
			DocumentBuilder builder = dbf.newDocumentBuilder();

			Document doc = builder.parse(stream);

			Node titleNode = doc.getFirstChild();
			NodeList children = titleNode.getChildNodes();

			for (int i = 0; i < children.getLength(); ++i) {
				if (children.item(i).getNodeName() != "#text") {

					dataitemname = children.item(i).getAttributes()
							.getNamedItem("name").getNodeValue().toString();
					dataitemval = children.item(i).getAttributes()
							.getNamedItem("val").getNodeValue().toString();
					modelName = children.item(i).getAttributes()
							.getNamedItem("model").getNodeValue().toString();

					displayData.add(dataitemname + " : " + dataitemval);
					modname = modelName;

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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {

			onBackPressed();
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onBackPressed() {

		Intent myIntent = new Intent(DataTab.this, Main.class);

		Bundle b = new Bundle();
		b.putString("asset", asset);

		myIntent.putExtras(b);
		startActivity(myIntent);
		finish();

		return;
	}

	public void SetDataitem(String asset, String di_name, String value,
			String mod_num) {
		try {
			AxedaMonitor app = (AxedaMonitor) getApplication();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new NameValuePair("deviceName", asset));
			params.add(new NameValuePair("dataItemName", di_name));
			params.add(new NameValuePair("dataItemValue", value));
			params.add(new NameValuePair("modelNumber", modelName));
			diresp = app.callScripto("Android_setDataItemValue", params);

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