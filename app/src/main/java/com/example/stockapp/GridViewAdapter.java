package com.example.stockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {
    Context context;
    String[] gridData;
    LayoutInflater inflater;

    public GridViewAdapter(Context context, String[] gridData) {
        this.context = context;
        this.gridData = gridData;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return gridData.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.grid_item, null);
        TextView textView = (TextView) convertView.findViewById(R.id.textViewGrid);
        textView.setText(gridData[position]);

        return convertView;
    }
}
