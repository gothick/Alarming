package uk.co.mattgibsoncreative.alarming;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompatExtras;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import timber.log.Timber;

public class AlarmService extends Service {

    private long mStartTime = 0;

    String NOTIFICATION_CHANNEL_ID = "uk.co.mattgibsoncreative.alarming.channel";

    // Binding stuff. This is a local service, so we don't need to muck about with IPC.
    public class LocalBinder extends Binder {
        AlarmService getService() {
            return AlarmService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public long getCurrentTimeMillis() {
        if (mStartTime != 0) {
            long current_time = System.currentTimeMillis() - mStartTime;
            Timber.d("Returning current time %d", current_time);
            return current_time;
        }
        else {
            Timber.e("getCurrentTimeMillis called when mStartTime was zero. " +
                    "This should probably never happen. Have we been recreated?");
            return 0;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("In onStartCommand");
        mStartTime = System.currentTimeMillis();
        return START_STICKY;
    }

    private NotificationManager mNM;

    @Override
    public void onCreate() {
        Timber.d("Alarming Service onCreate called.");
        super.onCreate();

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        showNotification();
    }

    @TargetApi(26)
    private void createNotificationChannel() {
        Timber.d("Creating notification channel");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getText(R.string.local_service_label), NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            mNM.createNotificationChannel(channel);
        }
    }

    private void showNotification() {
        Timber.d("Showing notification");
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification= new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(R.string.local_service_started, notification);
    }
}
