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

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.neptune.R;

public class DbManager {
	private static final String TAG = "DbManager";
	
	// Database column
	public static final String KEY_SENSOR_ID = "sensor_id";
	public static final String SENSOR_NAME = "sensor_name";
	public static final String VALUE = "value";
	public static final String KEY_DEVICE_ID = "device_id";
	public static final String DEVICE_NAME = "device_name";
	public static final String IS_FAV = "is_favourite";
	public static final String TIME = "sample_time";
	public static final String UNITS = "sensor_units";
	public static final String FILENAME = "filename";
	public static final String LOCATION = "location";
	public static final String DEPTH = "depth";
	public static final String LAT = "latitude";
	public static final String LONG = "longitude";
	public static final String DATEFROM = "date_from";
	public static final String STATUS = "status";
	public static final String SENSORS = "sensors";
	public static final String STREAM_ADDRESS = "streamerAddress";
	
	// Default device type
	public static final String DEFAULT_DEVICE_TYPE = "scalar";
    
	private static final String DATABASE_NAME = "DeviceData";
	private static final int DATABASE_VERSION = 1;
	
	//Database Creation Strings
	private static final String TABLE_DEVICES = "create table Device ("
		+ "device_id integer primary key,"
		+ "device_name text,"
		+ "filename text,"					//filename for a video stream, should it exist
		+ "location text,"
		+ "depth text,"
		+ "latitude text,"
		+ "longitude text,"
		+ "status text,"					//status, should that come to exist (is null currently in webservice)
		+ "is_favourite boolean,"
		+ "streamerAddress text);";			//the address for the video stream, sans filename
	private static final String TABLE_SENSORS = "create table Sensor ("
		+ "sensor_id integer primary key,"
		+ "device_id integer references Device(device_id),"
		+ "sensor_name text,"
		+ "sample_time text,"
		+ "value text," 
		+ "sensor_units text);";
	private static final String TABLE_LOCATIONS = "create table Location ("
		+ "device_id integer,"
		+ "location text,"
		+ "latitude text,"
		+ "longitude text,"
		+ "depth text,"
		+ "date_from text);";
	
	private final Context mContext;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	/**
	 * Database Helper class
	 * Manages creation and versions/upgrades
	 * see http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_DEVICES);
			db.execSQL(TABLE_SENSORS);
			db.execSQL(TABLE_LOCATIONS);
		}

		
		//TODO: for an upgrade, do not want to loose favourites. Make more specific than 'drop table'? Or put in the option?
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS Device");
            db.execSQL("DROP TABLE IF EXISTS Sensor");
            db.execSQL("DROP TABLE IF EXISTS Location");
            onCreate(db);
		}		
	}//END DataBaseHelper
	
	
	
	/**
	 * Constructor of DbManager.
	 * 
	 * @param context
	 */
	public DbManager(Context context){
		this.mContext = context;
	}
	
	/**
	 * Open (or create) database.
	 * 
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
	}
	
	/**
	 * Close DB and release resources
	 */
	public void close(){
		mDbHelper.close();
	}
	
	/**
	 * Get the device details given a Device ID
	 * 
	 * @param device_id
	 * @return Cursor of device details of a given Device ID.
	 */
	public Cursor fetchDeviceDetails(long device_id){
		return mDb.query("Device", new String[]{LOCATION,
												DEPTH,
												LAT,
												LONG,
												STATUS,
												STREAM_ADDRESS},
						KEY_DEVICE_ID + "=" + device_id,
						null, null, null, null);
	}
	
	/**
	 * Get all sensors + sensor data given a Device ID. Returns as a cursor.
	 * 
	 * @param device_id
	 * @return Cursor of all the sensors of a given Device ID.
	 */
	public Cursor fetchAllSensorData(long device_id){
		return mDb.query("Sensor", new String[]{KEY_SENSOR_ID,
												SENSOR_NAME,
												VALUE,
												UNITS,
												TIME},
						KEY_DEVICE_ID + "=" + device_id,
						null,null,null,KEY_SENSOR_ID);
	}
	
