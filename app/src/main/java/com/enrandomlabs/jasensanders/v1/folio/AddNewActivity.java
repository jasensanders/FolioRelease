package com.enrandomlabs.jasensanders.v1.folio;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
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

import java.text.DateFormat;
import java.util.Calendar;

import static com.enrandomlabs.jasensanders.v1.folio.R.id.upc;

public class AddNewActivity extends AppCompatActivity {

    private static final String ACTIVITY_NAME = "AddNewActivity";
    private static final String LOG_TAG = AddNewActivity.class.getSimpleName();

    //Instance State Keys
    private static final String CURRENT_UPC_KEY = "UPC_CURRENT";
    private static final String CURRENT_STATUS_KEY = "STATUS_CURRENT";
    private static final String CURRENT_VIEW_TYPE = "VIEW_TYPE_CURRENT";
    private static final String CURRENT_ITEM_ARRAY = "CURRENT_ARRAY";
    private static final String MANUAL_ENTRY_FLAG = "MANUAL_ENTRY";

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
    //We are always in manual entry mode, but this alerts the user to it.
    private boolean manualEntry = false;

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
    private EditText title;
    private EditText byline;
    private EditText authors;
    private ImageView artImage;
    private EditText releaseDate;
    private EditText SubTextOne;
    private EditText SubTextTwo;
    private ImageView branding;
    private EditText synopsis;
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


