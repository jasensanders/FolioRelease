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

package com.enrandomlabs.jasensanders.v1.folio.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.enrandomlabs.jasensanders.v1.folio.Utility;

import java.util.ArrayList;


/**
 * Created by Jasen Sanders on 10/11/2016.
 * A simple {@link ContentProvider} subclass used to query items from the database.
 *
 */

public class FolioProvider extends ContentProvider {

    public static final String ACTION_DATA_UPDATED =
            "com.enrandomlabs.jasensanders.v1.folio.ACTION_DATA_UPDATED";

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FolioDBHelper mOpenHelper;

    //For selecting all MOVIES in the DataBase and Deleting/Inserting/Updating all
    static final int MOVIE_ALL = 100;
    //For Selecting one Movie with possibly multiple items by TMDB movieID code owned list
    static final int MOVIE_BY_MID = 101;
    //For Selecting one Movie by upc code in list
    static final int MOVIE_BY_UPC = 102;

    //For selecting all WISH_LIST items and Deleting/Inserting/Updating all
    static final int WISH_LIST_ALL = 200;
    //For Selecting one ITEM in Wish lisr  with possibly multiple items by API ID code
    static final int WISH_ITEM_BY_MID = 201;
    //For Selecting one ITEM by upc code in wish list
    static final int WISH_ITEM_BY_UPC = 202;

    //For selecting all BOOKS in the DataBase and Deleting/Inserting/Updating all
    static final int BOOKS_ALL = 300;
    //For Selecting one Movie with possibly multiple items by TMDB movieID code owned list
    static final int BOOK_BY_BID = 301;
    //For Selecting one Movie by upc code in list
    static final int BOOK_BY_UPC = 302;

    static final int SEARCH_MOVIE = 1000;

    static final int SEARCH_WISHLIST = 1200;

    static final int SEARCH_BOOK = 1300;

    static final int SEARCH_ALL = 3500;



    private static final SQLiteQueryBuilder mQueryBuilder;

    private static final SQLiteQueryBuilder bQueryBuilder;

    private static final SQLiteQueryBuilder wQueryBuilder;



    static{

        //Set The Tables!!
        mQueryBuilder = new SQLiteQueryBuilder();
        mQueryBuilder.setTables(DataContract.MovieEntry.TABLE_NAME );

        bQueryBuilder = new SQLiteQueryBuilder();
        bQueryBuilder.setTables(DataContract.BookEntry.TABLE_NAME);

        wQueryBuilder = new SQLiteQueryBuilder();
        wQueryBuilder.setTables(DataContract.WishEntry.TABLE_NAME);

    }

    //Selection of individual row by Movie ID according to theMovieDataBase.org
    private static final String fMovieIdSelection =
            DataContract.MovieEntry.TABLE_NAME+
                    "." + DataContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";
    private static final String fApiIdSelection =
            DataContract.WishEntry.TABLE_NAME+
                    "." + DataContract.WishEntry.COLUMN_API_TYPE_ID + " = ? ";
    private static final String fBookIdSelection =
            DataContract.BookEntry.TABLE_NAME+
                    "." + DataContract.BookEntry.COLUMN_BOOKS_ID + " = ? ";
    private static final String fAllSelection = null;
    //Selection of a single Item by UPC code
    private static final String fUpcMoviesSelection = DataContract.MovieEntry.TABLE_NAME +
            "." + DataContract.MovieEntry.COLUMN_UPC + " = ? ";
    private static final String fUpcWishItemSelection = DataContract.WishEntry.TABLE_NAME +
            "." + DataContract.WishEntry.COLUMN_UPC + " = ? ";
    private static final String fUpcBookSelection = DataContract.BookEntry.TABLE_NAME +
            "." + DataContract.BookEntry.COLUMN_UPC + " = ? ";
    //Selection of all Items by Status OWNED or WISH
    private static final String fMoviesAll = null;//DataContract.MovieEntry.TABLE_NAME +
            //"." +"* ";
    private static final String fWishItemsAll = null;//DataContract.WishEntry.TABLE_NAME +
            //"." +"* ";
    private static final String fBooksAll = null;//DataContract.BookEntry.TABLE_NAME +
            //"." +"* ";



    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String MovieId = DataContract.MovieEntry.getMovieIdFromUri(uri);

