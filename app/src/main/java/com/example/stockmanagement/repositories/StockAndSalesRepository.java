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
import com.example.stockmanagement.pojos.CartItem;
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


/**
 * Repository handling stock adjustments and sales transactions.
 * It coordinates multiple DAOs and manages background task execution.
 */
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

    /**
     * Increases or decreases stock for a product and logs the change for sync.
     * @param productId The ID of the product.
     * @param quantity The amount to adjust (can be negative).
     */
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

    /**
     * Records a new sale in the database.
     * Performs atomic updates to both the sale record and stock inventory.
     * @param productId The product being sold.
     * @param quantity The quantity sold.
     * @throws ProductNotFoundException If product does not exist.
     * @throws InsufficientStockException If requested quantity exceeds available stock.
     */
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
        var productPrice = product.getPrice() != null ? product.getPrice() : 0.0;
        var buyingPrice = product.getBuyingPrice() != null ? product.getBuyingPrice() : 0.0;
        // 2. Deduct from stock
        stockItemDao.adjustStockWithCurrentDate(productId, -quantity);
        // 3. Record the sale
        Sale sale = new Sale();
        sale.setProduct_id(productId);
        sale.setQuantity(quantity);
        sale.setUnit_price(productPrice);
        sale.setBuying_price(buyingPrice);
        double totalPrice = productPrice * quantity;
        sale.setTotal_price(totalPrice);
        sale.setNet_profit((productPrice - buyingPrice) * quantity);
        saleDao.insertSale(sale);
        // 4. Log both changes for sync
        logSync(TABLE_STOCK_ITEM, productId, Action.UPDATE);
        logSync(TABLE_SALE, productId, Action.INSERT);
    }

    /**
     * Records a multi-product cart sale transaction.
     */
    @Transaction
    public void recordCartSale(List<CartItem> cartItems, String transactionId)
            throws ProductNotFoundException, InsufficientStockException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty");
        }

        // 1. Validate stock levels for all items first
        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            if (p == null || p.getId() == null) {
                throw new ProductNotFoundException("Invalid product in cart");
            }
            int stock = stockItemDao.getStockQuantityByProduct(p.getId());
            if (item.getQuantity() > stock) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for %s! Requested: %d, Available: %d",
                                p.getName(), item.getQuantity(), stock));
            }
        }

        // 2. Process all cart items atomically
        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            int qty = item.getQuantity();

            double unitPrice = p.getPrice() != null ? p.getPrice() : 0.0;
            double buyingPrice = p.getBuyingPrice() != null ? p.getBuyingPrice() : 0.0;
            double total = unitPrice * qty;
            double netProfit = (unitPrice - buyingPrice) * qty;

            stockItemDao.adjustStockWithCurrentDate(p.getId(), -qty);

            Sale sale = new Sale();
            sale.setProduct_id(p.getId());
            sale.setQuantity(qty);
            sale.setUnit_price(unitPrice);
            sale.setBuying_price(buyingPrice);
            sale.setTotal_price(total);
            sale.setNet_profit(netProfit);
            sale.setTransaction_id(transactionId);

            saleDao.insertSale(sale);

            logSync(TABLE_STOCK_ITEM, p.getId(), Action.UPDATE);
            logSync(TABLE_SALE, p.getId(), Action.INSERT);
        }
    }

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