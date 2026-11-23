package com.example.stockmanagement.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.stockmanagement.entities.Sale;
import com.example.stockmanagement.pojos.DailySummary;
import com.example.stockmanagement.pojos.TopSellingProduct;

import java.util.Date;
import java.util.List;

@Dao
public interface SaleDao {
    @Insert
    void insertSale(Sale sale);

    @Query("SELECT * FROM sales " +
            "WHERE product_id = :productId " +
            "ORDER BY sale_date DESC")
    List<Sale> getSalesByProduct(int productId);

    @Query("SELECT p.name, p.unit, SUM(s.quantity) as total_sold, " +
            "SUM(COALESCE(s.net_profit, (s.unit_price - COALESCE(s.buying_price, 0.0)) * s.quantity, 0.0)) as total_profit " +
            "FROM sales s JOIN products p ON p.id = s.product_id " +
            "WHERE s.sale_date >= :fromDate GROUP BY s.product_id ORDER BY total_sold DESC")
    List<TopSellingProduct> getTopSellingProducts(Date fromDate);

    @Query("SELECT * FROM sales WHERE synced = 0")
    List<Sale> getUnsyncedSales();
    @Query("SELECT SUM(quantity * unit_price) FROM sales WHERE sale_date >= :fromDate")
    LiveData<Double> getTotalRevenue(Date fromDate);
    @Query("SELECT SUM(net_profit) FROM sales WHERE sale_date >= :fromDate")
    LiveData<Double> getTotalNetProfit(Date fromDate);
    @Query("SELECT * FROM sales WHERE transaction_id = :transactionId")
    List<Sale> getSalesByTransactionId(String transactionId);
    @Query("SELECT s.sale_date as date, " +
            "SUM(s.quantity) as totalQuantity, " +
            "SUM(s.quantity * s.unit_price) as totalRevenue, " +
            "SUM(COALESCE(s.net_profit, (s.unit_price - COALESCE(s.buying_price, 0.0)) * s.quantity, 0.0)) as totalProfit, " +
            "(SELECT GROUP_CONCAT(qty_unit_name) FROM (" +
            "  SELECT SUM(s2.quantity) || ' ' || p2.unit || ' of ' || p2.name as qty_unit_name " +
            "  FROM sales s2 JOIN products p2 ON s2.product_id = p2.id " +
            "  WHERE s2.sale_date = s.sale_date GROUP BY p2.id" +
            ")) as unitDetails " +
            "FROM sales s " +
            "WHERE s.sale_date BETWEEN :from AND :to " +
            "GROUP BY s.sale_date " +
            "ORDER BY s.sale_date ASC")
    List<DailySummary> getDailySummary(Date from, Date to);

    @Query("UPDATE sales SET synced = 1 WHERE id = :saleId")
    void markSaleSynced(Long saleId);
}

