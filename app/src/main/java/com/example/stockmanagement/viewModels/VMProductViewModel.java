package com.example.stockmanagement.viewModels;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.repositories.ProductRepository;
import com.example.stockmanagement.enums.ProductSortColumn;
import com.example.stockmanagement.repositories.StockAndSalesRepository;
import com.example.stockmanagement.pojos.ProductWithStock;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class VMProductViewModel extends ViewModel {
    private ProductRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StockAndSalesRepository stockAndSalesRepository;

    public interface OnResult {
        void onSuccess();
        void onError(Exception e);
    }

    public VMProductViewModel(ProductRepository repository, StockAndSalesRepository stockAndSalesRepository) {
        this.repository = repository;
        this.stockAndSalesRepository = stockAndSalesRepository;
    }

    public LiveData<Product> getById(long productId) {
        return repository.getProductById(productId);
    }

    // --- Reactive Search + Sort State ---
    private static class SearchSortState {
        final String query;
        final ProductSortColumn column;
        final boolean ascending;
        SearchSortState(String query, ProductSortColumn column, boolean ascending) {
            this.query = query;
            this.column = column;
            this.ascending = ascending;
        }
    }
    // MutableLiveData holds the current state:
    private final MutableLiveData<SearchSortState> searchSortState = new MutableLiveData<>(
            new SearchSortState("", ProductSortColumn.NAME, true)
    );
    // When state changes, Transformations.switchMap re-runs the query:
    public final LiveData<List<Product>> products = Transformations.switchMap(
            searchSortState,
            state -> repository.searchProducts(state.query, state.column, state.ascending)
    );
    // UI calls these three setters — each creates a new SearchSortState:
    public void setSearchQuery(String query) {
        SearchSortState current = searchSortState.getValue();
        if (current != null) {
            searchSortState.setValue(new SearchSortState(query, current.column, current.ascending));
        }
    }

    public LiveData<List<ProductWithStock>> lowStockProducts() {
        return stockAndSalesRepository.getProductsWithLowStocks();
    }

    public void setSortColumn(ProductSortColumn column) {
        SearchSortState current = searchSortState.getValue();
        if (current != null) {
            searchSortState.setValue(new SearchSortState(current.query, column, current.ascending));
        }
    }

    public void setAscending(boolean ascending) {
        SearchSortState current = searchSortState.getValue();
        if (current != null) {
            searchSortState.setValue(new SearchSortState(current.query, current.column, ascending));
        }
    }

    // --- Write operations: errors now surface back on the main thread ---

    public void insert(Product product, int initialQuantity, OnResult callback) {
        executor.execute(() -> {
            try {
                repository.insertProduct(product, initialQuantity);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    // Executor is shut down in onCleared() to prevent memory leaks:
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
    public void update(Product product, OnResult callback) {
        executor.execute(() -> {
            try {
                repository.updateProduct(product);
                mainHandler.post(callback::onSuccess);
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void delete(Product product) {
        executor.execute(() -> repository.deleteProduct(product));
    }
}
