package com.example.stockmanagement.activities;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stockmanagement.R;
import com.example.stockmanagement.activities.productActivities.LowStockAlertActivity;
import com.example.stockmanagement.viewModels.VMProductViewModel;
import com.example.stockmanagement.viewModels.VMReportViewModel;
import com.example.stockmanagement.viewModels.VMStockViewModel;
import com.example.stockmanagement.activities.productActivities.ProductListActivity;
import com.example.stockmanagement.utils.CurrencyUtils;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * App landing screen. Pulls from three ViewModels at once (stock value,
 * this week's revenue, low-stock count) via the shared AppViewModelFactory —
 * that's exactly the case AppViewModelFactory was built for.
 */

import android.widget.Toast;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    public static final String ONBOARDING_DONE = "onboarding_done";
    public static final String APP_PREFS = "app_prefs";

    private TextView textStockValue;
    private TextView textRevenue;
    private View lowStockCard;
    private TextView textLowStockSummary;

    private VMStockViewModel stockViewModel;
    private VMProductViewModel productViewModel;
    private VMReportViewModel reportViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindViews();
        setUpViewModels();
        observeViewModels();
        setUpQuickNav();

        checkOnboarding();
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void checkOnboarding() {
        android.content.SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean(ONBOARDING_DONE, false);
        if (!onboardingDone) {
            showOnboarding();
        }
    }

    private int currentStep = 0;
    private void showOnboarding() {
        var builder = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        
        android.view.View v = getLayoutInflater().inflate(R.layout.dialog_onboarding, null);
        ImageView img = v.findViewById(R.id.onboarding_image);
        TextView title = v.findViewById(R.id.onboarding_title);
        TextView desc = v.findViewById(R.id.onboarding_description);
        MaterialButton btnNext = v.findViewById(R.id.btn_onboarding_next);
        MaterialButton btnSkip = v.findViewById(R.id.btn_onboarding_skip);

        var dialog = builder.setView(v)
                .setCancelable(false)
                .create();

        btnSkip.setOnClickListener(view -> {
            getSharedPreferences(APP_PREFS, MODE_PRIVATE)
                    .edit().putBoolean(ONBOARDING_DONE, true).apply();
            dialog.dismiss();
        });

        btnNext.setOnClickListener(view -> {
            currentStep++;
            if (currentStep == 1) {
                img.setImageResource(R.drawable.ic_stock);
                title.setText(R.string.onboarding_step2_title);
                desc.setText(R.string.onboarding_step2_desc);
            } else if (currentStep == 2) {
                img.setImageResource(R.drawable.ic_sale);
                title.setText(R.string.onboarding_step3_title);
                desc.setText(R.string.onboarding_step3_desc);
            } else if (currentStep == 3) {
                img.setImageResource(R.drawable.ic_report);
                title.setText(R.string.onboarding_step4_title);
                desc.setText(R.string.onboarding_step4_desc);
                btnNext.setText(R.string.onboarding_finish);
            } else {
                getSharedPreferences(APP_PREFS, MODE_PRIVATE)
                        .edit().putBoolean(ONBOARDING_DONE, true).apply();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // ---------------------------------------------------------------
    // View binding
    // ---------------------------------------------------------------

    private void bindViews() {
        textStockValue = findViewById(R.id.text_dashboard_stock_value);
        textRevenue = findViewById(R.id.text_dashboard_revenue);
        lowStockCard = findViewById(R.id.card_low_stock_summary);
        textLowStockSummary = findViewById(R.id.text_dashboard_low_stock);
        
        findViewById(R.id.btn_settings).setOnClickListener(v -> showCurrencyDialog());
    }

    private void showCurrencyDialog() {
        String[] currencies = {"FCFA", "$", "€", "£", "GHS", "NGN", "KES"};
        int checkedItem = 0;
        String current = CurrencyUtils.getCurrencySymbol(this);
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(current)) {
                checkedItem = i;
                break;
            }
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Select Currency")
                .setSingleChoiceItems(currencies, checkedItem, (dialog, which) -> {
                    CurrencyUtils.setCurrencySymbol(DashboardActivity.this, currencies[which]);
                    dialog.dismiss();
                    // Refresh UI
                    observeViewModels();
                    Toast.makeText(this, "Currency updated to " + currencies[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------------------------------------------------------
    // ViewModels
    // ---------------------------------------------------------------

    private void setUpViewModels() {
        Application app = getApp();

        stockViewModel = new ViewModelProvider(
                this,
                ((StockActivity.HasStockViewModelFactory) app).getStockViewModelFactory()
        ).get(VMStockViewModel.class);

        productViewModel = new ViewModelProvider(
                this,
                ((ProductListActivity.HasProductViewModelFactory) app).getProductViewModelFactory()
        ).get(VMProductViewModel.class);

        reportViewModel = new ViewModelProvider(
                this,
                ((ReportActivity.HasReportViewModelFactory) app).getReportViewModelFactory()
        ).get(VMReportViewModel.class);
    }

    @NonNull
    private Application getApp() {
        Application app = getApplication();

        if (!(app instanceof StockActivity.HasStockViewModelFactory)) {
            throw new IllegalStateException(
                    "Application must implement HasStockViewModelFactory");
        }
        if (!(app instanceof ProductListActivity.HasProductViewModelFactory)) {
            throw new IllegalStateException(
                    "Application must implement HasProductViewModelFactory");
        }
        if (!(app instanceof ReportActivity.HasReportViewModelFactory)) {
            throw new IllegalStateException(
                    "Application must implement HasReportViewModelFactory");
        }
        return app;
    }

    // ---------------------------------------------------------------
    // Observers
    // ---------------------------------------------------------------

    private void observeViewModels() {
        stockViewModel.totalStockValue.observe(this, total ->
                textStockValue.setText(CurrencyUtils.formatCurrency(this, total)));

        productViewModel.lowStockProducts().observe(this, lowStock -> {
            int count = lowStock != null ? lowStock.size() : 0;
            lowStockCard.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            textLowStockSummary.setText(count > 0
                    ? getResources().getQuantityString(R.plurals.products_need_reordering, count, count)
                    : getString(R.string.no_low_stock_products));
        });

        reportViewModel.getRevenueThisWeek().observe(this, revenue ->
                textRevenue.setText(CurrencyUtils.formatCurrency(this, revenue)));

        Calendar sevenDaysAgo = Calendar.getInstance();
        sevenDaysAgo.add(Calendar.DAY_OF_MONTH, -7);
        reportViewModel.loadReport(sevenDaysAgo.getTime(), new Date());
    }

    // ---------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------

    private void setUpQuickNav() {
        bindNavCard(R.id.nav_products, R.drawable.ic_products,
                R.string.nav_products_title, R.string.nav_products_subtitle,
                ProductListActivity.class);

        bindNavCard(R.id.nav_stock, R.drawable.ic_stock,
                R.string.nav_stock_title, R.string.nav_stock_subtitle,
                StockActivity.class);

        bindNavCard(R.id.nav_sale, R.drawable.ic_sale,
                R.string.nav_sale_title, R.string.nav_sale_subtitle,
                RecordSaleActivity.class);

        bindNavCard(R.id.nav_report, R.drawable.ic_report,
                R.string.nav_report_title, R.string.nav_report_subtitle,
                ReportActivity.class);

        lowStockCard.setOnClickListener(v -> openActivity(LowStockAlertActivity.class));
    }

    private void bindNavCard(@IdRes int includeId, @DrawableRes int iconRes,
                             @StringRes int titleRes, @StringRes int subtitleRes,
                             Class<? extends AppCompatActivity> destination) {
        View card = findViewById(includeId);
        if (card == null) {
            Log.w(TAG, "Nav card not found for id: " + includeId);
            return;
        }

        ImageView icon = card.findViewById(R.id.image_nav_icon);
        TextView title = card.findViewById(R.id.text_nav_title);
        TextView subtitle = card.findViewById(R.id.text_nav_subtitle);

        if (icon != null) icon.setImageResource(iconRes);
        if (title != null) title.setText(titleRes);
        if (subtitle != null) subtitle.setText(subtitleRes);

        card.setOnClickListener(v -> openActivity(destination));
    }

    /**
     * Launches {@code destination} on top of the Dashboard without finishing it.
     * Plain startActivity() already leaves Dashboard on the back stack (we never
     * call finish() here), so pressing Back from the destination naturally
     * returns the user to the Dashboard.
     */
    private void openActivity(Class<? extends AppCompatActivity> destination) {
        try {
            startActivity(new Intent(this, destination));
        } catch (Exception e) {
            Log.e(TAG, "Failed to open " + destination.getSimpleName(), e);
            Toast.makeText(this, "Couldn't open " + destination.getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }
}