package com.jonathansautter.easewave;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    private SharedPreferences reminderprefs, settingsprefs;
    private Context ct;
    protected String language, message, today;
    private int AlarmNum;

    @Override
    public void onReceive(Context context, Intent intent) {

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        ct = context;
        AlarmNum = intent.getIntExtra("AlarmNum", 0);
        reminderprefs = context.getSharedPreferences("REMINDER", 0);
        settingsprefs = context.getSharedPreferences("SETTINGS", 0);
        today = dateFormat.format(date);

        loadQuote();

    }

    private void alarm() {

        NotificationManager manager = (NotificationManager) ct.getSystemService(Context.NOTIFICATION_SERVICE);
        int NOTIFICATION_ID = 1;
        PendingIntent contentIntent = PendingIntent.getActivity(ct, NOTIFICATION_ID, new Intent(ct, MainActivity.class), 0);

        String bigtxt = message.replaceAll("<b>", "").replaceAll("</b>", "");

        Intent shareIntent1 = new Intent();
        shareIntent1.setAction(Intent.ACTION_SEND);
        shareIntent1.putExtra(Intent.EXTRA_TEXT, bigtxt);
        shareIntent1.putExtra(Intent.EXTRA_SUBJECT, "freshen up your brain");
        shareIntent1.setType("text/plain");
        PendingIntent shareIntent = PendingIntent.getActivity(ct, 0, shareIntent1, 0);

        if (AlarmNum == 100) { // persistent Notification
            manager.cancelAll();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(ct);
            builder.setContentTitle("easeWave")
                    .setContentText("freshen up your brain!")
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(contentIntent);
            if (reminderprefs.getBoolean("persistentNotification", false)) {
                builder.setAutoCancel(false);
                builder.setOngoing(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    builder.setPriority(Notification.PRIORITY_MAX);
                }
            } else {
                builder.setAutoCancel(true);
            }

            if (!bigtxt.equals("freshen up your brain!")) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    builder.addAction(R.drawable.ic_fab_share, ct.getString(R.string.share), shareIntent);
                    Notification notification = new NotificationCompat.BigTextStyle(builder)
                            .bigText(bigtxt).build();
                    manager.notify(NOTIFICATION_ID, notification);
                } else {
                    builder.setContentText(bigtxt);
                    manager.notify(NOTIFICATION_ID, builder.build());
                }
            } else {
                manager.notify(NOTIFICATION_ID, builder.build());
            }
        }

        if (AlarmNum == 1) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            String daystr = String.valueOf(day).replace("1", "su").replace("2", "mo").replace("3", "tu").replace("4", "we").replace("5", "th").replace("6", "fr").replace("7", "sa");
            if (reminderprefs.getBoolean("r1" + daystr, true)) {
                manager.cancelAll();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ct);
                builder.setContentTitle("easeWave")
                        .setContentText("freshen up your brain!")
                        .setTicker("freshen up your brain!")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLights(0x1d6bad, 1000, 4000)
                        .setContentIntent(contentIntent);
                if (reminderprefs.getBoolean("r1vibrate", true)) {
                    builder.setVibrate(new long[]{100, 200, 100, 500});
                }
                if (reminderprefs.getBoolean("r1sound", true)) {
                    if (!reminderprefs.getString("r1tone", "").equals("")) {
                        builder.setSound(Uri.parse(reminderprefs.getString("r1tone", "")));
                    } else {
                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    }
                }
                if (reminderprefs.getBoolean("persistentNotification", false)) {
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    builder.setPriority(NotificationCompat.PRIORITY_MAX);
                } else {
                    builder.setAutoCancel(true);
                }

                if (!bigtxt.equals("freshen up your brain!")) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.addAction(R.drawable.ic_fab_share, ct.getString(R.string.share), shareIntent);
                        Notification notification = new NotificationCompat.BigTextStyle(builder)
                                .bigText(bigtxt).build();
                        manager.notify(NOTIFICATION_ID, notification);
                    } else {
                        builder.setContentText(bigtxt);
                        manager.notify(NOTIFICATION_ID, builder.build());
                    }
                } else {
                    manager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        }

        if (AlarmNum == 2) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            String daystr = String.valueOf(day).replace("1", "su").replace("2", "mo").replace("3", "tu").replace("4", "we").replace("5", "th").replace("6", "fr").replace("7", "sa");
            if (reminderprefs.getBoolean("r2" + daystr, true)) {
                manager.cancelAll();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ct);
                builder.setContentTitle("easeWave")
                        .setContentText("freshen up your brain!")
                        .setTicker("freshen up your brain!")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLights(0x1d6bad, 1000, 4000)
                        .setContentIntent(contentIntent);
                if (reminderprefs.getBoolean("r2vibrate", true)) {
                    builder.setVibrate(new long[]{100, 200, 100, 500});
                }
                if (reminderprefs.getBoolean("r2sound", true)) {
                    if (!reminderprefs.getString("choosenSound", "").equals("")) {
                        builder.setSound(Uri.parse(reminderprefs.getString("choosenSound", "")));
                    } else {
                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    }
                }
                if (reminderprefs.getBoolean("persistentNotification", false)) {
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        builder.setPriority(Notification.PRIORITY_MAX);
                    }
                } else {
                    builder.setAutoCancel(true);
                }

                if (!bigtxt.equals("freshen up your brain!")) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.addAction(R.drawable.ic_fab_share, ct.getString(R.string.share), shareIntent);
                        Notification notification = new NotificationCompat.BigTextStyle(builder)
                                .bigText(bigtxt).build();
                        manager.notify(NOTIFICATION_ID, notification);
                    } else {
                        builder.setContentText(bigtxt);
                        manager.notify(NOTIFICATION_ID, builder.build());
                    }
                } else {
                    manager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        }

        if (AlarmNum == 3) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            String daystr = String.valueOf(day).replace("1", "su").replace("2", "mo").replace("3", "tu").replace("4", "we").replace("5", "th").replace("6", "fr").replace("7", "sa");
            if (reminderprefs.getBoolean("r3" + daystr, true)) {
                manager.cancelAll();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(ct);
                builder.setContentTitle("easeWave")
                        .setContentText("freshen up your brain!")
                        .setTicker("freshen up your brain!")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLights(0x1d6bad, 1000, 4000)
                        .setContentIntent(contentIntent);
                if (reminderprefs.getBoolean("r3vibrate", true)) {
                    builder.setVibrate(new long[]{100, 200, 100, 500});
                }
                if (reminderprefs.getBoolean("r3sound", true)) {
                    if (!reminderprefs.getString("choosenSound", "").equals("")) {
                        builder.setSound(Uri.parse(reminderprefs.getString("choosenSound", "")));
                    } else {
                        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    }
                }
                if (reminderprefs.getBoolean("persistentNotification", false)) {
                    builder.setAutoCancel(false);
                    builder.setOngoing(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        builder.setPriority(Notification.PRIORITY_MAX);
                    }
                } else {
                    builder.setAutoCancel(true);
                }

                if (!bigtxt.equals("freshen up your brain!")) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        builder.addAction(R.drawable.ic_fab_share, ct.getString(R.string.share), shareIntent);
                        Notification notification = new NotificationCompat.BigTextStyle(builder)
                                .bigText(bigtxt).build();
                        manager.notify(NOTIFICATION_ID, notification);
                    } else {
                        builder.setContentText(bigtxt);
                        manager.notify(NOTIFICATION_ID, builder.build());
                    }
                } else {
                    manager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        }
    }

    private void loadQuote() {
        // load once a day
        if (isNetworkAvailable()) {
            if (settingsprefs.getString("lastDownloadedQuote", "1991/03/01").equals(today)) {
                loadLastDownloadedQuote();
            } else {
                downloadQuote();
            }
        } else {
            loadLastDownloadedQuote();
        }
    }

    private void downloadQuote() {
        Calendar cal = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = formatter.format(cal.getTime());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Quotes");
        query.whereEqualTo("Date", today);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    //noquote = false;
                    String quote = object.getString("en");
                    String source = object.getString("verse");
                    message = quote + "\n" + source;
                    settingsprefs.edit().putString("lastDownloadedQuote", today).apply();
                    settingsprefs.edit().putString("quote", quote).apply();
                    settingsprefs.edit().putString("source", source).apply();
                    alarm();
                } else {
                    //noquote = true;
                    // try to load last from prefs
                    loadLastDownloadedQuote();
                }
            }
        });
    }

    private void loadLastDownloadedQuote() {
        if (!settingsprefs.getString("quote", "").equals("")) {
            //noquote = false;
            message = settingsprefs.getString("quote", "") + "\n" + settingsprefs.getString("source", "");
        } else {
            //noquote = true;
            message = "freshen up your brain!";
        }
        alarm();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
