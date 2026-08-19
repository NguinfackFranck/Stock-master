package com.example.stockmanagement.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyUtils {
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_CURRENCY = "currency_symbol";
    private static final String DEFAULT_CURRENCY = "FCFA";

    public static String getCurrencySymbol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY);
    }

    public static void setCurrencySymbol(Context context, String symbol) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENCY, symbol).apply();
    }

    /**
     * Formats a double value as currency using the user's preferred symbol.
     * Example: "1,500 FCFA" or "$1,500"
     */
    public static String formatCurrency(Context context, Double amount) {
        if (amount == null) amount = 0.0;
        
        String symbol = getCurrencySymbol(context);
        
        // Use a standard decimal format for grouping (e.g., 1,234.56)
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.getDefault());
        
        // Most African currencies using FCFA don't use decimals in common practice,
        // but we'll keep 2 decimal places if it's not a round number, or just 0 if requested.
        // For FCFA, usually it's whole numbers.
        if (symbol.equals("FCFA")) {
            formatter.setMaximumFractionDigits(0);
        } else {
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
        }

        String formattedAmount = formatter.format(amount);
        
        // Return "Amount Symbol" (common for FCFA) or "SymbolAmount" (common for $)
        if (symbol.equals("$") || symbol.equals("£") || symbol.equals("€")) {
            return symbol + formattedAmount;
        } else {
            return formattedAmount + " " + symbol;
        }
    }
}
