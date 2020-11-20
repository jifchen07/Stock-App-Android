package com.example.stockapp;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements NewsCardAdapter.OnNewsListener {
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
    private Double marketValue;

    private int volume;

    private WebView webView;


    private RequestQueue queue;

    TextView tickerTextView;
    TextView nameTextView;
    TextView lastPriceTextView;
    TextView changeTextView;
    TextView sharesOwnedTextView;
    TextView marketValueTextView;

    GridView gridViewStats;
    GridViewAdapter gridViewAdapter;
    String[] gridData = {"Current Price: ", "Low: ", "Bid Price: ", "Open Price: ", "Mid: ", "High: ", "Volume: "};

    ExpandableTextView expTv1;

    ArrayList<JSONObject> newsItems = new ArrayList<>();
    ImageView firstNewsImageView;
    TextView firstNewsSourceTextView;
    TextView firstNewsDateTextView;
    TextView firstNewsTitleTextView;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter newsAdapter;

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

        findViews();
        fetchDescription();
        fetchLatestPrice();
        fetchNews();
        // renderViews();

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled (true);
        webView.loadUrl("file:///android_asset/highchart.html?ticker=" + ticker);

        gridViewAdapter = new GridViewAdapter(getApplicationContext(), gridData);
        gridViewStats.setAdapter(gridViewAdapter);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        newsAdapter = new NewsCardAdapter(getApplicationContext(), newsItems, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(newsAdapter);


    }

    private void findViews() {
        tickerTextView = (TextView) findViewById(R.id.textViewTicker);
        nameTextView = (TextView) findViewById(R.id.textViewName);
        lastPriceTextView = (TextView) findViewById(R.id.textViewLastPrice);
        changeTextView = (TextView) findViewById(R.id.textViewChange);

        sharesOwnedTextView = (TextView) findViewById(R.id.textViewSharesOwned);
        marketValueTextView = (TextView) findViewById(R.id.textViewMarketValue);
        gridViewStats = (GridView) findViewById(R.id.gridViewStats);

        expTv1 = (ExpandableTextView) findViewById(R.id.expand_text_view);

        firstNewsImageView = (ImageView) findViewById(R.id.imageViewFirstNews);
        firstNewsSourceTextView = (TextView) findViewById(R.id.textViewFirstNewsSource);
        firstNewsDateTextView = (TextView) findViewById(R.id.textViewFirstNewsDate);
        firstNewsTitleTextView = (TextView) findViewById(R.id.textViewFirstNewsTitle);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewNews);

    }

    private void renderViews() {
        tickerTextView.setText(ticker);
        nameTextView.setText(name);
        lastPriceTextView.setText(Double.toString(lastPrice));
        changeTextView.setText(Double.toString(changePrice));
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

                            nameTextView.setText(name);
                            tickerTextView.setText(ticker);
                            expTv1.setText(description);

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
                            lowPrice = data.isNull("low") ? null : data.getDouble("low");
                            midPrice = data.isNull("mid") ? null : data.getDouble("mid");
                            highPrice = data.isNull("high") ? null : data.getDouble("high");
                            bidPrice = data.isNull("bidPrice") ? null : data.getDouble("bidPrice");
                            openPrice = data.isNull("open") ? null : data.getDouble("open");
                            volume = data.isNull("volume") ? null : data.getInt("volume");

                            marketValue = stock.getNumOfShares() * lastPrice;

                            Log.d(TAG, "onResponse: " + String.format("%.2f", lastPrice));
                            lastPriceTextView.setText(String.format("%.2f", lastPrice));
                            changeTextView.setText(String.format("%.2f", changePrice));
                            sharesOwnedTextView.setText("Shares owned: " + stock.getNumOfShares());
                            marketValueTextView.setText("Market Value: $" + String.format("%.2f", marketValue));

                            gridData[0] = "Current Price: " + String.format("%.2f", lastPrice);
                            gridData[1] = "Low: " + ((lowPrice == null) ? "-" : String.format("%.2f", lowPrice));
                            gridData[2] = "Bid Price: " + ((bidPrice == null) ? "-" : String.format("%.2f", bidPrice));
                            gridData[3] = "Open Price: " + ((openPrice == null) ? "-" : String.format("%.2f", openPrice));
                            gridData[4] = "Mid: " + ((midPrice == null) ? "-" : String.format("%.2f", midPrice));
                            gridData[5] = "High: " + ((highPrice == null) ? "-" : String.format("%.2f", highPrice));
                            gridData[6] = "Volume: " + volume;

                            gridViewAdapter.notifyDataSetChanged();

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

    private void fetchNews() {
        String url = "https://stock-search-backend-110320.wl.r.appspot.com/search/news/" + ticker;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            newsItems.clear();
                            JSONArray articles = response.getJSONArray("articles");
                            JSONObject object;
                            for (int i = 1; i < articles.length(); i++) {
                                object = articles.getJSONObject(i);
                                newsItems.add(object);
                            }

                            final JSONObject firstNews = articles.getJSONObject(0);
                            Glide.with(getApplicationContext())
                                    .load(firstNews.getString("urlToImage"))
                                    .centerCrop()
                                    .into(firstNewsImageView);
                            firstNewsSourceTextView
                                    .setText(firstNews.getJSONObject("source").getString("name"));
                            firstNewsTitleTextView
                                    .setText(firstNews.getString("title"));
                            firstNewsDateTextView
                                    .setText(calTimeDiff(firstNews.getString("publishedAt")));
                            CardView firstNewsCardView = (CardView) findViewById(R.id.cardViewFirstNews);
                            firstNewsCardView.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent browserIntent = null;
                                    try {
                                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(firstNews.getString("url")));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(browserIntent);
                                }
                            });

                            newsAdapter.notifyDataSetChanged();
//                            layoutManager = new LinearLayoutManager(getApplicationContext());
//                            newsAdapter = new NewsCardAdapter(getApplicationContext(), newsItems);
//                            recyclerView.setLayoutManager(layoutManager);
//                            recyclerView.setAdapter(newsAdapter);

                        } catch (JSONException | ParseException e) {
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

    public static String calTimeDiff(String publishedAt) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date1 = format.parse(publishedAt);
        Date date2 = new Date();
        long difference = date2.getTime() - date1.getTime();
        long seconds = difference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) {
            return String.format("%d days ago", days);
        } else {
            return String.format("%d minutes ago", minutes);
        }
    }

    @Override
    public void onNewsClick(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}