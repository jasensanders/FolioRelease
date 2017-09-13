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
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.enrandomlabs.jasensanders.v1.folio.R;
import com.enrandomlabs.jasensanders.v1.folio.Utility;

import java.util.concurrent.ExecutionException;

import static com.enrandomlabs.jasensanders.v1.folio.widget.DetailWidgetProvider.PREF_SORT_ORDER_KEY;
import static com.enrandomlabs.jasensanders.v1.folio.widget.DetailWidgetProvider.PREF_SORT_TYPE_KEY;
import static com.enrandomlabs.jasensanders.v1.folio.widget.DetailWidgetProvider.PREF_VIEW_TYPE_KEY;


/**
 * Created by Jasen Sanders on 10/11/2016.
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String SPACE = " ";

    // these indices must match the projection
    static final int INDEX_UPC = 0;
    static final int INDEX_DISC_ART = 1;
    static final int INDEX_TITLE = 2;
    static final int INDEX_ADD_DATE = 3;
    static final int INDEX_BYLINE = 4;
    static final int INDEX_STATUS = 5;
    static final int INDEX_SUBTEXT = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new RemoteViewsFactory() {

            private int mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            private Cursor data = null;

            //Make Keys
            final String viewKey = PREF_VIEW_TYPE_KEY + String.valueOf(mAppWidgetId);
            final String sortKey = PREF_SORT_TYPE_KEY + String.valueOf(mAppWidgetId);
            final String orderKey = PREF_SORT_ORDER_KEY + String.valueOf(mAppWidgetId);

            String mViewType;
            String mSortOrder;
            String mSortType;
            int LOADER_TYPE = 0;
            Uri mDataRequest;
            private  String[] PROJECT_COLUMNS = null;

            @Override
            public void onCreate() {

                //Load Settings
                loadSettings();

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission

                final long identityToken = Binder.clearCallingIdentity();

                if( loadSettings()) {

                    data = getContentResolver().query(mDataRequest,
                            PROJECT_COLUMNS,
                            null,
                            null,
                            mSortType + SPACE + mSortOrder);
                }

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    Log.e(LOG_TAG, "No Data Received");
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);


                //This is the artImage download with glide
                Bitmap ArtImage = null;
                int errorImage = Utility.errorImageMatcher(LOADER_TYPE);

                String ArtResourceUrl = data.getString(INDEX_DISC_ART);
                try {
                    ArtImage = Glide.with(DetailWidgetRemoteViewsService.this)
                            .load(ArtResourceUrl)
                            .asBitmap()
                            .error(errorImage)
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(LOG_TAG, "Error retrieving large icon from " + ArtResourceUrl, e);
                }

                String title = data.getString(INDEX_TITLE);
                String byline = data.getString(INDEX_BYLINE);
                String date = data.getString(INDEX_ADD_DATE);
                String addDate = Utility.addDateToYear(date);
                String subText = data.getString(INDEX_SUBTEXT);

                //ALLy support content description
                String description = title + " " + byline + " " + subText + " " + date;

                if (ArtImage != null) {
                    views.setImageViewBitmap(R.id.thumbnail, ArtImage);
                } else {
                    views.setImageViewResource(R.id.thumbnail, errorImage);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }
                views.setTextViewText(R.id.headline_title, title);
                views.setTextViewText(R.id.byline, byline);
                views.setTextViewText(R.id.date_added, addDate);
                views.setTextViewText(R.id.sub_text, subText);



                final Intent fillInIntent = new Intent();
                String detailsData = Utility.detailUriMatcher(LOADER_TYPE, data.getString(0)).toString();
                fillInIntent.putExtra(DetailWidgetProvider.UPC_ITEM_URI, detailsData);
                //make a uri for the Item and add it to the intent passed
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.thumbnail, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return Long.valueOf(data.getString(INDEX_UPC));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            private boolean loadSettings(){

                mViewType = Utility.getStringPreference(getApplicationContext(), viewKey, getString(R.string.pref_view_movies));
                LOADER_TYPE = Utility.stateMatcher(mViewType);
                mSortType = Utility.getStringPreference(getApplicationContext(), sortKey, getString(R.string.pref_sort_by_add_date));
                mSortOrder = Utility.getStringPreference(getApplicationContext(), orderKey, getString(R.string.pref_sort_order_dsc));

                mDataRequest = Utility.uriPathMatcher(mViewType);
                PROJECT_COLUMNS = Utility.projectionSearchStateMatcher(LOADER_TYPE);

//                return LOADER_TYPE != 0 && PROJECT_COLUMNS != null && mDataRequest != null
//                        && mSortType != null && mSortOrder != null;
                return true;
            }
        };
    }
}
