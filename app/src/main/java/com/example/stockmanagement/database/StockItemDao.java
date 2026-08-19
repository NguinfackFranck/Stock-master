package com.example.stockmanagement.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.stockmanagement.pojos.LowStockAlert;
import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.pojos.ProductWithStock;
import com.example.stockmanagement.entities.StockItem;
import com.example.stockmanagement.pojos.StockItemDisplay;

import java.util.Date;
import java.util.List;

@Dao
public interface StockItemDao {
    @Insert
    void insertStockItem(StockItem stockItem);

    @Update
    void updateStockItem(StockItem stockItem);

    @Query("SELECT * FROM stock_items WHERE product_id = :productId")
    StockItem getStockByProduct(Long productId);

    @Query("SELECT p.*, s.quantity, s.last_updated " +
            "FROM products p " +
            "LEFT JOIN stock_items s ON p.id = s.product_id " +
            "WHERE p.id = :productId")
    ProductWithStock getProductWithStock(Long productId);

    @Query("SELECT p.name, p.unit, s.quantity, p.threshold " +
            "FROM stock_items s " +
            "JOIN products p ON p.id = s.product_id " +
            "WHERE s.quantity <= p.threshold")
    List<LowStockAlert> getLowStockAlertsSync();

    @Query("SELECT p.name, p.unit, s.quantity, p.threshold " +
            "FROM stock_items s " +
            "JOIN products p ON p.id = s.product_id " +
            "WHERE s.quantity <= p.threshold")
    LiveData<List<LowStockAlert>> getLowStockAlerts();

    @Query("UPDATE stock_items SET quantity = quantity + :amount, last_updated = :timestamp WHERE product_id = :productId")
    void adjustStock(Long productId, int amount, Date timestamp);

    @Query("UPDATE stock_items SET quantity = quantity + :amount, last_updated = datetime('now') WHERE product_id = :productId")
    void adjustStockWithCurrentDate(Long productId, int amount);

    @Query("UPDATE stock_items SET synced = 1 WHERE product_id = :productId")
    void markStockSynced(long productId);
    @RawQuery(observedEntities = {StockItem.class, Product.class})
    LiveData<List<StockItemDisplay>> getAllStockItemsDynamics(SupportSQLiteQuery query);

    @Query("SELECT p.*, s.quantity, s.last_updated " +
            "FROM products p " +
            "INNER JOIN stock_items s ON p.id = s.product_id " +
            "WHERE s.quantity <= p.threshold")
    LiveData<List<ProductWithStock>> getProductsWithLowStock();
    @Query("SELECT * FROM stock_items")
    LiveData<List<StockItem>> getAllStock();

    @Query("SELECT SUM(p.price * s.quantity) " +
            "FROM stock_items s " +
            "JOIN products p ON p.id = s.product_id")
    LiveData<Double> getTotalStockValue();
    @Query("SELECT quantity FROM stock_items WHERE product_id = :productId")
    int getStockQuantityByProduct(long productId);
}
