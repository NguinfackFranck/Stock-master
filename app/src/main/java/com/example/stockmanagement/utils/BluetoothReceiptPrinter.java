package com.example.stockmanagement.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.stockmanagement.pojos.CartItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Utility for formatting thermal receipts and printing via Bluetooth ESC/POS printers.
 */
public class BluetoothReceiptPrinter {

    // Standard SPP UUID for Bluetooth RFCOMM serial printers
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public interface PrintCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static boolean isBluetoothSupported() {
        return getBluetoothAdapter() != null;
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        return adapter != null && adapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public static List<BluetoothDevice> getPairedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (adapter != null && adapter.isEnabled()) {
            Set<BluetoothDevice> paired = adapter.getBondedDevices();
            if (paired != null) {
                devices.addAll(paired);
            }
        }
        return devices;
    }

    /**
     * Formats receipt as plain readable string (useful for previews or log output).
     */
    public static String formatReceiptText(Context context, String transactionId, List<CartItem> cartItems,
                                          double totalAmount, double totalProfit) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateStr = dateFormat.format(new Date());

        sb.append("================================\n");
        sb.append("       STOCK MANAGEMENT         \n");
        sb.append("       SALES RECEIPT            \n");
        sb.append("================================\n");
        sb.append("Date: ").append(dateStr).append("\n");
        sb.append("Txn ID: ").append(transactionId != null ? transactionId.substring(0, Math.min(8, transactionId.length())) : "N/A").append("\n");
        sb.append("--------------------------------\n");
        sb.append(String.format(Locale.getDefault(), "%-16s %3s %11s\n", "Item", "Qty", "Price"));
        sb.append("--------------------------------\n");

        for (CartItem item : cartItems) {
            String name = item.getProduct() != null ? item.getProduct().getName() : "Unknown";
            if (name.length() > 16) {
                name = name.substring(0, 16);
            }
            String line = String.format(Locale.getDefault(), "%-16s %3d %11s\n",
                    name,
                    item.getQuantity(),
                    CurrencyUtils.formatCurrency(context, item.getSubtotal()));
            sb.append(line);
        }

        sb.append("--------------------------------\n");
        sb.append("TOTAL: ").append(CurrencyUtils.formatCurrency(context, totalAmount)).append("\n");
        sb.append("NET PROFIT: ").append(CurrencyUtils.formatCurrency(context, totalProfit)).append("\n");
        sb.append("================================\n");
        sb.append("    Thank you for your business!  \n\n\n");

        return sb.toString();
    }

    /**
     * Formats receipt items into ESC/POS bytes for 58mm / 80mm thermal bluetooth printers.
     */
    public static byte[] generateEscPosBytes(Context context, String transactionId, List<CartItem> cartItems,
                                             double totalAmount, double totalProfit) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // ESC @ : Initialize printer
        out.write(new byte[]{0x1B, 0x40});

        // ESC a 1 : Align Center
        out.write(new byte[]{0x1B, 0x61, 0x01});

        // GS ! 0x11 : Double height & width for header
        out.write(new byte[]{0x1D, 0x21, 0x11});
        out.write("STOCK MANAGEMENT\n".getBytes());

        // Normal text size
        out.write(new byte[]{0x1D, 0x21, 0x00});
        out.write("OFFICIAL RECEIPT\n".getBytes());
        out.write("--------------------------------\n".getBytes());

        // ESC a 0 : Align Left
        out.write(new byte[]{0x1B, 0x61, 0x00});

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateStr = dateFormat.format(new Date());
        out.write(("Date & Time: " + dateStr + "\n").getBytes());
        if (transactionId != null) {
            String shortId = transactionId.length() > 8 ? transactionId.substring(0, 8) : transactionId;
            out.write(("Txn #: " + shortId + "\n").getBytes());
        }
        out.write("--------------------------------\n".getBytes());

        // Line Items
        for (CartItem item : cartItems) {
            String name = item.getProduct() != null ? item.getProduct().getName() : "Item";
            String subtotalStr = CurrencyUtils.formatCurrency(context, item.getSubtotal());
            String itemLine = String.format(Locale.getDefault(), "%-14s x%-2d %11s\n",
                    name.length() > 14 ? name.substring(0, 14) : name,
                    item.getQuantity(),
                    subtotalStr);
            out.write(itemLine.getBytes());
        }

        out.write("--------------------------------\n".getBytes());

        // ESC E 1 : Bold On
        out.write(new byte[]{0x1B, 0x45, 0x01});
        String totalLine = "TOTAL: " + CurrencyUtils.formatCurrency(context, totalAmount) + "\n";
        out.write(totalLine.getBytes());

        String profitLine = "NET PROFIT: " + CurrencyUtils.formatCurrency(context, totalProfit) + "\n";
        out.write(profitLine.getBytes());

        // ESC E 0 : Bold Off
        out.write(new byte[]{0x1B, 0x45, 0x00});
        out.write("--------------------------------\n".getBytes());

        // ESC a 1 : Align Center
        out.write(new byte[]{0x1B, 0x61, 0x01});
        out.write("Thank you for your visit!\n\n\n\n".getBytes());

        // Paper Cut command (GS V 66 0)
        out.write(new byte[]{0x1D, 0x56, 0x42, 0x00});

        return out.toByteArray();
    }

    /**
     * Connects to a Bluetooth device and sends thermal receipt data.
     */
    @SuppressLint("MissingPermission")
    public static void printViaBluetooth(BluetoothDevice device, byte[] data, PrintCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            BluetoothSocket socket = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                socket.connect();
                OutputStream outStream = socket.getOutputStream();
                outStream.write(data);
                outStream.flush();

                // Wait briefly for buffer transmit
                Thread.sleep(500);

                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess();
                });
            } catch (Exception e) {
                String error = e.getMessage() != null ? e.getMessage() : "Failed to connect to Bluetooth printer.";
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(error);
                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {}
                }
            }
        });
    }
}
