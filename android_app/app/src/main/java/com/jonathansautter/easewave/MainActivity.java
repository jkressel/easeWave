package com.jonathansautter.easewave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences settingsprefs;
    private String[] navigationTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_main);

        settingsprefs = getSharedPreferences("SETTINGS", 0);

        setTranslucentStatusNavigation();

        setLocale();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.getBackground().setAlpha(0);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_quote);

        //initialize container
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        QuoteFragment fragment = new QuoteFragment();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

        navigationTitles = getResources().getStringArray(R.array.navigation);

        setTitle(navigationTitles[0]);

        if (settingsprefs.getBoolean("keepscreenon", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (settingsprefs.getBoolean("firstlaunch", true)) {
            settingsprefs.edit().putBoolean("firstlaunch", false).apply();
            Intent intent = new Intent(MainActivity.this, Intro.class);
            startActivity(intent);
            // create app folder
            File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File path = new File(sdCard + "/easeWave");
            path.mkdirs();
            convertFavourites();
        }

        if (settingsprefs.getBoolean("firstlaunch2.1", true)) {
            convertFavourites();
        }
    }

    private void convertFavourites() {
        AsyncTaskConvertFavs runner = new AsyncTaskConvertFavs();
        runner.execute();
    }

    private class AsyncTaskConvertFavs extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {

            boolean error = false;

            try {
                SharedPreferences favsprefs = getSharedPreferences("FAVS", 0);
                Map<String, ?> keys = favsprefs.getAll();
                if (!favsprefs.getAll().isEmpty()) {
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {

                        if (entry.getKey().matches("[0-9]+")) {
                            // only numbers -> app version 2.1 and up
                            // key is date added
                            String quote = entry.getValue().toString().split("\\}")[0];
                            String source = entry.getValue().toString().split("\\}")[1];
                            String date = entry.getKey();
                            if (quote.contains("»") || quote.contains("«")) {
                                quote = quote.replace("»", "").replace("«", "");
                            }
                            String combined = quote + "}" + source;
                            favsprefs.edit().remove(date).apply();
                            favsprefs.edit().putString(date, combined).apply();
                        } else {
                            // key is quote -> app version 2.0 and down
                            String quote = entry.getKey();
                            String source = entry.getValue().toString();
                            if (quote.contains("»") || quote.contains("«")) {
                                quote = quote.replace("»", "").replace("«", "");
                            }
                            String combined = quote + "}" + source;
                            favsprefs.edit().remove(quote).apply();
                            favsprefs.edit().putString(String.valueOf(System.currentTimeMillis()), combined).apply();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            }
            return error;
        }

        @Override
        protected void onPostExecute(Boolean error) {
            if (!error) {
                settingsprefs.edit().putBoolean("firstlaunch2.1", false).apply();
            }
        }
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

    private void setTranslucentStatusNavigation() {
        // Translucent Status/Navigation Bar on KitKat and up
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            //w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            /*getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);*/
            // fix toolbar margin
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, getStatusBarHeight(), 0, 0);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setLayoutParams(params);
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        Fragment fragment = null;
        int position = 0;
        int id = item.getItemId();

        if (id == R.id.nav_quote) {
            fragment = new QuoteFragment();
            position = 0;
        } else if (id == R.id.nav_favs) {
            fragment = new FavouritesFragment();
            position = 1;
        } else if (id == R.id.nav_browse) {
            fragment = new BrowseQuotesFragment();
            position = 2;
        } else if (id == R.id.nav_reminder) {
            fragment = new RemindersFragment();
            position = 3;
        } else if (id == R.id.nav_submit) {
            fragment = new SubmitQuoteFragment();
            position = 4;
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
            position = 5;
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, Intro.class);
            startActivity(intent);
        } else if (id == R.id.nav_contact) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "contact@easewave.com", null));
            if (emailIntent.resolveActivity(getPackageManager()) == null) { // no email client installed
                Toast.makeText(getApplicationContext(), R.string.noemailclient, Toast.LENGTH_LONG).show();
            } else {
                startActivity(Intent.createChooser(emailIntent, getString(R.string.sendemailvia)));
            }
        } else if (id == R.id.nav_website) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://easewave.com"));
            startActivity(browserIntent);
        }

        if (fragment != null) {
            Handler handler = new Handler();
            final Fragment finalFragment = fragment;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    //fragmentManager.beginTransaction()
                    //        .replace(R.id.container, finalFragment).commit();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();

                    //ft.setCustomAnimations(R.anim.no_change, R.anim.fade_out_fragment);
                    ft.replace(R.id.container, finalFragment);
                    ft.commit();
                }
            }, 0);

            setTitle(navigationTitles[position]);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
            }, 100);

        }/* else {
            // error in creating fragment
            //Log.e("MainActivity", "Error in creating fragment");
        }*/

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        //Log.d("easeWave", "permission callback");

        QuoteFragment fragment = (QuoteFragment) getSupportFragmentManager().findFragmentById(R.id.container);

        /*
         1 Download BG Image
         2 Load BG Image from sdcard
         3 Share Image
        */

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.
            //Log.d("easeWave", "permission granted");
            fragment.doTaskWithSpecialPermission(requestCode);
        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            //Log.d("easeWave", "permission denied");
            fragment.permissionDenied(requestCode);
        }
    }

    /*@TargetApi(Build.VERSION_CODES.M)
    private void checkReadWritePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    2);
        }
    }*/
}
