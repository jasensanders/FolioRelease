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
