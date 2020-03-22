package org.transitclock.gtfs_rt_exporter.service;

import org.transitclock.gtfs_rt_exporter.model.CustomVehiclePosition;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
/**
 * 
 * @author vperez
 *
 */
public interface VehicleService {
	/**
	 * Cretant a feed entity builder given a GPS position
	 * This postion might be taken from any datasoruce database, txtFile
	 * @param position
	 * @return
	 */
	FeedEntity.Builder  processPosition(CustomVehiclePosition position);
}
