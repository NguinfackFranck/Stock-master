package com.example.stockmanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmanagement.pojos.CartItem;
import com.example.stockmanagement.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    private List<CartItem> items = new ArrayList<>();
    private final OnItemRemoveListener removeListener;

    public CartAdapter(OnItemRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<CartItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item, position, removeListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textUnitPrice;
        private final TextView textProfit;
        private final TextView textSubtotal;
        private final ImageButton buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_cart_product_name);
            textUnitPrice = itemView.findViewById(R.id.text_cart_unit_price);
            textProfit = itemView.findViewById(R.id.text_cart_item_profit);
            textSubtotal = itemView.findViewById(R.id.text_cart_subtotal);
            buttonRemove = itemView.findViewById(R.id.button_remove_cart_item);
        }

        public void bind(CartItem item, int position, OnItemRemoveListener listener) {
            var context = itemView.getContext();
            String name = item.getProduct() != null ? item.getProduct().getName() : "";
            textName.setText(name);

            String unitPriceFormatted = CurrencyUtils.formatCurrency(context, item.getUnitPrice());
            String costPriceFormatted = CurrencyUtils.formatCurrency(context, item.getBuyingPrice());
            textUnitPrice.setText(String.format(java.util.Locale.getDefault(), "%dx %s (Cost: %s)", item.getQuantity(), unitPriceFormatted, costPriceFormatted));

            String profitFormatted = CurrencyUtils.formatCurrency(context, item.getNetProfit());
            textProfit.setText(context.getString(R.string.net_profit_format, profitFormatted));

            textSubtotal.setText(CurrencyUtils.formatCurrency(context, item.getSubtotal()));

            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemove(position);
                }
            });
        }
    }
}
