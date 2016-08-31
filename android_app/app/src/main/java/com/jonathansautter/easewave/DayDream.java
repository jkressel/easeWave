package com.jonathansautter.easewave;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.text.Html;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class DayDream extends DreamService implements ViewSwitcher.ViewFactory {

    private SharedPreferences settingsprefs;
    private String today;
    private TextView quote, source;
    private Animation fade_in;
    private Animation fade_in2;
    private Animation aniIn;
    private Animation aniOut;
    private ImageSwitcher imageSwitcher;
    private boolean imageset;
    private boolean noBGImagefound;
    private boolean noquote;
    private String filepath;
    private boolean newimage;
    private Handler handler = new Handler();
    private boolean colorbackground;
    private int screenwidth, screenheight;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        setInteractive(false);
        setFullscreen(true);

        setContentView(R.layout.daydream);

        setup();
    }

    private void setup() {

        setLocale();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;

        settingsprefs = getSharedPreferences("SETTINGS", 0);
        colorbackground = settingsprefs.getBoolean("dreambackground", false);

        //Brightness
        if (settingsprefs.getBoolean("daydreamnightmode", true)) {
            setScreenBright(false);
        } else {
            setScreenBright(true);
        }

        Calendar cal = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = formatter.format(cal.getTime());

        quote = (TextView) findViewById(R.id.quote);
        source = (TextView) findViewById(R.id.source);

        Typeface light = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        Typeface regular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fade_in2 = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        quote.setTypeface(light);
        source.setTypeface(regular);

        int appfontsize = settingsprefs.getInt("appfontsize", Math.round(getResources().getDimension(R.dimen.appfontsize) / getResources().getDisplayMetrics().density));
        quote.setTextSize(appfontsize);
        source.setTextSize(Math.round(appfontsize - (appfontsize * 0.3)));

        aniIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        aniIn.setDuration(3000);
        aniOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        aniOut.setDuration(3000);

        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        //imageSwitcher.setInAnimation(aniIn);
        //imageSwitcher.setOutAnimation(aniOut);
        imageSwitcher.setFactory(this);

        loadQuote();

    }

    private class AsyncTaskDownloadImage extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
            File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File path = new File(sdCard + "/easeWave");
            path.mkdirs();
            String filename = "background.jpg";
            File file = new File(path, filename);
            File imgFile = new File(file.getPath());
            try {
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    Drawable drawable = new BitmapDrawable(getResources(), myBitmap);
                    if (!imageset) {
                        imageSwitcher.setImageDrawable(drawable);
                        imageset = true;
                    }
                    noBGImagefound = false;
                } else {
                    noBGImagefound = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*progressBar.setProgress(0);
            progressBar.startAnimation(fade_in2);
            progressBar.setVisibility(View.VISIBLE);*/
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // check if connected to internet
                if (isNetworkAvailable()) {
                    // check if new colorbackground is available
                    URLConnection connection = new URL("http://easewave.com/img/home_bg_320.jpg").openConnection();
                    //String lastModifiedBG = connection.getHeaderField("Last-Modified");
                    String lastDownloadedBG = settingsprefs.getString("lastDownloadedBG", "Sun, 01 Mar 1991 12:00:00 GMT");
                    long date = connection.getLastModified();
                    SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                    Date lastModifiedBGDate = new Date(date);
                    String lastModifiedBG = sdf.format(lastModifiedBGDate);
                    Date lastDownloadedBGDate;
                    try {
                        //lastModifiedBGDate = sdf.parse(lastModifiedBG);
                        lastDownloadedBGDate = sdf.parse(lastDownloadedBG);
                        if (lastModifiedBGDate.getTime() > lastDownloadedBGDate.getTime() || noBGImagefound) {
                            // calculate image size version
                            Display display = getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            int width = size.x;
                            int height = size.y;
                            int largerSide;
                            if (width > height) {
                                largerSide = width;
                            } else {
                                largerSide = height;
                            }
                            URL url = null;
                            if (largerSide > 960) {
                                url = new URL("http://easewave.com/img/home_bg.jpg");
                                //Log.i("ImageVersion", "1920");
                            } else if (largerSide > 640) {
                                url = new URL("http://easewave.com/img/home_bg_960.jpg");
                                //Log.i("ImageVersion", "960");
                            } else if (largerSide > 320) {
                                url = new URL("http://easewave.com/img/home_bg_640.jpg");
                                //Log.i("ImageVersion", "640");
                            } else if (largerSide <= 320) {
                                url = new URL("http://easewave.com/img/home_bg_320.jpg");
                                //Log.i("ImageVersion", "320");
                            }
                            assert url != null;
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.setDoOutput(true);
                            urlConnection.connect();
                            File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
                            File path = new File(sdCard + "/easeWave");
                            path.mkdirs();
                            String filename = "background.jpg";
                            //Log.i("Local filename", "" + filename);
                            File file = new File(path, filename);
                            if (file.createNewFile()) {
                                file.createNewFile();
                            }
                            // create nomedia file
                            String nomedia = ".nomedia";
                            File nomedia_file = new File(path, nomedia);
                            if (!nomedia_file.exists()){
                                try {
                                    nomedia_file.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            FileOutputStream fileOutput = new FileOutputStream(file);
                            InputStream inputStream = urlConnection.getInputStream();
                            int totalSize = urlConnection.getContentLength();
                            //progressBar.setMax(totalSize);
                            int downloadedSize = 0;
                            byte[] buffer = new byte[1024];
                            int bufferLength;
                            while ((bufferLength = inputStream.read(buffer)) > 0) {
                                fileOutput.write(buffer, 0, bufferLength);
                                downloadedSize += bufferLength;
                                //progressBar.setProgress(downloadedSize);
                                //Log.i("Progress", "downloadedSize: " + downloadedSize + "  totalSize: " + totalSize);
                            }
                            fileOutput.close();
                            if (downloadedSize == totalSize) {
                                filepath = file.getPath();
                                settingsprefs.edit().putString("lastDownloadedBG", lastModifiedBG).apply();
                                newimage = true;
                            }
                        } else {
                            newimage = false;
                        }
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    newimage = false;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                filepath = null;
                e.printStackTrace();
            }
            //Log.i("filepath:", " " + filepath);
            return filepath;
        }

        @Override
        protected void onPostExecute(String filepath) {
            // execution of result of Long time consuming operation
            /*if (progressBar.isShown()) {
                progressBar.startAnimation(fade_out);
                progressBar.setVisibility(View.INVISIBLE);
            }*/
            settingsprefs.edit().putString("lastDownloadedImage", today).apply();
            if (newimage) {
                if (filepath != null) {
                    File imgFile = new File(filepath);
                    if (imgFile.exists()) {
                        //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        //Drawable drawable = new BitmapDrawable(getResources(), myBitmap);
                        Bitmap myBitmap = decodeSampledBitmapFromResource(imgFile.getAbsolutePath(), screenwidth, screenheight);
                        Drawable drawable = new BitmapDrawable(getResources(), myBitmap);
                        imageSwitcher.setInAnimation(aniIn);
                        imageSwitcher.setOutAnimation(aniOut);
                        imageSwitcher.setImageDrawable(drawable);
                    }
                }
            }
        }
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    private void loadQuote() {
        // load once a day
        if (isNetworkAvailable()) {
            if (settingsprefs.getString("lastDownloadedQuote", "1991/03/01").equals(today)) {
                loadLastDownloadedQuote();
                if (colorbackground) {
                    setColorBackground();
                } else {
                    if (settingsprefs.getString("lastDownloadedImage", "1991/03/01").equals(today)) {
                        loadLastDownloadedBackgroundImage();
                    } else {
                        // check for new background image and download if availible
                        AsyncTaskDownloadImage runner = new AsyncTaskDownloadImage();
                        runner.execute();
                    }
                }
            } else {
                downloadQuote();
                if (colorbackground) {
                    setColorBackground();
                } else {
                    // check for new background image and download if availible
                    AsyncTaskDownloadImage runner = new AsyncTaskDownloadImage();
                    runner.execute();
                }
            }
        } else {
            loadLastDownloadedQuote();
            if (colorbackground) {
                setColorBackground();
            } else {
                loadLastDownloadedBackgroundImage();
            }
        }
    }

    private void setColorBackground() {
        int backgroundcolor = settingsprefs.getInt("dreambackgroundcolor", getResources().getColor(R.color.appthemecolor));
        imageSwitcher.setVisibility(View.INVISIBLE);
        RelativeLayout toplayout = (RelativeLayout) findViewById(R.id.toplayout);
        toplayout.setBackgroundColor(backgroundcolor);
        imageset = true;
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
                    noquote = false;
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
                    updateWidget();
                } else {
                    noquote = true;
                    // try to load last from prefs
                    loadLastDownloadedQuote();
                }
            }
        });
    }

    private void updateWidget() {
        Intent intent = new Intent(DayDream.this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    private void loadLastDownloadedQuote() {
        String quote = "";
        String source = "";
        if (!settingsprefs.getString("quote", "").equals("")) {
            quote = settingsprefs.getString("quote", "");
            source = settingsprefs.getString("source", "");
            noquote = false;
        } else {
            noquote = true;
        }
        displayQuote(quote, source);
    }

    private void loadLastDownloadedBackgroundImage() {
        File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File path = new File(sdCard + "/easeWave");
        path.mkdirs();
        String filename = "background.jpg";
        File file = new File(path, filename);
        File imgFile = new File(file.getPath());
        try {
            if (imgFile.exists()) {
                //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //Drawable drawable = new BitmapDrawable(getResources(), myBitmap);
                Bitmap myBitmap = decodeSampledBitmapFromResource(imgFile.getAbsolutePath(), screenwidth, screenheight);
                Drawable drawable = new BitmapDrawable(getResources(), myBitmap);
                if (!imageset) {
                    imageSwitcher.setImageDrawable(drawable);
                    imageset = true;
                }
                noBGImagefound = false;
            } else {
                if (isNetworkAvailable()) {
                    AsyncTaskDownloadImage runner = new AsyncTaskDownloadImage();
                    runner.execute();
                    noBGImagefound = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayQuote(String en, String verse) {
        if (noquote) {
            source.setText(R.string.unabletoreceivequote);
            source.startAnimation(fade_in2);
            source.setVisibility(View.VISIBLE);
        } else {
            /*int bgColor = 0xccffffff;
            if (settingsprefs.getBoolean("dreambackground", false)) {
                bgColor = 0xffffffff;
            }*/
            //Spannable spanna = new SpannableString(en);
            //spanna.setSpan(new BackgroundColorSpan(bgColor), 0, en.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            quote.setText(en);
            source.setText(Html.fromHtml(verse));
            RelativeLayout quote_wrap = (RelativeLayout) findViewById(R.id.quote_wrap);
            quote_wrap.startAnimation(fade_in);
            quote_wrap.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    source.startAnimation(fade_in2);
                    source.setVisibility(View.VISIBLE);
                }
            }, 500);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    private void setLocale() {
        String defaultLocale = Locale.getDefault().getLanguage();
        if (settingsprefs.getString("language", defaultLocale).equals("de")) {
            settingsprefs.edit().putString("language", "de").apply();
            Locale locale = new Locale("de");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else if (settingsprefs.getString("language", defaultLocale).equals("en")) {
            settingsprefs.edit().putString("language", "en").apply();
            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        } else { // old setting from older version
            settingsprefs.edit().putString("language", defaultLocale).apply();
            if (defaultLocale.equals("de")) {
                settingsprefs.edit().putString("language", "de").apply();
                Locale locale = new Locale("de");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            } else {
                settingsprefs.edit().putString("language", "en").apply();
                Locale locale = new Locale("en");
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            }
        }
    }
}
