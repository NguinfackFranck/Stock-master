package com.example.stockmanagement.pojos;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.example.stockmanagement.entities.Product;

import java.util.Date;


public class ProductWithStock {
    @Embedded
    private Product product;

    @ColumnInfo(name = "quantity")
    private Integer quantity;

    @ColumnInfo(name = "last_updated")
    private Date lastUpdated;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
