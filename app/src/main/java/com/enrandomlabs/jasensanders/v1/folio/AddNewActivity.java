package com.enrandomlabs.jasensanders.v1.folio;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.enrandomlabs.jasensanders.v1.folio.barcode.BarcodeActivity;
import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;
import com.enrandomlabs.jasensanders.v1.folio.services.FetchBookService;
import com.enrandomlabs.jasensanders.v1.folio.services.FetchMovieDataService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

public class AddNewActivity extends AppCompatActivity {
    //private static final String LOG_TAG = AddNewActivity.class.getSimpleName();
    private static final String ACTIVITY_NAME = "AddNewActivity";
    private static final String LOG_TAG = AddNewActivity.class.getSimpleName();

    //Instance State Keys
    private static final String CURRENT_UPC_KEY = "UPC_CURRENT";
    private static final String CURRENT_STATUS_KEY = "STATUS_CURRENT";
    private static final String CURRENT_STORE_KEY = "STORE_CURRENT";
    private static final String CURRENT_NOTES_KEY = "NOTES_CURRENT";
    private static final String CURRENT_VIEW_TYPE = "VIEW_TYPE_CURRENT";

    //Message Receiver Keys
    public static final String SERVICE_EVENT_MOVIE = "SERVICE_EVENT_MOVIE";
    public static final String SERVICE_EXTRA_MOVIE = "SERVICE_EXTRA_MOVIE";
    public static final String SERVICE_EVENT_BOOK = "SERVICE_EVENT_BOOK";
    public static final String SERVICE_EXTRA_BOOK = "SERVICE_EXTRA_BOOK";

    //Branding send to URLs
    private static final String GOOGLE_BOOKS = "https://books.google.com/";
    private static final String TMDB_SITE = "https://www.themoviedb.org/";

    //View Sates
    private static final int STATE_MOVIE = 100;
    private static final int STATE_BOOK = 300;

    //Barcode Flag
    private static final int RC_BARCODE_CAPTURE = 9001;

    //Receiver
    private BroadcastReceiver messageReceiver;

    //Error
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String SERVER_ERROR = "ERROR";

    //State variables
    private String[] movie;
    private String[] book;
    private int STATE;
    private String currentUPC;
    private String STATUS;

    //Input variables
    private EditText inputText;
    private RadioButton selectMovie;
    private RadioButton selectBook;
    private CheckBox favButton;

    //Progress bars
    private ProgressBar mProgress;

    //Views
    private TextView error;
    private LinearLayout topButtonLayout;
    private TextView title;
    private TextView byline;
    private TextView authors;
    private ImageView artImage;
    private TextView releaseDate;
    private TextView SubTextOne;
    private EditText SubTextTwo;
    private ImageView branding;
    private TextView synopsis;
    private EditText store;
    private EditText notes;
    private LinearLayout trailerScroll;
    private LinearLayout detailView;
    private LinearLayout buttonLayout;
    private LinearLayout userInput;

    private String[] TrailerArray;
    private View divider;

    private FirebaseAnalytics mFirebaseAnalytics;
    AdView mAdView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize view references
        setContentView(R.layout.activity_add_new);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        LogActionEvent(ACTIVITY_NAME, "ActivityStarted", "action");
        initializeViews();
        registerMessageReceiver();

