package org.transitclock.gtfs_rt_exporter.service;

import java.util.List;

import org.transitclock.gtfs_rt_exporter.model.CustomVehiclePosition;

public interface NewShapeEventReader {

	/**
	 * 
	 * @param timeStep
	 * @return Gets a list of CustomVehiclePosition from the last get given a time step
	 * it is select * from Table where timeStamp beween lastRead and timeStep, where lastRead
	 * is manage inside the class.
	 */
	List<CustomVehiclePosition> getCustomPostions(long timeStep);

}
