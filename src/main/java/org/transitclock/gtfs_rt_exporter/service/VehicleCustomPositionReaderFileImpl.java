package org.transitclock.gtfs_rt_exporter.service;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.transitclock.gtfs_rt_exporter.model.CustomVehiclePosition;
import org.transitclock.gtfs_rt_exporter.model.NewShapeEvent;

/**
 * 
 * @author vperez
 *
 */
public class VehicleCustomPositionReaderFileImpl implements VehicleCustomPositionReader {
	
	
	String fileName;
	public VehicleCustomPositionReaderFileImpl(String file)
	{
		if(file==null)
			file="./customPositions.txt";
		this.fileName=file;
	}
	Logger _log=LogManager.getLogger(VehicleCustomPositionReaderFileImpl.class);
	int maxLinesInList=10000;
	int currentLine=0;
	int currentLineReading=0;
	Date currentDate;
	int currentIndex;
	List<CustomVehiclePosition> myList;
	BufferedReader br;
	FileReader fr;
	
	@PostConstruct
	void init() throws IOException	
	{
		myList=new ArrayList<CustomVehiclePosition>();
		File file=new File(fileName);    //creates a new file instance  
		fr=new FileReader(file);   //reads the file  
		getNextEvents();
		
		br=new BufferedReader(fr);  //creates a buffering character input stream
		
	}
	private void getNextEvents() throws IOException
	{
		
		synchronized (myList) {
			currentLineReading=0;
			myList.clear();
			String line;
			currentIndex=0;
			while((line=br.readLine())!=null && currentLineReading<maxLinesInList)  
			{  
				currentLine++;
				currentLineReading++;
				try
				{
					CustomVehiclePosition event=CustomVehiclePosition.fromString(line);
					myList.add(event);
				}
				catch (Exception e) {
					_log.error("Could not read line " +currentLine,e);
				}
			
			}
			currentDate=myList.get(0).getGpsDate();
			
		}
	}
	
	@Override
	/**
	 * Returns a list of  CustomVehiclePosition since last GPS reading until last GPS reading+timeStep
	 *
	 */
	public synchronized List<CustomVehiclePosition> getCustomPostions(long timeStep) throws IOException {
		List<CustomVehiclePosition> returnList=new ArrayList<CustomVehiclePosition>();
		Date searchUntil=new Date(currentDate.getTime()+timeStep);

		for(int i=currentIndex;i<maxLinesInList;i++,currentIndex++)
		{
			CustomVehiclePosition currentValue = myList.get(i);
			if(currentValue.getGpsDate().after(searchUntil))
				break;
			returnList.add(currentValue);
		}
		if(currentIndex==maxLinesInList)
		{
			getNextEvents(); 
		}
		return returnList;
	}
	@PreDestroy
	public void close() throws IOException {
		br.close();
		fr.close();
		
	}


	
	
}
