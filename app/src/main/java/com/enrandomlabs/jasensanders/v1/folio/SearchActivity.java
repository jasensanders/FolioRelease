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

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;

/**
 * Created by Jasen Sanders on 10/11/2016.
 * A simple {@link AppCompatActivity} subclass used to display items from the database.
 * This is launched from the Google Assistant.
 *
 */


public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();
    private static final String GOOGLE_VOICE_ACTION = "com.google.android.gms.actions.SEARCH_ACTION";

    private static final String TWO_PANE_DETAIL_FRAGMENT = "TWO_PANE_DETAIL_FRAG";

    private static final String TITLE = "TITLE";
    private static final String ADD_DATE = "ADD_DATE";
    private static final String RELEASE_DATE = "DATE";
    private static final String ASC = " ASC";
    private static final String DESC = " DESC";

    private RecyclerView itemList;
    private TextView mEmptyView;
    private ListItemAdapter listItemAdapter;
    private Fragment mCurrentFragment;
    private Uri mSearchUri;
    private String mSortOrder = ADD_DATE + DESC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent = getIntent();
        if(intent.getAction().equals(GOOGLE_VOICE_ACTION)){
            String currentQuery = intent.getStringExtra(SearchManager.QUERY);
            if(currentQuery != null) {
                //Build the URI
                mSearchUri = DataContract.buildSearchUri(currentQuery);

                //Query the Content Provider for any match
                getLoaderManager().initLoader(0, null, this);
            }

        }

        //Initialize empty view
        mEmptyView = (TextView) findViewById(R.id.sRecyclerView_empty);

        //Initialize recycler view
        itemList = (RecyclerView) findViewById(R.id.content_list);

        //If in TWO PANE mode the re-launch current detail fragment
        if(findViewById(R.id.detail_container) != null){
            mCurrentFragment = getSupportFragmentManager().getFragment(savedInstanceState, TWO_PANE_DETAIL_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, mCurrentFragment)
                    .commit();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        //Save the fragment's instance if in TWO PANE mode
        if(mCurrentFragment != null) {
            getSupportFragmentManager().putFragment(savedInstanceState, TWO_PANE_DETAIL_FRAGMENT, mCurrentFragment);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        //If there is something to query, determine appropriate cursor and return it
        if(mSearchUri != null) {

            return new CursorLoader(this,
                    mSearchUri,
                    null,
                    null,
                    null,
                    mSortOrder
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst() && !data.isNull(0)) {
            mEmptyView.setVisibility(View.GONE);
            itemList.setVisibility(View.VISIBLE);

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
//

        } else {
            itemList.setAdapter(null);
            itemList.removeAllViews();
            itemList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(R.string.search_is_empty);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(listItemAdapter != null) {
            listItemAdapter.swapCursor(null);
        }

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
}
