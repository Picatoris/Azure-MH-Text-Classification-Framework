package com.example.sentimentanalysis;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class WaterReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "water_reminder";
    private static final int NOTIFICATION_ID = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Create the Notification Channel (Safe to call repeatedly)
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour < 8 || hour >= 22) {
            return;
        }
        createNotificationChannel(context);

        // 2. Prepare the Intent to open the App when clicked
        Intent openApp = new Intent(context, UserDashboardActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_water_drop) // Ensure this vector exists!
                .setContentTitle("Hydration Check \uD83D\uDCA7") // Added emoji
                .setContentText("Time to drink a glass of water to stay focused!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // 4. Show Notification (With Android 13 Permission Check)
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted. We cannot show notification.
                // In a real app, you might want to log this or handle it gracefully.
                return;
            }
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        // 5. Reschedule the NEXT alarm (Important for repeating logic)
        // Ensure this class exists and sets the time for the FUTURE, not immediately.
        WaterReminderScheduler.scheduleWaterReminders(context);
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = "Water Reminders";
        String description = "Reminds you to drink water periodically";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.enableVibration(true);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}