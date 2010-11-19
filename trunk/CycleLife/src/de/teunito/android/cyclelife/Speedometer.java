/**
 * 
 */
package de.teunito.android.cyclelife;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import de.teunito.android.cyclelife.database.TrackDb;
import de.teunito.android.cyclelife.model.Constants;
import de.teunito.android.cyclelife.model.IObserver;
import de.teunito.android.cyclelife.model.TrackInfo;
import de.teunito.android.cyclelife.service.TrackingServiceManager;

/**
 * @author teunito
 * 
 */
public class Speedometer extends Activity implements IObserver, Constants {

	// Options Menu
	private static final int MENU_TRACKING = 0;
	private static final int MENU_WAKELOCK = 1;
	private static final int MENU_MAP = 2;
	private static final int MENU_PREFS = 4;
	private static final int MENU_ABOUT = 5;
	
	// Dialogs
	public static final int DIALOG_CONTROL = 0;
	
	private static final String TAG = "Speedometer";
	private TrackingServiceManager mServiceManager;
	private long trackId;
	private TextView tvSpeed;
	private TextView tvDistance;
	private PowerManager mPowerManager;
	private TrackDb mTrackDb;
	private cyclelifeApplication app;
	private PowerManager.WakeLock wl;
	private DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
	private Chronometer chTotalTime;
	private TextView tvHeartBeat;
	private TextView tvMaxSpeed;
	private TextView tvAvgSpeed;
	private TextView tvLatitude;
	private TextView tvLongitude;
	private TextView tvAltitude;
	private String btAddress;
	private TrackInfo mCurrentTrack;
	private ProgressDialog dialog;
	private TextView tvCadence;
	private TextView tvAvgCadence;
	private TextView tvMaxAltitude;
	private TextView tvAltitudeUp;
	private TextView tvAltitudeDown;
	private int maxHeartrate;
	private int minHeartrate;
	private LocationManager lm;

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speedometer);
		app = (cyclelifeApplication) getApplication();
		mServiceManager = TrackingServiceManager.getInstance(getApplicationContext());
		mServiceManager.connectService();
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mTrackDb = TrackDb.getInstance(getApplicationContext());
		wl = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		lm.addGpsStatusListener(gpsStatus);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		maxHeartrate = Integer.parseInt(prefs.getString(Preferences.MAX_HEARTRATE, "0"));
		minHeartrate = Integer.parseInt(prefs.getString(Preferences.MIN_HEARTRATE, "0"));

		Bundle data = new Bundle();
		data = getIntent().getExtras();
		trackId = data.getLong("trackId");
		btAddress = data.getString("btAddress");
		tvSpeed = (TextView) findViewById(R.id.speed_register);
		tvHeartBeat = (TextView) findViewById(R.id.heartbeat_register);
		tvDistance = (TextView) findViewById(R.id.total_distance_register);
		chTotalTime = (Chronometer) findViewById(R.id.total_time_register);
		tvMaxSpeed = (TextView) findViewById(R.id.max_speed_register);
		tvAvgSpeed = (TextView) findViewById(R.id.average_speed_register);
		tvLatitude = (TextView) findViewById(R.id.latitude_register);
		tvLongitude = (TextView) findViewById(R.id.longitude_register);
		tvAltitude = (TextView) findViewById(R.id.elevation_register);
		tvCadence = (TextView) findViewById(R.id.cadence_register);
		tvAvgCadence = (TextView) findViewById(R.id.average_cadence_register);
		tvMaxAltitude = (TextView) findViewById(R.id.altitude_max_register);
		tvAltitudeUp = (TextView) findViewById(R.id.altitude_up_register);
		tvAltitudeDown = (TextView) findViewById(R.id.altitude_down_register);
	}
		

	@Override
	protected void onPause(){
		//Disable WakeLock when going into background
		if(wl.isHeld()){
		disableWakeLock();
		}
		mTrackDb.removeObserver(Speedometer.this);
		chTotalTime.stop();
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		
		if(app.getTrackingState()==STATE_TRACKING){
			chTotalTime.start();
		}
		mTrackDb.addObserver(Speedometer.this);
		super.onResume();
	}
	
	@Override
	public void update(TrackInfo trackInfo) {
		mCurrentTrack = trackInfo;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				if(mCurrentTrack.getCurrentLocation()!=null){
				String speed = mDecimalFormat.format((mCurrentTrack
						.getCurrentLocation().getSpeed()*3.6));
				tvSpeed.setText(speed);
				String dist = mDecimalFormat
						.format(mCurrentTrack.getDistance());
				if (mCurrentTrack.getZephyrData() != null) {

					setHeartBeatColor();
					tvCadence.setText(String.valueOf(mCurrentTrack.getZephyrData().getCadence())); 
					tvAvgCadence.setText(String.valueOf(mCurrentTrack.getAvgCadence()));
							
					

				}

				tvDistance.setText(dist);
				String maxSpeed = mDecimalFormat.format(mCurrentTrack
						.getMaxSpeed());
				tvMaxSpeed.setText(maxSpeed);
				String avgSpeed = mDecimalFormat.format(mCurrentTrack
						.getAvgSpeed());
				tvAvgSpeed.setText(avgSpeed);
				tvAltitude.setText("" + mCurrentTrack.getCurrentLocation().getAltitude());
				tvLatitude.setText("" + mCurrentTrack.getCurrentLocation().getLatitude());
				tvLongitude.setText("" + mCurrentTrack.getCurrentLocation().getLongitude());
				tvMaxAltitude.setText(""+mCurrentTrack.getMaxAltitude()); 
				tvAltitudeUp.setText(String.valueOf(mCurrentTrack.getAltitudeUp()));
				tvAltitudeDown.setText("");
				}
			}
		});

	}

	private void disableWakeLock() {
		wl.release();
		Toast.makeText(getApplicationContext(),
				"Wake-Lock has been disabled!", Toast.LENGTH_SHORT)
				.show();
	}

	private void enableWakeLock() {
		wl.acquire();
		Toast.makeText(getApplicationContext(),
				"Wake-Lock has been enabled!", Toast.LENGTH_SHORT)
				.show();
	}
	
	
	@Override
	   public boolean onCreateOptionsMenu( Menu menu )
	   {
	      boolean result = super.onCreateOptionsMenu( menu );

	      menu.add( Menu.NONE, MENU_TRACKING, Menu.NONE, R.string.menu_tracking ).setIcon( android.R.drawable.ic_menu_agenda);
	      menu.add( Menu.NONE, MENU_WAKELOCK, Menu.NONE, "WakeLock" ).setIcon( android.R.drawable.ic_lock_lock );      
	      menu.add( Menu.NONE, MENU_MAP, Menu.NONE, R.string.menu_map ).setIcon( android.R.drawable.ic_menu_mapmode);
	      menu.add( Menu.NONE, MENU_PREFS, Menu.NONE, R.string.menu_settings ).setIcon( android.R.drawable.ic_menu_preferences );
	      menu.add( Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about ).setIcon( android.R.drawable.ic_menu_info_details );  
	      return result;
	   }
	   

	   @Override
	   public boolean onOptionsItemSelected( MenuItem item )
	   {
//		   return MenuFactory.menuItemSelected(item, this);
	      switch (item.getItemId())
	      {
	         case MENU_TRACKING:
	            showDialog( DIALOG_CONTROL );
	            return true;
	         case MENU_WAKELOCK:
	            if(!wl.isHeld()){
	            	enableWakeLock();
	            	item.setTitle("Disable WakeLock");
	            } else {
	            	disableWakeLock();
	            	item.setTitle("Enable WakeLock");
	            }
	            
	            return true;
	         case MENU_PREFS:
	            Intent i = new Intent( this, Preferences.class );
	            startActivity( i );
	          return true;
	         case MENU_MAP:
	            if( this.trackId >= 0 )
	            {
	               Intent intent = new Intent( this, TrackOnMap.class );
	               intent.putExtra("trackId", trackId);
	               startActivity(intent);
	               return true;
	            }
	            else
	            {
	               Toast.makeText(this, "No Track selected to show statistics", Toast.LENGTH_SHORT).show();
	            }
	            return true;
	         case MENU_ABOUT:
	            Intent intent = new Intent( this, AboutActivity.class);
	            startActivity(intent);
	            return true;
		default:
	            return super.onOptionsItemSelected( item );
	      }
	   }

	   /*
	    * (non-Javadoc)
	    * @see android.app.Activity#onCreateDialog(int)
	    */
	   @Override
	   protected Dialog onCreateDialog( int id )
	   {
	      Dialog dialog = null;
	      LayoutInflater factory = null;
	      View view = null;
	      Builder builder = null;
	      switch (id)
	      {
	         
	      case DIALOG_CONTROL:
	            builder = new AlertDialog.Builder( this );
	            factory = LayoutInflater.from( this );
	            view = factory.inflate( R.layout.logcontrol, null );
	            builder  
	               .setTitle( R.string.menu_tracking )
	               .setIcon( android.R.drawable.ic_dialog_alert )
	               .setNegativeButton( android.R.string.cancel, null )
	               .setView( view );
	            dialog = builder.create();
	            return dialog;   
	         default:
	            return super.onCreateDialog( id );
	      }
	   }

	   /*
	    * (non-Javadoc)
	    * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	    */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		int state = app.getTrackingState();
		switch (id) {
		case DIALOG_CONTROL:
			Button start = (Button) dialog.findViewById(R.id.logcontrol_start);
			Button pause = (Button) dialog.findViewById(R.id.logcontrol_pause);
			Button resume = (Button) dialog.findViewById(R.id.logcontrol_resume);
			Button stop = (Button) dialog.findViewById(R.id.logcontrol_stop);
			start.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switchTrackingControl(v);
					dismissDialog(DIALOG_CONTROL);
				}
			});
			pause.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switchTrackingControl(v);
					dismissDialog(DIALOG_CONTROL);
				}
			});
			resume.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switchTrackingControl(v);
					dismissDialog(DIALOG_CONTROL);
				}
			});
			stop.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					switchTrackingControl(v);
					dismissDialog(DIALOG_CONTROL);
				}
			});

			switch (state) {
			case Constants.STATE_STOPPED:
				start.setEnabled(true);
				pause.setEnabled(false);
				resume.setEnabled(false);
				stop.setEnabled(false);
				break;
			case Constants.STATE_TRACKING:
				start.setEnabled(false);
				pause.setEnabled(true);
				resume.setEnabled(false);
				stop.setEnabled(true);
				break;
			case Constants.STATE_PAUSED:
				start.setEnabled(false);
				pause.setEnabled(false);
				resume.setEnabled(true);
				stop.setEnabled(true);
				break;
			default:
				start.setEnabled(true);
				pause.setEnabled(true);
				resume.setEnabled(true);
				stop.setEnabled(true);
				break;
			}
			break;
		default:
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	private void switchTrackingControl(View v) {
		int id = v.getId();
		switch (id)
		{
		   case R.id.logcontrol_start:
		      mServiceManager.start(trackId, btAddress);
		      dialog = new ProgressDialog(Speedometer.this);
		      dialog.setCanceledOnTouchOutside(true);
		    	  dialog.setMessage(getString(R.string.waiting_for_gps));
		      dialog.show();
		      mTrackDb.addObserver(Speedometer.this);
		      chTotalTime.setBase(SystemClock.elapsedRealtime());
		      break;
		   case R.id.logcontrol_pause:
		     mServiceManager.pause();
		     tvSpeed.setText(mDecimalFormat.format(0));
		     chTotalTime.stop();
		     break;
		   case R.id.logcontrol_resume:
		      mServiceManager.resume(trackId);
		      chTotalTime.start();
		      break;
		   case R.id.logcontrol_stop:
		      mServiceManager.stop();
		      mTrackDb.removeObserver(this);
		      chTotalTime.stop();
		      Intent intent = new Intent(Speedometer.this, TrackDetails.class);
		      intent.putExtra("trackId", trackId);
		      startActivity(intent);
		      Speedometer.this.finish();
		      break;
		   default:
		      break;
		}
	}
	
	GpsStatus.Listener gpsStatus = new GpsStatus.Listener() {
		
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:
					dialog.dismiss();
					chTotalTime.start();
				break;

			default:
				break;
			}
	}
};
	

	private void setHeartBeatColor(){
		int pulse = mCurrentTrack.getZephyrData().getCurrentPulse();
		tvHeartBeat.setText(String.valueOf(pulse));
		if(maxHeartrate==0 || pulse < maxHeartrate*0.6){
			tvHeartBeat.setTextColor(Color.BLACK);
		}
		if (pulse>(maxHeartrate*0.6) && pulse<(maxHeartrate*0.75)){
			tvHeartBeat.setTextColor(Color.GREEN);	
		} 
		if(pulse> (maxHeartrate*0.75)){
			tvHeartBeat.setTextColor(Color.RED);
		}
		
	}
}
