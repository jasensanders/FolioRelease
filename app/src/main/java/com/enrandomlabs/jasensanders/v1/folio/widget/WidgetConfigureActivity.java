package com.enrandomlabs.jasensanders.v1.folio.widget;

import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.enrandomlabs.jasensanders.v1.folio.R;
import com.enrandomlabs.jasensanders.v1.folio.Utility;

import static com.enrandomlabs.jasensanders.v1.folio.widget.DetailWidgetProvider.PREF_SORT_ORDER_KEY;
import static com.enrandomlabs.jasensanders.v1.folio.widget.DetailWidgetProvider.PREF_SORT_TYPE_KEY;
import static com.enrandomlabs.jasensanders.v1.folio.widget.DetailWidgetProvider.PREF_VIEW_TYPE_KEY;

public class WidgetConfigureActivity extends AppCompatActivity {

    //The particular widget instance we are making settings for.
    private int mAppWidgetId;
    private Spinner mViewSpin;
    private Spinner mSortTypeSpin;
    private Spinner mSortOrderSpin;

    private String mViewSelected;
    private String mSortTypeSelected;
    private String mSortOrderSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the AppWidgetId (Unique Instance of widget)
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        //Make Keys
        final String viewKey = PREF_VIEW_TYPE_KEY + String.valueOf(mAppWidgetId);
        final String sortKey = PREF_SORT_TYPE_KEY + String.valueOf(mAppWidgetId);
        final String orderKey = PREF_SORT_ORDER_KEY + String.valueOf(mAppWidgetId);

        //Get Spinner values
        final String[] viewValues = getResources().getStringArray(R.array.pref_view_type_values);
        final String[] sortTypeValues = getResources().getStringArray(R.array.pref_sort_type_values);
        final String[] sortOrderValues = getResources().getStringArray(R.array.pref_sort_order_values);

        //Get current preferences
        final String viewType = Utility.getStringPreference(this, viewKey, getString(R.string.pref_view_movies));
        final String sortType = Utility.getStringPreference(this, sortKey, getString(R.string.pref_sort_by_add_date));
        final String sortOrder = Utility.getStringPreference(this, orderKey, getString(R.string.pref_sort_order_dsc));

        //Set Result of activity to cancelled in case of activity early termination
        setResultCanceled();

        //Build the config dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_widget_configure, null);



        //Set onClick for each Spinner
        mViewSpin = rootView.findViewById(R.id.widget_view_type);
        mViewSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mViewSelected = viewValues[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                mViewSelected = viewType;

            }
        });
        mViewSpin.setSelection(Utility.indexOf(viewValues, viewType));

        mSortTypeSpin = rootView.findViewById(R.id.widget_sort_type);
        mSortTypeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mSortTypeSelected = sortTypeValues[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                mSortTypeSelected = sortType;

            }
        });
        mSortTypeSpin.setSelection(Utility.indexOf(sortTypeValues, sortType));

        mSortOrderSpin = rootView.findViewById(R.id.widget_sort_order);
        mSortOrderSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mSortOrderSelected = sortOrderValues[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                mSortOrderSelected = sortOrder;

            }
        });
        mSortOrderSpin.setSelection(Utility.indexOf(sortOrderValues, sortOrder));

        //Set the view into the dialog
        builder.setView(rootView);

        //Set the save button
        builder.setPositiveButton(R.string.widget_config_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utility.setStringPreference(getApplicationContext(), viewKey, mViewSelected);
                Utility.setStringPreference(getApplicationContext(), sortKey, mSortTypeSelected);
                Utility.setStringPreference(getApplicationContext(), orderKey, mSortOrderSelected);
                //Notify Widget to update
                notifyUpdateWidget();

                //Set result OK
                setResultOk();
                finish();
            }
        });

        //Set the cancel button
        builder.setNegativeButton(R.string.widget_config_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //Finish Activity
                finish();
            }
        });

        //create then show the dialog
        builder.create();
        builder.show();

    }

    private void setResultCanceled(){
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_CANCELED, resultValue);
    }

    private void setResultOk(){
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
    }

    private void notifyUpdateWidget(){

        //This notifies the Widget Provider to update the other views in the widget
        Intent intent = new Intent(this,DetailWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = {mAppWidgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);


        //This notifies the widget ListView adapter(RemoteViewsService) for this instance (mAppWidgetId)
        // that data has changed.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.widget_list);


    }

}
