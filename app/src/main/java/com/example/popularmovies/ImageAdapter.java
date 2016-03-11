package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private String[] thumbs;

    public ImageAdapter(Context c) {
        mContext = c;

        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.PopularityEntry.CONTENT_URI,
                new String[]{MovieContract.Columns.POSTER_THUMB},
                null, null, null);

        if (cursor != null) {
            thumbs = new String[cursor.getCount()];
            try {
                while (cursor.moveToNext()) {
                    thumbs[cursor.getPosition()] = cursor.getString(0);
                }
            } finally {
                cursor.close();
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(mContext);
            //view.setScaleType(CENTER_INSIDE);//todo change back to centrecrop for release
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        String url = getItem(position);

        Picasso mPicasso = Picasso.with(mContext);
        //mPicasso.setIndicatorsEnabled(true);
        mPicasso.load(url)
                .placeholder(R.drawable.black_square)
                .into(view);

        return view;
    }

    @Override
    public int getCount() {
        return thumbs.length;
    }

    @Override
    public String getItem(int position) {
        return thumbs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

