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

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.manager.DbManager;
import com.neptune.model.TrackingCategory;

/**
 * Favourite Button listener is set up to use multiple button types, so long as they are
 * derived from android.view.View. Currently supports changing Buttons (with text) and
 * the yellow/grey star image.
 */
public class FavButtonListener implements Button.OnClickListener {
	
	public static final int FAV_BUTTON_TEXT = 0;
	public static final int FAV_BUTTON_STAR = 1;
		
	private GoogleAnalyticsTracker tracker;
		
	private View mButton;
	private long mDeviceId;
	private int mType;	
	private DbManager db;
			
	/**
	 * Create the listener for the Favourite button and update the display (either text or star button) 
	 * accordingly by checking wheteher the device is in the favourite list or not.
	 * 
	 * @param context
	 * @param view
	 * @param deviceId
	 * @param type
	 */
	public FavButtonListener(Context context, View view, long deviceId, int type){

		mButton = view;
		mDeviceId = deviceId;
		mType = type;
		
		db = new DbManager(context);
		db.open();
		boolean state = db.checkFavourite(mDeviceId);
		db.close();
		updateButton(state);
	}

	/**
	 * Callback for the button click event to toggle the favourite state of the device.
	 * 
	 * If the device is a favourite device, it will be removed from the favourite list.
	 * 
	 * If the device is not a favourite device, it will be added into the favourite list.
	 */
	public void onClick(View v) {
		db.open();
		boolean state = db.checkFavourite(mDeviceId);
		if(state == true){
			db.removeFavourite(mDeviceId);
		} else {
			db.addFavourite(mDeviceId);
		}
		// Now track for google analytics
		// If getInstance() was in the constructor, then it would be created and destroyed repeatedly
		// As the user scrolled through the list. That visibly slows scrolling down. 
		// So it's retrieved at the time of the click.
		tracker = GoogleAnalyticsTracker.getInstance();
		
		if(mType == FAV_BUTTON_TEXT){ 
			tracker.trackEvent(TrackingCategory.CATEGORY_BUTTON, "Detail_favourite", Long.toString(mDeviceId), 0);
		}
		else if(mType == FAV_BUTTON_STAR){
			tracker.trackEvent(TrackingCategory.CATEGORY_BUTTON, "DeviceList_favourite", Long.toString(mDeviceId), 0);
		}
		db.close();
		updateButton(!state);
	}
		
		
	/**
	 * Update the button based on its type (either text or image) according to the state given.
	 * 
	 * @param state
	 */
	private void updateButton(boolean state){
		switch(mType){
		case FAV_BUTTON_TEXT:
			if(state == true){
				((Button) mButton).setText(R.string.btn_remove_from_fav);
			}
			else{
				((Button) mButton).setText(R.string.btn_add_to_fav);
			}
			break;
		case FAV_BUTTON_STAR:
			if(state == true){
				((ImageButton) mButton).setImageResource(R.drawable.fav);
			}
			else{
				((ImageButton) mButton).setImageResource(R.drawable.un_fav);
			}
			break;
		}
	}
		
	
}

