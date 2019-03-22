package uk.co.ecorunners;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import uk.co.ecorunners.ecorunners.AlarmReceiver;
import uk.co.ecorunners.ecorunners.activity.LoginActivity;

@RunWith(RobolectricTestRunner.class)
public class AlarmReceiverTests {
    private LoginActivity loginActivity;
    private Context context;

    @Before
    public void setUp(){

        FirebaseApp.initializeApp(RuntimeEnvironment.application);
        loginActivity = Robolectric.buildActivity(LoginActivity.class)
                .create()
                .visible()
                .get();
    }

    @Test
    public void testSetNotificationBuilderActionOne(){

        AlarmManager alarmManager = (AlarmManager) loginActivity.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(loginActivity, AlarmReceiver.class);

        notificationIntent.setAction("1");

        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(loginActivity, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), broadcast);
        AlarmReceiver alarmReceiver = new AlarmReceiver();

        alarmReceiver.onReceive(RuntimeEnvironment.application,notificationIntent);

    }

    @Test
    public void testSetNotificationBuilderActionTwo(){

        AlarmManager alarmManager = (AlarmManager) loginActivity.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(loginActivity, AlarmReceiver.class);

        notificationIntent.setAction("2");

        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(loginActivity, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), broadcast);
        AlarmReceiver alarmReceiver = new AlarmReceiver();

        alarmReceiver.onReceive(RuntimeEnvironment.application,notificationIntent);

    }
}
