package com.example.sentimentanalysis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DailyReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UserDashboardActivity activity = new UserDashboardActivity();
        // We can't call directly, so trigger via intent
        Intent i = new Intent(context, UserDashboardActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}