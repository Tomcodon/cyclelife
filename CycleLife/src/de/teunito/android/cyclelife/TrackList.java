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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import de.teunito.android.cyclelife.database.TrackDb;
import de.teunito.android.cyclelife.service.TrackingService;

public class TrackList extends Activity implements OnItemClickListener {

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	public static final int REQUEST_ADD_TRACK = 3;

	// Options Menu
	private static final int MENU_ADD = 0;
	private static final int MENU_SCAN_BT = 1;
	private static final int MENU_PREF = 2;
	private static final int MENU_ABOUT = 3;
	
	
	//Context menu
	private static final int CT_MENU_DETELE = 2;
	private static final int CT_MENU_SHARE = 3;
	private static final int CT_MENU_RENAME = 4;
	private static final int CT_MENU_STATS = 5;

	// Dialog
	private static final int DIALOG_RENAME = 10;
	private static final int DIALOG_DELETE = 11;

	private ImageButton ibAdd;
	private long trackId;
	private TrackDb mTrackDb;
	private ListView lvTracks;
	private ActivityManager am;
	private EditText etRenameTrack;
	private CharSequence mCurrentName;
	private SimpleCursorAdapter listAdapter;
	private long mCurrentTrackId;
	private Cursor mcAllTracks;
	private String btAddress = "";
	private boolean connectShown = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTrackDb = TrackDb.getInstance(getApplicationContext());
		am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

		ibAdd = (ImageButton) findViewById(R.id.ibAdd);
		lvTracks = (ListView) findViewById(R.id.lvTracks);

		ibAdd.setImageResource(android.R.drawable.ic_input_add);
		ibAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TrackList.this, AddTrack.class);
				startActivityForResult(intent, REQUEST_ADD_TRACK);
			}
		});

		setTracks();

		// register the contextmenu for the listview
		registerForContextMenu(lvTracks);
		
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
		
		if(prefs.getBoolean(Preferences.CONNECT_BLUETOOTH_STARTUP, false)&& !connectShown){
			Intent intent = new Intent(this, BluetoothDeviceListActivity.class);
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
            connectShown=true;
		}
	}

	@Override
	protected void onDestroy() {
		Intent intent = new Intent(getApplicationContext(),
				TrackingService.class);
		stopService(intent);
		super.onDestroy();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}

	private void setTracks() {
		mcAllTracks = mTrackDb.getAllTracks();
		startManagingCursor(mcAllTracks);
		listAdapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.tracklistitem, mcAllTracks, new String[] { "title",
						"date", "description", "distance" }, new int[] {
						R.id.tvTrackListName, R.id.tvTrackListDate,
						R.id.tvTrackListDescription, R.id.tvTrackListDistance });
		lvTracks.setAdapter(listAdapter);
		lvTracks.invalidate();
		lvTracks.setOnItemClickListener(this);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				btAddress = data.getExtras().getString(
						BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// TODO Bluetooth is now enabled, so set up connection
				Intent intent = new Intent(this,
						BluetoothDeviceListActivity.class);
				startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			} else {
				Toast.makeText(this, "Bluetooth could not be enabled",
						Toast.LENGTH_SHORT).show();
			}
		case REQUEST_ADD_TRACK:
			if (resultCode == Activity.RESULT_OK) {
				trackId = data.getExtras().getLong("trackId");
				setTracks();
				Intent intent = new Intent(TrackList.this,
						TrackingService.class);
				intent.putExtra("trackId", trackId);
				startService(intent);
				Intent it = new Intent(this, Speedometer.class);
				it.putExtra("trackId", trackId);
				it.putExtra("btAddress", btAddress);
				startActivity(it);
				TrackList.this.finish();
			}
		}

	}

	@Override
	public void onItemClick(AdapterView parent, View v, int position, long id) {

		// TODO switch between TrackDetails an TrackOnMap if running or not
		// Intent intent = new Intent(SportStats.this, TrackOnMap.class);
		Intent intent = new Intent(TrackList.this, TrackDetails.class);
		intent.putExtra("trackId", id);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_ADD, Menu.NONE, R.string.add_track).setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(ContextMenu.NONE, MENU_SCAN_BT, 0, R.string.menu_scan_bt)
				.setIcon(android.R.drawable.ic_menu_search);
		menu.add(ContextMenu.NONE, MENU_PREF, 1, R.string.menu_settings)
				.setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(ContextMenu.NONE, MENU_ABOUT, 2, R.string.menu_about).setIcon(
				android.R.drawable.ic_menu_info_details);
