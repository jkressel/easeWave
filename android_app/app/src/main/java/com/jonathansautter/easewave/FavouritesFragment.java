package com.jonathansautter.easewave;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devddagnet.bright.lib.Bright;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

public class FavouritesFragment extends android.support.v4.app.Fragment {

    private int FILE_CODE = 0;
    private View v;
    private SharedPreferences favsprefs;
    ArrayList<Card> cards = new ArrayList<>();
    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
    private String quotestr;
    private String sourcestr;
    private Card selectedCard;
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
    private boolean nofavs = true;
    private TextView searchResultsTitle;
    private Card emptyCard;
    private static final int MENU = Menu.FIRST;
    private static final int MENU_SEARCH = Menu.CATEGORY_SECONDARY;
    private static final int MENU_SORT = 3;
    private int screenwidth, screenheight;
    private SharedPreferences settingsprefs;
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

        favsprefs = getActivity().getSharedPreferences("FAVS", 0);

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

        mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);

        mRecyclerView.setAdapter(mCardArrayAdapter);

        settingsprefs = getActivity().getSharedPreferences("SETTINGS", 0);
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

        /*AsyncTaskReadFavs runner = new AsyncTaskReadFavs();
        runner.execute();*/

    }

    private class AsyncTaskFavsList extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressBar.startAnimation(fade_in2);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            try {
                Map<String, ?> keys = favsprefs.getAll();
                if (!favsprefs.getAll().isEmpty()) {
                    cards.clear();
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                        String quote = entry.getValue().toString().split("\\}")[0];
                        String source = entry.getValue().toString().split("\\}")[1];
                        result = result + quote + " - " + source + "\n\n";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String list) {
            if (isAdded()) {
                progressBar.startAnimation(fade_out3);
                progressBar.setVisibility(View.INVISIBLE);

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getString(R.string.myfavouritequotes));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, list);
                startActivity(Intent.createChooser(sharingIntent, getActivity().getString(R.string.sharewith)));
            }
        }
    }

    private class AsyncTaskReadFavs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            if (!mRecyclerView.isShown()) {
                progressBar.startAnimation(fade_in2);
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // check if connected to internet
                Map<String, ?> keys = favsprefs.getAll();
                if (!favsprefs.getAll().isEmpty()) {
                    cards.clear();
                    nofavs = false;
                    int index = 0;
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {

                        String quote = entry.getValue().toString().split("\\}")[0];
                        final String source = entry.getValue().toString().split("\\}")[1];
                        date = entry.getKey();
                        //Log.d("easeWave", "fav: " + date + " " + quote + " " + source);

                        if (quote.contains("»") || quote.contains("«")) {
                            // remove fav and save it again without » and «
                            favsprefs.edit().remove(entry.getKey()).apply();
                            quote = quote.replace("»", "").replace("«", "");
                            String combined = quote + "}" + source;
                            favsprefs.edit().putString(entry.getKey(), combined).apply();
                        }

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
                        card.getCardHeader().setId(String.valueOf(index));

                        card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                            @Override
                            public void onExpandEnd(Card card) {
                                SnackbarManager.dismiss();
                                quotestr = card.getTitle();
                                sourcestr = card.getCardHeader().getTitle();
                                date = card.getId();
                                selectedCard = card;
                            }
                        });

                        cards.add(card);

                        index++;
                    }
                } else {
                    nofavs = true;
                    //Create a Card
                    Card card = new Card(getActivity(), R.layout.card_inner_content);

                    //Create a CardHeader
                    CardHeader header = new CardHeader(getActivity());

                    header.setTitle(getActivity().getString(R.string.nofavouritessavedyet));

                    //Add Header to card
                    card.addCardHeader(header);

                    card.setTitle(getActivity().getString(R.string.addquotetofavourites));

                    //Card elevation
                    card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));

                    card.setId("nofavsyet");
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

                sortCards();

                if (!mRecyclerView.isShown()) {
                    mRecyclerView.startAnimation(fade_in);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                if (progressBar.isShown()) {
                    progressBar.startAnimation(fade_out3);
                    progressBar.setVisibility(View.INVISIBLE);
                }

                getActivity().invalidateOptionsMenu();
            }
        }
    }

    private class AsyncTaskImportFavs extends AsyncTask<Uri, String, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.startAnimation(fade_in2);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Uri... params) {

            Boolean res = false;
            Uri uri = params[0];
            try {
                File path = new File(uri.getPath());
                if (path.exists()) {
                    ObjectInputStream input = null;
                    try {
                        input = new ObjectInputStream(new FileInputStream(path));
                        SharedPreferences.Editor prefEdit = getActivity().getSharedPreferences("FAVS", 0).edit();
                        prefEdit.clear();
                        Map<String, ?> entries = (Map<String, ?>) input.readObject();
                        for (Map.Entry<String, ?> entry : entries.entrySet()) {
                            Object v = entry.getValue();
                            String key = entry.getKey();

                            if (v instanceof Boolean)
                                prefEdit.putBoolean(key, (Boolean) v);
                            else if (v instanceof Float)
                                prefEdit.putFloat(key, (Float) v);
                            else if (v instanceof Integer)
                                prefEdit.putInt(key, (Integer) v);
                            else if (v instanceof Long)
                                prefEdit.putLong(key, (Long) v);
                            else if (v instanceof String)
                                prefEdit.putString(key, ((String) v));
                        }
                        prefEdit.apply();
                        res = true;
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        importerror();
                    } finally {
                        try {
                            if (input != null) {
                                input.close();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    importerror();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return res;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (isAdded()) {
                progressBar.startAnimation(fade_out3);
                progressBar.setVisibility(View.INVISIBLE);

                if (result) {
                    // reload favs into cards
                    AsyncTaskReadFavs runner2 = new AsyncTaskReadFavs();
                    runner2.execute();

                    importsuccess();
                }
            }
        }
    }

    private class AsyncTaskExportFavs extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.startAnimation(fade_in2);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            boolean res = false;
            try {
                File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
                File dir = new File(sdCard + "/easeWave/");
                dir.mkdir();
                File path = new File(sdCard + "/easeWave/easeWave.favs");
                SharedPreferences pref = getActivity().getSharedPreferences("FAVS", 0);
                res = false;
                ObjectOutputStream output = null;
                try {
                    output = new ObjectOutputStream(new FileOutputStream(path));
                    output.writeObject(pref.getAll());

                    res = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null) {
                            output.flush();
                            output.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (isAdded()) {
                progressBar.startAnimation(fade_out3);
                progressBar.setVisibility(View.INVISIBLE);

                if (result) {
                    final Dialog dialog2 = new Dialog(getActivity(), R.style.myDialogStyle);
                    dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog2.setContentView(R.layout.alert_dialog);
                    dialog2.setCanceledOnTouchOutside(false);
                    dialog2.setCancelable(false);
                    setDialogSize(dialog2);

                    TextView title = (TextView) dialog2.findViewById(R.id.title);
                    TextView message = (TextView) dialog2.findViewById(R.id.message);
                    TextView ok = (TextView) dialog2.findViewById(R.id.ok);
                    TextView cancel = (TextView) dialog2.findViewById(R.id.cancel);

                    title.setText(getActivity().getString(R.string.success));
                    message.setText(getActivity().getString(R.string.favouritessaved));
                    ok.setText(getActivity().getString(R.string.sendfilevia));
                    cancel.setText(getActivity().getString(R.string.close));

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // share intent with file attached
                            File sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile();
                            File path = new File(sdCard + "/easeWave");
                            path.mkdirs();
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("*/*");
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(path));
                            startActivity(Intent.createChooser(intent, getActivity().getString(R.string.sendfile)));
                            dialog2.dismiss();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog2.dismiss();
                        }
                    });
                    dialog2.show();
                } else {
                    final Dialog dialog2 = new Dialog(getActivity(), R.style.myDialogStyle);
                    dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog2.setContentView(R.layout.alert_dialog);
                    dialog2.setCanceledOnTouchOutside(false);
                    dialog2.setCancelable(false);
                    setDialogSize(dialog2);

                    TextView title = (TextView) dialog2.findViewById(R.id.title);
                    TextView message = (TextView) dialog2.findViewById(R.id.message);
                    TextView ok = (TextView) dialog2.findViewById(R.id.ok);
                    TextView cancel = (TextView) dialog2.findViewById(R.id.cancel);

                    title.setText(getActivity().getString(R.string.error));
                    message.setText(getActivity().getString(R.string.somethingwentwrong));
                    ok.setText("OK");
                    cancel.setVisibility(View.GONE);

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog2.dismiss();
                        }
                    });

                    dialog2.show();
                }
            }
        }
    }

    public class CustomExpandCard extends CardExpand {

        //Use your resource ID for your inner layout
        public CustomExpandCard(Context context) {
            super(context, R.layout.card_inner_expand);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;

            //Retrieve elements
            LinearLayout share_layout = (LinearLayout) view.findViewById(R.id.share_layout);
            LinearLayout remove_layout = (LinearLayout) view.findViewById(R.id.remove_layout);

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
                    Map<String, ?> keys = favsprefs.getAll();
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                        String quote = entry.getValue().toString().split("\\}")[0];
                        if (quote.equals(quotestr)) {
                            favsprefs.edit().remove(entry.getKey()).apply();
                        }
                    }
                    //favsprefs.edit().remove(date).apply();
                    if (favsprefs.getAll().isEmpty()) {
                        // show empty card and hide actionbar items
                        nofavs = true;
                        //Create a Card
                        Card card = new Card(getActivity(), R.layout.card_inner_content);
                        //Create a CardHeader
                        CardHeader header = new CardHeader(getActivity());
                        header.setTitle(getActivity().getString(R.string.nofavouritessavedyet));
                        //Add Header to card
                        card.addCardHeader(header);
                        card.setTitle(getActivity().getString(R.string.addquotetofavourites));
                        //Card elevation
                        card.setCardElevation(getResources().getDimension(R.dimen.carddemo_shadow_elevation));
                        card.setId("nofavsyet");
                        emptyCard = card;
                        cards.add(card);
                        mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);
                        mRecyclerView.setAdapter(mCardArrayAdapter);
                        getActivity().invalidateOptionsMenu();
                    }
                    cards.remove(selectedCard);
                    mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);
                    mRecyclerView.setAdapter(mCardArrayAdapter);
                    mCardArrayAdapter.notifyDataSetChanged();

                    Typeface typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD);

                    SnackbarManager.show(
                            Snackbar.with(getActivity()) // context
                                    .text(R.string.itemdeleted) // text to display
                                    .actionLabel(R.string.undo) // action button label
                                    .color(getActivity().getResources().getColor(R.color.snackbar)) // change the background color.actionLabelTypeface(myTypeface) // change the action button font
                                    .actionLabelTypeface(typeface) // change the action button font
                                    .attachToRecyclerView(mRecyclerView)
                                    .actionListener(new ActionClickListener() {
                                        @Override
                                        public void onActionClicked(Snackbar snackbar) {
                                            if (selectedCard != null) {
                                                nofavs = false;
                                                if (favsprefs.getAll().isEmpty()) {
                                                    cards.remove(emptyCard);
                                                }
                                                cards.add(Integer.parseInt(selectedCard.getCardHeader().getId()), selectedCard);
                                                mCardArrayAdapter.notifyDataSetChanged();
                                                selectedCard.setExpanded(false);
                                                String combined = quotestr + "}" + sourcestr;
                                                favsprefs.edit().putString(selectedCard.getId(), combined).apply();
                                                getActivity().invalidateOptionsMenu();
                                                //sortCards();
                                            }
                                        }
                                    }) // action button's ActionClickListener
                            , getActivity()); // activity where it is displayed
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
                String value = input.getText().toString().toLowerCase();
                AsyncTaskSearchFavs runner = new AsyncTaskSearchFavs();
                runner.execute(value);
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
        MenuItem item = menu.add(0, MENU, Menu.NONE, getString(R.string.exportimportfavs));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        if (!nofavs) {
            //inflater.inflate(R.menu.favourites_menu, menu);
            MenuItem item2 = menu.add(1, MENU_SEARCH, Menu.NONE, getString(R.string.search)).setIcon(R.drawable.seach_icon);
            item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            MenuItem item3 = menu.add(2, MENU_SORT, Menu.NONE, R.string.sort).setIcon(R.drawable.sort);
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
            case MENU_SORT:
                sortDialog();
                return true;
            case MENU:
                final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.list_dialog);
                dialog.setCanceledOnTouchOutside(true);
                dialog.setCancelable(true);
                setDialogSize(dialog);

                TextView title = (TextView) dialog.findViewById(R.id.title);
                ListView listView = (ListView) dialog.findViewById(R.id.listView);

                title.setText(getActivity().getString(R.string.exportimportfavs));

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            SharedPreferences pref = getActivity().getSharedPreferences("FAVS", 0);
                            if (pref.getAll().isEmpty()) {
                                final Dialog dialog2 = new Dialog(getActivity(), R.style.myDialogStyle);
                                dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog2.setContentView(R.layout.alert_dialog);
                                dialog2.setCanceledOnTouchOutside(false);
                                dialog2.setCancelable(false);
                                setDialogSize(dialog2);

                                TextView title = (TextView) dialog2.findViewById(R.id.title);
                                TextView message = (TextView) dialog2.findViewById(R.id.message);
                                TextView ok = (TextView) dialog2.findViewById(R.id.ok);
                                TextView cancel = (TextView) dialog2.findViewById(R.id.cancel);

                                title.setText(getActivity().getString(R.string.error));
                                message.setText(getActivity().getString(R.string.nofavsfound));
                                ok.setText("OK");
                                cancel.setVisibility(View.GONE);

                                ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog2.dismiss();
                                    }
                                });

                                dialog2.show();
                            } else {
                                AsyncTaskExportFavs runner = new AsyncTaskExportFavs();
                                runner.execute();
                                dialog.dismiss();
                            }
                        } else if (position == 1) {
                            Intent i = new Intent(getContext(), FilePickerActivity.class);
                            // This works if you defined the intent filter
                            // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                            // Set these depending on your use case. These are the defaults.
                            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                            // Configure initial directory by specifying a String.
                            // You could specify a String like "/storage/emulated/0/", but that can
                            // dangerous. Always use Android's API calls to get paths to the SD-card or
                            // internal memory.
                            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath() + "/easeWave/");

                            startActivityForResult(i, FILE_CODE);

                            dialog.dismiss();
                            /*if (loadSharedPreferencesFromFile()) {
                                // reload favs into cards
                                AsyncTaskReadFavs runner = new AsyncTaskReadFavs();
                                runner.execute();

                                final Dialog dialog2 = new Dialog(getActivity(), R.style.myDialogStyle);
                                dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog2.setContentView(R.layout.alert_dialog);
                                dialog2.setCanceledOnTouchOutside(false);
                                dialog2.setCancelable(false);
                                setDialogSize(dialog2);

                                TextView title = (TextView) dialog2.findViewById(R.id.title);
                                TextView message = (TextView) dialog2.findViewById(R.id.message);
                                TextView ok = (TextView) dialog2.findViewById(R.id.ok);
                                TextView cancel = (TextView) dialog2.findViewById(R.id.cancel);

                                title.setText(getActivity().getString(R.string.success));
                                message.setText(getActivity().getString(R.string.favouritessuccessfulrestored));
                                ok.setText("OK");
                                cancel.setVisibility(View.GONE);

                                ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog2.dismiss();
                                        dialog.dismiss();
                                    }
                                });

                                dialog2.show();
                            }*/
                        } else if (position == 2) {
                            SharedPreferences pref = getActivity().getSharedPreferences("FAVS", 0);
                            if (pref.getAll().isEmpty()) {
                                final Dialog dialog2 = new Dialog(getActivity(), R.style.myDialogStyle);
                                dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog2.setContentView(R.layout.alert_dialog);
                                dialog2.setCanceledOnTouchOutside(false);
                                dialog2.setCancelable(false);
                                setDialogSize(dialog2);

                                TextView title = (TextView) dialog2.findViewById(R.id.title);
                                TextView message = (TextView) dialog2.findViewById(R.id.message);
                                TextView ok = (TextView) dialog2.findViewById(R.id.ok);
                                TextView cancel = (TextView) dialog2.findViewById(R.id.cancel);

                                title.setText(getActivity().getString(R.string.error));
                                message.setText(getActivity().getString(R.string.nofavsfound));
                                ok.setText("OK");
                                cancel.setVisibility(View.GONE);

                                ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog2.dismiss();
                                        dialog.dismiss();
                                    }
                                });

                                dialog2.show();
                            } else {
                                AsyncTaskFavsList runner = new AsyncTaskFavsList();
                                runner.execute();
                                dialog.dismiss();
                            }
                        }
                    }
                });
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortDialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.myDialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.language_dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        setDialogSize(dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(R.string.sortby);

        final RadioButton dateAdded = (RadioButton) dialog.findViewById(R.id.en);
        final RadioButton author = (RadioButton) dialog.findViewById(R.id.de);
        dateAdded.setText(R.string.dateadded);
        author.setText(R.string.author);

        if (settingsprefs.getString("sortMethod", "dateAdded").equals("dateAdded")) {
            dateAdded.setChecked(true);
        } else if (settingsprefs.getString("sortMethod", "dateAdded").equals("author")) {
            author.setChecked(true);
        }

        RadioButton.OnClickListener toggleHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dateAdded.setChecked(false);
                author.setChecked(false);

                switch (v.getId()) {
                    case R.id.en:
                        dateAdded.setChecked(true);
                        settingsprefs.edit().putString("sortMethod", "dateAdded").apply();
                        break;
                    case R.id.de:
                        author.setChecked(true);
                        settingsprefs.edit().putString("sortMethod", "author").apply();
                        break;
                }
                sortCards();
                dialog.dismiss();
            }
        };

        dateAdded.setOnClickListener(toggleHandler);
        author.setOnClickListener(toggleHandler);

        dialog.show();
    }

    private void sortCards() {
        if (settingsprefs.getString("sortMethod", "dateAdded").equals("author")) {
            Collections.sort(cards, new Comparator<Card>() {
                @Override
                public int compare(Card card1, Card card2) {
                    // it is just an example, use your logic
                    return card1.getCardHeader().getTitle().compareTo
                            (card2.getCardHeader().getTitle());
                }
            });
        } else if (settingsprefs.getString("sortMethod", "dateAdded").equals("dateAdded")) {
            Collections.sort(cards, new Comparator<Card>() {
                @Override
                public int compare(Card card1, Card card2) {
                    // it is just an example, use your logic
                    return card2.getId().compareTo
                            (card1.getId());
                }
            });
        }

        mCardArrayAdapter.notifyDataSetChanged();
    }

    private class AsyncTaskSearchFavs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
            mRecyclerView.startAnimation(fade_out);
            mRecyclerView.setVisibility(View.INVISIBLE);
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
                Map<String, ?> keys = favsprefs.getAll();
                if (!favsprefs.getAll().isEmpty()) {
                    int index = 0;
                    for (Map.Entry<String, ?> entry : keys.entrySet()) {
                        for (int i = 0; i < keywords.size(); i++) {
                            String quote = entry.getValue().toString().split("\\}")[0];
                            String source = entry.getValue().toString().split("\\}")[1];
                            if (quote.toLowerCase().contains(keywords.get(i).trim().toLowerCase()) || source.toLowerCase().contains(keywords.get(i).trim().toLowerCase())) {

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

                                card.setId(date);
                                card.getCardHeader().setId(String.valueOf(index));

                                card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
                                    @Override
                                    public void onExpandEnd(Card card) {
                                        SnackbarManager.dismiss();
                                        quotestr = card.getTitle();
                                        sourcestr = card.getCardHeader().getTitle();
                                        date = card.getId();
                                        selectedCard = card;
                                    }
                                });

                                cards.add(card);

                                index++;
                            }
                        }
                    }
                    if (cards.size() < 1) {
                        // no search results
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
                // execution of result of Long time consuming operation
                mCardArrayAdapter = new CardArrayRecyclerViewAdapter(getActivity(), cards);

                mRecyclerView.setAdapter(mCardArrayAdapter);

                if (!mRecyclerView.isShown()) {
                    mRecyclerView.startAnimation(fade_in);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                progressBar.startAnimation(fade_out);
                progressBar.setVisibility(View.INVISIBLE);

                //searchResults.startAnimation(fade_in2);
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
                        //mRecyclerView.startAnimation(fade_out2);
                        //mRecyclerView.setVisibility(View.INVISIBLE);
                        //searchResults.startAnimation(fade_out);
                        searchResults.setVisibility(View.GONE);
                        cards.clear();
                        searchTerm.setText("");
                        AsyncTaskReadFavs runner = new AsyncTaskReadFavs();
                        runner.execute();
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

    private void importsuccess() {
        final Dialog dialog2 = new Dialog(getActivity(), R.style.myDialogStyle);
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog2.setContentView(R.layout.alert_dialog);
        dialog2.setCanceledOnTouchOutside(false);
        dialog2.setCancelable(false);
        setDialogSize(dialog2);

        TextView title = (TextView) dialog2.findViewById(R.id.title);
        TextView message = (TextView) dialog2.findViewById(R.id.message);
        TextView ok = (TextView) dialog2.findViewById(R.id.ok);
        TextView cancel = (TextView) dialog2.findViewById(R.id.cancel);

        title.setText(getActivity().getString(R.string.success));
        message.setText(getActivity().getString(R.string.favouritessuccessfulrestored));
        ok.setText("OK");
        cancel.setVisibility(View.GONE);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
            }
        });

        dialog2.show();
    }

    private void importerror() {
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

        title.setText(R.string.error);
        message.setText(R.string.somethingwentwrong);
        ok.setText("OK");
        cancel.setVisibility(View.GONE);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
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
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            // reread favs bc cards is null
            AsyncTaskReadFavs runner = new AsyncTaskReadFavs();
            runner.execute();
        }
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            // Do something with the URI
                            //loadSharedPreferencesFromFile(uri);
                            AsyncTaskImportFavs runner = new AsyncTaskImportFavs();
                            runner.execute(uri);
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path: paths) {
                            Uri uri = Uri.parse(path);
                            // Do something with the URI
                            //loadSharedPreferencesFromFile(uri);
                            AsyncTaskImportFavs runner = new AsyncTaskImportFavs();
                            runner.execute(uri);
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                // Do something with the URI
                //loadSharedPreferencesFromFile(uri);
                AsyncTaskImportFavs runner = new AsyncTaskImportFavs();
                runner.execute(uri);
            }
        }
    }
}
