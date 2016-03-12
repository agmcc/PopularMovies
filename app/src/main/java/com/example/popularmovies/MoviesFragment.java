package com.example.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MoviesFragment extends Fragment implements OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private final String POPULARITY = "popularity.desc";
    private final String RATING = "vote_average.desc";
    protected String sortMode = POPULARITY;
    protected GridView gridView;
    private MovieAdapter mAdapter;
    private static final int MOVIE_LOADER = 0;

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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String choice = parent.getItemAtPosition(pos).toString();
        if (choice.equals(getString(R.string.sort_popularity))) {
            sortMode = POPULARITY;
        } else if (choice.equals(getString(R.string.sort_rating))) {
            sortMode = RATING;
        }
        Toast.makeText(getContext(), "Sort by " + choice, Toast.LENGTH_SHORT).show();
        getMovieData();
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
                        .setData(MovieContract.PopularityEntry.buildPopularityUri(position + 1));
                startActivity(detailIntent);
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
    public void onStart() {
        super.onStart();
        getMovieData();
    }

    private void getMovieData() {
        FetchMoviesTask task = new FetchMoviesTask();
        task.execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = {
                MovieContract.PopularityEntry.TABLE_NAME + "." + MovieContract.PopularityEntry._ID,
                Columns.POSTER_THUMB
        };

        return new CursorLoader(getActivity(),
                MovieContract.PopularityEntry.CONTENT_URI,
                projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr;

            try {

                final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/discover";
                final String CONTENT_TYPE = "movie";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendEncodedPath(CONTENT_TYPE)
                        .appendQueryParameter(SORT_PARAM, sortMode)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                //Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                moviesJsonStr = buffer.toString();

                //Log.v(LOG_TAG, "Movies JSON String: " + moviesJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                getMovieDataFromJSON(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private void getMovieDataFromJSON(String movieDataJsonStr) throws JSONException {

            final String MOVIE_DB_RESULTS = "results";
            final String MOVIE_DB_POSTER = "poster_path";
            final String MOVIE_DB_OVERVIEW = "overview";
            final String MOVIE_DB_DATE = "release_date";
            final String MOVIE_DB_TITLE = "title";
            final String MOVIE_DB_VOTE = "vote_average";

            final String POSTER_BASE_URL = "https://image.tmdb.org/t/p";
            final String POSTER_THUMB_SIZE = "w185";
            final String POSTER_FULL_SIZE = "w780";

            JSONObject movieDataJson = new JSONObject(movieDataJsonStr);
            JSONArray movieDataArray = movieDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numMovies = movieDataArray.length();

            ContentResolver resolver = getContext().getContentResolver();
            resolver.delete(MovieContract.PopularityEntry.CONTENT_URI, null, null);
            resolver.delete(MovieContract.RatingEntry.CONTENT_URI, null, null);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieDataArray.length());

            for (int i = 0; i < numMovies; i++) {
                JSONObject movieObject = movieDataArray.getJSONObject(i);

                String poster_thumb = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendEncodedPath(POSTER_THUMB_SIZE)
                        .appendEncodedPath(movieObject.getString(MOVIE_DB_POSTER))
                        .build()
                        .toString();

                String poster_full = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendEncodedPath(POSTER_FULL_SIZE)
                        .appendEncodedPath(movieObject.getString(MOVIE_DB_POSTER))
                        .build()
                        .toString();

                String summary = movieObject.getString(MOVIE_DB_OVERVIEW);
                String date = movieObject.getString(MOVIE_DB_DATE);
                String title = movieObject.getString(MOVIE_DB_TITLE);
                String rating = movieObject.getString(MOVIE_DB_VOTE);
                //todo parse trailer and review- will do separatey

                //todo add to different db based on spinner (popular, rating)
                //todo add first trailer and review (test)

                ContentValues movieValues = new ContentValues();

                movieValues.put(Columns.POSTER_THUMB, poster_thumb);
                movieValues.put(Columns.POSTER_FULL, poster_full);
                movieValues.put(Columns.SUMMARY, summary);
                movieValues.put(Columns.DATE, date);
                movieValues.put(Columns.TITLE, title);
                movieValues.put(Columns.RATING, rating);
//                movieValues.put(Columns.TRAILER, trailer);
                //movieValues.put(Columns.REVIEW, review);

                cVVector.add(movieValues);
            }

            //n.b. this is where would decide which db (rating, popular) to put cvs into
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                resolver.bulkInsert(MovieContract.PopularityEntry.CONTENT_URI, cvArray);
            }
        }

//        @Override
//        protected void onPostExecute(Void v) {
//            gridView.setAdapter(new ImageAdapter(getContext()));
//        }
    }

}
