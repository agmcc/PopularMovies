package com.example.popularmovies;

import android.content.ContentValues;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.MovieContract.Columns;
import com.example.popularmovies.data.Serializer;

import java.net.URL;
import java.util.HashMap;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final int DETAIL_LOADER = 0;
    private Cursor mCursor;
    private RecyclerView mRecyclerView;
    private boolean favourited = false;
    private MenuItem favouriteItem;
    private boolean viewingFavourites;
    private Intent mShareIntent;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.detail_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);

        favouriteItem = menu.findItem(R.id.menu_favourite);
        setFavouriteIcon();
    }

    private void setFavouriteIcon() {
        if (favouriteItem != null) {
            if (favourited)
                favouriteItem.setIcon(R.drawable.ic_favourite_true);
            else
                favouriteItem.setIcon(R.drawable.ic_favourite_false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favourite:
                favourited = !favourited;
                setFavouriteIcon();
                if (viewingFavourites && !favourited)
                    getActivity().finish();
                break;
            case R.id.menu_share:
                if (mShareIntent != null)
                    startActivity(mShareIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addToFavourites() {
//        Log.v(LOG_TAG, "Inserting favourite");
        ContentValues values = new ContentValues();

        values.put(Columns.POSTER, mCursor.getString(Indices.poster));
        values.put(Columns.SUMMARY, mCursor.getString(Indices.summary));
        values.put(Columns.DATE, mCursor.getInt(Indices.date));
        values.put(Columns.TITLE, mCursor.getString(Indices.title));
        values.put(Columns.RATING, mCursor.getString(Indices.rating));
        values.put(Columns.TRAILERS, mCursor.getBlob(Indices.trailers));
        values.put(Columns.REVIEWS, mCursor.getBlob(Indices.reviews));

        getContext().getContentResolver().insert(
                MovieContract.FavouritesEntry.CONTENT_URI,
                values);
        favourited = true;
    }

    private void removeFromFavourites() {
//        Log.v(LOG_TAG, "Removing favourite");
        getContext().getContentResolver().delete(MovieContract.FavouritesEntry.CONTENT_URI,
                Columns.TITLE + " = ?",
                new String[]{mCursor.getString(Indices.title)});
        favourited = false;
    }

    private boolean checkFavourited() {
        if (mCursor != null) {
            Cursor titleCursor = null;
            try {
                String title = mCursor.getString(Indices.title);
                titleCursor = getContext().getContentResolver().query(
                        MovieContract.FavouritesEntry.CONTENT_URI,
                        new String[]{Columns.TITLE},
                        Columns.TITLE + " = ?",
                        new String[]{title},
                        null);
                if (titleCursor != null)
                    return titleCursor.getCount() > 0;
            } finally {
                if (titleCursor != null) {
                    if (!titleCursor.isClosed())
                        titleCursor.close();
                }
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        if (favourited && !checkFavourited())
            addToFavourites();
        else
            removeFromFavourites();
        super.onPause();
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
                Columns.POSTER,
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
        mRecyclerView.setAdapter(new DetailAdapter(cursor, getActivity()));
        mCursor = cursor;

        favourited = checkFavourited();
        setFavouriteIcon();
        viewingFavourites = getActivity().getIntent().getBooleanExtra("Favourite", false);

        mShareIntent = createShareTrailerIntent();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(new DetailAdapter(null, null));
        mCursor = null;
    }

    private Intent createShareTrailerIntent() {
        if (mCursor.moveToFirst()) {
            HashMap<String, URL> trailerMap = (HashMap<String, URL>) Serializer.deserialize(
                    mCursor.getBlob(Indices.trailers));
            if (trailerMap.values().size() > 0) {
                String trailerName = (String) trailerMap.keySet().toArray()[0];
                URL trailerUrl = (URL) trailerMap.values().toArray()[0];
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, trailerUrl.toString());
                return Intent.createChooser(intent, getString(R.string.share));
            }
        }
        return null;
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
