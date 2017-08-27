/*
 Copyright 2017 Jasen Sanders (EnRandomLabs).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.enrandomlabs.jasensanders.v1.folio.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.enrandomlabs.jasensanders.v1.folio.AddNewActivity;
import com.enrandomlabs.jasensanders.v1.folio.BuildConfig;
import com.enrandomlabs.jasensanders.v1.folio.Utility;
import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Jasen Sanders on 10/11/2016.
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This Queries two API's for data related to the movie submitted via UPC.
 * Any data that can be obtained will be returned.
 * <p>
 * helper methods.
 */
public class FetchMovieDataService extends IntentService {

    private final String LOG_TAG = FetchMovieDataService.class.getSimpleName();

    // IntentService can perform
    private static final String FETCH_MOVIE_DETAILS = "com.enrandomlabs.jasensanders.v1.folio.services.action.FETCH_MOVIE_DETAILS";

    //Intent Service params
    private static final String UPC_PARAM = "com.enrandomlabs.jasensanders.v1.folio.services.extra.UPC_PARAM";
    private String param1 = "";

    //Error
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String SERVER_ERROR = "ERROR";





    public FetchMovieDataService() {
        super("FetchMovieDataService");
    }

    /**
     * Starts this service to perform action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionFetchData(Context context, String upc_param) {

        Intent intent = new Intent(context, FetchMovieDataService.class);
        intent.setAction(FETCH_MOVIE_DETAILS);
        intent.putExtra(UPC_PARAM, upc_param);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_MOVIE_DETAILS.equals(action)) {
                final String upc_code = intent.getStringExtra(UPC_PARAM);
                param1 = intent.getStringExtra(UPC_PARAM);

                handleActionFetchDetails(upc_code);
            }
        }
    }

    /**
     * Handle action in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchDetails(String upc) {

        //This String array MUST BE in the same order as MovieEntry columns!!
        //We do not include the _ID column because it is auto-generated, so 19 (Not 20) in length
        String[] FullRow = new String[19];

        //Variables to hold row data and known variables initialized
        String TMDB_MOVIE_ID = "";
        String UPC_CODE = upc;
        String MOVIE_THUMB = "";
        String POSTER_URL = "";
        String ART_IMAGE_URL = "";
        String BARCODE_URL = "http://www.searchupc.com/drawupc.aspx?q=" + UPC_CODE;
        String TITLE = "";
        String RELEASE_DATE = "";
        String RUNTIME = "";
        DateFormat df = DateFormat.getDateInstance();
        String ADD_DATE = df.format(Calendar.getInstance().getTime());
        String FORMATS = "";
        String STORE = "";
        String NOTES = "";
        //Default is you own the movie, can be changed in AddNewActivity
        String STATUS = DataContract.STATUS_MOVIES;
        String RATING = "";
        String SYNOPSIS = "";
        String TRAILERS = "";
        String GENRES = "";
        String IMDB_NUMBER = "";


        //FAIL TESTS
        //Verify input UPC
        if(upc == null ||upc.length() != 12){
            Log.e(LOG_TAG, "UPC is null or invalid, nothing will be added to Database");
            sendApologies("UPC is Null or Invalid");
            return;
        }
        //Verify Internet access
        if(!Utility.isNetworkAvailable(getApplicationContext())){
            sendApologies("No Internet Access");
            return;
        }

        //Get UPC data from SearchUPC.com database Verify Success.
        String upcJson = requestUPCdata(upc);
        if(upcJson == null){
            Log.v(LOG_TAG, "Not found in SearchUPC database");
            sendApologies("UPC Lookup Error");
            return;
        }

        //Parse UPC data. Verify Success.
        String[] upcResults = parseUPCdataJson(upcJson);
        if(upcResults == null){
            Log.e(LOG_TAG, "Error Parsing UPC json data");
            sendApologies("UPC parse Error");
            return;
        }
        //Add data to row
        ART_IMAGE_URL = upcResults[1];
        String[] nameAndFormat = Utility.parseProductName(upcResults[0]);
        TITLE = nameAndFormat[0];
        FORMATS = nameAndFormat[1];

        //Get Movie Data. Verify Success.
        String TMDBmovieJson = requestMovieData(nameAndFormat[0]);
        if(TMDBmovieJson == null){
            Log.e(LOG_TAG, "Not found in TMDB database");
            //sendApologies("Movie Lookup Error");
            //Return what we can even if its minimal.
            FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, ART_IMAGE_URL, TITLE,
                    FORMATS, MOVIE_THUMB, BARCODE_URL, RELEASE_DATE, RUNTIME, ADD_DATE,
                    POSTER_URL, STORE, NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES, IMDB_NUMBER};

            sendDataBack(FullRow);
            return;
        }

        //Parse Movie Data. Verify Success.
        String[] TMDBmovieData = parseTMDBdataJson(TMDBmovieJson);
        //String[]{id, thumb, poster, title, date, synopsis, genre};
        if(TMDBmovieData == null){
            //Log.e(LOG_TAG, "Error parsing TMDB json data");
            sendApologies("Movie Data Parse Error");
            return;
        }


        //Add Data to row.
        TMDB_MOVIE_ID = TMDBmovieData[0];
        MOVIE_THUMB = TMDBmovieData[1];
        POSTER_URL = TMDBmovieData[2];
        RELEASE_DATE = TMDBmovieData[4];
        SYNOPSIS = TMDBmovieData[5];
        GENRES = TMDBmovieData[6];

        //Attempt to Get Details Data
        String DetailsJson = requestDetailsData(TMDB_MOVIE_ID);
        if(DetailsJson != null){
            String[] detailsRes = parseTMDBdetailsJson(DetailsJson);
            String runtime = detailsRes[0];
            String imdbRes = detailsRes[1];
            if(runtime != null && imdbRes != null){
                RUNTIME = runtime;
                IMDB_NUMBER = imdbRes;
            }
            else{
                Log.e(LOG_TAG, "Error parsing movie runtime data");
            }
        }else{
            Log.e(LOG_TAG, "Error retrieving movie runtime data");
        }

        String TrailerJson = requestTrailerData(TMDB_MOVIE_ID);
        if(TrailerJson != null){
            String trailers = parseTMDBtrailerJson(TrailerJson);
            if(trailers != null){
                TRAILERS = trailers;
            }else{
                Log.e(LOG_TAG, "Error parsing movie trailers data");
            }
        }else{
            Log.e(LOG_TAG, "Error retrieving movie trailers data");
        }

        String ReleaseJson = requestReleaseData(TMDB_MOVIE_ID);
        if(ReleaseJson != null){
            String rating = parseTMDBreleaseJson(ReleaseJson);
            if(rating != null){
                RATING = Utility.normalizeRating(rating);
            }else{
                Log.e(LOG_TAG, "Error parsing movie rating data");
            }
        }else{
            Log.e(LOG_TAG, "Error retrieving movie rating data");
        }

        //send back what we got, Whatever that is.
        FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, ART_IMAGE_URL, TITLE,
                FORMATS, MOVIE_THUMB, BARCODE_URL, RELEASE_DATE, RUNTIME, ADD_DATE,
                POSTER_URL, STORE, NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES, IMDB_NUMBER};

        sendDataBack(FullRow);


    }



    private String requestUPCdata(String upc){

        if(upc == null){return null;}

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String upcJsonStr = null;

        try{
            final String SEARCHUPC_BASE_URL = "https://secure25.win.hostgator.com/searchupc_com/handlers/upcsearch.ashx?";
            final String REQUEST_TYPE = "request_type";
            final String TYPE_JSON = "3";
            final String ACCESS_TOKEN = "access_token";
            final String UPC_KEY = BuildConfig.SEARCH_UPC_KEY;
            final String UPC = "upc";

            Uri builtUri = Uri.parse(SEARCHUPC_BASE_URL).buildUpon()
                    .appendQueryParameter(REQUEST_TYPE, TYPE_JSON)
                    .appendQueryParameter(ACCESS_TOKEN, UPC_KEY)
                    .appendQueryParameter(UPC, upc)
                    .build();


            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            upcJsonStr = buffer.toString();
        }catch (IOException e){
            Log.e(LOG_TAG, "IO Error UPC Request", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "IO Error UPC Request");
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException f){
                    Log.e(LOG_TAG, "Error Closing Stream UPC Request", f);
                    FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error Closing Stream UPC Request");
                }
            }
        }
        return upcJsonStr;
    }

    private String requestMovieData(String movieName){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String MovieJsonStr = null;

        try {
            if(movieName.isEmpty()){
                return null;
            }
            // Construct the URL for the TMDB query
            final String TMDB_BASE_URL =
                    "https://api.themoviedb.org/3/search/movie?";
            final String APP_KEY = "api_key";
            final String TMDB_KEY = BuildConfig.TMDB_API_KEY;
            final String LANG_PARAM = "language";
            final String US_LANG = "en-US";
            final String QUERY_PARAM = "query";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .appendQueryParameter(LANG_PARAM,US_LANG)
                    .appendQueryParameter(QUERY_PARAM, movieName)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to TMDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            urlConnection.setRequestProperty("Accept","*/*");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            MovieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error TMDB data request" + movieName , e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "IO Error TMDB data request" + movieName);
            // If the code didn't successfully get the Movie data, there's no point in attempting
            // to parse it.

            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream TMDB data request", e);
                    FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error closing stream TMDB data request");
                }
            }
        }
        return MovieJsonStr;
    }

    private String requestDetailsData(String TMDBmovieID){
        String DetailsJsonStr;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try{
            final String TMDB_KEY = BuildConfig.TMDB_API_KEY;
            final String TMDB_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String LANGUAGE = "en-US";
            final String QUERY_LANGUAGE = "language";
            final String APP_KEY = "api_key";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendEncodedPath(TMDBmovieID)
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .appendQueryParameter(QUERY_LANGUAGE, LANGUAGE)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging. Were not doing this right now cause it will confuse the reader.
                //For now we will just append the line. (Logging the result instead of appending "\n")
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            DetailsJsonStr = buffer.toString();
            return DetailsJsonStr;
            //Log.v(LOG_TAG, DetailsJsonStr);
        }catch(IOException i){
            Log.e(LOG_TAG, "IO Error TMDB details request", i);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "IO Error TMDB details request");
            return null;
        }
    }

    private String requestTrailerData(String TMDBMovieID ){
        String TrailerJsonStr;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try{
            final String TMDB_KEY = BuildConfig.TMDB_API_KEY;
            final String TMDB_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String QUERY_PARAM = "videos";
            final String APP_KEY = "api_key";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendEncodedPath(TMDBMovieID)
                    .appendEncodedPath(QUERY_PARAM)
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStreamT = urlConnection.getInputStream();
            StringBuffer bufferT = new StringBuffer();
            if (inputStreamT == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStreamT));

            String lineT;
            while ((lineT = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging. Were not doing this right now cause it will confuse the reader.
                //For now we will just append the line. (Logging the result instead of appending "\n")
                bufferT.append(lineT);
            }

            if (bufferT.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            TrailerJsonStr = bufferT.toString();
            return TrailerJsonStr;
            //Log.v(LOG_TAG, TrailerJsonStr);
        }catch(IOException i){
            Log.e(LOG_TAG, "IO Error TMDB trailers request", i);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "IO Error TMDB trailers request");
            return null;
        }
    }

    private String requestReleaseData(String TMDBmovieID){
        String ReleaseJsonStr;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try{
            final String TMDB_KEY = BuildConfig.TMDB_API_KEY;
            final String TMDB_BASE_URL =
                    "http://api.themoviedb.org/3/movie/";
            final String QUERY_PARAM = "release_dates";
            final String APP_KEY = "api_key";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendEncodedPath(TMDBmovieID)
                    .appendEncodedPath(QUERY_PARAM)
                    .appendQueryParameter(APP_KEY, TMDB_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStreamT = urlConnection.getInputStream();
            StringBuffer bufferT = new StringBuffer();
            if (inputStreamT == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStreamT));

            String lineT;
            while ((lineT = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging. Were not doing this right now cause it will confuse the reader.
                //For now we will just append the line. (Logging the result instead of appending "\n")
                bufferT.append(lineT);
            }

            if (bufferT.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            ReleaseJsonStr = bufferT.toString();
            return ReleaseJsonStr;
            //Log.v(LOG_TAG, ReleaseJsonStr);
        }catch(IOException i){
            Log.e(LOG_TAG, "IO Error TMDB Release Dates request", i);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "IO Error TMDB Release Dates request");
            return null;
        }

    }



    private String[] parseUPCdataJson(String jsonString){
        final String ROOT = "0";
        final String PRODUCTNAME = "productname";
        final String IMAGEURL = "imageurl";

        try {
            JSONObject upcJson = new JSONObject(jsonString);
            JSONObject root = upcJson.getJSONObject(ROOT);
            String productName = root.getString(PRODUCTNAME);
            String imageUrl = root.getString(IMAGEURL);
            return new String[]{productName, imageUrl};
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error Parsing UPC JSON", j );
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error Parsing UPC JSON");
            return null;
        }
    }

    private String[] parseTMDBdataJson(String movieJsonStr){

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_LIST = "results";
        final String TMDB_TITLE = "original_title";
        final String TMDB_IMAGE = "poster_path";
        final String TMDB_DATE = "release_date";
        final String TMDB_SYNOPSIS = "overview";
        final String TMDB_GENRES = "genre_ids";
        final String TMDB_ID = "id";
        final String TMDB_RESULTS_TOTAL = "total_results";

        String thumbBaseUrl = "http://image.tmdb.org/t/p/w92/";
        String posterBaseUrl = "http://image.tmdb.org/t/p/w185/";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_LIST);

            //If there are no results, then there is nothing to parse
            int totalResults = movieJson.getInt(TMDB_RESULTS_TOTAL);
            if(totalResults == 0 || movieArray.length() == 0){
                Log.e(LOG_TAG, "Error Parsing TMDB data NO DATA" );
                return null;
            }

            // Strings to hold the data"
            String id, thumb, poster, title, date, synopsis, genre;

            // Get the JSON object representing the Movie
            JSONObject tmdb_movie = movieArray.getJSONObject(0);

            //Get the genres and turn into String CSV
            JSONArray genres = tmdb_movie.getJSONArray(TMDB_GENRES);
            genre = "";
            for (int i = 0; i < genres.length(); i++) {
                //If its the last one, don't add the comma
                if (i == genres.length() - 1) {
                    genre = genre + String.valueOf(genres.get(i));
                } else {
                    genre = genre + String.valueOf(genres.get(i)) + ", ";
                }
            }

            //Get the rest of the data
            id = tmdb_movie.getString(TMDB_ID);
            thumb = thumbBaseUrl + tmdb_movie.getString(TMDB_IMAGE);
            poster = posterBaseUrl + tmdb_movie.getString(TMDB_IMAGE);
            title = tmdb_movie.getString(TMDB_TITLE);
            date = tmdb_movie.getString(TMDB_DATE);
            synopsis = tmdb_movie.getString(TMDB_SYNOPSIS);

            return new String[]{id, thumb, poster, title, date, synopsis, genre};
        }catch (JSONException j){
            Log.e(LOG_TAG, "Error Parsing TMDB data JSON", j );
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error Parsing TMDB data JSON");
            return null;
        }

    }

    private String parseTMDBreleaseJson(String jsonString){
        final String RESULTS = "results";
        final String ISO_STANDARD = "iso_3166_1";
        final String COUNTRY = "US";
        final String RELEASE_DATES = "release_dates";
        final String TYPE = "type";
        final int TYPE_THEATRICAL = 3;
        final String MPAA_CERT = "certification";
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONArray rootArray = root.getJSONArray(RESULTS);
            for(int i =0; i<rootArray.length(); i++){
                JSONObject node = rootArray.getJSONObject(i);
                if(node.getString(ISO_STANDARD).contentEquals(COUNTRY)){
                    JSONArray nodeArray = node.getJSONArray(RELEASE_DATES);
                    for(int j =0; j<nodeArray.length(); j++){
                        JSONObject last = nodeArray.getJSONObject(j);
                        if(last.getInt(TYPE) == TYPE_THEATRICAL){
                            return last.getString(MPAA_CERT);
                    }
                    }
                }
            }
            return null;
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error parsing TMDB release JSON", j);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error parsing TMDB release JSON");
            return null;
        }
    }

    private String[] parseTMDBdetailsJson(String jsonString){
        //Todo Get imdb number and pass String[] with number.
        final String IMDB_NUM = "imdb_id";
        if(jsonString == null){return null;}
        final String RUNTIME = "runtime";
        try{
            JSONObject root = new JSONObject(jsonString);
            int runtime = root.getInt(RUNTIME);
            String imdbNum = root.getString(IMDB_NUM);
            return new String[]{String.valueOf(runtime) + " min", imdbNum};
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error parsing TMDB details JSON", j);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error parsing TMDB details JSON");
            return null;
        }
    }

    private String parseTMDBtrailerJson(String jsonStr){
        String output = "";
        if(jsonStr==null){return null;}
        try{

            StringBuffer bufferPT = new StringBuffer();
            final String TRAILER_LIST = "results";
            final String TRAILER_KEY = "key";
            final String TRAILER_BASE_URL = "https://www.youtube.com/watch?v=";
            final String SEPARATOR = ",";
            JSONObject result = new JSONObject(jsonStr);
            JSONArray list = result.getJSONArray(TRAILER_LIST);
            for (int i = 0; i < list.length(); i++) {
                JSONObject tmdb_trailer = list.getJSONObject(i);
                String youtubeKey = tmdb_trailer.getString(TRAILER_KEY);
                if (i == list.length() - 1) {
                    bufferPT.append(TRAILER_BASE_URL);
                    bufferPT.append(youtubeKey);
                } else {
                    bufferPT.append(TRAILER_BASE_URL);
                    bufferPT.append(youtubeKey);
                    bufferPT.append(SEPARATOR);
                }

            }
            output = bufferPT.toString();
            //Log.v(LOG_TAG, "Parsed: "+output);

        }catch(JSONException e){
            Log.e(LOG_TAG,"Error parsing TMDB trailers JSON", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error parsing TMDB trailers JSON");
            return null;
        }
        return output;

    }

    //TODO: Need a better failure notice with useful data
    private void sendApologies(String message){
        sendDataBack( new String[]{message, param1, "none", NOT_FOUND});
    }

    private void sendError(String message){
        sendDataBack(new String[]{message, param1, "none", SERVER_ERROR});
    }

    private void sendDataBack(String[] movieData){
        Intent messageIntent = new Intent(AddNewActivity.SERVICE_EVENT_MOVIE);
        messageIntent.putExtra(AddNewActivity.SERVICE_EXTRA_MOVIE,movieData);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }


}
