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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;


/**
 * Created by Jasen Sanders on 10/17/2016.
 * A RecyclerView Adapter used to fill a RecyclerView with data.
 */

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ViewHolder>  {

    private Cursor mCursor;
    final private Context mContext;
    private OnItemClickedListener mClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView artImage;
        public final TextView title;
        public final TextView byline;
        public final TextView addDate;
        public final TextView subText;

        //Set references for all the views
        public ViewHolder(View view){
            super(view);

            artImage = view.findViewById(R.id.thumbnail);
            title = view.findViewById(R.id.headline_title);
            byline = view.findViewById(R.id.byline);
            addDate = view.findViewById(R.id.date_added);
            subText = view.findViewById(R.id.sub_text);
            view.setOnClickListener(this);

        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String upc = mCursor.getString(0);
            String status = mCursor.getString(5);
            Uri send = Utility.uriStateMatcher(status, upc);

            //If this is a Tablet View wide enough for Two Pane, send data to Main Activity callback.
            if(((AppCompatActivity) mContext).findViewById(R.id.detail_container) != null){
                if(mClickListener != null){
                    mClickListener.onItemClicked(send, status);
                }

            }else {
                //Otherwise its not wide enough for Two Pane so send the data to the detail view activity
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation((AppCompatActivity)mContext).toBundle();

                Intent DetailIntent = new Intent(mContext, DetailActivity.class)
                        .setData(send);
                mContext.startActivity(DetailIntent, bundle);
            }

        }

    }

    public ListItemAdapter(Context context){
        mContext = context;
    }

    @Override
    public ListItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Inflate the new view
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_main, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        }else{
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(final ListItemAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        //Load the imageView
        String artUrl = mCursor.getString(1);
        if(Utility.isNetworkAvailable(mContext)){
            Glide.with(mContext).load(artUrl).into(holder.artImage);
        }

        //Get data from cursor
        String title = mCursor.getString(2);
        String byline = mCursor.getString(4);
        String date = mCursor.getString(3);
        String addDate = Utility.addDateToYear(date);
        String subText = mCursor.getString(6);

        //ALLy content descriptions
        String description;
        if(mCursor.getString(5).startsWith("MOVIE")) {
            description = title + " " + byline + " " + "Rated " + subText + "added " + date;
        }else{
            description = title + " " + byline + " " + subText + " Pages " + "added " + date;
        }
        holder.itemView.setContentDescription(description);

        //Set data into views
        holder.title.setText(title);
        holder.byline.setText(byline);
        holder.addDate.setText(addDate);
        holder.subText.setText(subText);

        //If this is a tablet View launch the first item to fill the fragment
        if(position == 0 && ((AppCompatActivity) mContext).findViewById(R.id.detail_container) != null) {
            String upc = mCursor.getString(0);
            String status = mCursor.getString(5);
            Uri send = Utility.uriStateMatcher(status, upc);
            //Send data to Main Activity callback
            if (mClickListener != null) {
                mClickListener.onItemClicked(send, status);
            }


        }

    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        this.notifyDataSetChanged();

    }

    public interface OnItemClickedListener{
        void onItemClicked(Uri data, String status);
    }

    public void setOnItemClickedListener(OnItemClickedListener listener){
        mClickListener = listener;
    }

    public Cursor getCursor() {
        return mCursor;
    }


}
