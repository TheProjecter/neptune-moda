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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.listener.FavButtonListener;
import com.neptune.listener.StreamingButtonListener;
import com.neptune.manager.DbManager;

/**
 * Activity to display sensor information on a device of type 'camera'.
 * Presents basic device info (location, depth etc) and a button to watch
 * live streaming video.
 */

public class VideoActivity extends Activity {
	private final static String PAGE = "/SensorsActivity/";	
	private GoogleAnalyticsTracker tracker;
	
	public static final String DEVICE_ID = "DEVICE_ID";
	public static final String DEVICE_NAME = "DEVICE_NAME";
	public static final String FILENAME = "FILENAME";
	
	private DbManager db;	
	private Cursor deviceCur;
	private String device_name;
	private Long device_id;
	private Context mContext;
	private String filename;
	
	/**
	 * Create the view to display the basic device information and
	 * button to watch live streaming video. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.video_list);
		
		mContext = this;
		
		//Google Analytics setup
		tracker = GoogleAnalyticsTracker.getInstance();		

		Bundle bundle = getIntent().getExtras();
		device_name = bundle.getString(DEVICE_NAME);
		device_id = bundle.getLong(DEVICE_ID);
		filename = bundle.getString(FILENAME);

		
		db = new DbManager(this);
		db.open();
		deviceCur=db.fetchDeviceDetails(device_id);
		startManagingCursor(deviceCur);
		deviceCur.moveToFirst();
		
		//Customize the view and set up the listener for the streaming video
		TextView header = (TextView) findViewById(R.id.videoview_header);
		header.setText(device_name);

		TextView loc = (TextView) findViewById(R.id.devicedetails_location);
		loc.setText(deviceCur.getString(deviceCur.getColumnIndex(DbManager.LOCATION)));

		TextView lat = (TextView) findViewById(R.id.devicedetails_lat);
		lat.setText(deviceCur.getString(deviceCur.getColumnIndex(DbManager.LAT))+"\u00B0");
		TextView longit = (TextView) findViewById(R.id.devicedetails_long);
		longit.setText(deviceCur.getString(deviceCur.getColumnIndex(DbManager.LONG))+"\u00B0");
		
		TextView depth = (TextView) findViewById(R.id.devicedetails_depth);
		depth.setText(String.format(getString(R.string.msg_mbsl_full), deviceCur.getString(deviceCur.getColumnIndex(DbManager.DEPTH))));
				
		String video_path = "rtsp://" +
							deviceCur.getString(deviceCur.getColumnIndex(DbManager.STREAM_ADDRESS)) + "/" +
							filename;
		Button stream = (Button) findViewById(R.id.streaming_button);
		StreamingButtonListener vlist = new StreamingButtonListener(this, video_path);
		stream.setOnClickListener(vlist);
		
		// Set onClickListener for 'favourite' button
		Button fav = (Button) findViewById(R.id.videoview_favourite);
		FavButtonListener fav_list = new FavButtonListener(this, fav, device_id, FavButtonListener.FAV_BUTTON_TEXT);
		fav.setOnClickListener(fav_list);
		
		// Set listener of 'Previous Locations' button from device_details layout
		Button locations_button = (Button) findViewById(R.id.devicedetails_prevlocationsbutton);
		locations_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				tracker.trackPageView("/LocationHistory/"+device_id);
				Intent i = new Intent(mContext, LocationActivity.class);
				i.putExtra(LocationActivity.DEVICE_ID, device_id);
				i.putExtra(LocationActivity.DEVICE_NAME, device_name);
				mContext.startActivity(i);
			}
		});
		
	}
	
	/**
	 * Track the page when the activity is being started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		tracker.trackPageView(PAGE + filename + "/");
	}
	
	/**
	 * Close the database when the activity is being destroyed.
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		db.close();
	}
	
}