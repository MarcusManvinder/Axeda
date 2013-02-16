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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ZoomControls;
import android.widget.AdapterView.OnItemClickListener;

import com.axeda.monitor.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MyCurrentLocation extends MapActivity implements LocationListener {

	EditText txted = null;
	Button btnSimple = null;
	MapView MapView = null;
	MapController mc = null;
	Drawable defaultMarker = null;
	GeoPoint gp = null;
	// double latitude = 42.035698, longitude = -71.2362154; //Axeda HQ
	double latitude = 41.851939, longitude = -87.611659;
	ArrayList<Alarm> alarms = new ArrayList<Alarm>();
	String locresp;
	String assetName;
	String modelName;
	AutoCompleteTextView actv;
	ArrayList<Asset> assets = new ArrayList<Asset>();
	ArrayList<String> deviceName = new ArrayList<String>();
	ArrayAdapter<String> arradapter;
	Intent intent;

	private String bestProvider;
	private LocationManager lm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mapview);
		setTitle("Your proximity to your Assets");

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		List<String> providers = lm.getAllProviders();
		for (String provider : providers) {
			printProvider(provider);
		}

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);

		bestProvider = lm.getBestProvider(criteria, false);
		{
			Location location = lm.getLastKnownLocation(bestProvider);
			printLocation(location);

			if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
						1, this);
			} else {
				showGPSDisabledAlertToUser();

			}

			if (lm.isProviderEnabled("network")) {

				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
						1000, 1, this);
				lm.getLastKnownLocation("network");
			} else if (lm.isProviderEnabled("gps")) {

			}

			// Creating and initializing Map
			MapView = (MapView) findViewById(R.id.mapView);
			gp = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
			MapView.setSatellite(false);
			mc = MapView.getController();

			mc.setCenter(gp);
			mc.setZoom(12);

			// Add a location mark
			MyLocationOverlay myLocationOverlay = new MyLocationOverlay();
			List<Overlay> list = MapView.getOverlays();
			list.add(myLocationOverlay);

			// Adding zoom controls to Map
			@SuppressWarnings("deprecation")
			ZoomControls zoomControls = (ZoomControls) MapView
					.getZoomControls();
			zoomControls.setLayoutParams(new ViewGroup.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			MapView.addView(zoomControls);
			MapView.displayZoomControls(true);

			getAllLocations();
			try {
				showMap();
			} catch (IOException e) {
				e.printStackTrace();
			}

			final RadioButton radio_onemile = (RadioButton) findViewById(R.id.onemile);
			final RadioButton radio_tenmile = (RadioButton) findViewById(R.id.tenmile);
			final RadioButton radio_twentmile = (RadioButton) findViewById(R.id.twentymile);

			radio_onemile.setOnClickListener(radio_listener);
			radio_tenmile.setOnClickListener(radio_listener);
			radio_twentmile.setOnClickListener(radio_listener);

		}
	}

	private OnClickListener radio_listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Perform action on clicks
			RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
			int checkedRadioButton = radioGroup.getCheckedRadioButtonId();

			switch (checkedRadioButton) {
			case R.id.onemile:
				mc.setZoom(16);
				mc.setCenter(gp);
				break;
			case R.id.tenmile:
				mc.setZoom(13);
				mc.setCenter(gp);
				break;
			case R.id.twentymile:
				mc.setZoom(12);
				mc.setCenter(gp);
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.alarm:

			Intent myIntent = new Intent(MyCurrentLocation.this, Main.class);

			startActivity(myIntent);
			finish();
			break;

		case R.id.logout:
			Intent intent = new Intent(MyCurrentLocation.this, Login.class);
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
					Intent i = new Intent(MyCurrentLocation.this, TabBar.class);
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

	/* This method is called when use position will get changed */
	@Override
	public void onLocationChanged(Location location) {

		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();

			gp = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
			mc.animateTo(gp);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(MyCurrentLocation.this,
				"Provider disabled by the user. GPS turned off",
				Toast.LENGTH_LONG).show();

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(MyCurrentLocation.this,
				"Provider Enabled by the user. GPS turned on",
				Toast.LENGTH_LONG).show();

		Intent i = new Intent(this, MyCurrentLocation.class);
		startActivity(i);

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// required for interface, not used
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

		Intent myIntent = new Intent(MyCurrentLocation.this, Main.class);

		startActivity(myIntent);
		finish();

		return;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// /* User can zoom in/out using keys provided on keypad */

	protected class MyLocationOverlay extends com.google.android.maps.Overlay {

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			Paint paint = new Paint();

			super.draw(canvas, mapView, shadow);
			// Converts lat/lng-Point to OUR coordinates on the screen.
			Point myScreenCoords = new Point();
			mapView.getProjection().toPixels(gp, myScreenCoords);

			paint.setStrokeWidth(1);
			paint.setARGB(255, 255, 255, 255);
			paint.setStyle(Paint.Style.STROKE);

			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.android);

			canvas.drawBitmap(bmp, myScreenCoords.x, myScreenCoords.y, paint);
			canvas.drawText("", myScreenCoords.x, myScreenCoords.y, paint);

			return true;
		}
	}

	public void showMap() throws IOException {
		for (Asset asset : assets) {

			double lat = Double.parseDouble(asset.lat);
			double lng = Double.parseDouble(asset.lng);
			List<Overlay> mapOverlays = MapView.getOverlays();

			Drawable drawable = getResources().getDrawable(R.drawable.green);
			if (!asset.condition.equals("Good")) {
				drawable = getResources().getDrawable(R.drawable.red);
			}
			itemizedOverlay itemizedoverlay = new itemizedOverlay(drawable,
					this, asset);
			GeoPoint pointf = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
			Geocoder geoCoder = new Geocoder(getBaseContext(),
					Locale.getDefault());
			List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);

			String add = "";

			if (addresses.size() > 0) {
				for (int j = 0; j < addresses.get(0).getMaxAddressLineIndex(); j++)
					add += addresses.get(0).getAddressLine(j) + "\n";
			}
			OverlayItem overlayitem = new OverlayItem(pointf, asset.name, add);
			itemizedoverlay.addOverlay(overlayitem);

			mapOverlays.add(itemizedoverlay);

		}
	}

	public void getAllLocations() {
		AxedaMonitor app = (AxedaMonitor) getApplication();
		locresp = app.callScripto("Android_getAllLocations", null);
		parseLocation(locresp);
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
						.getNodeValue(), locAttributes.getNamedItem("cond")
						.getNodeValue(), null);
				this.assets.add(assets);
				modelName = assets.model;
				assetName = assets.name;
			}
		}

	}

	private void printProvider(String provider) {

	}

	private void printLocation(Location location) {
		if (location == null) {

		}

	}

	private void showGPSDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MyCurrentLocation.this);
		alertDialogBuilder
				.setMessage(
						"GPS is disabled in your device. Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Goto Settings Page To Enable GPS",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								Intent callGPSSettingIntent = new Intent(
										android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(callGPSSettingIntent);
							}
						});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
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
					deviceName.add(locAttributes.getNamedItem("name")
							.getNodeValue());
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
