package com.example.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.popularmovies.data.MovieContract.Columns;
import com.example.popularmovies.data.Serializer;

import java.net.URL;
import java.util.HashMap;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private RecyclerView mRecyclerView;
    private DetailAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //temp - will read favourite status from db
    private boolean favourited = false;
    private ShareActionProvider mShareActionProvider;
    private Cursor mCursor;
    private static final String SHARE_MSG = " -Popular Movies\n";

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

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.detail_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

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
        MenuItem menuItem = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mCursor != null)
            mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favourite:
                Log.i(LOG_TAG, "Clicked favourite");
                favourited = !favourited;
                if (favourited)
                    item.setIcon(R.drawable.ic_favourite_true);
                else
                    item.setIcon(R.drawable.ic_favourite_false);
                break;
            case R.id.menu_share:
                Log.i(LOG_TAG, "Clicked share");
                //share movie
                break;
        }
        return super.onOptionsItemSelected(item);
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
        if (mShareActionProvider != null)
            mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(new DetailAdapter(null, null));
    }

    private Intent createShareForecastIntent() {
        if (mCursor.moveToFirst()) {
            HashMap<String, URL> trailerMap = (HashMap<String, URL>) Serializer.deserialize(
                    mCursor.getBlob(Indices.trailers));
            if (trailerMap.values().size() > 0) {
                String trailerName = (String)trailerMap.keySet().toArray()[0];
                URL trailerUrl = (URL) trailerMap.values().toArray()[0];
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, trailerName + SHARE_MSG + trailerUrl.toString());
                return shareIntent;
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
