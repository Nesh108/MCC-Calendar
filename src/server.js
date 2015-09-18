//////////////////////
// Server setup
//////////////////////

var express = require('express');
var app = express();
var bodyParser = require('body-parser');

//////////////////////
// Configure MongoDB
//////////////////////

var config = require('./deploy/config');
var mongoose = require('mongoose');
mongoose.connect('mongodb://' + config.db.user_name + ':' + config.db.password + '@' + config.db.URI);
var Event = require('./app/models/event');

//////////////////////
// Configure body parser
//////////////////////

app.use(bodyParser.urlencoded({ extended : true }));
app.use(bodyParser.json());

var port = process.env.PORT || 8080;

var router = express.Router();

//////////////////////
// Requests logging
//////////////////////

router.use(function(req, res, next) {
  // TODO: Check if user is authorized
    console.log('API request!');
    next();
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
  .post(function(req, res) {

    var evt = new Event();
    evt.name = req.body.name;
    evt.description = req.body.description;
    evt.location = req.body.location;
    evt.time = req.body.time;
    evt.dateStart = req.body.dateStart;
    evt.recurCount = req.body.recurCount;
    evt.periodFreq = req.body.periodFreq;
    evt.periodId = req.body.periodId;
    evt.status = req.body.status;
    evt.visibile = req.body.visibile;

    evt.save(function(err) {
      if(err)
        res.send(err);

      res.json({ message: 'Event created successfully!'});
    });
  })

  // Get all events (GET)
  .get(function(req, res) {
    Event.find(function(err, events) {
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
    .get(function(req, res) {
      Event.findById(req.params.event_id, function(err, evt) {
        if(err)
          res.send(err);

        res.json(evt);
      });
    })

    // Update specific event (PUT)
    .put(function(req, res) {

      Event.findById(req.params.event_id, function(err, evt) {
        if(err)
          res.send(err);

        evt.name = req.body.name;
        evt.description = req.body.description;
        evt.location = req.body.location;
        evt.time = req.body.time;
        evt.dateStart = req.body.dateStart;
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
    .delete(function(req, res) {
      Event.remove({
        _id: req.params.event_id
      }, function(err, evt) {
        if(err)
          res.send(err);

        res.json({ message:  'Event ' + req.params.event_id + ' successfully deleted!'})
      });
    });

app.use('/api', router);

// Start server

app.listen(port);
console.log('Doing the stuff you know on port ' + port);
