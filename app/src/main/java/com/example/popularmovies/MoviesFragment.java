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
        GridView gridView = (GridView)rootView.findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(getContext()));
        return rootView;
    }

}
