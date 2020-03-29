package org.transitclock.gtfs_rt_exporter.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.transitclock.gtfs_rt_exporter.model.CustomVehiclePosition;
import org.transitclock.gtfs_rt_exporter.model.KmlInfo;
import org.transitclock.gtfs_rt_exporter.model.NewShapeEvent;
import org.transitclock.gtfs_rt_exporter.util.KmlParser;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.LineString;

//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Geometry;
import com.google.transit.realtime.GtfsRealtime.FeedEntity.Builder;
import com.google.transit.realtime.GtfsRealtime.Shape;
import com.google.transit.realtime.GtfsRealtime.ShapeOrBuilder;
import com.google.transit.realtime.GtfsRealtime.ShapePoint;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.TripProperties;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
@Service
public class VehicleServiceImpl implements VehicleService {

	Logger logger=LogManager.getLogger(VehicleService.class);
	@Autowired
	VehicleCustomPositionReader _reader;
	@Autowired
	NewShapeEventReader _readerNewShape;
	@Autowired 
	GtfsRealtimeExporterImpl _exporter;
	Long currentTimeStamp;
	Long initTimeStamp;
	
	Map<String,FeedEntity.Builder> geometryMap=new HashMap<String,FeedEntity.Builder>();

	
	private List<NewShapeEvent> newShapesEvent =new ArrayList<NewShapeEvent>();
	//To keep in memory detours
	private HashMap<String, Geometry> shapeMap=new HashMap<String, Geometry>();
	/**
	 * To keep the last position of every vehicle
	 */
	private ConcurrentHashMap<String, CustomVehiclePosition> positions=new ConcurrentHashMap<String, CustomVehiclePosition>();

	

