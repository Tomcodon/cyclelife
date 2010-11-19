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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.teunito.android.cyclelife.database.TrackDb;
import de.teunito.android.cyclelife.model.TrackInfo;
import de.teunito.android.cyclelife.view.AltitudeChart;
import de.teunito.android.cyclelife.view.CadenceChart;
import de.teunito.android.cyclelife.view.HeartBeatChart;
import de.teunito.android.cyclelife.view.SpeedChart;
import de.teunito.android.cyclelife.view.ZoneChart;


public class TrackDetails extends Activity {
	

	private static final int MENU_MAP = 1;
	private static final int MENU_SHARE = 3;
	private static final int MENU_SETTINGS = 4;
	private static final int MENU_ABOUT = 5;
	
	private TextView tvDistance;
	private TrackDb mTrackDb;
	private long trackId = 0;
	private HeartBeatChart hbc;
	private SpeedChart speedChart;
	private AltitudeChart altitudeChart;
	private CadenceChart cadenceChart;
	private LinearLayout layoutView;
	private LinearLayout layoutView2;
	private LinearLayout layoutView3;
	private LinearLayout layoutView4;
	private LinearLayout layoutView5;
	private ProgressDialog dialog;
	private TrackInfo trackInfo;
	private Handler mHandler;
	DecimalFormat df = new DecimalFormat("0.00");
	private TextView tvWaypoint;
	private TextView tvAvgSpeed;
	private TextView tvMaxSpeed;
	private TextView tvAltitude;
	private TextView tvAvgHeart;
	private TextView tvMaxHeart;
	private TextView tvAvgCad;
	private TextView tvTitle;
	private ZoneChart zoneChart;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track_details);
				
		if(getIntent().hasExtra("trackId")){
			Bundle data = new Bundle();
			data = getIntent().getExtras();
			trackId = data.getLong("trackId");
		}
		
		mTrackDb= TrackDb.getInstance(getApplicationContext());
		
		tvTitle = (TextView) findViewById(R.id.details_title);
		tvDistance = (TextView) findViewById(R.id.detail_distance);
		tvWaypoint = (TextView) findViewById(R.id.detail_points);
		tvAvgSpeed = (TextView) findViewById(R.id.detail_averagespeed);
		tvMaxSpeed = (TextView) findViewById(R.id.detail_maximumspeed);
		tvAltitude = (TextView) findViewById(R.id.detail_altitideUp);
		tvAvgHeart = (TextView) findViewById(R.id.detail_averageheartbeat);
		tvMaxHeart = (TextView) findViewById(R.id.details_max_heartbeat);
		tvAvgCad = (TextView) findViewById(R.id.detail_average_cadence);
		
		
	    layoutView = (LinearLayout) findViewById(R.id.LinearLayout01);
	    layoutView2 = (LinearLayout) findViewById(R.id.LinearLayout02);
	    layoutView3 = (LinearLayout) findViewById(R.id.LinearLayout03);
	    layoutView4 = (LinearLayout) findViewById(R.id.LinearLayout04);
	    layoutView5 = (LinearLayout) findViewById(R.id.LinearLayout05);
	    
	    DataSetLoader.start();
	    dialog = ProgressDialog.show(TrackDetails.this, getString(R.string.loading_title), getString(R.string.loading_data));
	    dialog.setCancelable(true);
	    
	     mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				updateUi();
			}
		};
	    
	    
	}
	
	
	Thread DataSetLoader = new Thread(new Runnable() {
		

		@Override
		public void run() {
			Looper.prepare();
			hbc = new HeartBeatChart(getApplicationContext(), trackId);
			speedChart = new SpeedChart(getApplicationContext(), trackId);
			altitudeChart = new AltitudeChart(getApplicationContext(), trackId);
			cadenceChart = new CadenceChart(getApplicationContext(), trackId);
			zoneChart = new ZoneChart(getApplicationContext(), trackId);
			trackInfo = mTrackDb.getTrackDetails(trackId);
			mHandler.sendEmptyMessage(0);
			Looper.loop();
		}
	});
	
	

	private void updateUi() {
		if (trackInfo != null) {
			tvTitle.setText("Deatils of Track \"" + trackInfo.getTitle()
					+ "\"");
			tvDistance.setText(df.format(trackInfo.getDistance()) + " km");
			tvMaxSpeed.setText(df.format(trackInfo.getMaxSpeed()) + " km/h");
			tvAvgSpeed.setText(df.format(trackInfo.getAvgSpeed()) + " km/h");
			tvAltitude.setText(String.valueOf(trackInfo.getAltitudeUp()));
			tvWaypoint.setText(String.valueOf(trackInfo.getWaypoints()));
			tvAvgHeart.setText(String.valueOf(trackInfo.getAvgHeartBeat()));
			tvMaxHeart.setText(String.valueOf(trackInfo.getMaxHeartBeat()));
			tvAvgCad.setText(String.valueOf(trackInfo.getAvgCadence()));
		}
		layoutView.addView(hbc.getView());
		layoutView3.addView(speedChart.getView());
		layoutView4.addView(altitudeChart.getView());
		layoutView5.addView(cadenceChart.getView());
		layoutView2.addView(zoneChart.getView());
		dialog.dismiss();
	}
	
	@Override
	   public boolean onCreateOptionsMenu( Menu menu )
	   {
	      boolean result = super.onCreateOptionsMenu( menu );
	      menu.add( ContextMenu.NONE, MENU_MAP, ContextMenu.NONE, R.string.menu_map ).setIcon( android.R.drawable.ic_menu_mapmode );      
	      menu.add( ContextMenu.NONE, MENU_SHARE, ContextMenu.NONE, R.string.menu_shareTrack ).setIcon( android.R.drawable.ic_menu_share );
	      menu.add( ContextMenu.NONE, MENU_SETTINGS, ContextMenu.NONE, R.string.menu_settings ).setIcon( android.R.drawable.ic_menu_preferences );
	      menu.add( ContextMenu.NONE, MENU_ABOUT, ContextMenu.NONE, R.string.menu_about ).setIcon( android.R.drawable.ic_menu_info_details );  
	      return result;
	   }
	   

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MAP:
			Intent it = new Intent(this, TrackOnMap.class);
			it.putExtra("trackId", trackId);
			startActivity(it);
			return true;
		case MENU_SETTINGS:
			Intent i = new Intent(this, Preferences.class);
			startActivity(i);
			return true;
		case MENU_ABOUT:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		case MENU_SHARE:
			Intent share = new Intent(this, RennradNewsShare.class);
			share.putExtra("trackId", trackId);
			startActivity(share);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
}
