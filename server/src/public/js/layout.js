// shown date (actually contains day, month, year)
var current_date = new Date();

var monthByNumber = new Array();
monthByNumber[0] = "January";
monthByNumber[1] = "February";
monthByNumber[2] = "March";
monthByNumber[3] = "April";
monthByNumber[4] = "May";
monthByNumber[5] = "June";
monthByNumber[6] = "July";
monthByNumber[7] = "August";
monthByNumber[8] = "September";
monthByNumber[9] = "October";
monthByNumber[10] = "November";
monthByNumber[11] = "December";

var dayByNumber = new Array(7);
dayByNumber[0]=  "Sunday";
dayByNumber[1] = "Monday";
dayByNumber[2] = "Tuesday";
dayByNumber[3] = "Wednesday";
dayByNumber[4] = "Thursday";
dayByNumber[5] = "Friday";
dayByNumber[6] = "Saturday";

var _MS_PER_DAY = 1000 * 60 * 60 * 24;
// a and b are javascript Date objects
function dateDiffInDays(a, b) {
  // Discard the time and time-zone information.
  var utc1 = Date.UTC(a.getFullYear(), a.getMonth(), a.getDate());
  var utc2 = Date.UTC(b.getFullYear(), b.getMonth(), b.getDate());

  return Math.floor((utc2 - utc1) / _MS_PER_DAY);
}

// add event to calendar view
function populate_calendar(event_obj, rule, option) {
  var d_start = event_obj.dateStart;
  var d_end = event_obj.dateEnd;

  if(d_start<d_end && d_start.getMonth() == current_date.getMonth()) {
    var event_html;
    if(option == "googleCalendar") event_html = "<div class=\"single_event google_event\">";
    else event_html = "<div class=\"single_event\">";
    event_html += "<button class=\"modify_event\">Modify</button><button class=\"delete_event\">Delete</button>"
    event_html += "<p class=\"event_id\" style=\"display: none\">" + event_obj._id + "</p>";
    event_html += "<p> Name: " + event_obj.name + "</p>";
    if(event_obj.description) event_html += "<p> Description: " + event_obj.description + "</p>";
    if(event_obj.location) event_html += "<p> Location: " + event_obj.location + "</p>";
    event_html += "<p> From: " + d_start + "</p>";
    if(d_end) event_html += "<p> To: " + d_end + "</p>";
    if(rule) event_html += "<p> When: " + rule.toText() + "</p>";
    event_html += "</div>";

    if(d_start.getDate() != d_end.getDate()) {
      while(d_start <= d_end) {
        console.log(event_obj.name+" "+d_start+" "+d_end);
        $( "[day_number='" + d_start.getDate() + "']" ).find(".events_list").append(event_html);
        $( "[day_number='" + d_start.getDate() + "']" ).addClass("contains");
        d_start.setDate(d_start.getDate() + 1);
      }
    } else {
      console.log(event_obj.name+" "+d_start+" "+d_end);
      $( "[day_number='" + d_start.getDate() + "']" ).find(".events_list").append(event_html);
      $( "[day_number='" + d_start.getDate() + "']" ).addClass("contains");
    }
  }
}

function mapGoogleEventsToMccEvents(event) {
  var mccEvent = {};

  if(event.id) mccEvent._id = event.id;
  if(event.summary) mccEvent.name = event.summary;
  if(event.description) mccEvent.description = event.description;
  if(event.location) mccEvent.location = event.location;
  if(event.start.dateTime) mccEvent.dateStart = new Date(event.start.dateTime);
  if(event.end.dateTime) mccEvent.dateEnd = new Date(event.end.dateTime);

  console.log(mccEvent.dateStart);

  if(event.status) mccEvent.status = event.status;

  return mccEvent;
}

function mapMccEventsToGoogleEvents(event) {
  var googleEvent = {};

  if(event._id) googleEvent.id = event._id;
  if(event.name) googleEvent.summary = event.name;
  if(event.description) googleEvent.description = event.description;
  if(event.location) googleEvent.location = event.location;
  if(event.dateStart) googleEvent.start.dateTime = new Date(event.dateStart);
  if(event.dateEnd) googleEvent.end.dateTime = new Date(event.dateEnd);

  console.log(googleEvent.dateStart);

  if(event.status) googleEvent.status = event.status;

  return googleEvent;
}

