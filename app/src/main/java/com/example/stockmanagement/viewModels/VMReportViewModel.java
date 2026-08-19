package com.example.stockmanagement.viewModels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stockmanagement.pojos.DailySummary;
import com.example.stockmanagement.pojos.TopSellingProduct;
import com.example.stockmanagement.repositories.ReportRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VMReportViewModel extends ViewModel {
    private final ReportRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<DailySummary>> dailySummary = new MutableLiveData<>();
    private final LiveData<Double> revenueThisWeek;
    private final MutableLiveData<List<TopSellingProduct>> topProducts = new MutableLiveData<>();
    private final MutableLiveData<Uri> exportedCsvUri = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public VMReportViewModel(ReportRepository repository) {
        this.repository = repository;
        this.revenueThisWeek = repository.getRevenueThisWeek();
    }

    public LiveData<List<DailySummary>> getDailySummary() { return dailySummary; }
    public LiveData<Double> getRevenueThisWeek() { return revenueThisWeek; }
    public LiveData<List<TopSellingProduct>> getTopProducts() { return topProducts; }
    public LiveData<Uri> getExportedCsvUri() { return exportedCsvUri; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void loadReport(Date from, Date to) {
        isLoading.postValue(true);
        executor.execute(() -> {
            dailySummary.postValue(repository.getDailySummary(from, to));
            topProducts.postValue(repository.getTopProducts(from));
            isLoading.postValue(false);
        });
    }

    public void exportCsv() {
        List<DailySummary> current = dailySummary.getValue();
        if (current == null || current.isEmpty()) return;
        executor.execute(() -> {
            Uri uri = repository.exportCsv(current);
            exportedCsvUri.postValue(uri);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}