        //If app still in session, Restore State after rotation
        if(savedInstanceState != null){
            String recover = savedInstanceState.getString(CURRENT_UPC_KEY);
            currentUPC = recover;
            if(currentUPC == null || currentUPC.length()== 0){hideViews();}
            else {
                STATUS = savedInstanceState.getString(CURRENT_STATUS_KEY);
                store.setText(savedInstanceState.getString(CURRENT_STORE_KEY));
                notes.setText(savedInstanceState.getString(CURRENT_NOTES_KEY));
                inputText.setText(currentUPC);
                STATE = savedInstanceState.getInt(CURRENT_VIEW_TYPE);
                restoreRadioButtonState(STATE);
            }
        }else {
            //otherwise hide all views and wait for user input
            hideViews();
            //Set default state
            STATE = STATE_MOVIE;
            restoreRadioButtonState(STATE);
        }
        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String upc = s.toString();
                currentUPC = upc;
                if(upc.length()>9) {
                    if (upc.length() == 12 && STATE == STATE_MOVIE) {
                        //Start the progress bar
                        if(mProgress != null) {
                            mProgress.setIndeterminate(true);
                            mProgress.setVisibility(View.VISIBLE);
                        }
                        //Launch the service
                        FetchMovieDataService.startActionFetchData(getApplicationContext(), upc);
                        return;
                    }
                    if (upc.length() != 12 && STATE == STATE_MOVIE) {
                        Toast message = Toast.makeText(getApplicationContext(), "That is not a Valid UPC", Toast.LENGTH_SHORT);
                        message.setGravity(Gravity.CENTER, 0, 0);
                        message.show();
                        return;
                    }
                    if (upc.length() == 10 || upc.length() == 13 && STATE == STATE_BOOK) {
                        //Convert ean 10 to ean 13
                        if (upc.length() == 10 && !upc.startsWith("978")) {
                            upc = "978" + upc;
                        }
                        //Start the progress bar
                        if(mProgress != null) {
                            mProgress.setIndeterminate(true);
                            mProgress.setVisibility(View.VISIBLE);
                        }
                        //Launch the Service
                        FetchBookService.startActionFetchBook(getApplicationContext(), upc);
                        return;
                    }
                    if ((upc.length() != 10 || upc.length() != 13) && STATE == STATE_BOOK) {

                        Toast message = Toast.makeText(getApplicationContext(), "That is not a Valid UPC/ISBN", Toast.LENGTH_SHORT);
                        message.setGravity(Gravity.CENTER, 0, 0);
                        message.show();
                        return;
                        
                    }

                }

            }
        });



        mAdView = (AdView) findViewById(R.id.addNewAdView);
        //mAdView.setAdUnitId(BuildConfig.ADD_NEW_AD_ID);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("F18A92219F2A59F1")  //My Nexus 5x Test Phone
                .addTestDevice("ECF8814A2889BB1528CE0F6E1BCFA7ED")  //My GS3 Test Phone
                .build();
        mAdView.loadAd(adRequest);



    }

    private void registerMessageReceiver(){

        messageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICE_EVENT_MOVIE);
        filter.addAction(SERVICE_EVENT_BOOK);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);

    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringArrayExtra(SERVICE_EXTRA_MOVIE) != null && STATE == STATE_MOVIE) {
                //Kill the progress bar
                if(mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                //Report to user with results
                String[] received = intent.getStringArrayExtra(SERVICE_EXTRA_MOVIE);
                if ((received[3].equals(NOT_FOUND)|| received[3].equals(SERVER_ERROR)) && received.length == 4) {
                    Log.e(LOG_TAG, "SendApologies Error Received "+ received[0]);
                    movieError(received);
                }else{
                    movie = received;
                    book = null;
                    if(error.getVisibility() == View.VISIBLE){
                        error.setVisibility(View.GONE);
                    }
                    //We got Data! Lets show what we got!!!
                    showViews(STATE);
                    inflateMovieViews(movie);
                }
            }
            if(intent.getStringArrayExtra(SERVICE_EXTRA_BOOK)!= null && STATE == STATE_BOOK){
                //Kill the progress bar
                if(mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                //Report to user with results
                String[] received = intent.getStringArrayExtra(SERVICE_EXTRA_BOOK);

                if ((received[3].contentEquals(NOT_FOUND)|| received[3].contentEquals(SERVER_ERROR)) && received.length == 4) {
                    bookError(received);
                }else {
                    book = received;
                    movie = null;
                    if(error.getVisibility() == View.VISIBLE){
                        error.setVisibility(View.GONE);
                    }
                    //We got Data! Lets show what we got!!!
                    showViews(STATE);
                    inflateBookViews(book);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {

            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeActivity.BarcodeObject);
                    //Update the EditText
                    inputText.setText(barcode.displayValue);

                    //LOG ALL THE THINGS!!!!
                    //Log.d(LOG_TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    //Tell the user the Scan Failed, then log event.
                    ((TextView) findViewById(R.id.error)).setText(R.string.barcode_failure);
                    ((TextView) findViewById(R.id.error)).setVisibility(View.VISIBLE);
                    //Log.d(LOG_TAG, "No barcode captured, intent data is null");


                }
            } else {
                //Tell the user the Activity Failed
                ((TextView) findViewById(R.id.error)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.error)).setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(CURRENT_UPC_KEY, currentUPC);
        savedInstanceState.putString(CURRENT_STATUS_KEY, STATUS);
        savedInstanceState.putString(CURRENT_STORE_KEY, store.getText().toString());
        savedInstanceState.putString(CURRENT_NOTES_KEY, notes.getText().toString());
        savedInstanceState.putInt(CURRENT_VIEW_TYPE, STATE);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart(){
        registerMessageReceiver();
        super.onStart();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /**Called when activity is stopped**/
    @Override
    public void onStop(){

        super.onStop();
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();

    }

    public void LaunchBarcodeScanner(View view){
        //If an error message hasn't cleared, then clear it.
        LogActionEvent(ACTIVITY_NAME, "LaunchBarcodeScanner", "action");
        if(error.getVisibility() == View.VISIBLE){
            error.setVisibility(View.GONE);
        }

        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra(BarcodeActivity.AutoFocus, true);
        intent.putExtra(BarcodeActivity.UseFlash, false);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    public void onRadioButtonClicked(View view){
        if(view.getId() == R.id.selectMovie){
            STATE = STATE_MOVIE;
        }
        if(view.getId() == R.id.selectBook){
            STATE = STATE_BOOK;
        }
    }

    public void SetWishOwnStatus(View view){
        CheckBox input = (CheckBox) view;
        LogActionEvent(ACTIVITY_NAME,"SetToWishListbutton", "action");

        if(STATE == STATE_MOVIE) {
            if (STATUS.contentEquals(DataContract.STATUS_MOVIES) && input.isChecked()) {
                STATUS = DataContract.STATUS_MOVIE_WISH;
                movie[13] = STATUS;
                Toast.makeText(this, STATUS, Toast.LENGTH_SHORT).show();
            } else {
                STATUS = DataContract.STATUS_MOVIES;
                movie[13] = STATUS;
                input.setChecked(false);
                Toast.makeText(this, STATUS, Toast.LENGTH_SHORT).show();

            }
        }
        if(STATE == STATE_BOOK){
            if (STATUS.contentEquals(DataContract.STATUS_BOOK) && input.isChecked()) {
                STATUS = DataContract.STATUS_BOOK_WISH;
                book[13] = STATUS;
                Toast.makeText(this, STATUS, Toast.LENGTH_SHORT).show();
            } else {
                STATUS = DataContract.STATUS_BOOK;
                book[13] = STATUS;
                input.setChecked(false);
                Toast.makeText(this, STATUS, Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void sendToBrand(View view){
        if(STATE == STATE_MOVIE){
            Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(TMDB_SITE));
            result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(result.resolveActivity(getPackageManager()) != null){
                startActivity(result);
            }

        }
        if(STATE == STATE_BOOK){
            Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_BOOKS));
            result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(result.resolveActivity(getPackageManager()) != null){
                startActivity(result);
            }

        }
    }

    public void SaveToDataBase(View view){


        if(STATE == STATE_MOVIE) {
            if(movie != null) {
                if (STATUS.endsWith("_WISH")) {
                    ContentValues insert = Utility.makeWishRowValues(movie);
                    Uri returned = getContentResolver().insert(DataContract.WishEntry.CONTENT_URI, insert);
                    if (returned != null) {
                        Toast.makeText(this, movie[3] + " Saved to Folio", Toast.LENGTH_LONG).show();
                    }
                    resetFields();

                } else {
                    ContentValues insert = Utility.makeMovieRowValues(movie);
                    Uri returned = getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, insert);
                    if (returned != null) {
                        Toast.makeText(this, movie[3] + " Saved to Folio", Toast.LENGTH_LONG).show();
                    }
                    resetFields();
                }
            }
        }
        if(STATE == STATE_BOOK){
            if(book != null) {
                if (STATUS.endsWith("_WISH")) {
                    ContentValues insert = Utility.makeWishRowValues(book);
                    Uri returned = getContentResolver().insert(DataContract.WishEntry.CONTENT_URI, insert);
                    if (returned != null) {
                        Toast.makeText(this, book[3] + " Saved to Folio", Toast.LENGTH_LONG).show();
                    }
                    resetFields();


                } else {
                    ContentValues insert = Utility.makeBookRowValues(book);
                    Uri returned = getContentResolver().insert(DataContract.BookEntry.CONTENT_URI, insert);
                    if (returned != null) {
                        Toast.makeText(this, book[3] + " Saved to Folio", Toast.LENGTH_LONG).show();
                    }
                    resetFields();

                }
            }
        }
    }

    public void ClearFields(View view){
        resetFields();
    }

    private void resetFields(){

        resetStoreAndNotes();
        hideViews();
        if(STATE == STATE_MOVIE) {
            clearTrailerViews();
        }
        resetInputText();
        currentUPC = null;

    }

    private void initializeViews(){


        //Error Fields
        error = (TextView) findViewById(R.id.error);
        error.setVisibility(View.GONE);

        //Input Area
        inputText = (EditText) findViewById(R.id.upc);
        selectMovie = (RadioButton) findViewById(R.id.selectMovie);
        selectBook = (RadioButton) findViewById(R.id.selectBook);

        //Progress Bar
        mProgress = (ProgressBar) findViewById(R.id.ProgressBarWait);



        //Details Area
        detailView = (LinearLayout) findViewById(R.id.details);
        topButtonLayout = (LinearLayout) findViewById(R.id.top_button_layout);
        title = (TextView) findViewById(R.id.add_view_Title);
        byline = (TextView) findViewById(R.id.add_view_byline);
        authors = (TextView) findViewById(R.id.authors);
        artImage = (ImageView) findViewById(R.id.artView);
        releaseDate = (TextView) findViewById(R.id.releaseDate);
        SubTextOne = (TextView) findViewById(R.id.subText1);
        SubTextTwo = (EditText) findViewById(R.id.subText2);
        favButton = (CheckBox) findViewById(R.id.FavButton);
        branding = (ImageView) findViewById(R.id.addNewBranding);
        synopsis = (TextView) findViewById(R.id.synopsis);
        store = (EditText) findViewById(R.id.store);
        notes = (EditText) findViewById(R.id.notes);
        trailerScroll = (LinearLayout) findViewById(R.id.trailer_scroll);
        divider = (View)findViewById(R.id.divider);
        userInput = (LinearLayout) findViewById(R.id.user_input);
        buttonLayout = (LinearLayout)findViewById(R.id.button_layout);
    }

    private void inflateMovieViews(String[] data){
        //Todo Generalize this:  ....
        //Reminder of how array is loaded for Movies
//        FullRow = new String[]{TMDB_MOVIE_ID, UPC_CODE, ART_IMAGE_URL, TITLE,
//        FORMATS, MOVIE_THUMB, BARCODE_URL, RELEASE_DATE, RUNTIME, ADD_DATE,
//                POSTER_URL, STORE, NOTES, STATUS, RATING, SYNOPSIS, TRAILERS, GENRES};
        if(data != null) {
            title.setText(data[3]);
            Glide.with(getApplicationContext()).load(data[2]).into(artImage);
            releaseDate.setText(Utility.dateToYear(data[7]));
            byline.setVisibility(View.GONE);
            authors.setVisibility(View.GONE);
            SubTextOne.setText(data[14]);
            SubTextTwo.setText(data[4]);
            branding.setImageResource(R.drawable.tmdb_brand_120_47);
            String overview = String.format(getResources().getString(R.string.overview), data[15]);
            synopsis.setText(overview);
            addTrailers(trailerScroll, data[16]);

            //ALLy content descriptions for dynamic content
            String description = data[3] + " " + data[4] + " Released " + data[7] + " " + "Rated " + data[14];
            detailView.setContentDescription(description);
            synopsis.setContentDescription("Movie overview " + overview);
            artImage.setContentDescription(data[3] + " Movie Art");
            STATUS = data[13];
        }

    }

    private void inflateBookViews(String[] data){

        //Reminder of how array is loaded for Books
//        returnVal = new String[] {bookId, ean, imgUrl, title, subtitle, desc, barcode, pubDate, authors,
//                addDate, publisher, store, notes, status, pages, categories};
        if(data != null) {
            title.setText(data[3]);
            byline.setText(data[4]);
            String author = "By " + data[8];
            authors.setText(author);
            String url = data[2];
            Glide.with(getApplicationContext()).load(url).error(R.mipmap.ic_launcher).fitCenter().into(artImage);
            releaseDate.setText(data[7]);
            SubTextOne.setText(data[10]);
            String pages = data[14] + " pages";
            SubTextTwo.setText(pages);
            SubTextTwo.setInputType(0);
            branding.setImageResource(R.drawable.google_logo);
            String overview = String.format(getResources().getString(R.string.overview), data[5]);
            synopsis.setText(overview);
            trailerScroll.setVisibility(View.INVISIBLE);

            //ALLy content descriptions for dynamic content
            String description = data[3] + " " + data[4] + " Released " + data[7] + " " + "By " + data[8];
            detailView.setContentDescription(description);
            synopsis.setContentDescription("Book overview " + overview);
            artImage.setContentDescription(data[3] + " Cover Art");
            STATUS = data[13];
        }
    }

    private void clearTrailerViews(){
        trailerScroll.removeAllViews();
        trailerScroll.invalidate();
    }

    private void hideViews(){
        error.setVisibility(View.GONE);
        detailView.setVisibility(View.INVISIBLE);
        topButtonLayout.setVisibility(View.GONE);
        title.setVisibility(View.INVISIBLE);
        artImage.setVisibility(View.INVISIBLE);
        byline.setVisibility(View.GONE);
        authors.setVisibility(View.GONE);
        releaseDate.setVisibility(View.INVISIBLE);
        SubTextOne.setVisibility(View.INVISIBLE);
        SubTextTwo.setVisibility(View.INVISIBLE);
        favButton.setVisibility(View.INVISIBLE);
        branding.setVisibility(View.INVISIBLE);
        synopsis.setVisibility(View.INVISIBLE);
        trailerScroll.setVisibility(View.INVISIBLE);
        divider.setVisibility(View.INVISIBLE);
        store.setVisibility(View.INVISIBLE);
        notes.setVisibility(View.INVISIBLE);
        userInput.setVisibility(View.INVISIBLE);
        buttonLayout.setVisibility(View.INVISIBLE);
    }

    private void showViews(int state){

        switch (state){
            case STATE_MOVIE:{
                detailView.setVisibility(View.VISIBLE);
                topButtonLayout.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                artImage.setVisibility(View.VISIBLE);
                releaseDate.setVisibility(View.VISIBLE);
                SubTextOne.setVisibility(View.VISIBLE);
                SubTextTwo.setVisibility(View.VISIBLE);
                favButton.setVisibility(View.VISIBLE);
                branding.setVisibility(View.VISIBLE);
                synopsis.setVisibility(View.VISIBLE);
                trailerScroll.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                store.setVisibility(View.VISIBLE);
                notes.setVisibility(View.VISIBLE);
                userInput.setVisibility(View.VISIBLE);
                buttonLayout.setVisibility(View.VISIBLE);
                break;
            }
            case STATE_BOOK: {
                detailView.setVisibility(View.VISIBLE);
                topButtonLayout.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                byline.setVisibility(View.VISIBLE);
                authors.setVisibility(View.VISIBLE);
                artImage.setVisibility(View.VISIBLE);
                releaseDate.setVisibility(View.VISIBLE);
                SubTextOne.setVisibility(View.VISIBLE);
                SubTextTwo.setVisibility(View.VISIBLE);
                favButton.setVisibility(View.VISIBLE);
                branding.setVisibility(View.VISIBLE);
                synopsis.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                store.setVisibility(View.VISIBLE);
                notes.setVisibility(View.VISIBLE);
                userInput.setVisibility(View.VISIBLE);
                buttonLayout.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void resetInputText(){
        inputText.setText("");
    }

    private void resetStoreAndNotes(){
        store.setText("");
        notes.setText("");
    }

    private void restoreRadioButtonState(int state){
        switch (state){
            case STATE_MOVIE:{
                STATE = STATE_MOVIE;
                selectMovie.setChecked(true);
                break;
            }
            case STATE_BOOK: {
                STATE = STATE_BOOK;
                selectBook.setChecked(true);
                break;
            }
        }
    }

    private void addTrailers(LinearLayout view, String trailers){

        LayoutInflater vi = getLayoutInflater();
        final String[] tempTrail;

        int i = 0;
        try{
            tempTrail = trailers.split(",");
            if( tempTrail.length > 0 ) {
                TrailerArray = tempTrail;

                for(String url: tempTrail){
                    View v =  vi.inflate(R.layout.trailer_tile, view, false);
                    TextView listText = (TextView) v.findViewById(R.id.list_item_trailer_text);
                    String text = "Play Trailer number"+ String.valueOf(i+1);
                    //ALLy Content description for trailers
                    v.setContentDescription(text);
                    v.setFocusable(true);
                    listText.setText(text);
                    final Uri trailerUrl = Uri.parse(url);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, trailerUrl);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }

                        }
                    });
                    view.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    i++;
                    //Log.v("AddNew: "+ String.valueOf(i), text + " : "+ trailerUrl);


                }
                return;
            }

        }catch (NullPointerException e){
            //Log.e(LOG_TAG, "Error splitting and Adding Trailers", e);
            FirebaseCrash.log("Error splitting and Adding Trailers");
            return;
        }

    }

    private void bookError(String[] response){
        //ToDo set all views below scan to gone and set error message below scan
        String message;
        if(response[3].contentEquals(NOT_FOUND)) {
            message = "Sorry, the book with ISBN: " + response[1] + " is not in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and add a different book. \n" +
                    "Manual entry is coming soon!";
            FirebaseCrash.log("Book NOT FOUND" + response[1]);
        }else{
            message = "Sorry, something went wrong in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and add a different book. \n" +
                    "Manual entry is coming soon!";
        }
        error.setText(message);
        error.setVisibility(View.VISIBLE);
    }

    private void movieError(String[] response){
        //ToDo set all views below scan to gone and set error message below scan
        String message;
        if(response[3].contentEquals(NOT_FOUND)) {
            message = "Sorry, the movie with UPC: " + response[1] + " is not in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and add a different Movie. \n" +
                    "Manual entry is coming soon!";
            FirebaseCrash.log("Movie NOT FOUND" + response[1]);
        }else{
            message = "Sorry, something went wrong in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and add a different Movie. \n" +
                    "Manual entry is coming soon!";
        }
        error.setText(message);
        error.setVisibility(View.VISIBLE);
    }

    private void LogActionEvent(String activity, String actionName, String type ){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, actionName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}


