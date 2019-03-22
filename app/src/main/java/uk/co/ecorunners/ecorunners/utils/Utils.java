package uk.co.ecorunners.ecorunners.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import uk.co.ecorunners.ecorunners.AlarmReceiver;
import uk.co.ecorunners.ecorunners.R;
import uk.co.ecorunners.ecorunners.activity.ListCalendarActivity;

import static uk.co.ecorunners.ecorunners.activity.ListCalendarActivity.PENDING_ICON;
import static uk.co.ecorunners.ecorunners.utils.Constants.FOURTH_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.SECOND_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.THIRD_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.USERS;
import static uk.co.ecorunners.ecorunners.utils.Constants.UTF8;

public class Utils {

    public void getJsonResponseFromHttpUrlConnection(HttpURLConnection con, byte[] sendBytes) throws IOException {
        String jsonResponse;
        con.setFixedLengthStreamingMode(sendBytes.length);

        OutputStream outputStream = con.getOutputStream();

        outputStream.write(sendBytes);

        int httpResponse = con.getResponseCode();

        Log.i("httpResponse: ",  String.valueOf(httpResponse));

        if (httpResponse >= HttpURLConnection.HTTP_OK
                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {

            Scanner scanner = new Scanner(con.getInputStream(), UTF8);

            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            scanner.close();

        }
        else {

            Scanner scanner = new Scanner(con.getErrorStream(), UTF8);

            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

            scanner.close();
        }

        Log.i("jsonResponse:\n", jsonResponse);
    }

    public Set readDBToFindAdminUsers(DatabaseReference url) {

        DatabaseReference allUsersInUsersLevel = url.child(USERS);

        final Set adminUsersID = new LinkedHashSet<>();

        allUsersInUsersLevel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (DataSnapshot userIDLevel : dataSnapshot.getChildren()) {

                    for (DataSnapshot childrenOfID : userIDLevel.getChildren()) {

                        if (childrenOfID.getKey().equals("isAdmin")) {

                            boolean isAdmin = childrenOfID.getValue(Boolean.class);

                            if (isAdmin) {

                                adminUsersID.add(userIDLevel.getKey());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /*Not used */
            }
        });
        return adminUsersID;
    }

    public boolean clearSchedule (Activity activity) {

        String placeStoredMon = "";
        String placeStoredTue = "";
        String placeStoredWed = "";
        String placeStoredThu = "";
        String placeStoredFri = "";
        String placeStoredSat = "";
        String placeStoredSun = "";
        String timeStoredMon = "";
        String timeStoredTue = "";
        String timeStoredWed = "";
        String timeStoredThu = "";
        String timeStoredFri = "";
        String timeStoredSat = "";
        String timeStoredSun = "";

        ((ListCalendarActivity)activity).getMap().put(SECOND_COLUMN, placeStoredMon);
        ((ListCalendarActivity)activity).getMap2().put(SECOND_COLUMN, placeStoredTue);
        ((ListCalendarActivity)activity).getMap3().put(SECOND_COLUMN, placeStoredWed);
        ((ListCalendarActivity)activity).getMap4().put(SECOND_COLUMN, placeStoredThu);
        ((ListCalendarActivity)activity).getMap5().put(SECOND_COLUMN, placeStoredFri);
        ((ListCalendarActivity)activity).getMap6().put(SECOND_COLUMN, placeStoredSat);
        ((ListCalendarActivity)activity).getMap7().put(SECOND_COLUMN, placeStoredSun);

        ((ListCalendarActivity)activity).getMap().put(THIRD_COLUMN, timeStoredMon);
        ((ListCalendarActivity)activity).getMap2().put(THIRD_COLUMN, timeStoredTue);
        ((ListCalendarActivity)activity).getMap3().put(THIRD_COLUMN, timeStoredWed);
        ((ListCalendarActivity)activity).getMap4().put(THIRD_COLUMN, timeStoredThu);
        ((ListCalendarActivity)activity).getMap5().put(THIRD_COLUMN, timeStoredFri);
        ((ListCalendarActivity)activity).getMap6().put(THIRD_COLUMN, timeStoredSat);
        ((ListCalendarActivity)activity).getMap7().put(THIRD_COLUMN, timeStoredSun);
        populateWithPendingIcon(((ListCalendarActivity)activity).getMap(),activity);
        populateWithPendingIcon(((ListCalendarActivity)activity).getMap2(),activity);
        populateWithPendingIcon(((ListCalendarActivity)activity).getMap3(),activity);
        populateWithPendingIcon(((ListCalendarActivity)activity).getMap4(),activity);
        populateWithPendingIcon(((ListCalendarActivity)activity).getMap5(),activity);
        populateWithPendingIcon(((ListCalendarActivity)activity).getMap6(),activity);
        populateWithPendingIcon(((ListCalendarActivity)activity).getMap7(),activity);

        // update the list view
        ((ListCalendarActivity)activity).getListView().setAdapter(((ListCalendarActivity)activity).getAdapter());

        // this is to implement 1.2A till 1.2C user should not be able to click on a schedule ahead from the current week
        ((ListCalendarActivity)activity).setScheduleIsCleared(true);

        return true;
    }

    private void populateWithPendingIcon (Map tempMap, Activity activity) {

        RowItem item = new RowItem(PENDING_ICON);

        tempMap.put(FOURTH_COLUMN, String.valueOf(PENDING_ICON));

        ((ListCalendarActivity)activity).getRowItems().add(item);
    }

    public void triggerNotificationOnShiftChange (String action, Activity activity)  {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

            CharSequence name = "name";

            String desc = "description";

            int imp = NotificationManager.IMPORTANCE_HIGH;

            final String ChannelID = "my_channel_01";

            NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    imp);

            mChannel.setDescription(desc);

            mChannel.setLightColor(Color.CYAN);

            mChannel.canShowBadge();

            mChannel.setShowBadge(true);

            notificationManager.createNotificationChannel(mChannel);

            final int ncode = 101;

            Notification n = new Notification.Builder(activity, ChannelID)
                    .setContentTitle("Schedule Change")
                    .setContentText("There is a change in the schedule")
                    .setBadgeIconType(getNotificationIcon())
                    .setNumber(5)
                    .setSmallIcon(getNotificationIcon())
                    .setAutoCancel(true)
                    .build();

            n.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(ncode, n);
        }

        else {

            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent(activity, AlarmReceiver.class);

            notificationIntent.setAction(action);

            notificationIntent.addCategory("android.intent.category.DEFAULT");

            PendingIntent broadcast = PendingIntent.getBroadcast(activity, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), broadcast);
        }
    }

    public int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ecorunnerslogo : R.drawable.icon_silhouette;
    }

}
