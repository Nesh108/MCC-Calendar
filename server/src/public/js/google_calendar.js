// Your Client ID can be retrieved from your project in the Google
// Developer Console, https://console.developers.google.com
var CLIENT_ID = '131482151376-8tlm5rgh7rj0a5cfkcodegrendu7mpur.apps.googleusercontent.com';

var SCOPES = ["https://www.googleapis.com/auth/calendar"];

/**
* Check if current user has authorized this application.
*/
  function checkAuth() {
  gapi.auth.authorize(
    {
      'client_id': CLIENT_ID,
      'scope': SCOPES.join(' '),
      'immediate': true
    }, handleAuthResult);
  }

  /**
  * Handle response from authorization server.
  *
  * @param {Object} authResult Authorization result.
  */
  function handleAuthResult(authResult) {
    var authorizeDiv = document.getElementById('authorize-div');
    if (authResult && !authResult.error) {
      // Hide auth UI, then load client library.
      $("#authorize-div span").text("Your Google calendar is being displayed");
      $("#authorize-button").css("display", "none");
      authorizeDiv.style.display = 'block';
      $("#authorizeImport-button").attr("onclick","importGoogleCalendar()");
      $("#importcalendar-div").css("display", "block");
      loadCalendarApi();
    } else {
      // Show auth UI, allowing the user to initiate authorization by
      // clicking authorize button.
      $("#authorize-div span").text("Connect Google Calendar");
      $("#authorize-button").css("display", "inline");
      $("#authorizeImport-button").attr("onclick","handleAuthClickForImport(event)");
      $("#importcalendar-div").css("display", "block");
      authorizeDiv.style.display = 'block';
    }
  }

  /**
  * Handle response from authorization server for Import.
  *
  * @param {Object} authResult Authorization result.
  */
  function handleAuthResultForImport(authResult) {
    var authorizeDiv = document.getElementById('authorize-div');
    if (authResult && !authResult.error) {
      $("#authorizeImport-button").attr("onclick","importGoogleCalendar()");
      loadCalendarApiForImport();
    }
  }

  function handleAuthResultForExport(authResult) {
    var authorizeDiv = document.getElementById('authorize-div');
    if (authResult && !authResult.error) {
      $("#authorizeExport-button").attr("onclick","exportGoogleCalendar()");
      loadCalendarApiForExport();
    }
  }

  /**
  * Initiate auth flow in response to user clicking authorize button.
  *
  * @param {Event} event Button click event.
  */
  function handleAuthClick(event) {
    gapi.auth.authorize(
      {client_id: CLIENT_ID, scope: SCOPES, immediate: false},
      handleAuthResult);
      return false;
    }

    /**
    * Initiate auth flow in response to user clicking authorize button.
    *
    * @param {Event} event Button click event.
    */
    function handleAuthClickForImport(event) {
      gapi.auth.authorize(
        {client_id: CLIENT_ID, scope: SCOPES, immediate: false},
        handleAuthResultForImport);
        return false;
      }

      function handleAuthClickForExport(event) {
        gapi.auth.authorize(
          {client_id: CLIENT_ID, scope: SCOPES, immediate: false},
          handleAuthResultForExport);
          return false;
        }

    /**
    * Load Google Calendar client library. List upcoming events
    * once client library is loaded.
    */
    function loadCalendarApi() {
      gapi.client.load('calendar', 'v3', listUpcomingGoogleEvents);
    }

    /**
    * Load Google Calendar client library. List upcoming events
    * once client library is loaded.
    */
    function loadCalendarApiForImport() {
      gapi.client.load('calendar', 'v3', importGoogleCalendar);
    }

    function loadCalendarApiForExport() {
      gapi.client.load('calendar', 'v3', exportGoogleCalendar);
    }
