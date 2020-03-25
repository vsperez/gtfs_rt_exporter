package org.transitclock.gtfs_rt_exporter.model;

import java.util.Date;

import org.transitclock.gtfs_rt_exporter.service.MultiDatePattern;

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
	public static NewShapeEvent fromString(String line) throws Exception {
		//Separator
		String[] values=line.split(",|\\;");
		if(values.length!=4)
			throw new Exception("NewShapeEvent must have 4 colums. Is the file limiter , or ;?");
		NewShapeEvent event=new NewShapeEvent();
		event.initDateTime=MultiDatePattern.parseDate(values[0]);
		event.endDateTime=MultiDatePattern.parseDate(values[1]);
		event.tripId=values[2];
		event.kmlFile=values[3];
		return event;
	}
}
