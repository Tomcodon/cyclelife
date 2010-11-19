/**
 * 
 */
package de.teunito.android.cyclelife.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import de.teunito.android.cyclelife.database.TrackDb;

/**
 * @author teunito
 *
 */
public class HeartBeatChart extends de.teunito.android.cyclelife.view.AbstractChart{

	private GraphicalView mChartView;
	private long trackId;
	private Context mContext;
	private TrackDb mTrackDb;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer;
	
	public HeartBeatChart (Context ctx, long trackId){
		this.mContext = ctx;
		this.trackId  = trackId;
		mTrackDb = TrackDb.getInstance(mContext);
		generateData();
		buildView();
	}
	
	public GraphicalView getView(){
		return mChartView;
	}

	protected void generateData() {

		List<double[]> values = new ArrayList<double[]>();
		List<Date[]> date = new ArrayList<Date[]>();
		ArrayList data = mTrackDb.getHeartBeatForChart(trackId);
		String[] titles = new String[] { "HeartBeat of track "
				+ (String) data.get(0) + " (" + trackId + ")" };
		
		if (data.size()>1) {
			values.add((double[]) data.get(1));
			date.add((Date[]) data.get(2));
		} 
		
		mDataset = buildDateDataset(titles, date, values);

		int[] colors = new int[] { Color.GREEN };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
		mRenderer = buildRenderer(colors, styles);
		int length = mRenderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) mRenderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}
		setChartSettings(mRenderer, "Heartbeat overview", "time", "bpm", 0, 0,
				0, 0, Color.LTGRAY, Color.GRAY);
		mRenderer.setXLabels(12);
		mRenderer.setYLabels(10);
		mRenderer.setShowGrid(true);
	}
	
	
	protected GraphicalView buildView(){
		mChartView = ChartFactory.getTimeChartView(mContext, mDataset, mRenderer, null);
		return mChartView;
	}
}
