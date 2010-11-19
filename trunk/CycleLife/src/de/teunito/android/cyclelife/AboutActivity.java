/**
 * 
 */
package de.teunito.android.cyclelife;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * @author teunito
 * 
 */
public class AboutActivity extends Activity {

	private static final String TAG = "SportStats:AboutActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		

		TextView appNameField = (TextView) findViewById(R.id.field_app_name);
		appNameField.setText(R.string.app_name);

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			String version = packageInfo.versionName;
			TextView idField = (TextView) findViewById(R.id.field_version);
			idField.setText(version);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
		}
	}
}
