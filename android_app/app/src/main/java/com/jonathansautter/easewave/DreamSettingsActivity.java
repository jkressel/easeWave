package com.jonathansautter.easewave;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;

public class DreamSettingsActivity extends Activity {

    private int backgroundcolor;
    private SharedPreferences settingsprefs;
    private ImageButton backgroundcolorindicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daydream_configuration);

        RelativeLayout nightmodelayout = (RelativeLayout) findViewById(R.id.nightmodelayout);
        RelativeLayout unicoleredbackgroundlayout = (RelativeLayout) findViewById(R.id.unicoloredbackgroundlayout);
        final RelativeLayout backgroundcolorlayout = (RelativeLayout) findViewById(R.id.backgroundcolorlayout);

        final SwitchCompat nightmodeswitch = (SwitchCompat) findViewById(R.id.nightmodeswitch);
        final SwitchCompat unicoloredbackgroundswitch = (SwitchCompat) findViewById(R.id.centerswitch);

        TextView done = (TextView) findViewById(R.id.done);

        backgroundcolorindicator = (ImageButton) findViewById(R.id.backgroundcolorindicator);

        settingsprefs = getSharedPreferences("SETTINGS", 0);

        nightmodeswitch.setChecked(settingsprefs.getBoolean("daydreamnightmode", true));
        unicoloredbackgroundswitch.setChecked(settingsprefs.getBoolean("dreambackground", false));

        GradientDrawable bgShape = (GradientDrawable) backgroundcolorindicator.getBackground();
        bgShape.setColor(settingsprefs.getInt("dreambackgroundcolor", getResources().getColor(R.color.appthemecolor)));

        if (!unicoloredbackgroundswitch.isChecked()) {
            backgroundcolorlayout.setAlpha((float) 0.3);
            backgroundcolorlayout.setEnabled(false);
        } else {
            backgroundcolorlayout.setAlpha(1);
            backgroundcolorlayout.setEnabled(true);
        }

        nightmodelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nightmodeswitch.toggle();
            }
        });

        nightmodeswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsprefs.edit().putBoolean("daydreamnightmode", isChecked).apply();
            }
        });

        unicoleredbackgroundlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unicoloredbackgroundswitch.toggle();
            }
        });

        unicoloredbackgroundswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsprefs.edit().putBoolean("dreambackground", isChecked).apply();
                if (!isChecked) {
                    backgroundcolorlayout.setAlpha((float) 0.3);
                    backgroundcolorlayout.setEnabled(false);
                } else {
                    backgroundcolorlayout.setAlpha(1);
                    backgroundcolorlayout.setEnabled(true);
                }
            }
        });

        backgroundcolorlayout.setOnClickListener(new View.OnClickListener() {
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

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DreamSettingsActivity.this.finish();
            }
        });
    }

    private void colorpickerDialog() {

        final Dialog dialog = new Dialog(DreamSettingsActivity.this, R.style.myDialogStyle);
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
        picker.setOldCenterColor(settingsprefs.getInt("dreambackgroundcolor", getResources().getColor(R.color.appthemecolor)));
        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                backgroundcolor = i;
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsprefs.edit().putInt("dreambackgroundcolor", backgroundcolor).apply();
                GradientDrawable bgShape = (GradientDrawable) backgroundcolorindicator.getBackground();
                bgShape.setColor(backgroundcolor);
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
}
