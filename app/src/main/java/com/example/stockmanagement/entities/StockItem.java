package com.example.stockmanagement.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.stockmanagement.database.TimeConverters;

import java.util.Date;

@Entity(
        tableName = StockItem.TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "product_id",
                onDelete = ForeignKey.CASCADE
        ))
@TypeConverters({TimeConverters.class})
public class StockItem {
    public static final String TABLE_NAME = "stock_items";
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long product_id;
    private int quantity = 0;
    private Date last_updated;
    private int synced = 0;

    public StockItem() {
        this.last_updated = new Date();
    }
    public StockItem(Long id, Long product_id, int quantity, Date last_updated, int synced) {
        this.id = id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.last_updated = last_updated;
        this.synced = synced;
    }

    // Convenience constructor for new stock items (without ID)
    public StockItem(Long product_id, int quantity) {
        this.product_id = product_id;
        this.quantity = quantity;
        this.last_updated = new Date();
        this.synced = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(Date last_updated) {
        this.last_updated = last_updated;
    }

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

}
