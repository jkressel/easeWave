package com.jonathansautter.easewave;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class IntroSlide extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private View v;

    public static IntroSlide newInstance(int layoutResId) {
        IntroSlide sampleSlide = new IntroSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    private int layoutResId;

    public IntroSlide() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID))
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(layoutResId, container, false);

        setup();

        return v;
    }

    private void setup() {
        Typeface light = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");
        TextView title = (TextView) v.findViewById(R.id.title);
        title.setTypeface(light);
    }
}