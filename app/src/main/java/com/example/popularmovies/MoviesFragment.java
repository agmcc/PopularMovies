package com.example.popularmovies;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    public MoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        //test picasso image loading
        /*
        ImageView imageView = (ImageView) rootView
                .findViewById(R.id.testImageView);
        Picasso.with(getContext())
                .load("https://scontent-lhr3-1.xx.fbcdn.net/hphotos-xpt1/v/t1.0-9/11693880_904702142931018_4645832345442603442_n.jpg?oh=1e88cc598187c1793a0b1bbf3731b437&oe=5727AE07")
                .into(imageView);
                */
        //
        GridView gridView = (GridView)rootView.findViewById(R.id.gridview);
        gridView.setAdapter(new SampleGridViewAdapter(getContext()));
        return rootView;
    }

}
