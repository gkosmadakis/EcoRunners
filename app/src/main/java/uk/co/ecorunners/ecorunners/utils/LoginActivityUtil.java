package uk.co.ecorunners.ecorunners.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static uk.co.ecorunners.ecorunners.utils.Constants.APP_ID;
import static uk.co.ecorunners.ecorunners.utils.Constants.DATASTART;
import static uk.co.ecorunners.ecorunners.utils.Constants.FILTERS;
import static uk.co.ecorunners.ecorunners.utils.Constants.FILTERSEND;
import static uk.co.ecorunners.ecorunners.utils.Constants.ONE_HOUR_BEFORE_SHIFT;
import static uk.co.ecorunners.ecorunners.utils.Constants.PREFERENCES;
import static uk.co.ecorunners.ecorunners.utils.Constants.ROTA;
import static uk.co.ecorunners.ecorunners.utils.Constants.USER_ID;
import static uk.co.ecorunners.ecorunners.utils.Constants.WEEK;

public class LoginActivityUtil {
    private Context context;
    private String timeStored;

    public LoginActivityUtil(Context context) {
        this.context = context;
    }

    public void getTimeStoredFromDB(final Calendar calendar, final DatabaseReference url) {

        final Date date = calendar.getTime();

        //get the current day and pass it in the DB listener
        String currentDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        final String uid = preferences.getString(USER_ID,"");

        DatabaseReference workingDay = url.child(ROTA).child(uid).child(WEEK+getTheCurrentWorkingWeek()).child(currentDay);

        workingDay.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (com.google.firebase.database.DataSnapshot childrenOfDay : dataSnapshot.getChildren()) {

                    Log.i("childrenOfDay ",String.valueOf(childrenOfDay));

                    if (childrenOfDay.getKey().equals("Time")) {

                        if (!childrenOfDay.getValue(String.class).equals("OFF")) {

                            processCourierWhenNotOff(childrenOfDay, calendar, url, uid);
                        }

                        else if (childrenOfDay.getValue(String.class).equals("OFF")){

                            processCourierOffDay(calendar,url,uid);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e("OnCancelled ", String.valueOf(databaseError));

            }
        });
    }

    private void processCourierWhenNotOff(DataSnapshot childrenOfDay, Calendar calendar,final DatabaseReference url, final String uid) {

        timeStored = childrenOfDay.getValue(String.class);

        setTimeStored(timeStored);

        try {

            setCalendarOneHourBack(calendar);

        } catch (ParseException e) {

            Log.e("ParseException", e.getMessage());
        }

        //check if the current time is greater than the time it is scheduled to show notification for today
        if (System.currentTimeMillis() > calendar.getTime().getTime()) {

            Log.i("getTimeStoredFromDB ","SHIFT FOR TODAY HAS PASSED");

            calendar.add(Calendar.DATE, 1);

            getTimeStoredFromDBNextDay(calendar, url, uid);
        }

        // that means the loginActivity is called before the scheduled time for notification for today
        else {

            getNextTimeForNotificationMillis(calendar);

            sendNotificationsToUser(uid, ONE_HOUR_BEFORE_SHIFT, calendar.getTimeInMillis(), timeStored);

            Toast.makeText(context, "NOTIFICATION TO SHOW IS TRIGGERED", Toast.LENGTH_LONG).show();

        }
    }

    private void getTimeStoredFromDBNextDay(final Calendar calendar,final DatabaseReference url, final String uid) {

        final Date date = calendar.getTime();

        //get the next day and pass it in the DB listener
        String nextDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

        DatabaseReference workingDay = url.child(ROTA).child(uid).child("Week:"+getTheCurrentWorkingWeek()).child(nextDay);

        workingDay.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (com.google.firebase.database.DataSnapshot childrenOfDay : dataSnapshot.getChildren()) {

                    Log.i("childrenOfDay", String.valueOf(childrenOfDay));

                    if (childrenOfDay.getKey().equals("Time")) {

                        if (!childrenOfDay.getValue(String.class).equals("OFF")) {

                            timeStored = childrenOfDay.getValue(String.class);

                            setTimeStored(timeStored);

                            getNextTimeForNotificationMillis(calendar);

                            sendNotificationsToUser(uid, ONE_HOUR_BEFORE_SHIFT, calendar.getTimeInMillis(), timeStored);

                            Toast.makeText(context, "IT IS TRIGGERED", Toast.LENGTH_LONG).show();
                        }

                        else if (childrenOfDay.getValue(String.class).equals("OFF")) {

                            processCourierOffDay(calendar,url,uid);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.i("getTimeStoredDBNextDay", String.valueOf(databaseError));

            }
        });
    }


    public void processCourierOffDay(final Calendar calendar, DatabaseReference url, final String uid) {

        calendar.add(Calendar.DATE, 1);

        String nextDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.getTime());

        DatabaseReference nextWorkingDay = url.child(ROTA).child(uid).child("Week:" + getTheCurrentWorkingWeek()).child(nextDay);

