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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.model.TrackingCategory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

/**
 * This is a button click listener to launch the Android media player or browser to play videos.
 * It can accept a properly configured streaming videos from Wowza
 * with the URL in the format: rtsp://serverip/live/myStream.sdp
 * or a regular video links (ie youtube).
 */
public class StreamingButtonListener implements Button.OnClickListener{

	private Context mContext;
	private String mUrl;
	private GoogleAnalyticsTracker tracker;
	
	/**
	 * Constructor of the listener.
	 * 
	 * @param context
	 * @param url
	 */
	public StreamingButtonListener(Context context, String url){
		mContext = context;
		mUrl = url;
	}
	
	/**
	 * Callback for the button click to start the activity to play the video stream.
	 */
	public void onClick(View view) {	
		//Google Analytics setup
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.trackEvent(TrackingCategory.CATEGORY_BUTTON, "streamingvideo", "", 0);
		mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl)));
	}

}
