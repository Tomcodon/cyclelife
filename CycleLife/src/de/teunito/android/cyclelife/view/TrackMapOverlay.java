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
package de.teunito.android.cyclelife.view;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.teunito.android.cyclelife.database.TrackDb;

public class TrackMapOverlay extends Overlay {

	private TrackDb trackDb;
	private long trackId;
	private Cursor mCursor;
	Paint paint = new Paint();
	
	public TrackMapOverlay (TrackDb db, long trackId){
		this.trackDb = db;
		this.trackId = trackId;
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4);
		paint.setARGB(100, 0, 191, 255);
		paint.setAntiAlias(true);
		
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		Projection projection = mapView.getProjection();

			mCursor = trackDb.getPointsOfTrack(trackId);
			int colLat = mCursor.getColumnIndex("latitude");
			int colLong = mCursor.getColumnIndex("longitude");
			if (mCursor.getCount() > 0) {
				mCursor.moveToFirst();

				while (!mCursor.isLast()) {

					Point startPoint = new Point();
					double startLat = (mCursor.getDouble(colLat) * 1E6);
					double startLong = (mCursor.getDouble(colLong) * 1E6);
					GeoPoint startGeo = new GeoPoint(((int) startLat),
							((int) startLong));
					projection.toPixels(startGeo, startPoint);

					mCursor.moveToNext();
					Point stopPoint = new Point();

					double stopLat = (mCursor.getDouble(colLat) * 1E6);
					double stopLong = (mCursor.getDouble(colLong) * 1E6);
					GeoPoint stopGeo = new GeoPoint(((int) stopLat),
							((int) stopLong));
					projection.toPixels(stopGeo, stopPoint);

					canvas.drawLine(startPoint.x, startPoint.y, stopPoint.x,
							stopPoint.y, paint);

				}
				mCursor.close();
			}
//		}
	}

	public void setTrackId(long trackId) {
		this.trackId = trackId;
	}

	public void setTrackDb(TrackDb trackDb) {
		this.trackDb = trackDb;
	}

	public TrackDb getTrackDb() {
		return trackDb;
	}

}
