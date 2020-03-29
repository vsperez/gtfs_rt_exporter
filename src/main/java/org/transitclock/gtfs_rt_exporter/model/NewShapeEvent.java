package org.transitclock.gtfs_rt_exporter.model;

import java.util.Date;

import org.transitclock.gtfs_rt_exporter.service.MultiDatePattern;

public class NewShapeEvent {
	
	Date initDateTime; //UTC==JUST LIKE GPS
	Date endDateTime; //UTC 
	String tripId;
	String tripDate;
	String tripTime;
	String kmlFile;
	public String getTripDate() {
		return tripDate;
	}
	public void setTripDate(String tripDate) {
		this.tripDate = tripDate;
	}
	public String getTripTime() {
		return tripTime;
	}
	public void setTripTime(String tripTime) {
		this.tripTime = tripTime;
	}
	
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
		if(values.length!=6)
			throw new Exception("NewShapeEvent must have 6 colums. Is the file limiter , or ;?");
		NewShapeEvent event=new NewShapeEvent();
		event.initDateTime=MultiDatePattern.parseDate(values[0]);
		event.endDateTime=MultiDatePattern.parseDate(values[1]);
		event.tripId=values[2];
		event.tripDate=values[3];
		event.tripTime=values[4];
		event.kmlFile=values[5];
		
		return event;
	}
}
