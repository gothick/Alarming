package uk.co.mattgibsoncreative.alarming;

import androidx.annotation.TransitionRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompatExtras;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import timber.log.Timber;

public class AlarmService extends Service {

    private long mStartTime;
    private ChimePlayer mChimePlayer;

    String NOTIFICATION_CHANNEL_ID = "uk.co.mattgibsoncreative.alarmingservice.channel";

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
        if (intent != null) {
            if (intent.getExtras() != null && intent.getExtras().containsKey("START_TIME")) {
                // We're being started for the first time from our main screen in the app.
                Timber.d("Received intent with START_TIME of %d", mStartTime);
                mStartTime = intent.getExtras().getLong("START_TIME");
            }

            mChimePlayer.playChime(R.raw.rollover4);


            // TODO: We should probably use elapesedRealtime() here instead.
            // http://sangsoonam.github.io/2017/03/01/do-not-use-curenttimemillis-for-time-interval.html
            long currentTime = System.currentTimeMillis();
            long chimesSoFar = (currentTime - mStartTime) / 10000;
            long nextChimeTime = mStartTime + ((chimesSoFar + 1) * 10000);
            Timber.d("Scheduling next alarm for %d based on start time of %d.", nextChimeTime, mStartTime);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent alarmIntent = new Intent(this, ClockBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    42,
                    alarmIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            // Sigh.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextChimeTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextChimeTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextChimeTime, pendingIntent);
            }
        }
        return START_STICKY;
    }

    private NotificationManager mNM;

    @Override
    public void onCreate() {
        Timber.d("Alarming Service onCreate called.");
        super.onCreate();
        startInForeground();

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        mChimePlayer = new ChimePlayerMultipleMediaPlayers(this);
        mChimePlayer.prepareChime(R.raw.rollover4);
    }

    public void startInForeground() {
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
        startForeground(R.string.local_service_started, notification);
    }

    @TargetApi(26)
    private void createNotificationChannel() {
        Timber.d("Creating notification channel");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getText(R.string.local_service_label), NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            mNM.createNotificationChannel(channel);
        }
    }
}
