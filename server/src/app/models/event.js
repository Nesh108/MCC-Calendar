var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

// Inspired by: http://allenbrowne.com/apprecur.html
// Following iCal specs: https://en.wikipedia.org/wiki/ICalendar
// and https://tools.ietf.org/html/rfc5545#section-3.3.10

var EventSchema = new Schema({
    name: { type: String, required: true },         // Event name
    description: { type: String, required: true },  // Event description
    location: { type: String },                     // Event location
    dateStart: { type: Date, required: true },      // When the series of event starts
    dateEnd: { type: Date },                        // When the series of event ends

    recurCount: { type: Number },                   // How many times the event will recur. 0 for one-time. Blank for open ended.
    recurFreq: { type: String },                    // Event frequency: DAILY, WEEKLY, MONTHLY, YEARLY
    recurInterval: { type: Number },                // Interval between occurrences
    recurUntil: { type: Date },                     // Last day this event should be repeated
    recurByDay: { type: String },                   // On which day the occurrence should happen: MO, TU, WE... or 1FR (first friday). Please note you can specify more than one by separating with a comma.
    recurByMonthDay: { type: String },              // On which day of the month the occurrence should happen: 1, 2, 5... To specify more than one provide a list: 1,5,20
    recurByMonth: { type: String },                 // On which month the occurrence should happen: 1, 2, 5... To specify more than one provide a list: 1,5,12
    recurWeekStart: { type: String },               // Specify on which day the week should start: SU, MO...

    status: { type: String },                       // Status: TENTATIVE, CONFIRMED, CANCELLED, NEEDS-ACTION, COMPLETED, DRAFT...
    scope: { type: String, default: "PUBLIC" },     // PUBLIC, PRIVATE or CONFIDENTIAL
    owner: {type: String, required: true}           // user who created the event
});

module.exports = mongoose.model('Event', EventSchema);
