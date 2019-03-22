package uk.co.ecorunners.ecorunners.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.co.ecorunners.ecorunners.activity.MainActivity;

import static uk.co.ecorunners.ecorunners.utils.Constants.FRIDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.LASTNAME;
import static uk.co.ecorunners.ecorunners.utils.Constants.MONDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.NAME;
import static uk.co.ecorunners.ecorunners.utils.Constants.ROTA;
import static uk.co.ecorunners.ecorunners.utils.Constants.SATURDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.SUNDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.THURSDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.TUESDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.WEDNESDAY;

public class MainActivityUtil {
    private Map<String, Set<String>> userIDToNameLastName;
    private Utils util;
    private Map<String, Set<String>> dayToUserIDOnCover;
    private String loggedInUser;

    public Map readDBToFindNameLastNameByID(DatabaseReference allUsersInUsersLevel, final String loggedInUserID, final TextView welcomeText) {

        loggedInUser = loggedInUserID;

        userIDToNameLastName = new HashMap<>();

        allUsersInUsersLevel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (DataSnapshot userIDLevel : dataSnapshot.getChildren()) {

                    final Set<String> nameAndLastName = new LinkedHashSet<>();

                    for (DataSnapshot childrenOfID : userIDLevel.getChildren()) {

                        getNameAndLastName(userIDLevel, nameAndLastName, childrenOfID);

                    }
                }
                if(welcomeText != null) {

                    setNameLastNameToWelcomeText(loggedInUserID, welcomeText);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                /*Not used */
            }

        });
        return userIDToNameLastName;
    }

    private void getNameAndLastName(DataSnapshot userIDLevel, Set<String> nameAndLastName, DataSnapshot childrenOfID) {

        if (childrenOfID.getKey().equals(NAME)) {

            String name = childrenOfID.getValue(String.class);

            if (userIDToNameLastName.containsKey(userIDLevel.getKey())) {

                userIDToNameLastName.get(userIDLevel.getKey()).add("name:" + name);

            }

            else {

                nameAndLastName.add("name:" + name);

                userIDToNameLastName.put(userIDLevel.getKey(), nameAndLastName);
            }
        }

        if (childrenOfID.getKey().equals(LASTNAME)) {

            String lastname = childrenOfID.getValue(String.class);

            if (userIDToNameLastName.containsKey(userIDLevel.getKey())) {

                userIDToNameLastName.get(userIDLevel.getKey()).add("lastname:" +
                        lastname);

            }

            else {

                nameAndLastName.add("lastname:" + lastname);

                userIDToNameLastName.put(userIDLevel.getKey(), nameAndLastName);

            }
        }
    }

    public void setNameLastNameToWelcomeText(String loggedInUserID, TextView welcomeText) {

        if (!userIDToNameLastName.isEmpty()) {

            Set<String> nameLastName = userIDToNameLastName.get(loggedInUserID);

            String name = "";

            String lastname = "";

            for (String value : nameLastName) {

                if (value.startsWith(NAME)) {

                    name = value.substring(value.indexOf(':') + 1, value.length());

                } else if (value.startsWith(LASTNAME)) {

                    lastname = value.substring(value.indexOf(':') + 1, value.length());

                }
            }

            welcomeText.append(" " + name + " " + lastname);
        }
    }

    public boolean findUsersOnCoverAndNotify(String weekDay, String typeOfNotification, Set<String> userIDToNotifyLocal, String loggedInUser, Activity activity) {

        boolean sentToCovers = false;

        for (Map.Entry<String, Set<String>> entry : ((MainActivity)activity).getDayToUserIDOnCover().entrySet()) {

            if (entry.getKey().equals(weekDay)) {

                Set<String> list = entry.getValue();

                for (String user_IDs : list) {

                    userIDToNotifyLocal.add(user_IDs);

                }
            }
        }

        for (String user_ID : userIDToNotifyLocal) {

            // we dont want to send notifications to the same person when they are on
            // cover and accidentally press Emergency
            if (!loggedInUser.equals(user_ID)) {

                sendNotificationsToUser(user_ID, typeOfNotification, ((MainActivity)activity).getUserIDToNameLastNameNonStatic(),loggedInUser);

                sentToCovers = true;

            }
        }
        return sentToCovers;
    }

    public boolean findAdminsAndNotify(String typeOfNotification, Set<String> adminUsersID, String loggedInUser, Activity activity) {

        boolean sentToAdmins = false;

        for (String user_ID : adminUsersID) {

            // we dont want to send notifications to the same person when they are on
            // cover and accidentally press Emergency
            if (!loggedInUser.equals(user_ID)) {

                sendNotificationsToUser(user_ID, typeOfNotification, ((MainActivity)activity).getUserIDToNameLastNameNonStatic(), loggedInUser);

                sentToAdmins = true;
            }
        }
        return sentToAdmins;
    }

    private void sendNotificationsToUser(final String user_ID, final String typeOfNotification, final Map<String, Set<String>> userIDToNameLastNameNonStatic, final String loggedInUserID) {

        util = new Utils();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                int sdkInt = android.os.Build.VERSION.SDK_INT;

                if (sdkInt > 8) {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                    String name = getFirstName(userIDToNameLastNameNonStatic, loggedInUserID);
                    String lastname = getLastName(userIDToNameLastNameNonStatic, loggedInUserID)  ;

                    try {

                        URL urlLocal = new URL("https://onesignal.com/api/v1/notifications");

                        HttpURLConnection con = (HttpURLConnection) urlLocal.openConnection();

                        con.setUseCaches(false);

                        con.setDoOutput(true);

                        con.setDoInput(true);


                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                        con.setRequestProperty("Authorization", "Basic " +
                                "YmMwYjdlMzItYzYzYi00NjJjLTk1MTktNjU5NjcyNTFlOWZm");

                        con.setRequestMethod("POST");

                        String strJsonBody = "";

                        if (typeOfNotification.equals("emergency")) {

                            strJsonBody = "{"
                                    + "\"app_id\": \"4b78e5b2-4259-476d-9d30-af4419c6556f\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", " +
                                    "\"relation\": \"=\", \"value\": \"" + user_ID + "\"}],"

                                    + "\"data\": {\"" + name + "\": \"" + lastname + " called for" +
                                    " Emergency\"},"
                                    + "\"contents\": {\"en\": \"Emergency button is clicked!\"}"
                                    + "}";
                        }

                        else if (typeOfNotification.equals("support")) {

                            strJsonBody = "{"
                                    + "\"app_id\": \"4b78e5b2-4259-476d-9d30-af4419c6556f\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", " +
                                    "\"relation\": \"=\", \"value\": \"" + user_ID + "\"}],"

                                    + "\"data\": {\"" + name + " \": \"" + lastname + " called " +
                                    "for Support\"},"
                                    + "\"contents\": {\"en\": \"Support button is clicked!\"}"
                                    + "}";
                        }

                        Log.i("strJsonBody:\n" , strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);

                        util.getJsonResponseFromHttpUrlConnection(con, sendBytes);

                    }

                    catch (Exception t) {

                        Log.e("Exception", t.getMessage());
                    }
                }
            }
        });
    }

    private String getFirstName(Map<String, Set<String>> userIDToNameLastNameNonStatic, String loggedInUserID) {

        String name = "";

        Set<String> values = userIDToNameLastNameNonStatic.get(loggedInUserID);

        for (String valueName : values) {

            if (valueName.startsWith(NAME)) {

                String[] namesArray = valueName.split(":");

                name = namesArray[1];
            }
        }
        return name;
    }

    private String getLastName(Map<String, Set<String>> userIDToNameLastNameNonStatic, String loggedInUserID) {

        String lastname = "";

        Set<String> values = userIDToNameLastNameNonStatic.get(loggedInUserID);

        for (String valueName : values) {

            if (valueName.startsWith(LASTNAME)) {

                String [] namesArray = valueName.split(":");

                lastname = namesArray[1];
            }
        }
        return lastname;
    }

    public Map readDBToFindCouriersOnCover() {

        DatabaseReference url = FirebaseDatabase.getInstance().getReference();

        dayToUserIDOnCover = new HashMap<>();

        DatabaseReference allUsersID = url.child(ROTA);

        allUsersID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (DataSnapshot userIDLevel : dataSnapshot.getChildren()) {

                    for (DataSnapshot childrenOfUserID : userIDLevel.getChildren()) {

                        final Set<String> userIDs = new LinkedHashSet<>();

                        for (DataSnapshot workingWeekLevelChildren : childrenOfUserID.getChildren()) {

                            switch (workingWeekLevelChildren.getKey()) {

                                case MONDAY:

                                    dayToUserIDOnCover = checkIfUserIsOnCoverThisDay(MONDAY,
                                            workingWeekLevelChildren, userIDLevel, userIDs);
                                    break;

                                case TUESDAY:
                                    dayToUserIDOnCover = checkIfUserIsOnCoverThisDay(TUESDAY,
                                            workingWeekLevelChildren, userIDLevel, userIDs);

                                    break;

                                case WEDNESDAY:
                                    dayToUserIDOnCover = checkIfUserIsOnCoverThisDay(WEDNESDAY,
                                            workingWeekLevelChildren, userIDLevel, userIDs);

                                    break;

                                case THURSDAY:
                                    dayToUserIDOnCover = checkIfUserIsOnCoverThisDay(THURSDAY,
                                            workingWeekLevelChildren, userIDLevel, userIDs);

                                    break;

                                case FRIDAY:

                                    dayToUserIDOnCover = checkIfUserIsOnCoverThisDay(FRIDAY,
                                            workingWeekLevelChildren, userIDLevel, userIDs);

                                    break;

                                case SATURDAY:

                                    dayToUserIDOnCover = checkIfUserIsOnCoverThisDay(SATURDAY,
                                            workingWeekLevelChildren, userIDLevel, userIDs);
                                    break;

                                case SUNDAY:

                                    dayToUserIDOnCover = checkIfUserIsOnCoverThisDay(SUNDAY,
                                            workingWeekLevelChildren, userIDLevel, userIDs);

                                    break;

                                default:
                                    Log.i("readDB2FindCourierCover", workingWeekLevelChildren.getKey());
                                    break;
                            }
                        }// end of working week for loop
                    }
                }// end of main loop
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /*Not used */
            }
        });
        return dayToUserIDOnCover;
    }

    private Map checkIfUserIsOnCoverThisDay(String day, DataSnapshot workingWeekLevelChildren,
                                             DataSnapshot userIDLevel, final Set<String> userIDs) {

        if (workingWeekLevelChildren.getKey().equals(day)) {

            for (DataSnapshot childrenOfDay : workingWeekLevelChildren.getChildren()) {

                if (childrenOfDay.getKey().equals("OnCover") && childrenOfDay.getValue(String.class).equals("true")) {

                    // store in a map the day and the User ID that is OnCover
                    if (dayToUserIDOnCover.containsKey(day)) {

                        dayToUserIDOnCover.get(day).add(userIDLevel.getKey());

                    }

                    else {

                        userIDs.add(userIDLevel.getKey());

                        dayToUserIDOnCover.put(day, userIDs);

                    }
                }
            }
        }
        return dayToUserIDOnCover;

    }

    public String getLoggedInUser() {

        return loggedInUser;
    }


}
