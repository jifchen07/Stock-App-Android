package com.example.stockapp;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    private Stock stock;
    private MyApplication appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();

        stock = (Stock) intent.getSerializableExtra(MainActivity.EXTRA_TICKER);

        appData = (MyApplication) getApplicationContext();

        Log.d(TAG, "onCreate: " + stock.getTicker());
        Log.d(TAG, "onCreate: " + appData.getPortfolioList());
        Log.d(TAG, "onCreate: " + appData.getWatchList());
    }

    private void fetchData() {

    }
}