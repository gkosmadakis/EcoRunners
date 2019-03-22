package uk.co.ecorunners.ecorunners;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import uk.co.ecorunners.ecorunners.activity.LoginActivity;

/**
 * Created by cousm on 02/08/2017.
 */

public class SplashScreen extends Activity {

    // Splash screen timer
    private static int splashTimeOut = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);

                startActivity(i);

                // close this activity
                finish();
            }
        }, splashTimeOut);
    }
}
