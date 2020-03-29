package org.transitclock.gtfs_rt_exporter.model;

import java.util.Date;

import org.transitclock.gtfs_rt_exporter.service.MultiDatePattern;

public class CustomVehiclePosition {

	private String vehicleIndentifier;
	private String plateNumber;
	private float speed;
	private float latitude;
	private float longitude;
	private float heading;
	private String tripId;
	private String tripDate;
	private String tripTime;
	private String routeId;
	private int direction;
	private Date gpsDate;
	public Date getGpsDate() {
		return gpsDate;
	}

	public void setGpsDate(Date gpsDate) {
		this.gpsDate = gpsDate;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	
	
	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

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


	public String getVehicleIndentifier() {
		return vehicleIndentifier;
	}

	public void setVehicleIndentifier(String vehicleIndentifier) {
		this.vehicleIndentifier = vehicleIndentifier;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setHeading(float heading) {
		this.heading = heading;
	}


	public String getVehicleIdentifier() {
		return this.vehicleIndentifier;
	}

	public String getPlateNumber() {
		return this.plateNumber;
	}

	public void setPlateNumber(String plateNumber) {
		this.plateNumber=plateNumber;
		
	}
	//It must be in m/s
	public float getSpeed() {
		// TODO Auto-generated method stub
		return this.speed;
	}

	public float getHeading() {
		// TODO Auto-generated method stub
		return this.heading;
	}

	public static CustomVehiclePosition fromString(String line) throws Exception {
		String[] values=line.split(",|\\;");
		if(values.length!=12)
			throw new Exception("CustomVehiclePosition must have 12 colums. Found "+ values.length+ ". Is the file limiter , or ;?");
		CustomVehiclePosition custom=new CustomVehiclePosition();
		custom.setVehicleIndentifier(values[0]);//=MultiDatePattern.parseDate(values[0]);
		custom.setPlateNumber(values[1]);
		custom.setSpeed(Float.parseFloat(values[2]));
		custom.setLatitude(Float.parseFloat(values[3]));
		custom.setLongitude(Float.parseFloat(values[4]));
		custom.setHeading(Float.parseFloat(values[5]));
		custom.setTripId(values[6]);
		custom.setTripDate(values[7]);
		custom.setTripTime(values[8]);
		custom.setRouteId(values[9]);
		custom.setDirection(Integer.parseInt(values[10]));
		custom.setGpsDate(MultiDatePattern.parseDate(values[11]));
		
		
		return custom;
	}

	

}
