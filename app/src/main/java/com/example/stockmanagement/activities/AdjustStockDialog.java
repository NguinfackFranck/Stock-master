package com.example.stockmanagement.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stockmanagement.R;
import com.example.stockmanagement.viewModels.VMStockViewModel;

/**
 * Lets the user add or remove stock for a single product.
 * Positive numbers add stock, negative numbers remove stock — both go through
 * VMStockViewModel.adjustStock(id, qty), so make sure your repository's
 * addStock() accepts negative deltas (or split this into two buttons if not).
 */
public class AdjustStockDialog extends DialogFragment {

    private static final String ARG_PRODUCT_ID = "arg_product_id";
    private static final String ARG_PRODUCT_NAME = "arg_product_name";
    private static final String ARG_CURRENT_QUANTITY = "arg_current_quantity";

    public static AdjustStockDialog newInstance(long productId, String productName, int currentQuantity) {
        AdjustStockDialog dialog = new AdjustStockDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_PRODUCT_ID, productId);
        args.putString(ARG_PRODUCT_NAME, productName);
        args.putInt(ARG_CURRENT_QUANTITY, currentQuantity);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        long productId = args.getLong(ARG_PRODUCT_ID);
        String productName = args.getString(ARG_PRODUCT_NAME);
        int currentQuantity = args.getInt(ARG_CURRENT_QUANTITY);

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_adjust_stock, null);

        TextView textCurrent = view.findViewById(R.id.text_current_quantity);
        EditText editDelta = view.findViewById(R.id.edit_quantity_delta);

        textCurrent.setText(getString(R.string.current_quantity_format, currentQuantity));

        VMStockViewModel viewModel = new ViewModelProvider(
                requireActivity(),
                ((StockActivity.HasStockViewModelFactory) requireActivity().getApplication())
                        .getStockViewModelFactory()
        ).get(VMStockViewModel.class);

        return new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.adjust_stock_title_format, productName))
                .setView(view)
                .setPositiveButton(R.string.apply, (dialogInterface, which) -> {
                    String deltaText = editDelta.getText().toString().trim();
                    if (TextUtils.isEmpty(deltaText)) {
                        return;
                    }
                    try {
                        int delta = Integer.parseInt(deltaText);
                        viewModel.adjustStock(productId, delta);
                        Toast.makeText(requireContext(), R.string.stock_updated, Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), R.string.error_invalid_quantity, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}
