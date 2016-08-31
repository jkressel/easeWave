package com.jonathansautter.easewave;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            SharedPreferences reminderprefs = context.getSharedPreferences("REMINDER", 0);
            Calendar cal0 = Calendar.getInstance();

            int hour = cal0.get(Calendar.HOUR_OF_DAY);
            int min = cal0.get(Calendar.MINUTE);
            boolean storedPreference1 = reminderprefs.getBoolean("r1enabled", false);
            boolean storedPreference2 = reminderprefs.getBoolean("r2enabled", false);
            boolean storedPreference3 = reminderprefs.getBoolean("r3enabled", false);
            //Log.d("easeWave", "r1: " + storedPreference1 + " r2: " + storedPreference2 + " r3: " + storedPreference3);

            if (reminderprefs.getBoolean("persistentNotification", false)) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, min);
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent alarmintent = new Intent(context, AlarmReceiver.class);
                alarmintent.putExtra("AlarmNum", 100);
                PendingIntent sender = PendingIntent.getBroadcast(context, 444,
                        alarmintent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
                am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
            }

            if (storedPreference1) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(reminderprefs.getString("r1hour", "")));
                cal.set(Calendar.MINUTE, Integer.parseInt(reminderprefs.getString("r1minute", "")));
                //Log.d("easeWave", "hour: "+cal.get(Calendar.HOUR_OF_DAY)+" min: "+cal.get(Calendar.MINUTE));
                int id = 111;
                Intent alarmintent = new Intent(context, AlarmReceiver.class);
                alarmintent.putExtra("title", "freshen up your brain!");
                alarmintent.putExtra("note", "easeWave");
                alarmintent.putExtra("AlarmNum", 1);
                PendingIntent sender = PendingIntent.getBroadcast(context, id,
                        alarmintent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if (cal.getTimeInMillis() > cal0.getTimeInMillis()) {
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
                    //Log.d("easeWave", "start today");
                } else {
                    //Log.d("easeWave", "start tomorrow");
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    cal.set(Calendar.DATE, day);
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
                }
            }

            if (storedPreference2) {
                Calendar cal2 = Calendar.getInstance();
                cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(reminderprefs.getString("r2hour", "")));
                cal2.set(Calendar.MINUTE, Integer.parseInt(reminderprefs.getString("r2minute", "")));
                //Log.d("easeWave", "hour: " + cal2.get(Calendar.HOUR_OF_DAY) + " min: " + cal2.get(Calendar.MINUTE));
                int id2 = 222;
                Intent alarmintent2 = new Intent(context, AlarmReceiver.class);
                alarmintent2.putExtra("title", "freshen up your brain!");
                alarmintent2.putExtra("note", "easeWave");
                alarmintent2.putExtra("AlarmNum", 2);
                PendingIntent sender2 = PendingIntent.getBroadcast(context, id2,
                        alarmintent2, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
                AlarmManager am2 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if (cal2.getTimeInMillis() > cal2.getTimeInMillis()) {
                    am2.setRepeating(AlarmManager.RTC_WAKEUP, cal2.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender2);
                    //Log.d("easeWave", "start today");
                } else {
                    //Log.d("easeWave", "start tomorrow");
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    cal2.set(Calendar.DATE, day);
                    am2.setRepeating(AlarmManager.RTC_WAKEUP, cal2.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender2);
                }
            }

            if (storedPreference3) {
                Calendar cal3 = Calendar.getInstance();
                cal3.set(Calendar.HOUR_OF_DAY, Integer.parseInt(reminderprefs.getString("r3hour", "")));
                cal3.set(Calendar.MINUTE, Integer.parseInt(reminderprefs.getString("r3minute", "")));
                int id3 = 333;
                Intent alarmintent3 = new Intent(context, AlarmReceiver.class);
                alarmintent3.putExtra("title", "freshen up your brain!");
                alarmintent3.putExtra("note", "easeWave");
                alarmintent3.putExtra("AlarmNum", 3);
                PendingIntent sender3 = PendingIntent.getBroadcast(context, id3,
                        alarmintent3, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
                AlarmManager am3 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if (cal3.getTimeInMillis() > cal3.getTimeInMillis()) {
                    am3.setRepeating(AlarmManager.RTC_WAKEUP, cal3.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender3);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    cal3.set(Calendar.DATE, day);
                    am3.setRepeating(AlarmManager.RTC_WAKEUP, cal3.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender3);
                }
            }
        }
    }
}
