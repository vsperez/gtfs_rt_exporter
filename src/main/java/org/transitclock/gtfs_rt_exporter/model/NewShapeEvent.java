package org.transitclock.gtfs_rt_exporter.model;

import java.sql.Date;

public class NewShapeEvent {
	
	Date initDateTime; //UTC==JUST LIKE GPS
	Date endDateTime; //UTC 
	String tripId;
	String kmlFile;
	public Date getInitDateTime() {
		return initDateTime;
	}
	public void setInitDateTime(Date initDateTime) {
		this.initDateTime = initDateTime;
	}
	public Date getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
	public String getKmlFile() {
		return kmlFile;
	}
	public void setKmlFile(String kmlFile) {
		this.kmlFile = kmlFile;
	}
}
