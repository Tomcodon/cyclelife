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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Class to interact with the TrackingService
 * 
 * @author teunito
 * 
 */
public class TrackingServiceManager {
	
	private static TrackingServiceManager instance;

	private static final String TAG = "TrackingServiceManager";
	private TrackingService mService;
	private Context mContext;
	private Boolean isBound = false;
	private ServiceConnection mServiceConnection;
	
	private TrackingServiceManager(Context ctx) {
		this.mContext = ctx;
	}
	
	public static synchronized TrackingServiceManager getInstance(Context ctx){
		if(TrackingServiceManager.instance==null){
			TrackingServiceManager.instance = new TrackingServiceManager(ctx);
		}
		return TrackingServiceManager.instance;
	}

	public void start(long trackId, String btAddress) {
		if (isBound && mService != null) {
			mService.startTracking(trackId, btAddress);
		} else
			Log.i(TAG, "No Service bound! 1");
	}

	public void stop() {
		if (isBound && mService != null) {
			mService.stopTracking();
		} else
			Log.i(TAG, "No Service bound!");
	}

	public void pause() {
		if (isBound && mService != null) {
			mService.pauseTracking();
		} else
			Log.i(TAG, "No Service bound!");
	}

	public void resume(long trackId) {
		if (isBound && mService != null) {
			mService.resumeTracking(trackId);
		} else
			Log.i(TAG, "No Service bound!");
	}

	/**
	 * Method for binding the Service with client
	 */
	public boolean connectService() {

		mServiceConnection = new ServiceConnection() {
			/**
			 * This is called when the connection with the service has been
			 * established, giving us the service object we can use to interact
			 * with the service. We are communicating with our service through
			 * an IDL interface, so get a client-side representation of that
			 * from the raw service object.
			 */
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				TrackingServiceManager.this.mService = ((TrackingService.LocalBinder) service)
						.getService();

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				if (mService != null) {
					mService = null;
				}
			}
		};

		Intent mIntent = new Intent(mContext, TrackingService.class);
		this.isBound = this.mContext.bindService(mIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);
		return this.isBound;
	}

	public void disconnectService() {
		if (isBound) {
			this.mContext.unbindService(mServiceConnection);
			this.isBound = false;
		} else
			Log.i(TAG, "Service not bound at the moment!");
	}
}
