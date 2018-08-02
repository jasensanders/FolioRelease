package com.enrandomlabs.jasensanders.v1.folio;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ContentProviderInstrumentedTest {
    @Test
    public void useAppContext() {
          // Context of the app under test.
          Context appContext = InstrumentationRegistry.getTargetContext();


          assertEquals("com.enrandomlabs.jasensanders.v1.folio", appContext.getPackageName());

          final String[] MOVIE_COLUMNS = {
                DataContract.MovieEntry.COLUMN_UPC,
                DataContract.MovieEntry.COLUMN_DISC_ART,
                DataContract.MovieEntry.COLUMN_TITLE,
                DataContract.MovieEntry.COLUMN_ADD_DATE,
                DataContract.MovieEntry.COLUMN_FORMATS,
                DataContract.MovieEntry.COLUMN_STATUS,
                DataContract.MovieEntry.COLUMN_RATING,
          };

        //Set Defaults for loader
        String sortOrder = DataContract.MovieEntry.COLUMN_ADD_DATE + " DESC";
        Uri providerCall = DataContract.MovieEntry.buildUriAll();

        //FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, MOVIE_THUMB, POSTER_URL,
//                ART_IMAGE_URL, BARCODE_URL, TITLE, RELEASE_DATE, RUNTIME, ADD_DATE, FORMATS, STORE,
//                NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES};
//
//        String[] add = {"10020", "786936850628", "Http://www.google.com/thumb.jpg",
//                "Http://www.googleTest.com/poster.jpg", "Http://www.google.com/artImage.jpg", "Http://www.google.com/barcode.jpg",
//        "Movie1", "2014-11-1", "102 min", "2016-10-18", "DVD", "", "", "OWNED", "PG", "Once upon a time, the end", "", "10, 18, 36"};
//
//        String[] add2 = {"37014", "786936850383", "Http://www.google.com/thumb2.jpg",
//                "Http://www.googleTest.com/poster2.jpg", "Http://www.google.com/artImage2.jpg", "Http://www.google.com/barcode2.jpg",
//                "Movie2", "2015-11-1", "103 min", "2016-10-18", "DVD - Blu-ray", "", "", "OWNED", "PG-13", "Once upon a time, the end", "", "10, 19, 107"};
//
//        String[] add3 = {"95123", "786936850567", "Http://www.google.com/thumb3.jpg",
//                "Http://www.googleTest.com/poster3.jpg", "Http://www.google.com/artImag3e.jpg", "Http://www.google.com/barcode3.jpg",
//                "Movie3", "2016-11-1", "104 min", "2016-10-18", "Blu-ray - DVD - Digital HD", "", "", "OWNED", "G", "Once upon a time, the end", "", "24, 18, 36"};
//
//        ContentValues values = Utility.makeMovieRowValues(add);
//        appContext.getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, values);
//        ContentValues values2 = Utility.makeMovieRowValues(add2);
//        appContext.getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, values2);
//        ContentValues values3 = Utility.makeMovieRowValues(add3);
//        appContext.getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, values3);

        Cursor cursor = appContext.getContentResolver().query(providerCall, MOVIE_COLUMNS, null, null, sortOrder);
        String upcTest = null;
        if(cursor.moveToFirst()) {
            upcTest = cursor.getString(0);
        }

        assertNotNull("Cursor", cursor);
        assertNotNull("UPC", upcTest);
        Log.v("UPC: ", upcTest);
    }
}
