package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class GridAdapter extends CursorAdapter {

    private static final int POSTER_INDEX = 1;

    public GridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setAdjustViewBounds(true);
        ViewHolder holder = new ViewHolder(imageView);
        imageView.setTag(holder);
        return imageView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = ((ViewHolder) view.getTag()).mImageView;
        Picasso.with(context)
                .load(cursor.getString(POSTER_INDEX))
                .into(imageView);
    }

    private static class ViewHolder {

        public final ImageView mImageView;

        public ViewHolder(ImageView imageView) {
            mImageView = imageView;
        }
    }

}
