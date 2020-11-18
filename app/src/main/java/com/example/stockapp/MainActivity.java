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
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;
//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements ChildRecyclerAdapter.OnArrowClick {

    public static final String EXTRA_TICKER = "com.example.stockapp.EXTRA_TICKER";

    private static final String TAG = "MainActivity";

    // for autocomplete
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;

    // for section recycler view
    List<Section> sectionList = new ArrayList<>();
    RecyclerView mainRecyclerView;

    Map<String, Stock> stockSet;
    ArrayList<String> portfolioList;
    ArrayList<String> watchList;

    MainRecyclerAdapter mainRecyclerAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // initData();
        loadData();

        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        mainRecyclerAdapter = new MainRecyclerAdapter(sectionList, this);
        mainRecyclerView.setAdapter(mainRecyclerAdapter);

        // update the price data every 15 seconds
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                updatePrice();
//            }
//        }, 15000, 15000);

        // for auto complete
        autoComplete();

    }

    private void autoComplete() {
        final AppCompatAutoCompleteTextView autoCompleteTextView =
                findViewById(R.id.auto_complete_edit_text);
        final TextView selectedText = findViewById(R.id.selected_item);


        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        selectedText.setText(autoSuggestAdapter.getObject(position));
                    }
                });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
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
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        makeApiCall(autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    private void loadData2() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("section one", null);
        Type type = new TypeToken<ArrayList<Stock>>(){}.getType();
        List<Stock> sectionOneItems = gson.fromJson(json, type);

        if (sectionOneItems == null) {
            sectionOneItems = new ArrayList<>();
            sectionOneItems.add(new Stock("AAPL", "Apple Inc", 108.86, 5, true, -6.46, 500));
            sectionOneItems.add(new Stock("TSLA", "Tesla Inc", 388.04, 3, true, -22.79, 1000));
        }

        String json2 = sharedPreferences.getString("section two", null);
        List<Stock> sectionTwoItems = gson.fromJson(json2, type);

        if (sectionTwoItems == null) {
            sectionTwoItems = new ArrayList<>();
            sectionTwoItems.add(new Stock("NFLX", "NetFlix Inc", 100.00, 0, true, -5.55, 0));
            sectionTwoItems.add(new Stock("AAPL", "Apple Inc", 108.86, 5, true, -6.46, 500));
            sectionTwoItems.add(new Stock("TSLA", "Tesla Inc", 388.04, 3, true, -22.79, 1000));
        }

        String sectionOneName = "PORTFOLIO";
        String sectionTwoName = "FAVORITES";
        sectionList.add(new Section(sectionOneName, sectionOneItems));
        sectionList.add(new Section(sectionTwoName, sectionTwoItems));
    }

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
        if (stockSet.size() > 0) {
            updatePrice();
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

        saveData();

        updateSectionList();

    }

    private void updateSectionList() {
        String sectionOneName = "PORTFOLIO";
        String sectionTwoName = "FAVORITES";
        List<Stock> sectionOneItems = new ArrayList<>();
        List<Stock> sectionTwoItems = new ArrayList<>();
        for (int i = 0; i < portfolioList.size(); i++) {
            sectionOneItems.add(stockSet.get(portfolioList.get(i)));
        }
        for (int i = 0; i < watchList.size(); i++) {
            sectionTwoItems.add(stockSet.get(watchList.get(i)));
        }

        sectionList = new ArrayList<Section>();
        sectionList.add(new Section(sectionOneName, sectionOneItems));
        sectionList.add(new Section(sectionTwoName, sectionTwoItems));
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json_map = gson.toJson(stockSet);
        String json_portfolio = gson.toJson(portfolioList);
        String json_watchlist = gson.toJson(watchList);
        editor.putString("stock map", json_map);
        editor.putString("portfolio", json_portfolio);
        editor.putString("watchlist", json_watchlist);
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
                            mainRecyclerAdapter.notifyDataSetChanged();
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

    private void makeApiCall(String text) {
        ApiCall.make(this, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray array = responseObject.getJSONArray("results");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject row = array.getJSONObject(i);
                        stringList.add(row.getString("trackName"));
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

    @Override
    public void onArrowClick(Stock stock) {
        Log.d(TAG, "onArrowClick: " + stock.getTicker());
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(EXTRA_TICKER, stock);
        startActivity(intent);
    }
}