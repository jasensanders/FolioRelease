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

package com.enrandomlabs.jasensanders.v1.folio.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.enrandomlabs.jasensanders.v1.folio.DetailActivity;
import com.enrandomlabs.jasensanders.v1.folio.R;
import com.enrandomlabs.jasensanders.v1.folio.Utility;
import com.enrandomlabs.jasensanders.v1.folio.database.FolioProvider;


/**
 * Created by Jasen Sanders on 10/11/2016.
 * Provider for a scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetProvider extends AppWidgetProvider {

    public static final String DETAIL_ACTION = "com.enrandomlabs.jasensanders.v1.folio.widget.DETAIL_ACTION";
    public static final String UPC_ITEM_URI = "com.enrandomlabs.jasensanders.v1.folio.widget.UPC_ITEM";

    public static final String PREF_VIEW_TYPE_KEY = "com.enrandomlabs.jasensanders.v1.folio.widget.VIEW_TYPE/";
    public static final String PREF_SORT_TYPE_KEY = "com.enrandomlabs.jasensanders.v1.folio.widget.SORT_TYPE/";
    public static final String PREF_SORT_ORDER_KEY = "com.enrandomlabs.jasensanders.v1.folio.widget.SORT_ORDER/";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {

            //Get Views
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detail);

            // Create an Intent to bind RemoteViews Service to views
            Intent intent = new Intent(context, DetailWidgetRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Set up the collection adapter based on SDK_INT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(views, intent);
            } else {
                setRemoteAdapterV11(views, intent);
            }

            //Create Pending Intent and set onClick method for config button.
            Intent configActivityIntent = new Intent(context, WidgetConfigureActivity.class);
            configActivityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            final PendingIntent configIntent = PendingIntent.getActivity(context, 0, configActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_config_button, configIntent);

            //Setup pending intent template to be overridden by filin intent
            Intent clickDetailIntent = new Intent(context, DetailWidgetProvider.class);
            clickDetailIntent.setAction(DETAIL_ACTION);
            clickDetailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent clickPendingIntentTemplate = PendingIntent.getBroadcast(context, 0, clickDetailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);

            //Set EmptyView
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            //Set Title based on viewType
            String viewLabel = Utility.widgetViewLabel(appWidgetId, context);
            views.setTextViewText(R.id.widget_label, viewLabel);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (FolioProvider.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        }

        //This is where we receive the combined pending/fillin intent and launch  the Detail activity
        //with data from the remote views service item.
        if(DetailWidgetProvider.DETAIL_ACTION.equals(intent.getAction())){
            Uri data = Uri.parse(intent.getStringExtra(UPC_ITEM_URI));
            if(data != null){
                Intent DetailIntent = new Intent(context, DetailActivity.class)
                        .setData(data);
                context.startActivity(DetailIntent);

            }
        }

    }

    //Use this method to delete widget settings based on appWidgetID
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        super.onDeleted(context, appWidgetIds);
        //Delete the settings for each widgetID
        //Convention: com.enrandomlabs.jasensanders.v1.folio.widget + "." + (VIEW_TYPE_KEY) + String.valueOf(appWidgetId)
        for(int appwidgetId: appWidgetIds){
            String viewKey = PREF_VIEW_TYPE_KEY + String.valueOf(appwidgetId);
            String sortKey = PREF_SORT_TYPE_KEY + String.valueOf(appwidgetId);
            String orderKey = PREF_SORT_ORDER_KEY + String.valueOf(appwidgetId);
            Utility.deletePreference(context, viewKey);
            Utility.deletePreference(context, sortKey);
            Utility.deletePreference(context, orderKey);
        }


    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(@NonNull final RemoteViews views, Intent adapterIntent) {
        views.setRemoteAdapter(R.id.widget_list, adapterIntent);
    }

    /**
     * Sets the remote adapter used to fill in the list items
     *
     * @param views RemoteViews to set the RemoteAdapter
     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(@NonNull final RemoteViews views, Intent adapterIntent) {
        views.setRemoteAdapter(0, R.id.widget_list, adapterIntent);
    }
}
