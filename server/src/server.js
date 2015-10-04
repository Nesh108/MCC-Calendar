//////////////////////
// Web Server setup
//////////////////////

var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var fs = require('fs');
var http = require('http');
var path = require('path');
var url = require('url');

var port_api = process.env.PORT || 9090;  // Port for API requests
var port_web = process.env.PORT || 8080;  // Post for web requests

var WEB_DIR = 'views';  // Folder for web pages

//////////////////////
// Configure MongoDB
//////////////////////

var config = require('./deploy/config');
var mongoose = require('mongoose');
mongoose.connect('mongodb://' + config.db.user_name + ':' + config.db.password + '@' + config.db.URI);
var Event = require('./app/models/event');
var User = require('./app/models/user');

//////////////////////
// Configure passport
//////////////////////

var passport = require('passport');
var authController = require('./controllers/auth');
app.use(passport.initialize());

//////////////////////
// Configure body parser
//////////////////////

app.use(bodyParser.urlencoded({ extended : true }));
app.use(bodyParser.json());

var router = express.Router();  // Router for RESTful api

////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
/// RESTful API
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////

//////////////////////
// Middleware
//////////////////////

router.use(function(req, res, next) {
  console.log('API request!');
  next();
});

//////////////////////
// User route
//////////////////////

router.route('/users')

  // Create a new user (POST)
  .post(function(req, res) {
    var user = new User({
      username: req.body.username,
      password: req.body.password
    });

    user.save(function(err) {
      if (err)
        res.send(err);

      res.json({ message: 'User created successfully!' });
    });
  })

  // Get all users (GET) TODO: remove this in in final product
  .get(authController.authorized, function(req, res) {
    User.find(function(err, users) {
      if (err)
        res.send(err);

      res.json(users);
    });
  });

///////////////////////////////
// Root route: Welcome message
///////////////////////////////

router.get('/', function(req, res) {
  res.json({message: 'Welcome to the MCC-Calendar API. Read the documentaion for more information.' });
});

//////////////////////
// Events route
//////////////////////

router.route('/events')

  // Create a new event (POST)
  .post(authController.authorized, function(req, res) {

    var evt = new Event();
    evt.name = req.body.name;
    evt.description = req.body.description;
    evt.location = req.body.location;
    evt.dateStart = req.body.dateStart;
    evt.dateEnd = req.body.dateEnd;
    evt.recurCount = req.body.recurCount;
    evt.periodFreq = req.body.periodFreq;
    evt.periodId = req.body.periodId;
    evt.status = req.body.status;
    evt.visibile = req.body.visibile;
    evt.owner = req.user.username;

    evt.save(function(err) {
      if(err)
        res.send(err);

      res.json({ message: 'Event created successfully!'});
    });
  })

  // Get all events of a specific user (GET)
  .get(authController.authorized, function(req, res) {
    Event.find({owner:req.user.username}, function(err, events) {
      if(err)
        res.send(err);

      res.json(events);
    });
  });

//////////////////////
// Single event route
//////////////////////

router.route('/events/:event_id')

    // Read specific event (GET)
    .get(authController.authorized, function(req, res) {
      Event.findById(req.params.event_id, function(err, evt) {
        if(err)
          res.send(err);


        res.json(evt);
      });
    })

    // Update specific event (PUT)
    .put(authController.authorized, function(req, res) {

      Event.findById(req.params.event_id, function(err, evt) {
        if(err)
          res.send(err);

        evt.name = req.body.name;
        evt.description = req.body.description;
        evt.location = req.body.location;
        evt.dateStart = req.body.dateStart;
        evt.dateEnd = req.body.dateEnd;
        evt.recurCount = req.body.recurCount;
        evt.periodFreq = req.body.periodFreq;
        evt.periodId = req.body.periodId;
        evt.status = req.body.status;
        evt.visibile = req.body.visibile;

        evt.save(function(err) {
          if(err)
            res.send(err);

          res.json({ message: 'Event ' + req.params.event_id + ' successfully updated!'});
        });

      });
    })

    // Delete specific event (DELETE)
    .delete(authController.authorized, function(req, res) {
      Event.remove({
        _id: req.params.event_id
      }, function(err, evt) {
        if(err)
          res.send(err);

        res.json({ message:  'Event ' + req.params.event_id + ' successfully deleted!'});
      });
    });

//////////////////////
// Search route
//////////////////////

// Inspired by: https://stackoverflow.com/a/18933902/1214469


router.route('/events/searches')

  .post(authController.authorized, function(req, res){

    req.body["owner"] = req.user.username;

    Event.find(req.body).exec(function(err, evts){

    // DEBUG
    console.log(req.body);

    if(err)
      res.send(err);

    res.json(evts);

    });
  });

//////////////////////
// Calendar sync route
//////////////////////

// https://en.wikipedia.org/wiki/ICalendar

router.route('/events/synchronize/all')

  .get(function(req, res){

    var ics_file = "BEGIN:VCALENDAR\nMETHOD:PUBLISH\nPRODID:-//nesh//NONSGML v1.0//EN\nVERSION:2.0\n";

    // First simple version
    Event.find({owner:req.user.username}).stream()
      .on('data', function(evt){
        ics_file += "BEGIN:VEVENT\n" +
            "UID:" + evt._id + "\n" +
            "DTSTAMP:" + new Date().toISOString() + "\n" +
            "DTSTART:" + evt.dateStart.toISOString() + "\n" +
            "SUMMARY:" + evt.name + "\n" +
            "DESCRIPTION:" + evt.description + "\n" +
            "CLASS:PUBLIC\n" +
            "END:VEVENT\n";
      })
      .on('error', function(err){
        res.send(err);
      })
      .on('end', function(){
        ics_file += "END:VCALENDAR";
        res.send(ics_file);
      });
  });

////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
/// Web Server
////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////

// Inspired by: https://stackoverflow.com/questions/6084360/using-node-js-as-a-simple-web-server

http.createServer(function(request, response) {

  var uri = url.parse(request.url).pathname;

  console.log('Requested: ' + uri);

  // Allowing requests without HTML extensions
  if(uri.indexOf('.') == -1 && uri.indexOf('/', uri.length - 1) == -1)
    uri += '.html';

  var filename = path.join(process.cwd(), WEB_DIR + uri);
  var contentTypesByExtension = {
    '.html': "text/html",
    '.css': "text/css",
    '.js': "text/javascript"
  };

  fs.exists(filename, function(exists) {
    if(!exists) {

      // Load default 404 page
      fs.readFile('views/not_found.html', "binary", function(err, file) {
        // Error while loading the error page: great job devs!
        if(err){
          response.writeHead(500, {"Content-Type": "text/plain"});
          response.write(err + "\n");
          response.end();
          return;
        }

        response.writeHead(404, {"Content-Type": "text/html"});
        response.write(file, "binary");
        response.end();
      });

      return;
    }

    if(fs.statSync(filename).isDirectory())
      filename += 'index.html';


    fs.readFile(filename, "binary", function(err, file) {
      if(err){
        response.writeHead(500, {"Content-Type": "text/plain"});
        response.write(err + "\n");
        response.end();
        return;
      }

      var headers = {};
      var contentType = contentTypesByExtension[path.extname(filename)];
      if(contentType)
        headers["Content-Type"] = contentType;
      response.writeHead(200, headers);
      response.write(file, "binary");
      response.end();
    });
  });
}).listen(parseInt(port_web, 10));

app.use('/api', router);

// Start server

app.listen(port_api);
console.log('API: Listening on port ' + port_api);
console.log('Web: Listening on port ' + port_web);