        //By which Column?
        String selection = fMovieIdSelection;
        //Which items specifically
        String[] selectionArgs = new String[]{MovieId};

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWishItemById(Uri uri, String[] projection, String sortOrder) {
        String Id = DataContract.WishEntry.getBookIdFromUri(uri);

        //By which Column?
        String selection = fApiIdSelection;
        //Which items specifically
        String[] selectionArgs = new String[]{Id};

        return wQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getBookById(Uri uri, String[] projection, String sortOrder) {
        String BookId = DataContract.BookEntry.getBookIdFromUri(uri);

        //By which Column?
        String selection = fBookIdSelection;
        //Which items specifically
        String[] selectionArgs = new String[]{BookId};

        return bQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMoviesByUpc(Uri uri, String[] projection, String sortOrder){
        String upc = DataContract.MovieEntry.getUpcFromUri(uri);

        //By which Column?
        String selection = fUpcMoviesSelection;

        //Which Item specifically
        String[] selectionArgs = new String[]{upc};

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getWishItemByUpc(Uri uri, String[] projection, String sortOrder){
        String upc = DataContract.WishEntry.getUpcFromUri(uri);

        //By which Column?
        String selection = fUpcWishItemSelection;

        //Which Item specifically
        String[] selectionArgs = new String[]{upc};

        return wQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getBookByUpc(Uri uri, String[] projection, String sortOrder){
        String upc = DataContract.BookEntry.getUpcFromUri(uri);

        //By which Column?
        String selection = fUpcBookSelection;

        //Which Item specifically
        String[] selectionArgs = new String[]{upc};

        return bQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getAllMovies(Uri uri, String[] projection, String sortOrder){
        String selection = fMoviesAll;

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                null,
                null,
                null,
                sortOrder);
    }

    private Cursor getWishList(Uri uri, String[] projection, String sortOrder){
        String selection = fWishItemsAll;

        return wQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                null,
                null,
                null,
                sortOrder);
    }
    private Cursor getAllBooks(Uri uri, String[] projection, String sortOrder){
        String selection = fBooksAll;

        return bQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                null,
                null,
                null,
                sortOrder);
    }

    private Cursor getAll(Uri uri, String[] projection, String sortOrder) {

        return mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                fAllSelection,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor searchAll(Uri uri, String sortOrder){
        String q = uri.getLastPathSegment();
        final String query = "%"+q+"%";
        Log.e("Folio_Provider", "Query: "+query);
        Log.e("Folio_Provider", "SortOrder: "+ sortOrder);
        ArrayList<Cursor> returned = new ArrayList<Cursor>();
        Cursor m = mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                Utility.projectionSearchStateMatcher(MOVIE_ALL),
                Utility.selectionSearchStateMatcher(MOVIE_ALL),
                Utility.selectionArgsMatcher(MOVIE_ALL, query),
                null,
                null,
                sortOrder);
        if(m != null ){
            returned.add(m);
        }
        Cursor w = wQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                Utility.projectionSearchStateMatcher(WISH_LIST_ALL),
                Utility.selectionSearchStateMatcher(WISH_LIST_ALL),
                Utility.selectionArgsMatcher(WISH_LIST_ALL, query),
                null,
                null,
                sortOrder);
        if(w != null ){
            returned.add(w);
        }
        Cursor b = bQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                Utility.projectionSearchStateMatcher(BOOKS_ALL),
                Utility.selectionSearchStateMatcher(BOOKS_ALL),
                Utility.selectionArgsMatcher(BOOKS_ALL, query),
                null,
                null,
                sortOrder);
        if(b != null ){
            returned.add(b);
        }


        Cursor[] result = returned.toArray(new Cursor[returned.size()]);
        return new MergeCursor(result);


    }

    //URI matcher for states above
    static UriMatcher buildUriMatcher() {
        //Setup the Matcher
        final UriMatcher fURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        //Add Match possibilities for the states defined above
        fURIMatcher.addURI(authority,DataContract.PATH_MOVIES, MOVIE_ALL );
        fURIMatcher.addURI(authority,DataContract.PATH_WISH_LIST, WISH_LIST_ALL);
        fURIMatcher.addURI(authority,DataContract.PATH_BOOKS,BOOKS_ALL);
        fURIMatcher.addURI(authority,DataContract.PATH_MOVIES_MID + "/#",MOVIE_BY_MID );
        fURIMatcher.addURI(authority,DataContract.PATH_WISH_LIST_MID + "/#",WISH_ITEM_BY_MID );
        fURIMatcher.addURI(authority,DataContract.PATH_BOOKS_BID + "/#",BOOK_BY_BID );
        fURIMatcher.addURI(authority, DataContract.PATH_MOVIES_UPC + "/#",MOVIE_BY_UPC);
        fURIMatcher.addURI(authority, DataContract.PATH_WISH_LIST_UPC + "/#",WISH_ITEM_BY_UPC);
        fURIMatcher.addURI(authority, DataContract.PATH_BOOKS_UPC + "/#",BOOK_BY_UPC);
        fURIMatcher.addURI(authority, DataContract.PATH_MOVIE_SEARCH, SEARCH_MOVIE);
        fURIMatcher.addURI(authority, DataContract.PATH_WISHLIST_SEARCH, SEARCH_WISHLIST);
        fURIMatcher.addURI(authority, DataContract.PATH_BOOKS_SEARCH, SEARCH_BOOK);
        fURIMatcher.addURI(authority, DataContract.PATH_SEARCH_ALL + "/*", SEARCH_ALL);



        return fURIMatcher;
    }


