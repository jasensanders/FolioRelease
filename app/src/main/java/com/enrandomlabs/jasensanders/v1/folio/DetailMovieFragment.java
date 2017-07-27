package com.enrandomlabs.jasensanders.v1.folio;

import android.content.ContentValues;
import android.content.Context;
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

    private ShareActionProvider mShareActionProvider;
    private Uri mParam1;

    private LayoutInflater mInflater;
    private Context mContext = getActivity();
    private Resources rs;
    private String STATUS;
    private String StateSavedStore;
    private String StateSavedNotes;

    private View rootView;
    private TextView error;
    private TextView title;
    private TextView byline;
    private TextView authors;
    private ImageView posterImage;
    private ImageView branding;
    private ImageView barcodeImage;
    private TextView releaseDate;
    private TextView subTextOne;
    private TextView subTextTwo;
    private TextView synopsis;
    private EditText store;
    private EditText notes;
    private LinearLayout trailerScroll;
    private LinearLayout detailView;
    private CheckBox favButton;
    private Button searchRetail;
    private Button DeleteButton;
    private Button SaveButton;

    private String DetailMovieShare;

    private FirebaseAnalytics mFirebaseAnalytics;

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
        LogActionEvent(ACTIVITY_NAME, "ActivityStarted", "action");
        rs = getResources();
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
            StateSavedStore = savedInstanceState.getString(DETAIL_CURRENT_STORE);
            StateSavedNotes = savedInstanceState.getString(DETAIL_CURRENT_NOTES);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        // Inflate the layout for this fragment
         rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initializeViews();
        getLoaderManager().initLoader(DETAIL_MOVIE_LOADER, null, this);

        return rootView;
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
            mShareActionProvider.setShareIntent(createShareIntent(DetailMovieShare));
            mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {


                    LogShareEvent(ACTIVITY_NAME, "ShareButton", DetailMovieShare);

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
        error = (TextView) rootView.findViewById(R.id.error);
        error.setVisibility(View.GONE);

        //Movie Details Area
        detailView = (LinearLayout) rootView.findViewById(R.id.details);
        title = (TextView) rootView.findViewById(R.id.detail_view_title);
        byline = (TextView) rootView.findViewById(R.id.detail_view_byline);
        authors = (TextView) rootView.findViewById(R.id.detail_view_authors);
        posterImage = (ImageView) rootView.findViewById(R.id.posterView);
        barcodeImage = (ImageView) rootView.findViewById(R.id.upcBarcodeImage);
        releaseDate = (TextView) rootView.findViewById(R.id.releaseDate);
        subTextOne = (TextView) rootView.findViewById(R.id.detail_subtext1);
        subTextTwo = (TextView) rootView.findViewById(R.id.detail_subtext2);
        branding = (ImageView) rootView.findViewById(R.id.branding);
        favButton = (CheckBox) rootView.findViewById(R.id.FavButton);
        searchRetail = (Button) rootView.findViewById(R.id.search_retail);
        synopsis = (TextView) rootView.findViewById(R.id.synopsis);
        store = (EditText) rootView.findViewById(R.id.store);
        notes = (EditText) rootView.findViewById(R.id.notes);
        trailerScroll = (LinearLayout) rootView.findViewById(R.id.trailer_scroll);
        DeleteButton = (Button) rootView.findViewById(R.id.delete_button);
        SaveButton = (Button) rootView.findViewById(R.id.save_button);

    }

    private void inflateViews(Cursor row){

        STATUS = row.getString(DataContract.COL_STATUS);
        title.setText(row.getString(DataContract.COL_TITLE));
        byline.setVisibility(View.GONE);
        authors.setVisibility(View.GONE);
        Glide.with(getActivity()).load(row.getString(DataContract.COL_POSTER)).fitCenter().into(posterImage);
        Glide.with(getActivity()).load(row.getString(DataContract.COL_BARCODE)).fitCenter().into(barcodeImage);
        releaseDate.setText(Utility.dateToYear(row.getString(DataContract.COL_DATE)));
        String ratingRuntime = String.format(rs.getString(R.string.rating_runtime),
                row.getString(DataContract.COL_RATING), row.getString(DataContract.COL_RUNTIME) );
        subTextOne.setText(ratingRuntime);
        subTextTwo.setText(row.getString(DataContract.COL_FORMATS));
        branding.setImageResource(R.drawable.tmdb_brand_120_47);
        branding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToBrand();
            }
        });
        String overview = String.format(rs.getString(R.string.overview), row.getString(DataContract.COL_SYNOPSIS));
        synopsis.setText(overview);

        //Hide FavButton. This is not a wish view.
        favButton.setVisibility(View.GONE);

        //Restore from Saved State if necessary.
        if(StateSavedStore != null ){
            store.setText(StateSavedStore);
        }else {
            store.setText(row.getString(DataContract.COL_STORE));
        }
        if(StateSavedNotes != null){
            notes.setText(StateSavedNotes);
        }else {
            notes.setText(row.getString(DataContract.COL_NOTES));
        }

        addTrailers(trailerScroll, row.getString(DataContract.COL_TRAILERS));

        //ALLy content descriptions for dynamic content
        String description =rs.getString(R.string.movie_detail_view_description,
                row.getString(DataContract.COL_TITLE), row.getString(DataContract.COL_FORMATS), row.getString(DataContract.COL_DATE),
                row.getString(DataContract.COL_RATING));
        detailView.setContentDescription(description);
        synopsis.setContentDescription(overview);
        String artDesc = rs.getString(R.string.poster_description, row.getString(DataContract.COL_TITLE));
        posterImage.setContentDescription(artDesc);
        String barcodeDesc = rs.getString(R.string.barcode_description, row.getString(DataContract.COL_UPC));
        barcodeImage.setContentDescription(barcodeDesc);

        DetailMovieShare = rs.getString(R.string.detail_movie_desc, row.getString(DataContract.COL_TITLE),
                row.getString(DataContract.COL_FORMATS), row.getString(DataContract.COL_DATE),
                row.getString(DataContract.COL_RATING), row.getString(DataContract.COL_UPC));

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(DetailMovieShare));
        }

        //Setup Delete and Save Buttons for Movie View, Book View and WishList View
        final String CurrentUPC = mParam1.getLastPathSegment();

        //Set searchRetail click listener
        searchRetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogActionEvent(ACTIVITY_NAME, "SearchRetailersButton", "action");
                Uri send = mParam1;
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle();
                Intent SearchRetailIntent = new Intent(getActivity(), RetailerSearchActivity.class);
                SearchRetailIntent.setData(send);
                startActivity(SearchRetailIntent, bundle);
            }
        });

        DeleteButton.setOnClickListener(new View.OnClickListener(){
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
                    Toast.makeText(getActivity(), "Item removed from Folio.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        SaveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                //Get input changes
                String Store = store.getText().toString();
                String Notes = notes.getText().toString();

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
                    Toast.makeText(getActivity(), "Updates saved to Folio.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void addTrailers(LinearLayout view, String trailers){

        if(view.getChildCount() > 0){ view.removeAllViews();}

        final String[] tempTrail;
        //Log.v("AddNew: ", trailers);
        int i = 0;
        try{
            tempTrail = trailers.split(",");
            if(tempTrail != null || tempTrail.length > 0) {

                for(String url: tempTrail){
                    View v =  mInflater.inflate(R.layout.trailer_tile, view, false);
                    TextView listText = (TextView) v.findViewById(R.id.list_item_trailer_text);
                    String text = rs.getString(R.string.trailer_play_description, String.valueOf(i+1));
                    //ALLy Content description for trailers
                    v.setContentDescription(text);
                    v.setFocusable(true);
                    listText.setText(text);
                    final Uri trailerUrl = Uri.parse(url);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, trailerUrl);
                            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
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
        outState.putString(DETAIL_CURRENT_STORE, store.getText().toString());
        outState.putString(DETAIL_CURRENT_NOTES, notes.getText().toString());
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

    private void LogShareEvent(String activity, String buttonName, String shareable){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, shareable);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "movie_string");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
    }

    private void LogActionEvent(String activity, String actionName, String type ){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, actionName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
