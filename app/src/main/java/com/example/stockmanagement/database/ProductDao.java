package com.example.stockmanagement.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.stockmanagement.entities.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    Long insertProduct(Product product);
    @Update
    void updateProduct(Product product);
    @Delete
    void deleteProduct(Product product);
    // Queries
    @Query("SELECT * FROM products ORDER BY name")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :productId")
   LiveData<Product> getProductById(long productId);
    @Query("SELECT * FROM products WHERE id = :productId")

    Product getProductWithID(long productId);
    @Query("SELECT * FROM products WHERE id = :productId")
    Product getProductBy(long productId);

    @Query("SELECT * FROM products WHERE category = :category")
    List<Product> getProductsByCategory(String category);

    @Query("SELECT * FROM products WHERE synced = 0")
    List<Product> getUnsyncedProducts();

    @Query("UPDATE products SET synced = 1 WHERE id = :productId")
    void markProductSynced(long productId);

    @Query("SELECT * FROM products WHERE id IN (SELECT product_id FROM stock_items WHERE quantity <= threshold)")
    LiveData<List<Product>> getLowStockProducts();
    @RawQuery(observedEntities = Product.class)
    LiveData<List<Product>> getAllProductsDynamics(SupportSQLiteQuery query);
    @Query("SELECT COUNT(*) FROM " + Product.TABLE_NAME + " WHERE name = :name")
    int countByName(String name);
    @Query("SELECT COUNT(*) FROM " + Product.TABLE_NAME + " WHERE name = :name AND id != :id")
    int countByNameExcludingId(String name, Long id);
}
