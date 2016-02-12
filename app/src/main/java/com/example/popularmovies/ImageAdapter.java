package com.example.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private String[] imageUrls = {
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
            "https://www.facebook.com/photo.php?fbid=904702142931018&l=bc3c233949",
    };


    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return imageUrls.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }
/*
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
         //Picasso.with(mContext).load(imageUrls[position]);
        // imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }
    */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = (ImageView) convertView;
        if (view == null) {
            view = new ImageView(mContext);
            view.setScaleType(CENTER_CROP);
        }
        // Get the image URL for the current position.
        String url = imageUrls[position];

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(mContext) //
                .load(url) //
                .into(view);
        return view;
    }
}

