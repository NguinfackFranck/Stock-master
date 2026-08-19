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
        SyncLog.class}, version = 2, exportSchema = true)
@TypeConverters({TimeConverters.class,UuidConverter.class})

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
                            .addMigrations(MIGRATION_1_2)
                            .build();}}}
        return INSTANCE;
    }
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
    // Future migration: V2 -> V3 (add new columns, etc.)
    /*
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE products ADD COLUMN price REAL");
        }
    };
    */


}
