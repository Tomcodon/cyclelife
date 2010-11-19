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
public class AltitudeChart extends de.teunito.android.cyclelife.view.AbstractChart{

	private GraphicalView mChartView;
	private long trackId;
	private Context mContext;
	private TrackDb mTrackDb;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer;
	
	public AltitudeChart (Context ctx, long trackId){
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
		ArrayList data = mTrackDb.getAltitudeForChart(trackId);
		String[] titles = new String[] { "Altitude of track "
				+ (String) data.get(0) + " (" + trackId + ")" };
		
		if (data.size()>1) {
			values.add((double[]) data.get(1));
			date.add((Date[]) data.get(2));
		} 
		
		mDataset = buildDateDataset(titles, date, values);

		int[] colors = new int[] { Color.YELLOW };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
		mRenderer = buildRenderer(colors, styles);
		int length = mRenderer.getSeriesRendererCount();
//		for (int i = 0; i < length; i++) {
			XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) mRenderer.getSeriesRendererAt(0);
					seriesRenderer.setFillPoints(true);
					seriesRenderer.setFillBelowLine(true);
					seriesRenderer.setFillBelowLineColor(Color.YELLOW);
//		}
		setChartSettings(mRenderer, "Altitude overview", "time", "meter", 0, 0,
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
