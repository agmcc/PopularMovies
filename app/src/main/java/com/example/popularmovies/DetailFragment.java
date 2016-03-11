package com.example.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.MovieContract.Columns;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment {

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        int gridId = getActivity().getIntent().getIntExtra(Intent.EXTRA_TEXT, 0);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.poster_imageview);
        TextView title = (TextView) (rootView.findViewById(R.id.title_textview));
        TextView date = (TextView) (rootView.findViewById(R.id.date_textview));
        TextView rating = (TextView) (rootView.findViewById(R.id.rating_textview));
        TextView summary = (TextView) (rootView.findViewById(R.id.summary_textview));

//        SQLiteDatabase db = new MovieDbHelper(getContext()).getReadableDatabase();

        final String[] projection = {
                Columns.POSTER_FULL,
                Columns.TITLE,
                Columns.DATE,
                Columns.RATING,
                Columns.SUMMARY,
        };

        final int posterInd = 0;
        final int titleInd = 1;
        final int dateInd = 2;
        final int ratingInd = 3;
        final int summaryInd = 4;

        Uri uri = MovieContract.PopularityEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(gridId + 1))
                .build();

        Cursor cursor = getContext().getContentResolver().query(
                uri,
                projection,
                null, null, null);

        if(cursor != null){
            try {
                if(cursor.moveToFirst()) {
                    Picasso.with(getContext())
                            .load(cursor.getString(posterInd))
                            .into(imageView);

                    title.setText(cursor.getString(titleInd));
                    date.setText(cursor.getString(dateInd));
                    rating.setText(cursor.getString(ratingInd));
                    summary.setText(cursor.getString(summaryInd));
                }
            }finally {
                cursor.close();
            }
        }

//        Cursor cursor = db.query(
//                MovieContract.PopularityEntry.TABLE_NAME,
//                projection,
//                null,
//                null,
//                null,
//                null,
//                null
//        );
//
//        if (cursor != null) {
//            if (cursor.move(gridId + 1)) {
//                Picasso.with(getContext())
//                        .load(cursor.getString(posterInd))
//                        .into(imageView);
//
//                title.setText(cursor.getString(titleInd));
//                date.setText(cursor.getString(dateInd));
//                rating.setText(cursor.getString(ratingInd));
//                summary.setText(cursor.getString(summaryInd));
//            }
//            cursor.close();
//        }
//        db.close();

        return rootView;
    }

}
