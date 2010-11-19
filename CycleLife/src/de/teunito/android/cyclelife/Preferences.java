/**
 * 
 */
package de.teunito.android.cyclelife;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author teunito
 * 
 */
public class Preferences extends PreferenceActivity {

	
	/**
	 * Preference keys
	 */
	public static final String MIN_TRACKING_INTERVAL = "minTrackingInterval";
	public static final String MIN_TRACKING_DISTANCE = "minTrackingDistance";
	public static final String MIN_REQUIRED_ACCURACY = "minRequiredAccuracy";
	public static final String MAX_BELIEVABLE_SPEED = "maxBelievableSpeed";
	public static final String BLUETOOTH_DISABLED = "disableBluetooth";
	public static final String CONNECT_BLUETOOTH_STARTUP = "bluetoothstartup";
	public static final String RENNRADNEWS_API_KEY = "rennradnewsApiKey";
	public static final String RENNRADNEWS_BIKE_ID = "rennradnewsBikeId";
	public static final String MAX_HEARTRATE = "heartrateMax";
	public static final String MIN_HEARTRATE = "heartrateMin";
	public static final String AGE = "age";
	public static final String WEIGHT = "weight";
	
	/**
	 * Default Values - take a look at preferences.xml
	 */
	
	public static final String DEFAULT_MIN_TRACKING_INTERVAL = "0";
	public static final String DEFAULT_MIN_TRACKING_DISTANCE = "10";
	public static final String DEFAULT_MIN_REQUIRED_ACCURACY = "250";
	public static final String DEFAULT_MAX_BELIEVABLE_SPEED = "30";

	
		
		
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.perferences);
	}

}
