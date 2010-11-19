/*******************************************************************************
 * Copyright 2010 Tobias Teunissen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.teunito.android.cyclelife.model;

import android.location.Location;


public class TrackInfo {

	private String title;
	private String description;
	private long starttime;
	private long endtime;
	private long id;
	private double maxSpeed;
	private double avgSpeed;
	private double distance;
	private double altitudeUp;
	private String date;
	private int avgHeartBeat;
	private int maxHeartBeat;
	private int maxAltitude;
	private int avgCadence;
	private int waypoints;
	
	private HeartbeatData zephyrData;
	private Location currentLocation;
	


	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}

	public void setEndtime(long endtime) {
		this.endtime = endtime;
	}

	public long getEndtime() {
		return endtime;
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}

	public long getStarttime() {
		return starttime;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;	
	}

	public void setAvgSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;	
	}

	public void setDistance(double distance) {
		this.distance = distance;		
	}


	public double getMaxSpeed() {
		return maxSpeed;
	}

	public double getAvgSpeed() {
		return avgSpeed;
	}

	public double getDistance() {
		return distance;
	}


	public void setZephyrData(HeartbeatData zephyrData) {
		this.zephyrData = zephyrData;
	}

	public HeartbeatData getZephyrData() {
		return zephyrData;
	}
	


	public double getAltitudeUp() {
		return altitudeUp;
	}

	public String getDate() {
		return date;
	}

	public int getAvgHeartBeat() {
		return avgHeartBeat;
	}

	public int getMaxHeartBeat() {
		return maxHeartBeat;
	}

	public int getAvgCadence() {
		return avgCadence;
	}


	public void setDate(String date) {
		this.date = date;
		
	}

	public void setAltitudeUp(double up) {
		this.altitudeUp = up;
		
	}

	public void setAvgHeartBeat(int heartbeat) {
		this.avgHeartBeat = heartbeat;
		
	}

	public void setMaxHeartBeat(int int1) {
		this.maxHeartBeat = int1;		
	}

	public void setAvgCadence(int int1) {
		this.avgCadence = int1;
		
	}

	public void setWaypoints(int wp) {
		this.waypoints = wp;
		
	}

	public int getWaypoints() {
		return waypoints;
	}

	/**
	 * @return
	 */
	public int getMaxAltitude() {
		return this.maxAltitude;
	}
	
	public void setMaxAltitude(int maxaltitude){
		this.maxAltitude = maxaltitude;
	}
	
	

}
