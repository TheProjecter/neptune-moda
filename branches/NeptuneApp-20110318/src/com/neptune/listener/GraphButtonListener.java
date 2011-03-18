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

package com.neptune.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.activity.GraphActivity;
import com.neptune.activity.TimePickerActivity;
import com.neptune.model.TrackingCategory;

/**
 * This is a button click listener for each of the sensor in the list
 * to display a dialog box for user to select a time period to plot the graph.
 */
public class GraphButtonListener implements Button.OnClickListener{

	private GoogleAnalyticsTracker tracker;
		
	private Context mContext;
 	private String mSensorId;
 	 		
 	/**
 	 * Constructor of the listener
 	 * 
 	 * @param sensorId
 	 * @param context
 	 */
 	public GraphButtonListener(String sensorId, Context context){				
 		mContext = context;
 		mSensorId = sensorId;
 	}
 	
 	/**
 	 * Callback for the event when a sensor is selected.
	 *  1) Create a pop-up dialog offering some generic time parameters and the 'custom' time picker
	 *  
	 *  If a time period is selected other than 'Other' in the list:
	 *  	1) Retrieve the time parameters from the user input and pass them to the GraphActivity
	 *     	   so the graph can be retrieved and displayed.
	 *  if 'Other' is selected, display the custom date/time pickers.
 	 */
 	//Show the AlertDialog with the options of timeframes to pick from
 	public void onClick(View v) {	
 		v.setBackgroundColor(Color.BLACK);				//When a click event occurs, make sure the view is black
		tracker = GoogleAnalyticsTracker.getInstance();
				
  		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 		builder.setTitle(R.string.label_graph_picker);
 		builder.setItems(R.array.labels_graph_picker_times, new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int item){
 				//First check if the 'custom' button was selected
 				if (item == 4){
		 			tracker.trackEvent(TrackingCategory.CATEGORY_DATE_PICKER, "custom", "", 0);
					Intent i = new Intent(mContext, TimePickerActivity.class);
			 		i.putExtra(TimePickerActivity.SENSOR_ID, mSensorId);
				 	mContext.startActivity(i);
 				} else {
 					//Given the selected time frame, construct the appropriate strings to submit
 					//pad with zeros where appropriate, and add 1 to month as month integers range from 0-11
 					//Date format: 2010-06-02%2021:41:28	
 					Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "UTC"));
 					Date endDate = cal.getTime();
 					
 					switch(item){
 					case 0:
 						tracker.trackEvent(TrackingCategory.CATEGORY_DATE_PICKER, "10min", "", 0); //3rd param could be sensorID
 						cal.add(Calendar.MINUTE, -10); break;
 					case 1:
 						tracker.trackEvent(TrackingCategory.CATEGORY_DATE_PICKER, "hour", "", 0); 
 						cal.add(Calendar.HOUR_OF_DAY, -1); break;
 					case 2:
 						tracker.trackEvent(TrackingCategory.CATEGORY_DATE_PICKER, "day", "", 0); 
 						cal.add(Calendar.DATE, -1); break;
 					case 3:
 						tracker.trackEvent(TrackingCategory.CATEGORY_DATE_PICKER, "week", "", 0); 
 						cal.add(Calendar.DATE, -7); break;
 					}
 				
 					Date startDate = cal.getTime();
 					Intent i = new Intent(mContext, GraphActivity.class); 					
 					
 					i.putExtra(GraphActivity.START_DATE, startDate);
 					i.putExtra(GraphActivity.END_DATE, endDate);
 					i.putExtra(GraphActivity.SENSOR_ID, mSensorId);
 					mContext.startActivity(i);		
 				}
 			}
 		});
 		
 		
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
}


