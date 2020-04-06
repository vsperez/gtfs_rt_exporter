package org.transitclock.gtfs_rt_exporter.model;

import java.util.Date;

public interface ShapeEvent {

	Date getInitDateTime();

	Date getEndDateTime();

	String getKmlFile();

}
