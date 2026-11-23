package com.example.stockmanagement.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmanagement.R;
import com.example.stockmanagement.pojos.DailySummary;
import com.example.stockmanagement.pojos.TopSellingProduct;
import com.example.stockmanagement.utils.CurrencyUtils;
import com.example.stockmanagement.viewModels.VMReportViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ReportActivity extends AppCompatActivity {

    private VMReportViewModel viewModel;

    private TextView textDateRange;
    private TextView textRevenueThisWeek;
    private TextView textProfitThisWeek;
    private DailySummaryAdapter dailySummaryAdapter;
    private TopProductAdapter topProductAdapter;

    private final Calendar fromCalendar = Calendar.getInstance();
    private final Calendar toCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ViewModelProvider.Factory factory = ((HasReportViewModelFactory) getApplication())
                .getReportViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(VMReportViewModel.class);

        // Default range: last 7 days.
        fromCalendar.add(Calendar.DAY_OF_MONTH, -7);

        textDateRange = findViewById(R.id.text_date_range);
        textRevenueThisWeek = findViewById(R.id.text_revenue_this_week);
        textProfitThisWeek = findViewById(R.id.text_profit_this_week);
        Button buttonPickFrom = findViewById(R.id.button_pick_from);
        Button buttonPickTo = findViewById(R.id.button_pick_to);
        Button buttonExport = findViewById(R.id.button_export_csv);

        RecyclerView recyclerDailySummary = findViewById(R.id.recycler_daily_summary);
        recyclerDailySummary.setLayoutManager(new LinearLayoutManager(this));
        dailySummaryAdapter = new DailySummaryAdapter();
        recyclerDailySummary.setAdapter(dailySummaryAdapter);

        RecyclerView recyclerTopProducts = findViewById(R.id.recycler_top_products);
        recyclerTopProducts.setLayoutManager(new LinearLayoutManager(this));
        topProductAdapter = new TopProductAdapter();
        recyclerTopProducts.setAdapter(topProductAdapter);

        buttonPickFrom.setOnClickListener(v -> showDatePicker(fromCalendar));
        buttonPickTo.setOnClickListener(v -> showDatePicker(toCalendar));
        buttonExport.setOnClickListener(v -> viewModel.exportCsv());

        observeViewModel();
        updateDateRangeLabel();
        loadReport();
    }

    private void observeViewModel() {
        viewModel.getDailySummary().observe(this, dailySummaryAdapter::submitList);
        viewModel.getTopProducts().observe(this, topProductAdapter::submitList);

        viewModel.getRevenueThisWeek().observe(this, revenue -> {
            textRevenueThisWeek.setText(getString(R.string.revenue_this_week_format,
                    CurrencyUtils.formatCurrency(this, revenue)));
        });

        viewModel.getProfitThisWeek().observe(this, profit -> {
            textProfitThisWeek.setText(getString(R.string.net_profit_this_week_format,
                    CurrencyUtils.formatCurrency(this, profit)));
        });

        viewModel.getIsLoading().observe(this, loading ->
                findViewById(R.id.progress_report).setVisibility(
                        Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));

        viewModel.getExportedCsvUri().observe(this, this::shareExportedCsv);
    }

    private void shareExportedCsv(Uri uri) {
        if (uri == null) return;

        Toast.makeText(this, R.string.csv_exported, Toast.LENGTH_SHORT).show();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_report)));
    }

    private void showDatePicker(Calendar target) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            target.set(year, month, dayOfMonth);
            updateDateRangeLabel();
            loadReport();
        }, target.get(Calendar.YEAR), target.get(Calendar.MONTH), target.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDateRangeLabel() {
        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        textDateRange.setText(getString(R.string.date_range_format,
                format.format(fromCalendar.getTime()), format.format(toCalendar.getTime())));
    }

    private void loadReport() {
        viewModel.loadReport(fromCalendar.getTime(), toCalendar.getTime());
    }

    public interface HasReportViewModelFactory {
        ViewModelProvider.Factory getReportViewModelFactory();
    }

    static class DailySummaryAdapter extends ListAdapter<DailySummary, DailySummaryAdapter.VH> {

        DailySummaryAdapter() {
            super(DIFF_CALLBACK);
        }

        private static final DiffUtil.ItemCallback<DailySummary> DIFF_CALLBACK =
                new DiffUtil.ItemCallback<DailySummary>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull DailySummary oldItem, @NonNull DailySummary newItem) {
                        return Objects.equals(oldItem.getDate(), newItem.getDate());
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull DailySummary oldItem, @NonNull DailySummary newItem) {
                        return oldItem.getTotalQuantity() == newItem.getTotalQuantity()
                                && oldItem.getTotalRevenue() == newItem.getTotalRevenue()
                                && oldItem.getTotalProfit() == newItem.getTotalProfit();
                    }
                };

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_daily_summary, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            DailySummary item = getItem(position);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm", Locale.getDefault());

            holder.textDate.setText(item.getDate() != null ? dateFormat.format(item.getDate()) : "");

            String details = item.getUnitDetails();
            if (details == null || details.isEmpty()) {
                details = String.valueOf(item.getTotalQuantity());
            }
            holder.textQuantity.setText(details);

            var context = holder.itemView.getContext();
            holder.textRevenue.setText(CurrencyUtils.formatCurrency(context, item.getTotalRevenue()));
            holder.textProfit.setText(context.getString(R.string.net_profit_format,
                    CurrencyUtils.formatCurrency(context, item.getTotalProfit())));
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView textDate;
            final TextView textQuantity;
            final TextView textRevenue;
            final TextView textProfit;

            VH(@NonNull View itemView) {
                super(itemView);
                textDate = itemView.findViewById(R.id.text_summary_date);
                textQuantity = itemView.findViewById(R.id.text_summary_quantity);
                textRevenue = itemView.findViewById(R.id.text_summary_revenue);
                textProfit = itemView.findViewById(R.id.text_summary_profit);
            }
        }
    }

    static class TopProductAdapter extends ListAdapter<TopSellingProduct, TopProductAdapter.VH> {

        TopProductAdapter() {
            super(DIFF_CALLBACK);
        }

        private static final DiffUtil.ItemCallback<TopSellingProduct> DIFF_CALLBACK =
                new DiffUtil.ItemCallback<TopSellingProduct>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull TopSellingProduct oldItem, @NonNull TopSellingProduct newItem) {
                        return Objects.equals(oldItem.getName(), newItem.getName());
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull TopSellingProduct oldItem, @NonNull TopSellingProduct newItem) {
                        return oldItem.getTotal_sold() == newItem.getTotal_sold()
                                && oldItem.getTotal_profit() == newItem.getTotal_profit();
                    }
                };

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_top_product, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            TopSellingProduct item = getItem(position);
            holder.textName.setText(item.getName());
            String unit = item.getUnit() != null ? item.getUnit() : holder.itemView.getContext().getString(R.string.hint_unit);
            holder.textTotalSold.setText(String.format(Locale.getDefault(), "%d %s", item.getTotal_sold(), unit));

            var context = holder.itemView.getContext();
            holder.textProfit.setText(String.format(Locale.getDefault(), "+%s", CurrencyUtils.formatCurrency(context, item.getTotal_profit())));
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView textName;
            final TextView textTotalSold;
            final TextView textProfit;

            VH(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.text_top_product_name);
                textTotalSold = itemView.findViewById(R.id.text_top_product_sold);
                textProfit = itemView.findViewById(R.id.text_top_product_profit);
            }
        }
    }
}
