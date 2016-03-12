package com.example.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.popularmovies.sync.MovieSyncAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, new MoviesFragment())
                    .commit();
        }
        MovieSyncAdapter.initializeSyncAdapter(this);
    }

}
