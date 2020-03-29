package org.transitclock.gtfs_rt_exporter.model;

import java.io.Serializable;

import de.micromata.opengis.kml.v_2_2_0.Geometry;

//import com.vividsolutions.jts.geom.Geometry;

public class KmlInfo implements Serializable{

	String name;
	Geometry geometry;//We expect only one polyline.
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	
}
