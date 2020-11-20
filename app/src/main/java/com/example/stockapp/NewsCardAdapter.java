package com.example.stockapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class NewsCardAdapter extends RecyclerView.Adapter<NewsCardAdapter.NewsViewHolder> {
    private ArrayList<JSONObject> newsItems;
    private Context context;

    public NewsCardAdapter(Context context, ArrayList<JSONObject> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
    }

    @NonNull
    @NotNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.news_card_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NewsViewHolder holder, int position) {
        JSONObject item = newsItems.get(position);
        try {
            holder.textViewNewsSource.setText(item.getJSONObject("source").getString("name"));
            holder.textViewNewsTitle.setText(item.getString("title"));
            holder.textViewNewsDate.setText(DetailActivity.calTimeDiff(item.getString("publishedAt")));
            Glide.with(context)
                    .load(item.getString("urlToImage"))
                    .centerCrop()
                    .into(holder.imageViewNews);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNewsSource, textViewNewsDate, textViewNewsTitle;
        ImageView imageViewNews;
        public NewsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            textViewNewsSource = itemView.findViewById(R.id.textViewNewsSource);
            textViewNewsDate = itemView.findViewById(R.id.textViewNewsDate);
            textViewNewsTitle = itemView.findViewById(R.id.textViewNewsTitle);
            imageViewNews = itemView.findViewById(R.id.imageViewNews);
        }
    }

}
