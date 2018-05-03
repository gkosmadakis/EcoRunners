package uk.co.ecorunners.ecorunners;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private static String loggedIn_User_ID;
    private static Map<String, Set<String>> dayToUserID;
    private static Map<String, Set<String>> userIDToNameLastName;
    private static LinkedHashSet<String> adminUsersID;
    private static Set<String> user_ID_To_notify;
    DatabaseReference url;
    FirebaseUser user;
    private TextView welcomeText;
    private Button rotaCalendarBtn, guidesBtn, managementRotaBtn, managementChatBtn,
            emergencyBtn, supportBtn;
    private String uid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static String getUserID() {

        return loggedIn_User_ID;
    }

    public static Map<String, Set<String>> getUserIDToNameLastName() {

        return userIDToNameLastName;
    }

    public static Map<String, Set<String>> getDayToUserID() {

        return dayToUserID;
    }

    public static LinkedHashSet getUserAdminsID() {

        return adminUsersID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle("Home");

        OneSignal.startInit(this)
                .setNotificationReceivedHandler(new NotificationReceivedHandler(this))
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {

                    Intent registerIntent = new Intent(MainActivity.this, LoginActivity.class);

                    registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(registerIntent);
                }
            }
        };

        SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String email = sharedPrefs.getString("email", "");

        Boolean isAdmin = sharedPrefs.getBoolean("isAdminUser", false);

        uid = sharedPrefs.getString("userID", "");

        // send tags with the ID of the current user
        user = mAuth.getCurrentUser();

        loggedIn_User_ID = user.getUid();

        OneSignal.sendTag("User_ID", loggedIn_User_ID);

        if (isAdmin) {

            setContentView(R.layout.activity_home_screen);

            managementRotaBtn = (Button) findViewById(R.id.managementRotaBtn);

            managementChatBtn = (Button) findViewById(R.id.managementChatBtn);

            managementRotaBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(MainActivity.this, "Not " +
                            "yet implemented", Toast
                            .LENGTH_LONG).show();

                }
            }
            );

            managementChatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(MainActivity.this, "Not " +
                            "yet implemented", Toast
                            .LENGTH_LONG).show();
                }
            }
            );

        } else {

            setContentView(R.layout.activity_home_screen_couriers);
        }

        welcomeText = (TextView) findViewById(R.id.welcomeText);

        rotaCalendarBtn = (Button) findViewById(R.id.rotaCalendarBtn);

        guidesBtn = (Button) findViewById(R.id.guidesBtn);

        emergencyBtn = (Button) findViewById(R.id.emergencyBtn);

        supportBtn = (Button) findViewById(R.id.supportBtn);

        rotaCalendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ListCalendar.class);

                intent.putExtra("userID", uid);

                startActivity(intent);

            }
        }
        );

        guidesBtn.setOnClickListener(new View.OnClickListener() {
           @Override
             public void onClick(View view) {

             Intent intent = new Intent(MainActivity.this, GuidesActivity.class);

             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

             startActivity(intent);

           }
        }
        );

        emergencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send notification to covers and to management

                //get the current day first
                String weekDay;

                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

                Calendar calendar = Calendar.getInstance();

                weekDay = dayFormat.format(calendar.getTime());

                String typeOfNotification = "emergency";

                Set<String> user_ID_To_notify = new LinkedHashSet<String>();

                // retrieve which users by user ID are on cover the current day
                for (Map.Entry<String, Set<String>> entry : dayToUserID.entrySet()) {

                    if (entry.getKey().equals(weekDay)) {

                        Set<String> list = entry.getValue();

                        for (String user_IDs : list) {

                            user_ID_To_notify.add(user_IDs);

                        }
                    }
                }

                boolean sentToCovers = false;

                boolean sentToAdmins = false;

                for (String user_ID : user_ID_To_notify) {

                    // we dont want to send notifications to the same person when they are on
                    // cover and accidentally press Emergency
                    if (!mAuth.getCurrentUser().getUid().equals(user_ID)) {

                        sendNotificationsToUser(user_ID, typeOfNotification);

                        sentToCovers = true;

                    }
                }
                // retrieve which users by user ID are admins. Send them notification too
                for (String user_ID : adminUsersID) {

                    // we dont want to send notifications to the same person when they are on
                    // cover and accidentally press Emergency
                    if (!mAuth.getCurrentUser().getUid().equals(user_ID)) {

                        sendNotificationsToUser(user_ID, typeOfNotification);

                        sentToAdmins = true;
                    }
                }

                //add a confirmation popup that the Emergency request has been sent
                if (sentToCovers && sentToAdmins) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,
                            AlertDialog.THEME_HOLO_LIGHT)
                            .setTitle("INFORMATION")
                            // remove the leading and trailing curly braces as well as the colon
                            .setMessage("Emergency request has been sent to courier covers and to" +
                                    " Management team");

                    AlertDialog alert1;

                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            });

                    alert1 = builder.create();

                    alert1.show();
                }
            }
        });

        supportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //send notification to covers
                //get the current day first
                String weekDay;

                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

                Calendar calendar = Calendar.getInstance();

                weekDay = dayFormat.format(calendar.getTime());

                String typeOfNotification = "support";

                user_ID_To_notify = new LinkedHashSet<String>();

                // retrieve which users by user ID are on cover the current day
                for (Map.Entry<String, Set<String>> entry : dayToUserID.entrySet()) {

                    if (entry.getKey().equals(weekDay)) {

                        Set<String> list = entry.getValue();

                        for (String user_IDs : list) {

                            user_ID_To_notify.add(user_IDs);
                        }
                    }
                }

                for (String user_ID : user_ID_To_notify) {

                    // we dont want to send notifications to the same person when they are on
                    // cover and accidentally press Support
                    if (!mAuth.getCurrentUser().getUid().equals(user_ID)) {

                        sendNotificationsToUser(user_ID, typeOfNotification);
                    }
                }
            }
        });


        readDBToFindCouriersOnCover();

        readDBToFindAdminUsers();

        readDBToFindNameLastNameByID();


    }// end of onCreate

    @Override
    protected void onResume() {

        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    public void onStart() {

        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);

    }

    private void readDBToFindCouriersOnCover() {

        url = FirebaseDatabase.getInstance().getReference();

        dayToUserID = new HashMap<String, Set<String>>();

        DatabaseReference allUsersID = url.child("Rota");

        allUsersID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (DataSnapshot userIDLevel : dataSnapshot.getChildren()) {

                    for (DataSnapshot childrenOfUserID : userIDLevel.getChildren()) {

                        final Set<String> user_IDs = new LinkedHashSet<String>();

                        for (DataSnapshot workingWeekLevelChildren : childrenOfUserID.getChildren()) {

                            switch (workingWeekLevelChildren.getKey()) {

                                case "Monday":

                                    checkIfUserIsOnCoverThisDay("Monday",
                                            workingWeekLevelChildren, userIDLevel, user_IDs);
                                    break;

                                case "Tuesday":
                                    checkIfUserIsOnCoverThisDay("Tuesday",
                                            workingWeekLevelChildren, userIDLevel, user_IDs);

                                    break;

                                case "Wednesday":
                                    checkIfUserIsOnCoverThisDay("Wednesday",
                                            workingWeekLevelChildren, userIDLevel, user_IDs);

                                    break;

                                case "Thursday":
                                    checkIfUserIsOnCoverThisDay("Thursday",
                                            workingWeekLevelChildren, userIDLevel, user_IDs);

                                    break;

                                case "Friday":

                                    checkIfUserIsOnCoverThisDay("Friday",
                                            workingWeekLevelChildren, userIDLevel, user_IDs);

                                    break;

                                case "Saturday":

                                    checkIfUserIsOnCoverThisDay("Saturday",
                                            workingWeekLevelChildren, userIDLevel, user_IDs);
                                    break;

                                case "Sunday":

                                    checkIfUserIsOnCoverThisDay("Sunday",
                                            workingWeekLevelChildren, userIDLevel, user_IDs);

                                    break;
                            }
                        }// end of working week for loop
                    }
                }// end of main loop

                System.out.println(dayToUserID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    protected void onStop() {

        super.onStop();
    }

    private void sendNotificationsToUser(final String user_ID, final String typeOfNotification) {

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

                    Set<String> values = userIDToNameLastName.get(loggedIn_User_ID);

                    for (String valueName : values) {

                        if (valueName.startsWith("name")) {

                            String namesArray[] = valueName.split(":");

                            name = namesArray[1];
                        }

                        if (valueName.startsWith("lastname")) {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_logout:

                logout();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void logout() {

        mAuth.signOut();

    }

    private void checkIfUserIsOnCoverThisDay(String day, DataSnapshot workingWeekLevelChildren,
                                             DataSnapshot userIDLevel, final Set<String> user_IDs) {

        if (workingWeekLevelChildren.getKey().equals(day)) {

            for (DataSnapshot childrenOfDay : workingWeekLevelChildren.getChildren()) {

                if (childrenOfDay.getKey().equals("OnCover")) {

                    if (childrenOfDay.getValue(String.class).equals("true")) {

                        // store in a map the day and the User ID that is OnCover
                        if (dayToUserID.containsKey(day)) {

                            dayToUserID.get(day).add(userIDLevel.getKey());

                        }

                        else {

                            user_IDs.add(userIDLevel.getKey());

                            dayToUserID.put(day, user_IDs);

                        }
                    }
                }
            }
        }

    }

    private void readDBToFindAdminUsers() {

        url = FirebaseDatabase.getInstance().getReference();

        DatabaseReference allUsersInUsersLevel = url.child("Users");

        adminUsersID = new LinkedHashSet<String>();

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

            }
        });
    }

    private void readDBToFindNameLastNameByID() {

        url = FirebaseDatabase.getInstance().getReference();

        DatabaseReference allUsersInUsersLevel = url.child("Users");

        userIDToNameLastName = new HashMap<String, Set<String>>();

        allUsersInUsersLevel.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (DataSnapshot userIDLevel : dataSnapshot.getChildren()) {

                    final Set<String> nameAndLastName = new LinkedHashSet<String>();

                    for (DataSnapshot childrenOfID : userIDLevel.getChildren()) {

                        if (childrenOfID.getKey().equals("name")) {

                            String name = childrenOfID.getValue(String.class);

                            if (userIDToNameLastName.containsKey(userIDLevel.getKey())) {

                                userIDToNameLastName.get(userIDLevel.getKey()).add("name:" + name);

                            }

                            else {

                                nameAndLastName.add("name:" + name);

                                userIDToNameLastName.put(userIDLevel.getKey(), nameAndLastName);
                            }
                        }

                        if (childrenOfID.getKey().equals("lastname")) {

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
                }

                Set<String> nameLastName = userIDToNameLastName.get(loggedIn_User_ID);

                String name = "";

                String lastname = "";

                for (String value : nameLastName) {

                    if (value.startsWith("name")) {

                        name = value.substring(value.indexOf(":") + 1, value.length());

                    }

                    else if (value.startsWith("lastname")) {

                        lastname = value.substring(value.indexOf(":") + 1, value.length());

                    }
                }

                welcomeText.append(" " + name + " " + lastname);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

}
