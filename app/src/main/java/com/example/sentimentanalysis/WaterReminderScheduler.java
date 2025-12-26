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
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long firstTrigger = calendar.getTimeInMillis();
        if (firstTrigger <= System.currentTimeMillis()) {
            firstTrigger += AlarmManager.INTERVAL_DAY; // Start tomorrow
        }

        long interval = 2 * 60 * 60 * 1000; // Every 2 hours

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstTrigger,
                interval,
                pendingIntent
        );
    }
}