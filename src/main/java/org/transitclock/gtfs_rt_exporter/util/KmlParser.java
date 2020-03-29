package org.transitclock.gtfs_rt_exporter.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;


import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;


public class KmlParser {
	
	String name;
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

	Geometry geometry;
	public   KmlParser(String fileName) {
		String ret = "";
		Kml kml;
		kml = Kml.unmarshal(new File(fileName));

		
		
		geometry = parse(kml.getFeature());
		if(name==null)
			name=fileName;
		System.out.println("GOT "+this.geometry);
	}
	
	private static Geometry parse(Feature feature) {
		if(feature == null) return null;
		
		if(feature instanceof Document) {
			List<Feature> featureList = ((Document) feature).getFeature();
			for(Feature f : featureList)
				if(f instanceof Folder || f instanceof Placemark)
				{
					System.out.println(f.getName());
					return parse(f);
				}
		}
		else if(feature instanceof Folder) {
			List<Feature> featureList = ((Folder) feature).getFeature();
			for(Feature f : featureList)
				if(f instanceof Folder || f instanceof Placemark)
				{
					System.out.println(f.getName());
					return parse(f);
				}
				
		}
		else if(feature instanceof Placemark)
			return parse(((Placemark) feature).getGeometry());
		return null;
	}
	
	private static Geometry parse(Geometry geometry) {
		//if(geometry == null) return new LinkedList<Coordinate>();
		
		if(geometry instanceof Point)
			return ((Point) geometry);
		else if(geometry instanceof LineString)
			return ((LineString) geometry);
		else if(geometry instanceof Polygon) {
			Boundary boundary = ((Polygon) geometry).getOuterBoundaryIs();
			if(boundary != null) {
				LinearRing ring = boundary.getLinearRing();
				if(ring != null) return ring;
			}
		}
		else if(geometry instanceof MultiGeometry) {
			List<Geometry> geoList = ((MultiGeometry) geometry).getGeometry();
			for(Geometry g : geoList)
				if(g instanceof Point || g instanceof Polygon || g instanceof LineString)
					return parse(g);
		}
		return null;
		//return new LinkedList<Coordinate>();
	}
	
}
