package com.axeda.monitor;

public class Asset {

	String name;
	String model;
	String lat;
	String lng;
	String condition;
	String currentlocation;

	public Asset(String name, String model, String lat, String lng,
			String condition, String currentlocation) {
		super();
		this.name = name;
		this.model = model;
		this.lat = lat;
		this.lng = lng;
		this.condition = condition;
		this.currentlocation = currentlocation;
	}

	public Asset(String name, String model) {
		super();
		this.name = name;
		this.model = model;
	}

}
