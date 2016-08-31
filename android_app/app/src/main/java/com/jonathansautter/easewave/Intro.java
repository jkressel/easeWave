package com.jonathansautter.easewave;

import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;

public class Intro extends AppIntro2 {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(IntroSlide.newInstance(R.layout.intro_slide1));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide2));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide3));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide4));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide5));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide6));
        addSlide(IntroSlide.newInstance(R.layout.intro_slide7));
        //addSlide(second_fragment);
        //addSlide(third_fragment);
        //addSlide(fourth_fragment);

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        //addSlide(AppIntroFragment.newInstance("Test", "description", image, Color.parseColor("#3F51B5")));

        // OPTIONAL METHODS
        // Override bar/separator color
        //setBarColor(Color.parseColor("#3F51B5"));
        //setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button
        //showSkipButton(false);
        //showDoneButton(false);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        setVibrate(false);
        //setVibrateIntensity(30);
    }

    /*@Override
    public void onSkipPressed() {
        // Do something when users tap on Skip button.
    }*/

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        Intro.this.finish();
    }
}