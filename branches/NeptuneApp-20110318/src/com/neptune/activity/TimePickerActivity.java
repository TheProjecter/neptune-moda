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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.neptune.R;


/**
 * This activity works with Date and TimePickerDialog to select custom
 * date/times and then request a graph with those date/times. GraphActivity
 * can be launched from this Activity.
 */
public class TimePickerActivity extends Activity {
	public static final String SENSOR_ID = "SENSOR_ID";
	
	private String mSensorId;
	
	// Objects associated with the start date and time
	private TextView mDateDisplayStart;
	private Button mPickDateStart;
	private Button mPickTimeStart;
	
	private int mYearStart;
	private int mMonthStart;
	private int mDayStart;
	private int mHourStart;
	private int mMinuteStart;
	
	// Objects associated with the end date and time
	private TextView mDateDisplayEnd;
	private Button mPickDateEnd;
	private Button mPickTimeEnd;
	
	private int mYearEnd;
	private int mMonthEnd;
	private int mDayEnd;
	private int mHourEnd;
	private int mMinuteEnd;

	// Constant IDs of the dialogs for both the start and end date and time.
	private static final int START_DATE_DIALOG_ID = 0;
	private static final int START_TIME_DIALOG_ID = 1;
	private static final int END_DATE_DIALOG_ID = 2;
	private static final int END_TIME_DIALOG_ID = 3;
	
	/**
	 * Create the view for user to select the start and end timestamp for the graph plotting.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.timepicker);
		
		Bundle bundle = getIntent().getExtras();
		mSensorId = bundle.getString(SENSOR_ID);
				
		// Set listener for Date
		mPickDateStart = (Button) findViewById(R.id.datepicker_start_button);
		mPickDateStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				showDialog(START_DATE_DIALOG_ID);
			}
		});
		// Set listener for Time
		mPickTimeStart = (Button) findViewById(R.id.timepicker_start_button);
		mPickTimeStart.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				showDialog(START_TIME_DIALOG_ID);
			}
		});
		// Set listener for End Date
		mPickDateEnd = (Button) findViewById(R.id.datepicker_end_button);
		mPickDateEnd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				showDialog(END_DATE_DIALOG_ID);
			}
		});
		// Set listener for End Time
		mPickTimeEnd =(Button) findViewById(R.id.timepicker_end_button);
		mPickTimeEnd.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				showDialog(END_TIME_DIALOG_ID);
			}
		});		

		// Get the current UTC date and time
		final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		mYearStart = c.get(Calendar.YEAR);
		mMonthStart = c.get(Calendar.MONTH); 			//need to add 1 to month as the month numbers range from 0-11
		mDayStart = c.get(Calendar.DAY_OF_MONTH);
		mHourStart = c.get(Calendar.HOUR_OF_DAY);
		mMinuteStart = c.get(Calendar.MINUTE);
		
		mYearEnd = mYearStart;
		mMonthEnd = mMonthStart;
		mDayEnd = mDayStart;
		mHourEnd = mHourStart;
		mMinuteEnd = mMinuteStart;
		
		// Display current UTC date and time
		updateDisplay();	
		
		// 'Graph' button to actually send request for graph
		Button graph = (Button) findViewById(R.id.graph_timepicker);
		graph.setOnClickListener(new OnClickListener(){
			public void onClick(View v){				
			 	c.set(mYearStart, mMonthStart, mDayStart, mHourStart, mMinuteStart, 0);
			 	Date startDate = c.getTime();
			 	c.set(mYearEnd, mMonthEnd, mDayEnd, mHourEnd, mMinuteEnd, 0);
			 	Date endDate = c.getTime();
			 	
			 	//make sure end date occurs after start date
			 	if(endDate.before(startDate)) {			 		
			 		Toast t = Toast.makeText(getApplicationContext(), R.string.msg_start_end_dates, Toast.LENGTH_SHORT);
			 		t.setGravity(Gravity.CENTER, 0, 0);
			 		t.show();
			 	} else {
 					Intent i = new Intent(TimePickerActivity.this, GraphActivity.class);
 					i.putExtra(GraphActivity.START_DATE, startDate);
 					i.putExtra(GraphActivity.END_DATE, endDate);
 					i.putExtra(GraphActivity.SENSOR_ID, mSensorId);
 					TimePickerActivity.this.startActivity(i);		
			 	}	
			}
		});
	}
	
	
	/**
	 *  Callback for 'setting' a date using the start date picker
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener(){
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
			mYearStart = year;
			mMonthStart = monthOfYear;
			mDayStart = dayOfMonth;
			updateDisplay();
		}
	};
	
	/**
	 *  Callback for 'setting' a time using the start time picker
	 */
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener(){
		public void onTimeSet(TimePicker view, int hourOfDay, int minute){
			mHourStart = hourOfDay;
			mMinuteStart = minute;
			updateDisplay();
		}
	};
	
	/**
	 *  Callback for 'setting' a date using the end date picker
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListenerEnd = new DatePickerDialog.OnDateSetListener(){
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
			mYearEnd = year;
			mMonthEnd = monthOfYear;
			Log.d("TimePickerActivity", "Month End: " + mMonthEnd);
			mDayEnd = dayOfMonth;
			updateDisplay();
		}
	};
	
	/**
	 *  Callback for 'setting' a time using the end time picker
	 */
	private TimePickerDialog.OnTimeSetListener mTimeSetListenerEnd = new TimePickerDialog.OnTimeSetListener(){
		public void onTimeSet(TimePicker view, int hourOfDay, int minute){			
			mHourEnd = hourOfDay;
			mMinuteEnd = minute;
			updateDisplay();
		}
	};
	
	/**
	 * Show DatePicker or TimePicker dialog box by a given ID.
	 */
	@Override 
	protected Dialog onCreateDialog(int id){
		switch (id) {
		case START_DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYearStart, mMonthStart, mDayStart);
		case START_TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, mHourStart, mMinuteStart, true); //true makes it 24 hour 
		case END_DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListenerEnd, mYearEnd, mMonthEnd, mDayEnd);
		case END_TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListenerEnd, mHourEnd, mMinuteEnd, true); //true makes it 24 hour 
		}
		return null;
	}
	

	/**
	 * Update the display of date and time for the start and end labels.
	 */
	private void updateDisplay(){
		Calendar c = Calendar.getInstance();
		
		mDateDisplayStart = (TextView) findViewById(R.id.start_datetime);		
		c.set(mYearStart, mMonthStart, mDayStart, mHourStart, mMinuteStart);
		mDateDisplayStart.setText(String.format(getString(R.string.msg_picked_start_date_time), c));
		
		mDateDisplayEnd = (TextView) findViewById(R.id.end_datetime);		
		c.set(mYearEnd, mMonthEnd, mDayEnd, mHourEnd, mMinuteEnd);
		mDateDisplayEnd.setText(String.format(getString(R.string.msg_picked_end_date_time), c));
	}
}
		
