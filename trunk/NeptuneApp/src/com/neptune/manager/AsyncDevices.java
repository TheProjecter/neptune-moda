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

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.neptune.R;


/**
 *  This class makes an asynchronous call to the webservice and updates the database
 *  with new device/sensor information.
 *  
 *  This class can either block the UI screen or run the process in the background depends
 *  on the isShowProgress flag set in constructor.
 */
public class AsyncDevices extends AsyncTask<String, Void, Boolean>{

	private static final String TAG = "AsyncDevices";

	private Context mContext;
	private Toast t;
	private DbManager db;
	private ProgressDialog dialog;
	private boolean isShowProgress;

	/**
	 * Constructor of the AsyncDevices.
	 * 
	 * @param context
	 * @param isShowProgress true to prompt the progress dialog box, else just run in the background.
	 */
	public AsyncDevices(Context context, boolean isShowProgress){
		super();
		mContext = context;
		this.isShowProgress = isShowProgress;
	}
	
	/**
	 * If isShowProgress is set to be true, display a progress dialog box 
	 * and block screen until process is done.
	 */
	protected void onPreExecute(){
		if(isShowProgress) {
			dialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
			dialog.setTitle(mContext.getString(R.string.label_fetching));
			dialog.setCancelable(false);					//pressing back would cancel the dialog, NOT the task
			dialog.show();									//thus do not make the dialog cancelable
		}
	}
	
	/**
	 * Request the webservice and add/update the data returned into the database.
	 */
	protected Boolean doInBackground(String...url){		
		try{
			db = new DbManager(mContext);
			db.open();
			JSONObject j = HttpConnectionManager.getSensorData(url[0]);
			db.insertFromJSONObject(j);
			Log.i("AsyncDevices", "DATABASE LOADING IS DONE.");
		} catch(Exception ex) {
			Log.e(TAG, "Error in Async: " + ex);
			ex.printStackTrace();
			return false;
		} finally {
			db.close();
		}
		return true;
	}
	
	/**
	 * After executing the request to get the device information,
	 * 1) close the progress dialog box if isShowProgress flag was set.
	 * 2) display refresh successful message if the process was successfully done, 
	 *    else display refresh failed message.
	 */
	protected void onPostExecute(Boolean result){
		if(isShowProgress && dialog != null) {
			dialog.dismiss();
		}
		String message;
		if(result == true) {
			message = mContext.getString(R.string.msg_refresh_success);
		} else {
			message = mContext.getString(R.string.msg_refresh_fail);
		}
		t = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();		
	}

}