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
import com.example.popularmovies.data.MovieContract.PopularityEntry;
import com.example.popularmovies.data.MovieContract.RatingEntry;
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

    public static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
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
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
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
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void getMovieData(Uri contentUri) {
        final String API_SORT_POPULARITY = "popular";
        final String API_SORT_RATING = "top_rated";
        String sortMode = null;

        if (contentUri.equals(MovieContract.PopularityEntry.CONTENT_URI)) {
            sortMode = API_SORT_POPULARITY;
        } else if (contentUri.equals(MovieContract.RatingEntry.CONTENT_URI)) {
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
        if (moviesJsonStr != null)
            parseMovieJSON(moviesJsonStr, contentUri);
        else
            Log.w(LOG_TAG, "Unable to fetch JSON data");
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        getMovieData(PopularityEntry.CONTENT_URI);
        getMovieData(RatingEntry.CONTENT_URI);
    }

    private String getJSONStr(URL url) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;

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

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return jsonStr;
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
        final String POSTER_SIZE = "w342"; //   w185,w342,w500
        try {
            JSONObject movieDataJson = new JSONObject(movieDataJsonStr);
            JSONArray movieDataArray = movieDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numMovies = movieDataArray.length();

            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieDataArray.length());

            for (int i = 0; i < numMovies; i++) {
                JSONObject movieObject = movieDataArray.getJSONObject(i);

                String poster = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendEncodedPath(POSTER_SIZE)
                        .appendEncodedPath(movieObject.getString(MOVIE_DB_POSTER))
                        .build()
                        .toString();

                String summary = movieObject.getString(MOVIE_DB_OVERVIEW);
                String date = movieObject.getString(MOVIE_DB_DATE);
                String id = movieObject.getString(MOVIE_DB_ID);
                String title = movieObject.getString(MOVIE_DB_TITLE);
                String rating = movieObject.getString(MOVIE_DB_VOTE);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.Columns.POSTER, poster);
                movieValues.put(MovieContract.Columns.SUMMARY, summary);
                movieValues.put(MovieContract.Columns.DATE, date);
                movieValues.put(MovieContract.Columns.TITLE, title);
                movieValues.put(MovieContract.Columns.RATING, rating);

                final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3";
                final String CONTENT_TYPE = "movie";
                final String API_TRAILER = "videos";
                final String API_KEY_PARAM = "api_key";

                Uri trailerUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendEncodedPath(CONTENT_TYPE)
                        .appendEncodedPath(id)
                        .appendEncodedPath(API_TRAILER)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
                URL trailerUrl = new URL(trailerUri.toString());

                String trailerJsonStr = getJSONStr(trailerUrl);
                if (trailerJsonStr != null) {
                    byte[] trailerByteArray = parseTrailerJSON(trailerJsonStr);
                    movieValues.put(MovieContract.Columns.TRAILERS, trailerByteArray);
                }

                final String API_REVIEW = "reviews";

                Uri reviewUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendEncodedPath(CONTENT_TYPE)
                        .appendEncodedPath(id)
                        .appendEncodedPath(API_REVIEW)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
                URL reviewURL = new URL(reviewUri.toString());

                String reviewJsonStr = getJSONStr(reviewURL);
                if (reviewJsonStr != null) {
                    byte[] reviewByteArray = parseReviewJSON(reviewJsonStr);
                    movieValues.put(MovieContract.Columns.REVIEWS, reviewByteArray);
                }

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

        try {
            JSONObject trailerDataJson = new JSONObject(trailerJsonStr);
            JSONArray trailerDataArray = trailerDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numTrailers = trailerDataArray.length();

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
            return Serializer.serialize(trailerMap);
        } catch (JSONException | IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
    }

    private byte[] parseReviewJSON(String reviewJsonStr) {
        final String MOVIE_DB_RESULTS = "results";
        final String MOVIE_DB_AUTHOR = "author";
        final String MOVIE_DB_CONTENT = "content";

        try {
            JSONObject reviewDataJson = new JSONObject(reviewJsonStr);
            JSONArray reviewDataArray = reviewDataJson.getJSONArray(MOVIE_DB_RESULTS);

            int numReviews = reviewDataArray.length();

            HashMap<String, String> reviewMap = new HashMap<>();

            for (int i = 0; i < numReviews; i++) {
                JSONObject reviewObject = reviewDataArray.getJSONObject(i);

                String author = reviewObject.getString(MOVIE_DB_AUTHOR);
                String content = reviewObject.getString(MOVIE_DB_CONTENT);

                reviewMap.put(author, content);
            }
            return Serializer.serialize(reviewMap);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return null;
    }

}