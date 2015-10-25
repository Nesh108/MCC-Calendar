Calendar for the Mobile Cloud Computing course at Aalto University (Espoo, Finland).

**Course**: T-110.5121 - Mobile Cloud Computing

**Link to course**: https://mycourses.aalto.fi/course/view.php?id=8779

### API Routes

Check [here](https://github.com/Nesh108/MCC-Calendar/blob/master/docs/routes) for the API documentation.


#### How to Run the WebServer (Linux)

- Install `nodejs` and `npm`
- Execute `npm install` in the main folder (/server/src)
- Add the username, password and URI of your MongoDB instance (/server/deploy/config.js)
- Execute `node server.js` to start the server


#### How to Compile Android Project

- Install [Android Studio](https://developer.android.com/sdk/index.html)
- Import project (select /mobile as the root)
- Run after gradle has completed

**Current Features:**

- Synchronize from external calendar
- Export events to Android calendar
- Import events from Android calendar
- Calendar View (show, add, delete and update events)
- List View (show, delete and update events)

**Current SDK Version:** 23

**Current Build Tools Version:** 23.0.1

**Tested on:** Nexus 10

**Screenshot:**

<p align="center"><img src="https://github.com/Nesh108/MCC-Calendar/blob/master/docs/images/Android_app_screenshot.png" width="600"/></p>
