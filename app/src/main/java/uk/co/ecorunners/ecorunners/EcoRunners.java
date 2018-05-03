package uk.co.ecorunners.ecorunners;

import android.app.Application;
import android.content.Context;

import com.firebase.client.Firebase;

/**
 * Created by cousm on 02/08/2017.
 */

public class EcoRunners extends Application {

    private static Context context;

    @Override
    public void onCreate() {

        super.onCreate();

        Firebase.setAndroidContext(this);

        EcoRunners.context = getApplicationContext();

    }

    public static Context getAppContext() {
        return EcoRunners.context;
    }

}
