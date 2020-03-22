package org.transitclock.gtfs_rt_exporter.service;

import java.util.ArrayList;
import java.util.List;

import org.transitclock.gtfs_rt_exporter.model.CustomVehiclePosition;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;

public class VehicleCustomPositionReaderFileImpl implements VehicleCustomPositionReader {
	//The file should be sort by gpsTime
	@Override
	public List<CustomVehiclePosition> getCustomPostions(long timeStep) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
