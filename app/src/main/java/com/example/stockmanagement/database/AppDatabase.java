package com.example.stockmanagement.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.entities.Sale;
import com.example.stockmanagement.entities.StockItem;
import com.example.stockmanagement.entities.SyncLog;

@Database(entities = {StockItem.class, Product.class, Sale.class,
        SyncLog.class}, version = 3, exportSchema = true)
@TypeConverters({TimeConverters.class,UuidConverter.class})

/**
 * Central database class using Room Persistence Library.
 * Manages the SQLite database and provides access to DAOs.
 * Includes migration logic to handle schema updates without data loss.
 */
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract StockItemDao stockItemDao();
    public abstract SaleDao saleDao();
    public abstract SyncLogDao syncLogDao();
    private static final String DATABASE_NAME = "inventory.db";
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();}}}
        return INSTANCE;
    }

    /**
     * Migration from Version 1 to 2.
     * Adds high-performance indexes to transactional columns to ensure 
     * the app remains fast as the database grows.
     */
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create indexes for better query performance
            database.execSQL("CREATE INDEX idx_products_category ON products(category)");
            database.execSQL("CREATE INDEX idx_products_synced ON products(synced)");
            database.execSQL("CREATE INDEX idx_stock_product ON stock_items(product_id)");
            database.execSQL("CREATE INDEX idx_stock_quantity ON stock_items(quantity)");
            database.execSQL("CREATE INDEX idx_sales_product_date ON sales(product_id, sale_date)");
            database.execSQL("CREATE INDEX idx_sales_synced ON sales(synced)");
            database.execSQL("CREATE INDEX idx_sync_log_table_record ON sync_log(table_name, record_id)");
        }
    };

    /**
     * Migration from Version 2 to 3.
     * Adds buying price to products, and buying_price, net_profit, and transaction_id to sales.
     */
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE products ADD COLUMN buyingPrice REAL");
            database.execSQL("ALTER TABLE sales ADD COLUMN buying_price REAL");
            database.execSQL("ALTER TABLE sales ADD COLUMN net_profit REAL");
            database.execSQL("ALTER TABLE sales ADD COLUMN transaction_id TEXT");
            database.execSQL("CREATE INDEX idx_sales_transaction_id ON sales(transaction_id)");
        }
    };


}
