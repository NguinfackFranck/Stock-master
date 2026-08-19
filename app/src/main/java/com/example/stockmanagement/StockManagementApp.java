package com.example.stockmanagement;

import android.app.Application;

import androidx.lifecycle.ViewModelProvider;

import com.example.stockmanagement.activities.ReportActivity;
import com.example.stockmanagement.activities.StockActivity;
import com.example.stockmanagement.activities.productActivities.ProductListActivity;
import com.example.stockmanagement.database.AppDatabase;
import com.example.stockmanagement.repositories.ProductRepository;
import com.example.stockmanagement.repositories.ReportRepository;
import com.example.stockmanagement.repositories.StockAndSalesRepository;
import com.example.stockmanagement.viewModelFactories.AppViewModelFactory;
import com.example.stockmanagement.workers.AlertWorker;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class StockManagementApp extends Application implements StockActivity.HasStockViewModelFactory,
        ProductListActivity.HasProductViewModelFactory,
        ReportActivity.HasReportViewModelFactory {

    private ViewModelProvider.Factory factory;
    @Override
    public void onCreate() {
        super.onCreate();

        var db = AppDatabase.getInstance(this);

        var productRepository = new ProductRepository(
                db.productDao(), db.stockItemDao(),
                db.syncLogDao()
        );

        var stockAndSalesRepository = new StockAndSalesRepository(
                db.stockItemDao(),
                db.saleDao(),
                db.syncLogDao(),
                db.productDao()
        );

        var reportRepository = new ReportRepository(
                this,
                db.saleDao(),
                getExternalFilesDir(null) // where exported CSVs get written
        );

        factory = new AppViewModelFactory(
                productRepository,
                stockAndSalesRepository,
                reportRepository
        );

        scheduleLowStockAlerts();
    }

    private void scheduleLowStockAlerts() {
        PeriodicWorkRequest alertRequest =
                new PeriodicWorkRequest.Builder(AlertWorker.class, 15, TimeUnit.MINUTES)
                        .addTag("low_stock_alerts")
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "low_stock_alerts",
                ExistingPeriodicWorkPolicy.KEEP,
                alertRequest
        );
    }
    @Override
    public ViewModelProvider.Factory getReportViewModelFactory() {
        return factory;
    }

    @Override
    public ViewModelProvider.Factory getStockViewModelFactory() {
        return factory;
    }

    @Override
    public ViewModelProvider.Factory getProductViewModelFactory() {
        return factory;
    }
}
