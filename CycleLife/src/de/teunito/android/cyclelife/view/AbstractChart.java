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

import java.util.Date;
import java.util.List;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public abstract class AbstractChart {
	
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
	      int seriesLength = yV.length;
	      for (int k = 0; k < seriesLength-1; k++) {
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
	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		if (xMin != 0) {
			renderer.setXAxisMin(xMin);
		}
		if (xMax != 0) {
			renderer.setXAxisMax(xMax);
		}
		if(yMin!=0){
		renderer.setYAxisMin(yMin);
		}
		if(yMax!=0){
		renderer.setYAxisMax(yMax);
		}
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}
	

	  
	  /**
	   * Builds a category renderer to use the provided colors.
	   * 
	   * @param colors the colors
	   * @return the category renderer
	   */
	  protected DefaultRenderer buildCategoryRenderer(int[] colors) {
	    DefaultRenderer renderer = new DefaultRenderer();
	    for (int color : colors) {
	      SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	      r.setColor(color);
	      renderer.addSeriesRenderer(r);
	    }
	    return renderer;
	  }
	
	/**
	 * Generates the Dataset for the Chart
	 */
	protected abstract void generateData();
	
	/**
	 * Generates the View to be added to the layout
	 * @return GraphicalView
	 */
	protected abstract GraphicalView buildView();

}
