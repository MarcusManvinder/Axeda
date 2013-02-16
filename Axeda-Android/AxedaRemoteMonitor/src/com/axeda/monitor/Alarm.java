package com.axeda.monitor;

public class Alarm {

	public Alarm(String name, String deviceName, String severity, String date,
			String longDate, String model, String status) {
		super();
		this.name = name;
		this.deviceName = deviceName;
		this.severity = severity;
		this.date = date;
		this.longDate = longDate;
		this.model = model;
		this.status = status;
	}

	String name;
	String deviceName;
	String severity;
	String date;
	String longDate;
	String model;
	String status;
}
