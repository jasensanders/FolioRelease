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

import static com.enrandomlabs.jasensanders.v1.folio.database.DataContract.W_COL_FIVE;
import static com.enrandomlabs.jasensanders.v1.folio.database.DataContract.W_COL_NINE;

/**
 * A simple {@link Fragment} subclass.
 *
 * Use the {@link DetailWishFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailWishFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = DetailWishFragment.class.getSimpleName();
    private static final String ACTIVITY_NAME = "DetailWishFragment";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String WISH_DETAIL_URI = "WISH_URI";
    private static final String DETAIL_WISH_CURRENT_STORE = "DETAIL_WISH_CURRENT_STORE";
    private static final String DETAIL_WISH_CURRENT_NOTES = "DETAIL_WISH_CURRENT_NOTES";

    //Branding send to URLs
    private static final String GOOGLE_BOOKS = "https://books.google.com/";
    private static final String TMDB_SITE = "https://www.themoviedb.org/";
    private static final int DETAIL_WISH_LOADER = 202;

    private FirebaseAnalytics mFirebaseAnalytics;
    private ShareActionProvider mShareActionProvider;
    private Uri mParam1;
    private String mDetailShare;
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



    public DetailWishFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1
     * @return A new instance of fragment DetailWishFragment.
     */

    public static DetailWishFragment newInstance(Uri param1) {
        DetailWishFragment fragment = new DetailWishFragment();
        Bundle args = new Bundle();
        args.putParcelable(WISH_DETAIL_URI, param1);
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
            mParam1 = getArguments().getParcelable(WISH_DETAIL_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initializeViews();
        getLoaderManager().initLoader(DETAIL_WISH_LOADER, null, this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state.
            mStateSavedStore = savedInstanceState.getString(DETAIL_WISH_CURRENT_STORE);
            mStateSavedNotes = savedInstanceState.getString(DETAIL_WISH_CURRENT_NOTES);
        }
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
            mShareActionProvider.setShareIntent(createShareIntent(mDetailShare));
            mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
                    logShareEvent(ACTIVITY_NAME, "ShareButton", mDetailShare);
                    return false;
                }
            });
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DETAIL_WISH_CURRENT_STORE, mStore.getText().toString());
        outState.putString(DETAIL_WISH_CURRENT_NOTES, mNotes.getText().toString());

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

        //A Wish Item in database could be a Book or a Movie
        //Find out which one we have
        mStatus = row.getString(DataContract.W_COL_STATUS);
        mTitle.setText(row.getString(DataContract.W_COL_TITLE));
        if(mStatus.startsWith("MOVIE")) {
            //Its a movie so load views accordingly
            Glide.with(getActivity()).load(row.getString(DataContract.W_COL_ELEVEN)).fitCenter().into(mPosterImage);
            String ratingRuntime = String.format(mResources.getString(R.string.rating_runtime),
                    row.getString(DataContract.W_COL_FIFTEEN), row.getString(W_COL_NINE) );
            mSubTextOne.setText(ratingRuntime);
            mSubTextTwo.setText(row.getString(W_COL_FIVE));
            mBranding.setImageResource(R.drawable.tmdb_brand_120_47);
            mBranding.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendToBrand();
                }
            });
            mByline.setVisibility(View.GONE);
            mAuthors.setVisibility(View.GONE);
            String overview = String.format(mResources.getString(R.string.overview), row.getString(DataContract.W_COL_SIXTEEN));
            mSynopsis.setText(overview);
            addTrailers(mTrailerScroll, row.getString(DataContract.W_COL_SEVENTEEN));

            //ALLy content descriptions for dynamic content
            String description =getActivity().getResources().getString(R.string.movie_detail_view_description,
                    row.getString(DataContract.W_COL_TITLE), row.getString(W_COL_FIVE), row.getString(DataContract.W_COL_DATE),
                    row.getString(DataContract.W_COL_FIFTEEN));
            mDetailView.setContentDescription(description);
            mSynopsis.setContentDescription(overview);

            mDetailShare = mResources.getString(R.string.detail_movie_desc, row.getString(DataContract.W_COL_TITLE),
                    row.getString(DataContract.W_COL_FIVE), row.getString(DataContract.W_COL_DATE),
                    row.getString(DataContract.W_COL_FIFTEEN), row.getString(DataContract.W_COL_UPC));
        }
        else{
            //Its a book so load views accordingly
            Glide.with(getActivity()).load(row.getString(DataContract.W_COL_THUMB)).fitCenter().into(mPosterImage);
            mSubTextOne.setText(row.getString(DataContract.W_COL_ELEVEN));
            mSubTextTwo.setText(row.getString(DataContract.W_COL_FIFTEEN));
            mBranding.setImageResource(R.drawable.google_logo);
            mBranding.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendToBrand();
                }
            });
            mByline.setText(row.getString(DataContract.W_COL_FIVE));
            mAuthors.setText(row.getString(W_COL_NINE));
            String overview = String.format(mResources.getString(R.string.overview), row.getString(DataContract.W_COL_SIX));
            mSynopsis.setText(overview);
            mTrailerScroll.setVisibility(View.GONE);


            //ALLy content descriptions for dynamic content
            String description = mResources.getString(R.string.book_detail_view_description,
                    row.getString(DataContract.W_COL_TITLE), row.getString(W_COL_NINE), row.getString(DataContract.W_COL_DATE));
            mDetailView.setContentDescription(description);
            mSynopsis.setContentDescription(overview);

            mDetailShare = getActivity().getResources().getString(R.string.detail_book_desc, row.getString(DataContract.W_COL_TITLE),
                    row.getString(W_COL_NINE), row.getString(DataContract.W_COL_DATE), row.getString(DataContract.W_COL_UPC));

        }

        Glide.with(this).load(row.getString(DataContract.W_COL_SEVEN)).fitCenter().into(mBarcodeImage);
        mReleaseDate.setText(Utility.dateToYear(row.getString(DataContract.W_COL_DATE)));

        //Set the checkbox accordingly.
        if(!mFavButton.isChecked()){
            mFavButton.setChecked(true);
            mFavButton.setEnabled(false);
        }

        //Restore from Saved State if necessary.
        if(mStateSavedStore != null ){
            mStore.setText(mStateSavedStore);
        }else {
            mStore.setText(row.getString(DataContract.W_COL_STORE));
        }
        if(mStateSavedNotes != null){
            mNotes.setText(mStateSavedNotes);
        }else {
            mNotes.setText(row.getString(DataContract.W_COL_NOTES));
        }

        //Rest of A11Y content descriptions
        String artDesc = mResources.getString(R.string.poster_description, row.getString(DataContract.W_COL_TITLE));
        mPosterImage.setContentDescription(artDesc);
        String barcodeDesc = mResources.getString(R.string.barcode_description, row.getString(DataContract.W_COL_UPC));
        mBarcodeImage.setContentDescription(barcodeDesc);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(mDetailShare));
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
                String deleteSelection = DataContract.WishEntry.COLUMN_UPC + " = ?";

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
                ContentValues update = Utility.makeUpdateValues(Store, Notes, Utility.WISH_ITEM_BY_UPC);
                String saveSelection = DataContract.WishEntry.COLUMN_UPC + " = ?";
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


    private void addTrailers(LinearLayout view, String trailers) {

        //Clear any views
        if (view.getChildCount() > 0) {view.removeAllViews();}

        //If there are no trailers, then nothing to do
        if(trailers == null || trailers.equals("")){
            return;
        }

        LayoutInflater vi = getActivity().getLayoutInflater();
        final String[] tempTrail;
        //Log.v("AddNew: ", trailers);
        int i = 0;
        try {
            tempTrail = trailers.split(",");
            if (tempTrail.length > 0) {

                for (String url : tempTrail) {
                    View v = vi.inflate(R.layout.trailer_tile, view, false);
                    TextView listText = (TextView) v.findViewById(R.id.list_item_trailer_text);
                    String text = mResources.getString(R.string.trailer_play_description, String.valueOf(i + 1));
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
                    //Log.v("AddNew: " + String.valueOf(i), text + " : " + trailerUrl);

                }

            }

        } catch (NullPointerException e) {
            //Log.e(LOG_TAG, "Error splitting and Adding Trailers", e);
            FirebaseCrash.log("Error splitting and Adding Trailers");
        }
    }

    public void sendToBrand(){
        if(mStatus.startsWith("MOVIE")){
            Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(TMDB_SITE));
            result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(result.resolveActivity(getActivity().getPackageManager()) != null){
                startActivity(result);
            }

        }
        if(mStatus.startsWith("BOOK")){
            Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_BOOKS));
            result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(result.resolveActivity(getActivity().getPackageManager()) != null){
                startActivity(result);
            }

        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                mParam1,
                DataContract.WISH_COLUMNS,
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
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void logShareEvent(String activity, String buttonName, String shareable){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, shareable);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "wish_string");
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
