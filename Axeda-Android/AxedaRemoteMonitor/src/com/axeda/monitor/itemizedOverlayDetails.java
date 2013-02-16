package com.axeda.monitor;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.sax.StartElementListener;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class itemizedOverlayDetails extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	Intent myintent;
	OverlayItem item;
	String session;

	public itemizedOverlayDetails(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));

	}

	public itemizedOverlayDetails(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;

	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

}
