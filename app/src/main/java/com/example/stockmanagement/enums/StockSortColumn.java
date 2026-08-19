package com.example.stockmanagement.enums;


public enum StockSortColumn {
    PRODUCT_NAME("p.name"),
    QUANTITY("s.quantity"),
    LAST_UPDATED("s.last_updated");

    public String getColumnName() {
        return columnName;
    }

    private final String columnName;

    StockSortColumn(String columnName) {
        this.columnName = columnName;
    }
}
