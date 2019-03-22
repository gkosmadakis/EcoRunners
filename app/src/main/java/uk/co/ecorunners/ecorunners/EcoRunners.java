package uk.co.ecorunners.ecorunners;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.firebase.client.Firebase;

/**
 * Created by cousm on 02/08/2017.
 */

public class EcoRunners extends Application {

    @SuppressLint("StaticFieldLeak")
    private Context context;

    @Override
    public void onCreate() {

        super.onCreate();

        Firebase.setAndroidContext(this);

        this.context = getApplicationContext();

    }

}
