package com.example.popularmovies;

import android.content.Intent;
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
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                //put info in here? or just refer to MovieData
                startActivity(detailIntent);
            }
        });
        return rootView;
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

    private class FetchMoviesTask extends AsyncTask<String, Void, String[][]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected String[][] doInBackground(String... params) {
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
                return getMovieDataFromJSON(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private String[][] getMovieDataFromJSON(String movieDataJsonStr)
                throws JSONException {

            final String MOVIE_DB_RESULTS = "results";
            final String MOVIE_DB_POSTER = "poster_path";
            final String MOVIE_DB_OVERVIEW = "overview";
            final String MOVIE_DB_DATE = "release_date";
            final String MOVIE_DB_TITLE = "title";
            final String MOVIE_DB_VOTE = "vote_average";

            final String POSTER_BASE_URL = "https://image.tmdb.org/t/p";
            final String POSTER_THUMB_SIZE = "w185";
            final String POSTER_FULL_SIZE = "w780"; //could be "original"

            JSONObject movieDataJson = new JSONObject(movieDataJsonStr);
            JSONArray movieDataArray = movieDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numMovies = movieDataArray.length();

            String[][] results = new String[6][numMovies];

            for (int i = 0; i < numMovies; i++) {
                JSONObject movieObject = movieDataArray.getJSONObject(i);

                Uri poster_thumb = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendEncodedPath(POSTER_THUMB_SIZE)
                        .appendEncodedPath(
                                movieObject.getString(MOVIE_DB_POSTER))
                        .build();

                Uri poster_full = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendEncodedPath(POSTER_FULL_SIZE)
                        .appendEncodedPath(
                                movieObject.getString(MOVIE_DB_POSTER))
                        .build();

                String overview = movieObject.getString(MOVIE_DB_OVERVIEW);
                String date = movieObject.getString(MOVIE_DB_DATE);
                String title = movieObject.getString(MOVIE_DB_TITLE);
                String vote = movieObject.getString(MOVIE_DB_VOTE);

                results[0][i] = poster_thumb.toString();
                results[1][i] = poster_full.toString();
                results[2][i] = overview;
                results[3][i] = date;
                results[4][i] = title;
                results[5][i] = vote;
            }

            return results;
        }

        @Override
        protected void onPostExecute(String[][] results) {
            super.onPostExecute(results);

            MovieData.poster_thumbnail = results[0];
            MovieData.poster_full_size = results[1];
            MovieData.overview = results[2];
            MovieData.date = results[3];
            MovieData.title = results[4];
            MovieData.vote = results[5];

            gridView.setAdapter(new ImageAdapter(getContext()));
        }
    }

}
