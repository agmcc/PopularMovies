package com.example.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.popularmovies.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static String getContentDirType(String path) {
        return ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + path;
    }

    private static String getContentItemType(String path) {
        return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + path;
    }

    //could replace individual buildpopularituri etc methods witj this
    public static Uri buildUri(Uri contentUri, long id) {
        return ContentUris.withAppendedId(contentUri, id);
    }

    public static final class Columns {
        public static final String POSTER = "poster_thumb";
        public static final String SUMMARY = "summary";
        public static final String DATE = "date";
        public static final String TITLE = "title";
        public static final String RATING = "rating";
        public static final String TRAILERS = "trailers";
        public static final String REVIEWS = "reviews";
    }

    public static final class PopularityEntry implements BaseColumns {
        public static final String PATH_POPULARITY = "popularity";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_POPULARITY).build();

        public static final String CONTENT_DIR_TYPE = getContentDirType(PATH_POPULARITY);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_POPULARITY);

        public static final String TABLE_NAME = "popularity";

        public static Uri buildPopularityUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class RatingEntry implements BaseColumns {
        public static final String PATH_RATING = "rating";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RATING).build();

        public static final String CONTENT_DIR_TYPE = getContentDirType(PATH_RATING);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_RATING);

        public static final String TABLE_NAME = "rating";

        public static Uri buildRatingUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class FavouritesEntry implements BaseColumns {
        public static final String PATH_FAVOURITES = "favourites";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVOURITES).build();

        public static final String CONTENT_DIR_TYPE = getContentDirType(PATH_FAVOURITES);
        public static final String CONTENT_ITEM_TYPE = getContentItemType(PATH_FAVOURITES);

        public static final String TABLE_NAME = "favourites";

        public static Uri buildFavouritesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