    //Create a new FolioDbHelper for later use in onCreate
    @Override
    public boolean onCreate() {
        mOpenHelper = new FolioDBHelper(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MOVIE_BY_UPC:
                return DataContract.MovieEntry.CONTENT_ITEM_TYPE;
            case WISH_ITEM_BY_UPC:
                return DataContract.WishEntry.CONTENT_ITEM_TYPE;
            case BOOK_BY_UPC:
                return DataContract.BookEntry.CONTENT_ITEM_TYPE;
            case MOVIE_BY_MID:
                return DataContract.MovieEntry.CONTENT_TYPE;
            case WISH_ITEM_BY_MID:
                return DataContract.WishEntry.CONTENT_TYPE;
            case BOOK_BY_BID:
                return DataContract.BookEntry.CONTENT_TYPE;
            case MOVIE_ALL:
                return DataContract.MovieEntry.CONTENT_TYPE;
            case WISH_LIST_ALL:
                return DataContract.WishEntry.CONTENT_TYPE;
            case BOOKS_ALL:
                return DataContract.BookEntry.CONTENT_TYPE;
            case SEARCH_MOVIE:
                return DataContract.MovieEntry.CONTENT_TYPE;
            case SEARCH_WISHLIST:
                return DataContract.WishEntry.CONTENT_TYPE;
            case SEARCH_BOOK:
                return DataContract.BookEntry.CONTENT_TYPE;
            case SEARCH_ALL:
                return DataContract.SEARCH_CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movies/owned/mid/#"
            case MOVIE_BY_MID:
            {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            //"movies/wish/mid/#"
            case WISH_ITEM_BY_MID:
            {
                retCursor = getWishItemById(uri, projection, sortOrder);
                break;
            }
            case BOOK_BY_BID:
            {
                retCursor = getBookById(uri, projection, sortOrder);
                break;
            }
            case BOOK_BY_UPC:
            {
                retCursor = getBookByUpc(uri, projection, sortOrder);
                break;
            }
            case WISH_ITEM_BY_UPC:
            {
                retCursor = getWishItemByUpc(uri, projection, sortOrder);
                break;
            }
            case MOVIE_BY_UPC:
            {
                retCursor = getMoviesByUpc(uri, projection, sortOrder);
                break;
            }
            case BOOKS_ALL:
            {
                retCursor = getAllBooks(uri, projection, sortOrder);
                break;
            }
            case WISH_LIST_ALL: {
                retCursor = getWishList(uri, projection, sortOrder);
                break;
            }
            // "movies"
            case MOVIE_ALL: {
                retCursor = getAllMovies(uri, projection, sortOrder);
                break;
            }
            case SEARCH_MOVIE: {
                retCursor =  mQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SEARCH_WISHLIST:{
                retCursor = wQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SEARCH_BOOK: {
                retCursor = bQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case SEARCH_ALL:{

                retCursor = searchAll(uri, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        updateWidgets();
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {

            case MOVIE_ALL: {
                long _id = db.insert(DataContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DataContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri+ String.valueOf(_id));
                break;

            }
            case WISH_LIST_ALL: {
                long _id = db.insert(DataContract.WishEntry.TABLE_NAME, null, values);
                if(_id >0)
                    returnUri = DataContract.WishEntry.buildTableUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri+ String.valueOf(_id));
                break;
            }
            case BOOKS_ALL: {
                long _id = db.insert(DataContract.BookEntry.TABLE_NAME, null, values);
                if(_id >0)
                    returnUri = DataContract.BookEntry.buildBookUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri+ String.valueOf(_id));
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        updateWidgets();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MOVIE_BY_UPC:
                rowsDeleted = db.delete(
                        DataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_BY_UPC:
                rowsDeleted = db.delete(DataContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WISH_ITEM_BY_UPC:
                rowsDeleted = db.delete(DataContract.WishEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ALL:
                rowsDeleted = db.delete(
                        DataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WISH_LIST_ALL:
                rowsDeleted = db.delete(
                        DataContract.WishEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOKS_ALL:
                rowsDeleted = db.delete(
                        DataContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        //Notify content resolver and any listeners
        if(selection == null||rowsDeleted!=0){
            getContext().getContentResolver().notifyChange(uri, null);
            updateWidgets();
        }

        //rows deleted
        return rowsDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE_BY_UPC:
                rowsUpdated = db.update(DataContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case WISH_ITEM_BY_UPC:
                rowsUpdated = db.update(DataContract.WishEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case BOOK_BY_UPC:
                rowsUpdated = db.update(DataContract.BookEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            updateWidgets();
        }
        return rowsUpdated;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE_ALL:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                updateWidgets();
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }


}