function listUpcomingGoogleEvents() {
  var next_month = (current_date.getMonth()+1)%12;
  var year_end = current_date.getFullYear();
  if(next_month == 0) year_end++;

  var request = gapi.client.calendar.events.list({
    'calendarId': 'primary',
    'timeMin': (new Date(current_date.getFullYear(), current_date.getMonth())).toISOString(),
    'timeMax': (new Date(year_end, next_month)).toISOString(),
    'showDeleted': false,
    'singleEvents': true,
    'orderBy': 'startTime'
  });

  request.execute(function(resp) {
    var events = resp.items;

    if (events.length > 0) {
      for (i = 0; i < events.length; i++) {
        var event = events[i];
        var mccEvent = mapGoogleEventsToMccEvents(event);
        populate_calendar(mccEvent, "", "googleCalendar");
      }
      set_handlers();
    }

  });
}

function importGoogleCalendar() {
  var request = gapi.client.calendar.events.list({
    'calendarId': 'primary',
    'timeMin': (new Date(0000, 1)).toISOString(),
    'timeMax': (new Date(9999, 12)).toISOString(),
    'showDeleted': false,
    'singleEvents': true,
    'orderBy': 'startTime'
  });

  request.execute(function(resp) {
    var events = resp.items;

    if (events.length > 0) {
      for (i = 0; i < events.length; i++) {
        var event = events[i];
        var requestData = "name="+event.summary+"&";
        if(event.description) requestData += "description="+event.description+"&";
        if(event.location) requestData += "location="+event.location+"&";
        if(event.start.dateTime) requestData += "dateStart="+new Date(event.start.dateTime)+"&";
        if(event.end.dateTime) requestData += "dateEnd="+new Date(event.end.dateTime)+"&";
        if(event.status) requestData += "status="+event.status;
        send_create_event_request(requestData);
      }
    }
  });
}

function exportGoogleCalendar() {
  var request1 = gapi.client.calendar.calendars.insert({
    'summary': 'MCC Calendar'
  });

  request1.execute(function(resp) {
    console.log(resp.id);
    request = '{'
    +'"name": {"$regex": "."}'
    +'}';
    console.log(request);
    $.ajax({
      url: '/api/events/searches',
      type: 'post',
      data:request,
      contentType:"application/json; charset=utf-8",
      dataType:"json",
      success: function(data) {
        $.each(data, function(i, event_obj) {
          send_import_GoogleEvent_request(event_obj, event_obj._id, resp.id);
        });
        alert("Events exported succesfully");
        set_handlers();
      }
    });
  });
}

// retrieve events of the month to be shown in the calendar
function retrieve_events(month, year) {
  month++;
  var year_end = year
  if(month == 12) year_end++;

  // request events entirely in the month
  var request = '{'
  +'"dateStart" : {"$gte": "'+year+', '+month+'", "$lt": "'+year_end+', '+((month%12)+1)+'"},'
  +'"dateEnd" : {"$gte": "'+year+', '+month+'", "$lt": "'+year_end+', '+((month%12)+1)+'"},'
  +'"recurFreq": {"$exists": false}'
  +'}';
  console.log(request);
  send_search_request(request);

  // request events that start in a previous moment but end in this month
  request = '{'
  +'"dateStart" : {"$gte": "'+0000+', '+month+'", "$lt": "'+year+', '+month+'"},'
  +'"dateEnd" : {"$gte": "'+year+', '+month+'", "$lt": "'+year_end+', '+((month%12)+1)+'"},'
  +'"recurFreq": {"$exists": false}'
  +'}';
  console.log(request);
  send_search_request(request);

  // request events that start in a this month but end in a future month
  request = '{'
  +'"dateStart" : {"$gte": "'+year+', '+month+'", "$lt": "'+year_end+', '+((month%12)+1)+'"},'
  +'"dateEnd" : {"$gte": "'+year_end+', '+((month%12)+1)+'", "$lt": "'+9999+', '+((month%12)+1)+'"},'
  +'"recurFreq": {"$exists": false}'
  +'}';
  console.log(request);
  send_search_request(request);

  // request repeated events
  request = '{'
  +'"recurFreq": {"$regex": "."}'
  +'}';
  console.log(request);
  send_search_request(request);
}

function send_create_event_request(request) {
  $.ajax({
    url: '/api/events',
    type: 'post',
    data:request,
    success: function(message) {
      console.log(message);
      update_view();
      alert("Event created successfully");
    }
  });
}

