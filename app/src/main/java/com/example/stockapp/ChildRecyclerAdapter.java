package com.example.stockapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.List;

public class ChildRecyclerAdapter extends RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder> {

    List<Stock> items;
    private OnArrowClick onArrowClickListener;

    public ChildRecyclerAdapter(List<Stock> items, OnArrowClick onArrowClickListener) {

        this.items = items;
        this.onArrowClickListener = onArrowClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view, onArrowClickListener);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView itemTextViewUL, itemTextViewUR, itemTextViewLL, itemTextViewLR;
        ImageView imageView;

        OnArrowClick onArrowClickListener;

        public ViewHolder(@NonNull @NotNull View itemView, OnArrowClick onArrowClickListener) {
            super(itemView);

            itemTextViewUL = itemView.findViewById(R.id.itemTextViewUL);
            itemTextViewUR = itemView.findViewById(R.id.itemTextViewUR);
            itemTextViewLL = itemView.findViewById(R.id.itemTextViewLL);
            itemTextViewLR = itemView.findViewById(R.id.itemTextViewLR);
            imageView = itemView.findViewById(R.id.imageView);
            this.onArrowClickListener = onArrowClickListener;

            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Stock stock = items.get(position);
            onArrowClickListener.onArrowClick(stock);
        }
    }

    public interface OnArrowClick {
        void onArrowClick(Stock stock);
    }
}
