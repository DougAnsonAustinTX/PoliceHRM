package com.arm.sensinode.gateway;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public class ScheduleReceiver extends BroadcastReceiver {
    private static final String TAG = "ScheduleReceiver";
    // Restart service every 60 seconds
    private static final long REPEAT_TIME = 1000 * 60;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get preferences
        final SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        if (preferences.getBoolean("service_enabled", false)) {
            AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, StartServiceReceiver.class);
            PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar cal = Calendar.getInstance();
            // Start 30 seconds after boot completed
            cal.add(Calendar.SECOND, 30);
            //
            // Fetch every 60 seconds
            // InexactRepeating allows Android to optimize the energy consumption
            service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);

            // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            // REPEAT_TIME, pending);
        }
        else {
            Log.w(TAG, "Not starting Sensinode Service, disabled in preferences.");
        }
    }
}