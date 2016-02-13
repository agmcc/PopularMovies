package com.example.popularmovies;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private final String POPULARITY = "popularity.desc";
    private final String RATING = "vote_average.desc";
    protected String sortMode = POPULARITY;

    public MoviesFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        getMovieData();
    }

    public void DoStuff(){
        Log.v( LOG_TAG,"Doing stuff on ui thread");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(getContext()));
        return rootView;
    }

    private void getMovieData() {
        //task should do 1 thing - return info from query
        // so feed different queries e.g. sort by popularity
        FetchMoviesTask task = new FetchMoviesTask();
        task.execute();
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, String[]> { //params, progress, result

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            /*
            query for most popular + highest rated (make popular default)
            Grid: Movie poster thumbnail
            Details: title, release date, poster, vote, plot
            //vote_average.desc
            currently returns 20 movies (1 page)

            //poster path is relative
            \/inVq3FRqcYIRl2la8iZikYYxFNR.jpg
            Append: Base url:  http://image.tmdb.org/t/p/
            Size "w92", "w154", "w185", "w342", "w500", "w780", or "original". try w185
            path

            "poster_path" (see above- may need 2 sizes (thumbnail and full res)
            "overview"
            "release_date"
            "title" n.b. also "original_title"
            "vote_average"

            start with just thumbnails


             */

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
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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

                Log.v(LOG_TAG, "Movies JSON String: " + moviesJsonStr);

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

        private String[] getMovieDataFromJSON(String movieDataJsonStr)
                throws JSONException {

            final String MOVIE_DB_RESULTS = "results";
            final String MOVIE_DB_TITLE  = "title";

            JSONObject movieDataJson = new JSONObject(movieDataJsonStr);
            JSONArray movieDataArray = movieDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numMovies = movieDataArray.length();

            String[] resultStrs = new String[numMovies];

            //start with just thumbnail
            for(int i = 0; i < numMovies; i++){
                JSONObject movieObject = movieDataArray.getJSONObject(i);
                //temp
                String title = movieObject.getString(MOVIE_DB_TITLE);

                resultStrs[i] = title;
            }

            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[] results) {
            super.onPostExecute(results);
            Log.v(LOG_TAG, "Printing movies titles\n");
            for(String result : results){
                Log.v(LOG_TAG, result);
            }
            //could have array of string array / array list strign array
            //then for each element add to correct list in movie data
            //will stat with just string[] and do moviethumbs
        }
    }

}
