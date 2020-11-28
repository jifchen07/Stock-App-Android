package com.example.stockapp;

//import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;
//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements StockListRecyclerAdapter.OnArrowClickListener {

    public static final String EXTRA_TICKER = "com.example.stockapp.EXTRA_TICKER";

    private static final String TAG = "MainActivity";

    // for autocomplete
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

    // for section recycler view
//    List<Section> sectionList = new ArrayList<>();
//    RecyclerView mainRecyclerView;

    Map<String, Stock> stockSet;
    ArrayList<String> portfolioList;
    ArrayList<String> watchList;
    ArrayList<Double> freeMoneyList;
    ArrayList<Stock> portfolioStockList = new ArrayList<>();
    ArrayList<Stock> favoritesStockList = new ArrayList<>();

//    MainRecyclerAdapter mainRecyclerAdapter;


    // for application data
    MyApplication appData;

    SearchView searchView;
    SearchView.SearchAutoComplete searchAutoComplete;

    StockListRecyclerAdapter portfolioRecyclerAdapter;
    StockListRecyclerAdapter favoritesRecyclerAdapter;
    RecyclerView portfolioRecyclerView;
    RecyclerView favoritesRecyclerView;

    TextView netWorthTextView;

    boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // initData();
        loadData();

        portfolioRecyclerView = findViewById(R.id.portfolioRecyclerView);
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        portfolioRecyclerAdapter = new StockListRecyclerAdapter(portfolioStockList, this);
        favoritesRecyclerAdapter = new StockListRecyclerAdapter(favoritesStockList, this);
        ItemTouchHelper.Callback callback = new ItemMoveCallback(favoritesRecyclerAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(favoritesRecyclerView);

        portfolioRecyclerView.setAdapter(portfolioRecyclerAdapter);
        favoritesRecyclerView.setAdapter(favoritesRecyclerAdapter);
        netWorthTextView = findViewById(R.id.textViewNetWorth);

        enableSwipeToDelete();
        // update the price data every 15 seconds
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                updatePrice();
//            }
//        }, 15000, 15000);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main, menu);
            MenuItem item = menu.findItem(R.id.action_search);
            searchView = (SearchView) item.getActionView();
            searchAutoComplete = searchView.findViewById(com.google.android.material.R.id.search_src_text);
            autoComplete();
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!loaded) {
            loaded = true;
        } else {
            portfolioRecyclerAdapter.notifyDataSetChanged();
            favoritesRecyclerAdapter.notifyDataSetChanged();
            netWorthTextView.setText(String.format("%.2f", appData.getNetWorth()));

        }
    }

    private void autoComplete() {
//        final AppCompatAutoCompleteTextView autoCompleteTextView =
//                findViewById(R.id.auto_complete_edit_text);


        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        searchAutoComplete.setThreshold(3);
        searchAutoComplete.setAdapter(autoSuggestAdapter);
        searchAutoComplete.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
//                        selectedText.setText(autoSuggestAdapter.getObject(position));
                        String line = autoSuggestAdapter.getObject(position);
                        String ticker = line.split(" - ")[0];
                        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                        intent.putExtra(EXTRA_TICKER, ticker);
                        startActivity(intent);
                    }
                });
        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        makeApiCall(searchAutoComplete.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    private void makeApiCall(String text) {
        ApiCall.make(this, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
//                    JSONObject responseObject = new JSONObject(response);
//                    JSONArray array = responseObject.getJSONArray("results");
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject row = array.getJSONObject(i);
//                        stringList.add(row.getString("trackName"));
//                    }
                    JSONArray responseObject = new JSONArray(response);
                    for (int i = 0; i < responseObject.length(); i++) {
                        JSONObject suggestItem = responseObject.getJSONObject(i);
                        String ticker = suggestItem.getString("ticker");
                        String name = suggestItem.getString("name");
                        String line = ticker + " - " + name;
                        stringList.add(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }


    // load data from shared preference
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json_map = sharedPreferences.getString("stock map", null);
        Type type = new TypeToken<HashMap<String, Stock>>(){}.getType();
        stockSet = gson.fromJson(json_map, type);
        if (stockSet == null) {
            Log.d(TAG, "loadData: stockSet is null");
            stockSet = new HashMap<>();
            stockSet.put("AAPL", new Stock("AAPL", "Apple Inc", 108.86, 5, true, -6.46, 500));
            stockSet.put("TSLA", new Stock("TSLA", "Tesla Inc", 388.04, 3, true, -22.79, 1000));
            stockSet.put("NFLX", new Stock("NFLX", "NetFlix Inc", 100.00, 0, true, -5.55, 0));
        }

        String json_portfolio = sharedPreferences.getString("portfolio", null);
        Type type2 = new TypeToken<ArrayList<String>>(){}.getType();
        portfolioList = gson.fromJson(json_portfolio, type2);
        if (portfolioList == null) {
            Log.d(TAG, "loadData: portfolio is null");
            portfolioList = new ArrayList<>(Arrays.asList("AAPL", "TSLA"));
        }

        String json_watchList = sharedPreferences.getString("watchlist", null);
        watchList = gson.fromJson(json_watchList, type2);
        if (watchList == null) {
            Log.d(TAG, "loadData: watchlist is null");
            watchList= new ArrayList<>(Arrays.asList("NFLX", "AAPL", "TSLA"));
        }

        String json_freeMoney = sharedPreferences.getString("free money", null);
        Type type3 = new TypeToken<ArrayList<Double>>(){}.getType();
        freeMoneyList = gson.fromJson(json_freeMoney, type3);
        if (freeMoneyList == null) {
            Log.d(TAG, "loadData: free money is new");
            freeMoneyList = new ArrayList<>(Arrays.asList(18500.00));
        }

        updateStockLists();


        passDataToApplication();

        saveData();

        if (stockSet.size() > 0) {
            updatePrice();
        }
    }

    private void passDataToApplication() {
        appData = (MyApplication) getApplicationContext();
        appData.setStockSet(stockSet);
        appData.setFavoritesStockList(favoritesStockList);
        appData.setPortfolioStockList(portfolioStockList);
        appData.setFreeMoney(freeMoneyList.get(0));
    }

    private void updateStockLists() {

        for (int i = 0; i < portfolioList.size(); i++) {
            portfolioStockList.add(stockSet.get(portfolioList.get(i)));
        }
        for (int i = 0; i < watchList.size(); i++) {
            favoritesStockList.add(stockSet.get(watchList.get(i)));
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json_map = gson.toJson(stockSet);

        portfolioList.clear();
        for (int i = 0; i < portfolioStockList.size(); i++) {
            portfolioList.add(portfolioStockList.get(i).getTicker());
        }
        String json_portfolio = gson.toJson(portfolioList);

        watchList.clear();
        for (int i = 0; i < favoritesStockList.size(); i++) {
            watchList.add(favoritesStockList.get(i).getTicker());
        }
        String json_watchlist = gson.toJson(watchList);

        freeMoneyList.set(0, appData.getFreeMoney());
        String json_freeMoney = gson.toJson(freeMoneyList);
        editor.putString("stock map", json_map);
        editor.putString("portfolio", json_portfolio);
        editor.putString("watchlist", json_watchlist);
        editor.putString("free money", json_freeMoney);
        editor.apply();
    }



    private void updatePrice() {
        List<String> tickers = new ArrayList<>(stockSet.keySet());
        String tickersString  = "";
        for (int i = 0; i < tickers.size(); i++) {
            tickersString = tickersString + tickers.get(i) + ",";
        }
        tickersString = tickersString.substring(0, tickersString.length() - 1);
        String url = "https://stock-search-backend-110320.wl.r.appspot.com/search/latestprice/" + tickersString;

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject data = response.getJSONObject(i);
                                String ticker = data.getString("ticker");
                                Double lastPrice = data.getDouble("last");
                                Double prevClose = data.getDouble("prevClose");
                                Double change = (lastPrice - prevClose) / prevClose * 100;
                                stockSet.get(ticker).setLastPrice(lastPrice);
                                stockSet.get(ticker).setChange(change);
                            }
                            Log.d(TAG, "onResponse: " + stockSet.get("AAPL").getLastPrice());
                            portfolioRecyclerAdapter.notifyDataSetChanged();
                            favoritesRecyclerAdapter.notifyDataSetChanged();
                            netWorthTextView.setText(String.format("%.2f", appData.getNetWorth()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(jsonArrayRequest);
    }

    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                favoritesRecyclerAdapter.removeItem(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(favoritesRecyclerView);
    }


    @Override
    public void onArrowClick(String ticker) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(EXTRA_TICKER, ticker);
        startActivity(intent);
    }
}