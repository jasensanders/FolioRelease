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

package com.enrandomlabs.jasensanders.v1.folio;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;
import com.enrandomlabs.jasensanders.v1.folio.database.FolioDBHelper;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by Jasen Sanders on 002,03/02/16.
 * Utility file with tools for processing and saving data
 */
public class Utility {

    //For Selecting one Movie by upc code in list
    public static final int MOVIE_BY_UPC = 102;
    //For selecting all MOVIE items and Deleting/Inserting/Updating all
    public static final int MOVIE_ALL = 100;
    //For Selecting one ITEM by upc code in wish list
    public static final int WISH_ITEM_BY_UPC = 202;
    //For selecting all WISH_LIST items and Deleting/Inserting/Updating all
    public static final int WISH_LIST_ALL = 200;
    //For Selecting one Movie by upc code in list
    public static final int BOOK_BY_UPC = 302;
    //For selecting all BOOKS in the DataBase and Deleting/Inserting/Updating all
    static final int BOOKS_ALL = 300;
    //For selecting a any item type
    public static final int SEARCH_ALL = 3500;

    //Backup preference keys
    public static final String FLAG_RESTORE_BACKUP = "FOLIO_RESTORE_BACKUP";

    //First Run Flag Preference key
    public static final String FLAG_FIRST_RUN = "FOLIO_FIRST_RUN";


    private static final String TITLE = "TITLE";
    private static final String ADD_DATE = "ADD_DATE";
    private static final String RELEASE_DATE = "DATE";
    private static final String ASC = " ASC";
    private static final String DESC = " DESC";

