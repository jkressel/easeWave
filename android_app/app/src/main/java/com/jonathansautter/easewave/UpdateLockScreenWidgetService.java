package com.jonathansautter.easewave;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.widget.RemoteViews;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UpdateLockScreenWidgetService extends Service {

	private Handler handler = new Handler();
	private String today;
	private RemoteViews remoteViews;
	private SharedPreferences settingsprefs;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (final int widgetId : allWidgetIds) {

			//Date
			Date date = new Date();
			SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
			today = dateFormate.format(date);

            /*float widgetquotesize = getResources().getDimension(R.dimen.widgetfontsize);
            float widgetsourcesize = getResources().getDimension(R.dimen.widgetfontsize);
            float met = getResources().getDisplayMetrics().scaledDensity;
            float spquote = widgetquotesize / met;
            float spsource = widgetsourcesize / met;*/

			//Log.d("easeWave", "updatewidgetservice started");

			settingsprefs = getSharedPreferences("SETTINGS", 0);

			//Display Today's Quote
			remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.lockscreenwidget_layout);

			int textColor = settingsprefs.getInt("lockscreenwidgetTextColor", Color.parseColor("#ffffff"));
			int lockscreenwidgetfontsize = settingsprefs.getInt("lockscreenwidgetfontsize", Math.round(getResources().getDimension(R.dimen.lockscreenwidgetfontsize) / getResources().getDisplayMetrics().density));

            remoteViews.setTextColor(R.id.widgetquote, textColor);
            remoteViews.setTextColor(R.id.widgetsource, textColor);
			remoteViews.setFloat(R.id.widgetquote, "setTextSize", lockscreenwidgetfontsize);
			remoteViews.setFloat(R.id.widgetsource, "setTextSize", Math.round(lockscreenwidgetfontsize - (lockscreenwidgetfontsize * 0.2)));

			if (isNetworkAvailable()) {
				if (settingsprefs.getString("lastDownloadedQuote", "1991/03/01").equals(today)) {
					loadLastDownloadedQuote();
				} else {
					downloadQuote();
				}
			} else {
				loadLastDownloadedQuote();
			}

			Intent intent2 = new Intent(this.getApplicationContext(), MainActivity.class);
			PendingIntent pendingIntent2 = PendingIntent.getActivity(getApplicationContext(), 0, intent2, 0);
			remoteViews.setOnClickPendingIntent(R.id.widgetquote, pendingIntent2);

			//Reload
			Intent clickIntent = new Intent(this.getApplicationContext(),
					WidgetProvider.class);

			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.bg, pendingIntent);

			handler.postDelayed(new Runnable() {
				public void run() {
					appWidgetManager.updateAppWidget(widgetId, remoteViews);
				}
			}, 3000);

		}
		stopSelf();
		super.onStartCommand(intent, 0, startId);
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
		String quote = getString(R.string.makesureyouareconnected);
		String source = getString(R.string.connectionerror);
		if (!settingsprefs.getString("quote", "").equals("")) {
			quote = settingsprefs.getString("quote", "");
			source = settingsprefs.getString("source", "");
		}
		displayQuote(quote, source);
	}

	private void displayQuote(String en, String verse) {
		remoteViews.setTextViewText(R.id.widgetquote, en);
		remoteViews.setTextViewText(R.id.widgetsource, Html.fromHtml(verse));
	}
} 
