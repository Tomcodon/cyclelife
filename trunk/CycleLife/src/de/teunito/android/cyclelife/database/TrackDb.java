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
package de.teunito.android.cyclelife.database;

import static android.provider.BaseColumns._ID;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.preference.PreferenceManager;
import de.teunito.android.cyclelife.Preferences;
import de.teunito.android.cyclelife.model.HeartbeatData;
import de.teunito.android.cyclelife.model.IObservable;
import de.teunito.android.cyclelife.model.IObserver;
import de.teunito.android.cyclelife.model.TrackInfo;

public class TrackDb extends SQLiteOpenHelper implements IObservable {

	private static final String DB_NAME = "tracks.db";
	private static final int DB_VERSION = 6;
	private static TrackDb instance;
	private ArrayList<IObserver> observers = new ArrayList<IObserver>();
	private Location prevLocation = null;
	private Location mCurrentLocation = null;
	private TrackInfo mCurrentTrack;
	private SQLiteDatabase writeableDB = this.getWritableDatabase();
	private SQLiteDatabase readableDB = this.getReadableDatabase();
	private Context mContext;

	// Constructor
	private TrackDb(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
	}
	
	public static synchronized TrackDb getInstance(Context ctx){
		if (TrackDb.instance == null){
			TrackDb.instance = new TrackDb(ctx);
		}
		return TrackDb.instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE tracks("
						+ _ID
						+ " INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT, starttime DATETIME NOT NULL, endtime DATETIME)");
		db.execSQL("CREATE TABLE positions("
						+ _ID
						+ " INTEGER PRIMARY KEY AUTOINCREMENT, trackid INTEGER NOT NULL, longitude REAL NOT NULL, latitude REAL NOT NULL, altitude REAL, altitudeUP REAL, speed REAL, accuracy REAL, distToPrev REAL, timestamp TIMESTAMP)");
		db.execSQL("CREATE TABLE heartbeats("
				+ _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, trackid INTEGER NOT NULL, heartbeat INTEGER NOT NULL, cadence INTEGER, timestamp TIMESTAMP)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int old, int neu) {
		if (old == 2 && neu == 3) {
			db.execSQL("ALTER TABLE positions ADD COLUMN distToPrev REAL");
		}
		if (old == 3 && neu == 4){
			db.execSQL("CREATE TABLE heartbeats(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, trackid INTEGER NOT NULL, heartbeat INTEGER NOT NULL, cadence INTEGER, timestamp TIMESTAMP)");
		}
		if(old==4 && neu == 5){
			db.execSQL("ALTER TABLE positions ADD COLUMN altitudeUP REAL");
		}
		if(old==5 && neu == 6){
			db.execSQL("CREATE INDEX IDX_HEARTBEAT_TRACKID ON heartbeats(trackid)");
			db.execSQL("CREATE INDEX IDX_POSITIONS_TRACKID ON positions (trackid)");
		}
	}

	public void insertPoint(Location location, long trackId) {
		ContentValues values = new ContentValues();
		values.put("trackid", trackId);
		values.put("latitude", location.getLatitude());
		values.put("longitude", location.getLongitude());
		if (location.hasAltitude()) {
			values.put("altitude", location.getAltitude());
			if (prevLocation != null) {
				if (prevLocation.getAltitude() <= location.getAltitude()) {
					values.put("altitudeUP",(location.getAltitude() - prevLocation.getAltitude()));
				}
			}
		}
		if (location.hasSpeed()) {
			values.put("speed", location.getSpeed());
		}
		if (location.hasAccuracy()) {
			values.put("accuracy", location.getAccuracy());
		}
		if (prevLocation != null) {
			values.put("distToPrev", location.distanceTo(prevLocation));
		}

		values.put("timestamp", location.getTime());
		writeableDB.insertOrThrow("positions", null, values);
		mCurrentLocation = location;
		updateTrackValues(trackId);
		notifyObservers(mCurrentTrack);
		prevLocation = location;
	}

