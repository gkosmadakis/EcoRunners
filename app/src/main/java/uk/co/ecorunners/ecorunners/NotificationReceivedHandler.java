package uk.co.ecorunners.ecorunners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uk.co.ecorunners.ecorunners.utils.MainActivityUtil;
import uk.co.ecorunners.ecorunners.utils.Utils;

import static uk.co.ecorunners.ecorunners.utils.Constants.CURLY_BRACES_REGEX;
import static uk.co.ecorunners.ecorunners.utils.Constants.LASTNAME;
import static uk.co.ecorunners.ecorunners.utils.Constants.NAME;
import static uk.co.ecorunners.ecorunners.utils.Constants.OK_CLICKED;
import static uk.co.ecorunners.ecorunners.utils.Constants.SUPPORT;
import static uk.co.ecorunners.ecorunners.utils.Constants.USERS;

/**
 * Created by cousm on 06/10/2017.
 */

public class NotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {

    private Context context;
    private String userLoggedInID;
    private Map<String, Set<String>> userIDToNameLastName;
    private Set<String> userIDToNotify;
    private Set<String> adminUsersID;
    private Utils util;
    private AlertDialog alert;

    public NotificationReceivedHandler(Context context) {

        this.context = context;
        util = new Utils();
    }

    @Override
    public void notificationReceived(OSNotification notification) {

        JSONObject data = notification.payload.additionalData;

        String body = notification.payload.body;

        MainActivityUtil mainUtil = new MainActivityUtil();

        Map<String, Set<String>> dayToUserID = mainUtil.readDBToFindCouriersOnCover();

        userLoggedInID = mainUtil.getLoggedInUser();

        userIDToNameLastName = mainUtil.readDBToFindNameLastNameByID(FirebaseDatabase.getInstance().getReference().child(USERS),userLoggedInID,null);

        DatabaseReference url = FirebaseDatabase.getInstance().getReference();

        adminUsersID = util.readDBToFindAdminUsers(url);

        userIDToNotify = findUsersOnCover(dayToUserID);

        if (body != null) {

            if (body.contains("Emergency")) {

                showEmergencyNotification(data);
            }

            else if (body.contains("Support")) {

                showSupportNotification(data);

            }

            else if (body.contains("has accepted the support request")) {

                showHasAcceptedSupportRequest(data);

            }

            else if (body.contains("You have shift in one hour")) {

                showNotificationOneHourBeforeShift(data);

            }
        }

    }

