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

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Jasen Sanders on 10/11/2016.
 * A simple {@link AppCompatActivity} subclass used to display items from the database.
 *
 */


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String ACTIVITY_NAME = "MainActivity";

    public static final String MAIN_PREFS = "MAIN_PREFS";
    private static final String SORT_ORDER_STATE = "SORT";
    private static final String LIST_VIEW_STATE = "VIEW";
    private static final String VIEW_LABEL_STATE = "VIEW_LABEL";
    private static final String TWO_PANE_DETAIL_FRAGMENT = "TWO_PANE_DETAIL_FRAG";
    private static final int MY_WRITE_EXT_STORAGE_PERMISSON = 11264;

    //Loader flags also act as view state indicators
    private static final int MOVIE_LOADER = 100;
    private static final int WISHLIST_LOADER = 200;
    private static final int BOOKS_LOADER = 300;

    //Triggers a search based on the above Loader/View state
    private static final int SEARCH_LOADER = 3500;


    private static final String TITLE = "TITLE";
    private static final String ADD_DATE = "ADD_DATE";
    private static final String RELEASE_DATE = "DATE";
    private static final String ASC = " ASC";
    private static final String DESC = " DESC";

    //Loader state represents ViewType State
    private int mFolioLoader = 0;

    //State variables
    private String mSortOrder;
    private String mViewLabel;
    private Fragment mCurrentFragment;
    private int mPermission;


    //Views
    private TextView mTitleViewLabel;
    private SearchView mSearchView;
    public final Activity mActivity = this;
    private RecyclerView itemList;
    private ListItemAdapter listItemAdapter;
    AdView mAdView;

    //Firebase
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start Tracking events
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //((FolioApplication) getApplication()).startTracking();
        //If the app is still in session, Restore State after rotation.
        if (savedInstanceState != null) {
            mSortOrder = savedInstanceState.getString(SORT_ORDER_STATE);
            mFolioLoader = savedInstanceState.getInt(LIST_VIEW_STATE);
            mViewLabel = savedInstanceState.getString(VIEW_LABEL_STATE);

            //If in TWO PANE mode the re-launch current detail fragment
            if(findViewById(R.id.detail_container) != null){
                mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, TWO_PANE_DETAIL_FRAGMENT);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, mCurrentFragment)
                        .commit();
            }

        } else {

                //Otherwise set view defaults Which is Movies for now.
                //TODO Settings Activity to set defaults. For now they are set here.
                mFolioLoader = MOVIE_LOADER;
                mSortOrder = DataContract.MovieEntry.COLUMN_ADD_DATE + " DESC";
                mViewLabel = getString(R.string.movie_view);

        }

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        mAdView = (AdView) findViewById(R.id.adView);

        if(!BuildConfig.DEBUG_BUILD) {
            //Ads app ID Initialization
            MobileAds.initialize(getApplicationContext(), getString(R.string.adAppId));

        }
        AdRequest.Builder builder = new AdRequest.Builder();
        if(BuildConfig.DEBUG_BUILD) {
            builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            builder.addTestDevice("9929AC1F80C13986BA10336A6CEE1CF6"); //My Nexus 5x Test Phone
            builder.addTestDevice("ECF8814A2889BB1528CE0F6E1BCFA7ED");  //My GS3 Test Phone
        }
        AdRequest request= builder.build();
        mAdView.loadAd(request);

        //Initialize recycler view
        itemList = (RecyclerView) findViewById(R.id.content_list);

        //Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitleViewLabel = (TextView) findViewById(R.id.viewLabel);
        mTitleViewLabel.setText(mViewLabel);

        //Setup Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                startActivity(new Intent(getApplicationContext(), AddNewActivity.class), bundle);
            }
        });

        //Setup Nav Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Check for the WRITE_EXTERNAL_STORAGE permission. This is needed for local backup.  If the
        // permission is not granted yet, request permission.
         mPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (mPermission != PackageManager.PERMISSION_GRANTED) {
            setupPermissions();
        }

        //Load available data from FolioProvider
        getLoaderManager().initLoader(mFolioLoader, null, this);

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

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(SORT_ORDER_STATE, mSortOrder);
        savedInstanceState.putInt(LIST_VIEW_STATE, mFolioLoader);
        savedInstanceState.putString(VIEW_LABEL_STATE, mViewLabel);
        //Save the fragment's instance if in TWO PANE mode
        if(mCurrentFragment != null) {
            getSupportFragmentManager().putFragment(savedInstanceState, TWO_PANE_DETAIL_FRAGMENT, mCurrentFragment);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mSearchItem = menu.findItem(R.id.action_search);
        SearchManager Manager =  (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setSearchableInfo(Manager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(true);
        if(mSearchView != null){
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    updateUI(SEARCH_LOADER);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

        }

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_search_retail:
                logActionEvent(ACTIVITY_NAME,"nav_search_retail", "action");
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle();
                Intent SearchRetailIntent = new Intent(this, RetailerSearchActivity.class);
                startActivity(SearchRetailIntent, bundle);
                break;
            case R.id.nav_all_movies:
                logActionEvent(ACTIVITY_NAME,"nav_all_movies", "action");
                String movieLabel = getString(R.string.movie_view);
                mViewLabel = movieLabel;
                mTitleViewLabel.setText(mViewLabel);
                mFolioLoader = MOVIE_LOADER;
                getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
                break;
            case R.id.nav_all_books:
                logActionEvent(ACTIVITY_NAME,"nav_all_books", "action");
                String booksLabel = getString(R.string.books_view);
                mViewLabel = booksLabel;
                mTitleViewLabel.setText(mViewLabel);
                mFolioLoader = BOOKS_LOADER;
                getLoaderManager().restartLoader(BOOKS_LOADER, null, this);
                break;
            case R.id.nav_wish_list:
                logActionEvent(ACTIVITY_NAME,"nav_wish_list", "action");
                String wishListLabel = getString(R.string.wish_list_view);
                mViewLabel = wishListLabel;
                mTitleViewLabel.setText(mViewLabel);
                mFolioLoader = WISHLIST_LOADER;
                getLoaderManager().restartLoader(WISHLIST_LOADER, null, this);
                break;
            case R.id.nav_title_asc:
                //call provider and refresh
                mSortOrder = Utility.sortOrderStringMatcher(mFolioLoader, TITLE, ASC);
                getLoaderManager().restartLoader(mFolioLoader, null, this);
                break;
            case R.id.nav_title_desc:
                //call provider and refresh
                mSortOrder = Utility.sortOrderStringMatcher(mFolioLoader, TITLE, DESC);
                getLoaderManager().restartLoader(mFolioLoader, null, this);
                break;
            case R.id.nav_date_add_desc:
                //call provider and refresh
                mSortOrder = Utility.sortOrderStringMatcher(mFolioLoader, ADD_DATE, DESC);
                getLoaderManager().restartLoader(mFolioLoader, null, this);
                break;
            case R.id.nav_date_add_asc:
                //call provider ans refresh
                mSortOrder = Utility.sortOrderStringMatcher(mFolioLoader, ADD_DATE, ASC);
                getLoaderManager().restartLoader(mFolioLoader, null, this);
                break;
            case R.id.nav_restore_db:
                //Set Prefrences to restore backup on restart
                Utility.setPrefRestoreBackup(this, true);
                logActionEvent(ACTIVITY_NAME,"nav_restore_db", "action");
                //Launch snackbar to ask if user wants to restart now
                final String request = "Backup will be restored on restart";
                final Snackbar restartMessage = Snackbar.make(findViewById(R.id.root), request, Snackbar.LENGTH_LONG );
                restartMessage.setAction("RESTART", new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Utility.reStart(mActivity, 100, SplashActivity.class);
                    }
                });
                restartMessage.show();
                break;


        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
    @Override
    public void onStop(){
        super.onStop();
        if(mPermission == PackageManager.PERMISSION_GRANTED) {
            //Do not backup an empty database, you may overwrite a fully backed up database
            if(!Utility.isDBEmpty(this)) {
                if (Utility.backupDBtoMobileDevice(getApplicationContext())) {
                    //Log.v(LOG_TAG, "Backup Success!");
                    FirebaseCrash.log("Backup Success");
                } else {
                    //Log.v(LOG_TAG, "Backup Failed!");
                    FirebaseCrash.log("Backup Failed");
                }
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //Get the sort order
        switch (id) {
            case MOVIE_LOADER:
            {
                return new CursorLoader(this,
                        DataContract.MovieEntry.buildUriAll(),
                        Utility.MOVIE_COLUMNS,
                        null,
                        null,
                        mSortOrder);
            }
            case BOOKS_LOADER:
            {
                return new CursorLoader(this,
                        DataContract.BookEntry.buildUriAll(),
                        Utility.BOOK_COLUMNS,
                        null,
                        null,
                        mSortOrder);
            }
            case WISHLIST_LOADER:
            {
                return new CursorLoader(this,
                        DataContract.WishEntry.buildUriAll(),
                        Utility.WISHLIST_COLUMNS,
                        null,
                        null,
                        mSortOrder);
            }
            case SEARCH_LOADER:
            {
                //Get the Query from the widget **Google Voice Search launches in a separate activity**
                String searchString = mSearchView.getQuery().toString();

                //Log.v("Search Loader Started: ", searchString);
                logSearchEvent(ACTIVITY_NAME, "SearchButton", searchString, "SQL");
                //If there is something to query, determine appropriate cursor and return it
                if(searchString != null && searchString.length()>0){

                    searchString = "%"+searchString+"%";
                    final String[] selectionArgs = Utility.selectionArgsMatcher(mFolioLoader, searchString);
                    final Uri searchUri = Utility.uriSearchStateMatcher(mFolioLoader);
                    final String selection = Utility.selectionSearchStateMatcher(mFolioLoader);
                    final String[] projection = Utility.projectionSearchStateMatcher(mFolioLoader);

                    return new CursorLoader(this,
                            searchUri,
                            projection,
                            selection,
                            selectionArgs,
                            mSortOrder
                    );
                }

            }
            default:
            {
                return new CursorLoader(this,
                        DataContract.MovieEntry.buildUriAll(),
                        Utility.MOVIE_COLUMNS,
                        null,
                        null,
                        DataContract.MovieEntry.COLUMN_ADD_DATE + " DESC");
            }
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()) {
            Log.e(LOG_TAG, "SearchLoader arrived in onLoadFinished");
            listItemAdapter = new ListItemAdapter(this);
            listItemAdapter.swapCursor(data);

            //This is only used in TWO PANE mode
            listItemAdapter.setOnItemClickedListener(new ListItemAdapter.OnItemClickedListener() {
                @Override
                public void onItemClicked(Uri data, String status) {
                    launchTwoPaneFragment(data, status);
                }
            });
            itemList.setAdapter(listItemAdapter);
            LinearLayoutManager list = new LinearLayoutManager(this);
            list.setOrientation(LinearLayoutManager.VERTICAL);
            itemList.setLayoutManager(list);
            itemList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, R.drawable.line_divider));

        } else {
            itemList.setAdapter(null);
            itemList.removeAllViews();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(listItemAdapter != null) {
            listItemAdapter.swapCursor(null);
        }

    }

    public void updateUI(int Loader_Type) {

        getLoaderManager().restartLoader(Loader_Type, null, this);

    }

    //Only used in TWO PANE mode
    public void launchTwoPaneFragment(Uri data, String status) {

        //If this is not a wish view Item
        if(!status.contains("_WISH")) {
            //If its a movie load the Movie fragment
            if(status.contains("MOVIE")) {
                DetailMovieFragment fragment = DetailMovieFragment.newInstance(data);
                mCurrentFragment = fragment;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, fragment)
                        .commit();
            }else{
                //otherwise its a Book so load the book fragment
                DetailBookFragment fragment = DetailBookFragment.newInstance(data);
                mCurrentFragment = fragment;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, fragment)
                        .commit();
            }
        }else{
            //its a Wish item so load the wish fragment
            DetailWishFragment fragment = DetailWishFragment.newInstance(data);
            mCurrentFragment = fragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        }
    }

    private void setupPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_WRITE_EXT_STORAGE_PERMISSON);

    }

    private void logSearchEvent(String activity, String buttonName, String term, String type ){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, term);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void logActionEvent(String activity, String buttonName, String type ){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