function send_edit_event_request(request, id) {
  $.ajax({
    url: '/api/events/'+id,
    type: 'put',
    data:request,
    success: function(message) {
      update_view();
      console.log(message);
      alert("Event modified successfully");
    }
  });
}

function send_delete_event_request(id) {
  $.ajax({
    url: '/api/events/'+id,
    type: 'delete',
    success: function(message) {
      update_view();
      alert("Event deleted successfully");
    }
  });
}

function send_retrieve_event_request(id) {
  $.ajax({
    url: '/api/events/'+id,
    type: 'get',
    success: function(event_obj) {
      if(event_obj.name) $("#modifyEvent_name").val(event_obj.name);
      else $("#modifyEvent_name").val("");
      if(event_obj.description) $("#modifyEvent_desc").val(event_obj.description);
      else $("#modifyEvent_desc").val("");
      if(event_obj.location) $("#modifyEvent_location").val(event_obj.location);
      else $("#modifyEvent_location").val("");
      if(event_obj.dateEnd) $("#modifyEvent_endDate").val((event_obj.dateEnd));
      else $("#modifyEvent_endDate").val("");

      if(event_obj.recurFreq) $("#modifyEvent_recurFreq").val(event_obj.recurFreq);
      else $("#modifyEvent_recurFreq").val("nonapplicable");
      if(event_obj.dateStart) $("#modifyEvent_startDate").val((event_obj.dateStart));
      else $("#modifyEvent_startDate").val("");
      if(event_obj.recurInterval) $("#modifyEvent_recurInterval").val(event_obj.recurInterval);
      else $("#modifyEvent_recurInterval").val("");
      if(event_obj.recurCount) $("#modifyEvent_recurCount").val(event_obj.recurCount);
      else $("#modifyEvent_recurCount").val("");
      if(event_obj.recurUntil) $("#modifyEvent_recurUntil").val((event_obj.recurUntil));
      else $("#modifyEvent_recurUntil").val("");

      if(event_obj.recurFreq || event_obj.recurInterval || event_obj.recurCount || event_obj.recurUntil ) $('.toggle_repeat').prop('checked', true);
      else $('.toggle_repeat').prop('checked', false);

      if( $( ".toggle_repeat" ).is(':checked')) $( ".hider" ).css("display", "block");
      else $( ".hider" ).css("display", "none");

      console.log(event_obj);

      $("#modifyEventForm").css("visibility", "visible");
    }
  });
}

function send_retrieve_GoogleEvent_request(event_id) {
  var request = gapi.client.calendar.events.list({
    'calendarId': 'primary',
    'timeMin': (new Date(0000, 1)).toISOString(),
    'timeMax': (new Date(9999, 12)).toISOString(),
    'showDeleted': false,
    'singleEvents': true,
    'orderBy': 'startTime'
  });

  request.execute(function(resp) {
    var events = resp.items;

    if (events.length > 0) {
      for (i = 0; i < events.length; i++) {
        var event = events[i];
        if(event_id == event.id) {
          var event_obj = mapGoogleEventsToMccEvents(event);
          if(event_obj.name) $("#modifyEvent_name").val(event_obj.name);
          else $("#modifyEvent_name").val("");
          if(event_obj.description) $("#modifyEvent_desc").val(event_obj.description);
          else $("#modifyEvent_desc").val("");
          if(event_obj.location) $("#modifyEvent_location").val(event_obj.location);
          else $("#modifyEvent_location").val("");
          if(event_obj.dateEnd) $("#modifyEvent_endDate").val((event_obj.dateEnd).toISOString());
          else $("#modifyEvent_endDate").val("");

          if(event_obj.recurFreq) $("#modifyEvent_recurFreq").val(event_obj.recurFreq);
          else $("#modifyEvent_recurFreq").val("nonapplicable");
          if(event_obj.dateStart) $("#modifyEvent_startDate").val((event_obj.dateStart).toISOString());
          else $("#modifyEvent_startDate").val("");
          if(event_obj.recurInterval) $("#modifyEvent_recurInterval").val(event_obj.recurInterval);
          else $("#modifyEvent_recurInterval").val("");
          if(event_obj.recurCount) $("#modifyEvent_recurCount").val(event_obj.recurCount);
          else $("#modifyEvent_recurCount").val("");
          if(event_obj.recurUntil) $("#modifyEvent_recurUntil").val((event_obj.recurUntil).toISOString());
          else $("#modifyEvent_recurUntil").val("");

          if(event_obj.recurFreq || event_obj.recurInterval || event_obj.recurCount || event_obj.recurUntil ) $('.toggle_repeat').prop('checked', true);
          else $('.toggle_repeat').prop('checked', false);

          if( $( ".toggle_repeat" ).is(':checked')) $( ".hider" ).css("display", "block");
          else $( ".hider" ).css("display", "none");

          console.log(event_obj);

          $("#modifyEventForm").css("visibility", "visible");
        }
      }
    }
  });
}

