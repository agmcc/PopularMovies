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

import com.example.popularmovies.data.MovieContract.Columns;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//        int gridId = getActivity().getIntent().getIntExtra(Intent.EXTRA_TEXT, 0);
//
//        ImageView imageView = (ImageView) rootView.findViewById(R.id.poster_imageview);
//        TextView title = (TextView) (rootView.findViewById(R.id.title_textview));
//        TextView date = (TextView) (rootView.findViewById(R.id.date_textview));
//        TextView rating = (TextView) (rootView.findViewById(R.id.rating_textview));
//        TextView summary = (TextView) (rootView.findViewById(R.id.summary_textview));
//
//        final String[] projection = {
//                Columns.POSTER_FULL,
//                Columns.TITLE,
//                Columns.DATE,
//                Columns.RATING,
//                Columns.SUMMARY,
//        };
//
//        final int posterInd = 0;
//        final int titleInd = 1;
//        final int dateInd = 2;
//        final int ratingInd = 3;
//        final int summaryInd = 4;
//
//        Uri uri = MovieContract.PopularityEntry.buildPopularityUri(gridId + 1);
//
//        Cursor cursor = getContext().getContentResolver().query(
//                uri,
//                projection,
//                null, null, null);
//
//        if (cursor != null) {
//            try {
//                if (cursor.moveToFirst()) {
//                    Picasso.with(getContext())
//                            .load(cursor.getString(posterInd))
//                            .into(imageView);
//
//                    title.setText(cursor.getString(titleInd));
//                    date.setText(cursor.getString(dateInd));
//                    rating.setText(cursor.getString(ratingInd));
//                    summary.setText(cursor.getString(summaryInd));
//                }
//            } finally {
//                cursor.close();
//            }
//        }
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

        final String[] projection = {
                Columns.POSTER_FULL,
                Columns.TITLE,
                Columns.DATE,
                Columns.RATING,
                Columns.SUMMARY,
        };

        return new CursorLoader(
                getActivity(),
                intent.getData(),
                projection,
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        DetailViewHolder viewHolder = (DetailViewHolder)getView().getTag();

        final int posterInd = 0;
        final int titleInd = 1;
        final int dateInd = 2;
        final int ratingInd = 3;
        final int summaryInd = 4;

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    Picasso.with(getContext())
                            .load(cursor.getString(posterInd))
                            .into(viewHolder.imageView);

                    viewHolder.title.setText(cursor.getString(titleInd));
                    viewHolder.date.setText(cursor.getString(dateInd));
                    viewHolder.rating.setText(cursor.getString(ratingInd));
                    viewHolder.summary.setText(cursor.getString(summaryInd));
                }
            } finally {
                cursor.close();
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

        public DetailViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.poster_imageview);
            title = (TextView) view.findViewById(R.id.title_textview);
            date = (TextView) view.findViewById(R.id.date_textview);
            rating = (TextView) view.findViewById(R.id.rating_textview);
            summary = (TextView) view.findViewById(R.id.summary_textview);
        }
    }
}