    public void showNotificationOneHourBeforeShift(JSONObject data) {
        // remove the leading and trailing curly braces as well as the colon
        // and remove the leading : colon
        String messageToDisplay = data.toString().replaceAll(CURLY_BRACES_REGEX, "");

        if (messageToDisplay.startsWith(":")) {

            messageToDisplay = messageToDisplay.substring(1, data.toString().replaceAll(CURLY_BRACES_REGEX, "").length());

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog))
                .setTitle("ONE HOUR BEFORE SHIFT")
                .setMessage(messageToDisplay);

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        Log.i("Ok Clicked", OK_CLICKED);
                    }
        });

        alert = builder.create();

        alert.show();
    }

    public void showHasAcceptedSupportRequest(JSONObject data) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog))
                .setTitle("INFORMATION")
                // remove the leading and trailing curly braces as well as the colon
                .setMessage(data.toString().replaceAll(CURLY_BRACES_REGEX, "").replaceAll(":", " ")+" has accepted the support request");

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        Log.i("Ok Clicked ", OK_CLICKED);

                    }
                });

        alert = builder.create();

        alert.show();
    }

    public void showSupportNotification(JSONObject data) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog))
                .setTitle("THIS IS A SUPPORT NOTIFICATION")
                // remove the leading and trailing curly braces as well as the colon
                .setMessage(data.toString().replaceAll(CURLY_BRACES_REGEX, "").replaceAll(":", " "));

        builder.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        Log.i("Yes Clicked ","YES CLICKED");

                        // send notification to other cover couriers that have been sent support notification
                        for (String user_ID : userIDToNotify) {

                            //exclude the logged in user and send notification to the other couriers who are onCover
                            if (!user_ID.equals(userLoggedInID)){

                                //send notification to other couriers
                                sendNotificationsToUser(user_ID, userIDToNameLastName, userLoggedInID);

                            }
                        }

                        //send to management team to inform that this courier is going for support
                        for (String user_ID : adminUsersID) {

                            if (!user_ID.equals(userLoggedInID)){

                                //send notification to management
                                sendNotificationsToUser(user_ID, userIDToNameLastName, userLoggedInID);

                            }

                        }
                    }
                });

        builder.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.i("No Clicked ", "NO CLICKED");

                        // Do nothing
                        dialog.dismiss();

                    }
                });

        alert = builder.create();

        alert.show();
    }

    public void showEmergencyNotification(JSONObject data) {

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog))
                .setTitle("THIS IS AN EMERGENCY NOTIFICATION")
                // remove the leading and trailing curly braces as well as the colon
                .setMessage(data.toString().replaceAll(CURLY_BRACES_REGEX, "").replaceAll(":", " "));

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        Log.i("OK Clicked", OK_CLICKED);

                    }
                });

        alert = builder.create();

        alert.show();
    }

    public Set findUsersOnCover(Map<String, Set<String>> dayToUserID) {

        userIDToNotify = new LinkedHashSet<>();

        // retrieve which users by user ID are on cover the current day
        for (Map.Entry<String, Set<String>> entry: dayToUserID.entrySet()) {

            if (entry.getKey().equals(getTheCurrentDay())) {

                userIDToNotify = dayToUserID.get(entry.getKey());

            }
        }
        return userIDToNotify;
    }

    private void sendNotificationsToUser(final String user_ID, final Map userIDToNameLastName, final String userLoggedInID) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                int sdkInt = android.os.Build.VERSION.SDK_INT;

                if (sdkInt > 8) {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                    String name = getFirstName(userIDToNameLastName, userLoggedInID);

                    String lastname = getLastName(userIDToNameLastName, userLoggedInID);

                    try {

                        URL url = new URL("https://onesignal.com/api/v1/notifications");

                        HttpURLConnection con = (HttpURLConnection) url.openConnection();

                        con.setUseCaches(false);

                        con.setDoOutput(true);

                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                        con.setRequestProperty("Authorization", "Basic YmMwYjdlMzItYzYzYi00NjJjLTk1MTktNjU5NjcyNTFlOWZm");

                        con.setRequestMethod("POST");

                        String strJsonBody = "";

                        strJsonBody = "{"
                                + "\"app_id\": \"4b78e5b2-4259-476d-9d30-af4419c6556f\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + user_ID + "\"}],"

                                + "\"data\": {\"" + name + " \": \"" + lastname + "\"},"
                                + "\"contents\": {\"en\": \"Courier "+name +" "+lastname+" has accepted the support request.\"}"
                                + "}";

                        Log.i("strJsonBody:\n", strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);

                        util.getJsonResponseFromHttpUrlConnection(con, sendBytes);
                    }

                    catch (Exception t) {

                        Log.e("Exception", t.getMessage());

                    }
                }
            }
        }
        );
    }

    public String getFirstName(Map userIDToNameLastName, String userLoggedInID) {

        String name = "";

        Set<String> values = (Set<String>) userIDToNameLastName.get(userLoggedInID);

        for (String valueName: values) {

            if (valueName.startsWith(NAME)){

                String [] namesArray = valueName.split(":");

                name = namesArray[1];

            }
        }
        return name;
    }

    public String getLastName(Map userIDToNameLastName, String userLoggedInID) {

        String lastname = "";

        Set<String> values = (Set<String>) userIDToNameLastName.get(userLoggedInID);

        for (String valueName: values) {

            if (valueName.startsWith(LASTNAME)){

                String [] namesArray = valueName.split(":");

                lastname = namesArray[1];

            }
        }
        return lastname;
    }

    public String getTheCurrentDay () {

        //get the current day first
        String weekDay;

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

        Calendar calendar = Calendar.getInstance();

        weekDay = dayFormat.format(calendar.getTime());

        return weekDay;

    }

    public AlertDialog getAlert() {
        return alert;
    }
}
