package com.example.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.MovieContract.Columns;
import com.example.popularmovies.data.Serializer;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.HashMap;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 1;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        DetailViewHolder detailViewHolder = new DetailViewHolder(rootView);
        rootView.setTag(detailViewHolder);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();

        if (intent == null)
            return null;

        String sortTable = null;
        String sortId = null;
        if (intent.getData() == MovieContract.PopularityEntry.CONTENT_URI) {
            sortTable = MovieContract.PopularityEntry.TABLE_NAME;
            sortId = MovieContract.PopularityEntry._ID;
        } else if (intent.getData() == MovieContract.RatingEntry.CONTENT_URI) {
            sortTable = MovieContract.RatingEntry.TABLE_NAME;
            sortId = MovieContract.RatingEntry._ID;
        } else {
            sortTable = MovieContract.FavouritesEntry.TABLE_NAME;
            sortId = MovieContract.FavouritesEntry._ID;
        }

        final String[] projection = {
                Columns.POSTER_FULL,
                Columns.TITLE,
                Columns.DATE,
                Columns.RATING,
                Columns.SUMMARY,
                Columns.TRAILERS,
        };

        //or could try and change intent
        return new CursorLoader(getActivity(),
                intent.getData(),
                projection,
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        DetailViewHolder viewHolder = (DetailViewHolder) getView().getTag();

        final int posterInd = 0;
        final int titleInd = 1;
        final int dateInd = 2;
        final int ratingInd = 3;
        final int summaryInd = 4;
        final int trailersInd = 5;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Picasso.with(getContext())
                        .load(cursor.getString(posterInd))
                        .into(viewHolder.imageView);

                viewHolder.title.setText(cursor.getString(titleInd));
                viewHolder.date.setText(cursor.getString(dateInd));
                viewHolder.rating.setText(cursor.getString(ratingInd));
                viewHolder.summary.setText(cursor.getString(summaryInd));

                byte[] trailerByteArray = cursor.getBlob(trailersInd);
                HashMap<String, URL> trailerMap =
                        (HashMap<String, URL>) Serializer.deserialize(trailerByteArray);

                //Temp- will feed into recycler view
                StringBuilder stringBuilder = new StringBuilder("Trailers\n");

                for (String key : trailerMap.keySet()) {
                    for (URL value : trailerMap.values()) {
                        stringBuilder.append(key + "\t" + value.toString() + "\n");
                    }
                }
                viewHolder.trailer.setText(stringBuilder.toString());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static class DetailViewHolder {

        public final ImageView imageView;
        public final TextView title;
        public final TextView date;
        public final TextView rating;
        public final TextView summary;
        public final TextView trailer;

        public DetailViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.poster_imageview);
            title = (TextView) view.findViewById(R.id.title_textview);
            date = (TextView) view.findViewById(R.id.date_textview);
            rating = (TextView) view.findViewById(R.id.rating_textview);
            summary = (TextView) view.findViewById(R.id.summary_textview);
            trailer = (TextView) view.findViewById(R.id.trailer_textview);
        }
    }
}
