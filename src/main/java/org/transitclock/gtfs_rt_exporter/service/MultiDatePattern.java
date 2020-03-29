package org.transitclock.gtfs_rt_exporter.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultiDatePattern {

	
	static List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
	
	public static synchronized Date parseDate(String dateIn) throws ParseException
	{
		if(knownPatterns.isEmpty())
		{
			
			knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
			knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ssZ"));
			knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
			knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
		}

		for (SimpleDateFormat pattern : knownPatterns) {
			try {
				return new Date(pattern.parse(dateIn).getTime());

			} catch (ParseException pe) {
				// Loop on
			}
		}
		
		throw new ParseException("No known Date format found", 0);
	}
}
