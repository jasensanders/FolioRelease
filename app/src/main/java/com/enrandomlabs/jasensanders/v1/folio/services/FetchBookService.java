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
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;


/**
 * Created by Jasen Sanders on 10/11/2016.
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This Queries two API's for data related to the book via UPC.
 * Any data that can be obtained will be returned.
 * <p>
 *
 * helper methods.
 */
public class FetchBookService extends IntentService {
    private static final String LOG_TAG = FetchBookService.class.getSimpleName();
    //Action key
    private static final String FETCH_BOOK = "com.enrandomlabs.jasensanders.v1.folio.services.action.FETCH_BOOK";

    //Params
    private static final String EAN_PARAM = "com.enrandomlabs.jasensanders.v1.folio.services.extra.EAN_PARAM";

    private String param1 = "";

    //Error
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String SERVER_ERROR = "ERROR";

    public FetchBookService() {
        super("FetchBookService");
    }

    /**
     * Starts this service to perform action FetchBook with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFetchBook(Context context, String ean) {
        Intent intent = new Intent(context, FetchBookService.class);
        intent.setAction(FETCH_BOOK);
        intent.putExtra(EAN_PARAM, ean);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                param1 = intent.getStringExtra(EAN_PARAM);
                final String ean = intent.getStringExtra(EAN_PARAM);

                handleActionFetchBook(ean);

            }
        }
    }

    /**
     * Handle action FetchBook in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchBook(String ean){

        //Verify Internet Access
        if(!Utility.isNetworkAvailable(getApplicationContext())){
            sendApologies("No Internet Access");
            return;
        }

        String[] results = fetchBook(ean);
        String[] baseData = fetchProduct(ean);

        if(results != null ){
            //If results are good, then send data back.
            sendDataBack(results);

        }else{
            //Not Found in google books, but found in SearchUPC database.
            if(baseData != null){
//                new String[] {bookId, ean, imgUrl, title, subtitle, desc, barcode, pubDate, authors,
//                        addDate, publisher, store, notes, status, pages, categories};
                String barcode = "http://www.searchupc.com/drawupc.aspx?q=" + ean;
                DateFormat df = DateFormat.getDateInstance();
                String addDate = df.format(Calendar.getInstance().getTime());
                String status = DataContract.STATUS_BOOK;

                results = new String[]{DataContract.STATUS_NOID, ean, baseData[1], baseData[0], "", "", barcode,
                "", "", addDate, "", "", "", status, "", ""};
                sendDataBack(results);
            }else {
                //We got nothing. Book not in either database.
                sendApologies("Not found in Databases");
            }
        }


    }
    private String[] fetchBook(String ean) {

        if(ean.length()!=13){
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString;

        try {
            final String BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";
            final String ISBN_PARAM = "isbn:" + ean;
            final String KEY_PARAM = "key";
            final String API_KEY = BuildConfig.GOOGLE_BOOKS_KEY;

            Uri builtUri = Uri.parse(BOOKS_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .appendQueryParameter(KEY_PARAM, API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            bookJsonString = buffer.toString();
            if(bookJsonString  == null){
                return null;
            }
            String[] returnable = parseGoogleBooksJson(bookJsonString, ean);
            if(resultsValid(returnable)){
                return returnable;
            }else{
                Log.e(LOG_TAG, "Google books results not valid");
                return null;
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving Google book data ", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error retrieving Google book data");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error closing stream");
                }
            }

        }
        return null;

    }

    private String[] parseGoogleBooksJson(String bookJsonString, String ean){

        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";
        final String PUBLISHER = "publisher";
        final String PUB_DATE = "publishedDate";
        final String PAGES_COUNT = "pageCount";
        final String ID = "id";

        try {
            JSONObject bookJson = new JSONObject(bookJsonString);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            }else{
                return null;
            }
            JSONObject info = (JSONObject) bookArray.get(0);
            String bookId = "";
            if(info.has(ID)){
                bookId = info.getString(ID);
            }

            JSONObject volumeInfo = info.getJSONObject(VOLUME_INFO);

            String title = "";
            if(volumeInfo.has(TITLE)){
                title = volumeInfo.getString(TITLE);
            }

            String subtitle = "";
            if(volumeInfo.has(SUBTITLE)) {
                subtitle = volumeInfo.getString(SUBTITLE);
            }

            String desc="";
            if(volumeInfo.has(DESC)){
                desc = volumeInfo.getString(DESC);
            }

            String imgUrl = "";
            if(volumeInfo.has(IMG_URL_PATH) && volumeInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = volumeInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }else{
                String res = fetchAltImage(ean);
                if(res != null){
                    imgUrl = res;
                }
            }

            String publisher = "";
            if(volumeInfo.has(PUBLISHER)){
                publisher = volumeInfo.getString(PUBLISHER);
            }

            String pubDate = "";
            if(volumeInfo.has(PUB_DATE)){
                pubDate = volumeInfo.getString(PUB_DATE);
            }

            String pages = "";
            if(volumeInfo.has(PAGES_COUNT)){
                pages = String.valueOf(volumeInfo.getInt(PAGES_COUNT));
            }

            String authors = "";
            if(volumeInfo.has(AUTHORS)) {
                JSONArray array = volumeInfo.getJSONArray(AUTHORS);
                String result = "";
                for(int i = 0; i< array.length(); i++){
                    if(i == array.length()-1){
                        result = result + array.getString(i);
                    }else {
                        result = result + array.getString(i) + ", ";
                    }
                }
                authors = result;
            }

            String categories = "";
            String res = fetchCategories(bookId);
            if(res != null){
                categories = res;
            }
            String barcode = "http://www.searchupc.com/drawupc.aspx?q=" + ean;

            DateFormat df = DateFormat.getDateInstance();

            String addDate = df.format(Calendar.getInstance().getTime());
            String store = "";
            String notes = "";
            //Default Status is owned, but can be changed in addNew Activity.
            String status = DataContract.STATUS_BOOK;

            return new String[] {bookId, ean, imgUrl, title, subtitle, desc, barcode, pubDate, authors,
                    addDate, publisher, store, notes, status, pages, categories};

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing Google book data json.", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error parsing Google book data json.");
        }
        return null;

    }

    private String fetchCategories(String BookId){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String GOOGLE_BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes/";


            Uri builtUri = Uri.parse(GOOGLE_BOOK_BASE_URL).buildUpon()
                    .appendPath(BookId)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving Google book category data.", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error retrieving Google book category data.");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error closing stream");
                }
            }

        }
        try {
            final String CATEGORIES = "categories";

            JSONObject bookJson = new JSONObject(bookJsonString);

            String catagories = "";
            if(bookJson.has(CATEGORIES)){
                JSONArray array = bookJson.getJSONArray(CATEGORIES);
                String result = "";
                for(int i = 0; i< array.length(); i++){
                    if(i == array.length()-1){
                        result = result + array.getString(i);
                    }else {
                        result = result + array.getString(i) + ", ";
                    }
                }
                catagories = result;
            }
            return catagories;

        }catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing Google book category data.", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error parsing Google book category data.");
        }
        return null;
    }

    private String fetchAltImage(String ean){
        if(ean == null){return null;}

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
                    .appendQueryParameter(UPC, ean)
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
            Log.e(LOG_TAG, "Error retrieving SearchUPC alt image.", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error retrieving SearchUPC alt image.");
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException f){
                    Log.e(LOG_TAG, "Error Closing Stream.", f);
                    FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error Closing Stream.");
                }
            }
        }


        final String ROOT = "0";
        final String PRODUCTNAME = "productname";
        final String IMAGEURL = "imageurl";

        try {
            JSONObject upcJson = new JSONObject(upcJsonStr);
            JSONObject root = upcJson.getJSONObject(ROOT);
            String imageUrl = root.getString(IMAGEURL);
            return imageUrl;
        }catch(JSONException j){
            Log.e(LOG_TAG, "Error parsing SearchUPC alt image json.", j );
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error parsing SearchUPC alt image json.");
            return null;
        }

    }

    private String[] fetchProduct(String ean){
        if(ean == null){return null;}

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String upcCsvStr = null;

        try{
            final String SEARCHUPC_BASE_URL = "https://secure25.win.hostgator.com/searchupc_com/handlers/upcsearch.ashx?";
            final String REQUEST_TYPE = "request_type";
            final String TYPE_CSV = "1";
            final String ACCESS_TOKEN = "access_token";
            final String UPC_KEY = BuildConfig.SEARCH_UPC_KEY;
            final String UPC = "upc";

            Uri builtUri = Uri.parse(SEARCHUPC_BASE_URL).buildUpon()
                    .appendQueryParameter(REQUEST_TYPE, TYPE_CSV)
                    .appendQueryParameter(ACCESS_TOKEN, UPC_KEY)
                    .appendQueryParameter(UPC, ean)
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
            upcCsvStr = buffer.toString();
        }catch (IOException e){
            Log.e(LOG_TAG, "Error retrieving SearchUPC alt image.", e);
            FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error retrieving SearchUPC alt image.");
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException f){
                    Log.e(LOG_TAG, "Error Closing Stream.", f);
                    FirebaseCrash.logcat(Log.ERROR, LOG_TAG, "Error Closing Stream.");
                }
            }
        }

        return parseCsv(upcCsvStr);
    }

    private String[] parseCsv(String inCsv){
        String tables;
        String values;
        String[] output = null;

        BufferedReader inreader = new BufferedReader(new StringReader(inCsv));
        try {
            tables = inreader.readLine();
            values = inreader.readLine();
            inreader.close();
            output = values.split(",");

        }catch(IOException e){
            e.printStackTrace();
        }
        if(output != null) {
            String Name = output[0].replace("\"", "");
            String Url = output[1].replace("\"", "");
            return new String[]{Name, Url};
        }
        return null;
    }

    private String[] parseUPCcsv(String inCsv){
        String[] table = inCsv.split("\n");
        String[] values = table[1].split(",");
        return values;
    }

    private boolean resultsValid(String[] results){

        return results != null && results[0].length() != 0 && results[3].length() != 0;
    }
    private void sendApologies(String message){
        sendDataBack( new String[]{message, param1, "none", NOT_FOUND});
    }

    private void sendError(String message){
        sendDataBack(new String[]{message, param1, "none", SERVER_ERROR});
    }

    private void sendDataBack(String[] bookData){
        Intent messageIntent = new Intent(AddNewActivity.SERVICE_EVENT_BOOK);
        messageIntent.putExtra(AddNewActivity.SERVICE_EXTRA_BOOK,bookData);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }
}

