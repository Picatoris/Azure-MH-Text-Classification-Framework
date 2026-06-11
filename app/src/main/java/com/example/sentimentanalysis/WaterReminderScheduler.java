package com.example.sentimentanalysis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

public class WaterReminderScheduler {

    public static void scheduleWaterReminders(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        long now = System.currentTimeMillis();

        // Set Base Start Time: 8:00 AM Today
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // LOGIC FIX:
        // If it's already past 8 AM, don't skip to tomorrow!
        // Instead, find the NEXT 2-hour slot today.
        // e.g., if it's 9:00 AM, start at 10:00 AM.
        while (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.HOUR_OF_DAY, 2);
        }

        long interval = 2 * 60 * 60 * 1000; // 2 Hours

        // BATTERY OPTIMIZATION:
        // Use setInexactRepeating. It aligns alarms from different apps to save battery.
        // The phone will wake up roughly every 2 hours, but not at the exact second.
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                interval,
                pendingIntent
        );
    }

    // Helper to cancel alarms if the user turns off notifications
    public static void cancelReminders(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WaterReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}