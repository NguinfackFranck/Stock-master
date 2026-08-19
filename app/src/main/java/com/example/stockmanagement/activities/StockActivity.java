package com.example.stockmanagement.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockmanagement.R;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmanagement.enums.StockSortColumn;
import com.example.stockmanagement.pojos.StockItemDisplay;
import com.example.stockmanagement.utils.CurrencyUtils;
import com.example.stockmanagement.viewModels.VMStockViewModel;

import java.util.Locale;
import java.util.Objects;

public class StockActivity extends AppCompatActivity {

    private VMStockViewModel viewModel;
    private TextView textTotalStockValue;
    private boolean ascending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        Toolbar toolbar = findViewById(R.id.toolbar);  // Add this if you have a toolbar
        setSupportActionBar(toolbar);

        ViewModelProvider.Factory factory = ((HasStockViewModelFactory) getApplication())
                .getStockViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(VMStockViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_stock);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        StockAdapter adapter = new StockAdapter(item -> {
            AdjustStockDialog dialog = AdjustStockDialog.newInstance(
                    item.getProductId(), item.getProductName(), item.getQuantity());
            dialog.show(getSupportFragmentManager(), "adjust_stock");
        });
        recyclerView.setAdapter(adapter);

        textTotalStockValue = findViewById(R.id.text_total_stock_value);

        viewModel.stockItems.observe(this, adapter::submitList);
        viewModel.totalStockValue.observe(this, total -> {
            textTotalStockValue.setText(getString(R.string.total_stock_value_format,
                    CurrencyUtils.formatCurrency(this, total)));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stock, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_product_name) {
            viewModel.setSortColumn(StockSortColumn.PRODUCT_NAME);
            return true;
        } else if (id == R.id.action_sort_quantity) {
            viewModel.setSortColumn(StockSortColumn.QUANTITY);
            return true;
        } else if (id == R.id.action_sort_last_updated) {
            viewModel.setSortColumn(StockSortColumn.LAST_UPDATED);
            return true;
        } else if (id == R.id.action_toggle_direction) {
            ascending = !ascending;
            viewModel.setAscending(ascending);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface HasStockViewModelFactory {
        ViewModelProvider.Factory getStockViewModelFactory();
    }

    /** Small inline adapter — pulled out to its own file if it grows. */
    static class StockAdapter extends ListAdapter<StockItemDisplay, StockAdapter.VH> {

        interface OnItemClick {
            void onClick(StockItemDisplay item);
        }

        private final OnItemClick onItemClick;

        StockAdapter(OnItemClick onItemClick) {
            super(DIFF_CALLBACK);
            this.onItemClick = onItemClick;
        }

        private static final DiffUtil.ItemCallback<StockItemDisplay> DIFF_CALLBACK =
                new DiffUtil.ItemCallback<StockItemDisplay>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull StockItemDisplay oldItem, @NonNull StockItemDisplay newItem) {
                        return oldItem.getStockId() == newItem.getStockId();
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull StockItemDisplay oldItem, @NonNull StockItemDisplay newItem) {
                        return oldItem.getQuantity() == newItem.getQuantity()
                                && Objects.equals(oldItem.getProductName(), newItem.getProductName())
                                && oldItem.getLastUpdated() == newItem.getLastUpdated();
                    }
                };

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stock, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            StockItemDisplay item = getItem(position);
            holder.textProductName.setText(item.getProductName());

            String unit = item.getUnit() != null ? item.getUnit() : "units";
            holder.textQuantity.setText(String.format(Locale.getDefault(), "%d %s", item.getQuantity(), unit));

            if (item.getQuantity() <= item.getThreshold()) {
                holder.textQuantity.setTextColor(
                        holder.itemView.getContext().getColor(R.color.low_stock_warning));
            } else {
                holder.textQuantity.setTextColor(
                        holder.itemView.getContext().getColor(R.color.default_text));
            }

            holder.itemView.setOnClickListener(v -> onItemClick.onClick(item));
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView textProductName;
            final TextView textQuantity;

            VH(@NonNull android.view.View itemView) {
                super(itemView);
                textProductName = itemView.findViewById(R.id.text_stock_product_name);
                textQuantity = itemView.findViewById(R.id.text_stock_quantity);
            }
        }
    }
}
