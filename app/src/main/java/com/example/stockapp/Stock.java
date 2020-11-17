package com.example.stockapp;

public class Stock {
    private String ticker;
    private String name;
    private double lastPrice;
    private int numOfShares;
    private boolean isFavorite;
    private double change;


    public Stock(String ticker, String name, double lastPrice, int numOfShares, boolean isFavorite, double change) {
        this.ticker = ticker;
        this.name = name;
        this.lastPrice = lastPrice;
        this.numOfShares = numOfShares;
        this.isFavorite = isFavorite;
        this.change = change;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public int getNumOfShares() {
        return numOfShares;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public double getChange() {
        return change;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public void setNumOfShares(int numOfShares) {
        this.numOfShares = numOfShares;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setChange(double change) {
        this.change = change;
    }
}
