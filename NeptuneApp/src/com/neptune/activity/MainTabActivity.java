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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.manager.AsyncDevices;
import com.neptune.manager.HttpConnectionManager;
import com.neptune.model.TrackingCategory;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

/**
 * This is the main activity which is the entry point of the who system.
 * It is a container showing only the tabs, and has three visible
 * child activities: DevicesTabActivity, MediaTabActivity, and MapTabActivity.
 */
public class MainTabActivity extends TabActivity{
	private final static String PAGE = "/Launch";
	private GoogleAnalyticsTracker tracker;
	
	private static final String HAS_BEEN_RUN = "HasBeenRun";
	
	private String serviceUrl;
	
	/**
	 * Create all the tabs (Map, Devices and Media) and assign the correspondent Activity to each of the tabs.
	 * 
	 * If this application has just been installed and is run for the first time, 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//Setup for Google Analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(this.getString(R.string.google_analytics_id), this);	//manual dispatch mode. Must explicitly state when sending data.
		
		// This flag is needed for Android's bug in API level 4.
		// Without this flag, the HTTP request may fail sometimes.
		System.setProperty("http.keepAlive", "false");
	
		// Now build the tabs and launch the appropriate activity for each tab.
		Resources res = getResources();
		TabHost tabhost = getTabHost();
		TabHost.TabSpec spec; 									
		Intent intent;									
		
		intent = new Intent().setClass(this, MapTabActivity.class);
		spec = tabhost.newTabSpec(getString(R.string.tab_map)).setIndicator(getString(R.string.tab_map), res.getDrawable(R.drawable.tab_map)).setContent(intent);
		tabhost.addTab(spec);
		
		intent = new Intent().setClass(this, DevicesTabActivity.class);
		spec = tabhost.newTabSpec(getString(R.string.tab_devices)).setIndicator(getString(R.string.tab_devices), res.getDrawable(R.drawable.tab_devices)).setContent(intent);
		tabhost.addTab(spec);
		
		intent = new Intent().setClass(this, MediaTabActivity.class);
		spec = tabhost.newTabSpec(getString(R.string.tab_media)).setIndicator(getString(R.string.tab_media), res.getDrawable(R.drawable.tab_media)).setContent(intent);
		tabhost.addTab(spec);
		
		tabhost.setCurrentTab(0);	//Set Map tab to be the entry point.
		
		serviceUrl = getString(R.string.last_reading_service);
	
		//Check if this is the first time the app has ever been run. 
		//If so, we need to do an initial fetch of sensor/device data
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean has_been_run = preferences.getBoolean(HAS_BEEN_RUN, false);		
		if(has_been_run == false){
			if(HttpConnectionManager.isOnline(this) == true){
				new AsyncDevices(this, true).execute(serviceUrl);	
				Toast t;
				t = Toast.makeText(this, R.string.msg_refreshing, Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				
				//update the prefs file so that future app launches does not automatically refresh data
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean(HAS_BEEN_RUN, true);
				editor.commit();
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
		}	
		
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
	 * Stop the Google Analytics when the activity is being destroyed.
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		tracker.stop();			//stop represents the end of a 'visit' and should be called upon application close.
	}	
	
	/**
	 * Options menu is accessible by pushing the 'menu' button on the device
	 * As this is the 'parent Activity', this optionsMenu is visible/accessible
	 * from DevicesTabActivity, MediaTabActivity, and MapTabActivity.
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
		
		tracker.trackEvent(TrackingCategory.CATEGORY_BUTTON, "DeviceList_refresh", "", 0);
		
		switch (item.getItemId()){
		case R.id.menu_refresh_id:			
			if(HttpConnectionManager.isOnline(this) == true){
				tracker.dispatch();	//send Analytics data whenever we update the devices
				new AsyncDevices(this, false).execute(serviceUrl);
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
	
}


