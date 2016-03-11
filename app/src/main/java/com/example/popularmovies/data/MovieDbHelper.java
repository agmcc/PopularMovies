package com.example.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.popularmovies.data.MovieContract.Columns;
import com.example.popularmovies.data.MovieContract.PopularityEntry;
import com.example.popularmovies.data.MovieContract.RatingEntry;
import com.example.popularmovies.data.MovieContract.FavouritesEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //N.B. Will simplify and start with 1 traiiler and 1 review each movie, then later allow foreign keys
        final String SQL_COLUMNS_STUB =
                Columns.POSTER_THUMB + " TEXT NOT NULL, " +
                Columns.POSTER_FULL + " TEXT NOT NULL, " +
                Columns.SUMMARY + " TEXT NOT NULL, " +
                Columns.DATE + " INTEGER NOT NULL, " +
                Columns.TITLE + " TEXT NOT NULL, " +
                Columns.RATING + " REAL NOT NULL, " +
//                " FOREIGN KEY (" + Columns.TRAILERS_KEY + ") REFERENCES " +
//                TrailersEntry.TABLE_NAME + " (" + TrailersEntry._ID + "), " +
//                " FOREIGN KEY (" + Columns.REVIEWS_KEY + ") REFERENCES " +
//                ReviewsEntry.TABLE_NAME + " (" + ReviewsEntry._ID + "), " +
                Columns.TRAILER + " TEXT DEFAULT None, " +
                Columns.REVIEW + " TEXT DEFAULT None " +
                ");";

        final String SQL_CREATE_POPULARITY_TABLE = "CREATE TABLE " + PopularityEntry.TABLE_NAME + " (" +
                PopularityEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SQL_COLUMNS_STUB;

        final String SQL_CREATE_RATING_TABLE = "CREATE TABLE " + RatingEntry.TABLE_NAME + " (" +
                RatingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SQL_COLUMNS_STUB;

        final String SQL_CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FavouritesEntry.TABLE_NAME + " (" +
                FavouritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SQL_COLUMNS_STUB;

        //not sure if need separate trailer/review table for each
//        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + TrailersEntry.TABLE_NAME + " (" +
//                TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                Columns.TRAILER + " TEXT NOT NULL " +
//                " );";
//
//        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsEntry.TABLE_NAME + " (" +
//                ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                Columns.REVIEW + " TEXT NOT NULL " +
//                " );";

//        db.execSQL(SQL_CREATE_TRAILERS_TABLE);
//        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
        db.execSQL(SQL_CREATE_POPULARITY_TABLE);
        db.execSQL(SQL_CREATE_RATING_TABLE);
        db.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PopularityEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RatingEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavouritesEntry.TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + TrailersEntry.TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + ReviewsEntry.TABLE_NAME);
    }
}
