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

import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;

/**
 * Activity to display graph image fetched from DMAS plotting service 
 * by sending the detected orientation and size of the display.
 * 
 * This activity is prevented from being restarted when the graph is being
 * fetched if the orientation is changed until the graph fetched is done. 
 */
public class GraphActivity extends Activity {
	
	private static final String TAG = "GraphActivity";
	private final static String PAGE = "/GraphActivity/";
	private GoogleAnalyticsTracker tracker;
	
	public static final String START_DATE = "START_DATE";
	public static final String END_DATE = "END_DATE";
	public static final String SENSOR_ID = "SENSOR_ID";
	
	private int imageWidth = 500;					//default height/width of nexus one
	private int imageHeight = 300;
	
	private WebView mWebView;
	private TextView mTextView;
	private TextView nodata;
	private Display display;
	private String url;
	
	/**
	 * Constructing the graph view by getting the size (width & height) of the display and 
	 * calling the DMAS plotting service with the display size in a separate thread to get 
	 * the graph image generated by DMAS.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.graph);
		
		// Setup Google Analystics Tracker
		tracker = GoogleAnalyticsTracker.getInstance();
		
		mWebView = (WebView) findViewById(R.id.graph_container);
		mWebView.setInitialScale(100);
		mWebView.setWebViewClient(new PinchZoomableGraphViewClient(this));
		WebSettings settings = mWebView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		mTextView = (TextView) findViewById(R.id.backup_text);
		nodata = (TextView) findViewById(R.id.nodata);	
		
		Bundle bundle = getIntent().getExtras();
		Date startDate = (Date)bundle.get(START_DATE);
		Date endDate = (Date)bundle.get(END_DATE);
		String sensorId = bundle.getString(SENSOR_ID);
		
		//Choose the size of the image based on screen size and orientation
		//This automatically works even when the device is tilted (that is width and height are swapped)
		display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();   
		imageWidth = display.getWidth();  
		imageHeight = display.getHeight();  
						
		url = String.format(this.getString(R.string.plot_service), sensorId, imageWidth, imageHeight, startDate, endDate);
		
		Log.d(TAG, "Calling service: " + url);
	}

	/**
	 * Track the page when the activity is being started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		tracker.trackPageView(PAGE);
	}
	
	public void onResume() {
		super.onResume();
		mWebView.loadUrl(url);
	}
	
	private class PinchZoomableGraphViewClient extends WebViewClient {		
		private ProgressDialog dialog;
		private Context mContext;
		
		public PinchZoomableGraphViewClient(Context mContext) {
			this.mContext = mContext;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			int orientation = display.getOrientation();
			if(orientation == Surface.ROTATION_0){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			//launch 'loading' dialog
			dialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setTitle(mContext.getString(R.string.label_fetch_graph));
			//dialog.setCancelable(false);					//pressing back would cancel the dialog, NOT the task
			dialog.show();
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			dialog.dismiss();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			nodata.setVisibility(View.GONE);  					//remove the 'no data' text
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			dialog.dismiss();
			mTextView.setText(R.string.msg_get_graph_fail);
			mTextView.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * An asynchronous task to get the graph image from DMAS plotting service and 
	 * display it on the screen.
	 * 
	 * It prevents the orientation changes when fetching the graph. If the orientation
	 * is changed when fetching the graph, it sets the orientation request to the GraphActivity
	 * so that the activity will be restarted to the new orientation after the graph is fetched.
	 * 
	 * Because we are modifying UI elements (mTextView, nodata) from another thread, 
	 * this class needs to be in the GraphActivity class.
	 */
//	private class AsyncGraphs extends AsyncTask<String, Void, Bitmap>{
//
//		private Context mContext;
//		private ProgressDialog dialog;
//		
//		public AsyncGraphs(Context context){
//			super();
//			mContext = context;
//		}
//		
//		/**
//		 * Prevent the orientation changes when fetching the graph.
//		 * 
//		 * Display the fetching dialog box.
//		 */
//		protected void onPreExecute(){
//			//Temporarily prevent orientation changes while fetching the graph
//			int orientation = display.getOrientation();
//			if(orientation == Surface.ROTATION_0){
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//			} else {
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//			}
//			//launch 'loading' dialog
//			dialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
//			dialog.setIndeterminate(true);
//			dialog.setTitle(mContext.getString(R.string.label_fetch_graph));
//			//dialog.setCancelable(false);					//pressing back would cancel the dialog, NOT the task
//			dialog.show();									
//		}
//		
//		/**
//		 * Getting the image of the graph by calling the web service through HttpConnectionManager.
//		 */
//		protected Bitmap doInBackground(String... url) {
//			Bitmap graph = null;
//			try{
//				graph = HttpConnectionManager.getBitmap((String)url[0]);
//			}catch(Exception ex){
//				Log.e(TAG, "Failed asynchronous fetch of graph");
//				Log.e(TAG, "Exception: " + ex);
//				ex.printStackTrace();
//				mTextView.setText(R.string.msg_get_graph_fail);
//				mTextView.setVisibility(View.VISIBLE);
//			}
//			return graph;
//		}
//		
//		/**
//		 * Dismiss the progress dialog box, release the lock for orientation changes and
//		 * display the graph image after calling the web service.
//		 * 
//		 * If graph image is not available, display a failure dialog box.
//		 */
//		protected void onPostExecute(Bitmap b){
//			dialog.dismiss();
//			//no longer in danger with orientation changes, release lock
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);	
//			nodata.setVisibility(View.GONE);  					//remove the 'no data' text
//			if(b != null){
//				mImageView.setImageBitmap(b);
//			}else{
//				mTextView.setText(R.string.msg_get_graph_fail);
//				mTextView.setVisibility(View.VISIBLE);
//			}
//		}
//		
//	}
}