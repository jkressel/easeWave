package com.jonathansautter.easewave;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devddagnet.bright.lib.Bright;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;

import java.io.File;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private View v;
    private SharedPreferences settingsprefs;
    private TextView languageSummary;
    private RelativeLayout choosebackgroundcolorlayout;
    private SwitchCompat keepscreenonswitch, imagebackgroundswitch;
    private int backgroundcolor;
    private ImageButton backgroundcolorindicator;
    private RelativeLayout toplayout;
    private TextView appfontsizetxt;
    private TextView widgetfontsizetxt;
    private int screenwidth, screenheight;
    private RelativeLayout screenlayout;
    private FrameLayout choosebackgroundcolordivider;
    private SwitchCompat centerwidgetswitch;
    private TextView lockscreenwidgetfontsizetxt;
    private RelativeLayout lockscreenwidgetfontsizelayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_settings, container, false);

        setup();

        return v;
    }

    private void setup() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;

        settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);

        toplayout = (RelativeLayout) v.findViewById(R.id.toplayout);
        screenlayout = (RelativeLayout) v.findViewById(R.id.screenlayout);
        RelativeLayout cards = (RelativeLayout) v.findViewById(R.id.cards);
        RelativeLayout languagelayout = (RelativeLayout) v.findViewById(R.id.languagelayout);
        RelativeLayout keepscreenonlayout = (RelativeLayout) v.findViewById(R.id.keepscreenonlayout);
        RelativeLayout appfontsizelayout = (RelativeLayout) v.findViewById(R.id.appfontsizelayout);
        RelativeLayout widgetfontsizelayout = (RelativeLayout) v.findViewById(R.id.widgetfontsizelayout);
        lockscreenwidgetfontsizelayout = (RelativeLayout) v.findViewById(R.id.lockscreenwidgetfontsizelayout);
        RelativeLayout imagebackgroundlayout = (RelativeLayout) v.findViewById(R.id.imagebackgroundlayout);
        choosebackgroundcolorlayout = (RelativeLayout) v.findViewById(R.id.choosebackgroundcolorlayout);
        choosebackgroundcolordivider = (FrameLayout) v.findViewById(R.id.choosebackgroundcolordivider);
        RelativeLayout centerwidgetlayout = (RelativeLayout) v.findViewById(R.id.centerwidgetlayout);
        RelativeLayout aboutlayout = (RelativeLayout) v.findViewById(R.id.aboutlayout);
        RelativeLayout contactlayout = (RelativeLayout) v.findViewById(R.id.contactlayout);
        RelativeLayout websitelayout = (RelativeLayout) v.findViewById(R.id.websitelayout);

        keepscreenonswitch = (SwitchCompat) v.findViewById(R.id.centerswitch);
        imagebackgroundswitch = (SwitchCompat) v.findViewById(R.id.imagebackgroundswitch);
        centerwidgetswitch = (SwitchCompat) v.findViewById(R.id.centerwidgetswitch);

        languageSummary = (TextView) v.findViewById(R.id.language_summary);
        appfontsizetxt = (TextView) v.findViewById(R.id.appfontsizetxt);
        widgetfontsizetxt = (TextView) v.findViewById(R.id.widgetfontsizetxt);
        lockscreenwidgetfontsizetxt = (TextView) v.findViewById(R.id.lockscreenwidgetfontsizetxt);
        backgroundcolorindicator = (ImageButton) v.findViewById(R.id.backgroundcolorindicator);

        Animation fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);

        fixMargins();
        setBackgroundImage();
        initialize();

        cards.startAnimation(fade_in);
        cards.setVisibility(View.VISIBLE);

        languagelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageDialog();
            }
        });

        keepscreenonlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepscreenonswitch.toggle();
            }
        });

        appfontsizelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appfontsizedialog();
            }
        });

        widgetfontsizelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                widgetfontsizedialog();
            }
        });

        lockscreenwidgetfontsizelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockscreenwidgetfontsizedialog();
            }
        });

        imagebackgroundlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagebackgroundswitch.toggle();
            }
        });

        keepscreenonswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsprefs.edit().putBoolean("keepscreenon", isChecked).apply();
                if (isChecked) {
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });

        imagebackgroundswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsprefs.edit().putBoolean("imagebackground", isChecked).apply();
                if (isChecked) {
                    setBackgroundImage();
                    screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
                    choosebackgroundcolorlayout.setVisibility(View.GONE);
                    choosebackgroundcolordivider.setVisibility(View.GONE);
                } else {
                    screenlayout.setBackgroundColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
                    ImageView iv = (ImageView) v.findViewById(R.id.imageView);
                    iv.setImageResource(android.R.color.transparent);
                    choosebackgroundcolorlayout.setVisibility(View.VISIBLE);
                    choosebackgroundcolordivider.setVisibility(View.VISIBLE);
                    RelativeLayout card1 = (RelativeLayout) v.findViewById(R.id.card1);
                    RelativeLayout card2 = (RelativeLayout) v.findViewById(R.id.card2);
                    int bottom = card1.getPaddingBottom();
                    int top = card1.getPaddingTop();
                    int right = card1.getPaddingRight();
                    int left = card1.getPaddingLeft();
                    card1.setBackgroundResource(R.drawable.card_bg_solid);
                    card2.setBackgroundResource(R.drawable.card_bg_solid);
                    card1.setPadding(left, top, right, bottom);
                    card2.setPadding(left, top, right, bottom);
                }
            }
        });

        choosebackgroundcolorlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorpickerDialog();
            }
        });

        backgroundcolorindicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorpickerDialog();
            }
        });

        centerwidgetlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerwidgetswitch.toggle();
            }
        });

        centerwidgetswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsprefs.edit().putBoolean("widgetCentered", isChecked).apply();
                // update widgets
                AppWidgetManager man = AppWidgetManager.getInstance(getActivity());
                int[] ids = man.getAppWidgetIds(
                        new ComponentName(getActivity(), WidgetProvider.class));
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, getActivity(), WidgetProvider.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                getActivity().sendBroadcast(intent);
            }
        });

        aboutlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Intro.class);
                startActivity(intent);
            }
        });

        contactlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "contact@easewave.com", null));
                if (emailIntent.resolveActivity(getActivity().getPackageManager()) == null) { // no email client installed
                    Toast.makeText(getActivity().getApplicationContext(), R.string.noemailclient, Toast.LENGTH_LONG).show();
                } else {
                    startActivity(Intent.createChooser(emailIntent, getActivity().getString(R.string.sendemailvia)));
                }
            }
        });

        websitelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://easewave.com"));
                startActivity(browserIntent);
            }
        });
    }

    private void widgetfontsizedialog() {

        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
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

        title.setText(R.string.setwidgetfontsize);
        editText.setText(String.valueOf(settingsprefs.getInt("widgetfontsize", Math.round(getResources().getDimension(R.dimen.widgetfontsize) / getResources().getDisplayMetrics().density))));

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
                settingsprefs.edit().putInt("widgetfontsize", Integer.parseInt(editText.getText().toString())).apply();
                widgetfontsizetxt.setText(editText.getText().toString());
                // update widgets
                AppWidgetManager man = AppWidgetManager.getInstance(getActivity());
                int[] ids = man.getAppWidgetIds(
                        new ComponentName(getActivity(), WidgetProvider.class));
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, getActivity(), WidgetProvider.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                getActivity().sendBroadcast(intent);
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

    private void lockscreenwidgetfontsizedialog() {

        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
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

        title.setText(R.string.setwidgetfontsize);
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
                settingsprefs.edit().putInt("lockscreenwidgetfontsize", Integer.parseInt(editText.getText().toString())).apply();
                lockscreenwidgetfontsizetxt.setText(editText.getText().toString());
                // update widgets
                AppWidgetManager man = AppWidgetManager.getInstance(getActivity());
                int[] ids = man.getAppWidgetIds(
                        new ComponentName(getActivity(), LockScreenWidgetProvider.class));
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, getActivity(), LockScreenWidgetProvider.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                getActivity().sendBroadcast(intent);
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

    private void appfontsizedialog() {

        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
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

        title.setText(R.string.setappfontsize);
        editText.setText(String.valueOf(settingsprefs.getInt("appfontsize", Math.round(getResources().getDimension(R.dimen.appfontsize) / getResources().getDisplayMetrics().density))));

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
                settingsprefs.edit().putInt("appfontsize", Integer.parseInt(editText.getText().toString())).apply();
                appfontsizetxt.setText(editText.getText().toString());
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

    private void colorpickerDialog() {

        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
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

        title.setText(R.string.choosebackgroundcolor);

        opacityBar.setVisibility(View.GONE);
        //picker.addOpacityBar(opacityBar);
        picker.addSaturationBar(saturationBar);
        picker.addSVBar(svBar);
        picker.setOldCenterColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                backgroundcolor = i;
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsprefs.edit().putInt("backgroundcolor", backgroundcolor).apply();
                GradientDrawable bgShape = (GradientDrawable) backgroundcolorindicator.getBackground();
                bgShape.setColor(backgroundcolor);
                screenlayout.setBackgroundColor(backgroundcolor);
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

    private void initialize() {

        if (settingsprefs.getString("language", "en").equals("en")) {
            languageSummary.setText("English");
        } else if (settingsprefs.getString("language", "en").equals("de")) {
            languageSummary.setText("Deutsch");
        }

        keepscreenonswitch.setChecked(settingsprefs.getBoolean("keepscreenon", false));
        imagebackgroundswitch.setChecked(settingsprefs.getBoolean("imagebackground", true));
        centerwidgetswitch.setChecked(settingsprefs.getBoolean("widgetCentered", false));
        GradientDrawable bgShape = (GradientDrawable) backgroundcolorindicator.getBackground();
        bgShape.setColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
        appfontsizetxt.setText(String.valueOf(settingsprefs.getInt("appfontsize", Math.round(getResources().getDimension(R.dimen.appfontsize) / getResources().getDisplayMetrics().density))));
        widgetfontsizetxt.setText(String.valueOf(settingsprefs.getInt("widgetfontsize", Math.round(getResources().getDimension(R.dimen.widgetfontsize) / getResources().getDisplayMetrics().density))));
        lockscreenwidgetfontsizetxt.setText(String.valueOf(settingsprefs.getInt("lockscreenwidgetfontsize", Math.round(getResources().getDimension(R.dimen.lockscreenwidgetfontsize) / getResources().getDisplayMetrics().density))));

        if (settingsprefs.getBoolean("imagebackground", true)) {
            choosebackgroundcolorlayout.setVisibility(View.GONE);
            choosebackgroundcolordivider.setVisibility(View.GONE);
        } else {
            choosebackgroundcolorlayout.setVisibility(View.VISIBLE);
            choosebackgroundcolordivider.setVisibility(View.VISIBLE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FrameLayout lockscreenfontsizedivider = (FrameLayout) v.findViewById(R.id.lockscreenfontsizedivider);
            lockscreenfontsizedivider.setVisibility(View.GONE);
            lockscreenwidgetfontsizelayout.setVisibility(View.GONE);
        }
    }

    private void languageDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.language_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        setDialogSize(dialog);

        final RadioButton en = (RadioButton) dialog.findViewById(R.id.en);
        final RadioButton de = (RadioButton) dialog.findViewById(R.id.de);

        if (settingsprefs.getString("language", "en").equals("en")) {
            en.setChecked(true);
        } else if (settingsprefs.getString("language", "en").equals("de")) {
            de.setChecked(true);
        }

        RadioButton.OnClickListener toggleHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = 0;
                en.setChecked(false);
                de.setChecked(false);

                switch (v.getId()) {
                    case R.id.en:
                        position = 0;
                        en.setChecked(true);
                        break;
                    case R.id.de:
                        position = 1;
                        de.setChecked(true);
                        break;
                }
                dialog.dismiss();
                restartDialog(position);
            }
        };

        en.setOnClickListener(toggleHandler);
        de.setOnClickListener(toggleHandler);

        dialog.show();
    }

    private void restartDialog(int position) {

        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        setDialogSize(dialog);

        if (position == 0) {
            settingsprefs.edit().putString("language", "en").apply();
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(config,
                    getActivity().getBaseContext().getResources().getDisplayMetrics());
        }
        if (position == 1) {
            settingsprefs.edit().putString("language", "de").apply();
            Locale locale = new Locale("de");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(config,
                    getActivity().getBaseContext().getResources().getDisplayMetrics());
        }

        settingsprefs.edit().putString("lastDownloadedQuote", "1991/03/01").apply();

        String languagecode = settingsprefs.getString("language", "en");
        switch (languagecode) {
            case "en":
                languageSummary.setText("English");
                break;
            case "de":
                languageSummary.setText("Deutsch");
                break;
        }

        TextView now = (TextView) dialog.findViewById(R.id.ok);
        TextView later = (TextView) dialog.findViewById(R.id.cancel);

        now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setBackgroundImage() {
        if (settingsprefs.getBoolean("imagebackground", true)) {
            RelativeLayout card1 = (RelativeLayout) v.findViewById(R.id.card1);
            RelativeLayout card2 = (RelativeLayout) v.findViewById(R.id.card2);
            int bottom = card1.getPaddingBottom();
            int top = card1.getPaddingTop();
            int right = card1.getPaddingRight();
            int left = card1.getPaddingLeft();
            card1.setBackgroundResource(R.drawable.card_bg);
            card2.setBackgroundResource(R.drawable.card_bg);
            card1.setPadding(left, top, right, bottom);
            card2.setPadding(left, top, right, bottom);
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

            RelativeLayout card1 = (RelativeLayout) v.findViewById(R.id.card1);
            RelativeLayout card2 = (RelativeLayout) v.findViewById(R.id.card2);
            int bottom = card1.getPaddingBottom();
            int top = card1.getPaddingTop();
            int right = card1.getPaddingRight();
            int left = card1.getPaddingLeft();
            card1.setBackgroundResource(R.drawable.card_bg_solid);
            card2.setBackgroundResource(R.drawable.card_bg_solid);
            card1.setPadding(left, top, right, bottom);
            card2.setPadding(left, top, right, bottom);
        }
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
            toplayout.setLayoutParams(params);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        v = inflater.inflate(R.layout.fragment_settings, viewGroup);

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
    public void onDestroy(){
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
}
