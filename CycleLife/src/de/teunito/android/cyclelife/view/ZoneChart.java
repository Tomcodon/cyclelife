/**
 * Copyright 2010 Tobias Teunissen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package de.teunito.android.cyclelife.view;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import de.teunito.android.cyclelife.Preferences;
import de.teunito.android.cyclelife.database.TrackDb;

/**
 * @author teunito
 *
 */
public class ZoneChart extends AbstractChart{

	private GraphicalView mChartView;
	private long trackId;
	private Context mContext;
	private TrackDb mTrackDb;
	private CategorySeries mData;
	private DefaultRenderer mRenderer;
	private int maxheartrate;
	
	public ZoneChart (Context ctx, long trackId){
		this.mContext = ctx;
		this.trackId  = trackId;
		mTrackDb = TrackDb.getInstance(mContext);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		maxheartrate = Integer.parseInt(prefs.getString(Preferences.MAX_HEARTRATE, "185"));
		generateData();
		buildView();
	}
	public GraphicalView getView(){
		return mChartView;
	}
	
	protected GraphicalView buildView(){
 	    mChartView =  ChartFactory.getPieChartView(mContext, mData, mRenderer);
		return mChartView;
	}
	/* (non-Javadoc)
	 * @see de.teunito.android.cyclemeter.view.AbstractChart#generateData()
	 */
	@Override
	protected void generateData() {
		int[] colors = new int[] {Color.YELLOW, Color.GREEN, Color.RED};
		mRenderer = buildCategoryRenderer(colors);
		mRenderer.setLabelsTextSize(10);
		Integer[] values = mTrackDb.getTrackZones(trackId);
		mData = buildZonesDataset(values, maxheartrate);		
	}
	
	  /**
	   * Builds a category series using the provided values.
	   * 
	   * @param values the values
	   * @return the category series
	   */
	  protected CategorySeries buildZonesDataset(Integer[] values, int maxheartrate) {
	    CategorySeries series = new CategorySeries("trainings zones");
	      series.add("0-"+maxheartrate*0.6, values[0]);
	      series.add(maxheartrate*0.6+"-"+maxheartrate*0.85, values[1]);
	      series.add(maxheartrate*0.85+"-"+maxheartrate,values[2]);
	      return series;
	  }
}