	/**
	 * Fetch time for the sensors given a device
	 * @param device_id
	 * 
	 * @return Cursor with the time of sensors of a given Device ID
	 */
	public Cursor fetchSensorTime(long device_id){
		return mDb.query("Sensor", new String[]{TIME},
				KEY_DEVICE_ID+"="+Long.toString(device_id),
				null,null,null,KEY_SENSOR_ID);
		}
	
	
	/**
	 * Fetch a list of all 'active' locations from the Devices table
	 * @return Cursor of all distinct locations from Device table.
	 */
	public Cursor fetchHeaderLocations(){
		return mDb.query(true, "Device", new String[]{LOCATION},
				null,null,null,null, null, null);
	}
	
	
	/**
	 * Get a cursor over the list of all devices for a given location
	 * @param location
	 * @return Cursor of all devices of a given location.
	 */
	public Cursor fetchDevicesAtLocation(String location){
		return mDb.query("Device", new String[]{KEY_DEVICE_ID,
												DEVICE_NAME,
												IS_FAV,
												FILENAME}, 
				LOCATION+"='"+location+"'", null, null, null, 
				DEVICE_NAME);
	}
	
	/**
	 * Get all the locations for a given Device ID.
	 * 
	 * @param device_id
	 * @return Cursor of all location for a given Device ID.
	 */
	public Cursor fetchLocationsForDevice(long device_id){
		return mDb.query("Location", new String[]{LOCATION,
												  LAT,
												  LONG,
												  DEPTH,
												  DATEFROM},
				KEY_DEVICE_ID+"="+Long.toString(device_id),
				null, null, null, DATEFROM + " desc");
		}
	
	/**
	 * Get a cursor over the list of all devices sorted by device name
	 * 
	 * @return Cursor of the list of all devices sorted by device name
	 */
	public Cursor fetchAllDevices(){
		return mDb.query("Device", new String[]{KEY_DEVICE_ID,
												DEVICE_NAME,
												IS_FAV,
												FILENAME}, 
				null, null, null, null, DEVICE_NAME);	
	}
	
	/**
	 * Get a cursor over the list of all Favourite devices for a given location
	 * @param location
	 * @return Cursor of the list of all Favourite devices for a given location
	 */
	public Cursor fetchFavDevicesAtLocation(String location){
		return mDb.query("Device", new String[]{KEY_DEVICE_ID,
												DEVICE_NAME,
												IS_FAV,
												FILENAME}, 
				IS_FAV + "=1 " + "AND "+LOCATION+"='"+location+"'",
				null, null, null, DEVICE_NAME);	
	}
	
