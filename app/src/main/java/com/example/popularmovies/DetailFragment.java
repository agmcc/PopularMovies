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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.popularmovies.data.MovieContract.Columns;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private RecyclerView mRecyclerView;
    private DetailAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //todo rename
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

//        ToggleButton toggleButton = (ToggleButton) rootView.findViewById(R.id.toggle);
//        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                //maybe have content provider method that copies entry to other table?
//                if (isChecked) {
//                    Log.v(LOG_TAG, "Favourited");
//                } else {
//                    Log.v(LOG_TAG, "Unfavourited");
//                }
//            }
//        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

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

        String columnId = intent.getStringExtra(Intent.EXTRA_TEXT);
        Uri contentUri = intent.getData();

        final String[] projection = {
                columnId,
                Columns.POSTER_THUMB,
                Columns.TITLE,
                Columns.DATE,
                Columns.RATING,
                Columns.SUMMARY,
                Columns.TRAILERS,
                Columns.REVIEWS
        };

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
//        final int poster = 0;
//        final int title = 1;
//        final int date = 2;
//        final int rating = 3;
//        final int summary = 4;
//        final int trailers = 5;
//        final int reviews = 6;

//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                Picasso.with(getContext())
//                        .load(cursor.getString(poster))
//                        .into(viewHolder.imageView);
//
//                viewHolder.title.setText(cursor.getString(title));
//                viewHolder.date.setText(cursor.getString(date));
//                viewHolder.rating.setText(cursor.getString(rating));
//                viewHolder.summary.setText(cursor.getString(summary));
//
//                byte[] trailerByteArray = cursor.getBlob(trailers);
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

//                byte[] reviewBytes = cursor.getBlob(reviews);
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
        mRecyclerView.setAdapter(new DetailAdapter(cursor, getActivity()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       mRecyclerView.setAdapter(new DetailAdapter(null, null));
    }

    public class Indices {
        public static final int columnId = 0;
        public static final int poster = 1;
        public static final int title = 2;
        public static final int date = 3;
        public static final int rating = 4;
        public static final int summary = 5;
        public static final int trailers = 6;
        public static final int reviews = 7;
    }

}
