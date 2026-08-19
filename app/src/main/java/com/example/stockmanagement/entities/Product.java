package com.example.stockmanagement.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.stockmanagement.database.TimeConverters;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = Product.TABLE_NAME,
indices = {@Index(value = {"name"},unique = true)})

@TypeConverters({TimeConverters.class})
public class Product implements Serializable {
    public static final String TABLE_NAME = "products";

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Double price;
    private String name;
    private String category;
    private String unit; // "kg", "units", "carton"
    private int threshold = 5; // low-stock reorder point
    private Date created_at;
    private int synced = 0;

    // Default constructor (REQUIRED for Room)
    public Product() {
        this.created_at = new Date();
    }

    // Constructor with all fields (replaces @AllArgsConstructor)
    public Product(Long id, Double price, String name, String category,
                   String unit, int threshold, Date created_at, int synced) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.threshold = threshold;
        this.created_at = created_at;
        this.synced = synced;
    }

    // Convenience constructor for new products (without ID)
    public Product(Double price, String name, String category, String unit, int threshold) {
        this.price = price;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.threshold = threshold;
        this.created_at = new Date();
        this.synced = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

}