	public long insertTrack(TrackInfo trackInfo) {
		mCurrentTrack = trackInfo;
		ContentValues values = new ContentValues();
		values.put("title", trackInfo.getTitle());
		values.put("description", trackInfo.getDescription());
		values.put("starttime", trackInfo.getStarttime());
		values.put("endtime", "");
		long rowid = writeableDB.insertOrThrow("tracks", null, values);
		mCurrentTrack.setId(rowid);
		return rowid;
	}

	public Cursor getAllTracks() {
		Cursor mcAllTracks = readableDB
				.rawQuery(
						"SELECT _id, title, strftime('%d.%m.%Y', starttime, 'unixepoch', 'localtime') as date, description, round((SELECT sum(distToPrev)/1000 FROM positions p WHERE p.trackid=t._id), 2) || ' km' as distance FROM tracks t ORDER BY starttime DESC",
						null);
		return mcAllTracks;
	}
	
	public double getAvgSpeed(long trackId) {
		Cursor c = readableDB.rawQuery("SELECT (avg(speed)*3.6) as avgspeed FROM positions WHERE trackid = ?", new String[]{String.valueOf(trackId)});
		c.moveToFirst();
		int idxAvgSpeed = c.getColumnIndex("avgspeed");
		double avg = c.getDouble(idxAvgSpeed);
		c.close();
		return avg;
	}
	
	public double getMaxSpeed(long trackId) {
		Cursor c = readableDB.rawQuery("SELECT (max(speed)*3.6) as maxspeed FROM positions WHERE trackid = ?", new String[]{String.valueOf(trackId)});
		c.moveToFirst();
		int idxMaxSpeed = c.getColumnIndex("maxspeed");
		c.moveToFirst();
		double max = c.getDouble(idxMaxSpeed);
		c.close();
		return max;
	}

	public double getAltitudeUp(long trackId){
		Cursor c = readableDB.rawQuery("SELECT sum(altitudeUP) as climb FROM positions p WHERE p.trackid= ?", new String[]{String.valueOf(trackId)});
		int idxAltitudeUp = c.getColumnIndex("climb");
		c.moveToFirst();
		double result = c.getDouble(idxAltitudeUp);
		c.close();
		return result;
	}
	
	public int getWaypoint(long trackId){
		Cursor c = readableDB.rawQuery("SELECT count(latitude) as waypoints FROM positions p WHERE p.trackid= ?", new String[]{String.valueOf(trackId)});
		int idxWayP = c.getColumnIndex("waypoints");
		c.moveToFirst();
		int result = c.getInt(idxWayP);
		c.close();
		return result;
	}
	
	public int getAvgHeartrate(long trackId){
		Cursor c = readableDB.rawQuery("SELECT avg(heartbeat) as avgheartbeat FROM heartbeats h WHERE h.trackid= ?", new String[]{String.valueOf(trackId)});
		int idxAvgHeartbeat = c.getColumnIndex("avgheartbeat");
		c.moveToFirst();
		int result = c.getInt(idxAvgHeartbeat);
		c.close();
		return result;
	}
	
	public int getMaxHeartrate(long trackId){
		Cursor c = readableDB.rawQuery("SELECT max(heartbeat) as maxheartbeat FROM heartbeats h WHERE h.trackid= ?", new String[]{String.valueOf(trackId)});
		int idxMaxHeartbeat = c.getColumnIndex("maxheartbeat");
		c.moveToFirst();
		int result = c.getInt(idxMaxHeartbeat);
		c.close();
		return result;
	}
	
	public int getAvgCadence(long trackId){
		Cursor c = readableDB.rawQuery("SELECT avg(cadence) as avgcadence FROM heartbeats h WHERE h.trackid= ?", new String[]{String.valueOf(trackId)});
		int idxAvgCadence = c.getColumnIndex("avgcadence");
		c.moveToFirst();
		int result = c.getInt(idxAvgCadence);
		c.close();
		return result;
	}
	
