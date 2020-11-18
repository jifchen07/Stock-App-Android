package com.example.stockapp;

import java.io.Serializable;

public class Stock implements Serializable {
    private String ticker;
    private String name;
    private double lastPrice;
    private int numOfShares;
    private boolean isFavorite;
    private double change;
    private double totalCost;


    public Stock(String ticker, String name, double lastPrice, int numOfShares, boolean isFavorite, double change, double totalCost) {
        this.ticker = ticker;
        this.name = name;
        this.lastPrice = lastPrice;
        this.numOfShares = numOfShares;
        this.isFavorite = isFavorite;
        this.change = change;
        this.totalCost = totalCost;
    }


    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public int getNumOfShares() {
        return numOfShares;
    }

    public void setNumOfShares(int numOfShares) {
        this.numOfShares = numOfShares;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}