	/**
	 * Check if a device is in favourite or not.
	 * @param device_id
	 * @return true if it is a favourite, or false otherwise
	 */	
	public boolean checkFavourite(long device_id){
		Cursor c = mDb.query("Device", new String[]{IS_FAV},
								KEY_DEVICE_ID + "=" + device_id,
								null, null, null, null);
		c.moveToFirst();
		String result = c.getString(0);
		c.close();
		if (result.equals("1")){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Set a device as a favourite
	 * @param device_id
	 * @return true if success, return false if failed
	 */
	 public boolean addFavourite(long device_id){
		 ContentValues args = new ContentValues();
		 args.put(IS_FAV, "1");
		 return mDb.update("Device", args, KEY_DEVICE_ID + "=" + device_id, null) > 0;
	}

	 
	/**
	 * Remove a device as a favourite
	 * @param device_id
	 * @return true if success, return false if failed
	 */
	public boolean removeFavourite(long device_id){
		ContentValues args = new ContentValues();
		args.put(IS_FAV, "0");
		return mDb.update("Device", args, KEY_DEVICE_ID  + "=" + device_id, null) > 0;
	}
	
	/**
	 * Add the device, sensor and location data into database as returned by the web service in a JSON object
	 * 
     * The example of the expected JSON format returned from the web service: 
	 * {
	 *  "devices": [
	 *     {
	 *        "deviceId": 11102,
	 *        "deviceName": "WET Labs Fluorometer (analog)",
	 *        "filename": null,
	 *        "sensors": [
	 *          {
	 *              "correctedValue": "0.118",
	 *              "sampleTime": "2011-02-03T17:48:25.533Z",
	 *              "sensorId": 121,
	 *              "sensorName": "Turbidity",
	 *              "unitOfMeasure": "NTU"
	 *          },
	 *          {
	 *              "correctedValue": "0.087",
	 *              "sampleTime": "2011-02-03T17:48:25.533Z",
	 *              "sensorId": 120,
	 *              "sensorName": "Chlorophyll",
	 *              "unitOfMeasure": "ug/l"
	 *          }
	 *      ],
	 *      "sites": [
	 *          {
	 *              "dateFrom": "2007-01-01T00:00:00.000Z",
	 *              "depth": -20.0,
	 *              "latitude": 48.6492194,
	 *              "location": "NEPTUNE offices",
	 *              "longitude": -123.4462056
	 *          },
	 *          {
	 *              "dateFrom": "2009-09-10T18:45:24.000Z",
	 *              "depth": 899.5,
	 *              "latitude": 48.314925,
	 *              "location": "Barkley Canyon",
	 *              "longitude": -126.058022
	 *          },
	 *          {
	 *              "dateFrom": "2010-05-10T21:58:13.000Z",
	 *              "depth": 0.0,
	 *              "latitude": 0.0,
	 *              "location": "Port Alberni",
	 *              "longitude": 0.0
	 *          },
	 *          {
	 *              "dateFrom": "2010-05-16T07:34:48.000Z",
	 *              "depth": 896.3,
	 *              "latitude": 48.314912,
	 *              "location": "Barkley Canyon",
	 *              "longitude": -126.058008
	 *          }
	 *      ],
	 *      "status": null,
	 *      "streamerAddress": null
	 *  },
	 *  {
	 *      "deviceId": 12116,
	 *      "deviceName": "WET Labs CDOM Fluorometer",
	 *      "filename": null,
	 *      "sensors": [
	 *          {
	 *              "correctedValue": "7.2036",
	 *              "sampleTime": "2010-09-15T19:35:58.894Z",
	 *              "sensorId": 4266,
	 *              "sensorName": "CDOM",
	 *              "unitOfMeasure": "ppb QSDE"
	 *          }
	 *      ],
	 *      "sites": [
	 *          {
	 *              "dateFrom": "2007-01-01T00:00:00.000Z",
	 *              "depth": -20.0,
	 *              "latitude": 48.6492194,
	 *              "location": "NEPTUNE offices",
	 *              "longitude": -123.4462056
	 *          },
	 *          {
	 *              "dateFrom": "2009-08-25T09:30:00.000Z",
	 *              "depth": 396.0,
	 *              "latitude": 48.4272533333,
	 *              "location": "Barkley Canyon",
	 *              "longitude": -126.174095
	 *          },
	 *          {
	 *              "dateFrom": "2010-05-14T23:03:57.000Z",
	 *              "depth": -20.0,
	 *              "latitude": 48.6492194,
	 *              "location": "NEPTUNE offices",
	 *              "longitude": -123.4462056
	 *          },
	 *          {
	 *              "dateFrom": "2010-09-13T20:09:00.000Z",
	 *              "depth": 396.0,
	 *              "latitude": 48.4273866667,
	 *              "location": "Barkley Canyon",
	 *              "longitude": -126.1741316667
	 *          },
	 *          {
	 *              "dateFrom": "2010-10-05T17:51:14.000Z",
	 *              "depth": -20.0,
	 *              "latitude": 48.6492194,
	 *              "location": "NEPTUNE offices",
	 *              "longitude": -123.4462056
	 *          }
	 *      ],
	 *      "status": null,
	 *      "streamerAddress": null
	 *    }
	 *   ]
	 * }
	 * 
	 * @param jObject
	 */
	public void insertFromJSONObject(JSONObject jObject){
		
		JSONObject j;
		JSONObject s;
		String updated_time;
		JSONArray array;
		String last_reading;			// the actual double value from inside the json object...
		String reading;					// and the truncated reading put in the DB
		String long_lat;
		String long_longit;
		String lat;
		String longit;
		Cursor locations = null;
		
		Timestamp time = null;	

		// Round up to the decimal points that suits the display in UI.
		NumberFormat nf = NumberFormat.getInstance();		
		nf.setMaximumFractionDigits(this.mContext.getResources().getInteger(R.integer.sensor_decimal));
		
		NumberFormat nf_latlong = NumberFormat.getInstance();
		nf_latlong.setMaximumFractionDigits(this.mContext.getResources().getInteger(R.integer.latlong_decimal));
		
		try{
			JSONArray jArray = jObject.getJSONArray("devices");
			for(int i=0; i<jArray.length(); i++){
				j=jArray.getJSONObject(i);
							
				// Save all the location history for each device
				array = j.getJSONArray("sites");
				for(int k=0; k<array.length(); k++){
					s = array.getJSONObject(k);
					
					updated_time=s.getString("dateFrom");
					if(!updated_time.equals("")){
						time = timeConversion(mContext, updated_time);
					}
					
					long_lat = s.getString("latitude");
					long_longit = s.getString("longitude");
					lat ="";
					longit="";
					if(!long_lat.equals("") && !long_longit.equals("")){
						lat = nf_latlong.format(Double.parseDouble(long_lat));
						longit = nf_latlong.format(Double.parseDouble(long_longit));
					}
					
					addLocation(j.getString("deviceId"), s.getString("location"),lat, longit, s.getString("depth"), time);
				}
				
				// Save the device data with the location set to its latest location.
				try{
					locations = fetchLocationsForDevice(Long.valueOf(j.getString("deviceId")));
					if(locations.moveToFirst()) {
						addDevice(j.getString("deviceId"),j.getString("deviceName"),j.getString("filename"),
								locations.getString(locations.getColumnIndex(LOCATION)),
								locations.getString(locations.getColumnIndex(DEPTH)),
								locations.getString((locations.getColumnIndex(LAT))),	
								locations.getString(locations.getColumnIndex(LONG)),						
								j.getString("status"), j.getString("streamerAddress"));
					}
				} finally {
					locations.close();
				}
				
				// Save sensors for each device
				array = j.getJSONArray("sensors");	
				for(int l=0; l<array.length(); l++){
					s=array.getJSONObject(l);
					
					last_reading = s.getString("correctedValue");
					reading = "";
					if(!last_reading.equals("")){
						reading = nf.format(Double.parseDouble(last_reading));
					}
					
					updated_time=s.getString("sampleTime");
					if(!updated_time.equals("")){
						time = timeConversion(mContext, updated_time);
					}
					addSensor(j.getString("deviceId"),s.getString("sensorId"),s.getString("sensorName"),
						reading, s.getString("unitOfMeasure"), time);
				}
			
			}
		} catch(Exception e) {
			Log.e(TAG, "Error extracting JSON: " + e);
			e.printStackTrace();
		}	
	}
	
	/**
	 * Add a sensor into the database.
	 * 
	 * If the sensor has already existed, the sensor data will be updated instead.
	 * 
	 * @param device_id
	 * @param sensor_id
	 * @param sensor_name
	 * @param value
	 * @param units
	 * @param time
	 */
	private void addSensor(String device_id, String sensor_id, String sensor_name, String value, String units, Timestamp time){
		Cursor c = mDb.query("Sensor", new String[]{
				KEY_SENSOR_ID},
				KEY_SENSOR_ID +"="+sensor_id,
				null,null,null,null);
		
		if(c.getCount() == 0){
			mDb.execSQL("INSERT INTO Sensor ("
					+KEY_SENSOR_ID+","
					+KEY_DEVICE_ID+","
					+SENSOR_NAME+","
					+VALUE+","
					+UNITS+","
					+TIME+")"
					+" VALUES ('"+sensor_id+"','"+device_id+"','"+sensor_name+"','"+value+"','"+units+"', datetime('"+time+"'));");
		}else{
			mDb.execSQL("UPDATE Sensor SET sensor_name='"+sensor_name+"', device_id='"+device_id
					+"', value='"+value +"', sensor_units='"+units+"', sample_time=datetime('"+time+"')"
					+" WHERE sensor_id="+sensor_id+";");			
		}
		c.close();
	}
	
	

	/**
	 * Add a device into the database.
	 * 
	 * If the device has already existed, the device data will be updated instead.
	 * 
	 * @param id
	 * @param name
	 * @param type
	 * @param location
	 * @param depth
	 * @param lat
	 * @param longit
	 * @param stat
	 * @param stream
	 */
	private void addDevice(String id, String name, String type, String location, String depth, String lat, String longit, String stat, String stream){
		Cursor c = mDb.query("Device", new String[]{KEY_DEVICE_ID},
								KEY_DEVICE_ID + "=" + id,
								null,null,null,null);
		
		//Devices that do not have cameras will have null values in these fields
		if(type.equals("null")|| stream.equals("null")){
			type = DbManager.DEFAULT_DEVICE_TYPE;
			stream = "none";
		}
				
		if(c.getCount() == 0){
			mDb.execSQL("INSERT INTO Device ("
					+ KEY_DEVICE_ID+","
					+ DEVICE_NAME+","
					+ FILENAME+","
					+ LOCATION+","
					+ DEPTH+","
					+ LAT+","
					+ LONG+","
					+ STATUS+","
					+ IS_FAV+","
					+ STREAM_ADDRESS+")"
					+ " VALUES('"+id+"','"+name+"','"+type+"','"+location+"','"+depth+"','"+lat+"','"
					+longit+"','"+stat+"','0','"+stream+"');");		//Favourites are by default set to '0' (not favourited)
		}else{
			mDb.execSQL("UPDATE Device SET device_name='"+name
					+"', filename='"+type+"', location='"+location
					+"', depth='"+depth+"', latitude='"+lat+"', longitude='"+longit+"', status='"+stat
					+"', streamerAddress='"+stream+"' WHERE device_id='"+id+"';");
		}
		c.close();	
	}
	
	
	/**
	 * Add a location to the Location table if the location has not existed.
	 * 
	 * @param id
	 * @param location
	 * @param lat
	 * @param longit
	 * @param depth
	 * @param time
	 */
	private void addLocation(String id, String location, String lat, String longit, String depth, Timestamp time){
		Cursor c = mDb.query("Location", new String[]{KEY_DEVICE_ID},
			KEY_DEVICE_ID + "='" + id +"' AND " +
			LOCATION + "='" + location +"' AND " +
			LAT + "='" + lat +"' AND " +
			LONG + "='" + longit +"' AND " +
			DEPTH + "='" + depth +"' AND " +
			DATEFROM + "=datetime('"+time+"')",
			 null, null, null, null);

		if(c.getCount() == 0){
			mDb.execSQL("INSERT INTO Location ("
					+ KEY_DEVICE_ID+","
					+ LOCATION+","
					+ LAT+","
					+ LONG+","
					+ DEPTH+","
					+ DATEFROM+")"
					+ " VALUES('"+id+"','"+location+"','"+lat+"','"+longit+"','"
					+depth+"', datetime('"+time+"'));");
		}
		
		
		c.close();
	}
		
	/**
	 * Helper method to convert the timestamp in string with ISO8601 extended format 
	 * ("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'") to a Timestamp object.
	 * 
	 * @param context
	 * @param s_time Timestamp in string with ISO8601 extended format ("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'").
	 * @return Timestamp object of a given time in String.
	 */
	private Timestamp timeConversion(Context context, String s_time){
		SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.format_iso8601_timestamp));
		try {
			Date d = sdf.parse(s_time);
			java.sql.Timestamp ts = new Timestamp(d.getTime());			
			return ts;
			
		} catch (ParseException e) {
			Log.e(TAG, "Problem converting date to be stored in DB");
			e.printStackTrace();
			return null;
		}
	}
	
	
	
}
