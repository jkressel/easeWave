package com.jonathansautter.easewave;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.devddagnet.bright.lib.Bright;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Map;

public class QuoteFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ViewSwitcher.ViewFactory {

    /*private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;*/
    private View v;
    private TextView quote, source;
    private SwipeRefreshLayout swipeLayout;
    private String today;
    private Handler handler = new Handler();
    private Animation fade_in, fade_in2, fade_in3, fade_out3, fab_menu_in;
    private FloatingActionMenu floatingButton;
    private String filepath;
    private ProgressBar progressBar;
    private Animation fade_out;
    private SharedPreferences settingsprefs;
    private boolean newimage;
    private ImageSwitcher imageSwitcher;
    private Animation aniIn, aniOut;
    private SharedPreferences favsprefs;
    private FloatingActionButton floatingActionStar;
    private boolean noBGImagefound;
    private boolean noquote;
    private boolean imageset;
    private static final int MENU_SYNC_ERROR = Menu.FIRST;
    private boolean syncError;
    private RelativeLayout screenlayout;
    private TextView watermark;
    private int screenwidth, screenheight;
    private SharedPreferences reminderprefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_main, container, false);

        setup();

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void setup() {

        fixMargins();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;

        settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);
        favsprefs = getActivity().getSharedPreferences("FAVS", 0);
        reminderprefs = getActivity().getSharedPreferences("REMINDER", 0);

        Calendar cal = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = formatter.format(cal.getTime());

        screenlayout = (RelativeLayout) v.findViewById(R.id.screenlayout);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(QuoteFragment.this);
        swipeLayout.setColorSchemeColors(R.color.primary, R.color.half_black);

        watermark = (TextView) v.findViewById(R.id.watermark);

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        quote = (TextView) v.findViewById(R.id.quote);
        source = (TextView) v.findViewById(R.id.source);

        floatingButton = (FloatingActionMenu) v.findViewById(R.id.menu);
        floatingActionStar = (FloatingActionButton) v.findViewById(R.id.menu_item1);
        final FloatingActionButton floatingActionShare = (FloatingActionButton) v.findViewById(R.id.menu_item2);
        final FloatingActionButton floatingActionShareImage = (FloatingActionButton) v.findViewById(R.id.menu_item3);

        if (!settingsprefs.getBoolean("firstlaunchfab", true)) {
            floatingActionShare.setLabelVisibility(View.GONE);
            floatingActionStar.setLabelVisibility(View.GONE);
            floatingActionShareImage.setLabelVisibility(View.GONE);
        }

        final Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        Typeface regular = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fade_in2 = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fade_in3 = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        fade_out3 = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        fab_menu_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_menu_in);

        quote.setTypeface(light);
        source.setTypeface(regular);

        int appfontsize = settingsprefs.getInt("appfontsize", Math.round(getResources().getDimension(R.dimen.appfontsize) / getResources().getDisplayMetrics().density));
        quote.setTextSize(appfontsize);
        source.setTextSize(Math.round(appfontsize - (appfontsize * 0.3)));

        aniIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        aniIn.setDuration(3000);
        aniOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        aniOut.setDuration(3000);

        imageSwitcher = (ImageSwitcher) v.findViewById(R.id.imageSwitcher);
        //imageSwitcher.setInAnimation(aniIn);
        //imageSwitcher.setOutAnimation(aniOut);
        imageSwitcher.setFactory(this);

        floatingButton.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsprefs.getBoolean("firstlaunchfab", true)) {
                    settingsprefs.edit().putBoolean("firstlaunchfab", false).apply();
                }
                floatingButton.toggle(true);
            }
        });

        floatingActionShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });

        floatingActionShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = quote.getText().toString() + " -" + source.getText().toString() + "\n\n" + getActivity().getString(R.string.onerefreshingquoteeveryday);
                //floatingButton.toggle();
                floatingButton.toggle(true);
                //Uri imageUri = Uri.parse("file://\" + \"/sdcard/easeWave/logo.png");
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "freshen up your brain");
                //shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getActivity().getString(R.string.sharewith)));
            }
        });

        floatingActionStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatingActionStar.getColorNormal() == getResources().getColor(R.color.faved)) {
                    Map<String, ?> keys = favsprefs.getAll();
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                        String quoteStr = entry.getValue().toString().split("\\}")[0];
                        if (quoteStr.equals(quote.getText().toString())) {
                            favsprefs.edit().remove(entry.getKey()).apply();
                        }
                    }
                    //String date = settingsprefs.getString("lastDownloadedQuoteDate", "");
                    //favsprefs.edit().remove(date).apply();
                    floatingActionStar.setColorNormal(getResources().getColor(R.color.fab));
                    //floatingActionStar.setIcon(R.drawable.ic_fab_star);
                    floatingActionStar.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fab_star));
                } else {
                    String combined = quote.getText().toString() + "}" + source.getText().toString();
                    //String date = settingsprefs.getString("lastDownloadedQuoteDate", "");
                    favsprefs.edit().putString(String.valueOf(System.currentTimeMillis()), combined).apply();
                    //favsprefs.edit().putString(quote.getText().toString(), source.getText().toString()).apply();
                    floatingActionStar.setColorNormal(getResources().getColor(R.color.faved));
                    //floatingActionStar.setIcon(R.drawable.ic_fab_unstar);
                    floatingActionStar.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fab_unstar));
                }
                //floatingButton.toggle();
            }
        });

        loadQuote();

    }

    private void shareImage() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkReadWritePermissions(3);
        } else {
            File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File path = new File(sdCard + "/easeWave");
            path.mkdirs();

            //floatingButton.toggle();
            floatingButton.toggle(true);
            floatingButton.setVisibility(View.INVISIBLE);
            watermark.setVisibility(View.VISIBLE);
            Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
            watermark.setTypeface(light);
            watermark.setText("easeWave.com");
            adjustWatermarkColor();

            String name = quote.getText().toString();
            final String filename = name.substring(0, name.length() - 1);
            Boolean saveerror = false;
            screenlayout.setDrawingCacheEnabled(true);
            Bitmap bitmap = screenlayout.getDrawingCache();
            File file = new File(sdCard + "/easeWave/" + filename + ".jpg");
            try {
                if (!file.exists()) {
                    file.createNewFile();

                    FileOutputStream ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, ostream);

                    ostream.close();
                    //layout.invalidate();
                } else if (file.exists()) {
                    saveerror = true;
                    Toast toast = Toast.makeText(getActivity(), R.string.filealreadyexists, Toast.LENGTH_LONG);
                    toast.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                screenlayout.setDrawingCacheEnabled(false);
                floatingButton.setVisibility(View.VISIBLE);
                watermark.setVisibility(View.INVISIBLE);

                if (!saveerror) {
                    Toast.makeText(getActivity(), R.string.imagesavedtogallery, Toast.LENGTH_SHORT).show();
                    // share image intent
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/jpeg");
                    String imagePath = sdCard + "/easeWave/" + filename + ".jpg";
                    File imageFileToShare = new File(imagePath);
                    Uri capturedImage = null;
                    try {
                        capturedImage = Uri.parse(
                                android.provider.MediaStore.Images.Media.insertImage(
                                        getActivity().getContentResolver(),
                                        imageFileToShare.getAbsolutePath(), null, null));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    share.putExtra(Intent.EXTRA_STREAM, capturedImage);
                    startActivity(Intent.createChooser(share, getActivity().getString(R.string.shareimage)));

                    MediaScannerConnection.scanFile(getActivity(), new String[]{sdCard + "/easeWave/" + filename + ".jpg"}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            // TODO: working?
                            Intent intent = new Intent("finish_activity");
                            getActivity().sendBroadcast(intent);
                            //lastimage = uri;
                            //lastimageprefs.edit().putString("lastimage", lastimage.toString()).commit();
                        }
                    });
                }
            }
        }
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    private class AsyncTaskDownloadImage extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {

            // Things to be done before execution of long running operation. For
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
                        adjustActionBarColor(myBitmap, false);
                    }
                    noBGImagefound = false;
                } else {
                    noBGImagefound = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            progressBar.setProgress(0);
            progressBar.startAnimation(fade_in2);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // check if connected to internet
                if (isNetworkAvailable()) {
                    // check if new background is available
                    URLConnection connection = new URL("http://easewave.com/img/home_bg_320.jpg").openConnection();
                    //String lastModifiedBG = connection.getHeaderField("Last-Modified");
                    long date = connection.getLastModified();
                    String lastDownloadedBG = settingsprefs.getString("lastDownloadedBG", "Sun, 01 Mar 1991 12:00:00 GMT");
                    //Log.i("LastModifiedBG", "" + lastModifiedBG);
                    //Log.i("LastDownloadedBG", "" + lastDownloadedBG);
                    SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                    Date lastModifiedBGDate = new Date(date);
                    String lastModifiedBG = sdf.format(lastModifiedBGDate);
                    Date lastDownloadedBGDate;
                    try {
                        //lastModifiedBGDate = sdf.parse(lastModifiedBG);
                        lastDownloadedBGDate = sdf.parse(lastDownloadedBG);

                        if (lastModifiedBGDate.getTime() > lastDownloadedBGDate.getTime() || noBGImagefound) {
                            // calculate image size version
                            if (isAdded()) {
                                Display display = getActivity().getWindowManager().getDefaultDisplay();
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
                                if (!nomedia_file.exists()) {
                                    try {
                                        nomedia_file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                FileOutputStream fileOutput = new FileOutputStream(file);
                                InputStream inputStream = urlConnection.getInputStream();
                                int totalSize = urlConnection.getContentLength();
                                publishProgress(0, totalSize);
                                int downloadedSize = 0;
                                byte[] buffer = new byte[1024];
                                int bufferLength;
                                while ((bufferLength = inputStream.read(buffer)) > 0) {
                                    fileOutput.write(buffer, 0, bufferLength);
                                    downloadedSize += bufferLength;
                                    publishProgress(downloadedSize);
                                    //Log.i("Progress", "downloadedSize: " + downloadedSize + "  totalSize: " + totalSize);
                                }
                                fileOutput.close();
                                if (downloadedSize == totalSize) {
                                    filepath = file.getPath();
                                    settingsprefs.edit().putString("lastDownloadedBG", lastModifiedBG).apply();
                                    newimage = true;
                                }
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
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values.length > 1) {
                progressBar.setMax(values[1]);
            }
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String filepath) {
            if (isAdded()) {
                // execution of result of Long time consuming operation
                settingsprefs.edit().putString("lastDownloadedImage", today).apply();
                progressBar.startAnimation(fade_out);
                progressBar.setVisibility(View.INVISIBLE);
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
                            adjustActionBarColor(myBitmap, true);
                        }
                    }
                }
            }
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
            RelativeLayout toplayout = (RelativeLayout) v.findViewById(R.id.toplayout);
            toplayout.setLayoutParams(params);
        }
    }

    @Override
    public void onRefresh() {
        if (isNetworkAvailable()) {
            downloadQuote();
            if (settingsprefs.getBoolean("imagebackground", true)) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    checkReadWritePermissions(1);
                } else {
                    AsyncTaskDownloadImage runner = new AsyncTaskDownloadImage();
                    runner.execute();
                }
            }
        } else {
            if (!settingsprefs.getString("lastDownloadedQuote", "1991/03/01").equals(today)) {
                syncError = true;
                getActivity().invalidateOptionsMenu();
                //Log.d("easeWave", "sync error");
            }
            swipeLayout.setRefreshing(false);
        }
    }

    private void loadQuote() {
        // load once a day
        if (isNetworkAvailable()) {
            if (settingsprefs.getString("lastDownloadedQuote", "1991/03/01").equals(today)) {
                syncError = false;
                loadLastDownloadedQuote();
                if (settingsprefs.getBoolean("imagebackground", true)) {
                    screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
                    if (settingsprefs.getString("lastDownloadedImage", "1991/03/01").equals(today)) {
                        loadLastDownloadedBackgroundImage();
                    } else {
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            checkReadWritePermissions(1);
                        } else {
                            // check for new background image and download if availible
                            AsyncTaskDownloadImage runner = new AsyncTaskDownloadImage();
                            runner.execute();
                        }
                    }
                } else {
                    //screenlayout.setBackgroundColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
                    screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
                    ImageSwitcher is = (ImageSwitcher) v.findViewById(R.id.imageSwitcher);
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    Bitmap image = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
                    image.eraseColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
                    Drawable d = new BitmapDrawable(getResources(), image);
                    is.setImageDrawable(d);
                    adjustActionBarColor(image, false);
                }
            } else {
                downloadQuote();
                if (settingsprefs.getBoolean("imagebackground", true)) {
                    screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        checkReadWritePermissions(1);
                    } else {
                        // check for new background image and download if availible
                        AsyncTaskDownloadImage runner = new AsyncTaskDownloadImage();
                        runner.execute();
                    }
                } else {
                    //screenlayout.setBackgroundColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
                    screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
                    ImageSwitcher is = (ImageSwitcher) v.findViewById(R.id.imageSwitcher);
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    Bitmap image = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
                    image.eraseColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
                    Drawable d = new BitmapDrawable(getResources(), image);
                    is.setImageDrawable(d);
                    adjustActionBarColor(image, false);
                }
            }
        } else {
            //Log.d("easeWave", "network not availible");
            if (!settingsprefs.getString("lastDownloadedQuote", "1991/03/01").equals(today)) {
                syncError = true;
                getActivity().invalidateOptionsMenu();
                //Log.d("easeWave", "sync error");
            }
            loadLastDownloadedQuote();
            if (settingsprefs.getBoolean("imagebackground", true)) {
                screenlayout.setBackgroundColor(Color.parseColor("#00000000"));
                loadLastDownloadedBackgroundImage();
            } else {
                screenlayout.setBackgroundColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
            }
        }
    }

    private void downloadQuote() {
        swipeLayout.setRefreshing(true);
        Calendar cal = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = formatter.format(cal.getTime());
        //Log.d("easeWave", "today: " + today);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Quotes");
        query.whereEqualTo("Date", today);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                swipeLayout.setRefreshing(false);
                if (object != null) {
                    syncError = false;
                    noquote = false;
                    String quote;
                    String source;
                    if (settingsprefs.getString("language", Locale.getDefault().getLanguage()).equals("de")) {
                        quote = (object.getString("de")).trim();
                        source = (object.getString("vers")).trim();
                    } else {
                        quote = (object.getString("en")).trim();
                        source = (object.getString("verse")).trim();
                    }
                    displayQuote(quote, source);
                    settingsprefs.edit().putString("lastDownloadedQuote", today).apply();
                    settingsprefs.edit().putString("lastDownloadedQuoteDate", object.getString("Date").trim()).apply();
                    settingsprefs.edit().putString("quote", quote).apply();
                    settingsprefs.edit().putString("source", source).apply();
                    updateWidget();
                } else {
                    syncError = true;
                    noquote = true;
                    // try to load last from prefs
                    loadLastDownloadedQuote();
                }
                if (isAdded()) {
                    getActivity().invalidateOptionsMenu();
                }
            }
        });
    }

    private void updateWidget() {
        if (isAdded()) {
            Intent intent = new Intent(getActivity(), WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int ids[] = AppWidgetManager.getInstance(getActivity().getApplication()).getAppWidgetIds(new ComponentName(getActivity().getApplication(), WidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            getActivity().sendBroadcast(intent);
        }
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
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            checkReadWritePermissions(2);
        } else {
            //Log.d("easeWave", "loadbgimage");
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
                        adjustActionBarColor(myBitmap, false);
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
    }

    private void displayQuote(String en, String verse) {
        if (noquote) {
            source.setText(getActivity().getString(R.string.unabletoreceivequote));
            source.startAnimation(fade_in2);
            source.setVisibility(View.VISIBLE);
        } else {
            /*int bgColor = 0xccffffff;
            if (!settingsprefs.getBoolean("imagebackground", true)) {
                bgColor = 0xffffffff;
            }*/

            /*int padding = 10; // in pixels
            quote.setShadowLayer(padding *//* radius *//*, 0, 0, 0 *//* transparent *//*);
            //quote.setPadding(padding, padding, padding, padding);
            Spannable spannable = new SpannableString(en);
            spannable.setSpan(new PaddingBackgroundColorSpan(0xccffffff, padding), 0, en.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            quote.setText(spannable);*/

            //Spannable spanna = new SpannableString(en);
            //spanna.setSpan(new BackgroundColorSpan(bgColor), 0, en.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            quote.setText(en);
            source.setText(Html.fromHtml(verse));
            RelativeLayout quote_wrap = (RelativeLayout) v.findViewById(R.id.quote_wrap);
            quote_wrap.startAnimation(fade_in);
            quote_wrap.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    source.startAnimation(fade_in3);
                    source.setVisibility(View.VISIBLE);
                }
            }, 500);

            if (!floatingButton.isShown()) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isAdded()) {
                            floatingActionStar.setColorNormal(getActivity().getResources().getColor(R.color.fab));
                            //floatingActionStar.setIcon(R.drawable.ic_fab_star);
                            floatingActionStar.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fab_star));
                            Map<String, ?> keys = favsprefs.getAll();
                            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                                String quoteStr = entry.getValue().toString().split("\\}")[0];
                                if (quoteStr.equals(quote.getText().toString())) {
                                    floatingActionStar.setColorNormal(getActivity().getResources().getColor(R.color.faved));
                                    //floatingActionStar.setIcon(R.drawable.ic_fab_unstar);
                                    floatingActionStar.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fab_unstar));
                                    break;
                                }
                            }
                            floatingButton.startAnimation(fab_menu_in);
                            floatingButton.setVisibility(View.VISIBLE);
                        }
                    }
                }, 1200);
            }
            if (reminderprefs.getBoolean("persistentNotification", false)) {
                String message = en + "\n" + verse;
                setPersistentReminder(message);
            }
        }
    }

    public void setPersistentReminder(String message) {
        if (isAdded()) {
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

            if (!bigtxt.equals("freshen up your brain!")) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    builder.addAction(R.drawable.ic_fab_share, getString(R.string.share), shareIntent);
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

    private boolean isNetworkAvailable() {
        if (isAdded()) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void adjustActionBarColor(Bitmap source, boolean fade) {
        if (isAdded()) {
            Bright.Luminance bright = Bright.setup(Bright.Config.PERCEIVED | Bright.Config.PERFORMANCE);
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int x = 0;
            int y = 0;
            int width = size.x;
            int height = 80;

            try {
                Bitmap dest = Bitmap.createBitmap(source, x, y, width, height);
                int luminance = bright.brightness(dest);
        /*int textColor = bright.isBright(luminance) ? Color.parseColor("#808080")
                : Color.WHITE;
        Spannable text = new SpannableString(actionBar.getTitle());
        text.setSpan(new ForegroundColorSpan(textColor), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        actionBar.setTitle(text);*/

                FrameLayout actionbarShadow = (FrameLayout) v.findViewById(R.id.actionbarShadow);
                if (bright.isBright(luminance)) {
                    //Log.d("easeWave", "show shadow");
                    if (fade) {
                        actionbarShadow.startAnimation(fade_in3);
                    }
                    actionbarShadow.setVisibility(View.VISIBLE);
                } else {
                    //Log.d("easeWave", "hide shadow");
                    if (fade) {
                        actionbarShadow.startAnimation(fade_out3);
                    }
                    actionbarShadow.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {

            }
        }
    }

    private void adjustWatermarkColor() {
        if (isAdded()) {
            Bright.Luminance bright = Bright.setup(Bright.Config.PERCEIVED | Bright.Config.PERFORMANCE);
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File path = new File(sdCard + "/easeWave");
            String filename = "background.jpg";
            File file = new File(path, filename);
            Point size = new Point();
            display.getSize(size);
            int x = (int) watermark.getX();
            int y = (int) watermark.getY();
            int width = watermark.getWidth();
            int height = watermark.getHeight();
            File imgFile = new File(file.getPath());
            try {
                if (imgFile.exists()) {
                    Bitmap source = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    Bitmap dest = Bitmap.createBitmap(source, x, y, width, height);
                    int luminance = bright.brightness(dest);
                    //Log.d("easeWave", "lum: " + luminance);

                    int textColor = bright.isBright(luminance) ? Color.parseColor("#808080")
                            : Color.WHITE;
                    watermark.setTextColor(textColor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*public class PaddingBackgroundColorSpan implements LineBackgroundSpan {
        private int mBackgroundColor;
        private int mPadding;
        private RectF mBgRect;

        public PaddingBackgroundColorSpan(int backgroundColor, int padding) {
            super();
            mBackgroundColor = backgroundColor;
            mPadding = padding;
            // Precreate rect for performance
            mBgRect = new RectF();
        }

        @Override
        public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
            final int textWidth = Math.round(p.measureText(text, start, end));
            final int paintColor = p.getColor();
            // Draw the background
            mBgRect.set(left - mPadding,
                    top - (lnum == 0 ? mPadding / 2 : -(mPadding / 2)),
                    left + textWidth + mPadding,
                    bottom + mPadding / 2);
            p.setColor(mBackgroundColor);
            c.drawRoundRect(mBgRect, 10, 10, p);
            p.setColor(paintColor);
        }
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (syncError) {
            MenuItem item = menu.add(0, MENU_SYNC_ERROR, Menu.NONE, R.string.syncerror).setIcon(R.drawable.sync_error);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case MENU_SYNC_ERROR:
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

                title.setText(getActivity().getString(R.string.unabletoreceivequote));
                message.setText(getActivity().getString(R.string.syncerrordescription));
                ok.setText(getActivity().getString(R.string.tryagain));
                cancel.setText(getActivity().getString(R.string.close));

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadQuote();
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
                alertDialog.setTitle(R.string.unabletoreceivequote);
                alertDialog.setMessage(getActivity().getString(R.string.syncerrordescription));
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getActivity().getString(R.string.close),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getActivity().getString(R.string.tryagain),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                loadQuote();
                                dialog.dismiss();
                            }
                        });
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();*/
                break;
        }
        return false;
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
    public void onDestroy() {
        super.onDestroy();
        ImageView v = (ImageView) imageSwitcher.getNextView();
        BitmapDrawable bd = (BitmapDrawable) v.getDrawable();
        if (bd != null) {
            Bitmap b = bd.getBitmap();
            if (b != null) {
                b.recycle();
            }
        }
        imageSwitcher.setImageDrawable(null);
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        v = inflater.inflate(R.layout.fragment_main, viewGroup);

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

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("easeWave", "permission callback");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setup();
                    Log.d("easeWave", "permission granted");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/

    @TargetApi(Build.VERSION_CODES.M)
    private void checkReadWritePermissions(int task) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, task);
        }
    }

    public void doTaskWithSpecialPermission(int task) {
        switch (task) {
            case 1: // Download BG Image
                AsyncTaskDownloadImage runner = new AsyncTaskDownloadImage();
                runner.execute();
                break;
            case 2: // Load BG Image from sdcard
                loadLastDownloadedBackgroundImage();
                break;
            case 3: // Share Image
                shareImage();
                break;
        }
    }

    public void permissionDenied(int task) {
        switch (task) {
            case 1: // Download BG Image
                settingsprefs.edit().putBoolean("imagebackground", false).apply();
                screenlayout.setBackgroundColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
                break;
            case 2: // Load BG Image from sdcard
                settingsprefs.edit().putBoolean("imagebackground", false).apply();
                screenlayout.setBackgroundColor(settingsprefs.getInt("backgroundcolor", getResources().getColor(R.color.appthemecolor)));
                break;
            case 3: // Share Image
                Toast.makeText(getActivity(), "Permission Denied! Unable to save image.", Toast.LENGTH_LONG).show();
                break;
        }
    }
}