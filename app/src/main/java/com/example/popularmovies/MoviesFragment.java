package com.example.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.MovieContract.Columns;
import com.example.popularmovies.data.MovieDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MoviesFragment extends Fragment implements OnItemSelectedListener {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private final String POPULARITY = "popularity.desc";
    private final String RATING = "vote_average.desc";
    protected String sortMode = POPULARITY;
    protected GridView gridView;

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
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, position);
                startActivity(detailIntent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        Log.v(LOG_TAG, "onStart()");
        super.onStart();
        getMovieData();
    }

    private void getMovieData() {
        Log.v(LOG_TAG, "getMovieData");
        FetchMoviesTask task = new FetchMoviesTask();
        task.execute();
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

            //Log.v(LOG_TAG, "JSON: " + movieDataJsonStr);
            JSONObject movieDataJson = new JSONObject(movieDataJsonStr);
            JSONArray movieDataArray = movieDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numMovies = movieDataArray.length();

            //String[][] results = new String[6][numMovies];
//            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieDataArray.length());
            SQLiteDatabase db = new MovieDbHelper(getContext()).getWritableDatabase();

            db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{MovieContract.PopularityEntry.TABLE_NAME});
            db.delete("SQLITE_SEQUENCE", "NAME = ?", new String[]{MovieContract.RatingEntry.TABLE_NAME});
            db.delete(MovieContract.PopularityEntry.TABLE_NAME, null, null);
            db.delete(MovieContract.RatingEntry.TABLE_NAME, null, null);

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

                String overview = movieObject.getString(MOVIE_DB_OVERVIEW);
                String date = movieObject.getString(MOVIE_DB_DATE);
                String title = movieObject.getString(MOVIE_DB_TITLE);
                String vote = movieObject.getString(MOVIE_DB_VOTE);
                //todo parse trailer and review- will do separatey

                ContentValues movieValues = new ContentValues();
                //todo add to different db based on spinner (popular, rating)
                //todo add first trailer and review (test)
                //todo fix inconsistent names e.g. vote, summary etc.

//                public static final String POSTER_THUMB = "poster_thumb";
//                public static final String POSTER_FULL = "poster_full";
//                public static final String SUMMARY = "summary";
//                public static final String DATE = "date";
//                public static final String TITLE = "title";
//                public static final String RATING = "rating";
//                public static final String TRAILER = "trailer";
//                public static final String REVIEW = "review";

                movieValues.put(Columns.POSTER_THUMB, poster_thumb);
                movieValues.put(Columns.POSTER_FULL, poster_full);
                movieValues.put(Columns.SUMMARY, overview);
                movieValues.put(Columns.DATE, date);
                movieValues.put(Columns.TITLE, title);
                movieValues.put(Columns.RATING, vote);
//                movieValues.put(Columns.TRAILER, trailer);
                //movieValues.put(Columns.REVIEW, review);

                //temp only use underlying sqlite NOT Content provider
                db.insert(
                        MovieContract.PopularityEntry.TABLE_NAME,
                        null,
                        movieValues
                );
                //cVVector.add(movieValues);


//                results[0][i] = poster_thumb.toString();
//                results[1][i] = poster_full.toString();
//                results[2][i] = overview;
//                results[3][i] = date;
//                results[4][i] = title;
//                results[5][i] = vote;
            }
            db.close();

            //write to db
            //n.b. this is where would decide which db (rating, popular) to put cvs into
//            if (cVVector.size() > 0) {
//                ContentValues[] cvArray = new ContentValues[cVVector.size()];
//                cVVector.toArray(cvArray);
//                getContext().getContentResolver().bulkInsert(PopularityEntry.CONTENT_URI, cvArray);
//            }
        }

        @Override
        protected void onPostExecute(Void v) {
//            super.onPostExecute(results);
//
//            MovieData.poster_thumbnail = results[0];
//            MovieData.poster_full_size = results[1];
//            MovieData.overview = results[2];
//            MovieData.date = results[3];
//            MovieData.title = results[4];
//            MovieData.vote = results[5];

//            SQLiteDatabase db = new MovieDbHelper(getContext()).getWritableDatabase();
//            String[] projection = {
//                    Columns.TITLE,
//                    Columns.RATING
//            };
//            int titleInd = 0;
//            int ratingInd = 1;
//            String orderBy = Columns.RATING + " desc";
//            String limit = "10";
//            String selection = Columns.TITLE + " glob 'The*'";
//            Cursor cursor = db.query(
//                    MovieContract.PopularityEntry.TABLE_NAME,
//                    projection,
//                    selection,
//                    null,
//                    null,
//                    null,
//                    orderBy,
//                    limit);
//
//
//            if(cursor != null) {
//                Log.v(LOG_TAG, cursor.getCount() + " rows");
//                if (cursor.moveToFirst()) {
//                    do {
//                        String title = cursor.getString(titleInd);
//                        String rating = cursor.getString(ratingInd);
//                        Log.v(LOG_TAG, title +"\t" + rating);
//                    } while (cursor.moveToNext());
//                }
//                cursor.close();
//            }
//            db.close();

            gridView.setAdapter(new ImageAdapter(getContext()));
        }
    }

}
