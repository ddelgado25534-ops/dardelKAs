package com.darieldelgadoo.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final Context context;
    private final List<Product> productList;
    private final OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = productList.get(position);

        holder.tvSkuBadge.setText(p.getSku() + " • " + p.getCategoryName());
        holder.tvProductName.setText(p.getName());
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f / unit", p.getUnitPrice()));
        holder.tvQuantity.setText(p.getQuantity() + " pcs");

        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            String fullImageUrl = RetrofitClient.BASE_URL + p.getImagePath();
            Picasso.get()
                    .load(fullImageUrl)
                    .placeholder(R.drawable.ic_placeholder_box)
                    .error(R.drawable.ic_placeholder_box)
                    .resize(200, 200)
                    .centerCrop()
                    .into(holder.ivProductThumb);
        } else {
            holder.ivProductThumb.setImageResource(R.drawable.ic_placeholder_box);
        }

        if (p.isLowStock()) {
            holder.tvLowStockAlert.setVisibility(View.VISIBLE);
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.crimson_600));
            holder.containerItem.setBackgroundResource(R.drawable.bg_low_stock_card);
        } else {
            holder.tvLowStockAlert.setVisibility(View.GONE);
            holder.tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.emerald_600));
            holder.containerItem.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(p));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerItem;
        ImageView ivProductThumb;
        TextView tvSkuBadge, tvProductName, tvPrice, tvQuantity, tvLowStockAlert;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            containerItem = itemView.findViewById(R.id.containerItem);
            ivProductThumb = itemView.findViewById(R.id.ivProductThumb);
            tvSkuBadge = itemView.findViewById(R.id.tvSkuBadge);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvLowStockAlert = itemView.findViewById(R.id.tvLowStockAlert);
        }
    }
}