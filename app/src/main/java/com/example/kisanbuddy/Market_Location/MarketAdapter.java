
package com.example.kisanbuddy.Market_Location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kisanbuddy.R;

import java.util.List;

public class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.MarketViewHolder> {
    private List<MarketInfo> markets;
    private OnMarketClickListener listener;

    public interface OnMarketClickListener {
        void onMarketClick(MarketInfo market);
    }

    public MarketAdapter(List<MarketInfo> markets, OnMarketClickListener listener) {
        this.markets = markets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MarketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.market_item, parent, false);
        return new MarketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarketViewHolder holder, int position) {
        MarketInfo market = markets.get(position);
        holder.nameTextView.setText(market.getName());
        holder.vicinityTextView.setText(market.getVicinity());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMarketClick(market);
            }
        });
    }

    @Override
    public int getItemCount() {
        return markets.size();
    }

    static class MarketViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView vicinityTextView;

        MarketViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.market_name);
            vicinityTextView = itemView.findViewById(R.id.market_vicinity);
        }
    }

    public void updateMarkets(List<MarketInfo> newMarkets) {
        this.markets = newMarkets;
        notifyDataSetChanged();
    }
}