function send_edit_GoogleEvent_request(values, event_id) {
  var request = gapi.client.calendar.events.update({
    'calendarId': 'primary',
    'eventId': event_id,
    'summary': values['modifyEvent_name'],
    'description': values['modifyEvent_desc'],
    'location': values['modifyEvent_location'],
    "start": {
      "dateTime": values['modifyEvent_startDate']
    },
    "end": {
      "dateTime": values['modifyEvent_endDate']
    }
  });

  request.execute(function(resp) {
    console.log(resp);
    update_view();
    alert("Google Calendar event modified successfully");
  });
}

function send_delete_GoogleEvent_request(event_id) {
  var request = gapi.client.calendar.events.delete({
    'calendarId': 'primary',
    'eventId': event_id
  });

  request.execute(function(resp) {
    console.log(resp);
    update_view();
    alert("Google Calendar event deleted successfully");
  });
}

function send_import_GoogleEvent_request(event, event_id, cal_id) {
  var request = gapi.client.calendar.events.insert({
    'calendarId': cal_id,
    'eventId': event_id,
    'summary': event.name,
    'description': event.description,
    'location': event.location,
    "start": {
      "dateTime": event.dateStart
    },
    "end": {
      "dateTime": event.dateEnd
    }
  });

  request.execute(function(resp) {
    console.log(resp);
  });
}

function send_search_request(request) {
  $.ajax({
    url: '/api/events/searches',
    type: 'post',
    data:request,
    contentType:"application/json; charset=utf-8",
    dataType:"json",
    success: function(data) {
      $.each(data, function(i, event_obj) {
        // deal with repetitions
        var options = {};
        if(event_obj.recurFreq) {
          if(event_obj.recurFreq == "DAILY") options.freq = RRule.YEARLY;
          if(event_obj.recurFreq == "MONTHLY") options.freq = RRule.MONTHLY;
          if(event_obj.recurFreq == "WEEKLY") options.freq = RRule.WEEKLY;
          if(event_obj.recurFreq == "YEARLY") options.freq = RRule.YEARLY;
          if(event_obj.recurFreq == "HOURLY") options.freq = RRule.HOURLY;
          if(event_obj.recurFreq == "MINUTELY") options.freq = RRule.MINUTELY;
          if(event_obj.recurFreq == "SECONDLY") options.freq = RRule.SECONDLY;
        }
        if(event_obj.dateStart) options.dtstart = new Date(event_obj.dateStart);
        if(event_obj.recurInterval) options.interval = event_obj.recurInterval;
        if(event_obj.recurCount) options.count = event_obj.recurCount;
        if(event_obj.recurUntil) options.until = new Date(event_obj.recurUntil);
        //if(event_obj.recurByDay) options.byweekday = event_obj.recurByDay.replace(/\s+/g, '').split(",");
        //if(event_obj.recurByMonthDay) options.bymonthday = event_obj.recurByMonthDay.replace(/\s+/g, '').split(",");
        //if(event_obj.recurByMonth) options.bymonth = event_obj.recurByMonth.replace(/\s+/g, '').split(",");
        //if(event_obj.recurWeekStart) options.wkst = event_obj.recurWeekStart.replace(/\s+/g, '').split(",");

        var rule = new RRule(options);
        console.log(rule.options.until);

        if(rule.options.freq) {
          var dates = rule.between(new Date(0000, current_date.getMonth(), 1), new Date(current_date.getFullYear(), current_date.getMonth()+1, 0));
          console.log(dates);

          var dateNumber = dates.length;
          var dateDifference = dateDiffInDays(new Date(event_obj.dateStart), new Date(event_obj.dateEnd));
          console.log(dateDifference);
          for (var i = 0; i < dateNumber; i++) {
            event_obj.dateStart = dates[i];
            event_obj.dateEnd = new Date(event_obj.dateStart.toString());
            event_obj.dateEnd.setDate(event_obj.dateEnd.getDate() + dateDifference);
            populate_calendar(event_obj, rule, "");
          }
        } else {
          event_obj.dateStart = new Date(event_obj.dateStart);
          event_obj.dateEnd = new Date(event_obj.dateEnd);
          populate_calendar(event_obj, "", "");
        }
      });
      set_handlers();
    }
  });
}

