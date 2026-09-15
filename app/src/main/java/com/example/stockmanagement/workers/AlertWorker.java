package com.example.stockmanagement.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.stockmanagement.R;
import com.example.stockmanagement.database.AppDatabase;
import com.example.stockmanagement.pojos.LowStockAlert;

import java.util.List;

/**
 * Background worker that checks for products with low stock.
 * If such products are found, it triggers a system notification to alert the user.
 */
public class AlertWorker extends Worker {
    private static final String CHANNEL_ID = "low_stock_alerts";
    private static final int NOTIFICATION_ID = 101;

    public AlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Executes the background work. 
     * Queries the database for low-stock alerts and sends a notification if needed.
     * @return Result.success() always, as the task is non-critical and shouldn't retry indefinitely.
     */
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        List<LowStockAlert> lowStockItems = db.stockItemDao().getLowStockAlertsSync();

        if (lowStockItems != null && !lowStockItems.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (LowStockAlert item : lowStockItems) {
                message.append(item.getName())
                       .append(": ")
                       .append(item.getQuantity())
                       .append(" left (threshold ")
                       .append(item.getThreshold())
                       .append(")\n");
            }
            sendNotification(context, message.toString().trim());
        }

        return Result.success();
    }

    private void sendNotification(Context context, String content) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Low Stock Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(context.getString(R.string.screen_title_low_stock))
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
