# How to Change the Web Service #
In this Wiki, we will focus more about how to change the Web Service calls in NEPTUNE mobile application.

All web services that are being called are saved in "services.xml" resource file:

<p><i><NEPTUNEApp Project>/res/values/services.xml</i>

<p>The services that NEPTUNE is calling are:<br>
<p>
<ol><li>DMAS Last Reading service<br>
<ul><li><b>Resource ID</b> : last_reading_service<br>
</li><li><b>Service Call</b> : <a href='http://dmas.uvic.ca/LastReading'>http://dmas.uvic.ca/LastReading</a>
</li><li><b>Parameter</b> : N/A<br>
</li><li><b>Return</b> : Result of this service call is in JSON format.<br />Example of the result:<br />
</li></ul><blockquote><pre><code><br>
{<br>
"devices": [<br>
{<br>
"deviceId": 11102, // required<br>
"deviceName": "WET Labs Fluorometer (analog)",<br>
"filename": null,<br>
"sensors": [<br>
{<br>
"correctedValue": "0.118",<br>
"sampleTime": "2011-02-03T17:48:25.533Z",<br>
"sensorId": 121,<br>
"sensorName": "Turbidity",<br>
"unitOfMeasure": "NTU"<br>
},<br>
{<br>
"correctedValue": "0.087",<br>
"sampleTime": "2011-02-03T17:48:25.533Z",<br>
"sensorId": 120,<br>
"sensorName": "Chlorophyll",<br>
"unitOfMeasure": "ug/l"<br>
}<br>
],<br>
"sites": [<br>
{<br>
"dateFrom": "2007-01-01T00:00:00.000Z",<br>
"depth": -20.0,<br>
"latitude": 48.6492194,<br>
"location": "NEPTUNE offices",<br>
"longitude": -123.4462056<br>
},<br>
{<br>
"dateFrom": "2009-09-10T18:45:24.000Z",<br>
"depth": 899.5,<br>
"latitude": 48.314925,<br>
"location": "Barkley Canyon",<br>
"longitude": -126.058022<br>
},<br>
{<br>
"dateFrom": "2010-05-10T21:58:13.000Z",<br>
"depth": 0.0,<br>
"latitude": 0.0,<br>
"location": "Port Alberni",<br>
"longitude": 0.0<br>
},<br>
{<br>
"dateFrom": "2010-05-16T07:34:48.000Z",<br>
"depth": 896.3,<br>
"latitude": 48.314912,<br>
"location": "Barkley Canyon",<br>
"longitude": -126.058008<br>
}<br>
],<br>
"status": null,<br>
"streamerAddress": null<br>
},<br>
{<br>
"deviceId": 12116,<br>
"deviceName": "WET Labs CDOM Fluorometer",<br>
"filename": null,<br>
"sensors": [<br>
{<br>
"correctedValue": "7.2036",<br>
"sampleTime": "2010-09-15T19:35:58.894Z",<br>
"sensorId": 4266,<br>
"sensorName": "CDOM",<br>
"unitOfMeasure": "ppb QSDE"<br>
}<br>
],<br>
"sites": [<br>
{<br>
"dateFrom": "2007-01-01T00:00:00.000Z",<br>
"depth": -20.0,<br>
"latitude": 48.6492194,<br>
"location": "NEPTUNE offices",<br>
"longitude": -123.4462056<br>
},<br>
{<br>
"dateFrom": "2009-08-25T09:30:00.000Z",<br>
"depth": 396.0,<br>
"latitude": 48.4272533333,<br>
"location": "Barkley Canyon",<br>
"longitude": -126.174095<br>
},<br>
{<br>
"dateFrom": "2010-05-14T23:03:57.000Z",<br>
"depth": -20.0,<br>
"latitude": 48.6492194,<br>
"location": "NEPTUNE offices",<br>
"longitude": -123.4462056<br>
},<br>
{<br>
"dateFrom": "2010-09-13T20:09:00.000Z",<br>
"depth": 396.0,<br>
"latitude": 48.4273866667,<br>
"location": "Barkley Canyon",<br>
"longitude": -126.1741316667<br>
},<br>
{<br>
"dateFrom": "2010-10-05T17:51:14.000Z",<br>
"depth": -20.0,<br>
"latitude": 48.6492194,<br>
"location": "NEPTUNE offices",<br>
"longitude": -123.4462056<br>
}<br>
],<br>
"status": null,<br>
"streamerAddress": null<br>
}<br>
]<br>
}<br>
</code></pre><br />All the fields are required in order to build the SQLite database in NEPTUNE application.<br />
</blockquote></li><li>DMAS plotting service<br>
<ul><li><b>Resource ID</b> : plot_service<br>
</li><li><b>Service Call</b> : <a href='http://dmas.uvic.ca/Plot?sensorId=%1$s&plotwidth=%2$d&plotheight=%3$d&plottype=on&plottitle=on&datefrom=%4$tY-%4$tm-%4$td%%20%4$tH:%4$tM:%4$tS&dateto=%5$tY-%5$tm-%5$td%%20%5$tH:%5$tM:%5$tS'>http://dmas.uvic.ca/Plot?sensorId=%1$s&amp;plotwidth=%2$d&amp;plotheight=%3$d&amp;plottype=on&amp;plottitle=on&amp;datefrom=%4$tY-%4$tm-%4$td%%20%4$tH:%4$tM:%4$tS&amp;dateto=%5$tY-%5$tm-%5$td%%20%5$tH:%5$tM:%5$tS</a>
</li><li><b>Parameters</b> :<br>
<ul><li>sensorId = Sensor ID<br>
</li><li>plotwidth = Width of the screen size<br>
</li><li>plotheight = Height of the screen size<br>
</li><li>datefrom = Date and time (yyyy-mm-dd hh:mm:ss) the graph to plot from<br>
</li><li>dateto = Date and time (yyyy-mm-dd hh:mm:ss) the graph to plot until<br>
</li></ul></li><li><b>Return</b> : A graph image in PNG format with the width and height defined in the request.<br />
</li></ul></li><li>YouTube API<br>
<ul><li><b>Resource ID</b> : youtube_service<br>
</li><li><b>Service Call</b> : <a href='http://gdata.youtube.com/feeds/mobile/videos/-/mobile?&author=neptunecanada&format=6&alt=json&orderby=published'>http://gdata.youtube.com/feeds/mobile/videos/-/mobile?&amp;author=neptunecanada&amp;format=6&amp;alt=json&amp;orderby=published</a>
</li><li><b>Parameters</b> : As defined in the service call.<br>
</li><li><b>Return</b> : List of video stream information in JSON format. Please refer to the follow URLs for more information of the return format:<br /><a href='http://code.google.com/apis/youtube/2.0/developers_guide_protocol.html#Understanding_Video_Entries'>http://code.google.com/apis/youtube/2.0/developers_guide_protocol.html#Understanding_Video_Entries</a><br /><a href='http://code.google.com/apis/gdata/docs/json.html'>http://code.google.com/apis/gdata/docs/json.html</a>