// prepare the month view with the correct days
function prepare_month(month, year) {
  console.log( "submitted date: " + monthByNumber[month] + " " + year);

  var d = new Date(year,month,1);
  var firstDayOfMonth = d.getDay();
  d = new Date(year,month+1,0);
  var lastDayOfMonth = d.getDate();

  console.log( "firstDayOfMonth: " + dayByNumber[firstDayOfMonth] );
  console.log( "lastDayOfMonth: " + lastDayOfMonth + " " + dayByNumber[d.getDay()]);

  // add the title with the name of the month and the year
  $( "#month_title" ).text(monthByNumber[month] + " " + current_date.getFullYear());

  // add the numbers to the days
  var day_compare = 0;
  var day_number = 1;
  $( ".week td" ).each(function( index ) {
    if(day_compare >= firstDayOfMonth && day_number <= lastDayOfMonth) {
      $( this ).children("p").text(day_number);
      $( this ).attr("day_number", day_number);
      day_number++;
    } else {
      $( this ).children("p").text("");
      $( this ).removeAttr("day_number");
    }
    day_compare++;
  });

  $( ".events_list" ).text("");
  $( ".contains" ).removeClass("contains");

  retrieve_events(current_date.getMonth(), current_date.getFullYear());
}

// write the name of the days on the calendar
function set_days() {
  var temp = 0;
  $( ".dayNames td" ).each(function( index ) {
    $( this ).text(dayByNumber[temp]);
    temp++;
  });
}

// setup mouseover functions to show events
function set_handlers() {
  $( "td" ).unbind();
  $( ".contains" ).mouseover(function() {
    $( this ).children( "div" ).css( "visibility", "visible" );
  });
  $( ".contains" ).mouseleave(function() {
    $( this ).children( "div" ).css( "visibility", "hidden" );
  });
  $( ".delete_event" ).unbind();
  $( ".delete_event" ).click(function() {
    if($( this ).parent().hasClass( "google_event" )) send_delete_GoogleEvent_request($( this ).parent().find(".event_id").text());
    else send_delete_event_request($( this ).parent().find(".event_id").text());
  });
  $( ".modify_event" ).unbind();
  $( ".modify_event" ).click(function() {
    $("#modifyEventForm").css("visibility", "hidden");
    $( ".toggle_repeat" ).parent().css("display", "block");

    var event_id = $( this ).parent().find( ".event_id" ).text();
    console.log(event_id);
    $( "#modify_event-div" ).attr("event_id", event_id);

    console.log($( this ).parent().hasClass( "google_event" ));

    if($( this ).parent().hasClass( "google_event" )) {

      send_retrieve_GoogleEvent_request(event_id);
      $( "#modify_event-div" ).addClass( "google_event_flag" );
      $( ".toggle_repeat" ).parent().css("display", "none");
      $( ".toggle_repeat" ).prop('checked', false);
    } else {
      send_retrieve_event_request(event_id);
      $( "#modify_event-div" ).removeClass( "google_event_flag" );
    }

    if( $( ".toggle_repeat" ).is(':checked')) $( ".hider" ).css("display", "block");
    else $( ".hider" ).css("display", "none");
    if( $( "#modify_event-div" ).css("display") == "none" ) $( "#modify_event-div" ).css("display", "block");
    else $( "#modify_event-div" ).css("display", "none");
  });

  $( "table" ).css("visibility", "visible");
}

function update_view() {
  $(".day_events").css("visibility", "hidden");
  $( "table" ).css("visibility", "hidden");
  prepare_month(current_date.getMonth(), current_date.getFullYear());
  if($("#authorize-button").css("display") == "none") listUpcomingGoogleEvents();
}

