package com.example.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.popularmovies.BuildConfig;
import com.example.popularmovies.R;
import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.Serializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    /*
    Goal - sync with Api and write data to content provider
    Api calls -
    1) Discover, sorted by popualarity new api: /movie/popular
    2) Discover, sorted by rating : /movie/top_rated
    3) Trailers call - /movie/id/videos
    3) Reviews call /movie/id/reviews

    N.B. id can be fetched from popular or top rated calls e.g. 293660 (deadpool)
    example trailer json: {"id":293660,"results":[{"id":"56c4cb4bc3a3680d57000560","iso_639_1":"en","iso_3166_1":"US","key":"7jIBCiYg58k","name":"Trailer","site":"YouTube","size":1080,"type":"Trailer"}]}
    contains results array with id, key, name, site and others
    Deadpool key = 7jIBCiYg58k
    get youtube url: https://www.youtube.com/watch?v=   +   key
    e.g. https://www.youtube.com/watch?v=7jIBCiYg58k

    http://http//api.themoviedb.org/3/movie/now_playing?api_key=2e19e8fc023dd86dee79eb6b406fcd43

    step 1(String sortMode) - will call /movie/sortmode and return json and parse (inc. id)
    step 2 for each movie id call movie/id/trailer
    step 3 for each movie id call movie/id/reviews
    step 4 bulkinsert popularity, trailers and revies into popualrity table and popualr, trailers and reviews into popular table

        e.g getMovieData(popularity); > calls GetTrailers(list id), GetReviews(list id), insertvalues(popularity)
        getMovieData(rating);calls GetTrailers(list id), GetReviews(list id), insertvalues(rating)

        will start by using just popualrity and rating tables and only adding 1 trailer and 1 review each
        later worry about foreign keys / shared trailer tables etc.
     */

    //// TODO: 12/03/2016 settings menu with sync frequency
    public static final int SYNC_INTERVAL = 60 * 180; //seconds
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    private static final String POPULAR = "popular";
    private static final String RATING = "top_rated";

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
//        Log.v(LOG_TAG, "configurePeriodicSync");
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
//        Log.v(LOG_TAG, "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {

