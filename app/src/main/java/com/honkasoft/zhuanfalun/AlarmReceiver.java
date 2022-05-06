package com.honkasoft.zhuanfalun;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.POWER_SERVICE;

public class AlarmReceiver extends BroadcastReceiver
{
    // Globally define a set for the media players.
    // This is because GC can randomly collect the MediaPlayer, causing it to abruptly stop playing.
    private static final Set<MediaPlayer> activePlayers = new HashSet<MediaPlayer>();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int timesFired = readTimesFired(context);
        int alarmID = intent.getExtras().getInt("alarmID", 0);

        // Loop the "timesFired" from 1 to 3.
        timesFired += 1;
        if(timesFired > 3)
        {
            timesFired = 1;
        }

        long targetMillis = timesFired == 3 ?
                PreferencesFragment.getAlarmTimeFromAlarmID(alarmID) :
                System.currentTimeMillis() + 300000;    //NOTE: Current time + 300 000 milliseconds = "after 5 minutes".

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmID, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(targetMillis, null);
        alarmManager.setAlarmClock(ac, pendingIntent);

        saveTimesFired(context, timesFired);

        Log.i("AlarmReceiver.SetAlarm", "Scheduled next alarm at " + targetMillis + ". Times fired: " + timesFired);

        // Create a wake lock to allow the app to play the target clip to the end,
        // rather than stop midway because the app went to sleep.
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AlarmReceiver.class.getSimpleName());

        // Set the wake lock to release once the clip has finished playing.
        MediaPlayer mp = MediaPlayer.create(context, R.raw.fzn15);
        activePlayers.add(mp);
        mp.setOnCompletionListener(mediaPlayer ->
        {
            mp.release();
            activePlayers.remove(mp);
            wakeLock.release();
        });
        mp.start();
        wakeLock.acquire(5*60*1000L /*5 minutes*/);
    }

    private int readTimesFired(Context context)
    {
        SharedPreferences prefs = context.getSharedPreferences("AlarmReceiver", Context.MODE_PRIVATE);

        return prefs.getInt("timesFired", 0);
    }

    public static void saveTimesFired(Context context, int timesFired)
    {
        SharedPreferences prefs = context.getSharedPreferences("AlarmReceiver", Context.MODE_PRIVATE);

        prefs.edit().putInt("timesFired", timesFired).apply();
    }
}