	public ArrayList getTrackInfo(long trackId){
		Cursor c = readableDB.rawQuery("SELECT _id , title , strftime('%Y-%m-%d' , starttime , 'unixepoch' , 'localtime') as date , description FROM tracks WHERE _id = ?", new String[]{String.valueOf(trackId)});
		int idxTitle = c.getColumnIndex("title");
		int idxDate = c.getColumnIndex("date");
		int idxDescription = c.getColumnIndex("description");
		c.moveToFirst();
		ArrayList<String> result = new ArrayList<String>();
		result.add(c.getString(idxTitle));
		result.add(c.getString(idxDate));
		result.add(c.getString(idxDescription));
		c.close();
		return result;
	}
	
	
	public TrackInfo getTrackDetails(long trackId) {
		TrackInfo trackInfo = new TrackInfo();
//		Cursor c = readableDB.rawQuery("SELECT tid, date, waypoints, avg(heartbeat) AS avgheartbeat"+
//				" FROM (SELECT t._id AS tid, strftime('%Y-%m-%d' , starttime , 'unixepoch' , 'localtime') AS date, count(latitude) AS waypoints"+
//				" FROM tracks t LEFT JOIN positions p ON t._id = p.trackid" +
//				" WHERE t._id = ?"+
//				" GROUP BY t._id , strftime('%Y-%m-%d' , starttime , 'unixepoch' , 'localtime'))"+
//				" LEFT JOIN heartbeats h ON tid = h.trackid GROUP BY tid, date, waypoints", new String[]{String.valueOf(trackId)});
//		c.moveToFirst();
		ArrayList info = getTrackInfo(trackId);
		String title = info.get(0).toString();
		String date = info.get(1).toString();
		String desc = info.get(2).toString();
		trackInfo.setTitle(title);
		trackInfo.setDate(date);
		trackInfo.setDescription(desc);
		trackInfo.setDistance(getDistance(trackId));
		trackInfo.setWaypoints(getWaypoint(trackId));
		trackInfo.setAltitudeUp(getAltitudeUp(trackId));
		trackInfo.setAvgSpeed(getAvgSpeed(trackId));
		trackInfo.setMaxSpeed(getMaxSpeed(trackId));
		trackInfo.setAvgHeartBeat(getAvgHeartrate(trackId));
		trackInfo.setMaxHeartBeat(getMaxHeartrate(trackId));
		trackInfo.setAvgCadence(getAvgCadence(trackId));
		
		return trackInfo;
		
	}

	public Cursor getPointsOfTrack(long trackId) {
		Cursor mcPoints = readableDB.query("positions", new String[] { "_id",
				"trackid", "longitude", "latitude", "timestamp" },
				"trackid = ?", new String[] { String.valueOf(trackId) }, null,
				null, "timestamp DESC");
		return mcPoints;
	}
	
	public double getDistance(long trackId){
		Cursor c = readableDB.rawQuery("SELECT sum(distToPrev) as distance FROM positions WHERE trackid = ?", new String[]{String.valueOf(trackId)});
		c.moveToFirst();
		int columnIndex = c.getColumnIndex("distance");
		double distance = c.getDouble(columnIndex)/1000;
		c.close();
		return distance;
	}
	

	public String getTrackTitle(long id) {
		Cursor c = readableDB.rawQuery("SELECT title FROM tracks WHERE _id = ?", new String[]{String.valueOf(id)});
		c.moveToFirst();
		int columnIndex = c.getColumnIndex("title");
		String title = c.getString(columnIndex);
		c.close();
		return title;
	}
	
	
	/**
	 * @param trackId
	 * @return an ArrayList with the Name of the track at the first positon (0), an array of heartbeats at the 2nd position and an array of timestamps at the 3rd position 
	 */
	public ArrayList getHeartBeatForChart(long trackId) {
		ArrayList result = new ArrayList();
		Cursor c = readableDB.rawQuery("SELECT heartbeat, timestamp, title FROM tracks t LEFT JOIN heartbeats h ON h.trackId = t._id WHERE t._id = ? ", new String[]{String.valueOf(trackId)});
		if(c.moveToFirst()){
			int idxHb = c.getColumnIndex("heartbeat");
			int idxTimestamp = c.getColumnIndex("timestamp");
			int idxTitle = c.getColumnIndex("title");
			String title = c.getString(idxTitle);
			result.add(title);
			double[] heartbeat = new double[c.getCount()];
			Date[] timestamp = new Date[c.getCount()];
			int i =  c.getPosition();
			while(!c.isLast()){
				int hb = c.getInt(idxHb);
				heartbeat[i]=hb;
				Date tsp = new Date(c.getLong(idxTimestamp)*1000);
				timestamp[i]=tsp;
				c.moveToNext();
				i++;
		}
		result.add(heartbeat);
		result.add(timestamp);
		c.close();
		}
		return result;
	}
	
