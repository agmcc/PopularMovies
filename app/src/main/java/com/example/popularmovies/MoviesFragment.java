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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.MovieContract.Columns;
import com.example.popularmovies.data.MovieContract.FavouritesEntry;
import com.example.popularmovies.data.MovieContract.PopularityEntry;
import com.example.popularmovies.data.MovieContract.RatingEntry;
import com.example.popularmovies.sync.MovieSyncAdapter;

public class MoviesFragment extends Fragment implements OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private static final int MOVIE_LOADER = 0;
    protected GridView gridView;
    private Uri sortURI = PopularityEntry.CONTENT_URI;
    private String sortTable = PopularityEntry.TABLE_NAME;
    private String sortId = PopularityEntry._ID;
    private MovieAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movies_fragment_menu, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.spinner_choices,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0, false);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Log.i(LOG_TAG, "Refresh menu item selected");
                mSwipeRefreshLayout.setRefreshing(true);
                MovieSyncAdapter.syncImmediately(getActivity());
                return true;
            case R.id.menu_settings:
                Log.i(LOG_TAG, "Settings menu click");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String choice = parent.getItemAtPosition(pos).toString();

        if (choice.equals(getString(R.string.sort_popularity))) {
            sortURI = PopularityEntry.CONTENT_URI;
            sortTable = PopularityEntry.TABLE_NAME;
            sortId = PopularityEntry._ID;
        } else if (choice.equals(getString(R.string.sort_rating))) {
            sortURI = RatingEntry.CONTENT_URI;
            sortTable = RatingEntry.TABLE_NAME;
            sortId = RatingEntry._ID;
        } else if (choice.equals(getString(R.string.sort_favourites))) {
            sortURI = FavouritesEntry.CONTENT_URI;
            sortTable = FavouritesEntry.TABLE_NAME;
            sortId = FavouritesEntry._ID;
        }

        Toast.makeText(getContext(), "Sort by " + choice, Toast.LENGTH_SHORT).show();
        getLoaderManager().restartLoader(0, null, this);
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        gridView = (GridView) rootView.findViewById(R.id.gridview);
        mAdapter = new MovieAdapter(getActivity(), null, 0);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .setData(MovieContract.buildUri(sortURI, position + 1));
                startActivity(detailIntent);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                MovieSyncAdapter.syncImmediately(getActivity());
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = {
                sortTable + "." + sortId,
                Columns.POSTER_THUMB
        };

        return new CursorLoader(getActivity(),
                sortURI,
                projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        Log.i(LOG_TAG, "Load complete");
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
