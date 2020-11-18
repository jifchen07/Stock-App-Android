package com.example.stockapp;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    private Stock stock;
    private MyApplication appData;

    private String ticker;
    private String name;
    private String description;

    private Double lastPrice;
    private Double changePrice;
    private Double lowPrice;
    private Double midPrice;
    private Double highPrice;
    private Double bidPrice;
    private Double openPrice;

    private int volume;

    private WebView webView;


    private RequestQueue queue;

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
        ticker = stock.getTicker();

        appData = (MyApplication) getApplicationContext();

        Log.d(TAG, "onCreate: " + stock.getTicker());
        Log.d(TAG, "onCreate: " + appData.getPortfolioList());
        Log.d(TAG, "onCreate: " + appData.getWatchList());

        queue = Volley.newRequestQueue(getApplicationContext());

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled (true);
        webView.loadUrl("file:///android_asset/highchart.html?ticker=" + ticker);
    }

    private void fetchDescription() {
        String url = "https://stock-search-backend-110320.wl.r.appspot.com/search/description/" + ticker;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            name = response.getString("name");
                            description = response.getString("description");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonObjectRequest);
    }

    private void fetchLatestPrice() {
        String url = "https://stock-search-backend-110320.wl.r.appspot.com/search/latestprice/" + ticker;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject data = response.getJSONObject(0);
                            lastPrice = data.getDouble("last");
                            changePrice = lastPrice - data.getDouble("prevClose");
                            lowPrice = data.getDouble("low");
                            midPrice = data.getDouble("mid");
                            highPrice = data.getDouble("high");
                            bidPrice = data.getDouble("bid");
                            openPrice = data.getDouble("open");
                            volume = data.getInt("volume");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(jsonArrayRequest);
    }

}