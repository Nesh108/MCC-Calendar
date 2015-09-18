var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

// Inspired by: http://allenbrowne.com/apprecur.html

var EventSchema = new Schema({
    name: { type: String, required: true },         // Event name
    description: { type: String, required: true },  // Event description
    location: { type: String },                     // Event location
    time: { type: Number, required: true },         // Event time
    dateStart: { type: Number, required: true },    // When the series of event starts
    recurCount: { type: Number },                   // How many times the event will recur. 0 for one-time. Blank for open ended.
    periodFreq: { type: Number },                   // How many days/weeks/months/years between recurrences
    periodId: { type: Number },                     // 0: days, 1: weeks, 2: months, 3: years
    status: { type: String, default: "Available"},  // Status ('Available, 'Busy')
    visibile: { type: Boolean, default: true }      // Public or Private
});

module.exports = mongoose.model('Event', EventSchema);
