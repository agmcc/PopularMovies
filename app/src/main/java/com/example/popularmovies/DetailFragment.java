package com.example.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.popularmovies.data.MovieContract.Columns;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    //todo test with id = 0
    private static final int DETAIL_LOADER = 1;
    private RecyclerView mRecyclerView;
    private DetailAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        DetailViewHolder detailViewHolder = new DetailViewHolder(rootView);
        rootView.setTag(detailViewHolder);
        ToggleButton toggleButton = (ToggleButton) rootView.findViewById(R.id.toggle);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //maybe have content provider method that copies entry to other table?
                if (isChecked) {
                    Log.v(LOG_TAG, "Favourited");
                } else {
                    Log.v(LOG_TAG, "Unfavourited");
                }
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerAdapter = new DetailAdapter(getActivity(), null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);

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

//        String sortTable = null;
//        String sortId = null;
//        if (intent.getData() == MovieContract.PopularityEntry.CONTENT_URI) {
//            sortTable = MovieContract.PopularityEntry.TABLE_NAME;
//            sortId = MovieContract.PopularityEntry._ID;
//        } else if (intent.getData() == MovieContract.RatingEntry.CONTENT_URI) {
//            sortTable = MovieContract.RatingEntry.TABLE_NAME;
//            sortId = MovieContract.RatingEntry._ID;
//        } else {
//            sortTable = MovieContract.FavouritesEntry.TABLE_NAME;
//            sortId = MovieContract.FavouritesEntry._ID;
//        }
        String columnId = intent.getStringExtra(Intent.EXTRA_TEXT);
        Uri contentUri = intent.getData();

        final String[] projection = {
                columnId,
                Columns.POSTER_FULL,
                Columns.TITLE,
                Columns.DATE,
                Columns.RATING,
                Columns.SUMMARY,
                Columns.TRAILERS,
                Columns.REVIEWS
        };

        //or could try and change intent
        return new CursorLoader(getActivity(),
                contentUri,
                projection,
                null, null, null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        DetailViewHolder viewHolder = (DetailViewHolder) getView().getTag();
//
//        final int posterInd = 0;
//        final int titleInd = 1;
//        final int dateInd = 2;
//        final int ratingInd = 3;
//        final int summaryInd = 4;
//        final int trailersInd = 5;
//        final int reviewsInd = 6;

//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                Picasso.with(getContext())
//                        .load(cursor.getString(posterInd))
//                        .into(viewHolder.imageView);
//
//                viewHolder.title.setText(cursor.getString(titleInd));
//                viewHolder.date.setText(cursor.getString(dateInd));
//                viewHolder.rating.setText(cursor.getString(ratingInd));
//                viewHolder.summary.setText(cursor.getString(summaryInd));
//
//                byte[] trailerByteArray = cursor.getBlob(trailersInd);
//                HashMap<String, URL> trailerMap =
//                        (HashMap<String, URL>) Serializer.deserialize(trailerByteArray);

        //Temp- will feed into recycler view
//                StringBuilder trailerBuilder = new StringBuilder("*\tTrailers\t*\n\n");
//
//                for (String key : trailerMap.keySet()) {
//                    for (URL value : trailerMap.values()) {
//                        trailerBuilder.append("[" + key + "]\n" + value.toString() + "\n\n");
//                    }
//                }
//                viewHolder.trailer.setText(trailerBuilder.toString());

//                byte[] reviewBytes = cursor.getBlob(reviewsInd);
//                HashMap<String,String> reviewMap =
//                        (HashMap<String,String>)Serializer.deserialize(reviewBytes);

//                StringBuilder reviewBuilder = new StringBuilder("*\tReviews\t*\n\n");
//                for (String key : reviewMap.keySet()) {
//                    for (String value : reviewMap.values()) {
//                        reviewBuilder.append("[" + key + "]\n" + value.toString() + "\n\n");
//                    }
//                }
//
//                viewHolder.review.setText(reviewBuilder.toString());
//temp
//                mRecyclerAdapter = new DetailAdapter(reviewMap);
//                mRecyclerView.setAdapter(mRecyclerAdapter);
//            }
//        }
        mRecyclerAdapter.mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.mCursorAdapter.swapCursor(null);
    }

    public static final class ColumnIndices {
        public final int columnId = 0;
        public final int posterInd = 1;
        public final int titleInd = 2;
        public final int dateInd = 3;
        public final int ratingInd = 4;
        public final int summaryInd = 5;
        public final int trailersInd = 6;
        public final int reviewsInd = 7;
    }

    public static class DetailViewHolder {

        public final ImageView imageView;
        public final TextView title;
        public final TextView date;
        public final TextView rating;
        public final TextView summary;
//        public final TextView trailer;
//        public final TextView review;

        public DetailViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.poster_imageview);
            title = (TextView) view.findViewById(R.id.title_textview);
            date = (TextView) view.findViewById(R.id.date_textview);
            rating = (TextView) view.findViewById(R.id.rating_textview);
            summary = (TextView) view.findViewById(R.id.summary_textview);
//            trailer = (TextView) view.findViewById(R.id.trailer_textview);
//            review = (TextView) view.findViewById(R.id.review_textview);
        }
    }
}
