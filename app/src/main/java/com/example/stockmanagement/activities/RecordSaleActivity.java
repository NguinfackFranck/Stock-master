package com.example.stockmanagement.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockmanagement.CartAdapter;
import com.example.stockmanagement.R;
import com.example.stockmanagement.activities.productActivities.ProductListActivity;
import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.pojos.CartItem;
import com.example.stockmanagement.utils.BluetoothReceiptPrinter;
import com.example.stockmanagement.utils.CurrencyUtils;
import com.example.stockmanagement.viewModels.VMProductViewModel;
import com.example.stockmanagement.viewModels.VMStockViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart-based Record Sale Activity allowing multi-product sales,
 * net profit calculations, and Bluetooth thermal receipt printing.
 */
public class RecordSaleActivity extends AppCompatActivity {

    public static final String INITIAL_QUANTITY = "1";
    private static final int PERMISSION_REQUEST_BLUETOOTH = 101;

    private VMStockViewModel stockViewModel;

    private Spinner spinnerProduct;
    private EditText editQuantity;
    private TextView textUnitPrice;
    private TextView textRemainingStock;
    private TextView textTotalPrice;
    private TextView textTotalProfit;
    private TextView textEmptyCart;
    private Button buttonAddToCart;
    private Button buttonCheckoutPrint;
    private Button buttonCheckoutOnly;

    private CartAdapter cartAdapter;
    private final List<CartItem> cartList = new ArrayList<>();
    private List<Product> currentProducts = new ArrayList<>();

    private String pendingTransactionId;
    private boolean shouldPrintReceiptAfterSale = false;

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
        textTotalProfit = findViewById(R.id.text_total_profit);
        textEmptyCart = findViewById(R.id.text_empty_cart);
        buttonAddToCart = findViewById(R.id.button_add_to_cart);
        buttonCheckoutPrint = findViewById(R.id.button_checkout_print);
        buttonCheckoutOnly = findViewById(R.id.button_checkout_only);

        RecyclerView recyclerCart = findViewById(R.id.recycler_cart);
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this::removeCartItem);
        recyclerCart.setAdapter(cartAdapter);

        productViewModel.products.observe(this, this::populateProductSpinner);

        editQuantity.setText(INITIAL_QUANTITY);

        spinnerProduct.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateUnitPrice();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        buttonAddToCart.setOnClickListener(v -> addToCart());
        buttonCheckoutOnly.setOnClickListener(v -> processCheckout(false));
        buttonCheckoutPrint.setOnClickListener(v -> processCheckout(true));

        stockViewModel.getSaleSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, R.string.sale_recorded, Toast.LENGTH_SHORT).show();

                if (shouldPrintReceiptAfterSale) {
                    promptBluetoothPrinterSelection();
                } else {
                    finish();
                }
            }
        });

        stockViewModel.getSaleError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                setButtonsEnabled(true);
            }
        });

        updateCartUI();
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
            String unitPriceStr = CurrencyUtils.formatCurrency(this, selected.getPrice());
            String costPriceStr = CurrencyUtils.formatCurrency(this, selected.getBuyingPrice() != null ? selected.getBuyingPrice() : 0.0);
            textUnitPrice.setText(String.format("%s · %s",
                    getString(R.string.unit_price_format, unitPriceStr),
                    getString(R.string.buying_price_format, costPriceStr)));

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

    private int parseQuantityOrZero() {
        String text = editQuantity.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void addToCart() {
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

        // Check if item already exists in cart, if so update quantity
        boolean exists = false;
        for (CartItem item : cartList) {
            if (item.getProduct() != null && item.getProduct().getId().equals(selected.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                exists = true;
                break;
            }
        }

        if (!exists) {
            cartList.add(new CartItem(selected, quantity));
        }

        editQuantity.setText(INITIAL_QUANTITY);
        Toast.makeText(this, getString(R.string.item_added_to_cart, selected.getName()), Toast.LENGTH_SHORT).show();
        updateCartUI();
    }

    private void removeCartItem(int position) {
        if (position >= 0 && position < cartList.size()) {
            cartList.remove(position);
            updateCartUI();
        }
    }

    private void updateCartUI() {
        cartAdapter.setItems(new ArrayList<>(cartList));

        if (cartList.isEmpty()) {
            textEmptyCart.setVisibility(View.VISIBLE);
        } else {
            textEmptyCart.setVisibility(View.GONE);
        }

        double totalAmount = 0.0;
        double totalProfit = 0.0;

        for (CartItem item : cartList) {
            totalAmount += item.getSubtotal();
            totalProfit += item.getNetProfit();
        }

        textTotalPrice.setText(getString(R.string.total_price_format, CurrencyUtils.formatCurrency(this, totalAmount)));
        textTotalProfit.setText(getString(R.string.net_profit_format, CurrencyUtils.formatCurrency(this, totalProfit)));
    }

    private void processCheckout(boolean printReceipt) {
        // If cart is empty but a product is selected, auto-add it to cart
        if (cartList.isEmpty()) {
            Product selected = getSelectedProduct();
            int qty = parseQuantityOrZero();
            if (selected != null && qty > 0) {
                cartList.add(new CartItem(selected, qty));
                updateCartUI();
            } else {
                Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_LONG).show();
                return;
            }
        }

        setButtonsEnabled(false);
        shouldPrintReceiptAfterSale = printReceipt;
        pendingTransactionId = UUID.randomUUID().toString();

        stockViewModel.recordCartSale(cartList, pendingTransactionId);
    }

    private void setButtonsEnabled(boolean enabled) {
        buttonAddToCart.setEnabled(enabled);
        buttonCheckoutOnly.setEnabled(enabled);
        buttonCheckoutPrint.setEnabled(enabled);
    }

    private void promptBluetoothPrinterSelection() {
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }

        List<BluetoothDevice> devices = BluetoothReceiptPrinter.getPairedDevices();
        if (devices.isEmpty()) {
            Toast.makeText(this, R.string.no_printer_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String[] deviceNames = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            @SuppressLint("MissingPermission") String name = devices.get(i).getName();
            @SuppressLint("MissingPermission") String address = devices.get(i).getAddress();
            deviceNames[i] = (name != null ? name : "Unknown Device") + "\n(" + address + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_printer)
                .setItems(deviceNames, (dialog, which) -> {
                    BluetoothDevice selectedDevice = devices.get(which);
                    printReceiptToDevice(selectedDevice);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void printReceiptToDevice(BluetoothDevice device) {
        double totalAmount = 0.0;
        double totalProfit = 0.0;
        for (CartItem item : cartList) {
            totalAmount += item.getSubtotal();
            totalProfit += item.getNetProfit();
        }

        Toast.makeText(this, R.string.printing_receipt, Toast.LENGTH_SHORT).show();

        try {
            byte[] receiptBytes = BluetoothReceiptPrinter.generateEscPosBytes(
                    this, pendingTransactionId, cartList, totalAmount, totalProfit);

            BluetoothReceiptPrinter.printViaBluetooth(device, receiptBytes, new BluetoothReceiptPrinter.PrintCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(RecordSaleActivity.this, R.string.receipt_printed_success, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(RecordSaleActivity.this, getString(R.string.error_printing, errorMessage), Toast.LENGTH_LONG).show();
                    finish();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_printing, e.getMessage()), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    PERMISSION_REQUEST_BLUETOOTH);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                    PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                promptBluetoothPrinterSelection();
            } else {
                Toast.makeText(this, "Bluetooth permission required for receipt printing", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
