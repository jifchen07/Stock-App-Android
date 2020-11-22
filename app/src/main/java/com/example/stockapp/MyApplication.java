package com.example.stockapp;

import android.app.Application;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

public class MyApplication extends Application {
    private Map<String, Stock> stockSet;
    private ArrayList<Stock> portfolioStockList;
    private ArrayList<Stock> favoritesStockList;

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
}
