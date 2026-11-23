package com.example.stockmanagement.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.stockmanagement.database.TimeConverters;

import java.util.Date;
import java.util.UUID;

@Entity(
        tableName = Sale.TABLE_NAME,
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "product_id",
                onDelete = ForeignKey.CASCADE
        )
)
@TypeConverters({TimeConverters.class})
public class Sale {
    public static final String TABLE_NAME = "sales";

    @PrimaryKey()
    @NonNull
    private UUID id;
    private Long product_id;
    private Double unit_price;
    private Double buying_price;
    private Double total_price;
    private Double net_profit;
    private int quantity;
    private Date sale_date;
    private String transaction_id;
    private int synced = 0;

    // Default constructor (REQUIRED for Room)
    public Sale() {
        this.id = UUID.randomUUID();
        this.sale_date = new Date();
    }

    // Constructor with all fields (replaces @AllArgsConstructor)
    public Sale(UUID id, Long product_id, Double unit_price, Double total_price,
                int quantity, Date sale_date, int synced) {
        this.id = id;
        this.product_id = product_id;
        this.unit_price = unit_price;
        this.total_price = total_price;
        this.quantity = quantity;
        this.sale_date = sale_date;
        this.synced = synced;
    }

    // Convenience constructor for new sales (without ID)
    public Sale(Long product_id, Double unit_price, Double total_price, int quantity) {
        this.product_id = product_id;
        this.unit_price = unit_price;
        this.total_price = total_price;
        this.quantity = quantity;
        this.sale_date = new Date();
        this.synced = 0;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public Double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(Double unit_price) {
        this.unit_price = unit_price;
    }

    public Double getBuying_price() {
        return buying_price;
    }

    public void setBuying_price(Double buying_price) {
        this.buying_price = buying_price;
    }

    public Double getNet_profit() {
        return net_profit;
    }

    public void setNet_profit(Double net_profit) {
        this.net_profit = net_profit;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public Double getTotal_price() {
        return total_price;
    }

    public void setTotal_price(Double total_price) {
        this.total_price = total_price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getSale_date() {
        return sale_date;
    }

    public void setSale_date(Date sale_date) {
        this.sale_date = sale_date;
    }

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }}