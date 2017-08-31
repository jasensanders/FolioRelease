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

import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jasen Sanders on 10/11/2016.
 * A simple {@link AppCompatActivity} subclass used to display either items from the database
 * or a Settings Fragment.
 * Use the {@link DetailBookFragment#newInstance}
 * or {@link DetailMovieFragment#newInstance} or {@link DetailWishFragment#newInstance} or
 * {@link SettingsFragment#newInstance}factory method to create an instance of the appropriate
 * fragment and load it into the view container.
 */

public class DetailActivity extends AppCompatActivity {
    private Fragment mContent;
    private SettingsFragment mSettingsFragment;
    private String mTitle = "";
    private static final String DETAIL_VIEW_KEY = "mContent";
    private static final String SETTINGS_KEY ="mSettingsFragment";
    private static final String DETAIL_TITLE_KEY = "mDETAIL_TITLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        UriMatcher detailMatcher = Utility.buildDetailUriMatcher();
        Intent intent = getIntent();
        Uri data = intent.getData();
        int match = detailMatcher.match(data);
        //Log.v("DetailActivity", data.toString() + " Match #: "+ String.valueOf(match));
        //If there is already a fragment loaded (Rotation)
        if (savedInstanceState != null) {
            //Restore the Title
            mTitle = savedInstanceState.getString(DETAIL_TITLE_KEY);

            //Restore the fragment's instance
            if(savedInstanceState.containsKey(DETAIL_VIEW_KEY)) {
                setTitle(mTitle);
                mContent = getSupportFragmentManager().getFragment(savedInstanceState, DETAIL_VIEW_KEY);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.small_detail_container, mContent)
                        .commit();
            }
            if(savedInstanceState.containsKey(SETTINGS_KEY)){
                setTitle(mTitle);
                mSettingsFragment = (SettingsFragment) getFragmentManager().getFragment(savedInstanceState, SETTINGS_KEY);
                getFragmentManager().beginTransaction().replace(R.id.small_detail_container, mSettingsFragment)
                        .commit();
            }
        }else {

            switch (match) {
                case Utility.MOVIE_BY_UPC: {

                    mTitle = getString(R.string.title_movie_detail);
                    setTitle(mTitle);
                    mSettingsFragment = null;
                    DetailMovieFragment fragment = DetailMovieFragment.newInstance(data);
                    mContent = fragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.small_detail_container, fragment)
                            .commit();
                    break;
                }
                case Utility.WISH_ITEM_BY_UPC: {

                    mTitle = getString(R.string.title_activity_detail);
                    mSettingsFragment = null;
                    DetailWishFragment fragment = DetailWishFragment.newInstance(data);
                    mContent = fragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.small_detail_container, fragment)
                            .commit();
                    break;

                }
                case Utility.BOOK_BY_UPC: {

                    mTitle = getString(R.string.title_book_detail);
                    setTitle(mTitle);
                    mSettingsFragment = null;
                    DetailBookFragment fragment = DetailBookFragment.newInstance(data);
                    mContent = fragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.small_detail_container, fragment)
                            .commit();
                    break;
                }
                case Utility.SETTINGS:{

                    mTitle = getString(R.string.settings_title);
                    setTitle(mTitle);
                    mContent = null;
                    SettingsFragment fragment = SettingsFragment.newInstance();
                    mSettingsFragment = fragment;
                    getFragmentManager().beginTransaction()
                            .replace(R.id.small_detail_container, fragment)
                            .commit();
                    break;

                }
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the Title on rotation
        outState.putString(DETAIL_TITLE_KEY, mTitle);

        //Save the fragment's instance on rotation
        if(mSettingsFragment != null){

            getFragmentManager().putFragment(outState, SETTINGS_KEY, mSettingsFragment);

        }else {

            getSupportFragmentManager().putFragment(outState, DETAIL_VIEW_KEY, mContent);
        }
    }
}
