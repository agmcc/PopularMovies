package com.example.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment {

    public DetailFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        int gridId = getActivity().getIntent().getIntExtra(Intent.EXTRA_TEXT, 0);

        ImageView imageView = (ImageView)rootView.findViewById(R.id.poster_imageview);
        TextView title = (TextView)(rootView.findViewById(R.id.title_textview));
        TextView date = (TextView)(rootView.findViewById(R.id.date_textview));
        TextView rating = (TextView)(rootView.findViewById(R.id.rating_textview));
        TextView summary = (TextView)(rootView.findViewById(R.id.summary_textview));

        Picasso.with(getContext())
                .load(MovieData.poster_full_size[gridId])
                .into(imageView);
        title.setText(MovieData.title[gridId]);
        date.setText(MovieData.date[gridId]);
        rating.setText(MovieData.vote[gridId]);
        summary.setText(MovieData.overview[gridId]);

        return rootView;
    }

}