    static public void setStringPreference(Context c, String key, String value){
        SharedPreferences settings = c.getSharedPreferences(MainActivity.MAIN_PREFS,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    static public String getStringPreference(Context c, String key, String deFault){
        SharedPreferences settings = c.getSharedPreferences(MainActivity.MAIN_PREFS, 0);

        return settings.getString(key, deFault);
    }

    public static boolean isFirstRun(Context context){
        SharedPreferences settings = context.getSharedPreferences(MainActivity.MAIN_PREFS,0);
        SharedPreferences.Editor editor = settings.edit();
        if(settings.getBoolean(FLAG_FIRST_RUN, true)){
            editor.putBoolean(FLAG_FIRST_RUN, false).commit();
            return true;
        }
        return false;
    }

    public static boolean getPrefRestoreBackup(Context context){
        SharedPreferences settings = context.getSharedPreferences(MainActivity.MAIN_PREFS, 0);

        return settings.getBoolean(FLAG_RESTORE_BACKUP, false);
    }

    public static void setPrefRestoreBackup(Context context, boolean value){
        SharedPreferences settings = context.getSharedPreferences(MainActivity.MAIN_PREFS,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(FLAG_RESTORE_BACKUP, value).commit();
    }

    public static boolean backupExists(){
        File exportDir;
        File folioDir;

        //Build backup directory path
        exportDir = Environment.getExternalStorageDirectory();
        folioDir = new File(exportDir, "/Folio");
        if(folioDir.exists()) {
            //get reference to backup file
            File backup = new File(folioDir, FolioDBHelper.getBackupDbName());
            return backup.exists();
        }
        return false;
    }
    //Checks for internet connectivity
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    //Checks if the String is numeric
    static public boolean isNum(String s) {
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    //Checks if  the string is numeric and the right length for a UPC barcode.
    static public boolean isValidUpc(String s) {
        if(isNum(s)){
            //s.length() == 10 || s.length() == 13
            if(s.length() ==12 || s.length() == 10 || s.length() == 13){
                return true;
            }
        }
        return false;
    }

    static public String[] parseProductName(String productName){
        String name = "";
        String format = "";

        //Strips Product name from String
        if(productName.contains(" (")) {
            String[] tempParens = productName.split(" \\(");
            name = tempParens[0];
        }else if(productName.contains(" [")){
            String[] tempParens = productName.split(" \\[");
            name = tempParens[0];
        }else{
            name = productName;
        }


        //Finds all formats in productName string and appends to format string
        String[] match = {"Blu-ray", "DVD", "Digital Copy", "Digital HD"};
        //Dumps all matches into arraylist
        ArrayList<String> matched = new ArrayList<>();
        for(int i = 0; i < match.length; i++){
            if(productName.contains(match[i])){
                matched.add(match[i]);
            }
        }
        //Turns arraylist into String with dashes adding to format String
        for(int i = 0; i < matched.size(); i++) {
            if (i == matched.size() - 1) {
                format = format + matched.get(i);
            } else {
                format = format + matched.get(i) + " - ";
            }
        }


        return new String[]{name, format};
    }

    public static String processFormats(String input){
        String format= "";
        //Finds all formats in productName string and appends to format string
        String[] match = {"Blu-ray","Blu-ray 3D", "DVD", "Digital Copy", "Digital HD", "4K Ultra HD"};
        //Dumps all matches into arraylist
        ArrayList<String> matched = new ArrayList<>();
        for(int i = 0; i < match.length; i++){
            if(input.contains(match[i])){
                matched.add(match[i]);
            }
        }
        //Turns arraylist into String with dashes adding to format String
        for(int i = 0; i < matched.size(); i++) {
            if (i == matched.size() - 1) {
                format = format + matched.get(i);
            } else {
                format = format + matched.get(i) + " - ";
            }
        }
        return format;
    }

    public static String stringArrayToString(String[] inString){
        StringBuilder result = new StringBuilder();
        int i = 0;
        for(String add: inString){
            if(i != inString.length -1) {
                String toAdd = add + ", ";
                result.append(toAdd);
                i++;
            }else{
                result.append(add);
            }
        }

        return result.toString();
    }

    public static String dateToYear(String date){
        if(date.equals("")){ return date;}
        String[] temp;
        try{
            temp = date.split("-");
            return temp[0];
        }catch (NullPointerException e){
            //Log.e("Utility class","Error splitting year");
            return "XXXX";
        }

    }

    public static String addDateToYear(String date){
        String[] temp;
        try{
            temp = date.split(", ");
            return temp[1];
        }catch (NullPointerException e){
            //Log.e("Utility class","Error splitting addDate year");
            return "XXXX";
        }

    }
    public static boolean validateRating(String rating){
        rating = rating.toUpperCase();
        String[] valid = new String[]{"G", "PG", "PG-13", "R", "NC-17", "X"};
        for(String rate: valid){
            if(rating.equals(rate)){
                return true;
            }
        }
        return false;
    }

    //Adds spaces to the end of the String so that the string is always of length 5
     public static String normalizeRating(String rating){
         rating = rating.toUpperCase();
        final int standard = 5;
        int len = rating.length();
        String pad = "";
        int diff = standard - len;
        if(diff <= 0){
            return rating;
        }else{
            for(int i=0; i<diff; i++){
                pad = pad + " ";
            }
            return rating + pad;
        }

    }

    public static final String[] MOVIE_COLUMNS = {
            DataContract.MovieEntry.COLUMN_UPC,
            DataContract.MovieEntry.COLUMN_DISC_ART,
            DataContract.MovieEntry.COLUMN_TITLE,
            DataContract.MovieEntry.COLUMN_ADD_DATE,
            DataContract.MovieEntry.COLUMN_FORMATS,
            DataContract.MovieEntry.COLUMN_STATUS,
            DataContract.MovieEntry.COLUMN_RATING,
    };

    public static final String[] BOOK_COLUMNS = {
            DataContract.BookEntry.COLUMN_UPC,
            DataContract.BookEntry.COLUMN_THUMB,
            DataContract.BookEntry.COLUMN_TITLE,
            DataContract.BookEntry.COLUMN_ADD_DATE,
            DataContract.BookEntry.COLUMN_SUBTITLE,
            DataContract.BookEntry.COLUMN_STATUS,
            DataContract.BookEntry.COLUMN_PAGES
    };

    public static final String[] WISHLIST_COLUMNS = {
            DataContract.WishEntry.COLUMN_UPC,
            DataContract.WishEntry.COLUMN_THUMB,
            DataContract.WishEntry.COLUMN_TITLE,
            DataContract.WishEntry.COLUMN_ADD_DATE,
            DataContract.WishEntry.COLUMN_W_FIVE,
            DataContract.WishEntry.COLUMN_STATUS,
            DataContract.WishEntry.COLUMN_W_FIFTEEN
    };



    public static ContentValues makeMovieRowValues(String[] input){

        // FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, ART_IMAGE_URL, TITLE,
        // FORMATS, MOVIE_THUMB, BARCODE_URL, RELEASE_DATE, RUNTIME, ADD_DATE, POSTER_URL, STORE,
//                NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES};

        ContentValues insertMovie = new ContentValues();
        insertMovie.put(DataContract.MovieEntry.COLUMN_MOVIE_ID, input[0]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_UPC,input[1]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_DISC_ART,input[2]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_TITLE, input[3]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_FORMATS, input[4]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_THUMB,input[5]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_BARCODE,input[6]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_DATE, input[7]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_RUNTIME,input[8]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_ADD_DATE,input[9]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_POSTERURL,input[10]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_STORE, input[11]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_NOTES, input[12]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_STATUS, input[13]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_RATING, input[14]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_SYNOPSIS, input[15]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_TRAILERS, input[16]);
        insertMovie.put(DataContract.MovieEntry.COLUMN_M_GENRE, input[17]);
        if(input.length == 19){
            insertMovie.put(DataContract.MovieEntry.COLUMN_M_IMDB, input[18]);
        }else{
            insertMovie.put(DataContract.MovieEntry.COLUMN_M_IMDB, "none");
        }
        return insertMovie;
    }

    public static ContentValues makeWishRowValues(String[] input){

        ContentValues insertWish = new ContentValues();
        insertWish.put(DataContract.WishEntry.COLUMN_API_TYPE_ID, input[0]);
        insertWish.put(DataContract.WishEntry.COLUMN_UPC,input[1]);
        insertWish.put(DataContract.WishEntry.COLUMN_THUMB,input[2]);
        insertWish.put(DataContract.WishEntry.COLUMN_TITLE, input[3]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_FIVE, input[4]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_SIX,input[5]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_SEVEN,input[6]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_DATE, input[7]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_NINE,input[8]);
        insertWish.put(DataContract.WishEntry.COLUMN_ADD_DATE,input[9]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_ELEVEN,input[10]);
        insertWish.put(DataContract.WishEntry.COLUMN_STORE, input[11]);
        insertWish.put(DataContract.WishEntry.COLUMN_NOTES, input[12]);
        insertWish.put(DataContract.WishEntry.COLUMN_STATUS, input[13]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_FIFTEEN, input[14]);
        insertWish.put(DataContract.WishEntry.COLUMN_W_SIXTEEN, input[15]);
        //If its a book
        if(input[13].startsWith("BOOK")){
            //And we have the lexile code then insert it
            if(input.length == 17){
                insertWish.put(DataContract.WishEntry.COLUMN_W_SEVENTEEN, input[16]);
            }else {
                insertWish.put(DataContract.WishEntry.COLUMN_W_SEVENTEEN, " ");
            }
            //then pass blank spaces for the unused columns
            insertWish.put(DataContract.WishEntry.COLUMN_W_EIGHTEEN, " ");
            insertWish.put(DataContract.WishEntry.COLUMN_W_NINETEEN, " ");
        }else {
            //Otherwise its a movie so insert columns
            insertWish.put(DataContract.WishEntry.COLUMN_W_SEVENTEEN, input[16]);
            insertWish.put(DataContract.WishEntry.COLUMN_W_EIGHTEEN, input[17]);
            //If we have the IMDB number then insert it
            if(input.length == 19){
                insertWish.put(DataContract.WishEntry.COLUMN_W_NINETEEN, input[18]);
            }else{
                insertWish.put(DataContract.WishEntry.COLUMN_W_NINETEEN, " ");
            }
        }
        return insertWish;
    }

    public static ContentValues makeBookRowValues(String[] input) {

        ContentValues insertBook = new ContentValues();
        insertBook.put(DataContract.BookEntry.COLUMN_BOOKS_ID, input[0]);
        insertBook.put(DataContract.BookEntry.COLUMN_UPC, input[1]);
        insertBook.put(DataContract.BookEntry.COLUMN_THUMB, input[2]);
        insertBook.put(DataContract.BookEntry.COLUMN_TITLE, input[3]);
        insertBook.put(DataContract.BookEntry.COLUMN_SUBTITLE, input[4]);
        insertBook.put(DataContract.BookEntry.COLUMN_DESC, input[5]);
        insertBook.put(DataContract.BookEntry.COLUMN_BARCODE, input[6]);
        insertBook.put(DataContract.BookEntry.COLUMN_B_DATE, input[7]);
        insertBook.put(DataContract.BookEntry.COLUMN_AUTHOR, input[8]);
        insertBook.put(DataContract.BookEntry.COLUMN_ADD_DATE, input[9]);
        insertBook.put(DataContract.BookEntry.COLUMN_PUBLISHER, input[10]);
        insertBook.put(DataContract.BookEntry.COLUMN_STORE, input[11]);
        insertBook.put(DataContract.BookEntry.COLUMN_NOTES, input[12]);
        insertBook.put(DataContract.BookEntry.COLUMN_STATUS, input[13]);
        insertBook.put(DataContract.BookEntry.COLUMN_PAGES, input[14]);
        insertBook.put(DataContract.BookEntry.COLUMN_CATEGORY, input[15]);
        //If we have the Lexile Code then insert it
        if(input.length == 17){
            insertBook.put(DataContract.BookEntry.COLUMN_CODE, input[16]);
        }else {
            insertBook.put(DataContract.BookEntry.COLUMN_CODE, " ");
        }

        return insertBook;
    }



    public static ContentValues makeUpdateValues(String store, String notes, int loader){
        ContentValues updateItem = new ContentValues();

        if(loader != 0) {
            if(loader != 202) {
                if (loader == 102) {

                    updateItem.put(DataContract.MovieEntry.COLUMN_STORE, store);
                    updateItem.put(DataContract.MovieEntry.COLUMN_NOTES, notes);
                } else {

                    updateItem.put(DataContract.BookEntry.COLUMN_STORE, store);
                    updateItem.put(DataContract.BookEntry.COLUMN_NOTES, notes);
                }
            }else{

                updateItem.put(DataContract.WishEntry.COLUMN_STORE, store);
                updateItem.put(DataContract.WishEntry.COLUMN_NOTES, notes);
            }
        }
        return updateItem;
    }

    public static int uriLoaderMatcher(Uri param){
        String uri = param.toString();
        int result;
        if(uri.contains("movie/")){
            result = 102;
        }else if (uri.contains("wish/")){
            result = 202;
        }else{
            result = 302;
        }
        return result;
    }

    public static Uri uriStateMatcher(String status, String upc){
        Uri result;
        if(status.endsWith("_WISH")){
            result = DataContract.WishEntry.buildUPCUri(upc);
        }else{

            if(status.startsWith("MOVIE")){
                result = DataContract.MovieEntry.buildUPCUri(upc);
            }else{
                result = DataContract.BookEntry.buildUPCUri(upc);
            }
        }
        return result;
    }

    public static String columnSelectionMatcher(String status){
        String result;
        if(status.endsWith("_WISH")){
            result = DataContract.WishEntry.COLUMN_UPC + " = ?";
        }else{

            if(status.startsWith("MOVIE")){
                result = DataContract.MovieEntry.COLUMN_UPC + " = ?";
            }else{
                result = DataContract.BookEntry.COLUMN_UPC + " = ?";
            }
        }
        return result;
    }

    public static Uri uriSearchStateMatcher(int LoaderType){

        if(LoaderType == MOVIE_ALL){

            return DataContract.MovieEntry.buildSearchUri();
        }
        else if(LoaderType == WISH_LIST_ALL){

            return DataContract.WishEntry.buildSearchUri();
        }
        else if(LoaderType == BOOKS_ALL) {

            return DataContract.BookEntry.buildSearchUri();

        } else{

            return null;
        }

    }

    public static String selectionSearchStateMatcher(int LoaderType){

        if(LoaderType == MOVIE_ALL){
            return DataContract.MovieEntry.COLUMN_TITLE+" LIKE ? OR " + DataContract.MovieEntry.COLUMN_RATING + " LIKE ? OR "
                    +DataContract.MovieEntry.COLUMN_FORMATS + " LIKE ? OR " + DataContract.MovieEntry.COLUMN_UPC +  " LIKE ? ";
        }
        else if(LoaderType == WISH_LIST_ALL){
            return DataContract.WishEntry.COLUMN_TITLE+" LIKE ? OR " + DataContract.WishEntry.COLUMN_UPC + " LIKE ? OR "+
                    DataContract.WishEntry.COLUMN_W_FIVE+ " LIKE ? OR " + DataContract.WishEntry.COLUMN_W_NINE + " LIKE ? OR "+
                    DataContract.WishEntry.COLUMN_W_FIFTEEN + " LIKE ? ";
        }
        else if(LoaderType == BOOKS_ALL){
            return DataContract.BookEntry.COLUMN_TITLE+" LIKE ? OR " + DataContract.BookEntry.COLUMN_SUBTITLE + " LIKE ? OR "+
                    DataContract.BookEntry.COLUMN_UPC +" LIKE ? OR " + DataContract.BookEntry.COLUMN_AUTHOR + " LIKE ? ";
        }
        else{ return null;}

    }
    public static String[] selectionArgsMatcher(int loaderType, String searchString){

        if(loaderType == WISH_LIST_ALL){
            return new String[]{searchString, searchString, searchString, searchString, searchString};
        }
        else{
            return new String[]{searchString, searchString, searchString, searchString};
        }

    }
    public static String[] projectionSearchStateMatcher(int LoaderType){

        if(LoaderType == MOVIE_ALL){
            return MOVIE_COLUMNS;
        }
        else if(LoaderType == WISH_LIST_ALL){
            return WISHLIST_COLUMNS;
        }
        else if(LoaderType == BOOKS_ALL){
            return BOOK_COLUMNS;
        }
        else{ return null;}
    }

    //URI matcher for states above
    public static UriMatcher buildDetailUriMatcher() {
        //Setup the Matcher
        final UriMatcher fURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        //Add Match possibilities for the states defined above
        fURIMatcher.addURI(authority, DataContract.PATH_MOVIES_UPC + "/#",MOVIE_BY_UPC);
        fURIMatcher.addURI(authority, DataContract.PATH_WISH_LIST_UPC + "/#",WISH_ITEM_BY_UPC);
        fURIMatcher.addURI(authority, DataContract.PATH_BOOKS_UPC + "/#",BOOK_BY_UPC);




        return fURIMatcher;
    }

    public static String sortOrderStringMatcher(int LoaderType, String column, String dir) {
        String result = null;

        switch (LoaderType){
            case MOVIE_ALL:{
                if(column.contentEquals(TITLE)) {
                    if (dir.contentEquals(ASC)){
                        result = DataContract.MovieEntry.COLUMN_TITLE + ASC;
                    }else{
                        result = DataContract.MovieEntry.COLUMN_TITLE + DESC;
                    }
                }
                else if (column.contentEquals(ADD_DATE)){
                    if (dir.contentEquals(ASC)){
                        result = DataContract.MovieEntry.COLUMN_ADD_DATE + ASC;
                    }else{
                        result = DataContract.MovieEntry.COLUMN_ADD_DATE + DESC;
                    }
                }
                else if (column.contentEquals(RELEASE_DATE)){
                    if (dir.contentEquals(ASC)){
                        result = DataContract.MovieEntry.COLUMN_M_DATE + ASC;
                    }else{
                        result = DataContract.MovieEntry.COLUMN_M_DATE + DESC;
                    }
                }
                break;
            }
            case WISH_LIST_ALL:{
                if(column.contentEquals(TITLE)) {
                    if (dir.contentEquals(ASC)){
                        result = DataContract.WishEntry.COLUMN_TITLE + ASC;
                    }else{
                        result = DataContract.WishEntry.COLUMN_TITLE + DESC;
                    }
                }
                else if (column.contentEquals(ADD_DATE)){
                    if (dir.contentEquals(ASC)){
                        result = DataContract.WishEntry.COLUMN_ADD_DATE + ASC;
                    }else{
                        result = DataContract.WishEntry.COLUMN_ADD_DATE + DESC;
                    }
                }
                else if (column.contentEquals(RELEASE_DATE)){
                    if (dir.contentEquals(ASC)){
                        result = DataContract.WishEntry.COLUMN_W_DATE + ASC;
                    }else{
                        result = DataContract.WishEntry.COLUMN_W_DATE + DESC;
                    }
                }
                break;

            }
            case BOOKS_ALL: {
                if(column.contentEquals(TITLE)) {
                    if (dir.contentEquals(ASC)){
                        result = DataContract.BookEntry.COLUMN_TITLE + ASC;
                    }else{
                        result = DataContract.BookEntry.COLUMN_TITLE + DESC;
                    }
                }
                else if (column.contentEquals(ADD_DATE)){
                    if (dir.contentEquals(ASC)){
                        result = DataContract.BookEntry.COLUMN_ADD_DATE + ASC;
                    }else{
                        result = DataContract.BookEntry.COLUMN_ADD_DATE + DESC;
                    }
                }
                else if (column.contentEquals(RELEASE_DATE)){
                    if (dir.contentEquals(ASC)){
                        result = DataContract.BookEntry.COLUMN_B_DATE + ASC;
                    }else{
                        result = DataContract.BookEntry.COLUMN_B_DATE + DESC;
                    }
                }
                break;
            }
        }
        return result;


    }

    //TODO Finish Modifying this to export Tables
//    private void exportDB(Context context, String TableName) {
//
//
//        FolioDBHelper dbhelper = new FolioDBHelper(context);
//        File exportDir;
//
//        if(Build.VERSION.SDK_INT >= 19){
//            exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//        }else{
//            exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        }
//
//        File file = new File(exportDir, "FolioExport_" + TableName +".csv");
//        try {
//            //Make sure directory exists
//            exportDir.mkdirs();
//
//            file.createNewFile();
//            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
//            SQLiteDatabase db = dbhelper.getReadableDatabase();
//            Cursor curCSV = db.rawQuery("SELECT * FROM "+ TableName, null);
//            csvWrite.writeNext(curCSV.getColumnNames());
//            while (curCSV.moveToNext()) {
//                //Which column you want to export
//                String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2)};
//                csvWrite.writeNext(arrStr);
//            }
//            csvWrite.close();
//            curCSV.close();
//        } catch (Exception sqlEx) {
//            //Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
//        }
//
//    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean backupDBtoMobileDevice(Context context){

        if(isExternalStorageWritable()) {
            try {

                File exportDir;
                File folioDir;

                //Build output directory path
                exportDir = Environment.getExternalStorageDirectory();
                folioDir = new File(exportDir, "/Folio");

                //Get Database file
                File dbFile = context.getDatabasePath(FolioDBHelper.getDbName());
                File outFile;

                //Make Sure output directory exists
                folioDir.mkdirs();

                //make outfile
                outFile = new File(folioDir, FolioDBHelper.getBackupDbName());
                outFile.createNewFile();

                //Copy data from DB to backup file.
                copyFile(dbFile, outFile);
                //Log.v("Backup Database Size", String.valueOf(dbFile.length()));
                //Log.v("New Database Size", String.valueOf(outFile.length()));

                if (dbFile.length() == outFile.length()) {

                    return true;
                }




            } catch (IOException e) {
                //Log.e("Folio_Backup", "Backup DataBase Failed", e);
                //e.printStackTrace();
                FirebaseCrash.logcat(Log.ERROR,"Folio_Backup", "Backup DataBase Failed");
            }
        }
        return false;

    }

    public static boolean restoreDBfromMobileDevice(Context context){

        if(isExternalStorageWritable()) {
            FileChannel src;
            FileChannel dst;
            try {

                File exportDir;
                File backupDir;
                //Build output directory path
                exportDir = Environment.getExternalStorageDirectory();
                backupDir = new File(exportDir, "/Folio");

                //Get Database File
                File CurrentDB = context.getDatabasePath(FolioDBHelper.getDbName());
                File BackupDB;


                //Make Sure output directory exists
                if (exportDir.exists()) {

                    //If this is a reinstall, the Database file may not exist
                    //so create it.
                    if(!CurrentDB.exists()){
                        CurrentDB.createNewFile();
                    }

                    //Get outfile
                    BackupDB = new File(backupDir, FolioDBHelper.getBackupDbName());
                    //Make sure Database files exist
                    if (BackupDB.exists()&& CurrentDB.exists()) {
                        src = new FileInputStream(BackupDB).getChannel();
                        dst = new FileOutputStream(CurrentDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        return true;

                    }
                }
                return false;

            } catch (IOException e) {
                //Log.e("Folio_Backup", "Restore from Backup DataBase Failed", e);
                //e.printStackTrace();
                FirebaseCrash.logcat(Log.ERROR,"Folio_Backup", "Restore from Backup DataBase Failed");
            }
        }
        return false;
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean isDBEmpty(Context c){

//        FolioDBHelper helper = new FolioDBHelper(c);
//        SQLiteDatabase db = helper.getReadableDatabase();
//        ArrayList<String> TableNames = new ArrayList<String>();
//        Cursor cur = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
//
//        if (cur.moveToFirst()) {
//            while ( !cur.isAfterLast() ) {
//                TableNames.add( cur.getString( cur.getColumnIndex("name")) );
//                cur.moveToNext();
//            }
//            cur.close();
//        }
        //Booleans for Tables. All must be true for database to be empty.
        boolean W = true;
        boolean M = true;
        boolean B = true;
        //boolean A = true;

        Cursor WC = c.getContentResolver().query(DataContract.WishEntry.CONTENT_URI,
                DataContract.WISH_COLUMNS, null,null, DataContract.WishEntry.COLUMN_TITLE + ASC);
        if(WC != null  ){
            if(WC.moveToFirst()) {
                W = false;
            }
            WC.close();
        }

        Cursor MC = c.getContentResolver().query(DataContract.MovieEntry.CONTENT_URI,
                DataContract.MOVIE_COLUMNS, null,null, DataContract.MovieEntry.COLUMN_TITLE + ASC);
        if(MC != null  ){
            if(MC.moveToFirst()) {
                M = false;
            }
            MC.close();
        }

        Cursor BC = c.getContentResolver().query(DataContract.BookEntry.CONTENT_URI,
                DataContract.BOOK_COLUMNS, null,null, DataContract.BookEntry.COLUMN_TITLE + ASC);
        if(BC != null  ){
            if(BC.moveToFirst()) {
                B = false;
            }
            BC.close();
        }

//        Cursor AC = c.getContentResolver().query(DataContract.AlbumEntry.CONTENT_URI,
//                DataContract.ALBUM_COLUMNS, null,null, DataContract.AlbumEntry.COLUMN_TITLE + ASC);
//        if(AC != null  ){
//            if(AC.moveToFirst()) {
//                A = false;
//            }
//            AC.close();
//        }

        return M && W && B;
    }

    public static void reStart(Activity activity){

        Intent mStartActivity = new Intent(activity, SplashActivity.class);
        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(activity, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        activity.finish();

    }


}
