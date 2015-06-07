var express = require('express'),
    bodyparser = require('body-parser'),
    ibmbluemix = require('ibmbluemix'),
    ibmpush = require('ibmpush');
var url = require('url');
var request = require('request');
var twitter = require("twit");
//configuration for application
var appConfig = {
    applicationId: "22f0e136-38a1-468b-8e73-c1cde5002586",
    applicationRoute: "http://oneDayHack.mybluemix.net"
};
var tweeter = new twitter({
    consumer_key: 'D4LAZ3lSPWtoq90SLEZhijjsL',
    consumer_secret: 'd3EYIcS4Ky19jhzHcIzjFS1yj7K0YrT9j6ARMISDVDWyH624KX',
    access_token: '3163481023-I7vSWE50tjxgVSs8C9g8e1xQDAJsG2m7It52E9d',
    access_token_secret: 'cppgB8vvv3hWxYVRoW4shA0g9pxqV75ffAkSJYs2LKPlm'
});
// create an express app
var app = express();
app.use(bodyparser.json());
app.use(bodyparser.urlencoded({
  extended: true
}));
var lastlat;
var lastlong;
var count = 0;
//uncomment below code to protect endpoints created afterwards by MAS
//var mas = require('ibmsecurity')();
//app.use(mas);

var headers = {
    'User-Agent':       'Super Agent/0.0.1',
    'Content-Type':     'application/x-www-form-urlencoded'
}
//initialize mbaas-config module
ibmbluemix.initialize(appConfig);
var logger = ibmbluemix.getLogger();

app.use(function(req, res, next) {
	req.ibmpush = ibmpush.initializeService(req);
	req.logger = logger;
	next();
});

//initialize ibmconfig module
var ibmconfig = ibmbluemix.getConfig();

//get context root to deploy your application
//the context root is '${appHostName}/v1/apps/${applicationId}'
var contextRoot = ibmconfig.getContextRoot();
appContext=express.Router();
app.use(contextRoot, appContext);

console.log("contextRoot: " + contextRoot);

// log all requests
app.all('*', function(req, res, next) {
	console.log("Received request to " + req.url);
	next();
});

// create resource URIs
// endpoint: https://mobile.ng.bluemix.net/${appHostName}/v1/apps/${applicationId}/notifyOtherDevices/
appContext.post('/notifyOtherDevices', function(req,res) {
	var results = 'Sent notification to all registered devices successfully.';

	console.log("Trying to send push notification via JavaScript Push SDK");
	var message = { "alert" : "The BlueList has been updated.",
					"url": "http://www.google.com"
	};

	req.ibmpush.sendBroadcastNotification(message,null).then(function (response) {
		console.log("Notification sent successfully to all devices.", response);
		res.send("Sent notification to all registered devices.");
	}, function(err) {
		console.log("Failed to send notification to all devices.");
		console.log(err);
		res.send(400, {reason: "An error occurred while sending the Push notification.", error: err});
	});
});
appContext.get('/sendLocation',function(req,res){
	var url_parts = url.parse(req.url, true);
	var client_id = url_parts.query.id;
	var lat = url_parts.query.lat;
	var long = url_parts.query.long;
	var gotlat = parseFloat(lat);
	var gotlong = parseFloat(long);
	if(lastlat == null && lastlong == null)
	{
		lastlat = gotlat;
		lastlong = gotlong;
	}
	else
	{
		if(Math.abs(lastlat - gotlat)< 0.001 && Math.abs(lastlat-gotlat) < 0.001)
		{
			count++;
			
		}
		else
		{
			count = 0;
			//lastlat = null;
			//lastlong = null;
		}
		lastlat = gotlat;
		lastlong = gotlong;
	}
	if(count >= 3)
	{
		//res.send("Need to trigger places API");
		console.log(gotlat);
		count = 0;
		/*var path = 'location='+gotlat.toFixed(10)+','+gotlong.toFixed(10)+'&radius=100&types=food&key=AIzaSyC1uJPc4SgSpKuMsWtxTriphrWULC7MJwQ';
		console.log(path);
		var reses = [];
		//request({url:'https://maps.googleapis.com/maps/api/place/nearbysearch/json',method:'GET',path:'?location='+gotlat+','+gotlong+'&radius=100&types=food&key=AIzaSyC1uJPc4SgSpKuMsWtxTriphrWULC7MJwQ'}, function (error, response, body) {
		//request({url:'https://maps.googleapis.com/maps/api/place/nearbysearch/json?'+path,method:'GET'/*,path:'?location='+gotlat+','+gotlong+'&radius=100&types=food&key=AIzaSyC1uJPc4SgSpKuMsWtxTriphrWULC7MJwQ'}, function (error, response, body) {
		//	if (!error && response.statusCode == 200) {
        // Print out the response body
				//console.log(body);
				//jobId = JSON.parse(body)["jobID"];
				//console.log(jobId);
				//res.json(JSON.parse(body));
				
		/*		var places = JSON.parse(body)["results"];
				for(var i =0;i<places.length;i++)
				{
					var name = places[i].name;
					var types = places[i].types;
					var url = places[i].icon;
					reses.push({pName:name,pType:types,pIcon:url});
				}
				console.log(reses);
				//return res.json(reses);
			}
		});
		/*request({url:'https://mobile.ng.bluemix.net/oneDayHack/v1/apps/22f0e136-38a1-468b-8e73-c1cde5002586/notifyOtherDevices',method:'POST'},function (error, response, body){
		
		});*/
		var results = 'Sent notification to all registered devices successfully.';
		//var places = {"place":reses}
		console.log("Trying to send push notification via JavaScript Push SDK");
		var message = { "alert" : "We see you are enjoying somewhere. Do you want to Tweet?",
						"url": "http://www.google.com"
		};

		req.ibmpush.sendBroadcastNotification(message,null).then(function (response) {
			console.log("Notification sent successfully to all devices.", response);
			res.send("Sent notification to all registered devices.");
		}, function(err) {
			console.log("Failed to send notification to all devices.");
			console.log(err);
			res.send(400, {reason: "An error occurred while sending the Push notification.", error: err});
		});
		//return res.json(reses);
		//next('route');
		//triggerResult();
	}
	else{
	return res.send("OK");
	}
});

