package uk.co.ecorunners.ecorunners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by cousm on 06/10/2017.
 */

public class NotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {

    static Context context;
    public static Map<String, Set<String>> dayToUserID;
    public String userLogged_In_ID;
    public static Map<String, Set<String>> userIDToNameLastName;
    private static Set<String> user_ID_To_notify;
    private static LinkedHashSet<String> adminUsersID;

    public NotificationReceivedHandler (Context context) {
        this.context = context;
    }
    @Override
    public void notificationReceived(OSNotification notification) {

        JSONObject data = notification.payload.additionalData;

        String body = notification.payload.body;

        dayToUserID = MainActivity.getDayToUserID();

        userLogged_In_ID = MainActivity.getUserID();

        userIDToNameLastName = MainActivity.getUserIDToNameLastName();

        adminUsersID = MainActivity.getUserAdminsID();

        user_ID_To_notify = new LinkedHashSet<String>();

        // retrieve which users by user ID are on cover the current day
        for (String key: dayToUserID.keySet()) {

            if (key.equals(getTheCurrentDay ())) {

                user_ID_To_notify = dayToUserID.get(key);

            }
        }

        if (body != null) {

            if (body.contains("Emergency")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle("THIS IS AN EMERGENCY NOTIFICATION")
                        // remove the leading and trailing curly braces as well as the colon
                        .setMessage(data.toString().replaceAll("[{^\"|\"$}]", "").replaceAll(":", " "));

                AlertDialog alert1;

                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();

                                System.out.println("OK CLICKED");

                            }
                        });

                alert1 = builder.create();

                alert1.show();
            }

            else if (body.contains("Support")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle("THIS IS A SUPPORT NOTIFICATION")
                        // remove the leading and trailing curly braces as well as the colon
                        .setMessage(data.toString().replaceAll("[{^\"|\"$}]", "").replaceAll(":", " "));

                AlertDialog alert1;

                builder.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();

                                System.out.println("YES CLICKED");

                                // send notification to other cover couriers that have been sent support notification
                                for (String user_ID : user_ID_To_notify) {

                                    //exclude the logged in user and send notification to the other couriers who are onCover
                                    if (!user_ID.equals(userLogged_In_ID)){

                                        //send notification to other couriers
                                        sendNotificationsToUser(user_ID, "support");

                                    }
                                }

                                //send to management team to inform that this courier is going for support
                                for (String user_ID : adminUsersID) {

                                    if (!user_ID.equals(userLogged_In_ID)){

                                        //send notification to management
                                        sendNotificationsToUser(user_ID, "support");

                                    }

                                }
                            }
                        });

                builder.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                System.out.println("NO CLICKED");

                                // Do nothing
                                dialog.dismiss();

                            }
                        });

                alert1 = builder.create();

                alert1.show();

            }

            else if (body.contains("has accepted the support request")) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle("INFORMATION")
                        // remove the leading and trailing curly braces as well as the colon
                        .setMessage(data.toString().replaceAll("[{^\"|\"$}]", "").replaceAll(":", " ")+" has accepted the support request");

                AlertDialog alert1;

                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();

                                System.out.println("OK CLICKED");

                            }
                        });

                alert1 = builder.create();

                alert1.show();

            }

            else if (body.contains("You have shift in one hour")) {

                // remove the leading and trailing curly braces as well as the colon
                // and remove the leading : colon
                String messageToDisplay = data.toString().replaceAll("[{^\"|\"$}]", "");

                if (messageToDisplay.startsWith(":")) {

                    messageToDisplay = messageToDisplay.substring(1, data.toString().replaceAll("[{^\"|\"$}]", "").length());

                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle("ONE HOUR BEFORE SHIFT")
                        .setMessage(messageToDisplay);

                AlertDialog alert1;

                builder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();

                                System.out.println("OK CLICKED");
                            }
                });

                alert1 = builder.create();

                alert1.show();

            }
        }

    }

    private void sendNotificationsToUser (final String user_ID, final String typeOfNotification) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                int SDK_INT = android.os.Build.VERSION.SDK_INT;

                if (SDK_INT > 8) {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                    String name = "";

                    String lastname = "";

                    Set <String> values = userIDToNameLastName.get(userLogged_In_ID);

                    for (String valueName: values) {

                        if (valueName.startsWith("name")){

                            String namesArray[] = valueName.split(":");

                            name = namesArray[1];

                        }

                        if (valueName.startsWith("lastname")){

                            String namesArray[] = valueName.split(":");

                            lastname = namesArray[1];

                        }
                    }

                    try {

                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");

                        HttpURLConnection con = (HttpURLConnection) url.openConnection();

                        con.setUseCaches(false);

                        con.setDoOutput(true);

                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                        con.setRequestProperty("Authorization", "Basic YmMwYjdlMzItYzYzYi00NjJjLTk1MTktNjU5NjcyNTFlOWZm");

                        con.setRequestMethod("POST");

                        String strJsonBody = "";

                        if (typeOfNotification.equals("support")){

                            strJsonBody = "{"
                                    + "\"app_id\": \"4b78e5b2-4259-476d-9d30-af4419c6556f\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + user_ID + "\"}],"

                                    + "\"data\": {\"" + name + " \": \"" + lastname + "\"},"
                                    + "\"contents\": {\"en\": \"Courier "+name +" "+lastname+" has accepted the support request.\"}"
                                    + "}";
                        }

                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");

                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();

                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();

                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {

                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");

                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                            scanner.close();

                        }

                        else {

                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");

                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                            scanner.close();

                        }

                        System.out.println("jsonResponse:\n" + jsonResponse);

                    }

                    catch (Throwable t) {

                        t.printStackTrace();

                    }
                }
            }
        });
    }

    private String getTheCurrentDay () {

        //get the current day first
        String weekDay;

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

        Calendar calendar = Calendar.getInstance();

        weekDay = dayFormat.format(calendar.getTime());

        return weekDay;

    }

}
