package com.axeda.monitor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.axeda.monitor.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.ZoomControls;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsTab extends MapActivity {

	ArrayList<String> deviceName = new ArrayList<String>();
	ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	// ArrayAdapter<String> adapter;
	ArrayList<Asset> assets = new ArrayList<Asset>();
	ArrayAdapter<String> arradapter;
	String cust;
	String model;
	String desc;
	AutoCompleteTextView actv;
	String session;
	String alarmresp;
	String asset;
	String locresp;
	String modelName;
	String assetloc;
	String alarmname;
	MapView mapView;
	MapController mc;
	GeoPoint p;
	GeoPoint isCurrent;
	Intent intent;

	ProgressDialog myProgressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent myIntent = new Intent();
		myIntent = this.getIntent();

		alarmname = myIntent.getStringExtra("alarmname");
		asset = myIntent.getStringExtra("assetname");
		modelName = myIntent.getStringExtra("modelname");
		// assetloc = myIntent.getStringExtra("assetlocation");

		setContentView(R.layout.assetdetails);

		getLocation(asset);
		getAlarmsForAsset(asset);

		try {
			showMap();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Google " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	public void showMap() throws IOException {

		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		mc = mapView.getController();

		mc.setZoom(14);

		// Adding zoom controls to Map
		@SuppressWarnings("deprecation")
		ZoomControls zoomControls = (ZoomControls) mapView.getZoomControls();
		zoomControls.setLayoutParams(new ViewGroup.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mapView.addView(zoomControls);
		mapView.displayZoomControls(true);

		for (Asset asset : assets) {

		
			double lat = Double.parseDouble(asset.lat);
			double lng = Double.parseDouble(asset.lng);
			List<Overlay> mapOverlays = mapView.getOverlays();

			Drawable drawable = getResources().getDrawable(R.drawable.arrow_icon);
			

			if (!asset.currentlocation.equals("true"))
			{Drawable drawable2 = getResources().getDrawable(R.drawable.red);
				{
			}
				itemizedOverlayDetails itemizedoverlay = new itemizedOverlayDetails(
						drawable, this);
				GeoPoint point = new GeoPoint((int) (lat * 1E6),
						(int) (lng * 1E6));
				isCurrent = point;
				Geocoder geoCoder = new Geocoder(getBaseContext(),
						Locale.getDefault());
				List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);
				String add = "";

				if (addresses.size() > 0) {
					for (int j = 0; j < addresses.get(0)
							.getMaxAddressLineIndex(); j++)
						add += addresses.get(0).getAddressLine(j) + "\n";
				}
				OverlayItem overlayitem = new OverlayItem(point, asset.name,
						add);
				itemizedoverlay.addOverlay(overlayitem);

				mapOverlays.add(itemizedoverlay);

				mc.setCenter(isCurrent);
				mc.animateTo(isCurrent);

			
			}

			TextView assetName = (TextView) findViewById(R.id.AssName);
			TextView modName = (TextView) findViewById(R.id.AssMod);

			{
				assetName.setText(asset.name);
				modName.setText(asset.model);

			}
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
			Intent i = new Intent(DetailsTab.this, MyCurrentLocation.class);
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

			bund.putString("asset", asset);
			bund.putString("modelname", modelName);
			bund.putString("alarmname", alarmname);
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
					Intent i = new Intent(DetailsTab.this, TabBar.class);
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

	public void getAlarmsForAsset(String asset_name) {

		AxedaMonitor app = (AxedaMonitor) getApplication();

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		if (asset != null) {
			params.add(new NameValuePair("devicename", asset));
			params.add(new NameValuePair("modelname", modelName));
			if (alarmname != null) {
				params.add(new NameValuePair("alarmname", alarmname));
			}
		} else {
			params.add(new NameValuePair("devicename", assetloc));
			params.add(new NameValuePair("modelname", modelName));
			if (alarmname != null) {
				params.add(new NameValuePair("alarmname", alarmname));

			}
		}

		alarmresp = app.callScripto("Android_getAlarmsForAssetDetails", params);
		if (!alarmresp.contains("<Response />")) {
			parseAlarm(cust, model, desc, alarmresp);
		} else

		{

		}

		try {

		} catch (NullPointerException npe) {
			if (session == null) {
				Toast.makeText(this, "Session Timed Out. Please login",
						Toast.LENGTH_LONG).show();
				intent = new Intent(this, Login.class);
				startActivity(intent);
			} else {

			}

		}
	}

	public void parseAlarm(String desc, String model, String cust,
			String xmlresp) {
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

			if (locChildNodes.getLength() > 0) {
				locAttributes = locChildNodes.item(1).getAttributes();
				model = locAttributes.getNamedItem("model").getNodeValue();
				cust = locAttributes.getNamedItem("cust").getNodeValue();
				
				if (desc != null) {
					desc = locAttributes.getNamedItem("desc").getNodeValue().trim();
				   }
					else{
						desc = "No Alarm Description";
					}
						
				

				TextView assetName = (TextView) findViewById(R.id.AssName);
				TextView modName = (TextView) findViewById(R.id.AssMod);
				TextView description = (TextView) findViewById(R.id.AssDesc);
				TextView customer = (TextView) findViewById(R.id.AssCustomer);

				if (asset != null) {
					assetName.setText(asset);
				} else {
					assetName.setText(assetloc);
				}

				modName.setText(modelName);
				customer.setText(cust);
				description.setText(desc);

			}
			TextView assetName = (TextView) findViewById(R.id.AssName);
			if (asset != "") {
				assetName.setText(asset);
			} else {
				assetName.setText(assetloc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

	public void getLocation(String dev_name) {

		AxedaMonitor app = (AxedaMonitor) getApplication();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new NameValuePair("devicename", asset));

		locresp = app.callScripto("Android_getLocationForADevice", params);
		try {
			if (!locresp.contains("<Response />")) {
				parseLocation(locresp);
			} else {

			}

		} catch (NullPointerException npe) {
			Toast.makeText(this, "Session Timed Out. Please login",
					Toast.LENGTH_LONG).show();
			intent = new Intent(this, Login.class);
			startActivity(intent);
		}

	}

	public void parseLocation(String xmlresp) {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		}

		StringReader reader = new StringReader(locresp);
		InputSource inputSource = new InputSource(reader);
		Document document = null;
		try {
			document = documentBuilder.parse(inputSource);
		} catch (SAXException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		Node responseNode = document.getDocumentElement();
		NodeList locChildNodes = responseNode.getChildNodes();
		NamedNodeMap locAttributes = null;

		for (int i = 0; i < locChildNodes.getLength(); i++) {

			if (locChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				locAttributes = locChildNodes.item(i).getAttributes();
				Asset assets = new Asset(locAttributes.getNamedItem("devName")
						.getNodeValue(), locAttributes.getNamedItem("model")
						.getNodeValue(), locAttributes.getNamedItem("lat")
						.getNodeValue(), locAttributes.getNamedItem("long")
						.getNodeValue(), null, locAttributes.getNamedItem(
						"currLoc").getNodeValue());
				this.assets.add(assets);
			}
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
		Intent myIntent = new Intent(DetailsTab.this, Main.class);
		startActivity(myIntent);
		finish();

		return;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
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