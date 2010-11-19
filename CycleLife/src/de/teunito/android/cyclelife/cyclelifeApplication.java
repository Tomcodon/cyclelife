/**
 * 
 */
package de.teunito.android.cyclelife;

import android.app.Application;
import de.teunito.android.cyclelife.model.Constants;

/**
 * Class for maintaining global application state
 * 
 * @author teunito
 */
public class cyclelifeApplication extends Application implements Constants {
	
	
	private boolean isTracking = false;
	private boolean isPaused = false;
	
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	public void setTracking(boolean value){
		this.isTracking = value;
	}
	
	public void setPaused(boolean value){
		this.isPaused = value;
	}
	
	public int getTrackingState(){
		if (!isTracking && !isPaused){
			return STATE_STOPPED;
		}
		if (!isTracking && isPaused){
			return STATE_PAUSED;
		}
		if(isTracking && !isPaused){
			return STATE_TRACKING;
		} else return STATE_UNDEFINED;
	}
}
