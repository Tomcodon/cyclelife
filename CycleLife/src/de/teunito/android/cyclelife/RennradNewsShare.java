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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.teunito.android.cyclelife.database.TrackDb;
import de.teunito.android.cyclelife.model.TrackInfo;

public class RennradNewsShare extends Activity {
   
	private static final String REQUEST_URL = "http://trainingsverwaltung.rennrad-news.de/api/units/add.xml";
    private static final String DOMAIN = "trainingsverwaltung.rennrad-news.de";
	
	private TextView tv;
	private Button bt;
	private BufferedReader antwort;
	private StringBuffer sb;
	private TrackDb mTrackDb;
	private Spinner spSports;
	private Spinner spZone;
	private Spinner spWeather;
	private Spinner spMood;
	private ArrayAdapter<CharSequence> sportsAdapter;
	private ArrayAdapter<CharSequence> zoneAdapter;
	private ArrayAdapter<CharSequence> weatherAdapter;
	private ArrayAdapter<CharSequence> moodAdapter;
	private String APIKey = "";
	private String bikeId ="";
	private String sportsID;
	private String moodId;
	private String zoneId;
	private String weatherId;
	private EditText etTemp;
	private String temperature="";
	private String xmlContentToSend;
	private HttpHost targetHost;
	private HttpPost httpPost;
	private DefaultHttpClient httpClient;
	private ProgressDialog dialog;
	private Handler handler;
	private long trackId;
	private String weight;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_rennradnews);
        
		Bundle data = new Bundle();
		data = getIntent().getExtras();
		trackId = data.getLong("trackId");
        
        mTrackDb = TrackDb.getInstance(getApplicationContext());
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        APIKey = prefs.getString(Preferences.RENNRADNEWS_API_KEY, "");
        bikeId  = prefs.getString(Preferences.RENNRADNEWS_BIKE_ID, "");
        weight = prefs.getString(Preferences.WEIGHT, "");
        
        tv = (TextView)findViewById(R.id.rnsShareTitle);
        tv.setText("Share your track "+ trackId + " to rennrad-news.de community!");
        bt = (Button)findViewById(R.id.rnsShareBtn);
        etTemp = (EditText)findViewById(R.id.rnsShareTemp);
        spSports = (Spinner)findViewById(R.id.rnsShareSports);
        spZone = (Spinner)findViewById(R.id.rnsShareZone);
        spWeather = (Spinner)findViewById(R.id.rnsShareWeather);
        spMood = (Spinner)findViewById(R.id.rnsShareMood);
        
        sportsAdapter = ArrayAdapter.createFromResource(this, R.array.sports,
                android.R.layout.simple_spinner_item);
        zoneAdapter = ArrayAdapter.createFromResource(this, R.array.zone,
                android.R.layout.simple_spinner_item);
        weatherAdapter = ArrayAdapter.createFromResource(this, R.array.weather,
                android.R.layout.simple_spinner_item);
        moodAdapter = ArrayAdapter.createFromResource(this, R.array.mood,
                android.R.layout.simple_spinner_item);
        
        sportsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spSports.setAdapter(sportsAdapter);
        spZone.setAdapter(zoneAdapter);
        spWeather.setAdapter(weatherAdapter);
        spMood.setAdapter(moodAdapter);
        
  
        bt.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View v) {
			dialog = ProgressDialog.show(RennradNewsShare.this, "", 
                        "Uploading. Please wait...", true);
				
				sportsID = String.valueOf(spSports.getSelectedItemPosition()+1);
				zoneId = String.valueOf(spZone.getSelectedItemPosition()+1);
				weatherId = String.valueOf(spWeather.getSelectedItemPosition()+1);
				moodId = String.valueOf(spMood.getSelectedItemPosition()+1);
				temperature = etTemp.getText().toString();
			    
	         // execute is a blocking call, it's best to call this code in a thread separate from the ui's
	            uploadThread.start();
			}
		});
        
        handler = new Handler(){
        	public void handleMessage(Message msg) {
        		String result = msg.getData().getString("result");
        		if (result.contains("success")){
        			Toast.makeText(getApplicationContext(), "Uploaded: " + result, Toast.LENGTH_LONG).show();
        			finish();
        		} else
        			Toast.makeText(getApplicationContext(), "Error: " + result, Toast.LENGTH_LONG).show();
        	}
        };
        
        if (APIKey.length()<20) {
			AlertDialog.Builder builder = new AlertDialog.Builder(RennradNewsShare.this);
			builder.setMessage("Please enter first the rennrad-news.de API-key in the Settings!").setCancelable(false).
			setIcon(android.R.drawable.ic_dialog_alert)
		       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {            
		        	   RennradNewsShare.this.finish();
		        	   startActivity(new Intent(RennradNewsShare.this, Preferences.class));
		           }
		       });
			builder.create().show();	
		}
    }
    
    
    Thread uploadThread = new Thread(new Runnable() {

		@Override
		public void run() {
			TrackInfo trackInfo = mTrackDb.getTrackDetails(trackId);
			
			xmlContentToSend = generateXML(trackInfo);
			 
			httpClient = new DefaultHttpClient();
                
	        targetHost = new HttpHost(DOMAIN, 80, "http");
	        // Using POST here
	        httpPost = new HttpPost(REQUEST_URL);
	        // Make sure the server knows what kind of a response we will accept
	        httpPost.addHeader("Accept", "text/xml");
	        // Also be sure to tell the server what kind of content we are sending
	        httpPost.addHeader("Content-Type", "application/xml");
    
			try {
		        StringEntity entity = new StringEntity(xmlContentToSend, "UTF-8");
		        entity.setContentType("application/xml");
	            httpPost.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				HttpResponse response = httpClient.execute(
						targetHost, httpPost);
				antwort = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				sb = new StringBuffer("");
				while ((line = antwort.readLine()) != null) {
					sb.append(line);
				}
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("result", sb.toString());
				msg.setData(b);
				handler.sendMessage(msg);
	                
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dialog.dismiss();
		}
	});
    
    
    private String generateXML(TrackInfo trackInfo){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "request");
			
			serializer.startTag("", "key");
			serializer.text(APIKey);
			serializer.endTag("", "key");
			
			serializer.startTag("", "unit");
			
			serializer.startTag("", "title");
			serializer.text(trackInfo.getTitle());
			serializer.endTag("", "title");
			
			serializer.startTag("", "unitdate");
			serializer.text(trackInfo.getDate());
			serializer.endTag("", "unitdate");
			
			serializer.startTag("", "category_id");
			serializer.text(sportsID); // 1=Radfahren, 2=Laufen, 3=Kraftsport 4=Schwimmen, 5=Sonstige, 6=Skilanglauf
			serializer.endTag("", "category_id");
			
			serializer.startTag("", "bike_id");
			if(bikeId!=""){
				serializer.text(bikeId);
			}
			serializer.endTag("", "bike_id");
			
			serializer.startTag("", "unittype_id");
			serializer.text(zoneId); // Trainingsbereich: 1=Regeneration, ...
			serializer.endTag("", "unittype_id");
			
			serializer.startTag("", "condition_id");
			serializer.text(weatherId); // Wetter: 1=sonnig, 2=heiter ...
			serializer.endTag("", "condition_id");
			
			serializer.startTag("", "mood_id");
			serializer.text(moodId); // Stimmung: 1=sehr gut, 2=gut, ... 
			serializer.endTag("", "mood_id");
			
			serializer.startTag("", "temperature");
			serializer.text(temperature);
			serializer.endTag("", "temperature");
			
			serializer.startTag("", "length");
			serializer.text("180"); // Dauer der Einheit in Minuten
			serializer.endTag("", "length");
			
			serializer.startTag("", "distance");
			serializer.text(String.valueOf(trackInfo.getDistance()));
			serializer.endTag("" ,"distance");
			
			serializer.startTag("", "climbing");
			serializer.text(String.valueOf(trackInfo.getAltitudeUp())); // Hoehenmeter der Einheit in Meter
			serializer.endTag("", "climbing");
			  
			serializer.startTag("", "heartrate_avg");
			serializer.text(String.valueOf(trackInfo.getAvgHeartBeat()));
			serializer.endTag("", "heartrate_avg");
			
			serializer.startTag("", "heartrate_max");
			serializer.text(String.valueOf(trackInfo.getMaxHeartBeat()));
			serializer.endTag("", "heartrate_max");
			
			serializer.startTag("", "cadence");
			serializer.text(String.valueOf(trackInfo.getAvgCadence()));
			serializer.endTag("","cadence");
			
			serializer.startTag("", "weight");
			if(weight==""){
				serializer.text("0");
			} else serializer.text(weight);
			serializer.endTag("", "weight");
			
			serializer.startTag("", "maxspeed");
			serializer.text(String.valueOf(trackInfo.getMaxSpeed()));
			serializer.endTag("", "maxspeed");
			
			serializer.startTag("", "description");
			serializer.text(trackInfo.getDescription());
			serializer.endTag("", "description");
			
			serializer.startTag("", "public");
			serializer.text("0"); // 0 = nicht šffentlich, 1 = šffentlich
			serializer.endTag("","public");
			
			serializer.startTag("", "publish_wp");
			serializer.text("0"); // im Winterpokal veršffentlichen? 1= true, 0= false
			serializer.endTag("", "publish_wp");
			  
			serializer.endTag("", "unit");
			serializer.endTag("", "request");
			
			serializer.endDocument();
			String xml = writer.toString();
			return xml;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

}
