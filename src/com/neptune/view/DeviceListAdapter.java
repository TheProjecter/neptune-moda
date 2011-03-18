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

package com.neptune.view;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neptune.R;
import com.neptune.listener.FavButtonListener;
import com.neptune.manager.DbManager;


/**
 * DeviceListAdapter handles displaying a Cursor that contains devices of a location.
 * This includes drawing each individual entry in the list, displaying the
 * name, device id, etc.
 * 
 */
public class DeviceListAdapter extends BaseAdapter {
	
	private static final String TAG = "DeviceListAdapter";

	private Context mContext;
	private Cursor mDevicesCursor; 
	
	/**
	 * Constructor of the DeviceListAdapter.
	 * 
	 * @param context
	 * @param devices
	 */
	public DeviceListAdapter(Context context, Cursor devices){	
		mContext = context;
		mDevicesCursor = devices;		
	}

	/**
	 * Get the number of row, which is the number of devices in a location.
	 */
	public int getCount() {
		return mDevicesCursor.getCount();
	}
	
	/**
	 * Get the ID of a selected item of a given position, which is the device ID.
	 */
	public long getItemId(int position){
		mDevicesCursor.moveToPosition(position);		
		int index = mDevicesCursor.getColumnIndex(DbManager.KEY_DEVICE_ID);
		if(index == -1){
			Log.e(TAG, "No such column " + DbManager.KEY_DEVICE_ID);
			return -1;
		}
		return mDevicesCursor.getInt(index);
	}
	
	/**
	 * Get the value of a selected item in a given position, which is the cursor of the device.
	 */
	public Cursor getItem(int position) {
		mDevicesCursor.moveToPosition(position);
		int index = mDevicesCursor.getColumnIndex(DbManager.DEVICE_NAME);
		if(index == -1){
			Log.e(TAG, "No such column " + DbManager.DEVICE_NAME);
			return null;
		}
		return mDevicesCursor;
	}
	
	/**
	 * Helper function to change the cursor. Used to switch between favourites and all.
	 * 
	 * @param cursor
	 */
	public void updateCursor(Cursor cursor){
		mDevicesCursor = cursor;
		mDevicesCursor.requery();
	}
	
	/**
	 * Construct a view to hold each row (where a row contains a device)
	 */
	public View getView(int position, View convertView, ViewGroup parent){
		RelativeLayout view = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.device_row, null);
		
		ImageButton favButton = (ImageButton)view.findViewById(R.id.fav_button);
		// ImageButton needs to be set not focusable programatically else onListItemClick() in ListActivity will not work
		favButton.setFocusable(false);
		FavButtonListener favButtonListener = new FavButtonListener(mContext, favButton, Long.valueOf(getItemId(position)), FavButtonListener.FAV_BUTTON_STAR);
		favButton.setOnClickListener(favButtonListener);
		
		Cursor cursor = getItem(position);
		if(cursor != null) {
			TextView deviceNameView = (TextView)view.findViewById(R.id.device_name);
			deviceNameView.setText(cursor.getString(cursor.getColumnIndex(DbManager.DEVICE_NAME)));
			
			TextView deviceIdView = (TextView)view.findViewById(R.id.device_id);
			deviceIdView.setText(cursor.getString(cursor.getColumnIndex(DbManager.KEY_DEVICE_ID)));
			
			TextView deviceTypeView = (TextView)view.findViewById(R.id.device_type);
			String deviceType = cursor.getString(cursor.getColumnIndex(DbManager.FILENAME));
			if(deviceType.equals(DbManager.DEFAULT_DEVICE_TYPE)) {
				deviceTypeView.setText("");
			} else {
				deviceTypeView.setText(R.string.msg_video);
			}
		}

		return view;
	}
}
