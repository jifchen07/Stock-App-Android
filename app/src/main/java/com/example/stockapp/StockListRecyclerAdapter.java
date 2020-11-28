package com.example.stockapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class StockListRecyclerAdapter extends RecyclerView.Adapter<StockListRecyclerAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract{

    ArrayList<Stock> items;
    private OnArrowClickListener onArrowClickListener;


    public StockListRecyclerAdapter(ArrayList<Stock> items, OnArrowClickListener onArrowClickListener) {
        this.items = items;
        this.onArrowClickListener = onArrowClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_row, parent, false);
        return new StockListRecyclerAdapter.ViewHolder(view, onArrowClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Stock stock = items.get(position);
        holder.itemTextViewUL.setText(stock.getTicker());
        holder.itemTextViewUR.setText(String.valueOf(stock.getLastPrice()));
        holder.itemTextViewLR.setText(String.valueOf(stock.getChange()));
        if (stock.getNumOfShares() > 0) {
            holder.itemTextViewLL.setText(String.valueOf(stock.getNumOfShares()));
        } else {
            holder.itemTextViewLL.setText(stock.getName());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(StockListRecyclerAdapter.ViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(StockListRecyclerAdapter.ViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.WHITE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView itemTextViewUL, itemTextViewUR, itemTextViewLL, itemTextViewLR;
        ImageView imageView;
        View rowView;

        OnArrowClickListener onArrowClickListener;

        public ViewHolder(@NonNull @NotNull View itemView, OnArrowClickListener onArrowClickListener) {
            super(itemView);

            itemTextViewUL = itemView.findViewById(R.id.itemTextViewUL);
            itemTextViewUR = itemView.findViewById(R.id.itemTextViewUR);
            itemTextViewLL = itemView.findViewById(R.id.itemTextViewLL);
            itemTextViewLR = itemView.findViewById(R.id.itemTextViewLR);
            imageView = itemView.findViewById(R.id.imageView);
            this.onArrowClickListener = onArrowClickListener;

            imageView.setOnClickListener(this);

            rowView = itemView;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Stock stock = items.get(position);
            onArrowClickListener.onArrowClick(stock.getTicker());
        }
    }


    public interface OnArrowClickListener {
        void onArrowClick(String ticker);
    }

    public void removeItem(int position) {
        items.get(position).setFavorite(false);
        items.remove(position);
        notifyItemRemoved(position);
    }

}
