package com.enrandomlabs.jasensanders.v1.folio.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Jasen Sanders on 10/5/2016.
 */

public class DataContract {

    public static final String CONTENT_AUTHORITY ="com.enrandomlabs.jasensanders.v1.folio";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Primitive paths
    public static final String PATH_MOVIES = "movie";
    public static final String PATH_MOVIES_UPC = "movie/upc";
    public static final String PATH_MOVIES_MID = "movie/mid";

    public static final String PATH_BOOKS = "books";
    public static final String PATH_BOOKS_UPC = "books/upc";
    public static final String PATH_BOOKS_BID = "books/bid";

    public static final String PATH_WISH_LIST = "wish";
    public static final String PATH_WISH_LIST_UPC = "wish/upc";
    public static final String PATH_WISH_LIST_MID = "wish/mid";

    public static final String PATH_ALBUMS = "albums";
    public static final String PATH_ALBUMS_UPC = "albums/upc";
    public static final String PATH_ALBUMS_AID = "albums/aid";

    //Search Paths
    public static final String PATH_MOVIE_SEARCH = "movie/search";
    public static final String PATH_BOOKS_SEARCH = "books/search";
    public static final String PATH_WISHLIST_SEARCH = "wish/search";
    public static final String PATH_ALBUMS_SEARCH = "albums/search";
    public static final String PATH_SEARCH_ALL = "search";

    //Segments
    public static final String SEG_SEARCH = "search";
    public static final String SEG_UPC = "upc";
    public static final String SEG_MID = "mid";
    public static final String SEG_BID = "bid";
    public static final String SEG_AID = "aid";
    public static final String SEG_TITLE = "title";
    public static final String SEG_YEAR = "year";


    //Status Flags
    public static final String STATUS_NOID = "000";
    public static final String STATUS_MOVIES = "MOVIE";
    public static final String STATUS_BOOK = "BOOK";
    public static final String STATUS_ALBUM = "ALBUM";
    public static final String STATUS_MOVIE_WISH = "MOVIE_WISH";
    public static final String STATUS_BOOK_WISH = "BOOK_WISH";
    public static final String STATUS_ALBUM_WISH = "ALBUM_WISH";

    //Search Content Type
    public static final String SEARCH_CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_ALL;

