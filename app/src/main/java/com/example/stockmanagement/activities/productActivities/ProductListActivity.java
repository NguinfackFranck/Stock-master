package com.example.stockmanagement.activities.productActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmanagement.ProductAdapter;
import com.example.stockmanagement.R;
import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.enums.ProductSortColumn;
import com.example.stockmanagement.viewModels.VMProductViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ProductListActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private VMProductViewModel viewModel;
    private ProductAdapter adapter;
    private View lowStockBanner;
    private TextView textLowStockBannerLabel;
    private View root; // for Snackbar anchoring

    private boolean ascending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewModelProvider.Factory factory = ((HasProductViewModelFactory) getApplication())
                .getProductViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(VMProductViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.recycler_products);
        root = recyclerView; // Snackbars need any View already attached to the window
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(
                this::openEditProduct,
                product -> {
                    deleteWithUndo(product);
                    return true;
                }
        );
        recyclerView.setAdapter(adapter);

        lowStockBanner = findViewById(R.id.text_low_stock_banner);
        textLowStockBannerLabel = findViewById(R.id.text_low_stock_banner_label);
        lowStockBanner.setOnClickListener(v ->
                startActivity(new Intent(this, LowStockAlertActivity.class)));

        findViewById(R.id.fab_add_product).setOnClickListener(v ->
                startActivity(new Intent(this, AddProductActivity.class)));

        attachSwipeToDelete(recyclerView);
        showDeleteHintOnce();
        observeViewModel();
    }

    /**
     * One-time, unobtrusive nudge so the delete gesture isn't a hidden feature.
     * Shows once per install (SharedPreferences flag), not on every visit —
     * a hint that never goes away stops being a hint and starts being noise.
     */
    private void showDeleteHintOnce() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean alreadyShown = prefs.getBoolean("delete_hint_shown", false);
        if (alreadyShown) return;

        Snackbar.make(root, R.string.hint_swipe_or_hold_to_delete, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.got_it, v -> { /* just dismiss */ })
                .show();

        prefs.edit().putBoolean("delete_hint_shown", true).apply();
    }

    private void observeViewModel() {
        viewModel.products.observe(this, products -> adapter.submitList(products));

        viewModel.lowStockProducts().observe(this, lowStock -> {
            if (lowStock == null || lowStock.isEmpty()) {
                lowStockBanner.setVisibility(View.GONE);
            } else {
                lowStockBanner.setVisibility(View.VISIBLE);
                textLowStockBannerLabel.setText(getString(R.string.low_stock_banner_format, lowStock.size()));
            }
        });
    }

    private void attachSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Product product = adapter.getCurrentList().get(position);
                deleteWithUndo(product);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    /**
     * Deletes immediately but offers Undo via Snackbar instead of an "Are you sure?"
     * dialog — matches the quickness of a swipe gesture instead of interrupting it.
     */
    private void deleteWithUndo(Product product) {
        viewModel.delete(product);

        Snackbar.make(root,
                        getString(R.string.product_deleted_format, product.getName()),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> viewModel.insert(product, 0, new VMProductViewModel.OnResult() {
                    @Override
                    public void onSuccess() {
                        // Optional: Show success
                    }

                    @Override
                    public void onError(Exception e) {
                        // Optional: Show error
                    }
                }))
                .show();
    }

    private void openEditProduct(Product product) {
        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_list, menu);

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
        if (id == R.id.action_sort_name) {
            viewModel.setSortColumn(ProductSortColumn.NAME);
            return true;
        } else if (id == R.id.action_sort_price) {
            viewModel.setSortColumn(ProductSortColumn.PRICE);
            return true;
        } else if (id == R.id.action_toggle_direction) {
            ascending = !ascending;
            viewModel.setAscending(ascending);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Implement this on your Application class to hand back the factory that knows
     * how to build VMProductViewModel (see AppViewModelFactory from earlier).
     */
    public interface HasProductViewModelFactory {
        ViewModelProvider.Factory getProductViewModelFactory();
    }
}