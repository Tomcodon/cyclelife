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
package de.teunito.android.cyclelife;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import de.teunito.android.cyclelife.database.TrackDb;
import de.teunito.android.cyclelife.model.Constants;
import de.teunito.android.cyclelife.model.IObserver;
import de.teunito.android.cyclelife.model.TrackInfo;
import de.teunito.android.cyclelife.service.TrackingServiceManager;
import de.teunito.android.cyclelife.view.TrackMapOverlay;

public class TrackOnMap extends MapActivity implements IObserver, Constants {

	private static final int MENU_TRACKING = 0;
	private static final int MENU_MAPSTYLE = 1;
	private static final int MENU_STATS = 2;
	private static final int MENU_SHARE = 3;
	private static final int MENU_PREFS = 4;
	private static final int MENU_ABOUT = 5;
	
	private static final int DIALOG_CONTROL = 0;
	private static final int DIALOG_MAPSTYLE = 1;
	
	private MapView mapView;
	private MapController mapControl;
	private TrackDb trackDb;
	private long trackId;
	private cyclelifeApplication app;
	private CheckBox cbSat;
	private CheckBox cbTraffic;
	private CheckBox cbSpeed;
	private TextView tvSpeed;
	private TextView tvHeartBeat;
	private TrackingServiceManager mServiceManager;
	private DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
	private String btAddress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trackonmap);

		app = (cyclelifeApplication) getApplication();
		mServiceManager = TrackingServiceManager.getInstance(getApplicationContext());

		trackDb = TrackDb.getInstance(getApplicationContext());
		Bundle extras = new Bundle();
		extras = getIntent().getExtras();
		if (extras != null) {
			extras.getLong("trackId");
			trackId = extras.getLong("trackId");
			btAddress = extras.getString("btAddress");
		}

		mapView = (MapView) findViewById(R.id.mapview);
		tvSpeed = (TextView) findViewById(R.id.tvMapCurrentSpeed);
		tvHeartBeat = (TextView) findViewById(R.id.tvMapHeartBeat);

		TrackMapOverlay mapOverlay = new TrackMapOverlay(trackDb, trackId);


		mapControl = mapView.getController();
		mapView.setBuiltInZoomControls(true);

		mapView.setTraffic(true);
		mapView.getOverlays().add(mapOverlay);
		if (app.getTrackingState() == STATE_TRACKING) {
			MyLocationOverlay mLocOverlay = new MyLocationOverlay(getApplicationContext(), mapView);
			mLocOverlay.enableCompass();
			mLocOverlay.enableMyLocation();
			mapView.getOverlays().add(mLocOverlay);
			trackDb.addObserver(TrackOnMap.this);
			tvSpeed.setVisibility(TextView.VISIBLE);
			tvHeartBeat.setVisibility(TextView.VISIBLE);
		}

		mapView.displayZoomControls(true);
		mServiceManager.connectService();

	}
	
	

	@Override
	   public boolean onCreateOptionsMenu( Menu menu )
	   {
	      boolean result = super.onCreateOptionsMenu( menu );

	      if (app.getTrackingState() == STATE_TRACKING) {
	    	  menu.add( Menu.NONE, MENU_TRACKING, Menu.NONE, R.string.menu_tracking ).setIcon( android.R.drawable.ic_menu_agenda);
	      }
	      menu.add( Menu.NONE, MENU_MAPSTYLE, Menu.NONE, R.string.menu_mapstyle ).setIcon( android.R.drawable.ic_menu_mapmode );      
	      menu.add( Menu.NONE, MENU_STATS, Menu.NONE, R.string.menu_statistics ).setIcon( android.R.drawable.ic_menu_gallery);
	      if (app.getTrackingState() == STATE_STOPPED) {
	    	  menu.add( Menu.NONE, MENU_SHARE, Menu.NONE, R.string.menu_shareTrack ).setIcon( android.R.drawable.ic_menu_share );
	      }
	      menu.add( Menu.NONE, MENU_PREFS, Menu.NONE, R.string.menu_settings ).setIcon( android.R.drawable.ic_menu_preferences );
	      menu.add( Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about ).setIcon( android.R.drawable.ic_menu_info_details );  
	      return result;
	   }
	   

	   @Override
	   public boolean onOptionsItemSelected( MenuItem item )
	   {
	      switch (item.getItemId())
	      {
	         case MENU_TRACKING:
	            showDialog( DIALOG_CONTROL );
	            return true;
		case MENU_MAPSTYLE:
	            showDialog( DIALOG_MAPSTYLE );
	            return true;
		case MENU_PREFS:
	            Intent i = new Intent( this, Preferences.class );
	            startActivity( i );
	          return true;
		case MENU_STATS:
	            if( this.trackId >= 0 )
	            {
	               Intent intent = new Intent( this, Statistics.class );
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
		case MENU_SHARE:
	           Intent actionIntent = new Intent( this, RennradNewsShare.class );
	           actionIntent.putExtra("trackId", trackId);
	           startActivity(actionIntent);
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
	      case DIALOG_MAPSTYLE:
	            builder = new AlertDialog.Builder( this );
	            factory = LayoutInflater.from( this );
	            view = factory.inflate( R.layout.mapstyle, null );
	            cbSat = (CheckBox) view.findViewById( R.id.cb_mapstyle_satellite );
	            cbSat.setOnCheckedChangeListener( new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 switchMapStyle(buttonView, isChecked);
						
					}
				});
	            cbTraffic = (CheckBox) view.findViewById( R.id.cb_mapstyle_traffic );
	            cbTraffic.setOnCheckedChangeListener( new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 switchMapStyle(buttonView, isChecked);
						
					}
				});
	            cbSpeed = (CheckBox) view.findViewById( R.id.cb_mapstyle_speed );
	            cbSpeed.setOnCheckedChangeListener( new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						 switchMapStyle(buttonView, isChecked);
						
					}
				});
	            builder
	               .setTitle( R.string.dialog_mapstyle_title )
	               .setIcon( android.R.drawable.ic_dialog_map )
	               .setPositiveButton( android.R.string.ok, null )
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
			Button resume = (Button) dialog
					.findViewById(R.id.logcontrol_resume);
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
			
		case DIALOG_MAPSTYLE:
			cbSat.setChecked(mapView.isSatellite());
			cbTraffic.setChecked(mapView.isTraffic());
			cbSpeed.setChecked(false);
			break;
		default:
			break;
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param buttonView
	 * @param isChecked
	 */
	private void switchMapStyle(CompoundButton buttonView, boolean isChecked) {
		int id = buttonView.getId();
		    switch (id)
		    {
		       case R.id.cb_mapstyle_satellite:
		          mapView.setSatellite(isChecked);
		          break;
		       case R.id.cb_mapstyle_traffic:
		          mapView.setTraffic(isChecked);
		          break;
		       case R.id.cb_mapstyle_speed:
		          // TODO at SpeedLayer and HeartBeatLayer
		          break;
		       default:
		          break;
		    }
	}


	private void switchTrackingControl(View v) {
		int id = v.getId();
		switch (id)
		{
		   case R.id.logcontrol_start:
		      mServiceManager.start(trackId, btAddress);
		      break;
		   case R.id.logcontrol_pause:
		     mServiceManager.pause();
		      break;
		   case R.id.logcontrol_resume:
		      mServiceManager.resume(trackId);
		      break;
		   case R.id.logcontrol_stop:
		      mServiceManager.stop();
		      break;
		   default:
		      break;
		}
	}

	@Override
	public void update(TrackInfo trackInfo) {
		final TrackInfo mCurrentTrack = trackInfo;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(mCurrentTrack.getCurrentLocation()!=null){
					tvSpeed.setText(mDecimalFormat.format(mCurrentTrack.getCurrentLocation().getSpeed()*3.6)+ "km/h");
				}
				if(mCurrentTrack.getZephyrData()!=null){
					tvHeartBeat.setText("" + mCurrentTrack.getZephyrData().getCurrentPulse() + " bpm");	
				}
			}
		});
			
	}

}
