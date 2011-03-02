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
 
 		IMPORTANT: READ THIS FILE BEFORE CHANGING THE CODES
 
 0. Table of Contents
 ===============================================================================
 
 0. Table of Contents
 1. Introduction
 2. Minimum requirements
 3. Development Guide
 
 1. Introduction
 ===============================================================================
 
 NEPTUNE Canada ocean network is part of the Ocean Networks Canada (ONC) 
 Observatory. Our network extends the Internet from the rocky coast to the deep 
 abyss. We gather live data and video from instruments on the seafloor, making 
 them freely available to the world, 24/7.
 
 NEPTUNE mobile application allows convenient access to the data of NEPTUNE 
 Canada. The features provided:
 
   1. View sensor data of instruments
   2. View graph of sensor data
   3. NEPTUNE Canada video streaming
 
 2. Minimum requirements
 ===============================================================================
 
 The minimum API requirements for NEPTUNE is Android 1.6 API Level 4 and later.
 
 The minimum hard disk space required for NEPTUNE is 1.5MB.
 
 3. Development Guide
 ===============================================================================
 
 For general Android development guide, please refer to Android official website:
 
 	http://developer.android.com/guide/index.html
 
 In this Readme, we will focus more about how to change the Web Service calls in 
 NEPTUNE mobile application.
 
 All web services that are being called are saved in "services.xml" resource file:
 
 	<NEPTUNE Project>/res/values/services.xml
 
 The services that NEPTUNE is calling are:
 	i) DMAS Last Reading service
 	   -------------------------
 	   Resource ID : last_reading_service
 	   Service Call: http://dmas.uvic.ca/LastReading
 	   Parameter	: N/A 	
 	   Return		: Result of this service call is in JSON format.
 	   
 	   Example of the result: 	   
 	   {
	    "devices": [
	        {
	            "deviceId": 11102, // required
	            "deviceName": "WET Labs Fluorometer (analog)",
	            "filename": null,
	            "sensors": [
	                {
	                    "correctedValue": "0.118",
	                    "sampleTime": "2011-02-03T17:48:25.533Z",
	                    "sensorId": 121,
	                    "sensorName": "Turbidity",
	                    "unitOfMeasure": "NTU"
	                },
	                {
	                    "correctedValue": "0.087",
	                    "sampleTime": "2011-02-03T17:48:25.533Z",
	                    "sensorId": 120,
	                    "sensorName": "Chlorophyll",
	                    "unitOfMeasure": "ug/l"
	                }
	            ],
	            "sites": [
	                {
	                    "dateFrom": "2007-01-01T00:00:00.000Z",
	                    "depth": -20.0,
	                    "latitude": 48.6492194,
	                    "location": "NEPTUNE offices",
	                    "longitude": -123.4462056
	                },
	                {
	                    "dateFrom": "2009-09-10T18:45:24.000Z",
	                    "depth": 899.5,
	                    "latitude": 48.314925,
	                    "location": "Barkley Canyon",
	                    "longitude": -126.058022
	                },
	                {
	                    "dateFrom": "2010-05-10T21:58:13.000Z",
	                    "depth": 0.0,
	                    "latitude": 0.0,
	                    "location": "Port Alberni",
	                    "longitude": 0.0
	                },
	                {
	                    "dateFrom": "2010-05-16T07:34:48.000Z",
	                    "depth": 896.3,
	                    "latitude": 48.314912,
	                    "location": "Barkley Canyon",
	                    "longitude": -126.058008
	                }
	            ],
	            "status": null,
	            "streamerAddress": null
	        },
	        {
	            "deviceId": 12116,
	            "deviceName": "WET Labs CDOM Fluorometer",
	            "filename": null,
	            "sensors": [
	                {
	                    "correctedValue": "7.2036",
	                    "sampleTime": "2010-09-15T19:35:58.894Z",
	                    "sensorId": 4266,
	                    "sensorName": "CDOM",
	                    "unitOfMeasure": "ppb QSDE"
	                }
	            ],
	            "sites": [
	                {
	                    "dateFrom": "2007-01-01T00:00:00.000Z",
	                    "depth": -20.0,
	                    "latitude": 48.6492194,
	                    "location": "NEPTUNE offices",
	                    "longitude": -123.4462056
	                },
	                {
	                    "dateFrom": "2009-08-25T09:30:00.000Z",
	                    "depth": 396.0,
	                    "latitude": 48.4272533333,
	                    "location": "Barkley Canyon",
	                    "longitude": -126.174095
	                },
	                {
	                    "dateFrom": "2010-05-14T23:03:57.000Z",
	                    "depth": -20.0,
	                    "latitude": 48.6492194,
	                    "location": "NEPTUNE offices",
	                    "longitude": -123.4462056
	                },
	                {
	                    "dateFrom": "2010-09-13T20:09:00.000Z",
	                    "depth": 396.0,
	                    "latitude": 48.4273866667,
	                    "location": "Barkley Canyon",
	                    "longitude": -126.1741316667
	                },
	                {
	                    "dateFrom": "2010-10-05T17:51:14.000Z",
	                    "depth": -20.0,
	                    "latitude": 48.6492194,
	                    "location": "NEPTUNE offices",
	                    "longitude": -123.4462056
	                }
	            ],
	            "status": null,
	            "streamerAddress": null
		        }
		    ]
		}
		
		All the fields are required in order to build the SQLite database in NEPTUNE application.
	
	ii) DMAS plotting service
	    ---------------------
		Resource ID	: plot_service
		Service Call: http://dmas.uvic.ca/Plot?sensorId=%1$s&plotwidth=%2$d&plotheight=%3$d&plottype=on&plottitle=on&datefrom=%4$tY-%4$tm-%4$td%%20%4$tH:%4$tM:%4$tS&dateto=%5$tY-%5$tm-%5$td%%20%5$tH:%5$tM:%5$tS
		Parameters	: 1) sensorId   = Sensor ID
					  2) plotwidth  = Width of the screen size
					  3) plotheight = Height of the screen size
					  4) datefrom   = Date and time (yyyy-mm-dd hh:mm:ss) the graph to plot from
					  5) dateto     = Date and time (yyyy-mm-dd hh:mm:ss) the graph to plot until
		Return		: A graph image in PNG format with the width and height defined in the request.
		
	iii) YouTube API
	     -----------
		 Resource ID : youtube_service
		 Service Call: http://gdata.youtube.com/feeds/mobile/videos/-/mobile?&author=neptunecanada&format=6&alt=json&orderby=published
		 Parameters	 : As defined in the service call.
		 Return		 : List of video stream information in JSON format. 
		 			   Please refer to the follow URLs for more information of the return format:
		 	
		 	http://code.google.com/apis/youtube/2.0/developers_guide_protocol.html#Understanding_Video_Entries
		 	http://code.google.com/apis/gdata/docs/json.html

 ===============================================================================