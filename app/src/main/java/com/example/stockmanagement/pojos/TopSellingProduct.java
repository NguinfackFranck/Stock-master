package com.example.stockmanagement.pojos;

public class TopSellingProduct {
    private String name;
    private int total_sold;
    private String unit;
    private double total_profit;

    public int getTotal_sold() {
        return total_sold;
    }

    public void setTotal_sold(int total_sold) {
        this.total_sold = total_sold;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getTotal_profit() {
        return total_profit;
    }

    public void setTotal_profit(double total_profit) {
        this.total_profit = total_profit;
    }
}
