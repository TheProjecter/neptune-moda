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

import java.net.URLDecoder;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;
import com.neptune.listener.StreamingButtonListener;
import com.neptune.manager.HttpConnectionManager;

/**
 * This Activity manages all extra 'media' including YouTube Videos.
 * An Internet connection is required to view media.
 */
public class MediaTabActivity extends Activity {
	
	private static final String TAG = "MediaTabActivity";
	private static final String PAGE = "/Launch/MediaTab";
	private GoogleAnalyticsTracker tracker;
	
	private TextView nodata;
	private LinearLayout mLayout;
	
	/**
	 * Create the view that shows the list of YouTube videos by requesting
	 * the list of media from YouTube web service.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media);
		
		tracker = GoogleAnalyticsTracker.getInstance();
		
		mLayout = (LinearLayout) findViewById(R.id.media_linearlayout);
		nodata = (TextView) findViewById(R.id.nodata);	
		
		if(HttpConnectionManager.isOnline(this) == true){
			tracker.dispatch();	//send Analytics data whenever we connect
			new AsyncYoutube(this, mLayout).execute(getString(R.string.youtube_service));
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
	
	/**
	 * Track the page when the activity is being started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		tracker.trackPageView(PAGE);
	}
		
	/**
	 * Asynchronous class to allow more natural loading of the videos and images
	 * Private class as it is accessing UI elements from the MediaTabActivity (nodata),
	 * an element which is made global so that it can be modified from an asynchronous
	 * thread.
	 */
	private class AsyncYoutube extends AsyncTask<String, Void, JSONObject> {
		
		private LinearLayout mLayout;
		private Context mContext;
		private SimpleDateFormat displayFormatter;
		private SimpleDateFormat isoFormatter;
		
		private static final int MAX_VIDEO_THUMBNAIL_HEIGHT = 90;
		private static final int MAX_VIDEO_THUMBNAIL_WIDTH = 120;
		
		private ProgressDialog dialog;
		
		public AsyncYoutube(Context context, LinearLayout layout) {
			mContext =context;
			mLayout = layout;
			
			displayFormatter = new SimpleDateFormat(mContext.getString(R.string.format_display_timestamp));
			isoFormatter = new SimpleDateFormat(mContext.getString(R.string.format_iso8601_timestamp));
		}

		
		protected void onPreExecute(){
			dialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setTitle(mContext.getString(R.string.label_fetching));
			dialog.setCancelable(false);					//pressing back would cancel the dialog, NOT the task
			dialog.show();									//thus do not make the dialog cancelable. It will time out if it fails.
		}
		
		/**
		 * Request the list of media in JSON format from YouTube web service.
		 */
		@Override
		protected JSONObject doInBackground(String... link) {
			JSONObject jsonObj = HttpConnectionManager.getYouTubeVideos(link[0]);
			return jsonObj;
		}
		
		/**
		 * Create the view that shows the list of media from the JSON returned from the YouTube web service.
		 * 
		 * More information about the reurn of video entries from YouTube API:
		 * 
		 * http://code.google.com/apis/youtube/2.0/developers_guide_protocol.html#Understanding_Video_Entries
		 * http://code.google.com/apis/gdata/docs/json.html
		 */
		protected void onPostExecute(JSONObject jsonObj){
			dialog.dismiss();
			
			try {
				//remove the 'no data' text
				nodata.setVisibility(View.GONE);  				

				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				//create a row for each video and display it on screen
				
				JSONObject feed;
				JSONArray entries;
				JSONObject entry;
				JSONObject media;
				
				//Recyclable JSON object and array
				JSONObject jobj;
				JSONArray jarray;
				
				feed = jsonObj.getJSONObject("feed");
				entries = feed.getJSONArray("entry");
				Log.d(TAG, "Number of video="+entries.length());
				
				for(int i=0; i<entries.length(); i++){
					// Get every entry.
					entry = entries.getJSONObject(i);
					media = entry.getJSONObject("media$group");
					
					// Get the view object of each row.
					View mediaView = inflater.inflate(R.layout.media_row, null);
					
					// Get the title of the video.
					jobj = media.getJSONObject("media$title");
					TextView titleView = (TextView) mediaView.findViewById(R.id.media_title);					
					titleView.setText(jobj.getString("$t"));
					
					// Get the published date and time of the video.
					jobj = entry.getJSONObject("published");										
					TextView publishview = (TextView) mediaView.findViewById(R.id.media_published);
					publishview.setText(displayFormatter.format(isoFormatter.parse(jobj.getString("$t"))));
					
					// Get the URL of the video stream
					jarray = media.getJSONArray("media$player");
					jobj = jarray.getJSONObject(0);
					
					//Hook up an onClickListener to play the video when the row is selected.
					//Also manually making the background of the row change colour when 'clicked'.
					StreamingButtonListener list = new StreamingButtonListener(mContext, URLDecoder.decode(jobj.getString("url"), "UTF-8"));
					mediaView.setOnClickListener(list);
					mediaView.setOnTouchListener(new OnTouchListener(){
						public boolean onTouch(final View v, MotionEvent event) {
							Runnable r = new Runnable(){					//delay the return to black so we see the orange
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
					
					// Get the thumbnail of the video.
					jarray = media.getJSONArray("media$thumbnail");
					
					// Youtube service returns a list of thumbnails with size 120x90 and 320x240
					// Use the the thumbnail of size 120x90 to avoid OutOfMemoryError
					
					// Use the first thumbnail (of size 120x90) available
					// If it cannot find any thumbnail of size 120x90, use the last one 
					for(int index=0; index<jarray.length(); index++) {
						jobj = jarray.getJSONObject(index);
						int height = Integer.parseInt(jobj.getString("height"));
						int width = Integer.parseInt(jobj.getString("width"));
						if((height == MAX_VIDEO_THUMBNAIL_HEIGHT && width == MAX_VIDEO_THUMBNAIL_WIDTH) 
								|| index == (jarray.length()-1)) {
							ImageView thumbNailView = (ImageView) mediaView.findViewById(R.id.media_video);
							try{
								Bitmap b = HttpConnectionManager.getBitmap(URLDecoder.decode(jobj.getString("url"), "UTF-8"));						
								thumbNailView.setImageBitmap(b);
							}catch(Exception e){
								Log.e(TAG, "Problem retrieving video image." + e);
								e.printStackTrace();
								thumbNailView.setImageResource(R.drawable.icon); //NEPTUNE logo displayed if no image
							}
							
							break;
						}
					}
					
					// Add the view into the layout.
					mLayout.addView(mediaView);
				}
			} catch (Exception e) {
				Log.e(TAG, "Problem fetching videos: " + e);
				e.printStackTrace();
				AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
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
	} // End of Async class			
}

