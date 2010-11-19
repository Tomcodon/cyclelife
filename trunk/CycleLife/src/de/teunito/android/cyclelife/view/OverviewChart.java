/**
 * 
 */
package de.teunito.android.cyclelife.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import de.teunito.android.cyclelife.database.TrackDb;

/**
 * @author teunito
 *
 */
public class OverviewChart{

	private GraphicalView mChartView;
	private long trackId;
	private Context mContext;
	private TrackDb mTrackDb;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer;
	
	public OverviewChart (Context ctx, long trackId){
		this.mContext = ctx;
		this.trackId  = trackId;
		mTrackDb = TrackDb.getInstance(mContext);
		generateData();
		buildView();
	}
	
	public GraphicalView getView(){
		return mChartView;
	}

	private void generateData() {
		String name = "Overview of track " + "(" + trackId + ")";
//		mTrackDb.getHeartBeatForChart(trackId);
		String[] titles = new String[] { "Crete", "Corfu", "Thassos",
		"Skiathos" };
		List<double[]> x = new ArrayList<double[]>();
		long value = new Date().getTime() - 3 * TimeChart.DAY;
		Random r = new Random();
		List<Date[]> date = new ArrayList<Date[]>();
		Date[] datevalue = new Date[12];
		for (int i = 0; i < titles.length; i++) {
			for (int k = 0; k < 12; k++) {
				 datevalue[k] = new Date(value + k * TimeChart.DAY / 4);
			}
			date.add(datevalue);
		}
		List<double[]> values = new ArrayList<double[]>();
		values.add(new double[] { 12.3, 12.5, 13.8, 16.8, 20.4, 24.4, 26.4,
				26.1, 23.6, 20.3, 17.2, 13.9 });
		values.add(new double[] { 10, 10, 12, 15, 20, 24, 26, 26, 23, 18, 14,
				11 });
		values.add(new double[] { 5, 5.3, 8, 12, 17, 22, 24.2, 24, 19, 15, 9, 6 });
		values.add(new double[] { 9, 10, 11, 15, 19, 23, 26, 25, 22, 18, 13, 10 });
		int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.CYAN,
				Color.YELLOW };
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE,
				PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };
		mRenderer = buildRenderer(colors, styles);
		int length = mRenderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) mRenderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}
		this.setChartSettings(mRenderer, name , "Date",
				"Temperature", 0, 0, 0, 32, Color.LTGRAY, Color.GRAY);
		mRenderer.setXLabels(12);
		mRenderer.setYLabels(10);
		mRenderer.setShowGrid(true);
		mDataset = buildDateDataset(titles, date, values);
		
	}
	
	  /**
	   * Builds an XY multiple time dataset using the provided values.
	   * 
	   * @param titles the series titles
	   * @param xValues the values for the X axis
	   * @param yValues the values for the Y axis
	   * @return the XY multiple time dataset
	   */
	
	  protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
	      List<double[]> yValues) {
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	    int length = titles.length;
	    for (int i = 0; i < length; i++) {
	      XYSeries series = new XYSeries(titles[i]);
	      Date[] xV = xValues.get(i);
	      double[] yV = yValues.get(i);
	      int seriesLength = xV.length;
	      for (int k = 0; k < seriesLength; k++) {
	        series.add(xV[k].getTime(), yV[k]);
	      }
	      dataset.addSeries(series);
	    }
	    return dataset;
	  }
	  
	  /**
	   * Builds an XY multiple series renderer.
	   * 
	   * @param colors the series rendering colors
	   * @param styles the series point styles
	   * @return the XY multiple series renderers
	   */
	  protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    int length = colors.length;
	    for (int i = 0; i < length; i++) {
	      XYSeriesRenderer r = new XYSeriesRenderer();
	      r.setColor(colors[i]);
	      r.setPointStyle(styles[i]);
	      renderer.addSeriesRenderer(r);
	    }
	    return renderer;
	  }

	  /**
	   * Sets a few of the series renderer settings.
	   * 
	   * @param renderer the renderer to set the properties to
	   * @param title the chart title
	   * @param xTitle the title for the X axis
	   * @param yTitle the title for the Y axis
	   * @param xMin the minimum value on the X axis
	   * @param xMax the maximum value on the X axis
	   * @param yMin the minimum value on the Y axis
	   * @param yMax the maximum value on the Y axis
	   * @param axesColor the axes color
	   * @param labelsColor the labels color
	   */
	  protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
	      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
	      int labelsColor) {
	    renderer.setChartTitle(title);
	    renderer.setXTitle(xTitle);
	    renderer.setYTitle(yTitle);
	    if(xMin!=0){
	    renderer.setXAxisMin(xMin);
	    }
	    if(xMax!=0){
	    renderer.setXAxisMax(xMax);
	    }
	    renderer.setYAxisMin(yMin);
	    renderer.setYAxisMax(yMax);
	    renderer.setAxesColor(axesColor);
	    renderer.setLabelsColor(labelsColor);
	  }
	
	
	
	private GraphicalView buildView(){
		mChartView = ChartFactory.getTimeChartView(mContext, mDataset, mRenderer, null);
		return mChartView;
	}

	
	

	
}
