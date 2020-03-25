package org.transitclock.gtfs_rt_exporter.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.transitclock.gtfs_rt_exporter.model.CustomVehiclePosition;
import org.transitclock.gtfs_rt_exporter.model.NewShapeEvent;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.vividsolutions.jts.geom.Geometry;
import com.google.transit.realtime.GtfsRealtime.FeedEntity.Builder;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
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
		entity.setVehicle(vehicle.build());
		return entity;
	}
	@Scheduled(fixedRateString= "30000")
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
		
		List<NewShapeEvent> listOfNewShapes=_readerNewShape.getNewShapeEvents(positionList.get(0).getGpsDate());
		
		entities.clear();
		for(NewShapeEvent event:listOfNewShapes)
		{
			Builder feedEntityBuilder=processNewShapeEvent(event);
			if(feedEntityBuilder!=null)
				entities.add(feedEntityBuilder.build());
			
		}
		_exporter.handleFullUpdate("newShape",entities);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private Builder processNewShapeEvent(NewShapeEvent event) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
