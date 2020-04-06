package org.transitclock.gtfs_rt_exporter.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.transitclock.gtfs_rt_exporter.model.NewShapeEventByRoute;
import org.transitclock.gtfs_rt_exporter.model.ShapeEvent;

/**
 * Read events from txt file.
 * @author vperez
 *
 */
public class NewShapeEventReaderByRouteFileImpl implements NewShapeEventReader {
	String fileName;
	
	Logger _log=LogManager.getLogger(NewShapeEventReaderByRouteFileImpl.class);

	List<ShapeEvent> myList;//The file should be small..so to memory
	
	public NewShapeEventReaderByRouteFileImpl(String file)
	{
		if(file==null)
			file="./newShapeEvent.txt";
		this.fileName=file;
	}
	@PostConstruct
	void init() throws IOException	
	{
		myList=new ArrayList<ShapeEvent>();
		File file=new File(fileName);    //creates a new file instance  
		FileReader fr=new FileReader(file);   //reads the file  
		BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream
		String line; 
		int i=0;
		while((line=br.readLine())!=null)  
		{   i++;
			try
			{
				ShapeEvent event=NewShapeEventByRoute.fromString(line);
				myList.add(event);
			}
			catch (Exception e) {
				_log.error("Could not read line " +i,e);
			}
		}
		br.close();
		fr.close();
	
		
	}
	
	
	@Override
	public List<ShapeEvent> getNewShapeEvents(Date gpsEventTime) {
		return myList.stream().filter(obj->obj.getInitDateTime().before(gpsEventTime) && obj.getEndDateTime().after(gpsEventTime)).collect(Collectors.toList());
	}

}
