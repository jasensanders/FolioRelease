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
import com.enrandomlabs.jasensanders.v1.folio.database.DataContract;

import java.util.concurrent.ExecutionException;



/**
 * Created by Jasen Sanders on 10/11/2016.
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] MOVIE_COLUMNS = {
            DataContract.MovieEntry.COLUMN_UPC,
            DataContract.MovieEntry.COLUMN_DISC_ART,
            DataContract.MovieEntry.COLUMN_TITLE,
            DataContract.MovieEntry.COLUMN_ADD_DATE,
            DataContract.MovieEntry.COLUMN_FORMATS,
            DataContract.MovieEntry.COLUMN_STATUS,
            DataContract.MovieEntry.COLUMN_RATING,
    };
    // these indices must match the projection
    static final int INDEX_MOVIE_UPC = 0;
    static final int INDEX_MOVIE_DISC_ART = 1;
    static final int INDEX_MOVIE_TITLE = 2;
    static final int INDEX_MOVIE_ADD_DATE = 3;
    static final int INDEX_MOVIE_FORMATS = 4;
    static final int INDEX_MOVIE_STATUS = 5;
    static final int INDEX_MOVIE_RATING = 6;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
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

                Uri getOwnedMovies = DataContract.MovieEntry.buildUriAll();
                data = getContentResolver().query(getOwnedMovies,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        DataContract.MovieEntry.COLUMN_ADD_DATE + " DESC");
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
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                //TODO Load Views for each List Item
                //This is the artImage download with glide
                Bitmap ArtImage = null;

                    String ArtResourceUrl = data.getString(INDEX_MOVIE_DISC_ART);
                    try {
                        ArtImage = Glide.with(DetailWidgetRemoteViewsService.this)
                                .load(ArtResourceUrl)
                                .asBitmap()
                                .error(R.drawable.ic_filmstrip_black)
                                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e(LOG_TAG, "Error retrieving large icon from " + ArtResourceUrl, e);
                    }

                String Title = data.getString(INDEX_MOVIE_TITLE);
                String formats = data.getString(INDEX_MOVIE_FORMATS);
                String date = data.getString(INDEX_MOVIE_ADD_DATE);
                String addDate = Utility.addDateToYear(date);
                String rating = data.getString(INDEX_MOVIE_RATING);

                //ALLy support content description
                String description = Title + " " + formats + " " + "Rated "+ rating + "added "+date;

                if (ArtImage != null) {
                    views.setImageViewBitmap(R.id.thumbnail, ArtImage);
                } else {
                    views.setImageViewResource(R.id.thumbnail, R.drawable.ic_filmstrip_black);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }
                views.setTextViewText(R.id.headline_title, Title);
                views.setTextViewText(R.id.byline, formats);
                views.setTextViewText(R.id.date_added, addDate);
                views.setTextViewText(R.id.sub_text, rating);

                //TODO Update this to launch main activity
                final Intent fillInIntent = new Intent();
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
                    return Long.valueOf(data.getString(INDEX_MOVIE_UPC));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
