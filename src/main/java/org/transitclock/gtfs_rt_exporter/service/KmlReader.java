package org.transitclock.gtfs_rt_exporter.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.gml2.GMLConstants;
import com.vividsolutions.jts.io.gml2.GMLHandler;

public class KmlReader {

	public static void main(String[] str)
	{
		KmlReader kml = new KmlReader("/tmp/B_R_NORMAL_2020.kml");
		
	}
	private List geoms;
	public List getGeoms() {
		return geoms;
	}
	public void setGeoms(List geoms) {
		this.geoms = geoms;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private String name;
	public KmlReader(String fileName)
	{

		try
		{
			XMLReader xr; 

			xr = XMLReaderFactory.createXMLReader();
			KMLHandler kmlHandler = new KMLHandler();
			xr.setContentHandler(kmlHandler);
			xr.setErrorHandler(kmlHandler);

			Reader r = new BufferedReader(new FileReader(fileName));
			LineNumberReader myReader = new LineNumberReader(r);
			xr.parse(new InputSource(myReader));

			this.geoms = kmlHandler.getGeometries();
			System.out.println("GEOMS"+this.geoms);
			this.name =kmlHandler.getName();
			if(this.name==null)//If the property name is not defined
				this.name=fileName;
			myReader.close();
			r.close();
			//kmlHandler.startElement(uri, name, qName, atts);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	class KMLHandler extends DefaultHandler
	{
		private List geoms = new ArrayList();;

		private GMLHandler currGeomHandler;
		private String lastEltName = null;
		private GeometryFactory fact = new FixingGeometryFactory();

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public KMLHandler()
		{
			super();
		}

		public List getGeometries()
		{
			return geoms;
		}

		/**
		 *  SAX handler. Handle state and state transitions based on an element
		 *  starting.
		 *
		 *@param  uri               Description of the Parameter
		 *@param  name              Description of the Parameter
		 *@param  qName             Description of the Parameter
		 *@param  atts              Description of the Parameter
		 *@exception  SAXException  Description of the Exception
		 */
		public void startElement(String uri, String name, String qName,
				Attributes atts) throws SAXException {
			System.out.println("STARTING"+ name);
			if (name.equalsIgnoreCase(GMLConstants.GML_POLYGON)) {
				currGeomHandler = new GMLHandler(fact, null);
			}
			if (name.equalsIgnoreCase("LineString")) {
				currGeomHandler = new GMLHandler(fact, null);
			}
//			if (name.equalsIgnoreCase(GMLConstants.GML_COORDINATES)) {
//				currGeomHandler = new GMLHandler(fact, null);
//			}
			if (currGeomHandler != null)
				currGeomHandler.startElement(uri, name, qName, atts);
			if (currGeomHandler == null) {
				lastEltName = name;
				//System.out.println(name);
			}
		}

		public void characters(char[] ch, int start, int length) throws SAXException 
		{
			if (currGeomHandler != null) {
				currGeomHandler.characters(ch, start, length);
			}
			else {
				String content = new String(ch, start, length).trim();
				if (content.length() > 0) {
					if(lastEltName.compareToIgnoreCase("name")==0)
					{
						this.name=content;
					}
					System.out.println(lastEltName + "= " + content);
				}
			}
		}

		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			if (currGeomHandler != null)
				currGeomHandler.ignorableWhitespace(ch, start, length);
		}
		GeometryFactory factroy=new GeometryFactory();
		/**
		 *  SAX handler - handle state information and transitions based on ending
		 *  elements.
		 *
		 *@param  uri               Description of the Parameter
		 *@param  name              Description of the Parameter
		 *@param  qName             Description of the Parameter
		 *@exception  SAXException  Description of the Exception
		 */
		public void endElement(String uri, String name, String qName)
				throws SAXException {
			// System.out.println("/" + name);
			System.out.println("currGeomHandler"+ currGeomHandler);
			if (currGeomHandler != null) {
				currGeomHandler.endElement(uri, name, qName);
				System.out.println("currGeomHandler");
				if (currGeomHandler.isGeometryComplete()) {
					Geometry g=null;
//					if(currGeomHandler instanceof com.vividsolutions.jts.geom.impl.CoordinateArraySequence)
//					{
//						g=factroy.createLinearRing(currGeomHandler.getGeometry().getCoordinates());
//					}
//					else
					 g = currGeomHandler.getGeometry();
					// System.out.println(g);
					geoms.add(g);
			
					// reset to indicate no longer parsing geometry
					currGeomHandler = null;
				}
			}

		}
	}
	/**
	 * A GeometryFactory extension which fixes structurally bad coordinate sequences
	 * used to create LinearRings.
	 * 
	 * @author mbdavis
	 * 
	 */
	class FixingGeometryFactory extends GeometryFactory
	{
		public LinearRing createLinearRing(CoordinateSequence cs)
		{
			if (cs.getCoordinate(0).equals(cs.getCoordinate(cs.size() - 1))) 
				return super.createLinearRing(cs);

			// add a new coordinate to close the ring
			CoordinateSequenceFactory csFact = getCoordinateSequenceFactory();
			CoordinateSequence csNew = csFact.create(cs.size() + 1, cs.getDimension());
			CoordinateSequences.copy(cs, 0, csNew, 0, cs.size());
			CoordinateSequences.copyCoord(csNew, 0, csNew, csNew.size() - 1);
			return super.createLinearRing(csNew);
		}
		
	

	}


}