//        Log.v(LOG_TAG, "getSyncAccount");
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null))
                return null;
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
//        Log.v(LOG_TAG, "onAccountCreated");
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
//        Log.v(LOG_TAG, "initializeSyncAdapter");
        getSyncAccount(context);
    }

    private void getMovieData(Uri contentUri) {
        final String API_SORT_POPULARITY = "popular";
        final String API_SORT_RATING = "top_rated";
        String sortMode = null;

        if (contentUri.equals(MovieContract.PopularityEntry.CONTENT_URI)) {
            Log.v(LOG_TAG, "Sorting by popularity");
            sortMode = API_SORT_POPULARITY;
        } else if (contentUri.equals(MovieContract.RatingEntry.CONTENT_URI)) {
            Log.v(LOG_TAG, "Sorting by rating");
            sortMode = API_SORT_RATING;
        } else
            throw new UnsupportedOperationException("Unknown sortMode: " + sortMode);


        final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3";
        final String CONTENT_TYPE = "movie";
        final String API_KEY_PARAM = "api_key";
        URL movieUrl = null;

        Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                .appendEncodedPath(CONTENT_TYPE)
                .appendEncodedPath(sortMode)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        try {
            movieUrl = new URL(builtUri.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }


        String moviesJsonStr = getJSONStr(movieUrl);

        if (moviesJsonStr != null) {
            parseMovieJSON(moviesJsonStr, contentUri);
        } else {
            Log.e(LOG_TAG, "Unable to fetch JSON data");
            return;
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        getMovieData(MovieContract.PopularityEntry.CONTENT_URI);
        getMovieData(MovieContract.RatingEntry.CONTENT_URI);
//      Log.d(LOG_TAG, "onPerformSync");
//
//        HttpURLConnection urlConnection = null;
//        BufferedReader reader = null;
//        String moviesJsonStr;
//
//        try {
//            final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/discover";
//            final String CONTENT_TYPE = "movie";
//            final String SORT_PARAM = "sort_by";
//            final String API_KEY_PARAM = "api_key";
//
//            Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
//                    .appendEncodedPath(CONTENT_TYPE)
//                            //temp- will need diff functions for popualrity, rating etc.
//                    .appendQueryParameter(SORT_PARAM, "popularity.desc")
//                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
//                    .build();
//
//            URL url = new URL(builtUri.toString());
//            Log.v(LOG_TAG, "Built URI " + builtUri.toString());
//            //http://api.themoviedb.org/3/movie/now_playing?api_key=2e19e8fc023dd86dee79eb6b406fcd43
//
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.connect();
//
//            InputStream inputStream = urlConnection.getInputStream();
//            StringBuffer buffer = new StringBuffer();
//
//            if (inputStream == null)
//                return;
//
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line + "\n");
//            }
//
//            if (buffer.length() == 0)
//                return;
//
//            moviesJsonStr = buffer.toString();
//            getMovieDataFromJSON(moviesJsonStr);
//            Log.v(LOG_TAG, "Movies JSON String: " + moviesJsonStr);
//
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error ", e);
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (final IOException e) {
//                    Log.e(LOG_TAG, "Error closing stream", e);
//                }
//            }
//        }
    }

    private void parseMovieJSON(String movieDataJsonStr, Uri contentUri) {
        final String MOVIE_DB_RESULTS = "results";
        final String MOVIE_DB_POSTER = "poster_path";
        final String MOVIE_DB_OVERVIEW = "overview";
        final String MOVIE_DB_DATE = "release_date";
        final String MOVIE_DB_ID = "id";
        final String MOVIE_DB_TITLE = "title";
        final String MOVIE_DB_VOTE = "vote_average";

        final String POSTER_BASE_URL = "https://image.tmdb.org/t/p";
        final String POSTER_THUMB_SIZE = "w185";
        final String POSTER_FULL_SIZE = "w780";

        try {
            JSONObject movieDataJson = new JSONObject(movieDataJsonStr);
            JSONArray movieDataArray = movieDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numMovies = movieDataArray.length();

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
                String id = movieObject.getString(MOVIE_DB_ID);
                String title = movieObject.getString(MOVIE_DB_TITLE);
                String rating = movieObject.getString(MOVIE_DB_VOTE);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.Columns.POSTER_THUMB, poster_thumb);
                movieValues.put(MovieContract.Columns.POSTER_FULL, poster_full);
                movieValues.put(MovieContract.Columns.SUMMARY, summary);
                movieValues.put(MovieContract.Columns.DATE, date);
                movieValues.put(MovieContract.Columns.TITLE, title);
                movieValues.put(MovieContract.Columns.RATING, rating);

                final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3";
                final String CONTENT_TYPE = "movie";
                final String API_TRAILER = "videos";
                final String API_KEY_PARAM = "api_key";

                URL trailerUrl = null;

                Uri trailerUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendEncodedPath(CONTENT_TYPE)
                        .appendEncodedPath(id)
                        .appendEncodedPath(API_TRAILER)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                trailerUrl = new URL(trailerUri.toString());

                //tempoarily only caching trailer json and then parse in loader on demand
                String trailerJsonStr = getJSONStr(trailerUrl);
                byte[] trailerByteArray = parseTrailerJSON(trailerJsonStr);
//                String trailers = parseTrailerJSON(trailerJsonStr);
                movieValues.put(MovieContract.Columns.TRAILERS, trailerByteArray);

                final String API_REVIEW = "reviews";
                URL reviewURL = null;

                Uri reviewUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendEncodedPath(CONTENT_TYPE)
                        .appendEncodedPath(id)
                        .appendEncodedPath(API_REVIEW)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                reviewURL = new URL(reviewUri.toString());

                //temp
                String reviewJsonStr = getJSONStr(reviewURL);
                movieValues.put(MovieContract.Columns.REVIEWS, reviewJsonStr);

                cVVector.add(movieValues);
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.delete(contentUri, null, null);
                resolver.bulkInsert(contentUri, cvArray);
            }
        } catch (JSONException | IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private byte[] parseTrailerJSON(String trailerJsonStr) {
        final String MOVIE_DB_RESULTS = "results";
        final String MOVIE_DB_KEY = "key";
        final String MOVIE_DB_NAME = "name";

        if (trailerJsonStr == null) {
            Log.e(LOG_TAG, "No trailer json str");
            return null;
        }

//        Log.v(LOG_TAG, "JSOM: " + trailerJsonStr);
//        ByteArrayOutputStream byteStream = null;
//        ObjectOutputStream objectStream = null;
        try {
            JSONObject trailerDataJson = new JSONObject(trailerJsonStr);
            JSONArray trailerDataArray = trailerDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numTrailers = trailerDataArray.length();
            //listedhashmap? or other type
            HashMap<String, URL> trailerMap = new HashMap<>();

            for (int i = 0; i < numTrailers; i++) {
                JSONObject trailerObject = trailerDataArray.getJSONObject(i);
                String trailerName = trailerObject.getString(MOVIE_DB_NAME);
                String trailerKey = trailerObject.getString(MOVIE_DB_KEY);

                final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
                final String YOUTUBE_VIDEO_PARAM = "v";

                URL trailerURL;
                Uri uri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                        .appendQueryParameter(YOUTUBE_VIDEO_PARAM, trailerKey)
                        .build();

                trailerURL = new URL(uri.toString());

                trailerMap.put(trailerName, trailerURL);
            }

//            //if this works, will make serialize class
//            byteStream = new ByteArrayOutputStream();
//            objectStream = new ObjectOutputStream(byteStream);
//            objectStream.writeObject(trailerMap);
//            return byteStream.toByteArray();
            return Serializer.serialize(trailerMap);
        } catch (JSONException | IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
    }

    private String getJSONStr(URL url) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null)
                return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return null;

            jsonStr = buffer.toString();
//            Log.v(LOG_TAG, "JSON String: " + jsonStr);
            return jsonStr;

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
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
        return null;
    }

//    private void getMovieDataFromJSON(String movieDataJsonStr) throws JSONException {
//
//        final String MOVIE_DB_RESULTS = "results";
//        final String MOVIE_DB_POSTER = "poster_path";
//        final String MOVIE_DB_OVERVIEW = "overview";
//        final String MOVIE_DB_DATE = "release_date";
//        final String MOVIE_DB_TITLE = "title";
//        final String MOVIE_DB_VOTE = "vote_average";
//
//        final String POSTER_BASE_URL = "https://image.tmdb.org/t/p";
//        final String POSTER_THUMB_SIZE = "w185";
//        final String POSTER_FULL_SIZE = "w780";
//
//        try {
//            JSONObject movieDataJson = new JSONObject(movieDataJsonStr);
//            JSONArray movieDataArray = movieDataJson.getJSONArray(MOVIE_DB_RESULTS);
//
//            int numMovies = movieDataArray.length();
//
//            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieDataArray.length());
//
//            for (int i = 0; i < numMovies; i++) {
//                JSONObject movieObject = movieDataArray.getJSONObject(i);
//
//                String poster_thumb = Uri.parse(POSTER_BASE_URL).buildUpon()
//                        .appendEncodedPath(POSTER_THUMB_SIZE)
//                        .appendEncodedPath(movieObject.getString(MOVIE_DB_POSTER))
//                        .build()
//                        .toString();
//
//                String poster_full = Uri.parse(POSTER_BASE_URL).buildUpon()
//                        .appendEncodedPath(POSTER_FULL_SIZE)
//                        .appendEncodedPath(movieObject.getString(MOVIE_DB_POSTER))
//                        .build()
//                        .toString();
//
//                String summary = movieObject.getString(MOVIE_DB_OVERVIEW);
//                String date = movieObject.getString(MOVIE_DB_DATE);
//                String title = movieObject.getString(MOVIE_DB_TITLE);
//                String rating = movieObject.getString(MOVIE_DB_VOTE);
//                //todo parse trailer and review- will do separatey
//
//                //todo add to different db based on spinner (popular, rating)
//                //todo add first trailer and review (test)
//
//                ContentValues movieValues = new ContentValues();
//
//                movieValues.put(MovieContract.Columns.POSTER_THUMB, poster_thumb);
//                movieValues.put(MovieContract.Columns.POSTER_FULL, poster_full);
//                movieValues.put(MovieContract.Columns.SUMMARY, summary);
//                movieValues.put(MovieContract.Columns.DATE, date);
//                movieValues.put(MovieContract.Columns.TITLE, title);
//                movieValues.put(MovieContract.Columns.RATING, rating);
////                movieValues.put(Columns.TRAILERS, trailer);
//                //movieValues.put(Columns.REVIEWS, review);
//
//                cVVector.add(movieValues);
//            }
//
//            //n.b. this is where would decide which db (rating, popular) to put cvs into
//            if (cVVector.size() > 0) {
//                ContentValues[] cvArray = new ContentValues[cVVector.size()];
//                cVVector.toArray(cvArray);
//                ContentResolver resolver = getContext().getContentResolver();
//                resolver.delete(MovieContract.PopularityEntry.CONTENT_URI, null, null);
//                resolver.delete(MovieContract.RatingEntry.CONTENT_URI, null, null);
//                resolver.bulkInsert(MovieContract.PopularityEntry.CONTENT_URI, cvArray);
//            }
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        }
//    }
}