//		MenuFactory.creatOptionsMenu(menu, MenuFactory.CYCLEMETER_MENU);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		return MenuFactory.menuItemSelected(item, this);
		switch (item.getItemId()) {
		case MENU_ADD:
			Intent it = new Intent(TrackList.this, AddTrack.class);
			startActivityForResult(it, REQUEST_ADD_TRACK);
			return true;
		case MENU_PREF:
			startActivity(new Intent(this, Preferences.class));
			return true;
		case MENU_ABOUT:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case MENU_SCAN_BT:
			Intent intent = new Intent(this, BluetoothDeviceListActivity.class);
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
		default:
			break;
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.View.OnCreateContextMenuListener#onCreateContextMenu(ContextMenu
	 * , View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(0, CT_MENU_STATS, 0, R.string.menu_statistics);
		menu.add(0, CT_MENU_SHARE, 0, R.string.menu_shareTrack);
		menu.add(0, CT_MENU_RENAME, 0, R.string.menu_renameTrack);
		menu.add(0, CT_MENU_DETELE, 0, R.string.menu_deleteTrack);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean handled = false;
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return handled;
		}

		mCurrentName = mTrackDb.getTrackTitle(info.id);
		mCurrentTrackId = info.id;

		switch (item.getItemId()) {
		case CT_MENU_DETELE: {
			showDialog(DIALOG_DELETE);
			handled = true;
			break;
		}
		case CT_MENU_SHARE: {
			Intent intent = new Intent(this, RennradNewsShare.class);
			intent.putExtra("trackId", mCurrentTrackId);
			startActivity(intent);
			handled = true;
			break;
			// TODO export as KML and/or Upload to portal
		}
		case CT_MENU_RENAME: {
			showDialog(DIALOG_RENAME);
			handled = true;
			break;
		}
		case CT_MENU_STATS: {
			Intent actionIntent = new Intent(this, TrackDetails.class);
			actionIntent.putExtra("trackId", mCurrentTrackId);
			startActivity(actionIntent);
			handled = true;
			break;
		}
		default:
			handled = super.onContextItemSelected(item);
			break;
		}
		return handled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		Builder builder = null;
		switch (id) {
		case DIALOG_RENAME:
			LayoutInflater li = LayoutInflater.from(this);
			View view = li.inflate(R.layout.renamedialog, null);
			etRenameTrack = (EditText) view.findViewById(R.id.nameField);

			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_routename_title)
					.setMessage(R.string.dialog_routename_message)
					.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String title = etRenameTrack.getText().toString();
							mTrackDb.renameTrack(title, mCurrentTrackId);
							mcAllTracks.requery();
						}
					});
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.setView(view);
			dialog = builder.create();
			return dialog;
		case DIALOG_DELETE:
			String messageFormat = this.getResources().getString(
					R.string.dialog_delete_message);
			String message = String.format(messageFormat, mCurrentName);
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_delete_title)
					.setMessage(message)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mTrackDb.deleteTrack(mCurrentTrackId);
									mcAllTracks.requery();
								}
							});
			dialog = builder.create();
			return dialog;
		default:
			return super.onCreateDialog(id);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_RENAME:
			etRenameTrack.setText(mCurrentName);
			etRenameTrack.setSelection(0, mCurrentName.length());
			break;
		case DIALOG_DELETE:
			AlertDialog alert = (AlertDialog) dialog;
			String messageFormat = this.getResources().getString(
					R.string.dialog_delete_message);
			String message = String.format(messageFormat, mCurrentName);
			alert.setMessage(message);
			break;
		}
	}
}
