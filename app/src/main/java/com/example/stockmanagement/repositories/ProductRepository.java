package com.example.stockmanagement.repositories;

import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.stockmanagement.database.ProductDao;
import com.example.stockmanagement.database.StockItemDao;
import com.example.stockmanagement.database.SyncLogDao;
import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.entities.StockItem;
import com.example.stockmanagement.entities.SyncLog;
import com.example.stockmanagement.enums.Action;
import com.example.stockmanagement.enums.ProductSortColumn;
import com.example.stockmanagement.exceptions.DuplicateException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Repository responsible for Product-related data operations.
 * Handles duplicate checking, product creation with initial stock, and sync logging.
 */
public class ProductRepository {
    private final ProductDao productDao;
    private final StockItemDao stockItemDao;


    public ProductRepository(ProductDao productDao, StockItemDao stockItemDao, SyncLogDao syncLogDao) {
        this.productDao = productDao;
        this.stockItemDao = stockItemDao;
        this.syncLogDao = syncLogDao;


    }

    private final ExecutorService executorService= Executors.newSingleThreadExecutor();
    private final SyncLogDao syncLogDao;
    private static final String TABLE_PRODUCTS = Product.TABLE_NAME;

    /**
     * Inserts a new product into the database and creates its initial stock entry.
     * @param product The product entity to insert.
     * @param initialQuantity The starting stock level.
     * @throws DuplicateException If a product with the same name already exists.
     */
    public void insertProduct(Product product,int initialQuantity) {
        // Step 1: Check for duplicate name before any write
        if (productDao.countByName(product.getName()) > 0) {
            throw new DuplicateException(product.getName());}
        // Step 2: Insert the product — Room returns the generated ID
        try {
           var id = productDao.insertProduct(product);
            product.setId(id);
            // Step 3: Log the INSERT action for future backend sync
            logSync(id, Action.INSERT);
            // Step 4: Create the corresponding StockItem with initial quantity
            var stockItem = new StockItem();
            stockItem.setProduct_id(id);
            stockItem.setQuantity(initialQuantity);
            stockItem.setLast_updated(new Date());
            stockItemDao.insertStockItem(stockItem);
            // Step 5: Log the stock item creation for sync
            logSyncStock(id);
        } catch (SQLiteConstraintException e) {
            throw new DuplicateException(product.getName());
        }
    }

    /**
     * Updates an existing product's details.
     * @param product The updated product entity.
     * @throws DuplicateException If the new name conflicts with another existing product.
     */
    public void updateProduct(Product product){
        if (productDao.countByNameExcludingId(product.getName(), product.getId()) > 0) {
            throw new DuplicateException(product.getName());
        }
        productDao.updateProduct(product);
        logSync(product.getId(), Action.UPDATE);
    }
    private void logSync(Long recordId, Action action) {
        SyncLog syncLog = new SyncLog();
        syncLog.setTable_name(TABLE_PRODUCTS);
        syncLog.setAction(action);
        syncLog.setRecord_id(recordId);
        syncLogDao.insertSyncLog(syncLog);
    }

    private void logSyncStock(Long recordId) {
        var syncLog = new SyncLog();
        syncLog.setTable_name(TABLE_PRODUCTS);
        syncLog.setAction(Action.INSERT);
        syncLog.setRecord_id(recordId);
        syncLogDao.insertSyncLog(syncLog);
    }

    public void deleteProduct(Product product){
        productDao.deleteProduct(product);
        logSync(product.getId(), Action.DELETE);
    }


    public LiveData<List<Product>> searchProducts(
            String searchQuery,
            ProductSortColumn column,
            boolean isAscending) {

        String direction = isAscending ? "ASC" : "DESC";
        String sql;
        SimpleSQLiteQuery query;

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            sql = "SELECT * FROM " + Product.TABLE_NAME
                    + " ORDER BY " + column.getColumnName() + " " + direction;
            query = new SimpleSQLiteQuery(sql);
        } else {
            sql = "SELECT * FROM " + Product.TABLE_NAME
                    + " WHERE name LIKE '%' || ? || '%' "
                    + "ORDER BY " + column.getColumnName() + " " + direction;
            query = new SimpleSQLiteQuery(sql, new Object[]{searchQuery.trim()});
        }

        return productDao.getAllProductsDynamics(query);
    }
    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }

    public LiveData<Product> getProductById(long productId) {
        return productDao.getProductById(productId);
    }
}
