package com.example.stockmanagement.pojos;

import java.util.Date;

public class DailySummary {

        public Date date;
        public int totalQuantity;
        public double totalRevenue;
        public String unitDetails; // e.g. "10 kg, 5 units"

        public int getTotalQuantity() {
                return totalQuantity;
        }

        public void setTotalQuantity(int totalQuantity) {
                this.totalQuantity = totalQuantity;
        }

        public Date getDate() {
                return date;
        }

        public void setDate(Date date) {
                this.date = date;
        }

        public double getTotalRevenue() {
                return totalRevenue;
        }

        public void setTotalRevenue(double totalRevenue) {
                this.totalRevenue = totalRevenue;
        }

        public String getUnitDetails() {
                return unitDetails;
        }

        public void setUnitDetails(String unitDetails) {
                this.unitDetails = unitDetails;
        }

        public DailySummary() {}

        public DailySummary(int totalQuantity, Date date, double totalRevenue, String unitDetails) {
                this.totalQuantity = totalQuantity;
                this.date = date;
                this.totalRevenue = totalRevenue;
                this.unitDetails = unitDetails;
        }
}
