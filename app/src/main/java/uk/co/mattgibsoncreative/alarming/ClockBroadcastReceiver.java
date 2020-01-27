package uk.co.mattgibsoncreative.alarming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import timber.log.Timber;

public class ClockBroadcastReceiver extends BroadcastReceiver {
    // We VERY DELIBERATELY use a normal Broadcast intent to start our service,
    // rather than a Service intent. See https://stackoverflow.com/a/25856741/300836
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("Received broadcast message.");

        /*
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        // MG September 2014 debugging.
        if (pm == null) throw new AssertionError("null return from getSystemService(Context.POWER_SERVICE)");

        int wakeLockType = PowerManager.PARTIAL_WAKE_LOCK;
        PowerManager.WakeLock wakeLock = pm.newWakeLock(wakeLockType | PowerManager.ON_AFTER_RELEASE, "Alarming:AlarmService");

        // MG September 2014 debugging.
        if (wakeLock == null) throw new AssertionError("null return from pm.newWakeLock()");

        wakeLock.acquire();
        */

        Intent alarm_service_intent = new Intent(context,  AlarmService.class);
        // alarm_service_intent.putExtra("START_TIME", System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(alarm_service_intent);
        } else {
            context.startService(alarm_service_intent);
        }
    }
}
