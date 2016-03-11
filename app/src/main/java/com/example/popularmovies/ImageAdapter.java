package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private String[] thumbs;

    public ImageAdapter(Context c) {
        mContext = c;
        SQLiteDatabase db = new MovieDbHelper(mContext).getReadableDatabase();
//        Cursor cursor = db.query(
//                MovieContract.PopularityEntry.TABLE_NAME,
//                new String[]{MovieContract.Columns.POSTER_THUMB},
//                null,
//                null,
//                null,
//                null,
//                null
//        );
        final String query = "SELECT " + MovieContract.Columns.POSTER_THUMB
                + " FROM " + MovieContract.PopularityEntry.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            thumbs = new String[cursor.getCount()];
            if (cursor.moveToFirst()) {
                do {
                    thumbs[cursor.getPosition()] = cursor.getString(0);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
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
        //Log.e("Adapter", url);

        Picasso mPicasso = Picasso.with(mContext);
        //mPicasso.setIndicatorsEnabled(true);
        mPicasso.load(url)
                .placeholder(R.drawable.black_square)
                .into(view);

        return view;
    }

    @Override
    public int getCount() {
        //return MovieData.poster_thumbnail.length;
        return thumbs.length;
    }

    @Override
    public String getItem(int position) {
        return thumbs[position];
        //return MovieData.poster_thumbnail[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

