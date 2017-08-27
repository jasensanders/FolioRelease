/*
 * Copyright 2017 Jasen Sanders (EnRandomLabs).

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enrandomlabs.jasensanders.v1.folio;


import android.app.Application;
import android.os.Bundle;

/**
 * Created by Jasen Sanders on 10/11/2016.
 * Fixed Application object for Folio app.
 * Contains One fixed Bundle "hold"
 */


public class FolioApplication extends Application {

    public Bundle hold = new Bundle();

    public Bundle getHold(){
        return hold;
    }

//    public Tracker mTracker;
//
//    // Get the tracker associated with this app
//    public void startTracking() {
//
//        // Initialize an Analytics tracker using a Google Analytics property ID.
//
//        // Does the Tracker already exist?
//        // If not, create it
//
//        if (mTracker == null) {
//            GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
//
//            // Get the config data for the tracker
//            mTracker = ga.newTracker(R.xml.track_app);
//
//            // Enable tracking of activities
//            ga.enableAutoActivityReports(this);
//
//            // Set the log level to verbose.
//            ga.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
//        }
//    }
//
//    public Tracker getTracker() {
//        // Make sure the tracker exists
//        startTracking();
//
//        // Then return the tracker
//        return mTracker;
//    }



}
