package com.example.stockapp;

import android.app.Application;
import android.content.SharedPreferences;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MyApplication extends Application {
    private Map<String, Stock> stockSet;
    private ArrayList<Stock> portfolioStockList;
    private ArrayList<Stock> favoritesStockList;
    private Double freeMoney;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson;

    public void init() {
        sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    public Map<String, Stock> getStockSet() {
        return stockSet;
    }

    public void setStockSet(Map<String, Stock> stockSet) {
        this.stockSet = stockSet;
    }

    public ArrayList<Stock> getPortfolioStockList() {
        return portfolioStockList;
    }

    public void setPortfolioStockList(ArrayList<Stock> portfolioStockList) {
        this.portfolioStockList = portfolioStockList;
    }

    public ArrayList<Stock> getFavoritesStockList() {
        return favoritesStockList;
    }

    public void setFavoritesStockList(ArrayList<Stock> favoritesStockList) {
        this.favoritesStockList = favoritesStockList;
    }

    public Double getFreeMoney() {
        return freeMoney;
    }

    public void setFreeMoney(Double freeMoney) {
        this.freeMoney = freeMoney;
    }

    public Double getNetWorth() {
        Double holdingValue = 0.00;
        Stock stock;
        for (int i = 0; i < portfolioStockList.size(); i++) {
            stock = portfolioStockList.get(i);
            holdingValue += stock.getLastPrice() * stock.getNumOfShares();
        }
        return holdingValue + freeMoney;
    }

    public void saveStockSet() {
        String json_map = gson.toJson(stockSet);
        editor.putString("stock map", json_map);
        editor.apply();
    }

    public void savePortfolio() {
        ArrayList<String> portfolioList = new ArrayList<>();
        for (int i = 0; i < portfolioStockList.size(); i++) {
            portfolioList.add(portfolioStockList.get(i).getTicker());
        }
        String json_portfolio = gson.toJson(portfolioList);
        editor.putString("portfolio", json_portfolio);
        editor.apply();
    }

    public void saveWatchList() {
        ArrayList<String> watchList = new ArrayList<>();
        for (int i = 0; i < favoritesStockList.size(); i++) {
            watchList.add(favoritesStockList.get(i).getTicker());
        }
        String json_watchlist = gson.toJson(watchList);
        editor.putString("watchlist", json_watchlist);
        editor.apply();
    }

    public void saveFreeMoney() {
        ArrayList<Double> freeMoneyList= new ArrayList<>(Arrays.asList(getFreeMoney()));
        String json_freeMoney = gson.toJson(freeMoneyList);
        editor.putString("free money", json_freeMoney);
        editor.apply();
    }

}
