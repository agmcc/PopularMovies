package com.example.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.popularmovies.data.MovieContract.FavouritesEntry;
import com.example.popularmovies.data.MovieContract.PopularityEntry;
import com.example.popularmovies.data.MovieContract.RatingEntry;


public class MovieProvider extends ContentProvider {

    private static final String LOG_TAG = MovieProvider.class.getSimpleName();
    private static final int POPULARITY = 100;
    private static final int POPULARITY_WITH_ID = 101;
    private static final int RATING = 200;
    private static final int RATING_WITH_ID = 201;
    private static final int FAVOURITES = 300;
    private static final int FAVOURITES_WITH_ID = 301;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_POPULARITY, POPULARITY);
        matcher.addURI(authority, MovieContract.PATH_POPULARITY + "/#", POPULARITY_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_RATING, RATING);
        matcher.addURI(authority, MovieContract.PATH_RATING + "/#", RATING_WITH_ID);
        matcher.addURI(authority, MovieContract.PATH_FAVOURITES, FAVOURITES);
        matcher.addURI(authority, MovieContract.PATH_FAVOURITES + "/#", FAVOURITES_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POPULARITY:
                return PopularityEntry.CONTENT_TYPE;
            case POPULARITY_WITH_ID:
                return PopularityEntry.CONTENT_ITEM_TYPE;
            case RATING:
                return RatingEntry.CONTENT_TYPE;
            case RATING_WITH_ID:
                return RatingEntry.CONTENT_ITEM_TYPE;
            case FAVOURITES:
                return FavouritesEntry.CONTENT_TYPE;
            case FAVOURITES_WITH_ID:
                return FavouritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case POPULARITY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PopularityEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case POPULARITY_WITH_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PopularityEntry.TABLE_NAME,
                        projection,
                        PopularityEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case RATING: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RatingEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case RATING_WITH_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RatingEntry.TABLE_NAME,
                        projection,
                        RatingEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case FAVOURITES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavouritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case FAVOURITES_WITH_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavouritesEntry.TABLE_NAME,
                        projection,
                        FavouritesEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case POPULARITY: {
                long _id = db.insert(PopularityEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PopularityEntry.buildPopularityUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                break;
            }
            case RATING: {
                long _id = db.insert(RatingEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = RatingEntry.buildRatingUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                break;
            }
            case FAVOURITES: {
                long _id = db.insert(FavouritesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = FavouritesEntry.buildFavouritesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch (match) {
            case POPULARITY:
                numDeleted = db.delete(
                        PopularityEntry.TABLE_NAME, selection, selectionArgs);
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        PopularityEntry.TABLE_NAME + "'");
                break;
            case POPULARITY_WITH_ID:
                numDeleted = db.delete(PopularityEntry.TABLE_NAME,
                        PopularityEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        PopularityEntry.TABLE_NAME + "'");
                break;
            case RATING:
                numDeleted = db.delete(
                        RatingEntry.TABLE_NAME, selection, selectionArgs);
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        RatingEntry.TABLE_NAME + "'");
                break;
            case RATING_WITH_ID:
                numDeleted = db.delete(PopularityEntry.TABLE_NAME,
                        RatingEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        RatingEntry.TABLE_NAME + "'");
                break;
            case FAVOURITES:
                numDeleted = db.delete(
                        FavouritesEntry.TABLE_NAME, selection, selectionArgs);
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        FavouritesEntry.TABLE_NAME + "'");
                break;
            case FAVOURITES_WITH_ID:
                numDeleted = db.delete(PopularityEntry.TABLE_NAME,
                        FavouritesEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        FavouritesEntry.TABLE_NAME + "'");
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return numDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated;

        if (values == null) {
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch (sUriMatcher.match(uri)) {
            case POPULARITY: {
                numUpdated = db.update(PopularityEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case POPULARITY_WITH_ID: {
                numUpdated = db.update(PopularityEntry.TABLE_NAME,
                        values,
                        PopularityEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            case RATING: {
                numUpdated = db.update(RatingEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case RATING_WITH_ID: {
                numUpdated = db.update(RatingEntry.TABLE_NAME,
                        values,
                        RatingEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            case FAVOURITES: {
                numUpdated = db.update(FavouritesEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case FAVOURITES_WITH_ID: {
                numUpdated = db.update(FavouritesEntry.TABLE_NAME,
                        values,
                        FavouritesEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (numUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case POPULARITY:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PopularityEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case RATING:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RatingEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case FAVOURITES:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(FavouritesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
