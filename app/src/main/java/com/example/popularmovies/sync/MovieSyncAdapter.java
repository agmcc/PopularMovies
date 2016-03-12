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

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    //// TODO: 12/03/2016 settings menu with sync frequency
    public static final int SYNC_INTERVAL = 60 * 180; //seconds
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

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

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
//        Log.d(LOG_TAG, "onPerformSync");

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
                            //temp- will need diff functions for popualrity, rating etc.
                    .appendQueryParameter(SORT_PARAM, "popularity.desc")
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());
            //Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null)
                return;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0)
                return;

            moviesJsonStr = buffer.toString();
            getMovieDataFromJSON(moviesJsonStr);
            //Log.v(LOG_TAG, "Movies JSON String: " + moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
                String title = movieObject.getString(MOVIE_DB_TITLE);
                String rating = movieObject.getString(MOVIE_DB_VOTE);
                //todo parse trailer and review- will do separatey

                //todo add to different db based on spinner (popular, rating)
                //todo add first trailer and review (test)

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.Columns.POSTER_THUMB, poster_thumb);
                movieValues.put(MovieContract.Columns.POSTER_FULL, poster_full);
                movieValues.put(MovieContract.Columns.SUMMARY, summary);
                movieValues.put(MovieContract.Columns.DATE, date);
                movieValues.put(MovieContract.Columns.TITLE, title);
                movieValues.put(MovieContract.Columns.RATING, rating);
//                movieValues.put(Columns.TRAILER, trailer);
                //movieValues.put(Columns.REVIEW, review);

                cVVector.add(movieValues);
            }

            //n.b. this is where would decide which db (rating, popular) to put cvs into
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                ContentResolver resolver = getContext().getContentResolver();
                resolver.delete(MovieContract.PopularityEntry.CONTENT_URI, null, null);
                resolver.delete(MovieContract.RatingEntry.CONTENT_URI, null, null);
                resolver.bulkInsert(MovieContract.PopularityEntry.CONTENT_URI, cvArray);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}