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
import java.util.ArrayList;
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

    //View States
    private static final int STATE_MOVIE = 100;
    private static final int STATE_BOOK = 300;

    //Barcode Flag
    private static final int RC_BARCODE_CAPTURE = 9001;

    //Error
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String SERVER_ERROR = "ERROR";

    //Receiver
    private BroadcastReceiver mMessageReceiver;

    //State variables
    private String[] mMovie;
    private String[] mBook;
    private int mState;
    private String mStatus;
    private String mCurrentUpc;

    //We are always in manual entry mode, but this initializes a data array.
    private boolean mManualEntry = false;

    //Input variables
    private EditText mInputText;
    private RadioButton mSelectMovie;
    private RadioButton mSelectBook;
    private CheckBox mFavButton;
    private ArrayList<Integer> mSelectedItems;

    //Progress bars
    private ProgressBar mProgress;

    //Views
    private TextView mError;
    private LinearLayout mTopButtonLayout;
    private EditText mTitle;
    private EditText mByline;
    private EditText mAuthors;
    private ImageView mArtImage;
    private EditText mReleaseDate;
    private EditText mSubTextOne;
    private EditText mSubTextTwo;
    private ImageView mBranding;
    private EditText mSynopsis;
    private EditText mStore;
    private EditText mNotes;
    private LinearLayout mTrailerScroll;
    private LinearLayout mDetailView;
    private LinearLayout mButtonLayout;
    private LinearLayout mUserInput;

    private String[] mTrailerArray;
    private View mDivider;

    private FirebaseAnalytics mFirebaseAnalytics;
    AdView mAdView;


    //TODO: REFACTOR TO USE DETAILVIEW IN A FRAGMENT instead.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize view references
        setContentView(R.layout.activity_add_new);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        logActionEvent(ACTIVITY_NAME, "ActivityStarted", "action");
        initializeViews();
        registerMessageReceiver();

        //If app still in session, Restore State after rotation
        if(savedInstanceState != null){
            mCurrentUpc = savedInstanceState.getString(CURRENT_UPC_KEY);
            if(mCurrentUpc == null || mCurrentUpc.length()== 0){hideViews();}
            else {
                mStatus = savedInstanceState.getString(CURRENT_STATUS_KEY);
                mManualEntry = savedInstanceState.getBoolean(MANUAL_ENTRY_FLAG);
                mInputText.setText(mCurrentUpc);
                mState = savedInstanceState.getInt(CURRENT_VIEW_TYPE);
                if(mState == STATE_MOVIE){
                    mMovie = savedInstanceState.getStringArray(CURRENT_ITEM_ARRAY);
                    showViews(mState);
                    inflateMovieViews(mMovie);
                }else{
                    mBook = savedInstanceState.getStringArray(CURRENT_ITEM_ARRAY);
                    showViews(mState);
                    inflateBookViews(mBook);
                }
                restoreRadioButtonState(mState);
            }
        }else {
            //otherwise hide all views and wait for user input
            hideViews();
            //Set default state
            mState = STATE_MOVIE;
            mManualEntry = false;
            restoreRadioButtonState(mState);
        }
        /*mInputText.addTextChangedListener(new TextWatcher() {
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
                    if (upc.length() == 12 && state == STATE_MOVIE) {
                        //Start the progress bar
                        if(mProgress != null) {
                            mProgress.setIndeterminate(true);
                            mProgress.setVisibility(View.VISIBLE);
                        }
                        //Launch the service
                        FetchMovieDataService.startActionFetchData(getApplicationContext(), upc);
                        return;
                    }
                    if (upc.length() != 12 && state == STATE_MOVIE) {
                        Toast message = Toast.makeText(getApplicationContext(), "That is not a Valid UPC", Toast.LENGTH_SHORT);
                        message.setGravity(Gravity.CENTER, 0, 0);
                        message.show();
                        return;
                    }
                    if (upc.length() == 10 || upc.length() == 13 && state == STATE_BOOK) {
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
                    if ((upc.length() != 10 || upc.length() != 13) && state == STATE_BOOK) {

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
        if(mMessageReceiver == null) {
            mMessageReceiver = new MessageReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICE_EVENT_MOVIE);
        filter.addAction(SERVICE_EVENT_BOOK);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);

    }
    private void unRegisterMessageReciever(){
        if(mMessageReceiver != null){
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            }catch(Exception e){
                Log.e(LOG_TAG, "messageReciever unregisterd already", e);
            }
        }
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringArrayExtra(SERVICE_EXTRA_MOVIE) != null && mState == STATE_MOVIE) {
                //Kill the progress bar
                if(mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                //Report to user with results
                String[] received = intent.getStringArrayExtra(SERVICE_EXTRA_MOVIE);
                if ((received[3].equals(NOT_FOUND)|| received[3].equals(SERVER_ERROR)) && received.length == 4) {
                    Log.e(LOG_TAG, "SendApologies Error Received "+ received[0]);
                    mCurrentUpc = received[1];
                    lookupError(received);
                }else{
                    mMovie = received;
                    mBook = null;
                    if(mError.getVisibility() == View.VISIBLE){
                        mError.setVisibility(View.GONE);
                    }
                    //We got Data! Lets show what we got!!!
                    showViews(mState);
                    inflateMovieViews(mMovie);
                }
            }
            if(intent.getStringArrayExtra(SERVICE_EXTRA_BOOK)!= null && mState == STATE_BOOK){
                //Kill the progress bar
                if(mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                //Report to user with results
                String[] received = intent.getStringArrayExtra(SERVICE_EXTRA_BOOK);

                if ((received[3].contentEquals(NOT_FOUND)|| received[3].contentEquals(SERVER_ERROR)) && received.length == 4) {
                    mCurrentUpc = received[1];
                    lookupError(received);
                }else {
                    mBook = received;
                    mMovie = null;
                    if(mError.getVisibility() == View.VISIBLE){
                        mError.setVisibility(View.GONE);
                    }
                    //We got Data! Lets show what we got!!!
                    showViews(mState);
                    inflateBookViews(mBook);
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
                    mInputText.setText(barcode.displayValue);

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
        savedInstanceState.putString(CURRENT_UPC_KEY, mCurrentUpc);
        savedInstanceState.putInt(CURRENT_VIEW_TYPE, mState);
        savedInstanceState.putBoolean(MANUAL_ENTRY_FLAG, mManualEntry);
        savedInstanceState.putString(CURRENT_STATUS_KEY, mStatus);

        if(mState == STATE_MOVIE) {
            savedInstanceState.putStringArray(CURRENT_ITEM_ARRAY, mMovie);
        }else{
            savedInstanceState.putStringArray(CURRENT_ITEM_ARRAY, mBook);
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

    public void launchBarcodeScanner(View view){
        //If an error message hasn't cleared, then clear it.
        logActionEvent(ACTIVITY_NAME, "launchBarcodeScanner", "action");
        if(mError.getVisibility() == View.VISIBLE){
            mError.setVisibility(View.GONE);
        }

        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra(BarcodeActivity.AutoFocus, true);
        intent.putExtra(BarcodeActivity.UseFlash, false);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    public void onRadioButtonClicked(View view){
        if(view.getId() == R.id.selectMovie){
            mState = STATE_MOVIE;
        }
        if(view.getId() == R.id.selectBook){
            mState = STATE_BOOK;
        }
    }

    public void onSubmit(View view){
        String upc = mInputText.getText().toString();
        mCurrentUpc = upc;
        if(upc.length()>9) {
            if (upc.length() == 12 && mState == STATE_MOVIE) {
                //Start the progress bar
                if(mProgress != null) {
                    mProgress.setIndeterminate(true);
                    mProgress.setVisibility(View.VISIBLE);
                }
                //Launch the service
                FetchMovieDataService.startActionFetchData(getApplicationContext(), upc);
                return;
            }
            if (upc.length() != 12 && mState == STATE_MOVIE) {
                Toast message = Toast.makeText(getApplicationContext(), "That is not a Valid UPC", Toast.LENGTH_SHORT);
                message.setGravity(Gravity.CENTER, 0, 0);
                message.show();
                return;
            }
            if (upc.length() == 10 || upc.length() == 13 && mState == STATE_BOOK) {
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
            if ((upc.length() != 10 || upc.length() != 13) && mState == STATE_BOOK) {

                Toast message = Toast.makeText(getApplicationContext(), "That is not a Valid UPC/ISBN", Toast.LENGTH_SHORT);
                message.setGravity(Gravity.CENTER, 0, 0);
                message.show();
                return;

            }

        }

    }
    //Movie Array format: String[]{TMDB_MOVIE_ID 0, UPC_CODE 1, ART_IMAGE_URL 2, TITLE 3,
    //FORMATS 4, MOVIE_THUMB 5, BARCODE_URL 6, RELEASE_DATE 7, RUNTIME 8, ADD_DATE 9,
    //POSTER_URL 10, STORE 11, NOTES 12, status 13, RATING 14, SYNOPSIS 15, TRAILERS 16,
    // GENRES 17, IMDB_NUMBER 18};

    //Book Array format: String[] {bookId 0, ean 1, imgUrl 2, title 3, subtitle 4, desc 5,
    // barcode 6, pubDate 7, authors 8, addDate 9, publisher 10, store 11, notes 12,
    // status 13 , pages 14 , categories 15};

    public void setImageURL(View view){

        imageDialog();
    }



    public void manualEntry(View view){
        mManualEntry = true;
        String barcode = "http://www.searchupc.com/drawupc.aspx?q=" + mCurrentUpc;
        DateFormat df = DateFormat.getDateInstance();
        String addDate = df.format(Calendar.getInstance().getTime());
        if(mState == STATE_MOVIE) {
            //Initialize movie array
            mMovie = initialStringArray(19);
            mMovie[0] = DataContract.STATUS_NOID;
            mMovie[1] = mCurrentUpc;
            mMovie[6] = barcode;
            mMovie[9] =  addDate;
            mMovie[13] = DataContract.STATUS_MOVIES;
            //Clear error view
            clearErrorView();
            //Show views
            showViews(mState);
            inflateMovieViews(mMovie);
        }
        if(mState == STATE_BOOK){
            //Initialize book array
            mBook = initialStringArray(16);
            mBook[0]= DataContract.STATUS_NOID;
            mBook[1] = mCurrentUpc;
            mBook[6] = barcode;
            mBook[9] = addDate;
            mBook[13] = DataContract.STATUS_BOOK;
            //Clear error view
            clearErrorView();
            //Show views
            showViews(mState);
            inflateBookViews(mBook);

        }
    }

    public void setWishOwnStatus(View view){
        CheckBox input = (CheckBox) view;
        logActionEvent(ACTIVITY_NAME,"SetToWishListbutton", "action");

        if(mState == STATE_MOVIE) {
            if (mStatus.contentEquals(DataContract.STATUS_MOVIES) && input.isChecked()) {
                mStatus = DataContract.STATUS_MOVIE_WISH;
                mMovie[13] = mStatus;
                Toast.makeText(this, mStatus, Toast.LENGTH_SHORT).show();
            } else {
                mStatus = DataContract.STATUS_MOVIES;
                mMovie[13] = mStatus;
                input.setChecked(false);
                Toast.makeText(this, mStatus, Toast.LENGTH_SHORT).show();

            }
        }
        if(mState == STATE_BOOK){
            if (mStatus.contentEquals(DataContract.STATUS_BOOK) && input.isChecked()) {
                mStatus = DataContract.STATUS_BOOK_WISH;
                mBook[13] = mStatus;
                Toast.makeText(this, mStatus, Toast.LENGTH_SHORT).show();
            } else {
                mStatus = DataContract.STATUS_BOOK;
                mBook[13] = mStatus;
                input.setChecked(false);
                Toast.makeText(this, mStatus, Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void sendToBrand(View view){
        Intent result = null;
        if(mState == STATE_MOVIE){
            result = new Intent(Intent.ACTION_VIEW, Uri.parse(TMDB_SITE));
        }
        if(mState == STATE_BOOK){
            result = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_BOOKS));
        }
        if(result != null){
            result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(result.resolveActivity(getPackageManager()) != null){
                startActivity(result);
            }
        }
    }

    public void saveToDataBase(View view){
        //loadStateArray() -- load all EditTexts into movie/book array
        loadStateArray();

        if(mState == STATE_MOVIE && mMovie != null) {
            Uri returned;

            if (mStatus.endsWith("_WISH")) {
                ContentValues insert = Utility.makeWishRowValues(mMovie);
                 returned = getContentResolver().insert(DataContract.WishEntry.CONTENT_URI, insert);
            } else {
                ContentValues insert = Utility.makeMovieRowValues(mMovie);
                 returned = getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, insert);
            }
            if (returned != null) {
                Toast.makeText(this, mMovie[3] + getString(R.string.itemSaved), Toast.LENGTH_LONG).show();
                resetFields();
            }
        }

        if(mState == STATE_BOOK && mBook != null){
            Uri returned;

            if (mStatus.endsWith("_WISH")) {
                ContentValues insert = Utility.makeWishRowValues(mBook);
                returned = getContentResolver().insert(DataContract.WishEntry.CONTENT_URI, insert);
            } else {
                ContentValues insert = Utility.makeBookRowValues(mBook);
                returned = getContentResolver().insert(DataContract.BookEntry.CONTENT_URI, insert);
            }
            if (returned != null) {
                Toast.makeText(this, mBook[3] + getString(R.string.itemSaved), Toast.LENGTH_LONG).show();
                resetFields();
            }
        }
    }

    public void clearFields(View view){
        resetFields();
    }

    private void resetFields(){

        resetEditTexts();
        hideViews();
        if(mState == STATE_MOVIE) {
            clearTrailerViews();
        }
        resetInputText();
        mCurrentUpc = null;
    }

    private void initializeViews(){

        //Error Field
        mError = (TextView) findViewById(R.id.error);
        mError.setVisibility(View.GONE);

        //Input Area
        mInputText = (EditText) findViewById(upc);
        mSelectMovie = (RadioButton) findViewById(R.id.selectMovie);
        mSelectBook = (RadioButton) findViewById(R.id.selectBook);

        //Progress Bar
        mProgress = (ProgressBar) findViewById(R.id.ProgressBarWait);

        //Details Area
        mDetailView = (LinearLayout) findViewById(R.id.details);
        mTopButtonLayout = (LinearLayout) findViewById(R.id.top_button_layout);
        mTitle = (EditText) findViewById(R.id.add_view_Title);
        mByline = (EditText) findViewById(R.id.add_view_byline);
        mAuthors = (EditText) findViewById(R.id.authors);
        mArtImage = (ImageView) findViewById(R.id.artView);
        mReleaseDate = (EditText) findViewById(R.id.releaseDate);
        mSubTextOne = (EditText) findViewById(R.id.subText1);
        mSubTextTwo = (EditText) findViewById(R.id.subText2);
        mFavButton = (CheckBox) findViewById(R.id.FavButton);
        mBranding = (ImageView) findViewById(R.id.addNewBranding);
        mSynopsis = (EditText) findViewById(R.id.synopsis);
        mStore = (EditText) findViewById(R.id.store);
        mNotes = (EditText) findViewById(R.id.notes);
        mTrailerScroll = (LinearLayout) findViewById(R.id.trailer_scroll);
        mDivider = (View)findViewById(R.id.divider);
        mUserInput = (LinearLayout) findViewById(R.id.user_input);
        mButtonLayout = (LinearLayout)findViewById(R.id.button_layout);
    }

    private void inflateMovieViews(String[] data){

        //Reminder of how array is loaded for Movies
//        String[]{TMDB_MOVIE_ID 0, UPC_CODE 1, ART_IMAGE_URL 2, TITLE 3,
//        FORMATS 4, MOVIE_THUMB 5, BARCODE_URL 6, RELEASE_DATE 7, RUNTIME 8, ADD_DATE 9,
//                POSTER_URL 10, STORE 11, NOTES 12, status 13, RATING 14, SYNOPSIS 15, TRAILERS 16, GENRES 17,
//                 IMDB_NUMBER 18};
        if(data == null) {return;}

        //Hide book related views
        mByline.setVisibility(View.GONE);
        mAuthors.setVisibility(View.GONE);

        //Set branding image
        mBranding.setImageResource(R.drawable.tmdb_brand_120_47);

        //Set hints according to state
        mSubTextOne.setHint(R.string.NoRatingRuntime);
        mSubTextTwo.setHint(R.string.NoFormats);
        //Disable EditText for movie view for subTextOne and Two
        mSubTextOne.setInputType(InputType.TYPE_NULL);
        mSubTextTwo.setInputType(InputType.TYPE_NULL);

        //Set onClickListener for subTextOne to trigger AlertDialog
        mSubTextOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ratingRuntimeDialog();

            }
        });

        mSubTextTwo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                formatsDialog();

            }
        });

        //Fill in views with whatever data is available
        mTitle.setText(data[3]);
        String url = data[2];
        if(!url.equals("")) {
            Glide.with(getApplicationContext()).load(url).fitCenter().placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder).into(mArtImage);
        }
        mReleaseDate.setText(Utility.dateToYear(data[7]));
        mSubTextOne.setText(data[14]);
        mSubTextTwo.setText(data[4]);
        String overview = String.format(getResources().getString(R.string.overview), data[15]);
        mSynopsis.setText(overview);
        addTrailers(mTrailerScroll, data[16]);

        //ALLy content descriptions for dynamic content
        String description = data[3] + " " + data[4] + " Released " + data[7] + " " + "Rated " + data[14];
        mDetailView.setContentDescription(description);
        mSynopsis.setContentDescription("Movie overview " + overview);
        mArtImage.setContentDescription(data[3] + " Movie Art");
        mStatus = data[13];
        mStore.setText(mMovie[11]);
        mNotes.setText(mMovie[12]);

    }



    private void inflateBookViews(String[] data){

        //Reminder of how array is loaded for Books
//        String[] {bookId 0, ean 1, imgUrl 2, title 3, subtitle 4, desc 5, barcode 6, pubDate 7,
//                  authors 8,addDate 9 , publisher 10, store 11, notes 12, status 13, pages 14, categories 15};
        if(data == null) {return;}

        //Hide movie related views
        mTrailerScroll.setVisibility(View.INVISIBLE);

        //Set hints according to state
        mSubTextOne.setHint(R.string.NoPublisher);
        mSubTextTwo.setHint(R.string.NoPages);

        //Set branding image
        mBranding.setImageResource(R.drawable.google_logo);

        //Fill in views
        mTitle.setText(data[3]);
        mByline.setText(data[4]);
        String author = "By " + data[8];
        mAuthors.setText(author);
        String url = data[2];
        if(!url.equals("")) {
            Glide.with(getApplicationContext()).load(url).error(R.drawable.image_placeholder)
                    .placeholder(R.drawable.image_placeholder).fitCenter().into(mArtImage);
        }
        mReleaseDate.setText(data[7]);
        mSubTextOne.setText(data[10]);
        String pages = data[14] + " pgs.";
        mSubTextTwo.setText(pages);
        String overview = String.format(getResources().getString(R.string.overview), data[5]);
        mSynopsis.setText(overview);

        //ALLy content descriptions for dynamic content
        String description = data[3] + " " + data[4] + " Released " + data[7] + " " + "By " + data[8];
        mDetailView.setContentDescription(description);
        mSynopsis.setContentDescription("Book overview " + overview);
        mArtImage.setContentDescription(data[3] + " Cover Art");
        mStatus = data[13];
        mStore.setText(mBook[11]);
        mNotes.setText(mBook[12]);


    }

    private void imageDialog(){

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
                    if(mState == STATE_MOVIE) {
                        mMovie[2] = userURL;
                        mMovie[10] = userURL;
                    }
                    if(mState == STATE_BOOK){
                        mBook[2] = userURL;
                    }
                    //Update the ImageView
                    Glide.with(getApplicationContext()).load(userURL)
                            .fitCenter().error(R.drawable.image_placeholder).into(mArtImage);
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

    private void ratingRuntimeDialog(){

        //Create AlertDialog that allows user to input/update the rating and the runtime of movie.
        final View ratingRuntimeDialog = getLayoutInflater().inflate(R.layout.rating_runtime_dialog, null);
        //Get reference to Rating EditText and load data we have
        final EditText Rating = (EditText) ratingRuntimeDialog.findViewById(R.id.dialogRating);
        Rating.setText(mMovie[14]);
        //Get reference to Runtime and load data we have
        final EditText Runtime = (EditText) ratingRuntimeDialog.findViewById(R.id.dialogRuntime);
        Runtime.setText(mMovie[8]);

        //Make and Launch AlertDialog
        AlertDialog.Builder alert = new AlertDialog.Builder(AddNewActivity.this);
        alert.setTitle(getString(R.string.ratingRuntimeDialogTitle));
        alert.setMessage(getString(R.string.ratingRuntimeDialogMessage));
        alert.setView(ratingRuntimeDialog);
        alert.setPositiveButton(getString(R.string.updateButton), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Get rating entered from dialog
                String rating = Rating.getText().toString().toUpperCase();
                if(Utility.validateRating(rating)){
                    //If valid Update Movie[] with new data
                    rating = Utility.normalizeRating(rating);
                    mMovie[14] = rating;
                }else{
                    Toast.makeText(getApplicationContext(),  getString(R.string.RatingInputError), Toast.LENGTH_LONG).show();
                    Rating.setText("");
                    dialog.cancel();
                }

                //Get runtime from dialog
                String runtime = Runtime.getText().toString();
                runtime = runtime + " min";

                //Update movie[] with new data
                mMovie[8] = runtime;

                //Update View with new data
                String update = rating + runtime;
                mSubTextOne.setText(update);
            }
        });

        alert.setNegativeButton(getString(R.string.cancelButton), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                dialog.cancel();
            }
        });
        alert.create();
        alert.show();

    }

    private void formatsDialog(){

        mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
        final AlertDialog.Builder builder = new AlertDialog.Builder(AddNewActivity.this);
        // Set the dialog title
        builder.setTitle(getString(R.string.formatDialogTitle))
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                //ToDo: change null to remember what was already selected.
                .setMultiChoiceItems(R.array.formats, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton(getString(R.string.updateButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        String[] formatPicks = getResources().getStringArray(R.array.formats);
                        StringBuilder formatSelected = new StringBuilder();
                        for(int i= 0; i<mSelectedItems.size(); i++){
                            if(i == mSelectedItems.size() -1){
                                formatSelected.append(formatPicks[mSelectedItems.get(i)]);
                            }else {
                                formatSelected.append(formatPicks[mSelectedItems.get(i)]);
                                formatSelected.append(" - ");
                            }
                        }
                        String processed = formatSelected.toString();
                        mMovie[4] = processed;
                        mSubTextTwo.setText(processed);

                    }
                })
                .setNegativeButton(getString(R.string.cancelButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        builder.create();
        builder.show();

    }

    private void clearTrailerViews(){
        mTrailerScroll.removeAllViews();
        mTrailerScroll.invalidate();
    }
    private void clearErrorView(){
        mError.setText("");
        mError.setVisibility(View.GONE);
    }

    private void hideViews(){
        mError.setVisibility(View.GONE);
        mDetailView.setVisibility(View.INVISIBLE);
        mTopButtonLayout.setVisibility(View.GONE);
        mTitle.setVisibility(View.INVISIBLE);
        mArtImage.setVisibility(View.INVISIBLE);
        mByline.setVisibility(View.GONE);
        mAuthors.setVisibility(View.GONE);
        mReleaseDate.setVisibility(View.INVISIBLE);
        mSubTextOne.setVisibility(View.INVISIBLE);
        mSubTextTwo.setVisibility(View.INVISIBLE);
        mFavButton.setVisibility(View.INVISIBLE);
        mBranding.setVisibility(View.INVISIBLE);
        mSynopsis.setVisibility(View.INVISIBLE);
        mTrailerScroll.setVisibility(View.INVISIBLE);
        mDivider.setVisibility(View.INVISIBLE);
        mStore.setVisibility(View.INVISIBLE);
        mNotes.setVisibility(View.INVISIBLE);
        mUserInput.setVisibility(View.INVISIBLE);
        mButtonLayout.setVisibility(View.INVISIBLE);
    }

    private void showViews(int state){

        switch (state){
            case STATE_MOVIE:{
                mDetailView.setVisibility(View.VISIBLE);
                mTopButtonLayout.setVisibility(View.VISIBLE);
                mTitle.setVisibility(View.VISIBLE);
                mArtImage.setVisibility(View.VISIBLE);
                mReleaseDate.setVisibility(View.VISIBLE);
                mSubTextOne.setVisibility(View.VISIBLE);
                mSubTextTwo.setVisibility(View.VISIBLE);
                mFavButton.setVisibility(View.VISIBLE);
                mBranding.setVisibility(View.VISIBLE);
                mSynopsis.setVisibility(View.VISIBLE);
                mTrailerScroll.setVisibility(View.VISIBLE);
                mDivider.setVisibility(View.VISIBLE);
                mStore.setVisibility(View.VISIBLE);
                mNotes.setVisibility(View.VISIBLE);
                mUserInput.setVisibility(View.VISIBLE);
                mButtonLayout.setVisibility(View.VISIBLE);
                break;
            }
            case STATE_BOOK: {
                mDetailView.setVisibility(View.VISIBLE);
                mTopButtonLayout.setVisibility(View.VISIBLE);
                mTitle.setVisibility(View.VISIBLE);
                mByline.setVisibility(View.VISIBLE);
                mAuthors.setVisibility(View.VISIBLE);
                mArtImage.setVisibility(View.VISIBLE);
                mReleaseDate.setVisibility(View.VISIBLE);
                mSubTextOne.setVisibility(View.VISIBLE);
                mSubTextTwo.setVisibility(View.VISIBLE);
                mFavButton.setVisibility(View.VISIBLE);
                mBranding.setVisibility(View.VISIBLE);
                mSynopsis.setVisibility(View.VISIBLE);
                mDivider.setVisibility(View.VISIBLE);
                mStore.setVisibility(View.VISIBLE);
                mNotes.setVisibility(View.VISIBLE);
                mUserInput.setVisibility(View.VISIBLE);
                mButtonLayout.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void resetInputText(){
        mInputText.setText("");
    }

    private void resetEditTexts(){
        if(mState == STATE_BOOK){
            mByline.setText("");
            mAuthors.setText("");
        }
        mTitle.setText("");
        mReleaseDate.setText("");
        mSubTextOne.setText("");
        mSubTextTwo.setText("");
        mSynopsis.setText("");
        mStore.setText("");
        mNotes.setText("");

    }

    private void restoreRadioButtonState(int state){
        switch (state){
            case STATE_MOVIE:{
                this.mState = STATE_MOVIE;
                mSelectMovie.setChecked(true);
                break;
            }
            case STATE_BOOK: {
                this.mState = STATE_BOOK;
                mSelectBook.setChecked(true);
                break;
            }
        }
    }

    private void addTrailers(LinearLayout view, String trailers){
        //Clear any old views that still exist
        if(view.getChildCount() > 0){ view.removeAllViews();}
        //If there are no trailers, then nothing to do
        if(trailers == null || trailers.equals("")){
            return;
        }

        LayoutInflater vi = getLayoutInflater();
        final String[] tempTrail;

        int i = 0;
        try{
            tempTrail = trailers.split(",");
            if( tempTrail.length > 0 ) {
                mTrailerArray = tempTrail;

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

            }

        }catch (NullPointerException e){
            //Log.e(LOG_TAG, "Error splitting and Adding Trailers", e);
            FirebaseCrash.log("Error splitting and Adding Trailers");
        }

    }
    private void loadStateArray(){
        //Movie Array format: String[]{TMDB_MOVIE_ID 0, UPC_CODE 1, ART_IMAGE_URL 2, TITLE 3,
        //FORMATS 4, MOVIE_THUMB 5, BARCODE_URL 6, RELEASE_DATE 7, RUNTIME 8, ADD_DATE 9,
        //POSTER_URL 10, STORE 11, NOTES 12, status 13, RATING 14, SYNOPSIS 15, TRAILERS 16,
        // GENRES 17, IMDB_NUMBER 18};

        //Book Array format: String[] {bookId 0, ean 1, imgUrl 2, title 3, subtitle 4, desc 5,
        // barcode 6, pubDate 7, authors 8, addDate 9, publisher 10, store 11, notes 12,
        // status 13 , pages 14 , categories 15};

        // if movie null then nothing to save
        if(mState == STATE_MOVIE && mMovie != null){
            //Prioritize what's in the edit text, if user changed it, then save changes, otherwise skip.
            String uTitle = mTitle.getText().toString();
            if(mMovie[3]!= null &&  !mMovie[3].equals(uTitle)) {
                mMovie[3] = mTitle.getText().toString();
            }
            if(!mMovie[7].equals(mReleaseDate.getText().toString())) {
                mMovie[7] = mReleaseDate.getText().toString();
            }
            if(!mMovie[4].equals(mSubTextTwo.getText().toString())) {
                mMovie[4] = Utility.processFormats(mSubTextTwo.getText().toString());
            }
            String overview = mSynopsis.getText().toString().replaceFirst("Overview: \n\n", "");
            if(!mMovie[15].equals(overview)) {

                mMovie[15] = overview;
            }
            mMovie[11] = mStore.getText().toString();
            mMovie[12] = mNotes.getText().toString();

        }

        // if book null then nothing to save
        if(mState == STATE_BOOK && mBook != null){
            if(!mBook[3].equals(mTitle.getText().toString())){
                mBook[3] = mTitle.getText().toString();
            }
            if(!mBook[4].equals(mByline.getText().toString())){
                mBook[4] = mByline.getText().toString();
            }
            if(!mBook[8].equals(mAuthors.getText().toString())){
                mBook[8] = mAuthors.getText().toString();
            }
            if(!mBook[7].equals(mReleaseDate.getText().toString())){
                mBook[7] = mReleaseDate.getText().toString();
            }
            if(!mBook[10].equals(mSubTextOne.getText().toString())) {
                mBook[10] = mSubTextOne.getText().toString();
            }
            String pages = mSubTextTwo.getText().toString().replace(" pgs.", "");
            if(!mBook[14].equals(pages)){
                mBook[14] = pages;
            }
            String bookOverview = mSynopsis.getText().toString().replaceFirst("Overview: \n\n", "");
            if(!mBook[5].equals(bookOverview)){
                mBook[5] = bookOverview;
            }
            mBook[11] = mStore.getText().toString();
            mBook[12] = mNotes.getText().toString();

        }
    }

    public String[] initialStringArray(int size){
        String[] array = new String[size];
        for(int i = 0; i<array.length; i++){
            array[i] = "";
        }
        return array;
    }

    private void lookupError(String[] response){
        String message = "";
        if(mState == STATE_MOVIE){
            message = "Sorry, the movie with UPC: " + response[1] + " is not in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and Enter manually" ;
            FirebaseCrash.log("Movie NOT FOUND" + response[1]);
        }
        if(mState == STATE_BOOK){
            message = "Sorry, the book with ISBN: " + response[1] + " is not in our database. \n" +
                    "Error: " + response[0] + "\n" +
                    "Tap this message to clear and add a different book. \n" +
                    "Manual entry is coming soon!";
            FirebaseCrash.log("Book NOT FOUND" + response[1]);
        }
        mError.setText(message);
        mError.setVisibility(View.VISIBLE);
    }


    private void logActionEvent(String activity, String actionName, String type ){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, actionName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}