	public ArrayList getCadenceForChart(long trackId) {
		ArrayList result = new ArrayList();
		Cursor c = readableDB.rawQuery("SELECT cadence, timestamp, title FROM tracks t LEFT JOIN heartbeats h ON h.trackId = t._id WHERE t._id = ? ", new String[]{String.valueOf(trackId)});
		if(c.moveToFirst()){
			int idxCa = c.getColumnIndex("cadence");
			int idxTimestamp = c.getColumnIndex("timestamp");
			int idxTitle = c.getColumnIndex("title");
			String title = c.getString(idxTitle);
			result.add(title);
			double[] cadence = new double[c.getCount()];
			Date[] timestamp = new Date[c.getCount()];
			int i =  c.getPosition();
			while(!c.isLast()){
				int ca = c.getInt(idxCa);
				cadence[i]=ca;
				Date tsp = new Date(c.getLong(idxTimestamp)*1000);
				timestamp[i]=tsp;
				c.moveToNext();
				i++;
		}
		result.add(cadence);
		result.add(timestamp);
		c.close();
		}
		return result;
	}

	public ArrayList getSpeedForChart(long trackId) {
		ArrayList result = new ArrayList();
		Cursor c = readableDB.rawQuery("SELECT (speed*3.6) as speed, (timestamp/1000) as timestamp, title FROM positions p LEFT JOIN tracks t ON p.trackId = t._id WHERE t._id = ? ", new String[]{String.valueOf(trackId)});
		if(c.moveToFirst()){
		int idxSpeed = c.getColumnIndex("speed");
		int idxTimestamp = c.getColumnIndex("timestamp");
		int idxTitle = c.getColumnIndex("title");
		String title = c.getString(idxTitle);
		result.add(title);
		double[] speed = new double[c.getCount()];
		Date[] timestamp = new Date[c.getCount()];
		int i =  c.getPosition();
		while(!c.isLast()){
			int sp = c.getInt(idxSpeed);
			speed[i]=sp;
			Date tsp = new Date(c.getLong(idxTimestamp)*1000);
			timestamp[i]=tsp;
			c.moveToNext();
			i++;
		}
		result.add(speed);
		result.add(timestamp);
		c.close();
		}
		return result;
	}
	
	public ArrayList getAltitudeForChart(long trackId) {
		ArrayList result = new ArrayList();
		Cursor c = readableDB.rawQuery("SELECT altitude, (timestamp/1000) as timestamp, title FROM positions p LEFT JOIN tracks t ON p.trackId = t._id WHERE t._id = ? ", new String[]{String.valueOf(trackId)});
		if(c.moveToFirst()){
		int idxAltitude = c.getColumnIndex("altitude");
		int idxTimestamp = c.getColumnIndex("timestamp");
		int idxTitle = c.getColumnIndex("title");
		String title = c.getString(idxTitle);
		result.add(title);
		double[] altitude = new double[c.getCount()];
		Date[] timestamp = new Date[c.getCount()];
		int i =  c.getPosition();
		while(!c.isLast()){
			int sp = c.getInt(idxAltitude);
			altitude[i]=sp;
			Date tsp = new Date(c.getLong(idxTimestamp)*1000);
			timestamp[i]=tsp;
			c.moveToNext();
			i++;
		}
		result.add(altitude);
		result.add(timestamp);
		c.close();
		}
		return result;
	}
	
