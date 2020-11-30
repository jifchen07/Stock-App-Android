package com.example.stockapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.ms.square.android.expandabletextview.ExpandableTextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DetailActivity extends AppCompatActivity implements NewsCardAdapter.OnNewsListener {
    private static final String TAG = "DetailActivity";
    private Stock stock;
    private MyApplication appData;
    private Map<String, Stock> stockSet;

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
    Button tradeButton;

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

    boolean isFav;

    ArrayList<Stock> favoritesStockList;
    ArrayList<Stock> portfolioStockList;

    Toast toast;

    ConstraintLayout detailMainLayout;
    ProgressBar progressBar;
    TextView pendingTextView;

    int numOfRequestToMake;

    Dialog tempDialog;
    int numOfSharesInput;
//    SharedPreferences.Editor editor;
//    Gson gson;

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

        ticker = intent.getStringExtra(MainActivity.EXTRA_TICKER);

        appData = (MyApplication) getApplicationContext();
        stockSet = appData.getStockSet();
        favoritesStockList = appData.getFavoritesStockList();
        portfolioStockList = appData.getPortfolioStockList();

        numOfRequestToMake = 3;

        if (stockSet.containsKey(ticker)) {
            stock = stockSet.get(ticker);
            isFav = stock.isFavorite();
        } else {
            stock = new Stock(ticker, "", 0.0, 0, false, 0.0,0.0);
            isFav = false;
        }


        queue = Volley.newRequestQueue(getApplicationContext());

//        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
//        editor = sharedPreferences.edit();
//        gson = new Gson();

        findViews();

        fetchDescription();
        fetchLatestPrice();
        fetchNews();
        // renderViews();

        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled (true);
        webView.loadUrl("file:///android_asset/highchart.html?ticker=" + ticker);

        gridViewAdapter = new GridViewAdapter(getApplicationContext(), gridData);
        gridViewStats.setAdapter(gridViewAdapter);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        newsAdapter = new NewsCardAdapter(getApplicationContext(), newsItems, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(newsAdapter);

        setTradeDialog();
        refreshDataPer15Seconds();
    }

    private void refreshDataPer15Seconds() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchLatestPrice();
            }
        }, 15000, 15000);
    }

    private void checkRequestStatus() {
        numOfRequestToMake--;
        if (numOfRequestToMake == 0) {
            progressBar.setVisibility(View.GONE);
            pendingTextView.setVisibility(View.GONE);
            detailMainLayout.setVisibility(View.VISIBLE);
        } else if (numOfRequestToMake < 0 ) {
            numOfRequestToMake = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem fav = menu.findItem(R.id.favorite);
        MenuItem unfav = menu.findItem(R.id.unFavorite);

        fav.setVisible(isFav);
        unfav.setVisible(!isFav);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite:
                isFav = false;
                Stock stock = stockSet.get(ticker);
                favoritesStockList.remove(stock);
                if (stock.getNumOfShares() == 0) {
                    stockSet.remove(ticker);
                } else {
                    stockSet.get(ticker).setFavorite(false);
                }
                supportInvalidateOptionsMenu();
                appData.saveStockSet();
                appData.saveWatchList();
                return true;
            case R.id.unFavorite:
                isFav = true;
                this.stock.setFavorite(true);
                favoritesStockList.add(this.stock);
                if (stockSet.containsKey(ticker)) {
                    stockSet.get(ticker).setFavorite(true);
                } else {
                    stockSet.put(ticker, this.stock);
                }
                supportInvalidateOptionsMenu();
                appData.saveStockSet();
                appData.saveWatchList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void findViews() {
        tickerTextView = findViewById(R.id.textViewTicker);
        nameTextView = findViewById(R.id.textViewName);
        lastPriceTextView = findViewById(R.id.textViewLastPrice);
        changeTextView = findViewById(R.id.textViewChange);

        sharesOwnedTextView = findViewById(R.id.textViewSharesOwned);
        marketValueTextView = findViewById(R.id.textViewMarketValue);
        tradeButton = findViewById(R.id.buttonTrade);
        gridViewStats = findViewById(R.id.gridViewStats);

        expTv1 = findViewById(R.id.expand_text_view);

        firstNewsImageView = findViewById(R.id.imageViewFirstNews);
        firstNewsSourceTextView = findViewById(R.id.textViewFirstNewsSource);
        firstNewsDateTextView = findViewById(R.id.textViewFirstNewsDate);
        firstNewsTitleTextView = findViewById(R.id.textViewFirstNewsTitle);

        recyclerView = findViewById(R.id.recyclerViewNews);

        detailMainLayout = findViewById(R.id.detail_main);
        detailMainLayout.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.ProgressBar02);
        pendingTextView = findViewById(R.id.textViewPending02);

    }

    private void setTradeDialog() {
        tradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTradeDialog();
            }
        });
    }

    private void updatePriceInDialog() {
        if (tempDialog != null) {
            TextView tradeTotalTextView = tempDialog.findViewById(R.id.textViewTradeTotal);
            tradeTotalTextView.setText(getPriceLine(numOfSharesInput, stock.getLastPrice()));
        }
    }

    private void openTradeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.trade_dialog);
        tempDialog = dialog;
        final EditText numOfSharesEditText = dialog.findViewById(R.id.editTextNumber);
        final TextView tradeTotalTextView = dialog.findViewById(R.id.textViewTradeTotal);
        TextView tradeTitleTextView = dialog.findViewById(R.id.textViewTradeTitle);
        TextView availabilityTextView = dialog.findViewById(R.id.textViewAvailability);
        Button buyButton = dialog.findViewById(R.id.buttonBuy);
        Button sellButton = dialog.findViewById(R.id.buttonSell);

        tradeTitleTextView.setText("Trade " + stock.getName() + " shares");
        tradeTotalTextView.setText(getPriceLine(0, stock.getLastPrice()));
        availabilityTextView.setText(
                "$" + String.format("%.2f", appData.getFreeMoney()) + " available to buy " + ticker);

        numOfSharesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = numOfSharesEditText.getText().toString();
                try {
                    numOfSharesInput = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    numOfSharesInput = 0;
                }
                tradeTotalTextView.setText(getPriceLine(numOfSharesInput, stock.getLastPrice()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }


        });

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = numOfSharesEditText.getText().toString();
                try {
                    numOfSharesInput = Integer.parseInt(text);
                    Double totalPrice = numOfSharesInput * stock.getLastPrice();
                    if ( totalPrice > appData.getFreeMoney()) {
                        makeToastMessage("Not enough money to buy");
                    } else if (numOfSharesInput <= 0){
                        makeToastMessage("Cannot buy less than 0 shares");
                    } else {
                        int currentNumOfShares = stock.getNumOfShares();
                        if (currentNumOfShares == 0) {
                            if (!stockSet.containsKey(ticker)) {
                                stockSet.put(ticker, stock);
                            }
                            portfolioStockList.add(stock);
                        }
                        stock.setNumOfShares(currentNumOfShares + numOfSharesInput);
                        appData.setFreeMoney(appData.getFreeMoney() - totalPrice);
                        dialog.dismiss();
                        updatePortfolioInfo();
                        openSuccessDialog("bought", numOfSharesInput, ticker);
                        appData.saveFreeMoney();
                        appData.saveStockSet();
                        appData.savePortfolio();

                    }

                } catch (NumberFormatException e) {
                    makeToastMessage("Please enter valid amount");
                }

            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = numOfSharesEditText.getText().toString();
                try {
                    numOfSharesInput = Integer.parseInt(text);
                    Double totalPrice = numOfSharesInput * stock.getLastPrice();
                    if ( numOfSharesInput > stock.getNumOfShares()) {
                        makeToastMessage("Not enough shares to sell");
                    } else if (numOfSharesInput <= 0){
                        makeToastMessage("Cannot sell less than 0 shares");
                    } else {
                        appData.setFreeMoney(appData.getFreeMoney() + totalPrice);
                        if (stock.getNumOfShares() > numOfSharesInput) {
                            stock.setNumOfShares(stock.getNumOfShares() - numOfSharesInput);
                        } else {
                            stock.setNumOfShares(0);
                            portfolioStockList.remove(stock);
                            if (!stock.isFavorite()) {
                                stockSet.remove(ticker);
                            }
                        }

                        dialog.dismiss();
                        updatePortfolioInfo();
                        openSuccessDialog("sold", numOfSharesInput, ticker);
                        appData.saveFreeMoney();
                        appData.saveStockSet();
                        appData.savePortfolio();
                    }

                } catch (NumberFormatException e) {
                    makeToastMessage("Please enter valid amount");
                }
            }
        });

        dialog.show();
    }

    private void openSuccessDialog(String type, int num, String ticker) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.success_dialog);
        TextView availabilityTextView = dialog.findViewById(R.id.textViewCongrats);
        availabilityTextView.setText(
                "You have successfully " + type + " " + num + " shares of " + ticker);
        Button doneButton = dialog.findViewById(R.id.buttonDone);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static String getPriceLine(int num, Double price) {
        Double total = num * price;
        return "" + num + " x " + "$" + String.format("%.2f", price)
                + "/share = " + "$" + String.format("%.2f", total);
    }

    private void makeToastMessage(String message) {
        if (toast != null) { toast.cancel(); }
        toast = Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updatePortfolioInfo() {
        if (stock.getNumOfShares() > 0) {
            sharesOwnedTextView.setText("Shares owned: " + stock.getNumOfShares());
            marketValue = lastPrice * stock.getNumOfShares();
            marketValueTextView.setText("Market Value: $" + String.format("%.2f", marketValue));
        } else {
            sharesOwnedTextView.setText("You have 0 shares of " + ticker);
            marketValueTextView.setText("Start trading!");
        }

    }


    private void fetchDescription() {
        String url = "https://stock-search-backend-110320.wl.r.appspot.com/search/description/" + ticker;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        checkRequestStatus();
                        try {
                            name = response.getString("name");
                            stock.setName(name);
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
                        checkRequestStatus();
                        Log.d(TAG, "onResponse: fetchLatestPrice()");
                        try {
                            JSONObject data = response.getJSONObject(0);
                            lastPrice = data.getDouble("last");
                            changePrice = lastPrice - data.getDouble("prevClose");
                            stock.setLastPrice(lastPrice);
                            stock.setChange(changePrice);

                            lowPrice = data.isNull("low") ? null : data.getDouble("low");
                            midPrice = data.isNull("mid") ? null : data.getDouble("mid");
                            highPrice = data.isNull("high") ? null : data.getDouble("high");
                            bidPrice = data.isNull("bidPrice") ? null : data.getDouble("bidPrice");
                            openPrice = data.isNull("open") ? null : data.getDouble("open");
                            volume = data.isNull("volume") ? null : data.getInt("volume");

                            lastPriceTextView.setText("$" + String.format("%.2f", lastPrice));
                            changeTextView.setText("$" + String.format("%.2f", changePrice));

                            if (changePrice > 0) {
                                changeTextView.setTextColor(Color.parseColor("#51A874"));
                            } else if (changePrice < 0) {
                                changeTextView.setTextColor(Color.RED);
                            } else {
                                changeTextView.setTextColor(Color.GRAY);
                            }

                            updatePortfolioInfo();
                            updatePriceInDialog();

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
                        checkRequestStatus();
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
                            final String newsUrl = firstNews.getString("url");

                            CardView firstNewsCardView = findViewById(R.id.cardViewFirstNews);
                            firstNewsCardView.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    onNewsClick(newsUrl);
                                }
                            });
                            firstNewsCardView.setOnLongClickListener(new View.OnLongClickListener() {

                                @Override
                                public boolean onLongClick(View v) {
                                    onNewsLongClick(firstNews);
                                    return true;
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

    private void openNewsDialog(JSONObject newsItem) throws JSONException {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.news_dialog);

        ImageView imageViewNewsDialog = dialog.findViewById(R.id.imageViewNewsDialog);
        TextView textViewNewsDialogTitle = dialog.findViewById(R.id.textViewNewsDialogTitle);
        ImageView imageViewTwitter = dialog.findViewById(R.id.imageViewTwitter);
        ImageView imageViewChrome = dialog.findViewById(R.id.imageViewChrome);

        String urlToImage = newsItem.getString("urlToImage");
        String title = newsItem.getString("title");
        final String url = newsItem.getString("url");
        final String urlTwitter = "https://twitter.com/intent/tweet?"
                + "text=Check out this Link:%0A" + url;
        Glide.with(this)
                .load(urlToImage)
                .centerCrop()
                .into(imageViewNewsDialog);
        textViewNewsDialogTitle.setText(title);

        imageViewTwitter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlTwitter));
                startActivity(browserIntent);
            }
        });

        imageViewChrome.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        dialog.show();
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

    @Override
    public void onNewsLongClick(JSONObject newsItem) {
        try {
            openNewsDialog(newsItem);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}