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
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;

/**
 * This Activity create a tab that the user can navigate to. It is simply a static image of a map, 
 * and cannot be interacted with.
 * 
 */
public class MapTabActivity extends Activity {

	private static final String PAGE = "/Launch/MapTab";
	private GoogleAnalyticsTracker tracker;

	/**
	 * Create a tab that the user can navigate to. It is simply a static image of a map
	 * and description about NEPTUNE Canada.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.map);
		
		//Setup for Google Analytics
		tracker = GoogleAnalyticsTracker.getInstance();
		
		ImageView mapView = (ImageView) findViewById(R.id.map_id);
		mapView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(MapTabActivity.this,	MapViewActivity.class);
				MapTabActivity.this.startActivity(i);
			}
			
		});
		
		//Setup text with URL links
		TextView text = (TextView) findViewById(R.id.map_text);
		text.setMovementMethod(LinkMovementMethod.getInstance());
		String s = this.getString(R.string.msg_neptube_about_with_link);
		text.setText(Html.fromHtml(s));
	}
	
	/**
	 * Track the page when the activity is being started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		tracker.trackPageView(PAGE);		
	}
}
