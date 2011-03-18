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

package com.neptune.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * This helper class provides methods to request webservices.
 * 
 */
public class HttpConnectionManager {
	
	private static final String TAG = "HttpConnectionManager";

	/**
	 * Open an http connection from the given url. 
	 * 
	 * Note: The connection must be closed explicitly after use by calling its disconnect();
	 */
	private static HttpURLConnection openHttpConnection(String urlString) throws IOException {
		int response = -1;	
		URL url = new URL(urlString); 
		URLConnection conn = url.openConnection();					//the actual communication link for exchanging data
		HttpURLConnection httpConn;     							//defines methods for handling HTTP connections
		
		 if (!(conn instanceof HttpURLConnection)){
			 Log.e(TAG, "Not an HTTP connection");
			 throw new IOException("Not an HTTP connection");
		 }
		 
		 try{
			 httpConn = (HttpURLConnection) conn;
			 httpConn.setAllowUserInteraction(false);
			 httpConn.setInstanceFollowRedirects(true);
			 httpConn.setRequestMethod("GET");
			 httpConn.connect(); 

			 response = httpConn.getResponseCode(); 
			 Log.d(TAG, "Response is: " + response);
			 if (response == HttpURLConnection.HTTP_OK){
				 return httpConn;
			 }else{
				 Log.e(TAG, "Unable to connect");
				 //TODO: something else to notify the user? Maybe based on the response code?
			 }
		 }
		 catch (Exception e){
			 Log.e(TAG, "Error conecting to retrieve data ");
			 e.printStackTrace();
			 throw new IOException("Error connecting"); 
		 }
		 return httpConn;
	}

	
	/**
	 * Get the JSON object returned by the Google API by giving a YouTube URL.
	 * 
	 * @param link YouTube URL
	 * @return result of the request in JSON
	 */
	public static JSONObject getYouTubeVideos(String link){

		InputStream in = null;
		JSONObject jobject = null;

		HttpURLConnection httpConn = null;
		BufferedReader br = null;
		try{
			httpConn = openHttpConnection(link);
			in = httpConn.getInputStream();
			
			br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line = null;	
			while((line=br.readLine()) != null){
				sb.append(line);
			}
			
			jobject = new JSONObject(sb.toString());

		} catch (Exception e){
			Log.e(TAG, "Caught an Exception " + e);
			e.printStackTrace();
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				// Ignored
			}
			try {
				if(in != null) {
					in.close();
				}
			} catch (IOException e) {
				// Ignored
			}
			if(httpConn != null) {
				httpConn.disconnect();
			}
		}
		return jobject;
	}
	 
	/**
	 * Get a bitmap by a given URL.
	 */
	public static Bitmap getBitmap(String url) throws IOException{
		
		Bitmap bitmap = null;
		InputStream in = null;
		HttpURLConnection httpConn = null;
		
        try {
            httpConn = openHttpConnection(url);
			in = httpConn.getInputStream();
            
			//Decode image size
            bitmap = BitmapFactory.decodeStream(in);
                    
        } catch (IOException e){
        	Log.e(TAG,"Caught an IO Exception during a get bitmap " + e);
        	e.printStackTrace();
        	throw e;
        } finally {
        	try {
	        	if(in != null) {
	        		in.close();
	        	}
        	} catch (IOException e) {
        		// Ignored.
        	}
        	if(httpConn != null) {
        		httpConn.disconnect();
        	}
        }
        return bitmap;                
    }
	
	/**
	 * Get the sensor data in JSON from DMAS last reading web service.
	 * 
	 */
	public static JSONObject getSensorData(String url) throws Exception{
		JSONObject jobject = null;
		
		BufferedReader br = null;
		InputStream in = null;
		HttpURLConnection httpConn = null;
		
		try{
			httpConn = openHttpConnection(url);
			in = httpConn.getInputStream();
			
			br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line = null;	
			while((line=br.readLine()) != null){
				sb.append(line);
			}
			
			jobject = new JSONObject(sb.toString());
		} catch (Exception e) {
			Log.e(TAG, "Caught an Exception during a getSensorData " + e);
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				// Ignored.
			}
			try {
				if(in != null) {
					in.close();
				}
			} catch (IOException e) {
				// Ignored.
			}
			if(httpConn != null) {
				httpConn.disconnect();
			}
		}
		return jobject;
	}
	
	
	/**
	 * Helper function to check if a network connection is available
	 * 
	 * @param context
	 * @return true if network connection is available, else return false.
	 */
	public static boolean isOnline(Context context){
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		// Check for connection either via wifi or mobile
		if(networkInfo == null || !networkInfo.isConnected()){
			Log.i(TAG, "Connection = false");		
			return false;
		}
		return true;
	}
	
	
	
	
}
