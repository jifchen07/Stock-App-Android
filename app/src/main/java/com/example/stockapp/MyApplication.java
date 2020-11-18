package com.example.stockapp;

import android.app.Application;

import java.util.ArrayList;
import java.util.Map;

public class MyApplication extends Application {
    private Map<String, Stock> stockSet;
    private ArrayList<String> portfolioList;
    private ArrayList<String> watchList;

    public Map<String, Stock> getStockSet() {
        return stockSet;
    }

    public void setStockSet(Map<String, Stock> stockSet) {
        this.stockSet = stockSet;
    }

    public ArrayList<String> getPortfolioList() {
        return portfolioList;
    }

    public void setPortfolioList(ArrayList<String> portfolioList) {
        this.portfolioList = portfolioList;
    }

    public ArrayList<String> getWatchList() {
        return watchList;
    }

    public void setWatchList(ArrayList<String> watchList) {
        this.watchList = watchList;
    }
}
