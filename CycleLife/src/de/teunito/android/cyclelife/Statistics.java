/**
 * 
 */
package de.teunito.android.cyclelife;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import de.teunito.android.cyclelife.view.HeartBeatChart;

/**
 * @author teunito
 *
 */
public class Statistics extends Activity {
	
	  private long trackId = 0;

	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.statistics);
	    
	    Bundle data = new Bundle();
		if(getIntent().hasExtra("trackId")){
			data = getIntent().getExtras();
			trackId = data.getLong("trackId");
		}
	    
	    LinearLayout layout = (LinearLayout) findViewById(R.id.heartBeatChart1);
	    HeartBeatChart hbc = new HeartBeatChart(getApplicationContext(), trackId);
	    GraphicalView chartView = hbc.getView();
	    if(chartView!=null){
	    	layout.addView(chartView, new LayoutParams(LayoutParams.FILL_PARENT,
	    	          LayoutParams.FILL_PARENT));
	    }
	   
	  }
}
