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

public class itemizedOverlay extends ItemizedOverlay {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context mContext;
	Intent myintent;
	OverlayItem item;
	Asset asset;

	public itemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));

	}

	public itemizedOverlay(Drawable defaultMarker, Context context, Asset asset) {
		super(boundCenterBottom(defaultMarker));
		this.mContext = context;
		this.asset = asset;
	}

	@Override
	protected boolean onTap(int index) {
		item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.setPositiveButton("Details",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(mContext, TabBar.class);
						Bundle b = new Bundle();
						b.putString("assetname", asset.name);
						b.putString("modelname", asset.model);

						intent.putExtras(b);
						mContext.startActivity(intent);
					}
				});
		dialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		dialog.show();

		return true;
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
