package com.example.stockmanagement.enums;

import lombok.Getter;


public enum ProductSortColumn {
    NAME("name"),
    PRICE("price"),
    ID("id"),
    CATEGORY("category");

    public String getColumnName() {
        return columnName;
    }

    private final String columnName;

    ProductSortColumn(String columnName) {
        this.columnName = columnName;
    }

}