$(function() {
  set_days();
  prepare_month(current_date.getMonth(), current_date.getFullYear());
  $( "#back" ).click(function() {
    current_date.setDate(1);
    current_date.setMonth(current_date.getMonth()-1);
    update_view();
  });
  $( "#forward" ).click(function() {
    current_date.setDate(1);
    current_date.setMonth(current_date.getMonth()+1);
    update_view();
  });
  $( "#options" ).click(function() {
    if( $( "#options-div" ).css("display") == "none") $( "#options-div" ).css("display", "block");
    else $( "#options-div" ).css("display", "none");
  });
  $( "#add_event" ).click(function() {
    if( $( ".toggle_repeat" ).is(':checked')) $( ".hider" ).css("display", "block");
    else $( ".hider" ).css("display", "none");
    if( $( "#create_event-div" ).css("display") == "none") $( "#create_event-div" ).css("display", "block");
    else $( "#create_event-div" ).css("display", "none");
  });
  $( ".close_window" ).click(function() {
    $( "#create_event-div" ).css("display", "none");
    $( "#modify_event-div" ).css("display", "none");
    $( "#options-div" ).css("display", "none");
  });
  $( ".toggle_repeat" ).click(function() {
    if( $( ".hider" ).css("display") == "none") $( ".hider" ).css("display", "block");
    else $( ".hider" ).css("display", "none");
  });

  $("#createEventForm").submit(function(e) {
    e.preventDefault();
    var $inputs = $('#createEventForm :input');
    var values = {};
    $inputs.each(function() {
      values[this.name] = $(this).val();
    });

    var requestData = "name="+values['createEvent_name']+"&";
    if(values['createEvent_desc']) requestData += "description="+values['createEvent_desc']+"&";
    if(values['createEvent_location']) requestData += "location="+values['createEvent_location']+"&";
    if(values['createEvent_startDate']) requestData += "dateStart="+new Date(values['createEvent_startDate'])+"&";
    if(values['createEvent_endDate']) requestData += "dateEnd="+new Date(values['createEvent_endDate'])+"&";

    if($( ".toggle_repeat" ).is(':checked')) {
      if(values['createEvent_recurCount'] && values['createEvent_recurCount'] != 0) requestData += "recurCount="+values['createEvent_recurCount']+"&";
      if(values['createEvent_recurFreq'] && values['createEvent_recurFreq']!="nonapplicable") requestData += "recurFreq="+values['createEvent_recurFreq']+"&";
      if(values['createEvent_recurInterval']) requestData += "recurInterval="+values['createEvent_recurInterval']+"&";
      if(values['createEvent_recurUntil']) requestData += "recurUntil="+new Date(values['createEvent_recurUntil'])+"&";
      if(values['createEvent_scope']) requestData += "scope="+values['createEvent_scope'];
    }

    console.log(requestData);

    send_create_event_request(requestData);
  });

  $("#modifyEventForm").submit(function(e) {
    e.preventDefault();
    var $inputs = $('#modifyEventForm :input');
    var values = {};
    $inputs.each(function() {
      values[this.name] = $(this).val();
    });

    var requestData = "name="+values['modifyEvent_name']+"&";
    if(values['modifyEvent_desc']) requestData += "description="+values['modifyEvent_desc']+"&";
    if(values['modifyEvent_location']) requestData += "location="+values['modifyEvent_location']+"&";
    if(values['modifyEvent_startDate']) requestData += "dateStart="+new Date(values['modifyEvent_startDate'])+"&";
    if(values['modifyEvent_endDate']) requestData += "dateEnd="+new Date(values['modifyEvent_endDate'])+"&";

    if($( ".toggle_repeat" ).is(':checked')) {
      if(values['modifyEvent_recurCount'] && values['modifyEvent_recurCount'] != 0) requestData += "recurCount="+values['modifyEvent_recurCount']+"&";
      if(values['modifyEvent_recurFreq'] && values['modifyEvent_recurFreq']!="nonapplicable") requestData += "recurFreq="+values['modifyEvent_recurFreq']+"&";
      if(values['modifyEvent_recurInterval']) requestData += "recurInterval="+values['modifyEvent_recurInterval']+"&";
      if(values['modifyEvent_recurUntil']) requestData += "recurUntil="+new Date(values['modifyEvent_recurUntil'])+"&";
      if(values['modifyEvent_scope']) requestData += "scope="+values['modifyEvent_scope'];
    }

    console.log(requestData);
    if($( this ).parent().hasClass( "google_event_flag" )) send_edit_GoogleEvent_request(values, $( this ).parent().attr("event_id"))
    else send_edit_event_request(requestData, $( this ).parent().attr("event_id"));
  });
});
