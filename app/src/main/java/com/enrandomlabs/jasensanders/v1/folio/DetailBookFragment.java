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

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;
import com.google.firebase.analytics.FirebaseAnalytics;

import static com.enrandomlabs.jasensanders.v1.folio.database.DataContract.B_COL_AUTHOR;

/**
 * Created by Jasen Sanders on 10/11/2016.
 * A simple {@link Fragment} subclass used to display items from the book database.
 * Use the {@link DetailBookFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailBookFragment.class.getSimpleName();
    private static final String ACTIVITY_NAME = "DetailBookFragment";

    private static final String DETAIL_BOOK_URI = "BOOK_URI";
    private static final int DETAIL_BOOK_LOADER = 302;
    private static final String DETAIL_BOOK_CURRENT_STORE = "DETAIL_BOOK_CURRENT_STORE";
    private static final String DETAIL_BOOK_CURRENT_NOTES = "DETAIL_BOOK_CURRENT_NOTES";
    private static final String GOOGLE_BOOKS = "https://books.google.com/";

    private FirebaseAnalytics mFirebaseAnalytics;
    private ShareActionProvider mShareActionProvider;
    private Uri mParam1;
    private String mDetailBookShare;
    private Resources mResources;

    private String mStatus;
    private String mStateSavedStore;
    private String mStateSavedNotes;

    private View mRootView;
    private TextView mError;
    private TextView mTitle;
    private TextView mByline;
    private TextView mAuthors;
    private ImageView mPosterImage;
    private ImageView mBranding;
    private ImageView mBarcodeImage;
    private TextView mReleaseDate;
    private TextView mSubTextOne;
    private TextView mSubTextTwo;
    private TextView mSynopsis;
    private EditText mStore;
    private EditText mNotes;
    private LinearLayout mTrailerScroll;
    private LinearLayout mDetailView;
    private CheckBox mFavButton;
    private Button mSearchRetail;
    private Button mDeleteButton;
    private Button mSaveButton;


    public DetailBookFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DetailBookFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailBookFragment newInstance(Uri param1) {
        DetailBookFragment fragment = new DetailBookFragment();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_BOOK_URI, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        logActionEvent(ACTIVITY_NAME, "ActivityStarted", "action");

        mResources = getResources();
        if (getArguments() != null) {
            mParam1 = getArguments().getParcelable(DETAIL_BOOK_URI);

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            // Restore last state.
            mStateSavedStore = savedInstanceState.getString(DETAIL_BOOK_CURRENT_STORE);
            mStateSavedNotes = savedInstanceState.getString(DETAIL_BOOK_CURRENT_NOTES);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);

        initializeViews();

        getLoaderManager().initLoader(DETAIL_BOOK_LOADER, null, this);

        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment_menu, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(mDetailBookShare));
            mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {


                    logShareEvent(ACTIVITY_NAME, "ShareButton", mDetailBookShare);

                    return false;
                }
            });
        }
    }

    private Intent createShareIntent(String itemDesc) {

        if(itemDesc != null) {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, itemDesc);
            return shareIntent;
        }else{

            String placeHolder = getString(R.string.placeholder_share);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, placeHolder);
            return shareIntent;
        }
    }

    private void initializeViews(){

        //Input Area
        mError = (TextView) mRootView.findViewById(R.id.error);
        mError.setVisibility(View.GONE);

        //Book Details Area
        mDetailView = (LinearLayout) mRootView.findViewById(R.id.details);
        mTitle = (TextView) mRootView.findViewById(R.id.detail_view_title);
        mByline = (TextView) mRootView.findViewById(R.id.detail_view_byline);
        mAuthors = (TextView) mRootView.findViewById(R.id.detail_view_authors);
        mPosterImage = (ImageView) mRootView.findViewById(R.id.posterView);
        mBranding = (ImageView) mRootView.findViewById(R.id.branding);
        mBarcodeImage = (ImageView) mRootView.findViewById(R.id.upcBarcodeImage);
        mReleaseDate = (TextView) mRootView.findViewById(R.id.releaseDate);
        mSubTextOne = (TextView) mRootView.findViewById(R.id.detail_subtext1);
        mSubTextTwo = (TextView) mRootView.findViewById(R.id.detail_subtext2);
        mFavButton = (CheckBox) mRootView.findViewById(R.id.FavButton);
        mSearchRetail = (Button) mRootView.findViewById(R.id.search_retail);
        mSynopsis = (TextView) mRootView.findViewById(R.id.synopsis);
        mStore = (EditText) mRootView.findViewById(R.id.store);
        mNotes = (EditText) mRootView.findViewById(R.id.notes);
        mTrailerScroll = (LinearLayout) mRootView.findViewById(R.id.trailer_scroll);
        mDeleteButton = (Button) mRootView.findViewById(R.id.delete_button);
        mSaveButton = (Button) mRootView.findViewById(R.id.save_button);

    }

    private void inflateViews(Cursor row){

        mStatus = row.getString(DataContract.W_COL_STATUS);

        //Its a book so load views accordingly
        Glide.with(getActivity()).load(row.getString(DataContract.B_COL_THUMB)).fitCenter().into(mPosterImage);
        Glide.with(getActivity()).load(row.getString(DataContract.B_COL_BARCODE)).fitCenter().into(mBarcodeImage);
        mTitle.setText(row.getString(DataContract.B_COL_TITLE));
        mByline.setText(row.getString(DataContract.B_COL_SUBTITLE));
        mAuthors.setText(row.getString(B_COL_AUTHOR));
        mReleaseDate.setText(Utility.dateToYear(row.getString(DataContract.B_COL_DATE)));
        mSubTextOne.setText(row.getString(DataContract.B_COL_PUB));
        String pages = row.getString(DataContract.B_COL_PAGES) + " pgs.";
        mSubTextTwo.setText(pages);

        mBranding.setImageResource(R.drawable.google_logo);
        mBranding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToBrand();
            }
        });

        String overview = String.format(mResources.getString(R.string.overview), row.getString(DataContract.B_COL_DESC));
        mSynopsis.setText(overview);

        //Hide trailers view
        mTrailerScroll.setVisibility(View.INVISIBLE);
        //Hide FavButton because this is not a Wish View
        mFavButton.setVisibility(View.GONE);

        //ALLy content descriptions for dynamic content
        String description = mResources.getString(R.string.book_detail_view_description,
                row.getString(DataContract.B_COL_TITLE), row.getString(B_COL_AUTHOR), row.getString(DataContract.B_COL_DATE));
        mDetailView.setContentDescription(description);
        mSynopsis.setContentDescription(overview);
        String artDesc = mResources.getString(R.string.poster_description, row.getString(DataContract.B_COL_TITLE));
        mPosterImage.setContentDescription(artDesc);
        String barcodeDesc = mResources.getString(R.string.barcode_description, row.getString(DataContract.B_COL_UPC));
        mBarcodeImage.setContentDescription(barcodeDesc);

        mDetailBookShare = getActivity().getResources().getString(R.string.detail_book_desc, row.getString(DataContract.B_COL_TITLE),
                row.getString(B_COL_AUTHOR), row.getString(DataContract.B_COL_DATE), row.getString(DataContract.B_COL_UPC));
        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(mDetailBookShare));
        }

        //Restore Store and Notes fields from Saved State if necessary.
        if(mStateSavedStore != null ){
            mStore.setText(mStateSavedStore);
        }else {
            mStore.setText(row.getString(DataContract.B_COL_STORE));
        }
        if(mStateSavedNotes != null){
            mNotes.setText(mStateSavedNotes);
        }else {
            mNotes.setText(row.getString(DataContract.B_COL_NOTES));
        }

        //Setup Delete, Save and Search Retail Buttons for Movie View, Book View and WishList View
        final String CurrentUPC = mParam1.getLastPathSegment();

        //Set searchRetail click listener
        mSearchRetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logActionEvent(ACTIVITY_NAME, "SearchRetailersButton", "action");
                Uri send = mParam1;
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle();
                Intent SearchRetailIntent = new Intent(getActivity(), RetailerSearchActivity.class);
                SearchRetailIntent.setData(send);
                startActivity(SearchRetailIntent, bundle);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                //Determine from which list we are deleting
                Uri deleteItem = mParam1;
                String deleteSelection = DataContract.BookEntry.COLUMN_UPC + " = ?";

                //Attempt delete
                int rowsDeleted = getActivity().getContentResolver().delete(deleteItem,
                        deleteSelection,
                        new String[]{CurrentUPC});
                //Notify User
                if(rowsDeleted == 1){
                    Toast.makeText(getActivity(), getString(R.string.detailItemRemoved), Toast.LENGTH_SHORT).show();
                }

            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                //Get input changes
                String Store = mStore.getText().toString();
                String Notes = mNotes.getText().toString();

                //Determine from which list we are updating
                Uri updateMovie = mParam1;;
                ContentValues update = Utility.makeUpdateValues(Store, Notes, Utility.BOOK_BY_UPC);
                String saveSelection = DataContract.BookEntry.COLUMN_UPC + " = ?";
                int rowsUpdated;

                //Attempt update
                rowsUpdated = getActivity().getContentResolver().update(updateMovie,update,
                        saveSelection,
                        new String[]{CurrentUPC});

                if(rowsUpdated == 1){
                    Toast.makeText(getActivity(), getString(R.string.detailItemUpdated), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void sendToBrand(){

        Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_BOOKS));
        result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(result.resolveActivity(getActivity().getPackageManager()) != null){
            startActivity(result);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putString(DETAIL_BOOK_CURRENT_STORE, mStore.getText().toString());
        outState.putString(DETAIL_BOOK_CURRENT_NOTES, mNotes.getText().toString());

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                mParam1,
                DataContract.BOOK_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.moveToFirst()){
            inflateViews(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void logShareEvent(String activity, String buttonName, String shareable){

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, shareable);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "book_string");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
    }

    private void logActionEvent(String activity, String actionName, String type ){

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, actionName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