	@Override
	public Builder processPosition(CustomVehiclePosition position) {

		FeedEntity.Builder entity = FeedEntity.newBuilder();

		VehiclePosition.Builder vehicle 		= VehiclePosition.newBuilder();
		VehicleDescriptor.Builder  	vehicleDescriptor= VehicleDescriptor.newBuilder();

		vehicleDescriptor.setId(position.getVehicleIdentifier());
		entity.setId(position.getVehicleIdentifier());
		if(position.getPlateNumber()==null)
			position.setPlateNumber("SPN");
		vehicleDescriptor.setLabel(position.getPlateNumber());
		vehicleDescriptor.setLicensePlate(position.getPlateNumber());
		vehicle.setVehicle(vehicleDescriptor.build());
		com.google.transit.realtime.GtfsRealtime.Position.Builder   positionBuilder= com.google.transit.realtime.GtfsRealtime.Position.newBuilder();

		positionBuilder.setSpeed(position.getSpeed());//m/s
		positionBuilder.setBearing(position.getHeading());
		positionBuilder.setLatitude(position.getLatitude());//.floatValue());
		positionBuilder.setLongitude(position.getLongitude());//.floatValue());
		
		vehicle.setPosition(positionBuilder.build());
		//object.setOdometer(p.getOdometer()*1000);
		
		
		TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
		tripDescriptor.setDirectionId(position.getDirection());//We have to have direction
		tripDescriptor.setRouteId(position.getRouteId());
		if(position.getTripId()!=null)
		{
			tripDescriptor.setTripId(position.getTripId());
			tripDescriptor.setStartDate(position.getTripDate());
			tripDescriptor.setStartTime(position.getTripTime());
		}
		tripDescriptor.setScheduleRelationship(ScheduleRelationship.SCHEDULED);//ESTA PROGRAMADO
		vehicle.setTrip(tripDescriptor);
		vehicle.setTimestamp(position.getGpsDate().getTime()/1000L);
		entity.setVehicle(vehicle.build());
		return entity;
	}
	@Scheduled(fixedRateString= "1000")
	public void processNewInformation()
	{
		logger.info("Running schedule");
		try {
		long timeStep=30000;
		List<FeedEntity> entities = new ArrayList<FeedEntity>();
		//Get positions to update
		List<CustomVehiclePosition> positionList;
	
			positionList = _reader.getCustomPostions(timeStep);
		

		entities.clear();
		for(CustomVehiclePosition position: positionList)
		{
			//REPLACE OR CREATE VALUE
			logger.info("UPDATE VALUE FOR "+position.getVehicleIdentifier()+ " "+position);
			this.positions.put(position.getVehicleIdentifier(),position);

		}
		//Generate entities
		for(String key : positions.keySet())
		{
			Builder feedEntityBuilder=processPosition(positions.get(key));
			if(feedEntityBuilder!=null)
				entities.add(feedEntityBuilder.build());
		}
		_exporter.handleFullUpdate("vehicle",entities);
		if(positionList.size()==0)
			return;
		List<NewShapeEvent> listOfNewShapes=_readerNewShape.getNewShapeEvents(positionList.get(0).getGpsDate());
		entities.clear();
		for(NewShapeEvent event:listOfNewShapes)
		{
			
				try
				{
					Builder feedEntityBuilderShape=processNewShapeEventShape(event);
					String shapeId=feedEntityBuilderShape.getShapeBuilder().getShapeId();
					Builder feedEntityBuilder=processNewShapeEventTrip(event,shapeId);
					entities.add(feedEntityBuilder.build());
					entities.add(feedEntityBuilderShape.build());
				}
				catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			
			
		}
		_exporter.handleFullUpdate("newShape",entities);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private Builder processNewShapeEventShape(NewShapeEvent event) throws Exception {
		FeedEntity.Builder entity = FeedEntity.newBuilder();
		FeedEntity.Builder builder=geometryMap.get(event.getKmlFile());
		if(builder!=null)
			return builder;
		KmlParser reader=new KmlParser(event.getKmlFile());
		KmlInfo kmlInfo=new KmlInfo();
		System.out.println("event.getKmlFile() "+ event.getKmlFile());
		kmlInfo.setGeometry(reader.getGeometry());//We receive only one geometry.. We ignore the rests.
		kmlInfo.setName(reader.getName());
			
		com.google.transit.realtime.GtfsRealtime.Shape.Builder shapeBuilder = Shape.newBuilder();
		shapeBuilder.setShapeId(kmlInfo.getName());
		int i=1;
		double traveledDistance=0;
		Coordinate lastCoordinate=null;
		if(!(kmlInfo.getGeometry() instanceof LineString))
			throw new Exception("The shape must be a plolyline (Linestring)");
		for(Coordinate c: ((LineString)kmlInfo.getGeometry()).getCoordinates())
		{
			 com.google.transit.realtime.GtfsRealtime.ShapePoint.Builder pointBuilder = ShapePoint.newBuilder();
			 pointBuilder.setShapePtLat((float) c.getLatitude());
			 pointBuilder.setShapePtLon((float) c.getLongitude());
			 if(lastCoordinate!=null)
			 { 
				 traveledDistance+=getDistance(c.getLatitude(), c.getLongitude(), lastCoordinate.getLatitude(), lastCoordinate.getLongitude());
			 }
			
			pointBuilder.setShapeDistTraveled((float)traveledDistance);
			System.out.println("indice "+i);
			shapeBuilder.addShapePoint(pointBuilder);
		}
		entity.setShape(shapeBuilder);
		entity.setId(kmlInfo.getName());
		geometryMap.put(event.getKmlFile(),entity);
		return entity;
	}
	private Builder processNewShapeEventTrip(NewShapeEvent event,String shapeId) {
		
		FeedEntity.Builder entity = FeedEntity.newBuilder();
		com.google.transit.realtime.GtfsRealtime.TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
		com.google.transit.realtime.GtfsRealtime.TripDescriptor.Builder tripDescriptorBuilder = TripDescriptor.newBuilder();
		tripDescriptorBuilder.setTripId(event.getTripId());
		tripDescriptorBuilder.setStartDate(event.getTripDate());
		tripDescriptorBuilder.setStartTime(event.getTripTime());
		com.google.transit.realtime.GtfsRealtime.TripProperties.Builder propertiesBuidler = TripProperties.newBuilder();
	
		propertiesBuidler.setShapeId(shapeId);//TODO
		tripUpdate.setTripProperties(propertiesBuidler);
		tripUpdate.setTrip(tripDescriptorBuilder);
		entity.setTripUpdate(tripUpdate);
		entity.setId(event.getTripId()+shapeId);
		return entity;
	}
	
	public synchronized Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2){
		Double phim=(Math.toRadians(lat1) + Math.toRadians(lat2))/2.0;
		Double k1=111.13209-0.56605*Math.cos(2*phim)+0.0012*Math.cos(4*phim);
		Double k2=111.15113*Math.cos(phim)-0.09455*Math.cos(3*phim)+0.00012*Math.cos(5*phim);
		Double distancia=1000.0*Math.sqrt(k1*k1*(lat2-lat1)*(lat2-lat1)+k2*k2*(lon2-lon1)*(lon2-lon1));
		return distancia;
	}
	
}
