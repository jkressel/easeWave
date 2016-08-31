package com.jonathansautter.easewave;

import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LockscreenWidgetConfigurationActivity extends Activity {

    private int mAppWidgetId = 0;
    private RemoteViews remoteViewsConf;
    private SharedPreferences settingsprefs;
    private int textColor;
    private TextView fontsizetxt;
    private TextView quote, source;
    private ImageButton fontcolorindicator;
    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreenwidget_configuration);
        setResult(RESULT_CANCELED);

        //Date
        Date date = new Date();
        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = dateFormate.format(date);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        remoteViewsConf = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.lockscreenwidget_layout);

        settingsprefs = getSharedPreferences("SETTINGS", 0);

        quote = (TextView) findViewById(R.id.widgetquote);
        source = (TextView) findViewById(R.id.widgetsource);
        final RelativeLayout fontcolorlayout = (RelativeLayout) findViewById(R.id.fontcolorlayout);
        TextView done = (TextView) findViewById(R.id.done);

        fontsizetxt = (TextView) findViewById(R.id.fontsizetxt);
        RelativeLayout fontsizelayout = (RelativeLayout) findViewById(R.id.fontsizelayout);

        fontcolorindicator = (ImageButton) findViewById(R.id.fontcolorindicator);

        GradientDrawable bgShape1 = (GradientDrawable) fontcolorindicator.getBackground();

        textColor = settingsprefs.getInt("lockscreenwidgetTextColor", Color.parseColor("#ffffff"));

        bgShape1.setColor(textColor);

        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();

        ImageView wallpaper = (ImageView) findViewById(R.id.wallpaper);
        wallpaper.setImageDrawable(wallpaperDrawable);

        //widgetbackground.setBackgroundColor(backgroundColor);
        quote.setTextColor(textColor);
        source.setTextColor(textColor);
        //remoteViewsConf.setTextColor(R.id.widgetquote, textColor);
        //remoteViewsConf.setTextColor(R.id.widgetsource, textColor);

        if (isNetworkAvailable()) {
            if (settingsprefs.getString("lastDownloadedQuote", "1991/03/01").equals(today)) {
                loadLastDownloadedQuote();
            } else {
                downloadQuote();
            }
        } else {
            loadLastDownloadedQuote();
        }

        //quote.setText(settingsprefs.getString("quote", "Please make sure you are connected to the internet so that today's quote can be received."));
        //source.setText(Html.fromHtml(settingsprefs.getString("source", "Connection Error")));

        int lockscreenwidgetfontsize = settingsprefs.getInt("lockscreenwidgetfontsize", Math.round(getResources().getDimension(R.dimen.lockscreenwidgetfontsize) / getResources().getDisplayMetrics().density));
        quote.setTextSize(lockscreenwidgetfontsize);
        source.setTextSize(Math.round(lockscreenwidgetfontsize - (lockscreenwidgetfontsize * 0.2)));
        fontsizetxt.setText(String.valueOf(lockscreenwidgetfontsize));

        fontcolorindicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fontcolorlayout.performClick();
            }
        });

        fontcolorlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Color Picker dialog
                final Dialog dialog = new Dialog(LockscreenWidgetConfigurationActivity.this, R.style.myDialogStyle);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.colorpicker_dialog);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                setDialogSize(dialog);

                TextView title = (TextView) dialog.findViewById(R.id.title);
                ColorPicker picker = (ColorPicker) dialog.findViewById(R.id.picker);
                OpacityBar opacityBar = (OpacityBar) dialog.findViewById(R.id.opacitybar);
                SaturationBar saturationBar = (SaturationBar) dialog.findViewById(R.id.saturationbar);
                SVBar svBar = (SVBar) dialog.findViewById(R.id.svbar);
                TextView ok = (TextView) dialog.findViewById(R.id.ok);
                TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

                title.setText(R.string.choosefontcolor);

                picker.addOpacityBar(opacityBar);
                picker.addSaturationBar(saturationBar);
                picker.addSVBar(svBar);
                picker.setOldCenterColor(settingsprefs.getInt("lockscreenwidgetTextColor", Color.parseColor("#ffffff")));
                picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int i) {
                        textColor = i;
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsprefs.edit().putInt("lockscreenwidgetTextColor", textColor).apply();
                        quote.setTextColor(textColor);
                        source.setTextColor(textColor);
                        GradientDrawable bgShape = (GradientDrawable) fontcolorindicator.getBackground();
                        bgShape.setColor(textColor);
                        remoteViewsConf.setTextColor(R.id.widgetquote, textColor);
                        remoteViewsConf.setTextColor(R.id.widgetsource, textColor);
                        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, LockscreenWidgetConfigurationActivity.this, LockScreenWidgetProvider.class);
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mAppWidgetId});
                        sendBroadcast(intent);
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
            }
        });

        fontsizelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                widgetfontsizedialog();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //widgetManager.updateAppWidget(mAppWidgetId, remoteViewsConf);
                // Return RESULT_OK from this activity
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);

                finish();
            }
        });
    }

    private void widgetfontsizedialog() {

        final Dialog dialog = new Dialog(LockscreenWidgetConfigurationActivity.this, R.style.myDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.numberpicker_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        setDialogSize(dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView minus = (TextView) dialog.findViewById(R.id.minus);
        TextView plus = (TextView) dialog.findViewById(R.id.plus);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText);
        TextView save = (TextView) dialog.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

        title.setText("Set widget font size");
        editText.setText(String.valueOf(settingsprefs.getInt("lockscreenwidgetfontsize", Math.round(getResources().getDimension(R.dimen.lockscreenwidgetfontsize) / getResources().getDisplayMetrics().density))));

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(String.valueOf(Integer.parseInt(editText.getText().toString()) - 1));
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(String.valueOf(Integer.parseInt(editText.getText().toString()) + 1));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newsize = Integer.parseInt(editText.getText().toString());
                settingsprefs.edit().putInt("lockscreenwidgetfontsize", newsize).apply();
                fontsizetxt.setText(editText.getText().toString());
                quote.setTextSize(newsize);
                source.setTextSize(Math.round(newsize-(newsize*0.2)));
                // update widgets
                AppWidgetManager man = AppWidgetManager.getInstance(LockscreenWidgetConfigurationActivity.this);
                int[] ids = man.getAppWidgetIds(
                        new ComponentName(LockscreenWidgetConfigurationActivity.this, LockScreenWidgetProvider.class));
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, LockscreenWidgetConfigurationActivity.this, LockScreenWidgetProvider.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                sendBroadcast(intent);
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
    }

    private void setDialogSize(Dialog dialog) {

        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) != Configuration.SCREENLAYOUT_SIZE_LARGE
                && (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) != Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        } else {
            Display display = getWindowManager().getDefaultDisplay();
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

    private void downloadQuote() {
        Calendar cal = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = formatter.format(cal.getTime());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Quotes");
        query.whereEqualTo("Date", today);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    String quote;
                    String source;
                    if (settingsprefs.getString("language", "en").equals("de")) {
                        quote = object.getString("de");
                        source = object.getString("vers");
                    } else {
                        quote = object.getString("en");
                        source = object.getString("verse");
                    }
                    displayQuote(quote, source);
                    settingsprefs.edit().putString("lastDownloadedQuote", today).apply();
                    settingsprefs.edit().putString("quote", quote).apply();
                    settingsprefs.edit().putString("source", source).apply();
                } else {
                    // try to load last from prefs
                    loadLastDownloadedQuote();
                }
            }
        });
    }

    private void loadLastDownloadedQuote() {
        String quote = "Please make sure you are connected to the internet so that today's quote can be received.";
        String source = "Connection Error";
        if (!settingsprefs.getString("quote", "").equals("")) {
            quote = settingsprefs.getString("quote", "");
            source = settingsprefs.getString("source", "");
        }
        displayQuote(quote, source);
    }

    private void displayQuote(String en, String verse) {
        quote.setText(en);
        source.setText(Html.fromHtml(verse));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