	public void renameTrack(String title, long trackId) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		writeableDB.update("tracks", values, "_id = ?", new String[]{String.valueOf(trackId)});
	}

	/**
	 * Deletes the track and all its positions
	 * @param trackId
	 */
	public void deleteTrack(long trackId) {
		ContentValues values = new ContentValues();
		values.put("_id", trackId);
		writeableDB.beginTransaction();
		try {
			writeableDB.delete("tracks", "_id = ?", new String[] { String
					.valueOf(trackId) });
			writeableDB.delete("positions", "trackid = ?",
					new String[] { String.valueOf(trackId) });
			writeableDB.delete("heartbeats", "trackid = ?",
					new String[] { String.valueOf(trackId) });
			writeableDB.setTransactionSuccessful();
		} finally {
			writeableDB.endTransaction();
		}
	}

	
	private void updateTrackValues(long trackId){
		if(mCurrentTrack !=null && mCurrentTrack.getId()==trackId){
			Cursor c = readableDB.rawQuery("SELECT (max(speed)*3.6) as maxSpeed, (avg(speed)*3.6) as avgSpeed, sum(distToPrev) as totalDistance, sum(altitudeUP) as climb, max(altitude) as maxaltitude FROM positions WHERE trackid = ?", new String[]{String.valueOf(trackId)});
			c.moveToFirst();
			int idxMaxSpeed = c.getColumnIndex("maxSpeed");
			int idxAvgSpeed = c.getColumnIndex("avgSpeed");
			int idxDist = c.getColumnIndex("totalDistance");
			int idxAltitudeUp = c.getColumnIndex("climb");
			int idxMaxAltitude = c.getColumnIndex("maxaltitude");
			mCurrentTrack.setMaxSpeed(c.getDouble(idxMaxSpeed));
			mCurrentTrack.setAvgSpeed(c.getDouble(idxAvgSpeed));
			mCurrentTrack.setDistance(c.getDouble(idxDist)/1000);
			mCurrentTrack.setAltitudeUp(c.getDouble(idxAltitudeUp));
			mCurrentTrack.setMaxAltitude(c.getInt(idxMaxAltitude));
			mCurrentTrack.setCurrentLocation(mCurrentLocation);
			mCurrentTrack.setAvgCadence(getAvgCadence(trackId));
			c.close();
		}		
	}

	public void insertHeartBeatData(HeartbeatData data, long trackId) {
		mCurrentTrack.setZephyrData(data);
		ContentValues values = new ContentValues();
		values.put("trackid", trackId);
		values.put("heartbeat", data.currentPulse);
		values.put("cadence", data.cadence);
		values.put("timestamp", System.currentTimeMillis()/1000);
		writeableDB.insertOrThrow("heartbeats", null, values);
		notifyObservers(mCurrentTrack);
		}
	
	@Override
	public void addObserver(IObserver ob) {
		if (ob != null)
			observers.add(ob);
	}

	@Override
	public void notifyObservers(TrackInfo trackInfo) {
		for (IObserver o : observers) {
			o.update(trackInfo);
		}
	}

	@Override
	public void removeObserver(IObserver ob) {
		if (ob != null)
			observers.remove(ob);
	}
	
	@Override
	public void removeObservers() {
		if (!observers.isEmpty())
			observers.clear();
	}

	/**
	 * @param trackId
	 * @return
	 */
	public Integer[] getTrackZones(long trackId) {
		Integer[] zones = new Integer[3];
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		int maxheartrate = Integer.parseInt(prefs.getString(Preferences.MAX_HEARTRATE, "185"));
		Cursor c = readableDB.rawQuery("Select count(*) as under from heartbeats WHERE trackid="+trackId+" AND heartbeat <" + maxheartrate*0.60, null);
		c.moveToFirst();
		int idxUnder = c.getColumnIndex("under");
		zones[0] = c.getInt(idxUnder);
		c.close();
		Cursor cursor = readableDB.rawQuery("Select count(*) as GA from heartbeats WHERE trackid="+trackId+" AND heartbeat BETWEEN " + maxheartrate*0.60 + " and " + maxheartrate*0.85, null);
		cursor.moveToFirst();
		int idxGA = cursor.getColumnIndex("GA");
		zones[1] = cursor.getInt(idxGA);
		cursor.close();
		
		Cursor c1 = readableDB.rawQuery("Select count(*) as over from heartbeats WHERE trackid= "+trackId+" AND heartbeat >" + maxheartrate*0.85,null);
		c1.moveToFirst();
		int idxOver = c1.getColumnIndex("over");
		zones[2] = c1.getInt(idxOver);
		c.close();
		
		return zones;
	}





}
