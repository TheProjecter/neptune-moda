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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.manager.DbManager;

/**
 * Simple activity for displaying the location history of a device.
 * It is launched from the SensorsActivity.
 */
public class LocationActivity extends Activity{
	private final static String PAGE = "/LocationHistory/";
	private GoogleAnalyticsTracker tracker;
	
	public static final String DEVICE_ID = "DEVICE_ID";
	public static final String DEVICE_NAME = "DEVICE_NAME";
	
	private Long mDevice_id;
	private String mDevice_name;
	private Cursor mCursor;
	private DbManager db;		

	/**
	 * Construct the table of location history of a device by getting the device name and device ID
	 * from the Intent.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.locationhistory);
		
		// Setup Google Analystics Tracker
		tracker = GoogleAnalyticsTracker.getInstance();
				
		//Get the data that was bundled with the launched intent.
		Bundle bundle = getIntent().getExtras();
		mDevice_id = bundle.getLong(DEVICE_ID);
		mDevice_name = bundle.getString(DEVICE_NAME);

		db = new DbManager(this);
		db.open();
		mCursor = db.fetchLocationsForDevice(mDevice_id);
	
		//Customize header of generic device_details layout
		TextView deviceDetailsHeaderView = (TextView) findViewById(R.id.devicedetails_header);
		deviceDetailsHeaderView.setText(mDevice_name);
		
		TableLayout tableLayout = (TableLayout)findViewById(R.id.locationhistory_linearlayout);
		populateTable(tableLayout, mCursor);
		
		mCursor.close();
		db.close();
	}
	
	/**
	 * Track the page when the activity is being started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		tracker.trackPageView(PAGE + mDevice_id);
	}
	
	/**
	 * Construct the table of location history.
	 * 
	 * @param tableLayout TableLayout defined in the layout resource file.
	 * @param cur Cursor of the location history of the device.
	 */
	private void populateTable(TableLayout tableLayout, Cursor cur) {
		//Properties for the whole TableLayout
		if(cur.getCount() > 0) {			
			tableLayout.setShrinkAllColumns(true);				//Shrink = shrink to fit parent object. Stretch = expand to fill space.
			tableLayout.setStretchAllColumns(true);				//Setting both allows it to behave as needed.
		}
						
		// The query of location is sorted by date_from desc,
		// so the first item in the list is the latest location.
		for(int i=0; i<cur.getCount(); i++) {
			cur.moveToPosition(i);
			
			//Grab everything from XML to display the rows
			View row = this.getLayoutInflater().inflate(R.layout.locationhistory_row, null);
			TextView loc = (TextView) row.findViewById(R.id.location_location);
			loc.setText(cur.getString(cur.getColumnIndex(DbManager.LOCATION)));
			TextView time = (TextView) row.findViewById(R.id.location_time);
			time.setText(cur.getString(cur.getColumnIndex(DbManager.DATEFROM)));
			TextView lat = (TextView) row.findViewById(R.id.location_lat);
			lat.setText(cur.getString(cur.getColumnIndex(DbManager.LAT))+"\u00B0");
			TextView longit = (TextView) row.findViewById(R.id.location_long);
			longit.setText(cur.getString(cur.getColumnIndex(DbManager.LONG))+"\u00B0");
			TextView depth = (TextView) row.findViewById(R.id.location_depth);
			depth.setText(cur.getString(cur.getColumnIndex(DbManager.DEPTH)));
		
			tableLayout.addView(row);
			
			View divider = this.getLayoutInflater().inflate(R.layout.divider, null);
			tableLayout.addView(divider);
		}
	}
}