    //TODO: REFACTOR TO USE DETAILVIEW IN A FRAGMENT instead.


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
            currentUPC = savedInstanceState.getString(CURRENT_UPC_KEY);
            if(currentUPC == null || currentUPC.length()== 0){hideViews();}
            else {
                STATUS = savedInstanceState.getString(CURRENT_STATUS_KEY);
                manualEntry = savedInstanceState.getBoolean(MANUAL_ENTRY_FLAG);
                inputText.setText(currentUPC);
                STATE = savedInstanceState.getInt(CURRENT_VIEW_TYPE);
                if(STATE == STATE_MOVIE){
                    movie = savedInstanceState.getStringArray(CURRENT_ITEM_ARRAY);
                    showViews(STATE);
                    inflateMovieViews(movie);
                }else{
                    book = savedInstanceState.getStringArray(CURRENT_ITEM_ARRAY);
                    showViews(STATE);
                    inflateBookViews(book);
                }
                restoreRadioButtonState(STATE);
            }
        }else {
            //otherwise hide all views and wait for user input
            hideViews();
            //Set default state
            STATE = STATE_MOVIE;
            manualEntry = false;
            restoreRadioButtonState(STATE);
        }
        /*inputText.addTextChangedListener(new TextWatcher() {
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
        });*/



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

        //If there isn't a messageReceiver yet make one.
        if(messageReceiver == null) {
            messageReceiver = new MessageReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICE_EVENT_MOVIE);
        filter.addAction(SERVICE_EVENT_BOOK);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);

    }
    private void unRegisterMessageReciever(){
        if(messageReceiver != null){
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
            }catch(Exception e){
                Log.e(LOG_TAG, "messageReciever unregisterd already", e);
            }
        }
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
                    currentUPC = received[1];
                    lookupError(received);
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
                    currentUPC = received[1];
                    lookupError(received);
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
        //Save recent updates
        loadStateArray();

        // Save the user's current state
        savedInstanceState.putString(CURRENT_UPC_KEY, currentUPC);
        savedInstanceState.putInt(CURRENT_VIEW_TYPE, STATE);
        savedInstanceState.putBoolean(MANUAL_ENTRY_FLAG, manualEntry);
        savedInstanceState.putString(CURRENT_STATUS_KEY, STATUS);

        if(STATE == STATE_MOVIE) {
            savedInstanceState.putStringArray(CURRENT_ITEM_ARRAY, movie);
        }else{
            savedInstanceState.putStringArray(CURRENT_ITEM_ARRAY, book);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart(){

        super.onStart();
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        unRegisterMessageReciever();
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        registerMessageReceiver();
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

    public void onSubmit(View view){
        String upc = inputText.getText().toString();
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
    //Movie Array format: String[]{TMDB_MOVIE_ID 0, UPC_CODE 1, ART_IMAGE_URL 2, TITLE 3,
    //FORMATS 4, MOVIE_THUMB 5, BARCODE_URL 6, RELEASE_DATE 7, RUNTIME 8, ADD_DATE 9,
    //POSTER_URL 10, STORE 11, NOTES 12, STATUS 13, RATING 14, SYNOPSIS 15, TRAILERS 16,
    // GENRES 17, IMDB_NUMBER 18};

    //Book Array format: String[] {bookId 0, ean 1, imgUrl 2, title 3, subtitle 4, desc 5,
    // barcode 6, pubDate 7, authors 8, addDate 9, publisher 10, store 11, notes 12,
    // status 13 , pages 14 , categories 15};

    public void setImageURL(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(AddNewActivity.this);
        final View urlInput = getLayoutInflater().inflate(R.layout.add_image_dialog, null);
        final EditText edittext = (EditText) urlInput.findViewById(R.id.dialogURL);
        alert.setMessage("Set Image");
        alert.setTitle("Enter Image URL:");
        alert.setView(urlInput);
        alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Get URL entered fro dialog
                String userURL = edittext.getText().toString();
                //Check that its a valid url
                if(Patterns.WEB_URL.matcher(userURL).matches()){
                    //Add to data array
                    if(STATE == STATE_MOVIE) {
                        movie[2] = userURL;
                        movie[10] = userURL;
                    }
                    if(STATE == STATE_BOOK){
                        book[2] = userURL;
                    }
                    //Update the ImageView
                    Glide.with(getApplicationContext()).load(userURL)
                            .fitCenter().error(R.drawable.image_placeholder).into(artImage);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                dialog.cancel();
            }
        });

        alert.create();
        alert.show();
    }
    public String[] initialStringArray(int size){
        String[] array = new String[size];
        for(int i = 0; i<array.length; i++){
            array[i] = "";
        }
        return array;
    }

    public void ManualEntry(View view){
        manualEntry = true;
        String barcode = "http://www.searchupc.com/drawupc.aspx?q=" + currentUPC;
        DateFormat df = DateFormat.getDateInstance();
        String addDate = df.format(Calendar.getInstance().getTime());
        if(STATE == STATE_MOVIE) {
            movie = initialStringArray(19);
            movie[0] = DataContract.STATUS_NOID;
            movie[1] = currentUPC;
            movie[6] = barcode;
            movie[9] =  addDate;
            movie[13] = STATUS;
            //clear error view
            clearErrorView();
            //show views
            showViews(STATE);
            inflateMovieViews(movie);
        }
        if(STATE == STATE_BOOK){
            book = initialStringArray(16);
            book[1] = currentUPC;
            book[6] = barcode;
            book[9] = addDate;
            book[13] = STATUS;
            //clear error view
            clearErrorView();
            //show views
            showViews(STATE);
            inflateBookViews(book);

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
        Intent result = null;
        if(STATE == STATE_MOVIE){
            result = new Intent(Intent.ACTION_VIEW, Uri.parse(TMDB_SITE));
        }
        if(STATE == STATE_BOOK){
            result = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_BOOKS));
        }
        if(result != null){
            result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(result.resolveActivity(getPackageManager()) != null){
                startActivity(result);
            }
        }
    }

    public void SaveToDataBase(View view){
        //loadStateArray() -- load all EditTexts into movie/book array
        loadStateArray();

        if(STATE == STATE_MOVIE && movie != null) {
            Uri returned;

            if (STATUS.endsWith("_WISH")) {
                ContentValues insert = Utility.makeWishRowValues(movie);
                 returned = getContentResolver().insert(DataContract.WishEntry.CONTENT_URI, insert);
            } else {
                ContentValues insert = Utility.makeMovieRowValues(movie);
                 returned = getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, insert);
            }
            if (returned != null) {
                Toast.makeText(this, movie[3] + " Saved to Folio", Toast.LENGTH_LONG).show();
                resetFields();
            }
        }

        if(STATE == STATE_BOOK && book != null){
            Uri returned;

            if (STATUS.endsWith("_WISH")) {
                ContentValues insert = Utility.makeWishRowValues(book);
                returned = getContentResolver().insert(DataContract.WishEntry.CONTENT_URI, insert);
            } else {
                ContentValues insert = Utility.makeBookRowValues(book);
                returned = getContentResolver().insert(DataContract.BookEntry.CONTENT_URI, insert);
            }
            if (returned != null) {
                Toast.makeText(this, book[3] + " Saved to Folio", Toast.LENGTH_LONG).show();
                resetFields();
            }
        }
    }

    public void ClearFields(View view){
        resetFields();
    }

    private void resetFields(){

        resetEditTexts();
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
        inputText = (EditText) findViewById(upc);
        selectMovie = (RadioButton) findViewById(R.id.selectMovie);
        selectBook = (RadioButton) findViewById(R.id.selectBook);

        //Progress Bar
        mProgress = (ProgressBar) findViewById(R.id.ProgressBarWait);

        //Details Area
        detailView = (LinearLayout) findViewById(R.id.details);
        topButtonLayout = (LinearLayout) findViewById(R.id.top_button_layout);
        title = (EditText) findViewById(R.id.add_view_Title);
        byline = (EditText) findViewById(R.id.add_view_byline);
        authors = (EditText) findViewById(R.id.authors);
        artImage = (ImageView) findViewById(R.id.artView);
        releaseDate = (EditText) findViewById(R.id.releaseDate);
        SubTextOne = (EditText) findViewById(R.id.subText1);
        SubTextTwo = (EditText) findViewById(R.id.subText2);
        favButton = (CheckBox) findViewById(R.id.FavButton);
        branding = (ImageView) findViewById(R.id.addNewBranding);
        synopsis = (EditText) findViewById(R.id.synopsis);
        store = (EditText) findViewById(R.id.store);
        notes = (EditText) findViewById(R.id.notes);
        trailerScroll = (LinearLayout) findViewById(R.id.trailer_scroll);
        divider = (View)findViewById(R.id.divider);
        userInput = (LinearLayout) findViewById(R.id.user_input);
        buttonLayout = (LinearLayout)findViewById(R.id.button_layout);
    }

    private void inflateMovieViews(String[] data){

        //Reminder of how array is loaded for Movies
//        String[]{TMDB_MOVIE_ID 0, UPC_CODE 1, ART_IMAGE_URL 2, TITLE 3,
//        FORMATS 4, MOVIE_THUMB 5, BARCODE_URL 6, RELEASE_DATE 7, RUNTIME 8, ADD_DATE 9,
//                POSTER_URL 10, STORE 11, NOTES 12, STATUS 13, RATING 14, SYNOPSIS 15, TRAILERS 16, GENRES 17,
//                 IMDB_NUMBER 18};
        if(data != null) {

            //Hide book related views
            byline.setVisibility(View.GONE);
            authors.setVisibility(View.GONE);

            //Set branding image
            branding.setImageResource(R.drawable.tmdb_brand_120_47);

            //Set hints according to state
            SubTextOne.setHint(R.string.NoRatingRuntime);
            SubTextTwo.setHint(R.string.NoFormats);
            //Disable EditText for movie view
            //SubTextOne.setEnabled(false);
            SubTextOne.setInputType(InputType.TYPE_NULL);

            //Set onClickListener for SubTextOne to trigger AlertDialog
            SubTextOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Create AlertDialog that allows user to input/update the rating and the runtime of movie.
                    final View ratingRuntimeDialog = getLayoutInflater().inflate(R.layout.rating_runtime_dialog, null);
                    //Get reference to Rating EditText and load data we have
                    final EditText Rating = (EditText) ratingRuntimeDialog.findViewById(R.id.dialogRating);
                    Rating.setText(movie[14]);
                    //Get reference to Runtime and load data we have
                    final EditText Runtime = (EditText) ratingRuntimeDialog.findViewById(R.id.dialogRuntime);
                    Runtime.setText(movie[8]);

                    //Make and Launch AlertDialog
                    AlertDialog.Builder alert = new AlertDialog.Builder(AddNewActivity.this);
                    alert.setTitle("Set Rating and Runtime");
                    alert.setMessage("Enter the Rating and the Runtime");
                    alert.setView(ratingRuntimeDialog);
                    alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //Get rating entered from dialog
                            String rating = Rating.getText().toString().toUpperCase();
                            if(Utility.validateRating(rating)){
                                //If valid Update Movie[] with new data
                                rating = Utility.normalizeRating(rating);
                                movie[14] = rating;
                            }else{
                                Toast.makeText(getApplicationContext(),  "Please Enter a Valid Rating Ex: PG-13", Toast.LENGTH_LONG).show();
                            }

                            //Get runtime from dialog
                            String runtime = Runtime.getText().toString();
                            runtime = runtime + " min";

                            //Update movie[] with new data
                            movie[8] = runtime;

                            //Update View with new data
                            String update = rating + runtime;
                            SubTextOne.setText(update);
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // what ever you want to do with No option.
                            dialog.cancel();
                        }
                    });
                    alert.create();
                    alert.show();

                }
            });

            //Fill in views with whatever data is available
            title.setText(data[3]);
            String url = data[2];
            if(!url.equals("")) {
                Glide.with(getApplicationContext()).load(url).fitCenter().placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder).into(artImage);
            }
            releaseDate.setText(Utility.dateToYear(data[7]));
            SubTextOne.setText(data[14]);
            SubTextTwo.setText(data[4]);
            String overview = String.format(getResources().getString(R.string.overview), data[15]);
            synopsis.setText(overview);
            addTrailers(trailerScroll, data[16]);

            //ALLy content descriptions for dynamic content
            String description = data[3] + " " + data[4] + " Released " + data[7] + " " + "Rated " + data[14];
            detailView.setContentDescription(description);
            synopsis.setContentDescription("Movie overview " + overview);
            artImage.setContentDescription(data[3] + " Movie Art");
            STATUS = data[13];
            store.setText(movie[11]);
            notes.setText(movie[12]);


        }

    }

    private void inflateBookViews(String[] data){

        //Reminder of how array is loaded for Books
//        String[] {bookId 0, ean 1, imgUrl 2, title 3, subtitle 4, desc 5, barcode 6, pubDate 7,
//                  authors 8,addDate 9 , publisher 10, store 11, notes 12, status 13, pages 14, categories 15};
        if(data != null) {

            //Hide movie related views
            trailerScroll.setVisibility(View.INVISIBLE);

            //Set hints according to state
            SubTextOne.setHint(R.string.NoPublisher);
            SubTextTwo.setHint(R.string.NoPages);

            //Set branding image
            branding.setImageResource(R.drawable.google_logo);

            //Fill in views
            title.setText(data[3]);
            byline.setText(data[4]);
            String author = "By " + data[8];
            authors.setText(author);
            String url = data[2];
            if(!url.equals("")) {
                Glide.with(getApplicationContext()).load(url).error(R.drawable.image_placeholder)
                        .placeholder(R.drawable.image_placeholder).fitCenter().into(artImage);
            }
            releaseDate.setText(data[7]);
            SubTextOne.setText(data[10]);
            String pages = data[14] + " pgs.";
            SubTextTwo.setText(pages);
            String overview = String.format(getResources().getString(R.string.overview), data[5]);
            synopsis.setText(overview);

            //ALLy content descriptions for dynamic content
            String description = data[3] + " " + data[4] + " Released " + data[7] + " " + "By " + data[8];
            detailView.setContentDescription(description);
            synopsis.setContentDescription("Book overview " + overview);
            artImage.setContentDescription(data[3] + " Cover Art");
            STATUS = data[13];
            store.setText(book[11]);
            notes.setText(book[12]);

        }
    }

    private void clearTrailerViews(){
        trailerScroll.removeAllViews();
        trailerScroll.invalidate();
    }
    private void clearErrorView(){
        error.setText("");
        error.setVisibility(View.GONE);
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

    private void resetEditTexts(){
        if(STATE == STATE_BOOK){
            byline.setText("");
            authors.setText("");
        }
        title.setText("");
        releaseDate.setText("");
        SubTextOne.setText("");
        SubTextTwo.setText("");
        synopsis.setText("");
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
        if(trailers.equals("")){
            return;
        }

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
    private void loadStateArray(){
        //Movie Array format: String[]{TMDB_MOVIE_ID 0, UPC_CODE 1, ART_IMAGE_URL 2, TITLE 3,
        //FORMATS 4, MOVIE_THUMB 5, BARCODE_URL 6, RELEASE_DATE 7, RUNTIME 8, ADD_DATE 9,
        //POSTER_URL 10, STORE 11, NOTES 12, STATUS 13, RATING 14, SYNOPSIS 15, TRAILERS 16,
        // GENRES 17, IMDB_NUMBER 18};

        //Book Array format: String[] {bookId 0, ean 1, imgUrl 2, title 3, subtitle 4, desc 5,
        // barcode 6, pubDate 7, authors 8, addDate 9, publisher 10, store 11, notes 12,
        // status 13 , pages 14 , categories 15};

        //TODO: load edit texts into correct slot in array according to state.
        //Todo ONLY UPDATE ARRAY ON SAVE!!!!!

        // if movie null then nothing to save
        if(STATE == STATE_MOVIE && movie != null){
            //ToDo: Prioritize what's in the edit text, if user changed it, then save changes, otherwise skip.
            String uTitle = title.getText().toString();
            if(movie[3]!= null &&  !movie[3].equals(uTitle)) {
                movie[3] = title.getText().toString();
            }
            if(!movie[7].equals(releaseDate.getText().toString())) {
                movie[7] = releaseDate.getText().toString();
            }
            if(!movie[4].equals(SubTextTwo.getText().toString())) {
                movie[4] = Utility.processFormats(SubTextTwo.getText().toString());
            }
            String overview = synopsis.getText().toString().replaceFirst("Overview: \n\n", "");
            if(!movie[15].equals(overview)) {

                movie[15] = overview;
            }
            movie[11] = store.getText().toString();
            movie[12] = notes.getText().toString();


        }
        // if book null then nothing to save
        if(STATE == STATE_BOOK && book != null){
            if(!book[3].equals(title.getText().toString())){
                book[3] = title.getText().toString();
            }
            if(!book[4].equals(byline.getText().toString())){
                book[4] = byline.getText().toString();
            }
            if(!book[8].equals(authors.getText().toString())){
                book[8] = authors.getText().toString();
            }
            if(!book[7].equals(releaseDate.getText().toString())){
                book[7] = releaseDate.getText().toString();
            }
            if(!book[10].equals(SubTextOne.getText().toString())) {
                book[10] = SubTextOne.getText().toString();
            }
            String pages = SubTextTwo.getText().toString().replace(" pgs.", "");
            if(!book[14].equals(pages)){
                book[14] = pages;
            }
            String bookOverview = synopsis.getText().toString().replaceFirst("Overview: \n\n", "");
            if(!book[5].equals(bookOverview)){
                book[5] = bookOverview;
            }
            book[11] = store.getText().toString();
            book[12] = notes.getText().toString();

        }
    }


    private void lookupError(String[] response){
        String message = "";
        if(STATE == STATE_MOVIE){
            message = "Sorry, the movie with UPC: " + response[1] + " is not in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and Enter manually" ;
            FirebaseCrash.log("Movie NOT FOUND" + response[1]);
        }
        if(STATE == STATE_BOOK){
            message = "Sorry, the book with ISBN: " + response[1] + " is not in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and add a different book. \n" +
                    "Manual entry is coming soon!";
            FirebaseCrash.log("Book NOT FOUND" + response[1]);
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


