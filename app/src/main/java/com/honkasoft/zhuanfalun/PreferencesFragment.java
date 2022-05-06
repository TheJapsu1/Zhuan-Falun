package com.honkasoft.zhuanfalun;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class PreferencesFragment extends Dialog
{
    Context context;
    SharedPreferences prefs;

    public static final int alarm0600ID = 1;
    public static final int alarm1200ID = 2;
    public static final int alarm1800ID = 3;
    public static final int alarm0000ID = 4;

    public PreferencesFragment(@NonNull Context context)
    {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_preferences);
        prefs = context.getSharedPreferences("PreferencesFrag", Context.MODE_PRIVATE);

        // Find references to the settings switches.
        Switch switch0600 = findViewById(R.id.alarmSwitch0600);
        Switch switch1200 = findViewById(R.id.alarmSwitch1200);
        Switch switch1800 = findViewById(R.id.alarmSwitch1800);
        Switch switch0000 = findViewById(R.id.alarmSwitch0000);
        Button testButton = findViewById(R.id.testButton);

        // Set the switches checked when needed.
        switch0600.setChecked(prefs.getBoolean("switch0600", false));
        switch1200.setChecked(prefs.getBoolean("switch1200", false));
        switch1800.setChecked(prefs.getBoolean("switch1800", false));
        switch0000.setChecked(prefs.getBoolean("switch0000", false));

        // Set switches' listeners.
        switch0600.setOnCheckedChangeListener((compoundButton, isChecked) -> onAlarmSwitchCheckedChanged(alarm0600ID, "switch0600", isChecked));
        switch1200.setOnCheckedChangeListener((compoundButton, isChecked) -> onAlarmSwitchCheckedChanged(alarm1200ID, "switch1200", isChecked));
        switch1800.setOnCheckedChangeListener((compoundButton, isChecked) -> onAlarmSwitchCheckedChanged(alarm1800ID, "switch1800", isChecked));
        switch0000.setOnCheckedChangeListener((compoundButton, isChecked) -> onAlarmSwitchCheckedChanged(alarm0000ID, "switch0000", isChecked));

        // Set test listener
        testButton.setOnClickListener(view -> playTestSound());
    }

    private void onAlarmSwitchCheckedChanged(int alarmID, String switchID, boolean isChecked)
    {
        if(isChecked)
        {
            setAlarm(alarmID);
        }
        else {
            cancelAlarm(alarmID);
        }

        prefs.edit().putBoolean(switchID, isChecked).apply();
    }

    private void setAlarm(int targetAlarmID)
    {
        PendingIntent pendingIntent = getIntentFromAlarmID(targetAlarmID);

        long targetMillis = getAlarmTimeFromAlarmID(targetAlarmID);

        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(targetMillis, null);
        am.setAlarmClock(ac, pendingIntent);

        // Reset the times fired.
        // NOTE: This may mean that if an alarm gets set at in the middle of a "alarm set" (one alarm passed, two to go),
        // the latter two will not be raised as the targetMillis is set for the next "alarm set" start.
        AlarmReceiver.saveTimesFired(context, 0);

        Log.i("PrefFrag.SetAlarm", "Created alarm with ID " + targetAlarmID + " at " + targetMillis);
    }

    private void cancelAlarm(int targetAlarmID)
    {
        PendingIntent pendingIntent = getIntentFromAlarmID(targetAlarmID);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        Log.i("PrefFrag.CancelAlarm", "Cancelled alarm with ID " + targetAlarmID);
    }

    private void playTestSound()
    {
        MediaPlayer mp;
        mp = MediaPlayer.create(getContext(), R.raw.fzn15);
        mp.setOnCompletionListener(mediaPlayer -> mp.release());
        mp.start();
    }

    private PendingIntent getIntentFromAlarmID(int targetAlarmID)
    {
        // Assign the alarmID as an extra for easy alarm canceling.
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmID", targetAlarmID);
        return PendingIntent.getBroadcast(context, targetAlarmID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static long getAlarmTimeFromAlarmID(int alarmID)
    {
        Calendar cal = Calendar.getInstance();
        switch (alarmID)
        {
            case PreferencesFragment.alarm0600ID:{
                cal.set(Calendar.HOUR_OF_DAY, 5);
                cal.set(Calendar.MINUTE, 55);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                //cal.set(Calendar.HOUR_OF_DAY, 10);            //NOTE: Test values!
                //cal.set(Calendar.MINUTE, 10);                 //NOTE: Test values!
                //cal.set(Calendar.SECOND, 0);                  //NOTE: Test values!
                //cal.set(Calendar.MILLISECOND, 0);             //NOTE: Test values!
                break;
            }

            case PreferencesFragment.alarm1200ID:{
                cal.set(Calendar.HOUR_OF_DAY, 11);
                cal.set(Calendar.MINUTE, 55);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            }

            case PreferencesFragment.alarm1800ID:{
                cal.set(Calendar.HOUR_OF_DAY, 17);
                cal.set(Calendar.MINUTE, 55);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            }

            case PreferencesFragment.alarm0000ID:{
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            }

            default:{
                Log.e("AlarmReceiver", "Invalid TargetTime!");
                return 0;
            }
        }

        // If the target time has passed already, it means we just need to add 24 hours.
        if(cal.getTimeInMillis() < System.currentTimeMillis())
        {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return cal.getTimeInMillis();
    }
}