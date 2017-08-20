package com.enrandomlabs.jasensanders.v1.folio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.enrandomlabs.jasensanders.v1.folio.barcode.BarcodeActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.analytics.FirebaseAnalytics;

public class RetailerSearchActivity extends AppCompatActivity {
    private static final String LOG_TAG = RetailerSearchActivity.class.getSimpleName();
    private static final String ACTIVITY_NAME = "RetailerSearchActivity";

    //State Variable keys
    private static final String SEARCH_CURRENT_UPC = "SEARCH_CURRENT_UPC";
    private static final String SEARCH_CURRENT_TITLE = "SEARCH_CURRENT_TITLE";
    private static final String SEARCH_CURRENT_QUERY = "SEARCH_CURRENT_QUERY";

    //Search Strings
    private static final String SEARCH_AMAZON = "http://www.amazon.com/gp/search?keywords=";
    private static final String SEARCH_TARGET = "http://www.target.com/s?searchTerm=";
    private static final String SEARCH_WALMART = "https://www.walmart.com/search/?query=";
    private static final String SEARCH_EBAY = "http://www.ebay.com/sch/i.html?_nkw=";
    private static final String SEARCH_BEST_BUY =
            "http://www.bestbuy.com/site/searchpage.jsp?_dyncharset=UTF-8&id=pcat17071&type=page&sc=Global&cp=1&nrp=&sp=&qp=&list=n&af=true&iht=y&usc=All+Categories&ks=960&keys=keys&st=";
    private static final String SEARCH_TOYSRUS = "http://www.toysrus.com/search/index.jsp?kw=";

    //Barcode flag
    private static final int RC_BARCODE_CAPTURE = 9001;

    //Views
    private EditText mUpcInput;
    private EditText mTitleInput;

    //Query to share
    private String mQuery;
    private ShareActionProvider mShareActionProvider;

    //Firebase Analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    //Firebase Ads
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_search);
        initializeViews();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAdView = (AdView) findViewById(R.id.retailerAdView);
        //mAdView.setAdUnitId(BuildConfig.RETAIL_SEARCH_AD_ID);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice("F18A92219F2A59F1")  //My Nexus 5x Test Phone
                .addTestDevice("ECF8814A2889BB1528CE0F6E1BCFA7ED")  //My GS3 Test Phone
                .build();
        mAdView.loadAd(adRequest);

        //If we were sent here from Detail Activity/ Fragment update UpcInput
        Uri data = getIntent().getData();
        if(data != null){mUpcInput.setText(data.getLastPathSegment());}

        //If App is still in session, then restore state data
        if(savedInstanceState != null){
            String restoreUpc = savedInstanceState.getString(SEARCH_CURRENT_UPC);
            if(restoreUpc != null){
                mUpcInput.setText(restoreUpc);
            }
            String restoreTitle = savedInstanceState.getString(SEARCH_CURRENT_TITLE);
            if(restoreTitle != null){
                mTitleInput.setText(restoreTitle);
            }
            mQuery = savedInstanceState.getString(SEARCH_CURRENT_QUERY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_retail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent(null));
            mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {

                    String shareContent = intent.getStringExtra(Intent.EXTRA_TEXT);
                    LogShareEvent(ACTIVITY_NAME, "ShareButton", shareContent);

                    return false;
                }
            });
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeActivity.BarcodeObject);
                    //Update the EditText
                    mUpcInput.setText(barcode.displayValue);

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
        // Save the user's current state
        savedInstanceState.putString(SEARCH_CURRENT_UPC, mUpcInput.getText().toString());
        savedInstanceState.putString(SEARCH_CURRENT_TITLE,mTitleInput.getText().toString());
        savedInstanceState.putString(SEARCH_CURRENT_QUERY, mQuery);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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


    public void searchAmazon(View view){
        String query = launchTermPriority();
        if(query != null){
            LogSearchEvent(ACTIVITY_NAME, "searchAmazon", query, "url");
            //update Query Saved State
            mQuery = SEARCH_AMAZON + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(mQuery));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_AMAZON, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){

                startActivity(startSomething);
            }
        }

    }
    public void searchTarget(View view){
        String query = launchTermPriority();
        if(query != null){
            LogSearchEvent(ACTIVITY_NAME, "searchTarget", query, "url");
            //update Query Saved State
            mQuery = SEARCH_TARGET + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(mQuery));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_TARGET, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }

    }
    public void searchWalmart(View view){
        String query = launchTermPriority();
        if(query != null){
            LogSearchEvent(ACTIVITY_NAME, "searchWalmart", query, "url");
            //update Query Saved State
            mQuery = SEARCH_WALMART + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(mQuery));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_WALMART, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }

    }
    public void searchEbay(View view){
        String query = launchTermPriority();
        if(query != null){
            LogSearchEvent(ACTIVITY_NAME, "searchEbay", query, "url");
            //update Query Saved State
            mQuery = SEARCH_EBAY + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(mQuery));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_EBAY, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }

    }

    public void searchBestBuy(View view){
        String query = launchTermPriority();
        if(query != null){
            LogSearchEvent(ACTIVITY_NAME, "searchBestBuy", query, "url");
            //update Query Saved State
            mQuery = SEARCH_BEST_BUY + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(mQuery));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_BEST_BUY, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }
    }

    public void searchToysRUs(View view){
        String query = launchTermPriority();
        if(query != null){
            LogSearchEvent(ACTIVITY_NAME, "SearchToysRUS", query, "url");
            //update Query Saved State
            mQuery = SEARCH_TOYSRUS + query;

            //update share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(mQuery));
            }

            //Start the intent
            Intent startSomething = createLaunchIntent(SEARCH_TOYSRUS, query);
            if(startSomething.resolveActivity(getPackageManager()) != null){
                startActivity(startSomething);
            }
        }
    }

    public void launchBarcodeScanner(View view){
        LogActionEvent(ACTIVITY_NAME, "launchBarcodeScanner", "action");

        Intent intent = new Intent(this, BarcodeActivity.class);
        intent.putExtra(BarcodeActivity.AutoFocus, true);
        intent.putExtra(BarcodeActivity.UseFlash, false);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);

    }

    private String launchTermPriority(){
        String upcText = mUpcInput.getText().toString();
        String titleText = mTitleInput.getText().toString();
        if(upcText.length() == 12){
            return upcText;
        }else{
            if(titleText.length() > 0){
                return titleText.replace(" ", "+");
            }
            return null;
        }
    }
    private void initializeViews(){

        mUpcInput = (EditText) findViewById(R.id.upc_input);
        mTitleInput = (EditText) findViewById(R.id.title_input);

    }

    private Intent createLaunchIntent(String url, String searchTerm){
        String query = url+searchTerm;
        Intent result = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
        result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return result;
    }

    private Intent createShareIntent(String queryUrl) {
        if(queryUrl != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, queryUrl);
            return shareIntent;
        }else{
            String placeHolder = SEARCH_AMAZON + launchTermPriority();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, placeHolder);
            return shareIntent;
        }
    }

    private void clearViews(){
        mUpcInput.setText("");
        mTitleInput.setText("");
    }

    private void LogSearchEvent(String activity, String buttonName, String term, String type ){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, term);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void LogActionEvent(String activity, String buttonName, String type ){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void LogShareEvent(String activity, String buttonName, String shareable){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, activity);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, buttonName);
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, shareable);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "url");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
    }
}
