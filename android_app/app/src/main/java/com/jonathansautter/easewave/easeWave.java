package com.jonathansautter.easewave;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

public class easeWave extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "uui8BnLPxppLXhfBAbdB8GhmBcrAmWjO1MWF6tBC", "41GytNU213bcT9qb4oJaERHincguCJnvnMFovhUI");
    }
}
