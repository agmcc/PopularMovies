package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class NewGridAdapter extends CursorAdapter {

    private static final int POSTER_INDEX = 1;

    public NewGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setAdjustViewBounds(true);
        return imageView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Picasso.with(context)
                .load(cursor.getString(POSTER_INDEX))
                .into((ImageView) view);
    }
}
