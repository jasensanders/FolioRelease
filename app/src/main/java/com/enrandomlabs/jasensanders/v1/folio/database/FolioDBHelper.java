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


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.enrandomlabs.jasensanders.v1.folio.database.DataContract.AlbumEntry;
import com.enrandomlabs.jasensanders.v1.folio.database.DataContract.BookEntry;
import com.enrandomlabs.jasensanders.v1.folio.database.DataContract.MovieEntry;
import com.enrandomlabs.jasensanders.v1.folio.database.DataContract.WishEntry;


/**
 * Created by Jasen Sanders on 10/11/2016.
 * A simple {@link SQLiteOpenHelper} subclass used to create and manage the database.
 *
 */

public class FolioDBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION =1;

    private static final String DATABASE_NAME = "folioDB.db";
    private static final String BACKUP_DB_NAME = "FolioBackupDB.db";

    public FolioDBHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    //INTEGER NOT NULL,
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_UPC + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_DISC_ART + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_FORMATS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_THUMB + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_BARCODE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ADD_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_POSTERURL + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_STORE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_NOTES + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_TRAILERS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_GENRE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_M_IMDB + " TEXT NOT NULL, " +
                "UNIQUE (" + MovieEntry.COLUMN_UPC +") ON CONFLICT REPLACE"+
                " );";
        final String SQL_CREATE_WISHLIST_TABLE = "CREATE TABLE " + WishEntry.TABLE_NAME + " (" +
                WishEntry._ID + " INTEGER PRIMARY KEY," +
                WishEntry.COLUMN_API_TYPE_ID + " TEXT NOT NULL, " +
                WishEntry.COLUMN_UPC + " TEXT NOT NULL, " +
                WishEntry.COLUMN_THUMB + " TEXT NOT NULL, " +
                WishEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_FIVE + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_SIX + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_SEVEN + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_DATE + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_NINE + " TEXT NOT NULL, " +
                WishEntry.COLUMN_ADD_DATE + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_ELEVEN + " TEXT NOT NULL, " +
                WishEntry.COLUMN_STORE + " TEXT NOT NULL, " +
                WishEntry.COLUMN_NOTES + " TEXT NOT NULL, " +
                WishEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_FIFTEEN + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_SIXTEEN + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_SEVENTEEN + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_EIGHTEEN + " TEXT NOT NULL, " +
                WishEntry.COLUMN_W_NINETEEN + " TEXT NOT NULL, " +
                "UNIQUE (" + WishEntry.COLUMN_UPC +") ON CONFLICT REPLACE"+
                " );";
        final String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
                BookEntry._ID + " INTEGER PRIMARY KEY," +
                BookEntry.COLUMN_BOOKS_ID + " TEXT NOT NULL, " +
                BookEntry.COLUMN_UPC + " TEXT NOT NULL, " +
                BookEntry.COLUMN_THUMB + " TEXT NOT NULL, " +
                BookEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                BookEntry.COLUMN_SUBTITLE + " TEXT NOT NULL, " +
                BookEntry.COLUMN_DESC + " TEXT NOT NULL, " +
                BookEntry.COLUMN_BARCODE + " TEXT NOT NULL, " +
                BookEntry.COLUMN_B_DATE + " TEXT NOT NULL, " +
                BookEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                BookEntry.COLUMN_ADD_DATE + " TEXT NOT NULL, " +
                BookEntry.COLUMN_PUBLISHER + " TEXT NOT NULL, " +
                BookEntry.COLUMN_STORE + " TEXT NOT NULL, " +
                BookEntry.COLUMN_NOTES + " TEXT NOT NULL, " +
                BookEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                BookEntry.COLUMN_PAGES + " TEXT NOT NULL, " +
                BookEntry.COLUMN_CATEGORY + " TEXT NOT NULL, " +
                BookEntry.COLUMN_CODE + " TEXT NOT NULL, " +
                "UNIQUE (" + BookEntry.COLUMN_UPC +") ON CONFLICT REPLACE"+
                " );";
        final String SQL_CREATE_ALBUM_TABLE = "CREATE TABLE " + AlbumEntry.TABLE_NAME + " (" +
                AlbumEntry._ID + " INTEGER PRIMARY KEY," +
                AlbumEntry.COLUMN_ALBUM_ID + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_UPC + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_DISC_ART + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_FORMATS + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_SUBTITLE + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_BARCODE + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_A_DATE + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_ADD_DATE + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_A_POSTERURL + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_STORE + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_NOTES + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_LABEL + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_TRACKS + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_TRACK_URLS + " TEXT NOT NULL, " +
                AlbumEntry.COLUMN_A_GENRE + " TEXT NOT NULL, " +
                "UNIQUE (" + AlbumEntry.COLUMN_UPC +") ON CONFLICT REPLACE"+
                " );";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_WISHLIST_TABLE);
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
        db.execSQL(SQL_CREATE_ALBUM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static String getBackupDbName(){
        return BACKUP_DB_NAME;
    }

    public static String getDbName(){
        return DATABASE_NAME;
    }

    public void dropTables(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WishEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
    }
}
