package com.example.stockmanagement.activities.productActivities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stockmanagement.R;
import com.example.stockmanagement.entities.Product;
import com.example.stockmanagement.exceptions.DuplicateException;
import com.example.stockmanagement.viewModels.VMProductViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

/**
 * Handles both "add new product" and "edit existing product", distinguished by
 * whether EXTRA_PRODUCT_ID was passed in. This activity does NOT do its own
 * lookup-by-id LiveData query — VMProductViewModel doesn't expose one yet.
 *
 * TODO: add something like `LiveData<Product> getProductById(long id)` to
 * VMProductViewModel/ProductRepository if you want the edit form to load
 * fresh data instead of relying on a Serializable extra (see below).
 */
public class AddProductActivity extends AppCompatActivity {


    private VMProductViewModel viewModel;

    private TextInputEditText editName;
    private TextInputEditText editCategory;
    private TextInputEditText editUnit;
    private TextInputEditText editPrice;
    private TextInputEditText editBuyingPrice;
    private TextInputEditText editThreshold;
    private TextInputEditText editInitialQuantity;

    private Product editingProduct; // null when creating a new product

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        ViewModelProvider.Factory factory = ((ProductListActivity.HasProductViewModelFactory) getApplication())
                .getProductViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(VMProductViewModel.class);

        editName = findViewById(R.id.edit_name);
        editCategory = findViewById(R.id.edit_category);
        editUnit = findViewById(R.id.edit_unit);
        editPrice = findViewById(R.id.edit_price);
        editBuyingPrice = findViewById(R.id.edit_buying_price);
        editThreshold = findViewById(R.id.edit_threshold);
        editInitialQuantity = findViewById(R.id.edit_initial_quantity);
        Button saveButton = findViewById(R.id.button_save);

        if (getIntent().hasExtra(ProductListActivity.EXTRA_PRODUCT_ID)) {
            editInitialQuantity.setVisibility(View.GONE);

            long productId = getIntent().getLongExtra(ProductListActivity.EXTRA_PRODUCT_ID, -1);
            if (productId != -1) {
                viewModel.getById(productId).observe(this, product -> {
                    if (product != null) {
                        editingProduct = product;
                        prefillForm(product);
                        setTitle(R.string.edit_product_title);
                    }
                });
            }
        } else {
            setTitle(R.string.add_product_title);
        }
        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void prefillForm(Product product) {
        editName.setText(product.getName());
        editCategory.setText(product.getCategory());
        editUnit.setText(product.getUnit());
        if (product.getPrice() != null) {
            editPrice.setText(String.valueOf(product.getPrice()));
        }
        if (product.getBuyingPrice() != null) {
            editBuyingPrice.setText(String.valueOf(product.getBuyingPrice()));
        }
        editThreshold.setText(String.valueOf(product.getThreshold()));
    }

    private void saveProduct() {
        String name = Objects.requireNonNull(editName.getText()).toString().trim();
        if (TextUtils.isEmpty(name)) {
            editName.setError(getString(R.string.error_name_required));
            return;
        }

        String category = Objects.requireNonNull(editCategory.getText()).toString().trim();
        String unit = Objects.requireNonNull(editUnit.getText()).toString().trim();

        Double price = null;
        String priceText = Objects.requireNonNull(editPrice.getText()).toString().trim();
        if (!TextUtils.isEmpty(priceText)) {
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                editPrice.setError(getString(R.string.error_invalid_price));
                return;
            }
        }

        Double buyingPrice = null;
        String buyingPriceText = Objects.requireNonNull(editBuyingPrice.getText()).toString().trim();
        if (!TextUtils.isEmpty(buyingPriceText)) {
            try {
                buyingPrice = Double.parseDouble(buyingPriceText);
            } catch (NumberFormatException e) {
                editBuyingPrice.setError(getString(R.string.error_invalid_price));
                return;
            }
        }

        int threshold = 5;
        String thresholdText = Objects.requireNonNull(editThreshold.getText()).toString().trim();
        if (!TextUtils.isEmpty(thresholdText)) {
            try {
                threshold = Integer.parseInt(thresholdText);
            } catch (NumberFormatException e) {
                editThreshold.setError(getString(R.string.error_invalid_threshold));
                return;
            }
        }

        Button saveButton = findViewById(R.id.button_save);
        saveButton.setEnabled(false); // guard against double-tap while the write is in flight

        if (editingProduct != null) {
            editingProduct.setName(name);
            editingProduct.setCategory(category);
            editingProduct.setUnit(unit);
            editingProduct.setPrice(price);
            editingProduct.setBuyingPrice(buyingPrice);
            editingProduct.setThreshold(threshold);

            viewModel.update(editingProduct, new VMProductViewModel.OnResult() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AddProductActivity.this, R.string.product_updated, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    saveButton.setEnabled(true);
                    handleSaveError(e);
                }
            });

        } else {
            int initialQuantity = 0;
            String qtyText = Objects.requireNonNull(editInitialQuantity.getText()).toString().trim();
            if (!TextUtils.isEmpty(qtyText)) {
                try {
                    initialQuantity = Integer.parseInt(qtyText);
                } catch (NumberFormatException e) {
                    editInitialQuantity.setError(getString(R.string.error_invalid_quantity));
                    saveButton.setEnabled(true);
                    return;
                }
            }

            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setUnit(unit);
            product.setPrice(price);
            product.setBuyingPrice(buyingPrice);
            product.setThreshold(threshold);

            viewModel.insert(product, initialQuantity, new VMProductViewModel.OnResult() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AddProductActivity.this, R.string.product_added, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    saveButton.setEnabled(true);
                    handleSaveError(e);
                }
            });
        }
    }

    /**
     * DuplicateException gets a targeted, actionable error right on the name
     * field; anything else falls back to a generic message so a failure is
     * never silent, even for cases we didn't specifically anticipate.
     */
    private void handleSaveError(Exception e) {
        if (e instanceof DuplicateException) {
            editName.setError(getString(R.string.error_duplicate_product_name));
            editName.requestFocus();
        } else {
            Toast.makeText(this, getString(R.string.error_generic_save_format, e.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }
}