        nextWorkingDay.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childrenOfDay : dataSnapshot.getChildren()) {

                    if (childrenOfDay.getKey().equals("Time") && !childrenOfDay.getValue(String.class).equals("OFF")) {

                        timeStored = childrenOfDay.getValue(String.class);

                        setTimeStored(timeStored);

                        getNextTimeForNotificationMillis(calendar);

                        sendNotificationsToUser(uid, ONE_HOUR_BEFORE_SHIFT, calendar.getTimeInMillis(), timeStored);

                        Toast.makeText(context, "NOTIFICATION IS TRIGGERED from OFF: ", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.e("On Cancelled ", String.valueOf(databaseError));

            }
        });
    }

    private String getTheCurrentWorkingWeek () {

        String week = "";

        String firstDayOfTheWeek = "";

        String lastDayOfTheWeek = "";

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        //the case when the current day is Sunday I need to show the current week MON-SUN
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        if (day == Calendar.SUNDAY) {

            lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            calendar.add(Calendar.DATE, -6);

            firstDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            week = firstDayOfTheWeek + " until " + lastDayOfTheWeek;

        } else {

            //get the Monday of the current week
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            firstDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            //here i get the first day of the week which is Sunday but is the Sunday of the previous week
            //and i add 6 days to get the Sunday of the current week
            calendar.add(Calendar.DATE, 6);

            lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            week = firstDayOfTheWeek + " until " + lastDayOfTheWeek;

        }
        return week;
    }

    private long getNextTimeForNotificationMillis(Calendar calendar) {

        long timeTillNextNotification = 0;

        if (timeStored != null) {

            try {

                setCalendarOneHourBack(calendar);

            } catch (ParseException e) {

                Log.e("ParseException",e.getMessage());
            }

            // get the time to get notified in milliseconds
            timeTillNextNotification = calendar.getTime().getTime();

            //store the long in preferences
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPrefs.edit();

            editor.putLong("timeTillNextNotification", timeTillNextNotification);

            editor.apply();
        }

        return timeTillNextNotification;
    }

    private void setCalendarOneHourBack(Calendar calendar) throws ParseException {

        String timeWhenShiftStarts = timeStored.substring(0, timeStored.indexOf('-'));

        Date dateWhenShiftStarts = null;

        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");

        String dateToday = formatDate.format(calendar.getTime());

        timeWhenShiftStarts = dateToday+" "+ timeWhenShiftStarts+":00";

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        dateWhenShiftStarts = format.parse(timeWhenShiftStarts);

        calendar.setTime(dateWhenShiftStarts);

        calendar.add(Calendar.HOUR, -1);
    }

    private void sendNotificationsToUser(final String user_ID, final String typeOfNotification, long time, final String shiftTime) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(time);

        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        final String timeToShowNotification = format.format(calendar.getTime());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                int sdkInt = android.os.Build.VERSION.SDK_INT;

                if (sdkInt > 8) {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                    String name = "";

                    String lastname = "";

                    try {

                        URL urlLocal = new URL("https://onesignal.com/api/v1/notifications");

                        HttpURLConnection con = (HttpURLConnection) urlLocal.openConnection();

                        con.setUseCaches(false);

                        con.setDoOutput(true);

                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                        con.setRequestProperty("Authorization", "Basic YmMwYjdlMzItYzYzYi00NjJjLTk1MTktNjU5NjcyNTFlOWZm");

                        con.setRequestMethod("POST");

                        String strJsonBody = "";

                        if (typeOfNotification.equals("emergency")) {

                            strJsonBody = "{"
                                    + APP_ID

                                    + FILTERS + user_ID + FILTERSEND

                                    + DATASTART + name + "\": \"" + lastname + " called for Emergency\"},"
                                    + "\"contents\": {\"en\": \"Emergency button is clicked!\"}"
                                    + "}";
                        }
                        else if (typeOfNotification.equals("support")){

                            strJsonBody = "{"
                                    + APP_ID

                                    + FILTERS + user_ID + FILTERSEND

                                    + DATASTART + name + " \": \"" + lastname + " called for Support\"},"
                                    + "\"contents\": {\"en\": \"Support button is clicked!\"}"
                                    + "}";
                        }

                        else if (typeOfNotification.equals("oneHourBeforeShift")){

                            strJsonBody = "{"
                                    + APP_ID

                                    + FILTERS + user_ID + FILTERSEND

                                    + DATASTART +"\":\"" + "You have shift in one hour at: " + shiftTime + "\"},"
                                    + "\"contents\": {\"en\": \"You have shift in one hour\"},"
                                    + "\"send_after\": \"" +timeToShowNotification + "\""
                                    + "}";
                        }

                        Log.i("strJsonBody:\n", strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);

                        Utils util = new Utils();

                        util.getJsonResponseFromHttpUrlConnection(con, sendBytes);

                    } catch (Exception t) {

                        Log.e("Exception", t.getMessage());
                    }
                }
            }
        });
    }

    private String setTimeStored(String timeStored){

        return  timeStored;

    }

}