var triggerResult = function(err,req,res){
	res.send('triggering');
}
appContext.get('/getPlaces',function(req,res){
		var path = 'location='+lastlat.toFixed(10)+','+lastlong.toFixed(10)+'&radius=100&types=food&key=AIzaSyC1uJPc4SgSpKuMsWtxTriphrWULC7MJwQ';
		console.log(path);
		var reses = [];
		//request({url:'https://maps.googleapis.com/maps/api/place/nearbysearch/json',method:'GET',path:'?location='+gotlat+','+gotlong+'&radius=100&types=food&key=AIzaSyC1uJPc4SgSpKuMsWtxTriphrWULC7MJwQ'}, function (error, response, body) {
		request({url:'https://maps.googleapis.com/maps/api/place/nearbysearch/json?'+path,method:'GET'/*,path:'?location='+gotlat+','+gotlong+'&radius=100&types=food&key=AIzaSyC1uJPc4SgSpKuMsWtxTriphrWULC7MJwQ'*/}, function (error, response, body) {
			if (!error && response.statusCode == 200) {
        // Print out the response body
				//console.log(body);
				//jobId = JSON.parse(body)["jobID"];
				//console.log(jobId);
				//res.json(JSON.parse(body));
				
				var places = JSON.parse(body)["results"];
				for(var i =0;i<places.length;i++)
				{
					var name = places[i].name;
					var types = places[i].types;
					var url = places[i].icon;
					reses.push({pName:name,pType:types,pIcon:url});
				}
				console.log(reses);
				return res.json(reses);
			}
		});
	});
	
appContext.get('/sendTweet',function(req,res){
	var incoming = decodeURI(req.url);
	var url_parts = url.parse(incoming, true);
	var tweet = url_parts.query.tweet;
	console.log(tweet);
	tweeter.post('statuses/update', {status:tweet} , function(err, data) {
	/*for (var i = 0; i < data.length ; i++) {
			console.log(data[i].text);
			tweets = tweets + "\n" + data[i].text;
			
		}*/
		if(err)
		{
			res.send(err);
		}
		else
		{
			res.send('OK');
		}
	})
});
/*appContext.get('/triggeredResult',function(req,res){
	res.send('triggering');
});
*/
// host static files in public folder
// endpoint:  https://mobile.ng.bluemix.net/${appHostName}/v1/apps/${applicationId}/static/
appContext.use('/static', express.static('public'));

//redirect to cloudcode doc page when accessing the root context
app.get('/', function(req, res){
	res.sendfile('public/index.html');
});

app.listen(ibmconfig.getPort());
console.log('Server started at port: '+ibmconfig.getPort());
