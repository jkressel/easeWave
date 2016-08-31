package com.jonathansautter.easewave;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devddagnet.bright.lib.Bright;
import com.github.clans.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RemindersFragment extends android.support.v4.app.Fragment implements com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener {

    private View v;
    private Animation fab_in;
    private Handler handler = new Handler();
    private FloatingActionButton add;
    private RelativeLayout r1expand;
    private RelativeLayout reminder1;
    private TextView r1time;
    private SwitchCompat persistentnotificationtoggle;
    private SharedPreferences reminderprefs;
    private SwitchCompat r1toggle;
    private SwitchCompat r2toggle;
    private SwitchCompat r3toggle;
    private RelativeLayout reminder2;
    private RelativeLayout reminder3;
    private RelativeLayout r2expand;
    private RelativeLayout r3expand;
    private TextView r2time;
    private TextView r3time;
    private TextView r1dayssum;
    private TextView r2dayssum;
    private TextView r3dayssum;
    private CheckBox r1mo, r1tu, r1we, r1th, r1fr, r1sa, r1su;
    private CheckBox r2mo, r2tu, r2we, r2th, r2fr, r2sa, r2su;
    private CheckBox r3mo, r3tu, r3we, r3th, r3fr, r3sa, r3su;
    private SwitchCompat r1sound, r2sound, r3sound;
    private SwitchCompat r1vibrate, r2vibrate, r3vibrate;
    private Calendar now;
    private TextView r1tone;
    private TextView r2tone;
    private TextView r3tone;
    private boolean solid = false;
    private int screenwidth, screenheight;
    private boolean is24h = false;
    private SharedPreferences settingsprefs;
    private String message;
    private String today;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_reminders, container, false);

        setup();

        return v;
    }

    private void setup() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;

        setBackgroundImage();

        fixMargins();

        reminderprefs = getActivity().getSharedPreferences("REMINDER", 0);
        settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);

        Animation fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fab_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_in);
        now = Calendar.getInstance();
        RelativeLayout content = (RelativeLayout) v.findViewById(R.id.content);
        add = (FloatingActionButton) v.findViewById(R.id.add);
        persistentnotificationtoggle = (SwitchCompat) v.findViewById(R.id.persistentnotificationtoggle);
        r1toggle = (SwitchCompat) v.findViewById(R.id.r1toggle);
        r2toggle = (SwitchCompat) v.findViewById(R.id.r2toggle);
        r3toggle = (SwitchCompat) v.findViewById(R.id.screenbrightness);
        RelativeLayout reminder0 = (RelativeLayout) v.findViewById(R.id.card0);
        reminder1 = (RelativeLayout) v.findViewById(R.id.card1);
        reminder2 = (RelativeLayout) v.findViewById(R.id.card2);
        reminder3 = (RelativeLayout) v.findViewById(R.id.card3);
        r1expand = (RelativeLayout) v.findViewById(R.id.card1expand);
        r2expand = (RelativeLayout) v.findViewById(R.id.card2expand);
        r3expand = (RelativeLayout) v.findViewById(R.id.card3expand);
        r1time = (TextView) v.findViewById(R.id.r1time);
        r2time = (TextView) v.findViewById(R.id.r2time);
        r3time = (TextView) v.findViewById(R.id.r3time);
        r1dayssum = (TextView) v.findViewById(R.id.r1dayssum);
        r2dayssum = (TextView) v.findViewById(R.id.r2dayssum);
        r3dayssum = (TextView) v.findViewById(R.id.r3dayssum);
        r1sound = (SwitchCompat) v.findViewById(R.id.r1sound);
        r2sound = (SwitchCompat) v.findViewById(R.id.r2sound);
        r3sound = (SwitchCompat) v.findViewById(R.id.r3sound);
        r1tone = (TextView) v.findViewById(R.id.r1tone);
        r2tone = (TextView) v.findViewById(R.id.r2tone);
        r3tone = (TextView) v.findViewById(R.id.r3tone);
        r1vibrate = (SwitchCompat) v.findViewById(R.id.r1vibrate);
        r2vibrate = (SwitchCompat) v.findViewById(R.id.r2vibrate);
        r3vibrate = (SwitchCompat) v.findViewById(R.id.r3vibrate);
        r1mo = (CheckBox) v.findViewById(R.id.r1mo);
        r1tu = (CheckBox) v.findViewById(R.id.r1tu);
        r1we = (CheckBox) v.findViewById(R.id.r1we);
        r1th = (CheckBox) v.findViewById(R.id.r1th);
        r1fr = (CheckBox) v.findViewById(R.id.r1fr);
        r1sa = (CheckBox) v.findViewById(R.id.r1sa);
        r1su = (CheckBox) v.findViewById(R.id.r1su);
        r2mo = (CheckBox) v.findViewById(R.id.r2mo);
        r2tu = (CheckBox) v.findViewById(R.id.r2tu);
        r2we = (CheckBox) v.findViewById(R.id.r2we);
        r2th = (CheckBox) v.findViewById(R.id.r2th);
        r2fr = (CheckBox) v.findViewById(R.id.r2fr);
        r2sa = (CheckBox) v.findViewById(R.id.r2sa);
        r2su = (CheckBox) v.findViewById(R.id.r2su);
        r3mo = (CheckBox) v.findViewById(R.id.r3mo);
        r3tu = (CheckBox) v.findViewById(R.id.r3tu);
        r3we = (CheckBox) v.findViewById(R.id.r3we);
        r3th = (CheckBox) v.findViewById(R.id.r3th);
        r3fr = (CheckBox) v.findViewById(R.id.r3fr);
        r3sa = (CheckBox) v.findViewById(R.id.r3sa);
        r3su = (CheckBox) v.findViewById(R.id.r3su);

        r1expand.setVisibility(View.GONE);
        r2expand.setVisibility(View.GONE);
        r3expand.setVisibility(View.GONE);

        if (Locale.getDefault().equals(Locale.GERMAN)) {
            is24h = true;
        }

        final SharedPreferences settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);
        if (!settingsprefs.getBoolean("imagebackground", true)) {
            solid = true;
        }
        if (solid) {
            int bottom = reminder0.getPaddingBottom();
            int top = reminder0.getPaddingTop();
            int right = reminder0.getPaddingRight();
            int left = reminder0.getPaddingLeft();
            reminder0.setBackgroundResource(R.drawable.card_bg_solid_ripple);
            reminder1.setBackgroundResource(R.drawable.card_bg_solid_ripple);
            reminder2.setBackgroundResource(R.drawable.card_bg_solid_ripple);
            reminder3.setBackgroundResource(R.drawable.card_bg_solid_ripple);
            reminder0.setPadding(left, top, right, bottom);
            reminder1.setPadding(left, top, right, bottom);
            reminder2.setPadding(left, top, right, bottom);
            reminder3.setPadding(left, top, right, bottom);
        }

        initiateLayout();

        content.startAnimation(fade_in);
        content.setVisibility(View.VISIBLE);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int whichreminder = 0;
                ArrayList<Integer> availibleReminders = new ArrayList<>();
                availibleReminders.add(1);
                availibleReminders.add(2);
                availibleReminders.add(3);
                if (reminderprefs.getBoolean("r1", false)) {
                    for (int i = 0; i < availibleReminders.size(); i++) {
                        if (availibleReminders.get(i) == 1) {
                            availibleReminders.remove(i);
                        }
                    }
                }
                if (reminderprefs.getBoolean("r2", false)) {
                    for (int i = 0; i < availibleReminders.size(); i++) {
                        if (availibleReminders.get(i) == 2) {
                            availibleReminders.remove(i);
                        }
                    }
                }
                if (reminderprefs.getBoolean("r3", false)) {
                    for (int i = 0; i < availibleReminders.size(); i++) {
                        if (availibleReminders.get(i) == 3) {
                            availibleReminders.remove(i);
                        }
                    }
                }
                if (availibleReminders.size() > 0) {
                    whichreminder = availibleReminders.get(0);
                }
                if (whichreminder != 0) {
                    reminderprefs.edit().putString("reminderNumber", String.valueOf(whichreminder)).apply();
                    reminderprefs.edit().putString("timepickerAction", "create").apply();
                    TimePickerDialog tpd = TimePickerDialog.newInstance(
                            RemindersFragment.this,
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            is24h
                    );
                    tpd.setThemeDark(false);
                    tpd.vibrate(false);
                    tpd.dismissOnPause(false);
                    tpd.setAccentColor(Color.parseColor("#ff4585dd"));
                    tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
                }
            }
        });

        r1time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = Integer.parseInt(r1time.getText().toString().substring(0, 2));
                int minute = Integer.parseInt(r1time.getText().toString().substring(3, 5));
                reminderprefs.edit().putString("reminderNumber", "1").apply();
                reminderprefs.edit().putString("timepickerAction", "edit").apply();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        RemindersFragment.this, hour, minute, is24h);
                tpd.setThemeDark(false);
                tpd.vibrate(false);
                tpd.dismissOnPause(false);
                tpd.setAccentColor(Color.parseColor("#ff4585dd"));
                tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
            }
        });

        r2time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = Integer.parseInt(r2time.getText().toString().substring(0, 2));
                int minute = Integer.parseInt(r2time.getText().toString().substring(3, 5));
                reminderprefs.edit().putString("reminderNumber", "2").apply();
                reminderprefs.edit().putString("timepickerAction", "edit").apply();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        RemindersFragment.this, hour, minute, is24h);
                tpd.setThemeDark(false);
                tpd.vibrate(false);
                tpd.dismissOnPause(false);
                tpd.setAccentColor(Color.parseColor("#ff4585dd"));
                tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
            }
        });

        r3time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = Integer.parseInt(r3time.getText().toString().substring(0, 2));
                int minute = Integer.parseInt(r3time.getText().toString().substring(3, 5));
                reminderprefs.edit().putString("reminderNumber", "3").apply();
                reminderprefs.edit().putString("timepickerAction", "edit").apply();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        RemindersFragment.this, hour, minute, is24h);
                tpd.setThemeDark(false);
                tpd.vibrate(false);
                tpd.dismissOnPause(false);
                tpd.setAccentColor(Color.parseColor("#ff4585dd"));
                tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
            }
        });

        reminder0.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // just to allow ripple
            }
        });

        reminder1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (r1expand.getVisibility() == View.GONE) {
                    expand(r1expand, reminder1);
                } else {
                    collapse(r1expand, reminder1);
                }
            }
        });

        reminder2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (r2expand.getVisibility() == View.GONE) {
                    expand(r2expand, reminder2);
                } else {
                    collapse(r2expand, reminder2);
                }
            }
        });

        reminder3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (r3expand.getVisibility() == View.GONE) {
                    expand(r3expand, reminder3);
                } else {
                    collapse(r3expand, reminder3);
                }
            }
        });

        reminder1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alert_dialog);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                setDialogSize(dialog);

                TextView title = (TextView) dialog.findViewById(R.id.title);
                TextView message = (TextView) dialog.findViewById(R.id.message);
                TextView ok = (TextView) dialog.findViewById(R.id.ok);
                TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

                title.setText(getActivity().getString(R.string.deletereminder));
                message.setVisibility(View.GONE);
                ok.setText(getActivity().getString(R.string.yesuppercase));
                cancel.setText(getActivity().getString(R.string.canceluppercase));

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reminderprefs.edit().putBoolean("r1", false).apply();
                        reminderprefs.edit().putBoolean("r1enabled", false).apply();
                        setReminder1(); // order is important here! hour can't be ""
                        reminderprefs.edit().putString("r1hour", "").apply();
                        reminderprefs.edit().putString("r1minute", "").apply();
                        reminderprefs.edit().putBoolean("r1sound", true).apply();
                        reminderprefs.edit().putBoolean("r1vibrate", true).apply();
                        reminderprefs.edit().putBoolean("r1mo", true).apply();
                        reminderprefs.edit().putBoolean("r1tu", true).apply();
                        reminderprefs.edit().putBoolean("r1we", true).apply();
                        reminderprefs.edit().putBoolean("r1th", true).apply();
                        reminderprefs.edit().putBoolean("r1fr", true).apply();
                        reminderprefs.edit().putBoolean("r1sa", true).apply();
                        reminderprefs.edit().putBoolean("r1su", true).apply();
                        collapse(r1expand, reminder1);
                        initiateLayout();
                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                /*int style;
                if (Build.VERSION.SDK_INT < 21) {
                    style = AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
                } else {
                    style = R.style.ThemeDialog;
                }
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), style).create();
                alertDialog.setTitle(getActivity().getString(R.string.deletereminder));
                //alertDialog.setMessage("");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getString(R.string.canceluppercase),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(R.string.yesuppercase),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                reminderprefs.edit().putBoolean("r1", false).apply();
                                reminderprefs.edit().putBoolean("r1enabled", false).apply();
                                setReminder1(); // order is important here! hour can't be ""
                                reminderprefs.edit().putString("r1hour", "").apply();
                                reminderprefs.edit().putString("r1minute", "").apply();
                                reminderprefs.edit().putBoolean("r1sound", true).apply();
                                reminderprefs.edit().putBoolean("r1vibrate", true).apply();
                                reminderprefs.edit().putBoolean("r1mo", true).apply();
                                reminderprefs.edit().putBoolean("r1tu", true).apply();
                                reminderprefs.edit().putBoolean("r1we", true).apply();
                                reminderprefs.edit().putBoolean("r1th", true).apply();
                                reminderprefs.edit().putBoolean("r1fr", true).apply();
                                reminderprefs.edit().putBoolean("r1sa", true).apply();
                                reminderprefs.edit().putBoolean("r1su", true).apply();
                                collapse(r1expand, reminder1);
                                initiateLayout();
                                dialog.dismiss();
                            }
                        });
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();*/
                return false;
            }
        });

        reminder2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alert_dialog);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                setDialogSize(dialog);

                TextView title = (TextView) dialog.findViewById(R.id.title);
                TextView message = (TextView) dialog.findViewById(R.id.message);
                TextView ok = (TextView) dialog.findViewById(R.id.ok);
                TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

                title.setText(getActivity().getString(R.string.deletereminder));
                message.setVisibility(View.GONE);
                ok.setText(getActivity().getString(R.string.yesuppercase));
                cancel.setText(getActivity().getString(R.string.canceluppercase));

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reminderprefs.edit().putBoolean("r2", false).apply();
                        reminderprefs.edit().putBoolean("r2enabled", false).apply();
                        setReminder2(); // order is important here! hour can't be ""
                        reminderprefs.edit().putString("r2hour", "").apply();
                        reminderprefs.edit().putString("r2minute", "").apply();
                        reminderprefs.edit().putBoolean("r2sound", true).apply();
                        reminderprefs.edit().putBoolean("r2vibrate", true).apply();
                        reminderprefs.edit().putBoolean("r2mo", true).apply();
                        reminderprefs.edit().putBoolean("r2tu", true).apply();
                        reminderprefs.edit().putBoolean("r2we", true).apply();
                        reminderprefs.edit().putBoolean("r2th", true).apply();
                        reminderprefs.edit().putBoolean("r2fr", true).apply();
                        reminderprefs.edit().putBoolean("r2sa", true).apply();
                        reminderprefs.edit().putBoolean("r2su", true).apply();
                        collapse(r2expand, reminder2);
                        initiateLayout();
                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                /*int style;
                if (Build.VERSION.SDK_INT < 21) {
                    style = AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
                } else {
                    style = R.style.ThemeDialog;
                }
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), style).create();
                alertDialog.setTitle(getActivity().getString(R.string.deletereminder));
                //alertDialog.setMessage("");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getString(R.string.canceluppercase),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(R.string.yesuppercase),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                reminderprefs.edit().putBoolean("r2", false).apply();
                                reminderprefs.edit().putBoolean("r2enabled", false).apply();
                                setReminder2(); // order is important here! hour can't be ""
                                reminderprefs.edit().putString("r2hour", "").apply();
                                reminderprefs.edit().putString("r2minute", "").apply();
                                reminderprefs.edit().putBoolean("r2sound", true).apply();
                                reminderprefs.edit().putBoolean("r2vibrate", true).apply();
                                reminderprefs.edit().putBoolean("r2mo", true).apply();
                                reminderprefs.edit().putBoolean("r2tu", true).apply();
                                reminderprefs.edit().putBoolean("r2we", true).apply();
                                reminderprefs.edit().putBoolean("r2th", true).apply();
                                reminderprefs.edit().putBoolean("r2fr", true).apply();
                                reminderprefs.edit().putBoolean("r2sa", true).apply();
                                reminderprefs.edit().putBoolean("r2su", true).apply();
                                collapse(r2expand, reminder2);
                                initiateLayout();
                                dialog.dismiss();
                            }
                        });
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();*/
                return false;
            }
        });

        reminder3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.alert_dialog);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                setDialogSize(dialog);

                TextView title = (TextView) dialog.findViewById(R.id.title);
                TextView message = (TextView) dialog.findViewById(R.id.message);
                TextView ok = (TextView) dialog.findViewById(R.id.ok);
                TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

                title.setText(getActivity().getString(R.string.deletereminder));
                message.setVisibility(View.GONE);
                ok.setText(getActivity().getString(R.string.yesuppercase));
                cancel.setText(getActivity().getString(R.string.canceluppercase));

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reminderprefs.edit().putBoolean("r3", false).apply();
                        reminderprefs.edit().putBoolean("r3enabled", false).apply();
                        setReminder3(); // order is important here! hour can't be ""
                        reminderprefs.edit().putString("r3hour", "").apply();
                        reminderprefs.edit().putString("r3minute", "").apply();
                        reminderprefs.edit().putBoolean("r3sound", true).apply();
                        reminderprefs.edit().putBoolean("r3vibrate", true).apply();
                        reminderprefs.edit().putBoolean("r3mo", true).apply();
                        reminderprefs.edit().putBoolean("r3tu", true).apply();
                        reminderprefs.edit().putBoolean("r3we", true).apply();
                        reminderprefs.edit().putBoolean("r3th", true).apply();
                        reminderprefs.edit().putBoolean("r3fr", true).apply();
                        reminderprefs.edit().putBoolean("r3sa", true).apply();
                        reminderprefs.edit().putBoolean("r3su", true).apply();
                        collapse(r3expand, reminder3);
                        initiateLayout();
                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

                /*int style;
                if (Build.VERSION.SDK_INT < 21) {
                    style = AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
                } else {
                    style = R.style.ThemeDialog;
                }
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), style).create();
                alertDialog.setTitle(getActivity().getString(R.string.deletereminder));
                //alertDialog.setMessage("");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getString(R.string.canceluppercase),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(R.string.yesuppercase),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                reminderprefs.edit().putBoolean("r3", false).apply();
                                reminderprefs.edit().putBoolean("r3enabled", false).apply();
                                setReminder3(); // order is important here! hour can't be ""
                                reminderprefs.edit().putString("r3hour", "").apply();
                                reminderprefs.edit().putString("r3minute", "").apply();
                                reminderprefs.edit().putBoolean("r3sound", true).apply();
                                reminderprefs.edit().putBoolean("r3vibrate", true).apply();
                                reminderprefs.edit().putBoolean("r3mo", true).apply();
                                reminderprefs.edit().putBoolean("r3tu", true).apply();
                                reminderprefs.edit().putBoolean("r3we", true).apply();
                                reminderprefs.edit().putBoolean("r3th", true).apply();
                                reminderprefs.edit().putBoolean("r3fr", true).apply();
                                reminderprefs.edit().putBoolean("r3sa", true).apply();
                                reminderprefs.edit().putBoolean("r3su", true).apply();
                                collapse(r3expand, reminder3);
                                initiateLayout();
                                dialog.dismiss();
                            }
                        });
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();*/
                return false;
            }
        });

        persistentnotificationtoggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("persistentNotification", isChecked).apply();
                loadQuote();
            }
        });

        r1toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1enabled", isChecked).apply();
                setReminder1();
            }
        });

        r2toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2enabled", isChecked).apply();
                setReminder2();
            }
        });

        r3toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3enabled", isChecked).apply();
                setReminder3();
            }
        });

        r1sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1sound", isChecked).apply();
            }
        });

        r2sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2sound", isChecked).apply();
            }
        });

        r3sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3sound", isChecked).apply();
            }
        });

        r1tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getActivity().getString(R.string.chooseremindersound));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(reminderprefs.getString("r1tone", String.valueOf(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)))));
                startActivityForResult(intent, 1);
            }
        });

        r2tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getActivity().getString(R.string.chooseremindersound));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(reminderprefs.getString("r2tone", String.valueOf(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)))));
                startActivityForResult(intent, 2);
            }
        });

        r3tone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getActivity().getString(R.string.chooseremindersound));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(reminderprefs.getString("r3tone", String.valueOf(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)))));
                startActivityForResult(intent, 3);
            }
        });

        r1vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1vibrate", isChecked).apply();
            }
        });

        r2vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2vibrate", isChecked).apply();
            }
        });

        r3vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3vibrate", isChecked).apply();
            }
        });

        r1mo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1mo", isChecked).apply();
                updateR1Weekdays();
            }
        });

        r1tu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1tu", isChecked).apply();
                updateR1Weekdays();
            }
        });

        r1we.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1we", isChecked).apply();
                updateR1Weekdays();
            }
        });

        r1th.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1th", isChecked).apply();
                updateR1Weekdays();
            }
        });

        r1fr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1fr", isChecked).apply();
                updateR1Weekdays();
            }
        });

        r1sa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1sa", isChecked).apply();
                updateR1Weekdays();
            }
        });

        r1su.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r1su", isChecked).apply();
                updateR1Weekdays();
            }
        });

        r2mo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2mo", isChecked).apply();
                updateR2Weekdays();
            }
        });

        r2tu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2tu", isChecked).apply();
                updateR2Weekdays();
            }
        });

        r2we.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2we", isChecked).apply();
                updateR2Weekdays();
            }
        });

        r2th.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2th", isChecked).apply();
                updateR2Weekdays();
            }
        });

        r2fr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2fr", isChecked).apply();
                updateR2Weekdays();
            }
        });

        r2sa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2sa", isChecked).apply();
                updateR2Weekdays();
            }
        });

        r2su.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r2su", isChecked).apply();
                updateR2Weekdays();
            }
        });

        r3mo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3mo", isChecked).apply();
                updateR3Weekdays();
            }
        });

        r3tu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3tu", isChecked).apply();
                updateR3Weekdays();
            }
        });

        r3we.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3we", isChecked).apply();
                updateR3Weekdays();
            }
        });

        r3th.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3th", isChecked).apply();
                updateR3Weekdays();
            }
        });

        r3fr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3fr", isChecked).apply();
                updateR3Weekdays();
            }
        });

        r3sa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3sa", isChecked).apply();
                updateR3Weekdays();
            }
        });

        r3su.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminderprefs.edit().putBoolean("r3su", isChecked).apply();
                updateR3Weekdays();
            }
        });

    }

    private void updateR1Weekdays() {
        String days = "";
        if (reminderprefs.getBoolean("r1mo", true)) {
            days = getActivity().getString(R.string.mo) + ", ";
        }
        if (reminderprefs.getBoolean("r1tu", true)) {
            days = days + getActivity().getString(R.string.tu) + ", ";
        }
        if (reminderprefs.getBoolean("r1we", true)) {
            days = days + getActivity().getString(R.string.we) + ", ";
        }
        if (reminderprefs.getBoolean("r1th", true)) {
            days = days + getActivity().getString(R.string.th) + ", ";
        }
        if (reminderprefs.getBoolean("r1fr", true)) {
            days = days + getActivity().getString(R.string.fr) + ", ";
        }
        if (reminderprefs.getBoolean("r1sa", true)) {
            days = days + getActivity().getString(R.string.sa) + ", ";
        }
        if (reminderprefs.getBoolean("r1su", true)) {
            days = days + getActivity().getString(R.string.su) + ", ";
        }
        days = days.substring(0, days.length() - 2);
        r1dayssum.setText(days);
    }

    private void updateR2Weekdays() {
        String days = "";
        if (reminderprefs.getBoolean("r2mo", true)) {
            days = getActivity().getString(R.string.mo) + ", ";
        }
        if (reminderprefs.getBoolean("r2tu", true)) {
            days = days + getActivity().getString(R.string.tu) + ", ";
        }
        if (reminderprefs.getBoolean("r2we", true)) {
            days = days + getActivity().getString(R.string.we) + ", ";
        }
        if (reminderprefs.getBoolean("r2th", true)) {
            days = days + getActivity().getString(R.string.th) + ", ";
        }
        if (reminderprefs.getBoolean("r2fr", true)) {
            days = days + getActivity().getString(R.string.fr) + ", ";
        }
        if (reminderprefs.getBoolean("r2sa", true)) {
            days = days + getActivity().getString(R.string.sa) + ", ";
        }
        if (reminderprefs.getBoolean("r2su", true)) {
            days = days + getActivity().getString(R.string.su) + ", ";
        }
        days = days.substring(0, days.length() - 2);
        r2dayssum.setText(days);
    }

    private void updateR3Weekdays() {
        String days = "";
        if (reminderprefs.getBoolean("r3mo", true)) {
            days = getActivity().getString(R.string.mo) + ", ";
        }
        if (reminderprefs.getBoolean("r3tu", true)) {
            days = days + getActivity().getString(R.string.tu) + ", ";
        }
        if (reminderprefs.getBoolean("r3we", true)) {
            days = days + getActivity().getString(R.string.we) + ", ";
        }
        if (reminderprefs.getBoolean("r3th", true)) {
            days = days + getActivity().getString(R.string.th) + ", ";
        }
        if (reminderprefs.getBoolean("r3fr", true)) {
            days = days + getActivity().getString(R.string.fr) + ", ";
        }
        if (reminderprefs.getBoolean("r3sa", true)) {
            days = days + getActivity().getString(R.string.sa) + ", ";
        }
        if (reminderprefs.getBoolean("r3su", true)) {
            days = days + getActivity().getString(R.string.su) + ", ";
        }
        days = days.substring(0, days.length() - 2);
        r3dayssum.setText(days);
    }

    private void initiateLayout() {
        if (reminderprefs.getBoolean("r1", false) && reminderprefs.getBoolean("r2", false) && reminderprefs.getBoolean("r3", false)) {
            add.setVisibility(View.GONE);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!reminderprefs.getBoolean("r1", false) || !reminderprefs.getBoolean("r2", false) || !reminderprefs.getBoolean("r3", false)) {
                        if (!add.isShown()) {
                            add.startAnimation(fab_in);
                            add.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }, 500);
        }
        persistentnotificationtoggle.setChecked(reminderprefs.getBoolean("persistentNotification", false));
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone defaultRingtone = null;
        Ringtone ringtone;
        if (defaultUri != null) {
            defaultRingtone = RingtoneManager.getRingtone(getActivity(), defaultUri);
        }
        if (reminderprefs.getBoolean("r1", false)) {
            r1sound.setChecked(reminderprefs.getBoolean("r1sound", true));
            String r1tonestr = reminderprefs.getString("r1tone", "");
            if (!r1tonestr.equals("")) {
                ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(r1tonestr));
                if (ringtone != null) {
                    r1tone.setText(ringtone.getTitle(getActivity()));
                } else {
                    r1tone.setText(R.string.errorreceivingringtone);
                }
            } else if (defaultRingtone != null) {
                ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(defaultRingtone.getTitle(getActivity())));
                if (ringtone != null) {
                    r1tone.setText(ringtone.getTitle(getActivity()));
                } else {
                    r1tone.setText(R.string.errorreceivingringtone);
                }
            } else {
                r1tone.setText(R.string.nodefaultringtone);
            }
            r1vibrate.setChecked(reminderprefs.getBoolean("r1vibrate", true));
            r1mo.setChecked(reminderprefs.getBoolean("r1mo", true));
            r1tu.setChecked(reminderprefs.getBoolean("r1tu", true));
            r1we.setChecked(reminderprefs.getBoolean("r1we", true));
            r1th.setChecked(reminderprefs.getBoolean("r1th", true));
            r1fr.setChecked(reminderprefs.getBoolean("r1fr", true));
            r1sa.setChecked(reminderprefs.getBoolean("r1sa", true));
            r1su.setChecked(reminderprefs.getBoolean("r1su", true));
            reminder1.setVisibility(View.VISIBLE);
            String r1timetext = reminderprefs.getString("r1hour", "") + ":" + reminderprefs.getString("r1minute", "") + " " + getActivity().getString(R.string.uhr);
            r1time.setText(r1timetext);
            r1toggle.setChecked(reminderprefs.getBoolean("r1enabled", false));
        } else {
            reminder1.setVisibility(View.GONE);
        }
        if (reminderprefs.getBoolean("r2", false)) {
            r2sound.setChecked(reminderprefs.getBoolean("r2sound", true));
            String r2tonestr = reminderprefs.getString("r2tone", "");
            if (!r2tonestr.equals("")) {
                ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(r2tonestr));
                if (ringtone != null) {
                    r2tone.setText(ringtone.getTitle(getActivity()));
                } else {
                    r2tone.setText(R.string.errorreceivingringtone);
                }
            } else if (defaultRingtone != null) {
                ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(defaultRingtone.getTitle(getActivity())));
                r2tone.setText(ringtone.getTitle(getActivity()));
            } else {
                r2tone.setText(R.string.nodefaultringtone);
            }
            r2vibrate.setChecked(reminderprefs.getBoolean("r2vibrate", true));
            r2mo.setChecked(reminderprefs.getBoolean("r2mo", true));
            r2tu.setChecked(reminderprefs.getBoolean("r2tu", true));
            r2we.setChecked(reminderprefs.getBoolean("r2we", true));
            r2th.setChecked(reminderprefs.getBoolean("r2th", true));
            r2fr.setChecked(reminderprefs.getBoolean("r2fr", true));
            r2sa.setChecked(reminderprefs.getBoolean("r2sa", true));
            r2su.setChecked(reminderprefs.getBoolean("r2su", true));
            reminder2.setVisibility(View.VISIBLE);
            String r2text = reminderprefs.getString("r2hour", "") + ":" + reminderprefs.getString("r2minute", "") + " " + getActivity().getString(R.string.uhr);
            r2time.setText(r2text);
            r2toggle.setChecked(reminderprefs.getBoolean("r2enabled", false));
        } else {
            reminder2.setVisibility(View.GONE);
        }
        if (reminderprefs.getBoolean("r3", false)) {
            r3sound.setChecked(reminderprefs.getBoolean("r3sound", true));
            String r3tonestr = reminderprefs.getString("r3tone", "");
            if (!r3tonestr.equals("")) {
                ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(r3tonestr));
                if (ringtone != null) {
                    r3tone.setText(ringtone.getTitle(getActivity()));
                } else {
                    r3tone.setText(R.string.errorreceivingringtone);
                }
            } else if (defaultRingtone != null) {
                ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(defaultRingtone.getTitle(getActivity())));
                r3tone.setText(ringtone.getTitle(getActivity()));
            } else {
                r3tone.setText(R.string.nodefaultringtone);
            }
            r3vibrate.setChecked(reminderprefs.getBoolean("r3vibrate", true));
            r3mo.setChecked(reminderprefs.getBoolean("r3mo", true));
            r3tu.setChecked(reminderprefs.getBoolean("r3tu", true));
            r3we.setChecked(reminderprefs.getBoolean("r3we", true));
            r3th.setChecked(reminderprefs.getBoolean("r3th", true));
            r3fr.setChecked(reminderprefs.getBoolean("r3fr", true));
            r3sa.setChecked(reminderprefs.getBoolean("r3sa", true));
            r3su.setChecked(reminderprefs.getBoolean("r3su", true));
            reminder3.setVisibility(View.VISIBLE);
            String r3text = reminderprefs.getString("r3hour", "") + ":" + reminderprefs.getString("r3minute", "") + " " + getActivity().getString(R.string.uhr);
            r3time.setText(r3text);
            r3toggle.setChecked(reminderprefs.getBoolean("r3enabled", false));
        } else {
            reminder3.setVisibility(View.GONE);
        }
        r1toggle.setChecked(reminderprefs.getBoolean("r1enabled", false));
        r2toggle.setChecked(reminderprefs.getBoolean("r2enabled", false));
        r3toggle.setChecked(reminderprefs.getBoolean("r3enabled", false));
        updateR1Weekdays();
        updateR2Weekdays();
        updateR3Weekdays();
    }

    private void setReminder1() {
        if (!reminderprefs.getString("r1hour", "").equals("")) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(reminderprefs.getString("r1hour", "")));
            cal.set(Calendar.MINUTE, Integer.parseInt(reminderprefs.getString("r1minute", "")));
            int id = 111;
            Intent alarmintent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
            alarmintent.putExtra("title", "freshen up your brain!");
            alarmintent.putExtra("note", "easeWave");
            alarmintent.putExtra("AlarmNum", 1);
            PendingIntent sender = PendingIntent.getBroadcast(getActivity().getApplicationContext(), id,
                    alarmintent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            if (reminderprefs.getBoolean("r1enabled", false)) {
                if (cal.getTimeInMillis() > now.getTimeInMillis()) {
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    cal.set(Calendar.DATE, day);
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
                }
            } else {
                am.cancel(sender);
            }
        }
    }

    private void setReminder2() {
        if (!reminderprefs.getString("r2hour", "").equals("")) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(reminderprefs.getString("r2hour", "")));
            cal.set(Calendar.MINUTE, Integer.parseInt(reminderprefs.getString("r2minute", "")));
            int id = 222;
            Intent alarmintent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
            alarmintent.putExtra("title", "freshen up your brain!");
            alarmintent.putExtra("note", "easeWave");
            alarmintent.putExtra("AlarmNum", 2);
            PendingIntent sender2 = PendingIntent.getBroadcast(getActivity().getApplicationContext(), id,
                    alarmintent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            if (reminderprefs.getBoolean("r2enabled", false)) {
                if (cal.getTimeInMillis() > now.getTimeInMillis()) {
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender2);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    cal.set(Calendar.DATE, day);
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender2);
                }
            } else {
                am.cancel(sender2);
            }
        }
    }

    private void setReminder3() {
        if (!reminderprefs.getString("r3hour", "").equals("")) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(reminderprefs.getString("r3hour", "")));
            cal.set(Calendar.MINUTE, Integer.parseInt(reminderprefs.getString("r3minute", "")));
            int id = 333;
            Intent alarmintent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
            alarmintent.putExtra("title", "freshen up your brain!");
            alarmintent.putExtra("note", "easeWave");
            alarmintent.putExtra("AlarmNum", 3);
            PendingIntent sender3 = PendingIntent.getBroadcast(getActivity().getApplicationContext(), id,
                    alarmintent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            if (reminderprefs.getBoolean("r3enabled", false)) {
                if (cal.getTimeInMillis() > now.getTimeInMillis()) {
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender3);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                    cal.set(Calendar.DATE, day);
                    am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender3);
                }
            } else {
                am.cancel(sender3);
            }
        }
    }

    public void setPersistentReminder() {
        /*AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent alarmintent = new Intent(getActivity(), AlarmReceiver.class);
        alarmintent.putExtra("AlarmNum", 100);
        PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 444,
                alarmintent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);

        if (persistentnotificationtoggle.isChecked()) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
        } else {
            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        }*/
        String bigtxt = message.replaceAll("<b>", "").replaceAll("</b>", "");
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        int NOTIFICATION_ID = 1;
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), NOTIFICATION_ID, new Intent(getActivity(), MainActivity.class), 0);
        Intent shareIntent1 = new Intent();
        shareIntent1.setAction(Intent.ACTION_SEND);
        shareIntent1.putExtra(Intent.EXTRA_TEXT, bigtxt);
        shareIntent1.putExtra(Intent.EXTRA_SUBJECT, "freshen up your brain");
        shareIntent1.setType("text/plain");
        PendingIntent shareIntent = PendingIntent.getActivity(getActivity(), 0, shareIntent1, 0);
        manager.cancelAll();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
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

        if (persistentnotificationtoggle.isChecked()) {
            if (!bigtxt.equals("freshen up your brain!")) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    builder.addAction(R.drawable.ic_fab_share, getActivity().getString(R.string.share), shareIntent);
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
        } else {
            manager.cancelAll();
        }
    }

    private void loadQuote() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = dateFormat.format(date);

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
                    setPersistentReminder();
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
        setPersistentReminder();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void expand(RelativeLayout expandLayout, RelativeLayout reminder) {
        if (solid) {
            int bottom = expandLayout.getPaddingBottom();
            int top = expandLayout.getPaddingTop();
            int right = expandLayout.getPaddingRight();
            int left = expandLayout.getPaddingLeft();
            expandLayout.setBackgroundResource(R.drawable.card_expand_bg_solid);
            expandLayout.setPadding(left, top, right, bottom);
        }
        //set Visible
        expandLayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        expandLayout.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, expandLayout.getMeasuredHeight(), expandLayout, reminder);
        mAnimator.start();
    }

    private void collapse(final RelativeLayout expandLayout, final RelativeLayout reminder) {
        int finalHeight = expandLayout.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, expandLayout, reminder);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                expandLayout.setVisibility(View.GONE);
                int bottom = reminder.getPaddingBottom();
                int top = reminder.getPaddingTop();
                int right = reminder.getPaddingRight();
                int left = reminder.getPaddingLeft();
                if (solid) {
                    reminder.setBackgroundResource(R.drawable.card_bg_solid_ripple);
                } else {
                    reminder.setBackgroundResource(R.drawable.card_bg_ripple);
                }
                reminder.setPadding(left, top, right, bottom);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        mAnimator.start();
    }

    private void setDialogSize(Dialog dialog) {

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) != Configuration.SCREENLAYOUT_SIZE_LARGE
                && (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        } else {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            int dialogwidth;
            if (width > height) {
                dialogwidth = (int) Math.round(height * 0.7);
            } else {
                dialogwidth = (int) Math.round(width * 0.7);
            }
            dialog.getWindow().setLayout(dialogwidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private ValueAnimator slideAnimator(int start, int end, final RelativeLayout expandLayout, final RelativeLayout reminder) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = expandLayout.getLayoutParams();
                layoutParams.height = value;
                expandLayout.setLayoutParams(layoutParams);
                int bottom = reminder.getPaddingBottom();
                int top = reminder.getPaddingTop();
                int right = reminder.getPaddingRight();
                int left = reminder.getPaddingLeft();
                if (solid) {
                    reminder.setBackgroundResource(R.drawable.card_bg_expanded_solid_ripple);
                } else {
                    reminder.setBackgroundResource(R.drawable.card_bg_expanded_ripple);
                }
                reminder.setPadding(left, top, right, bottom);
            }
        });
        return animator;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void fixMargins() {
        // Fix margins
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(0, getStatusBarHeight(), 0, 0);
            RelativeLayout toplayout = (RelativeLayout) v.findViewById(R.id.toplayout);
            toplayout.setLayoutParams(params);
        }
    }

    private void setBackgroundImage() {
        SharedPreferences settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);
        RelativeLayout screenlayout = (RelativeLayout) v.findViewById(R.id.screenlayout);
        if (settingsprefs.getBoolean("imagebackground", true)) {
            screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
            File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File path = new File(sdCard + "/easeWave");
            //path.mkdirs();
            String filename = "background.jpg";
            File file = new File(path, filename);
            File imgFile = new File(file.getPath());
            try {
                if (imgFile.exists()) {
                    //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    Bitmap myBitmap = decodeSampledBitmapFromResource(imgFile.getAbsolutePath(), screenwidth, screenheight);
                    ImageView iv = (ImageView) v.findViewById(R.id.imageView);
                    iv.setImageBitmap(myBitmap);
                    adjustActionBarColor(myBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //screenlayout.setBackgroundColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
            screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
            ImageView iv = (ImageView) v.findViewById(R.id.imageView);
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Bitmap image = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
            image.eraseColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
            iv.setImageBitmap(image);
            adjustActionBarColor(image);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                reminderprefs.edit().putString("r1tone", uri.toString()).apply();
                final Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
                if (ringtone != null) {
                    r1tone.setText(ringtone.getTitle(getActivity()));
                } else {
                    r1tone.setText(R.string.errorreceivingringtone);
                }
            } else {
                reminderprefs.edit().putString("r1tone", "").apply();
                Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (defaultUri != null) {
                    final Ringtone defaultRingtone = RingtoneManager.getRingtone(getActivity(), defaultUri);
                    if (defaultRingtone != null) {
                        r1tone.setText(defaultRingtone.getTitle(getActivity()));
                    }
                }
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == 2) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                reminderprefs.edit().putString("r2tone", uri.toString()).apply();
                final Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
                if (ringtone != null) {
                    r2tone.setText(ringtone.getTitle(getActivity()));
                } else {
                    r2tone.setText(R.string.errorreceivingringtone);
                }
            } else {
                reminderprefs.edit().putString("r2tone", "").apply();
                Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (defaultUri != null) {
                    final Ringtone defaultRingtone = RingtoneManager.getRingtone(getActivity(), defaultUri);
                    if (defaultRingtone != null) {
                        r2tone.setText(defaultRingtone.getTitle(getActivity()));
                    }
                }
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == 3) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                reminderprefs.edit().putString("r3tone", uri.toString()).apply();
                final Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
                if (ringtone != null) {
                    r3tone.setText(ringtone.getTitle(getActivity()));
                } else {
                    r3tone.setText(R.string.errorreceivingringtone);
                }
            } else {
                reminderprefs.edit().putString("r3tone", "").apply();
                Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (defaultUri != null) {
                    final Ringtone defaultRingtone = RingtoneManager.getRingtone(getActivity(), defaultUri);
                    if (defaultRingtone != null) {
                        r3tone.setText(defaultRingtone.getTitle(getActivity()));
                    }
                }
            }
        }
    }

    private void adjustActionBarColor(Bitmap source) {
        Bright.Luminance bright = Bright.setup(Bright.Config.PERCEIVED | Bright.Config.PERFORMANCE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int x = 0;
        int y = 0;
        int width = size.x;
        int height = 80;

        Bitmap dest = Bitmap.createBitmap(source, x, y, width, height);
        int luminance = bright.brightness(dest);
        /*int textColor = bright.isBright(luminance) ? Color.parseColor("#808080")
                : Color.WHITE;
        Spannable text = new SpannableString(actionBar.getTitle());
        text.setSpan(new ForegroundColorSpan(textColor), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        actionBar.setTitle(text);*/

        FrameLayout actionbarShadow = (FrameLayout) v.findViewById(R.id.actionbarShadow);
        if (bright.isBright(luminance)) {
            actionbarShadow.setVisibility(View.VISIBLE);
        } else {
            actionbarShadow.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        v = inflater.inflate(R.layout.fragment_reminders, viewGroup);

        setup();
    }

    public static Bitmap decodeSampledBitmapFromResource(String filepath,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }
        imageView.setImageDrawable(null);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        String timerpickerAction = reminderprefs.getString("timerpickerAction", "create");
        String reminderNumber = reminderprefs.getString("reminderNumber", "0");
        if (timerpickerAction.equals("create")) {
            String hourStr = String.valueOf(hourOfDay);
            if (hourOfDay < 10) {
                hourStr = "0" + hourStr;
            }
            String minuteStr = String.valueOf(minute);
            if (minute < 10) {
                minuteStr = "0" + minuteStr;
            }
            reminderprefs.edit().putBoolean("r" + reminderNumber, true).apply();
            reminderprefs.edit().putBoolean("r" + reminderNumber + "enabled", true).apply();
            reminderprefs.edit().putString("r" + reminderNumber + "hour", hourStr).apply();
            reminderprefs.edit().putString("r" + reminderNumber + "minute", minuteStr).apply();
            initiateLayout();
            switch (reminderNumber) {
                case "1":
                    setReminder1();
                    break;
                case "2":
                    setReminder2();
                    break;
                case "3":
                    setReminder3();
                    break;
            }
        } else if (timerpickerAction.equals("edit")) {
            String hourStr = String.valueOf(hourOfDay);
            if (hourOfDay < 10) {
                hourStr = "0" + hourStr;
            }
            String minuteStr = String.valueOf(minute);
            if (minute < 10) {
                minuteStr = "0" + minuteStr;
            }
            switch (reminderNumber) {
                case "1":
                    String r1text = hourStr + ":" + minuteStr + " " + getActivity().getString(R.string.uhr);
                    r1time.setText(r1text);
                    reminderprefs.edit().putString("r1hour", hourStr).apply();
                    reminderprefs.edit().putString("r1minute", minuteStr).apply();
                    setReminder1();
                    break;
                case "2":
                    String r2text = hourStr + ":" + minuteStr + " " + getActivity().getString(R.string.uhr);
                    r2time.setText(r2text);
                    reminderprefs.edit().putString("r2hour", hourStr).apply();
                    reminderprefs.edit().putString("r2minute", minuteStr).apply();
                    setReminder2();
                    break;
                case "3":
                    String r3text = hourStr + ":" + minuteStr + " " + getActivity().getString(R.string.uhr);
                    r3time.setText(r3text);
                    reminderprefs.edit().putString("r3hour", hourStr).apply();
                    reminderprefs.edit().putString("r3minute", minuteStr).apply();
                    setReminder3();
                    break;
            }
        }
    }
}