    //Search Content URI
    public static final Uri SEARCH_CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_ALL).build();

    public static Uri buildSearchUri(String query){
        return SEARCH_CONTENT_URI.buildUpon().appendPath(query).build();
    }


    //Movie Projection full row
    public static final String[] MOVIE_COLUMNS = {
            MovieEntry._ID,
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_UPC,
            MovieEntry.COLUMN_DISC_ART,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_FORMATS,
            MovieEntry.COLUMN_THUMB,
            MovieEntry.COLUMN_BARCODE,
            MovieEntry.COLUMN_M_DATE,
            MovieEntry.COLUMN_RUNTIME,
            MovieEntry.COLUMN_ADD_DATE,
            MovieEntry.COLUMN_M_POSTERURL,
            MovieEntry.COLUMN_STORE,
            MovieEntry.COLUMN_NOTES,
            MovieEntry.COLUMN_STATUS,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_M_SYNOPSIS,
            MovieEntry.COLUMN_M_TRAILERS,
            MovieEntry.COLUMN_M_GENRE,
            MovieEntry.COLUMN_M_IMDB
    };

    //Movie Column Indexes
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_UPC = 2;
    public static final int COL_DISC_ART = 3;
    public static final int COL_TITLE = 4;
    public static final int COL_FORMATS = 5;
    public static final int COL_THUMB = 6;
    public static final int COL_BARCODE = 7;
    public static final int COL_DATE = 8;
    public static final int COL_RUNTIME = 9;
    public static final int COL_ADD_DATE = 10;
    public static final int COL_POSTER = 11;
    public static final int COL_STORE = 12;
    public static final int COL_NOTES = 13;
    public static final int COL_STATUS = 14;
    public static final int COL_RATING = 15;
    public static final int COL_SYNOPSIS = 16;
    public static final int COL_TRAILERS = 17;
    public static final int COL_GENRES = 18;
    public static final int COL_IMDB = 19;

    public static final String[] WISH_COLUMNS = {
            WishEntry._ID,
            WishEntry.COLUMN_API_TYPE_ID,
            WishEntry.COLUMN_UPC,
            WishEntry.COLUMN_THUMB,
            WishEntry.COLUMN_TITLE,
            WishEntry.COLUMN_W_FIVE,
            WishEntry.COLUMN_W_SIX,
            WishEntry.COLUMN_W_SEVEN,
            WishEntry.COLUMN_W_DATE,
            WishEntry.COLUMN_W_NINE,
            WishEntry.COLUMN_ADD_DATE,
            WishEntry.COLUMN_W_ELEVEN,
            WishEntry.COLUMN_STORE,
            WishEntry.COLUMN_NOTES,
            WishEntry.COLUMN_STATUS,
            WishEntry.COLUMN_W_FIFTEEN,
            WishEntry.COLUMN_W_SIXTEEN,
            WishEntry.COLUMN_W_SEVENTEEN,
            WishEntry.COLUMN_W_EIGHTEEN,
            WishEntry.COLUMN_W_NINETEEN
    };

    //Wish Column Indexes
    public static final int W_COL_ID = 0;
    public static final int W_COL_API_ID = 1;
    public static final int W_COL_UPC = 2;
    public static final int W_COL_THUMB = 3;
    public static final int W_COL_TITLE = 4;
    public static final int W_COL_FIVE = 5;
    public static final int W_COL_SIX = 6;
    public static final int W_COL_SEVEN = 7;
    public static final int W_COL_DATE = 8;
    public static final int W_COL_NINE = 9;
    public static final int W_COL_ADD_DATE = 10;
    public static final int W_COL_ELEVEN = 11;
    public static final int W_COL_STORE = 12;
    public static final int W_COL_NOTES = 13;
    public static final int W_COL_STATUS = 14;
    public static final int W_COL_FIFTEEN = 15;
    public static final int W_COL_SIXTEEN = 16;
    public static final int W_COL_SEVENTEEN = 17;
    public static final int W_COL_EIGHTEEEN = 18;
    public static final int W_COL_NINETEEN = 19;

    //Books Projection full row
    public static final String[] BOOK_COLUMNS = {
            BookEntry._ID,
            BookEntry.COLUMN_BOOKS_ID,
            BookEntry.COLUMN_UPC,
            BookEntry.COLUMN_THUMB,
            BookEntry.COLUMN_TITLE,
            BookEntry.COLUMN_SUBTITLE,
            BookEntry.COLUMN_DESC,
            BookEntry.COLUMN_BARCODE,
            BookEntry.COLUMN_B_DATE,
            BookEntry.COLUMN_AUTHOR,
            BookEntry.COLUMN_ADD_DATE,
            BookEntry.COLUMN_PUBLISHER,
            BookEntry.COLUMN_STORE,
            BookEntry.COLUMN_NOTES,
            BookEntry.COLUMN_STATUS,
            BookEntry.COLUMN_PAGES,
            BookEntry.COLUMN_CATEGORY,
            BookEntry.COLUMN_CODE
    };

    //Books Column Indexes
    public static final int B_COL_ID = 0;
    public static final int B_COL_BOOKS_ID = 1;
    public static final int B_COL_UPC = 2;
    public static final int B_COL_THUMB = 3;
    public static final int B_COL_TITLE = 4;
    public static final int B_COL_SUBTITLE = 5;
    public static final int B_COL_DESC = 6;
    public static final int B_COL_BARCODE = 7;
    public static final int B_COL_DATE = 8;
    public static final int B_COL_AUTHOR = 9;
    public static final int B_COL_ADD_DATE = 10;
    public static final int B_COL_PUB = 11;
    public static final int B_COL_STORE = 12;
    public static final int B_COL_NOTES = 13;
    public static final int B_COL_STATUS = 14;
    public static final int B_COL_PAGES = 15;
    public static final int B_COL_CATEGORY = 16;
    public static final int B_COL_CODE = 17;

    //Album Projection full row
    public static final String[] ALBUM_COLUMNS = {
            AlbumEntry._ID,
            AlbumEntry.COLUMN_ALBUM_ID,
            AlbumEntry.COLUMN_UPC,
            AlbumEntry.COLUMN_DISC_ART,
            AlbumEntry.COLUMN_TITLE,
            AlbumEntry.COLUMN_FORMATS,
            AlbumEntry.COLUMN_SUBTITLE,
            AlbumEntry.COLUMN_BARCODE,
            AlbumEntry.COLUMN_A_DATE,
            AlbumEntry.COLUMN_RUNTIME,
            AlbumEntry.COLUMN_ADD_DATE,
            AlbumEntry.COLUMN_A_POSTERURL,
            AlbumEntry.COLUMN_STORE,
            AlbumEntry.COLUMN_NOTES,
            AlbumEntry.COLUMN_STATUS,
            AlbumEntry.COLUMN_LABEL,
            AlbumEntry.COLUMN_TRACKS,
            AlbumEntry.COLUMN_TRACK_URLS,
            AlbumEntry.COLUMN_A_GENRE

    };

    //Album Column Indexes
    public static final int A_COL_ID = 0;
    public static final int A_COL_ALBUM_ID = 1;
    public static final int A_COL_UPC = 2;
    public static final int A_COL_DISC_ART = 3;
    public static final int A_COL_TITLE = 4;
    public static final int A_COL_FORMATS = 5;
    public static final int A_COL_SUBTITLE = 6;
    public static final int A_COL_BARCODE = 7;
    public static final int A_COL_DATE = 8;
    public static final int A_COL_RUNTIME = 9;
    public static final int A_COL_ADD_DATE = 10;
    public static final int A_COL_POSTER = 11;
    public static final int A_COL_STORE = 12;
    public static final int A_COL_NOTES = 13;
    public static final int A_COL_STATUS = 14;
    public static final int A_COL_LABEL = 15;
    public static final int A_COL_TRACKS = 16;
    public static final int A_COL_TRACK_URLS = 17;
    public static final int A_COL_GENRES = 18;



    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";

        // The MOVIE_ID setting string is what will be sent to themoviedatabase.org
        // as the review and trailers.

        //TMDB movie ID number as a string
        public static final String COLUMN_MOVIE_ID = "API_ID";
        //String UPC code for disc
        public static final String COLUMN_UPC = "UPC";
        //Url for the thumb image
        public static final String COLUMN_DISC_ART = "U_DISC_ART";
        //String title of movie
        public static final String COLUMN_TITLE = "TITLE";
        //Movie formats included
        public static final String COLUMN_FORMATS = "U_FORMATS";
        //Url Movie disc package art
        public static final String COLUMN_THUMB = "THUMB";
        //Url for the upc barcode image
        public static final String COLUMN_BARCODE = "U_BARCODE";
        //Movie Release Date String in the format yyyy-mm-dd
        public static final String COLUMN_M_DATE = "DATE";
        //String movie runtime in min
        public static final String COLUMN_RUNTIME = "M_RUNTIME";
        //Date Movie Was added to database as a string
        public static final String COLUMN_ADD_DATE = "ADD_DATE";
        //Url for the poster image
        public static final String COLUMN_M_POSTERURL = "M_POSTERURL";
        //String Store Movie was purchased from (user input)
        public static final String COLUMN_STORE = "STORE";
        //String of notes about movie 140 char long (user input)
        public static final String COLUMN_NOTES = "NOTES";
        //String status of movie either WISH or OWN
        public static final String COLUMN_STATUS = "STATUS";
        //Movie Rating ie. "PG-13"
        public static final String COLUMN_RATING = "M_RATING";
        //String of the Short synopsis
        public static final String COLUMN_M_SYNOPSIS = "SYNOPSIS";
        //String of Trailer youtube urls. comma separated.
        public static final String COLUMN_M_TRAILERS = "M_TRAILERS";
        //String of movie/tv genres. comma separated.
        public static final String COLUMN_M_GENRE = "M_GENRE";
        //String imdb number
        public static final String COLUMN_M_IMDB = "M_IMDB";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        //Build URI based on row_id of movie in database
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        //Build URI based on MOVIE_ID for Owned
        public static Uri buildMovieIdUri(String Movie_Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_MID)
                    .appendPath(Movie_Id).build();
        }

        //Build URI based on UPC for Owned
        public static Uri buildUPCUri(String Upc_Code){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_UPC)
                    .appendPath(Upc_Code).build();
        }

        //Build Uri for all owned Items
        public static Uri buildUriAll() {
            return CONTENT_URI;
        }

        public static Uri buildSearchUri(){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_SEARCH).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getUpcFromUri(Uri uri){return uri.getLastPathSegment();}


    }

    public static final class BookEntry implements BaseColumns{
        public static final String TABLE_NAME = "books";


        //String Google Books ID
        public static final String COLUMN_BOOKS_ID = "API_ID";
        //String UPC (ISBN) number
        public static final String COLUMN_UPC = "UPC";
        //String thumb image URL
        public static final String COLUMN_THUMB = "THUMB";
        //String Book Title
        public static final String COLUMN_TITLE = "TITLE";
        //String Sub-title
        public static final String COLUMN_SUBTITLE = "SUBTITLE";
        //String Overview of book.
        public static final String COLUMN_DESC = "SYNOPSIS";
        //String barcode URL
        public static final String COLUMN_BARCODE = "U_BARCODE";
        //Publish date
        public static final String COLUMN_B_DATE = "DATE";
        //String All authors (comma separated)
        public static final String COLUMN_AUTHOR = "AUTHORS";
        //Date Book Was added to database as a string
        public static final String COLUMN_ADD_DATE = "ADD_DATE";
        //String name of publisher
        public static final String COLUMN_PUBLISHER = "PUBLISHER";
        //String Store that Book was purchased from (user input)
        public static final String COLUMN_STORE = "STORE";
        //String of notes about book 140 char long (user input)
        public static final String COLUMN_NOTES = "NOTES";
        //String status of row (book, movie or CD)
        public static final String COLUMN_STATUS = "STATUS";
        //String number of pages
        public static final String COLUMN_PAGES = "PAGES";
        //String book catagories (comma separated)
        public static final String COLUMN_CATEGORY = "CATEGORY";
        //String reader level code
        public static final String COLUMN_CODE = "LEXILE";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //Build URI based on row_id of movie in database
        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildBookIdUri(String Book_Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_BID)
                    .appendPath(Book_Id).build();
        }

        //Build URI based on UPC for Owned
        public static Uri buildUPCUri(String ISBN_Upc_Code){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_UPC)
                    .appendPath(ISBN_Upc_Code).build();
        }

        //Build Uri for all owned Items
        public static Uri buildUriAll(){ return CONTENT_URI; }

        public static Uri buildSearchUri(){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_SEARCH).build();
        }

        public static String getBookIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getUpcFromUri(Uri uri){return uri.getLastPathSegment();}


    }

    public static final class WishEntry implements BaseColumns{
        public static final String TABLE_NAME = "wishlist";

        //TMDB movie ID number as a string
        public static final String COLUMN_API_TYPE_ID = "API_ID";
        //String UPC code for disc
        public static final String COLUMN_UPC = "UPC";
        //Url for the thumb image
        public static final String COLUMN_THUMB = "THUMB";
        //Url for the poster image
        public static final String COLUMN_TITLE = "TITLE";
        //Url Movie disc package art
        public static final String COLUMN_W_FIVE = "W_FIVE";
        //Url for the upc barcode image
        public static final String COLUMN_W_SIX = "W_SIX";
        //String title of movie
        public static final String COLUMN_W_SEVEN = "W_SEVEN";
        //Movie Release Date String in the format yyyy-mm-dd
        public static final String COLUMN_W_DATE = "DATE";
        //String movie runtime in min
        public static final String COLUMN_W_NINE = "W_NINE";
        //Date Movie Was added to database as a string
        public static final String COLUMN_ADD_DATE = "ADD_DATE";
        //Movie formats included
        public static final String COLUMN_W_ELEVEN = "W_ELEVEN";
        //String Store Movie was purchased from (user input)
        public static final String COLUMN_STORE = "STORE";
        //String of notes about movie 140 char long (user input)
        public static final String COLUMN_NOTES = "NOTES";
        //String status of movie either WISH or OWN
        public static final String COLUMN_STATUS = "STATUS";
        //Movie Rating ie. "PG-13"
        public static final String COLUMN_W_FIFTEEN = "W_FIFTEEN";
        //String of the Short synopsis
        public static final String COLUMN_W_SIXTEEN = "W_SIXTEEN";
        //String of Trailer youtube urls. comma separated.
        public static final String COLUMN_W_SEVENTEEN = "W_SEVENTEEN";
        //String of movie/tv genres. comma separated.
        public static final String COLUMN_W_EIGHTEEN = "W_EIGHTEEN";
        //String imdb number
        public static final String COLUMN_W_NINETEEN = "W_NINETEEN";


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WISH_LIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WISH_LIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WISH_LIST;

        public static Uri buildTableUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildApiIdUri(String API_Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_BID)
                    .appendPath(API_Id).build();
        }

        //Build URI based on UPC for Owned
        public static Uri buildUPCUri(String UPC){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_UPC)
                    .appendPath(UPC).build();
        }

        //Build Uri for all owned Items
        public static Uri buildUriAll(){
            return CONTENT_URI;
        }

        public static Uri buildSearchUri(){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_SEARCH).build();
        }

        public static String getBookIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getUpcFromUri(Uri uri){return uri.getLastPathSegment();}
    }

    public static final class AlbumEntry implements BaseColumns {
        public static final String TABLE_NAME = "albums";

        //Album ID number as a string
        public static final String COLUMN_ALBUM_ID = "API_ID";
        //String UPC code for disc
        public static final String COLUMN_UPC = "UPC";
        //Url for the thumb image
        public static final String COLUMN_DISC_ART = "U_DISC_ART";
        //String title of album
        public static final String COLUMN_TITLE = "TITLE";
        //Album format
        public static final String COLUMN_FORMATS = "U_FORMATS";
        //Subtitle
        public static final String COLUMN_SUBTITLE = "SUBTITLE";
        //Url for the upc barcode image
        public static final String COLUMN_BARCODE = "U_BARCODE";
        //Album Release Date String in the format yyyy-mm-dd
        public static final String COLUMN_A_DATE = "DATE";
        //String album runtime in min
        public static final String COLUMN_RUNTIME = "A_RUNTIME";
        //Date Album Was added to database as a string
        public static final String COLUMN_ADD_DATE = "ADD_DATE";
        //Url for the poster image
        public static final String COLUMN_A_POSTERURL = "A_POSTERURL";
        //String Store Album was purchased from (user input)
        public static final String COLUMN_STORE = "STORE";
        //String of notes about album 140 char long (user input)
        public static final String COLUMN_NOTES = "NOTES";
        //String status of album either WISH or OWN
        public static final String COLUMN_STATUS = "STATUS";
        //Album label
        public static final String COLUMN_LABEL = "LABEL";
        //String comma seperated track names
        public static final String COLUMN_TRACKS = "TRACKS";
        //String of Track urls. comma separated.
        public static final String COLUMN_TRACK_URLS = "TRACK_URLS";
        //String of album genres. comma separated.
        public static final String COLUMN_A_GENRE = "A_GENRE";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALBUMS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALBUMS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ALBUMS;


        //Build URI based on row_id of album in database
        public static Uri buildAlbumUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        //Build URI based on ALBUM_ID for Owned
        public static Uri buildAlbumIdUri(String Album_Id) {

            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_AID)
                    .appendPath(Album_Id).build();
        }

        //Build URI based on UPC
        public static Uri buildUPCUri(String Upc_Code){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_UPC)
                    .appendPath(Upc_Code).build();
        }

        //Build Uri for all owned Items
        public static Uri buildUriAll() {
            return CONTENT_URI;
        }

        public static Uri buildSearchUri(){
            return CONTENT_URI.buildUpon()
                    .appendPath(SEG_SEARCH).build();
        }

        public static String getAlbumIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getUpcFromUri(Uri uri){return uri.getLastPathSegment();}
    }
}
