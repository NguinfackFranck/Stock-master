package com.example.stockmanagement.repositories;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.room.Transaction;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.stockmanagement.database.ProductDao;
import com.example.stockmanagement.database.SaleDao;
import com.example.stockmanagement.database.StockItemDao;
import com.example.stockmanagement.database.SyncLogDao;
import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.entities.Sale;
import com.example.stockmanagement.entities.StockItem;
import com.example.stockmanagement.entities.SyncLog;
import com.example.stockmanagement.enums.Action;
import com.example.stockmanagement.enums.StockSortColumn;
import com.example.stockmanagement.exceptions.InsufficientStockException;
import com.example.stockmanagement.exceptions.ProductNotFoundException;
import com.example.stockmanagement.pojos.LowStockAlert;
import com.example.stockmanagement.pojos.ProductWithStock;
import com.example.stockmanagement.pojos.StockItemDisplay;
import com.example.stockmanagement.pojos.TopSellingProduct;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ErrorManager;

import lombok.AllArgsConstructor;


public class StockAndSalesRepository {
    private final StockItemDao stockItemDao;
    private final SaleDao saleDao;
    private final SyncLogDao syncLogDao;
    private static final String TABLE_STOCK_ITEM = StockItem.TABLE_NAME;
    private static final String TABLE_SALE = Sale.TABLE_NAME;
    private final Executor executorService;
    private final ProductDao productDao;
    private final ErrorManager logger = new ErrorManager();

    public StockAndSalesRepository(StockItemDao stockItemDao,
                                   SaleDao saleDao, SyncLogDao syncLogDao,
                                   ProductDao productDao)
    {
        this.stockItemDao = stockItemDao;
        this.saleDao = saleDao;
        this.syncLogDao = syncLogDao;
        this.executorService = Executors.newSingleThreadExecutor();
        this.productDao = productDao;

    }

    public void addStock(Long productId, int quantity){
        executorService.execute(() -> {
            stockItemDao.adjustStockWithCurrentDate(productId, quantity);

            SyncLog syncLog = new SyncLog();
            syncLog.setTable_name(TABLE_STOCK_ITEM);
            syncLog.setRecord_id(productId);
            syncLog.setAction(Action.UPDATE);
            syncLogDao.insertSyncLog(syncLog);
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Transaction
    public void recordSale(long productId, int quantity) throws ProductNotFoundException, InsufficientStockException
    {if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // 1. Fetch product to validate and capture the price safely
        final var product = productDao.getProductWithID(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with ID: " + productId);
        }
        var stockQuantity = stockItemDao.getStockQuantityByProduct(productId);
        if (quantity > stockQuantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock! Requested: %d, Available: %d for product ID: %d",
                            quantity, stockQuantity, productId));}
        // Capture the current price to prevent race conditions
        var productPrice = product.getPrice();
        // 2. Deduct from stock
        stockItemDao.adjustStockWithCurrentDate(productId, -quantity);
        // 3. Record the sale
        Sale sale = new Sale();
        sale.setProduct_id(productId);
        sale.setQuantity(quantity);
        sale.setUnit_price(productPrice);
        double totalPrice = productPrice * quantity;
        sale.setTotal_price(totalPrice);
        saleDao.insertSale(sale);
        // 4. Log both changes for sync
        logSync(TABLE_STOCK_ITEM, productId, Action.UPDATE);
        logSync(TABLE_SALE, productId, Action.INSERT);}

    private void logSync(String tableName, long productId, Action action) {
        var stockLog = new SyncLog();
        stockLog.setTable_name(tableName);
        stockLog.setRecord_id(productId);
        stockLog.setAction(action);
        syncLogDao.insertSyncLog(stockLog);
    }

    public LiveData<List<LowStockAlert>> getLowStockAlerts() {
        return stockItemDao.getLowStockAlerts();
    }

    public ProductWithStock getProductWithStock(Long productId) {
        return stockItemDao.getProductWithStock(productId);
    }
    public LiveData<List<StockItem>> getAllStock() {
        return stockItemDao.getAllStock();
    }

    public LiveData<Double> getTotalStockValue() {
        return stockItemDao.getTotalStockValue();
    }

    public List<TopSellingProduct> getTopSellingProducts(Date fromDate) {
        return saleDao.getTopSellingProducts(fromDate);
    }
    public void syncLocalChanges() {
        executorService.execute(() -> {
            List<SyncLog> pendingSyncs = syncLogDao.getPendingSyncLogs();
            Date syncTime = new Date();

            for (SyncLog syncLog : pendingSyncs) {
                syncOne(syncLog);
                syncLogDao.markLogSynced(syncLog.getId(), syncTime);
            }
        });
    }

    private void syncOne(SyncLog syncLog) {
        switch (syncLog.getTable_name()) {
            case Product.TABLE_NAME:
                syncProduct(syncLog);
                break;
            case StockItem.TABLE_NAME:
                syncStockItem(syncLog);
                break;
            case Sale.TABLE_NAME:
                syncSale(syncLog);
                break;
            default:
                Log.w("Sync", "Unknown table: " + syncLog.getTable_name());
        }
    }

    private void syncProduct(SyncLog syncLog) {
        switch (syncLog.getAction()) {
            case INSERT:
            case UPDATE:
                var product = productDao.getProductBy(syncLog.getRecord_id());
                if (product != null) {
                    // send product to server...
                    productDao.markProductSynced(product.getId());
                }
                break;
            case DELETE:
                // send delete to server...
                break;
        }
    }

    private void syncStockItem(SyncLog syncLog) {
        // handle stock sync...
        stockItemDao.markStockSynced(syncLog.getRecord_id());
    }

    private void syncSale(SyncLog syncLog) {
        // handle sales sync...
        saleDao.markSaleSynced(syncLog.getRecord_id());
    }

    public LiveData<List<ProductWithStock>> getProductsWithLowStocks() {
        return stockItemDao.getProductsWithLowStock();
    }

    public LiveData<List<StockItemDisplay>> searchStockItems(
            String searchQuery,
            StockSortColumn column,
            boolean isAscending) {

        String direction = isAscending ? "ASC" : "DESC";
        String baseSql = "SELECT s.id, s.product_id, p.name as product_name, " +
                "p.category, p.unit, s.quantity, p.threshold, " +
                "s.last_updated, s.synced " +
                "FROM stock_items s " +
                "JOIN products p ON s.product_id = p.id ";

        String sql;
        SimpleSQLiteQuery query;

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            sql = baseSql + "ORDER BY " + column.getColumnName() + " " + direction;
            query = new SimpleSQLiteQuery(sql);
        } else {
            sql = baseSql + "WHERE p.name LIKE '%' || ? || '%' " +
                    "ORDER BY " + column.getColumnName() + " " + direction;
            query = new SimpleSQLiteQuery(sql, new Object[]{searchQuery.trim()});
        }

        return stockItemDao.getAllStockItemsDynamics(query);
    }

    public int getStockByProduct(Long productId) {
        return stockItemDao.getStockQuantityByProduct(productId);
    }
}