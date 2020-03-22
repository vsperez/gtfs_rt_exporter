/**
 * Copyright (C) 2013 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.transitclock.gtfs_rt_exporter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;



import com.google.transit.realtime.GtfsRealtime.FeedMessage;

//import javax.inject.Inject;

/**
 * Private implementation of {@link GtfsRealtimeExporter}.
 * 
 * @author vperez
 * based on bdferris source fo onebusaway gtfsrealtime exporter
 */
@Service
public
class GtfsRealtimeExporterImpl  {
  
	
	
  public static final java.lang.String VERSION = "2.0";
  private FeedHeader _header;
  private Map<String, FeedEntity> _feedEntities;

  private FeedMessage _cachedFeed;
  private int _cacheExpireSecs=0;


  


  GtfsRealtimeExporterImpl() {
	  _feedEntities=new HashMap<>();
	  _cachedFeed =null;
  }
  GtfsRealtimeExporterImpl(int cacheExpireSecs) {
	  _feedEntities=new HashMap<>();
	   this._cacheExpireSecs=cacheExpireSecs;
	   _cachedFeed =null;
  }

  public void add(int cacheExpireSecs) {
	
	  synchronized(_feedEntities)
	  {
		  Map<String, FeedEntity> _feedEntitiesInt = _feedEntities;
		  if(_feedEntitiesInt!=null)
			  return;
	    if (cacheExpireSecs > 0) {
	    	
	    		_feedEntitiesInt = CacheBuilder.newBuilder()
	              .expireAfterWrite(cacheExpireSecs, TimeUnit.SECONDS)
	              .<String, FeedEntity>build().asMap();
	    		
	    		_feedEntities= _feedEntitiesInt;
	    }
	    else {
	    	_feedEntitiesInt = new HashMap<String, FeedEntity>();
	    	_feedEntities= _feedEntitiesInt;
	    }
	}
  }

  /****
   * {@link GtfsRealtimeSink} Interface
   ****/

  
  /*public synchronized void setFeedHeaderDefaults(FeedHeader header) {
    _header = header;
    _cachedFeed = null;
  }*/

 
  public synchronized void handleFullUpdate(List<FeedEntity> entities) {
	  synchronized (_feedEntities) {

		  Map<String,FeedEntity> _feedEntitiesInt =  _feedEntities;
		  if(_feedEntitiesInt==null)
		  {
			  this.add(_cacheExpireSecs);
			  _feedEntitiesInt =  _feedEntities;
		  }
		  _cachedFeed=null;
		  _feedEntitiesInt.clear();
		  for (FeedEntity entity : entities) {
			  _feedEntitiesInt.put(entity.getId(), entity);
		  }
		  _feedEntities=_feedEntitiesInt ;
	  }

  }

//  @Override
//  public synchronized void handleIncrementalUpdate(
//      GtfsRealtimeIncrementalUpdate update) {
//    _cachedFeed = null;
//
//    for (FeedEntity toAdd : update.getUpdatedEntities()) {
//      _feedEntities.put(toAdd.getId(), toAdd);
//    }
//    for (String toRemove : update.getDeletedEntities()) {
//      _feedEntities.remove(toRemove);
//    }
//
//    FeedMessage.Builder feed = FeedMessage.newBuilder();
//    feed.setHeader(createIncrementalHeader());
//    feed.addAllEntity(update.getUpdatedEntities());
//    for (String toRemove : update.getDeletedEntities()) {
//      FeedEntity.Builder entity = FeedEntity.newBuilder();
//      entity.setIsDeleted(true);
//      entity.setId(toRemove);
//      feed.addEntity(entity);
//    }
//
//    FeedMessage differentialFeed = feed.build();
//    for (GtfsRealtimeIncrementalListener listener : _listeners) {
//      listener.handleFeed(differentialFeed);
//    }
//    _incrementalIndex++;
//  }

  /****
   * {@link GtfsRealtimeSource} Interface
   ****/

  public synchronized FeedMessage getFeed() {
	  
	 
    if (_cachedFeed == null) {
    	
    	 
      FeedHeader.Builder header = FeedHeader.newBuilder();
      if (_header != null) {
        header.mergeFrom(_header);
      }
      header.setIncrementality(Incrementality.FULL_DATASET);
      header.setTimestamp(System.currentTimeMillis() / 1000);
      header.setGtfsRealtimeVersion(VERSION);

      synchronized (_feedEntities) {
     Map<String, FeedEntity> _feedEntitiesInt =  _feedEntities;
    	  if(_feedEntitiesInt==null)
    		  return null;
      FeedMessage.Builder feed = FeedMessage.newBuilder();
      feed.setHeader(header);
      feed.addAllEntity(_feedEntitiesInt.values());
      _cachedFeed=feed.build();
      }
    }
    return _cachedFeed;
  }

  
}
