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
package de.teunito.android.cyclelife.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import de.teunito.android.cyclelife.cyclelifeApplication;
import de.teunito.android.cyclelife.Preferences;
import de.teunito.android.cyclelife.R;
import de.teunito.android.cyclelife.Speedometer;
import de.teunito.android.cyclelife.database.TrackDb;
import de.teunito.android.cyclelife.heartbeat.IBluetoothConnection;
import de.teunito.android.cyclelife.heartbeat.IHeartbeatObserver;
import de.teunito.android.cyclelife.heartbeat.ZephyrBluetoothConnection;
import de.teunito.android.cyclelife.model.Constants;
import de.teunito.android.cyclelife.model.HeartbeatData;

public class TrackingService extends Service implements LocationListener, IHeartbeatObserver,
		android.location.GpsStatus.Listener, Constants, SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "TrackingService";
	private static final int NOTIFY_ID = 1;

	private LocationManager mLocationManager;
	private TrackDb mTrackDb;
	private long trackId;
	private NotificationManager mNotificationManager;
	private int minRequiredAccuracy = 250;
	private Location prevLocation = null;
	private final IBinder mBinder = new LocalBinder();
	private Notification note;
	private cyclelifeApplication app;
	private BluetoothAdapter mBluetoothAdapter = null;
	private IBluetoothConnection mBtConnection;
	private int minTrackingDistance;
	private int minTrackingInterval;
	private int maxBelievableSpeed;


	/**
	 * Class for clients to access
	 */
	public class LocalBinder extends Binder {
		public TrackingService getService() {
			return TrackingService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		app = (cyclelifeApplication) getApplication();
		mNotificationManager = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mTrackDb = TrackDb.getInstance(this.getApplicationContext());
		
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
		onSharedPreferenceChanged(prefs, null);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

	}

	// onStart() deprecated,implement onStartCommand instead
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("TAG", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		setupNotification(TAG);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO
		disableNotification();
		if(mBtConnection!=null){
			mBtConnection.stop();
		}
		super.onDestroy();
		this.stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return this.mBinder;
	}

	@Override
	public void onLocationChanged(Location location) {
		Location filteredLocation = filterLocation(location);
//		 Location filteredLocation = location;
		// TODO Only for Debugging remove

		if (filteredLocation != null) {
			Log.i(TAG, "Received new valid Location: latitude: "
					+ filteredLocation.getLatitude() + " longitude: "
					+ filteredLocation.getLongitude());
			mTrackDb.insertPoint(filteredLocation, trackId);
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub
	}

	/**
	 * Some GPS locations received are of to low a quality for tracking use. In
	 * this Method we will filter them out.
	 * 
	 * @param mLocation
	 * @return the original or null when unacceptable
	 */
	public Location filterLocation(Location mLocation) {
		// Don't track a position if the accuracy is higher then configured in
		// the preferences
		if(mLocation != null){
			if (mLocation.hasAccuracy()) {
				if (mLocation.getAccuracy() > minRequiredAccuracy) {
					Log.i(TAG,	"A inacceptable location was recieved, based on inaccuracy... (received: " + mLocation.getAccuracy() + "; allowed: " + minRequiredAccuracy);
					return null;
				}
	
				/*
				 * Don't track a position if it is directly beside the previous
				 * Location or the inaccuracy is bigger then the distance to the
				 * previous Location
				 */
				if (prevLocation != null && mLocation.getAccuracy() > prevLocation.distanceTo(mLocation)) {
					Log.i(TAG, "A inacceptable location was recieved, based on distance to previous location and accuracy... accuracy:" + mLocation.getAccuracy() +" distance:" + prevLocation.distanceTo(mLocation));
					return null;
				}
			}
	
			/*
			 * checks if the speed is believable in relation to the distance of
			 * previous location and the time interval
			 */
			if (prevLocation != null) {
				float meters = mLocation.distanceTo(prevLocation);
				long seconds = (mLocation.getTime() - prevLocation.getTime()) / 1000;
				if ((meters / seconds) > maxBelievableSpeed) {
					Log.i(TAG, "A inacceptable location was recieved, based on really high speed...");
					return null;
				}
			}
		}
		prevLocation = mLocation;
		return mLocation;
	}
		

	public synchronized void startTracking(long trackId, String btAddress) {
		if (btAddress!=null && ((btAddress.length() == 17) && mBluetoothAdapter.isEnabled())) {
			mBtConnection = new ZephyrBluetoothConnection(getApplicationContext());
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btAddress);
			// sets also the listener
			mBtConnection.connect(device, this);
		}
		setupLocationUpdates();
		this.trackId = trackId;
		CharSequence notifyText = "TrackingService is running!";
		app.setTracking(true);
		setupNotification(notifyText);
	}


	public synchronized void stopTracking() {
		if (app.getTrackingState()==STATE_TRACKING || app.getTrackingState()==STATE_PAUSED) {
			disableNotification();
			mLocationManager.removeUpdates(this);
			app.setTracking(false);
			if(mBtConnection!=null){
				mBtConnection.stop();
			}
			stopSelf();
		} else
			Log
					.i(TAG,
							"Could not stop because service is not tracking at the moment");

	}

	public synchronized void resumeTracking(long trackId) {
		if (app.getTrackingState()==STATE_PAUSED) {
			this.trackId = trackId;
			setupLocationUpdates();
			CharSequence notifyText = "TrackingService is running!";
			app.setTracking(true);
			app.setPaused(false);
			setupNotification(notifyText);
		} else
			Log.i(TAG,
					"Could not resume because service is tracking already track "
							+ this.trackId);

	}

	public synchronized void pauseTracking() {
		if (app.getTrackingState()==STATE_TRACKING) {
			mLocationManager.removeUpdates(this);
			app.setTracking(false);
			app.setPaused(true);
			setupNotification("paused tracking!");
		} else
			Log
					.i(TAG,
							"Could not pause because service is not tracking at the moment");

	}

	/**
	 * Setup the notifications shown in the statusbar
	 * 
	 * @param Notification
	 *            Sequence
	 */
	private void setupNotification(CharSequence text) {
		Intent notificationIntent = new Intent(this, Speedometer.class);
		notificationIntent.putExtra("trackId", trackId);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		note = new Notification(R.drawable.ic_stat_notify_bike, text,
				System.currentTimeMillis());
		note.flags |= Notification.FLAG_NO_CLEAR;
		note.setLatestEventInfo(this.getApplicationContext(), text, text,
				contentIntent);
		mNotificationManager.notify(NOTIFY_ID, note);
	}

	private void updateNotification(CharSequence text) {
		Intent notificationIntent = new Intent(this, Speedometer.class);
		notificationIntent.putExtra("trackId", trackId);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		note.setLatestEventInfo(getApplicationContext(), text, text,
				contentIntent);
	}

	/**
	 * Disable the notifications shown in the statusbar
	 * 
	 * @param Notification
	 *            ID
	 */
	private void disableNotification() {
		mNotificationManager.cancel(NOTIFY_ID);
	}

	@Override
	public void pushHeartbeatInfo(HeartbeatData data) {
		mTrackDb.insertHeartBeatData(data, trackId);
		
	}

	@Override
	public void timeout() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(sharedPreferences != null){
		if (key == null || key.equals(Preferences.MIN_TRACKING_DISTANCE)) {
		      minTrackingDistance = Integer.parseInt(sharedPreferences.getString(
		    		  Preferences.MIN_TRACKING_DISTANCE, Preferences.DEFAULT_MIN_TRACKING_DISTANCE ));
		      
		    }
		if (key == null || key.equals(Preferences.MIN_TRACKING_INTERVAL)) {
		      minTrackingInterval = Integer.parseInt(sharedPreferences.getString(
		    		  Preferences.MIN_TRACKING_INTERVAL, Preferences.DEFAULT_MIN_TRACKING_INTERVAL ));
		      
		    }
		if (key == null || key.equals(Preferences.MIN_REQUIRED_ACCURACY)) {
		      minRequiredAccuracy = Integer.parseInt(sharedPreferences.getString(
		    		  Preferences.MIN_REQUIRED_ACCURACY, Preferences.DEFAULT_MIN_REQUIRED_ACCURACY ));
		      
		    }
		if (key == null || key.equals(Preferences.MAX_BELIEVABLE_SPEED)) {
		      maxBelievableSpeed = Integer.parseInt(sharedPreferences.getString(
		    		  Preferences.MAX_BELIEVABLE_SPEED, Preferences.DEFAULT_MAX_BELIEVABLE_SPEED ));
		      
		    }
		if(app.getTrackingState()==STATE_TRACKING)
			setupLocationUpdates();
		}
		
	}

	private void setupLocationUpdates() {
		mLocationManager.removeUpdates(this);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				minTrackingInterval*1000, minTrackingDistance, this);
		
	}
	
	

}
