/*
 * Copyright (c) 2010, University of Victoria
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    
 *    This product includes software developed by the University of Victoria.
 * 
 * 4. Neither the name of the University of Victoria nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE University of Victoria ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE University of Victoria BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.neptune.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.manager.DbManager;
import com.neptune.model.TrackingCategory;
import com.neptune.view.DeviceListAdapter;
import com.neptune.view.SectionedListAdapter;

/**
 *  This Activity is the viewable list of devices. Information to populate this list
 *  is taken from the MySQLite Database. Devices can be clicked/touched to launch
 *  a new activity and reveal more information about a device (sensors, readings etc).
 *  
 *  This activity uses a combination of adapters: SectionedListAdapter divides all the
 *  data into sections (location), and each section is managed by a DeviceListAdapter.
 */
public class DevicesTabActivity extends ListActivity {
	private final static String PAGE = "/Launch/DevicesTab";
	private GoogleAnalyticsTracker tracker;
	
	private DbManager db;
	private Cursor[] allCursors;
	private Cursor[] favCursors;
	private Cursor locations;
	
	// Define list adapter which contains sections for each location.
	private SectionedListAdapter adapter;

	/**
	 * Create the view of device list by querying all the devices of all locations and 
	 * all the favourite devices of all locations.
	 * 
	 * The default device list is all the devices of all locations.
	 * 
	 * Favourite button will toggle the display between all the devices and all the favourite devices.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.devices_list);

		//Setup for Google Analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		
		db = new DbManager(this);
		db.open();
		adapter = new SectionedListAdapter(this);
		
		//First get the list of all locations
		locations = db.fetchHeaderLocations();
		startManagingCursor(locations);
		allCursors = new Cursor[locations.getCount()];
		favCursors = new Cursor[locations.getCount()];

		//Then get a cursor object for each of the locations,
		//where each cursor consists of the devices at that location
		String location_name;
		int index = locations.getColumnIndex(DbManager.LOCATION);
		for(int i=0; i<locations.getCount(); i++){
			locations.moveToPosition(i);
			location_name = locations.getString(index);
			allCursors[i] = db.fetchDevicesAtLocation(location_name);
			startManagingCursor(allCursors[i]);
			favCursors[i] = db.fetchFavDevicesAtLocation(location_name);
			startManagingCursor(favCursors[i]);
			adapter.addSection(location_name, new DeviceListAdapter(this, allCursors[i])); //Default to list all devices
		}
		setListAdapter(adapter);

		//Programatically set up the onClickListener for the favourites/all toggle button
		final ToggleButton toggle = (ToggleButton) findViewById(R.id.bottom_control_toggle);
		toggle.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				tracker.trackEvent(TrackingCategory.CATEGORY_BUTTON, "DeviceList_filter", "", 0);
				if(toggle.isChecked()){
					adapter.updateCursors(favCursors);
				}else{
					adapter.updateCursors(allCursors);
				}
				adapter.notifyDataSetChanged();
			}
		});

		
	}
	
	/**
	 * Track the page when the activity is being started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		tracker.trackPageView(PAGE);
	}
	
	
	/**
	 * When DevicesTabActivity is resumed from pause, it will call requery() 
	 * for all the cursors that are being managed and refresh the display.
	 */
	@Override
	public void onResume(){
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * Close database connection when the activity is being destroyed.
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		db.close();
	}
	
	/**
	 * When an item of the device list is clicked, if the device is of default device type (scalar),
	 * start SensorActivity to display sensor data, else start VideoActivity to play video streaming.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		//Extract the information about the list item that was clicked on
		Cursor cursor = adapter.getItem(position);
		if(cursor != null) {
			String deviceName = cursor.getString(cursor.getColumnIndex(DbManager.DEVICE_NAME));
			
			Long deviceId  = cursor.getLong(cursor.getColumnIndex(DbManager.KEY_DEVICE_ID));
			
			String type = cursor.getString(cursor.getColumnIndex(DbManager.FILENAME));
			
			//Save analytics info
			tracker.trackEvent(TrackingCategory.CATEGORY_DETAIL_PAGE_VIEW, "DevicesViewed", Long.toString(deviceId), 0);
					
			if(type.equals(DbManager.DEFAULT_DEVICE_TYPE)){
				//Launch the generic activity which displays sensor data, get access to graphs etc
				Intent i = new Intent(this, SensorsActivity.class);
				i.putExtra(SensorsActivity.DEVICE_ID, deviceId);
				i.putExtra(SensorsActivity.DEVICE_NAME, deviceName);		
				this.startActivity(i);
			} else { 
				//Launch the video activity, providing links to streaming video
				Intent i = new Intent(this, VideoActivity.class);
				i.putExtra(VideoActivity.DEVICE_ID, deviceId);
				i.putExtra(VideoActivity.DEVICE_NAME, deviceName);	
				i.putExtra(VideoActivity.FILENAME, type);
				this.startActivity(i);
			}
		}
	}
}
