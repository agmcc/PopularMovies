package com.example.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;

    private final List<String> urls = new ArrayList<String>();

    public ImageAdapter(Context c) {
        mContext = c;

        Collections.addAll(urls, Data.URLS);
        ArrayList<String> copy = new ArrayList<String>(urls);
        urls.addAll(copy);
        urls.addAll(copy);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(mContext);
            view.setScaleType(CENTER_CROP);
        }
        String url = getItem(position);

        Picasso.with(mContext)
                .load(url)
                .placeholder(R.drawable.black_square)
                .fit()
                .into(view);

        return view;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public String getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

