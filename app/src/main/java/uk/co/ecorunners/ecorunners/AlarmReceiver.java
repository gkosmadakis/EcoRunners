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

public class AlarmReceiver extends BroadcastReceiver{
    public final String ACTION_ONE = "1";
    public final String ACTION_TWO = "2";
    public final String ACTION_THREE = "3";
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notificationIntent = new Intent(context, SplashScreen.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(SplashScreen.class);

        stackBuilder.addNextIntent(notificationIntent);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        //Notification notification =
                builder.setContentTitle("EcoRunners");
        if (intent.getAction().equalsIgnoreCase(ACTION_ONE)) {

            builder.setStyle(new NotificationCompat.BigTextStyle().bigText("There is a change in the schedule"));

            builder.setContentText("There is a change in the schedule");

        }

        else if (intent.getAction().equalsIgnoreCase(ACTION_TWO)){

            builder.setStyle(new NotificationCompat.BigTextStyle().bigText("You have not filled all your deliveries for this week"));

            builder.setContentText("You have not filled all your deliveries for this week. Please supply them on your schedule by clicking on the day");
        }

        else if (intent.getAction().equalsIgnoreCase(ACTION_THREE)){

            builder.setStyle(new NotificationCompat.BigTextStyle().bigText("You have shift in one hour"));

            String timeStored = intent.getStringExtra("timeStored");

            builder.setContentText("You have shift in one hour at: " +timeStored);
            //LoginActivity.getNextTimeForNotificationMillis();

        }

        builder.setTicker("EcoRunners");

        builder.setSmallIcon(getNotificationIcon());

        builder.setWhen(System.currentTimeMillis());

        builder.setContentIntent(pendingIntent).build();

        // add sound
        builder.setSound(soundUri);

        // add vibration
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);

    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.icon_silhouette : R.drawable.icon_silhouette;
    }
}