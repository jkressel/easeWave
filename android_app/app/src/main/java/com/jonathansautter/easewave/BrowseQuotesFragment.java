package com.jonathansautter.easewave;

import android.annotation.TargetApi;
import android.app.Dialog;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devddagnet.bright.lib.Bright;
import com.nispok.snackbar.SnackbarManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

public class BrowseQuotesFragment extends android.support.v4.app.Fragment {

    private View v;
    private SharedPreferences favsprefs;
    ArrayList<Card> cards = new ArrayList<>();
    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
    private String quotestr;
    private String sourcestr;
    private Animation fade_in;
    private Animation fade_in2;
    private Animation fade_out;
    private Animation fade_out3;
    private ProgressBar progressBar;
    private CardRecyclerView mRecyclerView;
    private CustomExpandCard expand;
    private TextView searchTerm;
    private RelativeLayout searchResults;
    private ImageButton close;
    private boolean noquotes = true;
    private TextView searchResultsTitle;
    private Card emptyCard;
    private static final int MENU_SEARCH = 1;
    private static final int MENU_RELOAD = 2;
    private int screenwidth, screenheight;
    private int limit, skip;
    private boolean loading;
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences allquotesprefs;
    private SharedPreferences settingsprefs;
    private String today;
    private String searchTerms;
    private String tagsStr;
    private String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_favourites, container, false);

        setup();

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void setup() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;

        settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);
        favsprefs = getActivity().getSharedPreferences("FAVS", 0);
        allquotesprefs = getActivity().getSharedPreferences("QUOTES", 0);

        fade_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fade_in2 = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        fade_out3 = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        searchResults = (RelativeLayout) v.findViewById(R.id.searchResults);
        searchResultsTitle = (TextView) v.findViewById(R.id.searchResultsTitle);
        searchTerm = (TextView) v.findViewById(R.id.searchTerm);
        close = (ImageButton) v.findViewById(R.id.close);

        mRecyclerView = (CardRecyclerView) v.findViewById(R.id.carddemo_recyclerview);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        SharedPreferences settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);
        if (!settingsprefs.getBoolean("imagebackground", true)) {
            mRecyclerView.setAlpha(1);
        }

        searchTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDialog(searchTerm.getText().toString());
            }
        });

        expand = new CustomExpandCard(getActivity());

        setBackgroundImage();

        fixMargins();

        limit = 25;
        skip = 0;

        mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);

        mRecyclerView.setAdapter(mCardArrayAdapter);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (!loading && !searchResults.isShown()) {
                    if ((visibleItemCount + firstVisibleItem + 5) >= totalItemCount) {
                        //Log.v("...", "Last Item Wow !");
                        skip = totalItemCount;
                        downloadQuotes();
                    }
                }
            }
        });

        Calendar cal = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        today = formatter.format(cal.getTime());

        loadQuotes();
    }

    private void loadQuotes() {
        if (!allquotesprefs.getAll().isEmpty()) {
            if (settingsprefs.getString("language", "en").equals(settingsprefs.getString("browseAllLanguage", settingsprefs.getString("language", "en")))) {
                loadLastDownloadedQuotes();
            } else {
                // load again since language changed
                if (isNetworkAvailable()) {
                    downloadQuotes();
                } else {
                    loadLastDownloadedQuotes(); // will show in wrong language
                }
            }
        } else {
            if (isNetworkAvailable()) {
                downloadQuotes();
            } else {
                loadLastDownloadedQuotes(); // will show card with hint
            }
        }
    }

    private void downloadNewQuotes() {

        String lastCardDate = cards.get(0).getId();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date lastDate = null;
        try {
            lastDate = format.parse(lastCardDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        if (lastDate != null) {
            Calendar calLast = Calendar.getInstance();
            calLast.setTime(lastDate);
            Calendar cal = Calendar.getInstance();
            //Log.d("easeWave", format.format(lastDate) + " " + format.format(cal.getTime()));
            long diff = cal.getTimeInMillis() - calLast.getTimeInMillis();
            float dayCount = (float) diff / (24 * 60 * 60 * 1000);
            int days = (int) dayCount;
            //Log.d("easeWave", "limit: " + days + "daycount: " + dayCount);

            if (!mRecyclerView.isShown()) {
                progressBar.startAnimation(fade_in2);
                progressBar.setVisibility(View.VISIBLE);
            }
            loading = true;

            ParseQuery<ParseObject> query = new ParseQuery<>("Quotes");
            query.setLimit(days);
            query.setSkip(0);
            query.addDescendingOrder("Date");
            query.whereLessThanOrEqualTo("Date", today);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> entry, ParseException e) {
                    if (e == null) {
                        if (!settingsprefs.getString("language", "en").equals(settingsprefs.getString("browseAllLanguage", settingsprefs.getString("language", "en")))) {
                            allquotesprefs.edit().clear().apply();
                        }

                        noquotes = false;
                        for (int i = 0; i < entry.size(); i++) {

                            String source, quote;
                            if (settingsprefs.getString("language", Locale.getDefault().getLanguage()).equals("de")) {
                                quote = String.valueOf(entry.get(i).get("de"));
                                source = String.valueOf(Html.fromHtml(String.valueOf(entry.get(i).get("vers"))));
                            } else {
                                quote = String.valueOf(entry.get(i).get("en"));
                                source = String.valueOf(Html.fromHtml(String.valueOf(entry.get(i).get("verse"))));
                            }
                            date = String.valueOf(entry.get(i).get("Date"));

                            //Log.d("easeWave", date + " " + quote + " " + source);

                            if (quote.contains("»") || quote.contains("«")) {
                                quote = quote.replace("»", "").replace("«", "");
                            }

                            if (allquotesprefs.getString(date, "").equals("")) {
                                String combined = quote + "}" + source;
                                allquotesprefs.edit().putString(date, combined).apply();

                                /*//Create a Card
                                Card card = new Card(getActivity(), R.layout.card_inner_content);

                                ViewToClickToExpand viewToClickToExpand =
                                        ViewToClickToExpand.builder()
                                                .highlightView(false)
                                                .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
                                card.setViewToClickToExpand(viewToClickToExpand);

                                //Set onClick listener
                                card.setOnClickListener(new Card.OnCardClickListener() {
                                    @Override
                                    public void onClick(Card card, View view) {
                                        card.doToogleExpand();
                                    }
                                });

                                //Create a CardHeader
                                CardHeader header = new CardHeader(getActivity());

                                header.setTitle(source);

                                //Set visible the expand/collapse button
                                header.setButtonExpandVisible(true);

                                card.addCardExpand(expand);

                                //Add Header to card
                                card.addCardHeader(header);

                                card.setTitle(quote);

                                //Card elevation
                                card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                                card.setId(date);

                                card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                                    @Override
                                    public void onExpandEnd(Card card) {
                                        SnackbarManager.dismiss();
                                        quotestr = card.getTitle();
                                        sourcestr = card.getCardHeader().getTitle();
                                        date = card.getId();
                                    }
                                });

                                cards.add(card);*/
                            }
                        }
                        loading = false;
                        settingsprefs.edit().putString("lastDownloadedAllQuotes", today).apply();
                        loadLastDownloadedQuotes();
                        /*loading = false;



                        settingsprefs.edit().putString("browseAllLanguage", settingsprefs.getString("language", Locale.getDefault().getLanguage())).apply();
                        //Log.d("easeWave", "language: " + settingsprefs.getString("language", "en") + " browselanguage: " + settingsprefs.getString("browseAllLanguage", settingsprefs.getString("language", "en")));

                        //Log.d("easeWave", "cardsarray: " + cards.size());

                        //mCardArrayAdapter.notifyItemRangeInserted(skip, limit);
                        mCardArrayAdapter.notifyDataSetChanged();

                        if (!mRecyclerView.isShown()) {
                            mRecyclerView.startAnimation(fade_in);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        }

                        if (progressBar.isShown()) {
                            progressBar.startAnimation(fade_out3);
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                        getActivity().invalidateOptionsMenu();*/
                    }
                }
            });
        }
    }

    private void downloadQuotes() {
        //Log.d("easeWave", "download quotes from parse");
        if (!mRecyclerView.isShown()) {
            progressBar.startAnimation(fade_in2);
            progressBar.setVisibility(View.VISIBLE);
        }
        loading = true;
        skip = mLayoutManager.getItemCount();

        if (emptyCard != null) {
            cards.remove(emptyCard);
            //Log.d("easeWave", "remove empty card");
            skip = 0;
        }

        ParseQuery<ParseObject> query = new ParseQuery<>("Quotes");
        query.setLimit(limit);
        query.setSkip(skip);
        query.whereLessThanOrEqualTo("Date", today);
        query.addDescendingOrder("Date");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> entry, ParseException e) {
                if (e == null) {
                    if (!settingsprefs.getString("language", "en").equals(settingsprefs.getString("browseAllLanguage", settingsprefs.getString("language", "en")))) {
                        allquotesprefs.edit().clear().apply();
                    }

                    noquotes = false;
                    for (int i = 0; i < entry.size(); i++) {

                        String source, quote;
                        if (settingsprefs.getString("language", Locale.getDefault().getLanguage()).equals("de")) {
                            quote = String.valueOf(entry.get(i).get("de"));
                            source = String.valueOf(Html.fromHtml(String.valueOf(entry.get(i).get("vers"))));
                        } else {
                            quote = String.valueOf(entry.get(i).get("en"));
                            source = String.valueOf(Html.fromHtml(String.valueOf(entry.get(i).get("verse"))));
                        }
                        date = String.valueOf(entry.get(i).get("Date"));

                        if (quote.contains("»") || quote.contains("«")) {
                            quote = quote.replace("»", "").replace("«", "");
                        }

                        //Log.d("easeWave", date + " " + quote + " " + source);

                        if (allquotesprefs.getString(date, "").equals("")) {
                            String combined = quote + "}" + source;
                            allquotesprefs.edit().putString(date, combined).apply();

                            //Create a Card
                            Card card = new Card(getActivity(), R.layout.card_inner_content);

                            ViewToClickToExpand viewToClickToExpand =
                                    ViewToClickToExpand.builder()
                                            .highlightView(false)
                                            .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
                            card.setViewToClickToExpand(viewToClickToExpand);

                            //Set onClick listener
                            card.setOnClickListener(new Card.OnCardClickListener() {
                                @Override
                                public void onClick(Card card, View view) {
                                    card.doToogleExpand();
                                }
                            });

                            //Create a CardHeader
                            CardHeader header = new CardHeader(getActivity());

                            header.setTitle(source);

                            //Set visible the expand/collapse button
                            header.setButtonExpandVisible(true);

                            card.addCardExpand(expand);

                            //Add Header to card
                            card.addCardHeader(header);

                            card.setTitle(quote);

                            //Card elevation
                            card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                            card.setId(date);

                            card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                                @Override
                                public void onExpandEnd(Card card) {
                                    SnackbarManager.dismiss();
                                    quotestr = card.getTitle();
                                    sourcestr = card.getCardHeader().getTitle();
                                    date = card.getId();
                                }
                            });

                            cards.add(card);
                        }
                    }
                    loading = false;

                    settingsprefs.edit().putString("lastDownloadedAllQuotes", today).apply();

                    settingsprefs.edit().putString("browseAllLanguage", settingsprefs.getString("language", Locale.getDefault().getLanguage())).apply();
                    //Log.d("easeWave", "language: " + settingsprefs.getString("language", "en") + " browselanguage: " + settingsprefs.getString("browseAllLanguage", settingsprefs.getString("language", "en")));

                    //Log.d("easeWave", "cardsarray: " + cards.size());

                    if (emptyCard == null) {
                        mCardArrayAdapter.notifyItemRangeInserted(skip, mLayoutManager.getItemCount());
                    } else {
                        mCardArrayAdapter.notifyDataSetChanged();
                        emptyCard = null;
                    }

                    if (!mRecyclerView.isShown()) {
                        mRecyclerView.startAnimation(fade_in);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }

                    if (progressBar.isShown()) {
                        progressBar.startAnimation(fade_out3);
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    getActivity().invalidateOptionsMenu();

                } else {
                    // handle Parse Exception here
                    Toast.makeText(getActivity(), "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadLastDownloadedQuotes() {
        //Log.d("easeWave", "load last downloaded quotes from prefs");
        AsyncTaskReadQuotesFromPrefs runner = new AsyncTaskReadQuotesFromPrefs();
        runner.execute();
    }

    private class AsyncTaskReadQuotesFromPrefs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
            if (!mRecyclerView.isShown()) {
                progressBar.startAnimation(fade_in2);
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                //Map<String,?> keys2 = allquotesprefs.getAll();
                TreeMap<String, ?> keys3 = new TreeMap<String, Object>(allquotesprefs.getAll());
                //TreeMap<String, ?> keys = new TreeMap<String, Object>(Collections.reverseOrder());
                Map<String, ?> keys = keys3.descendingMap();
                if (!allquotesprefs.getAll().isEmpty()) {
                    cards.clear();
                    noquotes = false;
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {

                        String quote = entry.getValue().toString().split("\\}")[0];
                        final String source = entry.getValue().toString().split("\\}")[1];
                        date = entry.getKey();
                        //Log.d("easeWave", date + " " + quote + " " + source);

                        //Create a Card
                        Card card = new Card(getActivity(), R.layout.card_inner_content);

                        ViewToClickToExpand viewToClickToExpand =
                                ViewToClickToExpand.builder()
                                        .highlightView(false)
                                        .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
                        card.setViewToClickToExpand(viewToClickToExpand);

                        //Set onClick listener
                        card.setOnClickListener(new Card.OnCardClickListener() {
                            @Override
                            public void onClick(Card card, View view) {
                                card.doToogleExpand();
                            }
                        });

                        //Create a CardHeader
                        CardHeader header = new CardHeader(getActivity());

                        header.setTitle(source);

                        //Set visible the expand/collapse button
                        header.setButtonExpandVisible(true);

                        card.addCardExpand(expand);

                        //Add Header to card
                        card.addCardHeader(header);

                        card.setTitle(quote);

                        //Card elevation
                        card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                        card.setId(date);

                        card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                            @Override
                            public void onExpandEnd(Card card) {
                                SnackbarManager.dismiss();
                                quotestr = card.getTitle();
                                sourcestr = card.getCardHeader().getTitle();
                                date = card.getId();
                            }
                        });

                        cards.add(card);
                    }
                } else {
                    noquotes = true;
                    cards.clear();
                    //Create a Card
                    Card card = new Card(getActivity(), R.layout.card_inner_content);

                    //Create a CardHeader
                    CardHeader header = new CardHeader(getActivity());

                    header.setTitle("Ups...");

                    //Add Header to card
                    card.addCardHeader(header);

                    card.setTitle("Please connect to the internet in order to browse all easeWave quotes.\n" +
                            "Loaded quotes will be cached to view offline.");

                    //Card elevation
                    card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                    card.setId("noconnection");
                    emptyCard = card;

                    cards.add(card);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String filepath) {
            if (isAdded()) {
                // execution of result of Long time consuming operation
                mCardArrayAdapter.notifyDataSetChanged();

                if (!mRecyclerView.isShown()) {
                    mRecyclerView.startAnimation(fade_in);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                if (progressBar.isShown()) {
                    progressBar.startAnimation(fade_out3);
                    progressBar.setVisibility(View.INVISIBLE);
                }

                getActivity().invalidateOptionsMenu();

                if (!settingsprefs.getString("lastDownloadedAllQuotes", "").equals(today)) {
                    if (isNetworkAvailable()) {
                        downloadNewQuotes();
                    }
                }
            }
        }
    }

    public class CustomExpandCard extends CardExpand {

        //Use your resource ID for your inner layout
        public CustomExpandCard(Context context) {
            super(context, R.layout.card_inner_expand2);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;

            //Retrieve elements
            LinearLayout share_layout = (LinearLayout) view.findViewById(R.id.share_layout);
            LinearLayout remove_layout = (LinearLayout) view.findViewById(R.id.remove_layout);

            final TextView remove = (TextView) view.findViewById(R.id.remove);
            final ImageView removeIcon = (ImageView) view.findViewById(R.id.remove_icon);

            remove.setText(R.string.addtofavs);
            removeIcon.setImageResource(R.drawable.ic_fab_star);

            if (quotestr != null && sourcestr != null) {
                Map<String, ?> keys = favsprefs.getAll();
                if (!favsprefs.getAll().isEmpty()) {
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                        String quote = entry.getValue().toString().split("\\}")[0];
                        if (quote.equals(quotestr)) {
                            remove.setText(R.string.remove);
                            removeIcon.setImageResource(R.drawable.ic_fab_unstar);
                        }
                    }
                }
            }

            share_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String shareBody = quotestr + " -" + sourcestr + "\n\n" + getActivity().getString(R.string.onerefreshingquoteeveryday);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "freshen up your brain");
                    shareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent, getActivity().getString(R.string.sharewith)));
                }
            });

            remove_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (remove.getText().toString().equals(getString(R.string.remove))) {
                        Map<String, ?> keys = favsprefs.getAll();
                        for (Map.Entry<String, ?> entry : keys.entrySet()) {
                            String quoteStr = entry.getValue().toString().split("\\}")[0];
                            if (quoteStr.equals(quotestr)) {
                                favsprefs.edit().remove(entry.getKey()).apply();
                            }
                        }
                        //favsprefs.edit().remove(date).apply();
                        remove.setText(R.string.addtofavs);
                        removeIcon.setImageResource(R.drawable.ic_fab_star);
                    } else {
                        String combined = quotestr + "}" + sourcestr;
                        favsprefs.edit().putString(String.valueOf(System.currentTimeMillis()), combined).apply();
                        remove.setText(R.string.remove);
                        removeIcon.setImageResource(R.drawable.ic_fab_unstar);
                    }
                }
            });
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

    private void searchDialog(String terms) {
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.search_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        setDialogSize(dialog);

        final EditText input = (EditText) dialog.findViewById(R.id.editText);
        final TextView save = (TextView) dialog.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel);

        if (!terms.equals("")) {
            String[] separated = terms.split("#");
            String formatted = "";
            for (String aSeparated : separated) {
                if (!aSeparated.trim().equals("")) {
                    formatted += aSeparated.trim() + ", ";
                }
            }
            input.setText(formatted);
            input.setSelection(input.getText().length());
        }

        input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save.performClick();
                    return true;
                }
                return false;
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTerms = input.getText().toString().toLowerCase();
                if (isNetworkAvailable()) {
                    // search on parse
                    searchQuotesOnline();
                } else {
                    // search in quotes from prefs
                    AsyncTaskSearchLocalQuotes runner = new AsyncTaskSearchLocalQuotes();
                    runner.execute(searchTerms);
                }
                inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                inputMethodManager.hideSoftInputFromWindow(input.getWindowToken(), 0);
            }
        });

        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!noquotes) {
            MenuItem item2 = menu.add(1, MENU_SEARCH, Menu.NONE, getString(R.string.search)).setIcon(R.drawable.seach_icon);
            item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            MenuItem item3 = menu.add(1, MENU_RELOAD, Menu.NONE, "Reload").setIcon(R.drawable.reload);
            item3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case MENU_SEARCH:
                searchDialog(searchTerm.getText().toString());
                return true;
            case MENU_RELOAD:
                loadQuotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void searchQuotesOnline() {

        if (mRecyclerView.isShown()) {
            mRecyclerView.startAnimation(fade_out);
            mRecyclerView.setVisibility(View.GONE);
        }
        progressBar.startAnimation(fade_in2);
        progressBar.setVisibility(View.VISIBLE);

        cards.clear();

        final List<String> keywords = Arrays.asList(searchTerms.split("\\s*,\\s*"));
        List<ParseQuery<ParseObject>> queries = new ArrayList<>();

        tagsStr = "";

        String parseFieldQuote, parseFieldSource;
        if (settingsprefs.getString("language", Locale.getDefault().getLanguage()).equals("de")) {
            parseFieldQuote = "de_search";
            parseFieldSource = "vers_search";
        } else {
            parseFieldQuote = "en_search";
            parseFieldSource = "verse_search";
        }

        for (int j = 0; j < keywords.size(); j++) {

            ParseQuery<ParseObject> query_quote = new ParseQuery<>("Quotes");
            //query_quote.setLimit(limit_search);
            //query_quote.setSkip(skip_search);
            //query_quote.addAscendingOrder("Date");
            query_quote.whereContains(parseFieldQuote, keywords.get(j).toLowerCase().trim());
            queries.add(query_quote);

            ParseQuery<ParseObject> query_source = new ParseQuery<>("Quotes");
            //query_source.setLimit(limit_search);
            //query_source.setSkip(skip_search);
            //query_source.addAscendingOrder("Date");
            query_source.whereContains(parseFieldSource, keywords.get(j).toLowerCase().trim());
            queries.add(query_source);

            tagsStr = tagsStr + " #" + keywords.get(j) + " ";
        }

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> resultList, ParseException e) {
                if (e == null) {
                    if (resultList.size() != 0) {
                        int index = 0;

                        ArrayList<String> searchResultsList = new ArrayList<>();

                        for (int i = 0; i < resultList.size(); i++) {

                            String source, quote;
                            if (settingsprefs.getString("language", Locale.getDefault().getLanguage()).equals("de")) {
                                quote = String.valueOf(resultList.get(i).get("de"));
                                source = String.valueOf(Html.fromHtml(String.valueOf(resultList.get(i).get("vers"))));
                            } else {
                                quote = String.valueOf(resultList.get(i).get("en"));
                                source = String.valueOf(Html.fromHtml(String.valueOf(resultList.get(i).get("verse"))));
                            }
                            //String date = String.valueOf(resultList.get(i).get("Date"));

                            //Log.d("easeWave", "search result: " + date + " " + quote + " " + source);

                            if (quote.contains("»") || quote.contains("«")) {
                                quote = quote.replace("»", "").replace("«", "");
                            }

                            boolean duplicate = false;
                            for (int j = 0; j < searchResultsList.size(); j++) {
                                duplicate = searchResultsList.get(j).equals(quote);
                            }
                            if (!duplicate) {

                                searchResultsList.add(quote);

                                //Create a Card
                                Card card = new Card(getActivity(), R.layout.card_inner_content);

                                ViewToClickToExpand viewToClickToExpand =
                                        ViewToClickToExpand.builder()
                                                .highlightView(false)
                                                .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
                                card.setViewToClickToExpand(viewToClickToExpand);

                                //Set onClick listener
                                card.setOnClickListener(new Card.OnCardClickListener() {
                                    @Override
                                    public void onClick(Card card, View view) {
                                        card.doToogleExpand();
                                    }
                                });

                                //Create a CardHeader
                                CardHeader header = new CardHeader(getActivity());

                                header.setTitle(source);

                                //Set visible the expand/collapse button
                                header.setButtonExpandVisible(true);

                                card.addCardExpand(expand);

                                //Add Header to card
                                card.addCardHeader(header);

                                card.setTitle(quote);

                                //Card elevation
                                card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                                card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                                    @Override
                                    public void onExpandEnd(Card card) {
                                        SnackbarManager.dismiss();
                                        quotestr = card.getTitle();
                                        sourcestr = card.getCardHeader().getTitle();
                                    }
                                });

                                card.setId(String.valueOf(index));

                                cards.add(card);

                                index++;
                            }
                        }
                    } else {
                        //Log.d("easeWave", "onlineSearchComplete");
                        //Create a Card
                        Card card = new Card(getActivity(), R.layout.card_inner_content);

                        //Create a CardHeader
                        CardHeader header = new CardHeader(getActivity());

                        header.setTitle("No Quote found matching your keyword(s)!");

                        //Add Header to card
                        card.addCardHeader(header);

                        card.setTitle("Try single words instead of sentences. Tap on any keyword above to edit them.");

                        //Card elevation
                        card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                        card.setId("nosearchresults");
                        emptyCard = card;

                        cards.add(card);
                    }
                    loading = false;

                    mCardArrayAdapter.notifyDataSetChanged();

                    if (!mRecyclerView.isShown()) {
                        mRecyclerView.startAnimation(fade_in);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }

                    if (progressBar.isShown()) {
                        progressBar.startAnimation(fade_out3);
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    searchResults.setVisibility(View.VISIBLE);

                    searchTerm.setText(tagsStr);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adjustSeachItemsColor();
                        }
                    }, 10);

                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchResults.setVisibility(View.GONE);
                            cards.clear();
                            searchTerm.setText("");
                            loadLastDownloadedQuotes();
                        }
                    });
                } else {
                    e.printStackTrace();
                    //Log.d("easeWave", e.getMessage());
                }
            }
        });
    }

    private class AsyncTaskSearchLocalQuotes extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            if (mRecyclerView.isShown()) {
                mRecyclerView.startAnimation(fade_out);
                mRecyclerView.setVisibility(View.GONE);
            }
            progressBar.startAnimation(fade_in2);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String value = params[0];
            String tagsStr = "";

            try {
                cards.clear();

                List<String> keywords = Arrays.asList(value.split("\\s*,\\s*"));
                Map<String, ?> keys = allquotesprefs.getAll();
                if (!allquotesprefs.getAll().isEmpty()) {
                    int index = 0;
                    ArrayList<String> searchResultsList = new ArrayList<>();
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                        for (int i = 0; i < keywords.size(); i++) {
                            String quote = entry.getValue().toString().split("\\}")[0];
                            String source = entry.getValue().toString().split("\\}")[1];
                            if (quote.toLowerCase().contains(keywords.get(i).trim().toLowerCase()) || source.toLowerCase().contains(keywords.get(i).trim().toLowerCase())) {

                                boolean duplicate = false;
                                for (int j = 0; j < searchResultsList.size(); j++) {
                                    duplicate = searchResultsList.get(j).equals(quote);
                                }
                                if (!duplicate) {

                                    searchResultsList.add(quote);

                                    //Create a Card
                                    Card card = new Card(getActivity(), R.layout.card_inner_content);

                                    //Set onClick listener
                                    card.setOnClickListener(new Card.OnCardClickListener() {
                                        @Override
                                        public void onClick(Card card, View view) {
                                            card.doToogleExpand();
                                        }
                                    });

                                    //Create a CardHeader
                                    CardHeader header = new CardHeader(getActivity());

                                    header.setTitle(source);

                                    //Set visible the expand/collapse button
                                    header.setButtonExpandVisible(true);

                                    card.addCardExpand(expand);

                                    //Add Header to card
                                    card.addCardHeader(header);

                                    card.setTitle(quote);

                                    //Card elevation
                                    card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                                    card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                                        @Override
                                        public void onExpandEnd(Card card) {
                                            SnackbarManager.dismiss();
                                            quotestr = card.getTitle();
                                            sourcestr = card.getCardHeader().getTitle();
                                        }
                                    });

                                    card.setId(String.valueOf(index));

                                    cards.add(card);

                                    index++;
                                }
                            }
                        }
                    }
                    if (cards.size() < 1) {
                        //Create a Card
                        Card card = new Card(getActivity(), R.layout.card_inner_content);

                        //Create a CardHeader
                        CardHeader header = new CardHeader(getActivity());

                        header.setTitle("No Quote found matching your keyword(s)!");

                        //Add Header to card
                        card.addCardHeader(header);

                        card.setTitle("Try single words instead of sentences. Tap on any keyword above to edit them.\\nNote: You need to be online to search all quotes instead of cached ones only.");

                        //Card elevation
                        card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                        card.setId("nosearchresults");
                        emptyCard = card;

                        cards.add(card);
                    }
                }
                for (int i = 0; i < keywords.size(); i++) {
                    tagsStr = tagsStr + " #" + keywords.get(i) + " ";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return tagsStr;
        }

        @Override
        protected void onPostExecute(String tagsStr) {
            if (isAdded()) {

                loading = false;

                mCardArrayAdapter.notifyDataSetChanged();

                if (!mRecyclerView.isShown()) {
                    mRecyclerView.startAnimation(fade_in);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                if (progressBar.isShown()) {
                    progressBar.startAnimation(fade_out3);
                    progressBar.setVisibility(View.INVISIBLE);
                }

                searchResults.setVisibility(View.VISIBLE);

                searchTerm.setText(tagsStr);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adjustSeachItemsColor();
                    }
                }, 10);

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchResults.setVisibility(View.GONE);
                        cards.clear();
                        searchTerm.setText("");
                        loadLastDownloadedQuotes();
                    }
                });
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

    private void adjustSeachItemsColor() {
        searchResultsTitle = (TextView) v.findViewById(R.id.searchResultsTitle);
        searchTerm = (TextView) v.findViewById(R.id.searchTerm);
        close = (ImageButton) v.findViewById(R.id.close);
        ImageView iv = (ImageView) v.findViewById(R.id.imageView);
        Bitmap source = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        Bright.Luminance bright = Bright.setup(Bright.Config.PERCEIVED | Bright.Config.PERFORMANCE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int[] x = {(int) searchResultsTitle.getX(), (int) searchTerm.getX(), (int) close.getX()};
        int[] y = {(int) searchResultsTitle.getY(), (int) searchTerm.getY(), (int) close.getY()};
        int[] width = {searchResultsTitle.getWidth(), searchTerm.getWidth(), close.getWidth()};
        int[] height = {searchResultsTitle.getHeight(), searchTerm.getHeight(), close.getHeight()};

        for (int i = 0; i < 3; i++) {
            Bitmap dest = Bitmap.createBitmap(source, x[i], y[i], width[i], height[i]);
            int luminance = bright.brightness(dest);
            //Log.d("easeWave", "lum: " + x[i] + " " + y[i] + " " + width[i] + " " + height[i] + " " + luminance);

            int textColor = bright.isBright(luminance) ? Color.parseColor("#808080")
                    : Color.WHITE;
            if (i == 0) {
                searchResultsTitle.setTextColor(textColor);
            } else if (i == 1) {
                searchTerm.setTextColor(textColor);
            } else if (i == 2) {
                if (bright.isBright(luminance)) {
                    close.setColorFilter(Color.argb(225, 100, 100, 100));
                }
            }
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        v = inflater.inflate(R.layout.fragment_favourites, viewGroup);

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
}
