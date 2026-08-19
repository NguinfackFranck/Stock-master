package com.example.stockmanagement.viewModels;

import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.stockmanagement.database.StockItemDao;
import com.example.stockmanagement.entities.StockItem;
import com.example.stockmanagement.enums.StockSortColumn;
import com.example.stockmanagement.exceptions.InsufficientStockException;
import com.example.stockmanagement.exceptions.ProductNotFoundException;
import com.example.stockmanagement.pojos.LowStockAlert;
import com.example.stockmanagement.pojos.ProductWithStock;
import com.example.stockmanagement.repositories.StockAndSalesRepository;
import com.example.stockmanagement.pojos.StockItemDisplay;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.RequiredArgsConstructor;


public class VMStockViewModel extends ViewModel {
    private StockAndSalesRepository repository ;
    public final LiveData<List<StockItem>> allStock;
    public final LiveData<Double> totalStockValue;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> saleError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saleSuccess = new MutableLiveData<>();

    public LiveData<String> getSaleError() {
        return saleError;
    }

    public LiveData<Boolean> getSaleSuccess() {
        return saleSuccess;
    }

    public VMStockViewModel(StockAndSalesRepository repository) {
        this.repository = repository;
        this.allStock = repository.getAllStock();
        this.totalStockValue = repository.getTotalStockValue();
    }

    public void recordSale(Long productId, int quantity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            executor.execute(() -> {
                try {
                    repository.recordSale(productId, quantity);
                    saleSuccess.postValue(true);
                } catch (ProductNotFoundException | InsufficientStockException | IllegalArgumentException e) {
                    saleError.postValue(e.getMessage());
                }
            });
        }
    }
    public int getStockQuantity(long productId) {
        // Since we need it synchronously for the UI hint and it's a simple query
        // it might be better to have a LiveData if we want it reactive,
        // but for now, the repository already has getStockByProduct
        return repository.getStockByProduct(productId);
    }
    public void adjustStock(long id, int qty) {
        executor.execute(() -> repository.addStock(id, qty));
    }

    // --- Reactive Search + Sort State ---
    private static class StockSearchSortState {
        final String query;
        final StockSortColumn column;
        final boolean ascending;

        StockSearchSortState(String query, StockSortColumn column, boolean ascending) {
            this.query = query;
            this.column = column;
            this.ascending = ascending;
        }
    }
    private final MutableLiveData<StockSearchSortState> state = new MutableLiveData<>(
            new StockSearchSortState("", StockSortColumn.PRODUCT_NAME, true)
    );

    /**
     * Observe this in your UI. Auto-updates when search text, sort column,
     * or sort direction changes. Also, auto-refreshes when stock_items or
     * products tables change.
     */
    public final LiveData<List<StockItemDisplay>> stockItems = Transformations.switchMap(
            state,
            s -> repository.searchStockItems(s.query, s.column, s.ascending)
    );
    public void setSearchQuery(String query) {
        StockSearchSortState current = state.getValue();
        if (current != null) {
            state.setValue(new StockSearchSortState(query, current.column, current.ascending));
        }
    }

    public void setSortColumn(StockSortColumn column) {
        StockSearchSortState current = state.getValue();
        if (current != null) {
            state.setValue(new StockSearchSortState(current.query, column, current.ascending));
        }
    }

    public void setAscending(boolean ascending) {
        StockSearchSortState current = state.getValue();
        if (current != null) {
            state.setValue(new StockSearchSortState(current.query, current.column, ascending));
        }
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}


