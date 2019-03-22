package uk.co.ecorunners.ecorunners;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import uk.co.ecorunners.ecorunners.activity.MainActivity;
import uk.co.ecorunners.ecorunners.utils.Utils;

public class AlarmReceiver extends BroadcastReceiver{
    public static final String ACTION_ONE = "1";
    public static final String ACTION_TWO = "2";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notificationIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(notificationIntent);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = setNotificationBuilder(context, intent, soundUri, pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);

    }

    public Notification setNotificationBuilder(Context context, Intent intent, Uri soundUri, PendingIntent pendingIntent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Channel_ID");

        builder.setContentTitle("EcoRunners");

        if (intent.getAction().equalsIgnoreCase(ACTION_ONE)) {

            builder.setContentTitle("Schedule Change");

            builder.setContentText("There is a change in the schedule");

        }

        else if (intent.getAction().equalsIgnoreCase(ACTION_TWO)){

            builder.setContentTitle("You have not filled all your deliveries for this week");

            builder.setContentText("You have not filled all your deliveries for this week. Please supply them on your schedule by clicking on the day");
        }

        builder.setTicker("EcoRunners");

        Utils utils = new Utils();

        builder.setSmallIcon(utils.getNotificationIcon());

        builder.setWhen(System.currentTimeMillis());

        builder.setContentIntent(pendingIntent).build();

        // add sound
        builder.setSound(soundUri);

        builder.setOngoing(true);

        // add vibration
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        return builder.build();
    }

}