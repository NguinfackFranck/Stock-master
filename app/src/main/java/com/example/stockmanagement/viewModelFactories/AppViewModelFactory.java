package com.example.stockmanagement.viewModelFactories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.stockmanagement.repositories.ProductRepository;
import com.example.stockmanagement.repositories.ReportRepository;
import com.example.stockmanagement.repositories.StockAndSalesRepository;
import com.example.stockmanagement.viewModels.VMProductViewModel;
import com.example.stockmanagement.viewModels.VMReportViewModel;
import com.example.stockmanagement.viewModels.VMStockViewModel;

public class AppViewModelFactory implements ViewModelProvider.Factory {
    private final ProductRepository productRepository;
    private final StockAndSalesRepository stockAndSalesRepository;
    private final ReportRepository reportRepository;

    public AppViewModelFactory(ProductRepository productRepository,
                               StockAndSalesRepository stockAndSalesRepository,
                               ReportRepository reportRepository) {
        this.productRepository = productRepository;
        this.stockAndSalesRepository = stockAndSalesRepository;
        this.reportRepository = reportRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(VMProductViewModel.class)) {
            return (T) new VMProductViewModel(productRepository, stockAndSalesRepository);
        }

        if (modelClass.isAssignableFrom(VMStockViewModel.class)) {
            return (T) new VMStockViewModel(stockAndSalesRepository);
        }

        if (modelClass.isAssignableFrom(VMReportViewModel.class)) {
            return (T) new VMReportViewModel(reportRepository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
