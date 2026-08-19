package com.example.stockmanagement.activities.productActivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmanagement.R;
import com.example.stockmanagement.pojos.ProductWithStock;
import com.example.stockmanagement.viewModels.VMProductViewModel;

import java.util.Objects;

public class LowStockAlertActivity extends AppCompatActivity {
    private LowStockAdapter adapter;
    private View textEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_stock_alert);

        ViewModelProvider.Factory factory = ((ProductListActivity.HasProductViewModelFactory) getApplication())
                .getProductViewModelFactory();
        VMProductViewModel viewModel = new ViewModelProvider(this, factory).get(VMProductViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_low_stock);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LowStockAdapter();
        recyclerView.setAdapter(adapter);

        textEmptyState = findViewById(R.id.text_empty_state);

        viewModel.lowStockProducts().observe(this, items -> {
            adapter.submitList(items);
            boolean isEmpty = items == null || items.isEmpty();
            textEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    static class LowStockAdapter extends ListAdapter<ProductWithStock, LowStockAdapter.VH> {

        LowStockAdapter() {
            super(DIFF_CALLBACK);
        }

        private static final DiffUtil.ItemCallback<ProductWithStock> DIFF_CALLBACK =
                new DiffUtil.ItemCallback<ProductWithStock>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull ProductWithStock oldItem, @NonNull ProductWithStock newItem) {
                        return Objects.equals(oldItem.getProduct().getId(), newItem.getProduct().getId());
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull ProductWithStock oldItem, @NonNull ProductWithStock newItem) {
                        return Objects.equals(oldItem.getQuantity(), newItem.getQuantity())
                                && Objects.equals(oldItem.getLastUpdated(), newItem.getLastUpdated());
                    }
                };

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_low_stock, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ProductWithStock item = getItem(position);
            holder.textName.setText(item.getProduct().getName());
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            holder.textDetail.setText(holder.itemView.getContext().getString(
                    R.string.low_stock_detail_format, quantity, item.getProduct().getThreshold()));
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView textName;
            final TextView textDetail;

            VH(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.text_low_stock_name);
                textDetail = itemView.findViewById(R.id.text_low_stock_detail);
            }
        }
    }
}
