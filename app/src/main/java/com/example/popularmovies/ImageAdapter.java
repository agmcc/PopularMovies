package com.example.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import static android.widget.ImageView.ScaleType.CENTER_INSIDE;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(mContext);
            view.setScaleType(CENTER_INSIDE);//todo change back to centrecrop for release
        }
        String url = getItem(position);

        Picasso mPicasso = Picasso.with(mContext);
        mPicasso.setIndicatorsEnabled(true);
        mPicasso.load(url)
                .placeholder(R.drawable.black_square)
                .into(view);

        return view;
    }

    @Override
    public int getCount() {
        return MovieData.poster_thumbnail.length;
    }

    @Override
    public String getItem(int position) {
        return MovieData.poster_thumbnail[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

