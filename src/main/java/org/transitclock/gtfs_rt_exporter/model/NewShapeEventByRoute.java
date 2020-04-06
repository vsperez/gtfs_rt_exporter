package org.transitclock.gtfs_rt_exporter.model;

import java.util.Date;

import org.transitclock.gtfs_rt_exporter.service.MultiDatePattern;

public class NewShapeEventByRoute implements ShapeEvent {
	
	Date initDateTime; //UTC==JUST LIKE GPS
	Date endDateTime; //UTC 
	String routeId;
	Integer direction;
	public Integer getDirection() {
		return direction;
	}
	public void setDirection(Integer direction) {
		this.direction = direction;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	String kmlFile;
	
	@Override
	public Date getInitDateTime() {
		return initDateTime;
	}
	public void setInitDateTime(Date initDateTime) {
		this.initDateTime = initDateTime;
	}
	@Override
	public Date getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	
	public String getKmlFile() {
		return kmlFile;
	}
	public void setKmlFile(String kmlFile) {
		this.kmlFile = kmlFile;
	}
	
	public static ShapeEvent fromString(String line) throws Exception {
		//Separator
		String[] values=line.split(",|\\;");
		if(values.length!=5)
			throw new Exception("NewShapeEventByRoute must have 5 colums. Is the file limiter , or ;?");
		NewShapeEventByRoute event=new NewShapeEventByRoute();
		event.initDateTime=MultiDatePattern.parseDate(values[0]);
		event.endDateTime=MultiDatePattern.parseDate(values[1]);
		event.routeId=values[2];
		event.direction= Integer.parseInt(values[3]);
		event.kmlFile=values[4];
		
		return event;
	}
}
