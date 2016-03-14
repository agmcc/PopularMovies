package com.example.popularmovies.data;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    private static final String LOG_TAG = Serializer.class.getSimpleName();

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream b = null;
        ObjectOutputStream o = null;
        try {
            b = new ByteArrayOutputStream();
            o = new ObjectOutputStream(b);
            o.writeObject(obj);
            return b.toByteArray();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            try {
                if (o != null)
                    o.close();
                if (b != null)
                    b.close();
            } catch (final IOException e) {
                Log.e(LOG_TAG, "Error closing stream", e);
            }
        }
        return null;
    }

    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream b = null;
        ObjectInputStream o = null;
        try {
            b = new ByteArrayInputStream(bytes);
            o = new ObjectInputStream(b);
            return o.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        } finally {
            try {
                if (o != null)
                    o.close();
                if (b != null)
                    b.close();
            } catch (final IOException e) {
                Log.e(LOG_TAG, "Error closing stream", e);
            }
        }
        return null;
    }

}
