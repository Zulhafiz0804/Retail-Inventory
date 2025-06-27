package com.example.inventoryapps;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    private List<String> recentList;
    private Context context;

    public RecentSearchAdapter(Context context, List<String> recentList) {
        this.context = context;
        this.recentList = recentList;
    }

    @Override
    public RecentSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecentSearchAdapter.ViewHolder holder, int position) {
        String productId = recentList.get(position);
        holder.tvRecentCode.setText(productId);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("productId", productId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecentCode;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRecentCode = itemView.findViewById(R.id.tvRecentCode);
        }
    }
}
