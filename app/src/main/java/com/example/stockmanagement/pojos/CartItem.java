package com.example.stockmanagement.pojos;

import com.example.stockmanagement.entities.Product;

import java.io.Serializable;

public class CartItem implements Serializable {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return (product != null && product.getPrice() != null) ? product.getPrice() : 0.0;
    }

    public double getBuyingPrice() {
        return (product != null && product.getBuyingPrice() != null) ? product.getBuyingPrice() : 0.0;
    }

    public double getSubtotal() {
        return getUnitPrice() * quantity;
    }

    public double getNetProfit() {
        return (getUnitPrice() - getBuyingPrice()) * quantity;
    }
}
