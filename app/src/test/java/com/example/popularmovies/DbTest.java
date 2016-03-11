package com.example.popularmovies;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.popularmovies.data.MovieDbHelper;

public class DbTest extends AndroidTestCase {

    private static final String LOG_TAG = DbTest.class.getSimpleName();

    public void setUp() throws Exception {
    }

    public void testCreateDb() {
        SQLiteDatabase db = new MovieDbHelper(mContext).getWritableDatabase();
        assertTrue(db.isOpen());
        db.close();
    }
}
