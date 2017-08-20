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
import com.google.firebase.crash.FirebaseCrash;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailMovieFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailMovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailMovieFragment.class.getSimpleName();
    private static final String ACTIVITY_NAME = "DetailMovieFragment";

    public static final String MOVIE_DETAIL_URI = "MOVIE_URI";
    private static final int DETAIL_MOVIE_LOADER = 102;

    private static final String DETAIL_CURRENT_STORE = "DETAIL_CURRENT_STORE";
    private static final String DETAIL_CURRENT_NOTES = "DETAIL_CURRENT_NOTES";

    private static final String TMDB_SITE = "https://www.themoviedb.org/";

    private FirebaseAnalytics mFirebaseAnalytics;
    private ShareActionProvider mShareActionProvider;
    private Uri mParam1;
    private String mDetailMovieShare;
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


    public DetailMovieFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1. A URI containing the upc of an item to display
     * @return A new instance of fragment DetailMovieFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailMovieFragment newInstance(Uri param1) {
        DetailMovieFragment fragment = new DetailMovieFragment();
        Bundle args = new Bundle();
        args.putParcelable(MOVIE_DETAIL_URI, param1);
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
        //If we were launched by fragment transaction, get arguments
        if (getArguments() != null) {
            mParam1 = getArguments().getParcelable(MOVIE_DETAIL_URI);

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state.
            mStateSavedStore = savedInstanceState.getString(DETAIL_CURRENT_STORE);
            mStateSavedNotes = savedInstanceState.getString(DETAIL_CURRENT_NOTES);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
         mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initializeViews();
        getLoaderManager().initLoader(DETAIL_MOVIE_LOADER, null, this);

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
            mShareActionProvider.setShareIntent(createShareIntent(mDetailMovieShare));
            mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {


                    logShareEvent(ACTIVITY_NAME, "ShareButton", mDetailMovieShare);

                    return false;
                }
            });
        }
    }

    private Intent createShareIntent(String movieDesc) {
        if(movieDesc != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, movieDesc);
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

        //Movie Details Area
        mDetailView = (LinearLayout) mRootView.findViewById(R.id.details);
        mTitle = (TextView) mRootView.findViewById(R.id.detail_view_title);
        mByline = (TextView) mRootView.findViewById(R.id.detail_view_byline);
        mAuthors = (TextView) mRootView.findViewById(R.id.detail_view_authors);
        mPosterImage = (ImageView) mRootView.findViewById(R.id.posterView);
        mBarcodeImage = (ImageView) mRootView.findViewById(R.id.upcBarcodeImage);
        mReleaseDate = (TextView) mRootView.findViewById(R.id.releaseDate);
        mSubTextOne = (TextView) mRootView.findViewById(R.id.detail_subtext1);
        mSubTextTwo = (TextView) mRootView.findViewById(R.id.detail_subtext2);
        mBranding = (ImageView) mRootView.findViewById(R.id.branding);
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

        mStatus = row.getString(DataContract.COL_STATUS);
        mTitle.setText(row.getString(DataContract.COL_TITLE));
        mByline.setVisibility(View.GONE);
        mAuthors.setVisibility(View.GONE);
        Glide.with(getActivity()).load(row.getString(DataContract.COL_POSTER)).fitCenter().into(mPosterImage);
        Glide.with(getActivity()).load(row.getString(DataContract.COL_BARCODE)).fitCenter().into(mBarcodeImage);
        mReleaseDate.setText(Utility.dateToYear(row.getString(DataContract.COL_DATE)));
        String ratingRuntime = String.format(mResources.getString(R.string.rating_runtime),
                row.getString(DataContract.COL_RATING), row.getString(DataContract.COL_RUNTIME) );
        mSubTextOne.setText(ratingRuntime);
        mSubTextTwo.setText(row.getString(DataContract.COL_FORMATS));
        mBranding.setImageResource(R.drawable.tmdb_brand_120_47);
        mBranding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToBrand();
            }
        });
        String overview = String.format(mResources.getString(R.string.overview), row.getString(DataContract.COL_SYNOPSIS));
        mSynopsis.setText(overview);

        //Hide FavButton. This is not a wish view.
        mFavButton.setVisibility(View.GONE);

        //Restore from Saved State if necessary.
        if(mStateSavedStore != null ){
            mStore.setText(mStateSavedStore);
        }else {
            mStore.setText(row.getString(DataContract.COL_STORE));
        }
        if(mStateSavedNotes != null){
            mNotes.setText(mStateSavedNotes);
        }else {
            mNotes.setText(row.getString(DataContract.COL_NOTES));
        }

        addTrailers(mTrailerScroll, row.getString(DataContract.COL_TRAILERS));

        //ALLy content descriptions for dynamic content
        String description = mResources.getString(R.string.movie_detail_view_description,
                row.getString(DataContract.COL_TITLE), row.getString(DataContract.COL_FORMATS), row.getString(DataContract.COL_DATE),
                row.getString(DataContract.COL_RATING));
        mDetailView.setContentDescription(description);
        mSynopsis.setContentDescription(overview);
        String artDesc = mResources.getString(R.string.poster_description, row.getString(DataContract.COL_TITLE));
        mPosterImage.setContentDescription(artDesc);
        String barcodeDesc = mResources.getString(R.string.barcode_description, row.getString(DataContract.COL_UPC));
        mBarcodeImage.setContentDescription(barcodeDesc);

        mDetailMovieShare = mResources.getString(R.string.detail_movie_desc, row.getString(DataContract.COL_TITLE),
                row.getString(DataContract.COL_FORMATS), row.getString(DataContract.COL_DATE),
                row.getString(DataContract.COL_RATING), row.getString(DataContract.COL_UPC));

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(mDetailMovieShare));
        }

        //Setup Delete and Save Buttons for Movie View, Book View and WishList View
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
                String deleteSelection = DataContract.MovieEntry.COLUMN_UPC + " = ?";

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
                ContentValues update = Utility.makeUpdateValues(Store, Notes, DETAIL_MOVIE_LOADER);
                String saveSelection = DataContract.MovieEntry.COLUMN_UPC + " = ?";
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


    private void addTrailers(LinearLayout view, String trailers){

        //Clear any old views that still exist
        if(view.getChildCount() > 0){ view.removeAllViews();}

        if(trailers == null || trailers.equals("")){
            return;
        }

        LayoutInflater vi = getActivity().getLayoutInflater();
        final String[] tempTrail;
        //Log.v("AddNew: ", trailers);
        int i = 0;
        try{
            tempTrail = trailers.split(",");
            if(tempTrail.length > 0) {

                for(String url: tempTrail){
                    View v =  vi.inflate(R.layout.trailer_tile, view, false);
                    TextView listText = (TextView) v.findViewById(R.id.list_item_trailer_text);
                    String text = mResources.getString(R.string.trailer_play_description, String.valueOf(i+1));
                    //ALLy Content description for trailers
                    v.setContentDescription(text);
                    v.setFocusable(true);
                    listText.setText(text);
                    final Uri trailerUrl = Uri.parse(url);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, trailerUrl);
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
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

    public void sendToBrand(){

        Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(TMDB_SITE));
        result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(result.resolveActivity(getActivity().getPackageManager()) != null){
            startActivity(result);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DETAIL_CURRENT_STORE, mStore.getText().toString());
        outState.putString(DETAIL_CURRENT_NOTES, mNotes.getText().toString());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                mParam1,
                DataContract.MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()) {

            inflateViews(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    private void logShareEvent(String activity, String buttonName, String shareable){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, shareable);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "movie_string");
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
