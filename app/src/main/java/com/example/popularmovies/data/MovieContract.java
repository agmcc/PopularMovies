package com.example.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
//todo use string resource for content authority
    public static String CONTENT_AUTHORITY = "com.example.popularmovies.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_POPULARITY = "popularity";
    public static final String PATH_RATING = "rating";
    public static final String PATH_FAVOURITES = "favourites";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";

    public static final class Columns{
        public static final String POSTER_THUMB = "poster_thumb";
        public static final String POSTER_FULL = "poster_full";
        public static final String SUMMARY = "summary";
        public static final String DATE = "date";
        public static final String TITLE = "title";
        public static final String RATING = "rating";
        public static final String TRAILER = "trailer";
        public static final String REVIEW = "review";
        //Trailers and Reviews cols will link to separate trailers and reviews tables by foreign key
        public static final String TRAILERS_KEY = "trailers_id";
        public static final String REVIEWS_KEY = "reviews_id";
    }

    public static String getContentDirType(String path){
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + path;
    }

    public static String getContentItemType(String path){
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + path;
    }

    public static final class PopularityEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_POPULARITY).build();

        public static final String CONTENT_DIR_TYPE = getContentDirType(PATH_POPULARITY);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_POPULARITY);

        public static final String TABLE_NAME = "popularity";

        public static Uri buildPopularityUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class RatingEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RATING).build();

        public static final String CONTENT_DIR_TYPE = getContentDirType(PATH_RATING);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_RATING);

        public static final String TABLE_NAME = "rating";

        public static Uri buildRatingUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class FavouritesEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVOURITES).build();

        public static final String CONTENT_DIR_TYPE = getContentDirType(PATH_FAVOURITES);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_FAVOURITES);

        public static final String TABLE_NAME = "favourites";

        public static Uri buildFavouritesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    //probs don't need content provider stuff for these below, just table name + implement basecol

    public static final class TrailersEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRAILERS).build();

        public static final String CONTENT_TYPE = getContentDirType(PATH_TRAILERS);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_TRAILERS);

        public static final String TABLE_NAME = "trailers";

        public static Uri buildTrailersUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ReviewsEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEWS).build();

        public static final String CONTENT_TYPE = getContentDirType(PATH_REVIEWS);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_REVIEWS);

        public static final String TABLE_NAME = "reviews";

        public static Uri buildReviewsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
