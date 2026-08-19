package com.example.stockmanagement.pojos;

import androidx.room.ColumnInfo;


public class StockItemDisplay {
    @ColumnInfo(name = "id")
    public long stockId;

    @ColumnInfo(name = "product_id")
    public long productId;

    @ColumnInfo(name = "product_name")
    public String productName;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "unit")
    public String unit;

    @ColumnInfo(name = "quantity")
    public int quantity;

    @ColumnInfo(name = "threshold")
    public int threshold;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated;

    @ColumnInfo(name = "synced")
    public boolean synced;

    public long getStockId() {
        return stockId;
    }

    public void setStockId(long stockId) {
        this.stockId = stockId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }
}
