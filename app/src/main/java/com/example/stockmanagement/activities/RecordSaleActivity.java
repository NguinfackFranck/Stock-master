package com.example.stockmanagement.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stockmanagement.R;
import com.example.stockmanagement.activities.productActivities.ProductListActivity;
import com.example.stockmanagement.entities.Product;

import com.example.stockmanagement.utils.CurrencyUtils;
import com.example.stockmanagement.viewModels.VMProductViewModel;
import com.example.stockmanagement.viewModels.VMStockViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pulls the product list from VMProductViewModel (read-only, for the picker) and
 * records the sale through VMStockViewModel.recordSale(productId, quantity).
 */
public class RecordSaleActivity extends AppCompatActivity {

    public static final String INITIAL_QUANTITY = "1";
    private VMStockViewModel stockViewModel;

    private Spinner spinnerProduct;
    private EditText editQuantity;
    private TextView textUnitPrice;
    private TextView textRemainingStock;
    private TextView textTotalPrice;

    private List<Product> currentProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sale);

        ViewModelProvider.Factory productFactory = ((ProductListActivity.HasProductViewModelFactory) getApplication())
                .getProductViewModelFactory();
        VMProductViewModel productViewModel = new ViewModelProvider(this, productFactory).get(VMProductViewModel.class);

        ViewModelProvider.Factory stockFactory = ((StockActivity.HasStockViewModelFactory) getApplication())
                .getStockViewModelFactory();
        stockViewModel = new ViewModelProvider(this, stockFactory).get(VMStockViewModel.class);

        spinnerProduct = findViewById(R.id.spinner_product);
        editQuantity = findViewById(R.id.edit_sale_quantity);
        textUnitPrice = findViewById(R.id.text_unit_price);
        textRemainingStock = findViewById(R.id.text_remaining_stock);
        textTotalPrice = findViewById(R.id.text_total_price);
        Button buttonRecordSale = findViewById(R.id.button_record_sale);

        productViewModel.products.observe(this, this::populateProductSpinner);

        editQuantity.setText(INITIAL_QUANTITY);
        updateTotalPrice();

        editQuantity.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                updateTotalPrice();
            }
        });

        spinnerProduct.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateUnitPrice();
                updateTotalPrice();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        buttonRecordSale.setOnClickListener(v -> recordSale());

        stockViewModel.getSaleSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, R.string.sale_recorded, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        stockViewModel.getSaleError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                buttonRecordSale.setEnabled(true);
            }
        });
    }

    private void populateProductSpinner(List<Product> products) {
        currentProducts = products != null ? products : new ArrayList<>();

        List<String> names = new ArrayList<>();
        for (Product p : currentProducts) {
            names.add(p.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(spinnerAdapter);

        updateUnitPrice();
        updateTotalPrice();
    }

    private Product getSelectedProduct() {
        int position = spinnerProduct.getSelectedItemPosition();
        if (position < 0 || position >= currentProducts.size()) {
            return null;
        }
        return currentProducts.get(position);
    }

    private void updateUnitPrice() {
        Product selected = getSelectedProduct();
        if (selected != null && selected.getPrice() != null) {
            textUnitPrice.setText(getString(R.string.unit_price_format,
                    CurrencyUtils.formatCurrency(this, selected.getPrice())));

            // Fetch and show remaining stock
            long productId = selected.getId();
            new Thread(() -> {
                int stock = stockViewModel.getStockQuantity(productId);
                runOnUiThread(() -> {
                    textRemainingStock.setText(getString(R.string.remaining_stock_format, stock));
                });
            }).start();
        } else {
            textUnitPrice.setText(R.string.price_not_set);
            textRemainingStock.setText("");
        }
    }

    private void updateTotalPrice() {
        Product selected = getSelectedProduct();
        int quantity = parseQuantityOrZero();

        if (selected != null && selected.getPrice() != null) {
            double total = selected.getPrice() * quantity;
            textTotalPrice.setText(getString(R.string.total_price_format, CurrencyUtils.formatCurrency(this, total)));
        } else {
            textTotalPrice.setText(R.string.price_not_set);
        }
    }

    private int parseQuantityOrZero() {
        String text = editQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void recordSale() {
        Product selected = getSelectedProduct();
        if (selected == null) {
            Toast.makeText(this, R.string.error_select_product, Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = parseQuantityOrZero();
        if (quantity <= 0) {
            editQuantity.setError(getString(R.string.error_invalid_quantity));
            return;
        }

        findViewById(R.id.button_record_sale).setEnabled(false);
        stockViewModel.recordSale(selected.getId(), quantity);
    }
}