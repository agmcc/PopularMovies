package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView view = new SquaredImageView(context);
//        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String url = cursor.getString(
                cursor.getColumnIndex(MovieContract.Columns.POSTER_THUMB));
        Picasso mPicasso = Picasso.with(context);
        mPicasso.setIndicatorsEnabled(true);
        mPicasso.load(url)
                .placeholder(R.drawable.black_square)
                .into((ImageView) view);
    }
}