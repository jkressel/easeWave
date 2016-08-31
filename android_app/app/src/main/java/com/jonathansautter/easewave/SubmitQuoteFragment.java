package com.jonathansautter.easewave;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.devddagnet.bright.lib.Bright;
import com.github.clans.fab.FloatingActionButton;
import com.parse.ParseObject;

import java.io.File;

public class SubmitQuoteFragment extends android.support.v4.app.Fragment {

    private View v;
    private Handler handler = new Handler();
    private int screenwidth, screenheight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_submitquote, container, false);

        setup();

        return v;
    }

    private void setup() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        fixMargins();

        final EditText quote = (EditText) v.findViewById(R.id.editText);
        final EditText author = (EditText) v.findViewById(R.id.editText2);
        final FloatingActionButton send = (FloatingActionButton) v.findViewById(R.id.send);
        final ScrollView card = (ScrollView) v.findViewById(R.id.scrollView2);

        final Animation fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        final Animation fab_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_in);

        SharedPreferences settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);
        if (!settingsprefs.getBoolean("imagebackground", true)) {
            int bottom = card.getPaddingBottom();
            int top = card.getPaddingTop();
            int right = card.getPaddingRight();
            int left = card.getPaddingLeft();
            card.setBackgroundResource(R.drawable.card_bg_solid);
            card.setPadding(left, top, right, bottom);
        }

        card.startAnimation(fade_in);
        card.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                send.startAnimation(fab_in);
                send.setVisibility(View.VISIBLE);
            }
        }, 600);

        setBackgroundImage();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quote.getText().toString().equals("")) {
                    final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert_dialog);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    setDialogSize(dialog);

                    TextView title = (TextView) dialog.findViewById(R.id.title);
                    TextView message = (TextView) dialog.findViewById(R.id.message);
                    TextView ok = (TextView) dialog.findViewById(R.id.ok);
                    TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

                    title.setText(getActivity().getString(R.string.pleaseenteryoursuggestion));
                    message.setVisibility(View.GONE);
                    ok.setText("OK");
                    cancel.setVisibility(View.GONE);

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                    /*AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(getActivity(), style);
                    alertDialogBuilder2.setTitle(R.string.pleaseenteryoursuggestion);
                    alertDialogBuilder2
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                    AlertDialog alertDialog2 = alertDialogBuilder2.create();
                    alertDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog2.show();*/
                } else {
                    if (!isNetworkAvailable()) {
                        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.alert_dialog);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        setDialogSize(dialog);

                        TextView title = (TextView) dialog.findViewById(R.id.title);
                        TextView message = (TextView) dialog.findViewById(R.id.message);
                        TextView ok = (TextView) dialog.findViewById(R.id.ok);
                        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

                        title.setText(getActivity().getString(R.string.error));
                        message.setText(getActivity().getString(R.string.pleasemakesureyourconnected));
                        ok.setText("OK");
                        cancel.setVisibility(View.GONE);

                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();

                        /*AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(getActivity(), style);
                        alertDialogBuilder2.setTitle(R.string.error);
                        alertDialogBuilder2
                                .setMessage(R.string.pleasemakesureyourconnected)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                        AlertDialog alertDialog2 = alertDialogBuilder2.create();
                        alertDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alertDialog2.show();*/
                    } else {
                        ParseObject gameScore = new ParseObject("AppSubmits");
                        gameScore.put("suggestion", quote.getText().toString());
                        gameScore.put("author", author.getText().toString());
                        gameScore.saveInBackground();

                        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.alert_dialog);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        setDialogSize(dialog);

                        TextView title = (TextView) dialog.findViewById(R.id.title);
                        TextView message = (TextView) dialog.findViewById(R.id.message);
                        TextView ok = (TextView) dialog.findViewById(R.id.ok);
                        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

                        title.setText(getActivity().getString(R.string.thankyou));
                        message.setText(getActivity().getString(R.string.sugestionsuccessfullysent));
                        ok.setText("OK");
                        cancel.setVisibility(View.GONE);

                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(quote.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(author.getWindowToken(), 0);
                        dialog.show();
                        quote.setText("");
                        author.setText("");

                        /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), style);
                        alertDialogBuilder.setTitle(R.string.thankyou);

                        alertDialogBuilder
                                .setMessage(R.string.sugestionsuccessfullysent)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alertDialog.show();
                        input.setText("");*/
                    }
                }
            }
        });
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        v = inflater.inflate(R.layout.fragment_submitquote, viewGroup);

        setup();
    }*/

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
