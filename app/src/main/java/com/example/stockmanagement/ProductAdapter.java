package com.example.stockmanagement;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmanagement.R;
import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.utils.CurrencyUtils;

import java.util.Objects;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onClick(Product product);
    }

    public interface OnProductLongClickListener {
        boolean onLongClick(Product product);
    }

    private final OnProductClickListener onClickListener;
    private final OnProductLongClickListener onLongClickListener;

    public ProductAdapter(OnProductClickListener onClickListener,
                          OnProductLongClickListener onLongClickListener) {
        super(DIFF_CALLBACK);
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Product>() {
                @Override
                public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    // Product doesn't declare @EqualsAndHashCode, so Lombok's @Getter/@Setter
                    // alone won't give it a value-based equals(). Compare fields manually.
                    return Objects.equals(oldItem.getName(), newItem.getName())
                            && Objects.equals(oldItem.getPrice(), newItem.getPrice())
                            && Objects.equals(oldItem.getCategory(), newItem.getCategory())
                            && Objects.equals(oldItem.getUnit(), newItem.getUnit())
                            && oldItem.getThreshold() == newItem.getThreshold();
                }
            };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position), onClickListener, onLongClickListener);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textCategory;
        private final TextView textPrice;
        private final TextView textUnit;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_product_name);
            textCategory = itemView.findViewById(R.id.text_product_category);
            textPrice = itemView.findViewById(R.id.text_product_price);
            textUnit = itemView.findViewById(R.id.text_product_unit);
        }

        void bind(Product product,
                  OnProductClickListener onClickListener,
                  OnProductLongClickListener onLongClickListener) {
            textName.setText(product.getName());
            textCategory.setText(product.getCategory());

            if (product.getPrice() != null) {
                textPrice.setText(CurrencyUtils.formatCurrency(itemView.getContext(), product.getPrice()));
            } else {
                textPrice.setText(R.string.price_not_set);
            }

            textUnit.setText(product.getUnit());

            itemView.setOnClickListener(v -> onClickListener.onClick(product));
            itemView.setOnLongClickListener(v -> onLongClickListener.onLongClick(product));
        }
    }
}

