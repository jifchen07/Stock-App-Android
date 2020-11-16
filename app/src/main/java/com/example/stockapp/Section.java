package com.example.stockapp;

import java.util.List;

public class Section {

    private String sectionName;
    private List<Stock> sectionItems;

    public Section(String sectionName, List<Stock> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<Stock> getSectionItems() {
        return sectionItems;
    }

    public void setSectionItems(List<Stock> sectionItems) {
        this.sectionItems = sectionItems;
    }

    @Override
    public String toString() {
        return "Section{" +
                "sectionName='" + sectionName + '\'' +
                ", sectionItems=" + sectionItems +
                '}';
    }
}
