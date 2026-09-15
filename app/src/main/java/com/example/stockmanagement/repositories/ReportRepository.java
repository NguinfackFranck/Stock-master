package com.example.stockmanagement.repositories;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.example.stockmanagement.database.SaleDao;
import com.example.stockmanagement.pojos.DailySummary;
import com.example.stockmanagement.pojos.TopSellingProduct;
import com.example.stockmanagement.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Repository dedicated to generating business intelligence reports.
 * Handles SQL aggregations for revenue tracking and CSV file generation.
 */
public class ReportRepository {
    private final Context context;
    private final SaleDao saleDao;
    private final java.io.File exportDir; // e.g. context.getExternalFilesDir(null)

    public ReportRepository(Context context, SaleDao saleDao, java.io.File exportDir) {
        this.context = context.getApplicationContext();
        this.saleDao = saleDao;
        this.exportDir = exportDir;
    }

    public List<DailySummary> getDailySummary(Date from, Date to) {
        return saleDao.getDailySummary(from, to);
    }

    public LiveData<Double> getRevenueThisWeek() {
        Date weekAgo = new Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000);
        return saleDao.getTotalRevenue(weekAgo);
    }

    public List<TopSellingProduct> getTopProducts(Date from) {
        return saleDao.getTopSellingProducts(from);
    }

    /**
     * Exports the provided summaries into a standardized CSV file.
     * Uses FileProvider to generate a secure URI for sharing with external apps.
     * @param summaries The list of daily summaries to export.
     * @return A secure Uri pointing to the generated CSV file, or null if export fails.
     */
    public Uri exportCsv(List<DailySummary> summaries) {
        try {
            var timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            var file = new java.io.File(exportDir, "Daily_Sales_Report_" + timeStamp + ".csv");
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                //  headers for the CSV
                writer.append("Date,Items Sold (Qty Unit Product),Total Revenue\n");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                for (DailySummary d : summaries) {
                    var dateStr = d.date != null ? sdf.format(d.date) : "N/A";
                    var details = d.getUnitDetails() != null ? d.getUnitDetails() : String.valueOf(d.totalQuantity);
                    var revenueStr = CurrencyUtils.formatCurrency(context, d.totalRevenue);

                    writer.append(dateStr).append(",")
                            .append("\"").append(details).append("\"").append(",") // Quote details as they contain commas
                            .append(revenueStr).append("\n");
                }
            }
            // Use of FileProvider so other apps can open it safely
            return androidx.core.content.FileProvider.getUriForFile(
                    context, "com.example.stockmanagement.fileprovider", file);
        } catch (java.io.IOException e) {
            return null;
        }
    }
}
