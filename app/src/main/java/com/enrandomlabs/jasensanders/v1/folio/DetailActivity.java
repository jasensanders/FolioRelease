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
 * A simple {@link AppCompatActivity} subclass used to display items from thedatabase.
 * Use the {@link DetailBookFragment#newInstance}
 * or {@link DetailMovieFragment#newInstance} or {@link DetailWishFragment#newInstance}factory method to
 * create an instance of the appropriate fragment and load it into the view container.
 */

public class DetailActivity extends AppCompatActivity {
    private Fragment mContent;

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
            //Restore the fragment's instance
            mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.small_detail_container, mContent)
                    .commit();
        }else {

            switch (match) {
                case Utility.MOVIE_BY_UPC: {
                    //Log.v("DetailActivity", "MovieByUPC -Fragment");
                    DetailMovieFragment fragment = DetailMovieFragment.newInstance(data);
                    mContent = fragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.small_detail_container, fragment)
                            .commit();
                    break;
                }
                case Utility.WISH_ITEM_BY_UPC: {
                    //Log.v("DetailActivity", "WishByUPC -Fragment");
                    DetailWishFragment fragment = DetailWishFragment.newInstance(data);
                    mContent = fragment;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.small_detail_container, fragment)
                            .commit();
                    break;

                }
                case Utility.BOOK_BY_UPC: {
                    //Log.v("DetailActivity", "BookByUPC -Fragment");
                    DetailBookFragment fragment = DetailBookFragment.newInstance(data);
                    mContent = fragment;
                    getSupportFragmentManager().beginTransaction()
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

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, "mContent", mContent);
    }
}
