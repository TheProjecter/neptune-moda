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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.listener.FavButtonListener;
import com.neptune.listener.GraphButtonListener;
import com.neptune.manager.AsyncDevices;
import com.neptune.manager.DbManager;
import com.neptune.manager.HttpConnectionManager;
import com.neptune.model.TrackingCategory;

/**
 * This Activity deals with displaying the details of a specific device,
 * when the type of the device is 'scalar' or default. That is
 * any device that has sensors reporting numerical data. (ie temp, pressure etc)
 */
public class SensorsActivity extends Activity {	
	private static final String TAG = "SensorActivity";
	private final static String PAGE = "/SensorsActivity/scalar/";
	private GoogleAnalyticsTracker tracker;
	
	public static final String DEVICE_ID = "DEVICE_ID";
	public static final String DEVICE_NAME = "DEVICE_NAME";

	private Long mDeviceId;
	private String mDeviceName;
	private Cursor sensorCur;
	private Cursor deviceCur;
	private Cursor timeCur;
	private DbManager db;	
	private Context mContext;

	/**
	 * Create the view showing the details of a device and the list of sensors 
	 * from the given device ID and device name.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.device_details);
		mContext = this;
		
		//Google Analytics setup
		tracker = GoogleAnalyticsTracker.getInstance();		
		
		//Another activity launched this activity.
		//Get the data that was bundled with the launched intent.
		Bundle bundle = getIntent().getExtras();
		mDeviceId = bundle.getLong(DEVICE_ID);
		mDeviceName = bundle.getString(DEVICE_NAME);
		
		db = new DbManager(this);
		db.open();
		sensorCur = db.fetchAllSensorData(mDeviceId);
		startManagingCursor(sensorCur);
		deviceCur=db.fetchDeviceDetails(mDeviceId);
		startManagingCursor(deviceCur);
		timeCur = db.fetchSensorTime(mDeviceId);
		startManagingCursor(timeCur);
	
		// Customize header of generic device_details layout
		TextView h = (TextView) findViewById(R.id.devicedetails_header);
		h.setText(mDeviceName);
		
		// Put in the Device information (time, lat/long, depth, location)
		// Because all sensors are 99% of the time the same 'time', we just display the first one.
		deviceCur.moveToFirst();
		timeCur.moveToFirst();
		TextView time = (TextView) findViewById(R.id.devicedetails_time);
		int index = timeCur.getColumnIndex(DbManager.TIME);
		
		if(timeCur.getCount() > 0) {
			timeCur.moveToLast();
			String lastUpdatedTime = getLastUpdatedTime(this, timeCur, index);
			time.setText(lastUpdatedTime);
		} else {
			time.setText(R.string.msg_no_time);
		}
		
		TextView loc = (TextView) findViewById(R.id.devicedetails_location);
		loc.setText(deviceCur.getString(deviceCur.getColumnIndex(DbManager.LOCATION)));

		TextView lat = (TextView) findViewById(R.id.devicedetails_lat);
		lat.setText(deviceCur.getString(deviceCur.getColumnIndex(DbManager.LAT))+"\u00B0"); //unicode for 'degrees' symbol
		TextView longit = (TextView) findViewById(R.id.devicedetails_long);
		longit.setText(deviceCur.getString(deviceCur.getColumnIndex(DbManager.LONG))+"\u00B0");
		
		TextView depth = (TextView) findViewById(R.id.devicedetails_depth);
		depth.setText(String.format(getString(R.string.msg_mbsl_full), deviceCur.getString(deviceCur.getColumnIndex(DbManager.DEPTH))));
		
		// Set listener of favourite button
		Button fav_button = (Button) findViewById(R.id.devicedetails_favbutton);
		FavButtonListener blist = new FavButtonListener(this, fav_button, mDeviceId, FavButtonListener.FAV_BUTTON_TEXT);
		fav_button.setOnClickListener(blist);
		
		// Set listener of 'Previous Locations' button
		Button locations_button = (Button) findViewById(R.id.devicedetails_prevlocationsbutton);
		locations_button.setOnClickListener(new OnClickListener(){
			
			public void onClick(View v) {				
				Intent i = new Intent(mContext, LocationActivity.class);
				i.putExtra(LocationActivity.DEVICE_ID, mDeviceId);
				i.putExtra(LocationActivity.DEVICE_NAME, mDeviceName);
				mContext.startActivity(i);
			}
		});
		
		TableLayout tableLayout = (TableLayout)findViewById(R.id.sensor_tablelayout);
		populateTable(tableLayout, sensorCur);
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
	 * Close the database connection when the activity is being destroyed.
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		db.close();
	}
		
	/**
	 * This is a duplicate of the onCreateOptionsMenu in MainTabActivity,
	 * however because this is a different and unrelated activity, the code
	 * must be repeated here.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	/**
	 * Callback function of the Click action of the menu item.
	 * 
	 * If 'Refresh All Devices' button is clicked, an asynchronous task will executed 
	 * to retrieve the lastest information of all the devices.
	 * 
	 * If 'Info' button is clicked, About page will be displayed.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){		
		tracker.trackEvent(TrackingCategory.CATEGORY_BUTTON, "Detail_refresh", "", 0);
		tracker.dispatch();	//send data whenever we update the devices
		
		switch (item.getItemId()){
		case R.id.menu_refresh_id:			
			if(HttpConnectionManager.isOnline(this) == true){
				new AsyncDevices(this, false).execute(getString(R.string.last_reading_service));
			} else {
				AlertDialog.Builder builder =  new AlertDialog.Builder(this);
		 		builder.setTitle(R.string.label_internet_fail);
		 		builder.setMessage(R.string.msg_no_network_connection);
		 		builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener(){
		 			public void onClick(DialogInterface dialog, int id){
		 				//do nothing
		 				}
		 		});
		 		AlertDialog alert = builder.create();
		 		alert.show();
			}
			return true;
		case R.id.menu_about_id:
			Intent i = new Intent(this,	AboutActivity.class);
			this.startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Helper function to get the 'last updated time' and how long ago the 'last updated time' was.
	 */
	private String getLastUpdatedTime(Context context, Cursor cur, int index){
		Date deviceDate;	//date of the device in the water
		Date curDate;		//local current date in UTC time
		
		//Retrieve current date/time in UTC format, make it a string and then a date object
		//for maths to determine the difference between them
		Calendar curCalendar = Calendar.getInstance(new SimpleTimeZone(0, "UTC"));
		
		SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.format_display_timestamp));
		sdf.setTimeZone(new SimpleTimeZone(0, "UTC"));
		try {
			deviceDate = sdf.parse(cur.getString(index));
			curDate = curCalendar.getTime();
		} catch(ParseException e) {
			Log.e(TAG, "Problem converting time stored in DB to date object. Using current device time. " + e);
			e.printStackTrace();
			deviceDate = curCalendar.getTime();
			curDate = curCalendar.getTime();
		} catch(CursorIndexOutOfBoundsException e) {
			Log.e(TAG, "Device does not have any sensors " + e);
			deviceDate = curCalendar.getTime();
			curDate = curCalendar.getTime();
		} 

		String displayText;
		long difference = ( (curDate.getTime() - deviceDate.getTime()) / (1000*60) ); //Minutes
		if(difference == 1){
			displayText = getString(R.string.msg_last_updated_minute);
		} else if(difference <= 59) {
			displayText = getString(R.string.msg_last_updated_minutes);
		} else if(difference <= 119) {
			displayText = getString(R.string.msg_last_updated_hour);
			difference = difference/60;
		} else if(difference <= 2879) {
			displayText = getString(R.string.msg_last_updated_hours);
			difference = difference/60;
		} else {
			displayText = getString(R.string.msg_last_updated_days);
			difference = difference/(60*24);
		}
		return String.format(displayText, timeCur.getString(index), difference);
	}
	
	/**
	 * Populate the list of sensors into the table layout.
	 * 
	 * @param tableLayout
	 * @param cur
	 */
	private void populateTable (TableLayout tableLayout, Cursor cur) {
		String sensorUnits;
		
		if(cur.getCount() > 0) {
			//Properties for the whole TableLayout
			tableLayout.setShrinkAllColumns(true);				//Shrink = shrink to fit parent object. Stretch = expand to fill space.
			tableLayout.setStretchAllColumns(true);				//Setting both allows it to behave as needed.
		

			//Display sensor information in rows added to the TableLayout
			for(int i=0; i < cur.getCount(); i++){
				cur.moveToPosition(i);
				
				//Grab everything from XML to display the rows
				View row = this.getLayoutInflater().inflate(R.layout.sensor_row, null);
				TextView s_id = (TextView) row.findViewById(R.id.sensor_id);	
				TextView name = (TextView) row.findViewById(R.id.sensor_name);
				TextView value = (TextView) row.findViewById(R.id.sensor_value);
				TextView unit = (TextView) row.findViewById(R.id.sensor_unit);
				
				GraphButtonListener mlistener = new GraphButtonListener(
						cur.getString(cur.getColumnIndex(DbManager.KEY_SENSOR_ID)), this);
				
				s_id.setText(cur.getString(cur.getColumnIndex(DbManager.KEY_SENSOR_ID)));
				name.setText(cur.getString(cur.getColumnIndex(DbManager.SENSOR_NAME)));
				value.setText(cur.getString(cur.getColumnIndex(DbManager.VALUE)));
				
				sensorUnits= cur.getString(cur.getColumnIndex(DbManager.UNITS));
				if(sensorUnits.equals("C") || sensorUnits.equals("F")){	//Degrees C or F
					sensorUnits = "\u00B0"+sensorUnits;
				}
				if(sensorUnits.contains("2")){	//Superscript 2 or 3
					sensorUnits = sensorUnits.replace("2", "\u00B2");
				}
				if(sensorUnits.contains("3")){
					sensorUnits = sensorUnits.replace("3", "\u00B3");
				}
				unit.setText(sensorUnits);
	
				row.setOnClickListener(mlistener);
				row.setOnTouchListener(new OnTouchListener(){
					public boolean onTouch(final View v, MotionEvent event) {
						Runnable r = new Runnable(){							//delay the return to black so we see the orange
							public void run(){
								v.setBackgroundColor(Color.BLACK);
							}
						};
						
						int action = event.getAction();
						switch(action){
						case MotionEvent.ACTION_DOWN:
							v.setBackgroundColor(Color.rgb(255, 165, 0)); break;	//highlights the row clicked in orange
						case MotionEvent.ACTION_MOVE:
							v.postDelayed(r, 800);
							break;
						case MotionEvent.ACTION_UP:
							v.postDelayed(r, 800);
							break;
						case MotionEvent.ACTION_CANCEL:
							v.postDelayed(r, 800);
							break;
						}
						return false;
					}
				});			
				tableLayout.addView(row);
				
				View divider = this.getLayoutInflater().inflate(R.layout.divider, null);
				tableLayout.addView(divider);
			}
		}
	}
}
