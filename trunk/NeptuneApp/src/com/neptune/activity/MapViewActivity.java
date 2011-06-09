package com.neptune.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.neptune.R;

public class MapViewActivity extends Activity {
	private final static String PAGE = "/MapView";
	private GoogleAnalyticsTracker tracker;
	
	private final static String MAP_PATH = "file:///android_res/drawable/map.png";
	
	private WebView mWebView;
	/**
	 * Create the view about NEPTUNE Canada project.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview);
		
		// Setup Google Analystics Tracker
		tracker = GoogleAnalyticsTracker.getInstance();
		
		mWebView = (WebView) findViewById(R.id.map_view_id);
		WebSettings settings = mWebView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	}
	
	/**
	 * Track the page when the activity is being started.
	 */
	@Override
	public void onStart(){
		super.onStart();
		tracker.trackPageView(PAGE);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mWebView.setBackgroundColor(Color.BLACK);
		mWebView.loadUrl(MAP_PATH);
	}
}