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

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import de.teunito.android.cyclelife.database.TrackDb;
import de.teunito.android.cyclelife.model.TrackInfo;

/**
 * @author teunito
 * 
 */
public class AddTrack extends Activity {

	private TrackInfo trackInfo;
	private EditText etName;
	private EditText etDescription;
	private TrackDb mTrackDB;
	private TextView tvDate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_track);

		mTrackDB = TrackDb.getInstance(getApplicationContext());
		trackInfo = new TrackInfo();

		etName = (EditText) findViewById(R.id.etAddTrackName);
		etDescription = (EditText) findViewById(R.id.etAddTrackDesc);
		tvDate = (TextView) findViewById(R.id.tvAddTrackDate);
		tvDate.setText("" + new Date().toGMTString());
	}

	// Method to handle click on OK-Button
	public void onClickOk(View v) {
		trackInfo.setTitle(etName.getText().toString());
		trackInfo.setDescription(etDescription.getText().toString());
		trackInfo.setStarttime((System.currentTimeMillis()/1000));

		long id = mTrackDB.insertTrack(trackInfo);
		Intent intent = new Intent();
		intent.putExtra("trackId", id);
		AddTrack.this.setResult(Activity.RESULT_OK, intent);
		AddTrack.this.finish();
	}

	// Method to handle click on Cancel-Button
	public void onClickCancel(View v) {
		AddTrack.this.setResult(Activity.RESULT_CANCELED);
		AddTrack.this.finish();
	}
}
