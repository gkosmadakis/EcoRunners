package uk.co.ecorunners.ecorunners;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;


public class LoginActivity extends AppCompatActivity {

    private EditText mEmailField, mPasswordField;
    private Button mloginBtn, registerBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String uid,email;
    private boolean isAdminValue;
    DatabaseReference url, mDatabase;
    private static String timeStored;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mEmailField = (EditText) findViewById(R.id.emailField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);

        mloginBtn = (Button) findViewById(R.id.loginBtn);
        registerBtn = (Button) findViewById(R.id.registerBtn);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {

                    email = firebaseAuth.getCurrentUser().getEmail();

                    uid = firebaseAuth.getCurrentUser().getUid();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    // retrieve the boolean isAdminUser. it is stored in RegisterActivity
                    SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                    isAdminValue = sharedPrefs.getBoolean("isAdminUser", false);

                    intent.putExtra("email", email);

                    intent.putExtra("isAdmin", isAdminValue);

                    startActivity(intent);
                }
            }
        };



        mloginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startSignIn();


            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

            }
        });

        //initial calendar with the current date the time when courier has shift
        final Calendar calendar = Calendar.getInstance();

        //this needs to be called once in the startSignIn method and not here. if it is called once on the startSignIn then it should
        //be called on the NotificationReceivedHandler for rescheduling.
        getTimeStoredFromDB(calendar);

    }// end of OnCreate



    @Override
    public void onStart() {

        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {

        super.onStop();

    }

    private void startSignIn () {

        String email = mEmailField.getText().toString();

        String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(LoginActivity.this, "Fields are empty", Toast.LENGTH_LONG).show();
        }

        if (password.length() < 6){

            Toast.makeText(LoginActivity.this, "Passwords must be at least 6 characters long", Toast.LENGTH_LONG).show();
        }

        else {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {

                        Toast.makeText(LoginActivity.this, "Sign in Problem", Toast.LENGTH_LONG).show();

                        Log.e("", "onComplete: Failed=" + task.getException().getMessage());
                    }
                    else {

                        checkUserExists();

                        mProgress.setMessage("Logging in...");

                        mProgress.show();

                        //call it again here as i see that when the user first logs in is not called
                        // initial calendar with the current date the time when courier has shift
                        final Calendar calendar = Calendar.getInstance();

                        getTimeStoredFromDB(calendar);
                    }
                }
            });
        }
    }

    private void getTimeStoredFromDB(final Calendar calendar) {

        final Date date = calendar.getTime();

        url = FirebaseDatabase.getInstance().getReference();

        SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        uid = preferences.getString("userID","");

        //get the current day and pass it in the DB listener
        String currentDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

        DatabaseReference workingDay = url.child("Rota").child(uid).child("Week:"+getTheCurrentWorkingWeek()).child(currentDay);

        workingDay.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (com.google.firebase.database.DataSnapshot childrenOfDay : dataSnapshot.getChildren()) {

                    System.out.println(childrenOfDay);

                    if (childrenOfDay.getKey().equals("Time")) {

                        if (!childrenOfDay.getValue(String.class).equals("OFF")) {

                            timeStored = childrenOfDay.getValue(String.class);

                            setTimeStored(timeStored);

                            String timeWhenShiftStarts = timeStored.substring(0, timeStored.indexOf("-"));

                            Date dateWhenShiftStarts = null;

                            try {

                                SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");

                                String dateToday = formatDate.format(calendar.getTime());

                                timeWhenShiftStarts = dateToday + " " + timeWhenShiftStarts + ":00";

                                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                                dateWhenShiftStarts = format.parse(timeWhenShiftStarts);

                                calendar.setTime(dateWhenShiftStarts);

                                calendar.add(Calendar.HOUR, -1);

                                //check if the current time is greater than the time it is scheduled to show notification for today
                                if (System.currentTimeMillis() > calendar.getTime().getTime()) {

                                    System.out.println("SHIFT FOR TODAY HAS PASSED");

                                    calendar.add(Calendar.DATE, 1);

                                    getTimeStoredFromDBNextDay(calendar);
                                }

                                // that means the loginActivity is called before the scheduled time for notification for today
                                else {

                                    getNextTimeForNotificationMillis(calendar);

                                    sendNotificationsToUser(uid, "oneHourBeforeShift", calendar.getTimeInMillis(), timeStored);

                                    Toast.makeText(LoginActivity.this, "NOTIFICATION TO SHOW IS TRIGGERED", Toast.LENGTH_LONG).show();

                                }

                            } catch (ParseException e) {

                                e.printStackTrace();
                            }
                        }

                        else if (childrenOfDay.getValue(String.class).equals("OFF")){

                            calendar.add(Calendar.DATE, 1);

                            String nextDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.getTime());

                            DatabaseReference nextWorkingDay = url.child("Rota").child(uid).child("Week:" + getTheCurrentWorkingWeek()).child(nextDay);

                            nextWorkingDay.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                                    for (com.google.firebase.database.DataSnapshot childrenOfDay : dataSnapshot.getChildren()) {

                                        if (childrenOfDay.getKey().equals("Time")) {

                                            if (!childrenOfDay.getValue(String.class).equals("OFF")) {

                                                timeStored = childrenOfDay.getValue(String.class);

                                                setTimeStored(timeStored);

                                                getNextTimeForNotificationMillis(calendar);

                                                sendNotificationsToUser(uid, "oneHourBeforeShift", calendar.getTimeInMillis(), timeStored);

                                                Toast.makeText(LoginActivity.this, "NOTIFICATION IS TRIGGERED from OFF: ", Toast.LENGTH_LONG).show();

                                          }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    System.out.println("OnCancelled called" + databaseError);

                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                System.out.println("OnCancelled called" + databaseError);

            }
        });
    }

    public long getNextTimeForNotificationMillis(Calendar calendar) {

        long timeTillNextNotification = 0;

        if (timeStored != null) {

            //final Calendar calendar = Calendar.getInstance();
            String timeWhenShiftStarts = timeStored.substring(0, timeStored.indexOf("-"));

            Date dateWhenShiftStarts = null;

            try {

                SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");

                String dateToday = formatDate.format(calendar.getTime());

                timeWhenShiftStarts = dateToday + " " + timeWhenShiftStarts + ":00";

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                dateWhenShiftStarts = format.parse(timeWhenShiftStarts);

                calendar.setTime(dateWhenShiftStarts);

                calendar.add(Calendar.HOUR, -1);

                // get the time to get notified in milliseconds
                timeTillNextNotification = calendar.getTime().getTime();

                //store the long in preferences
                SharedPreferences sharedPrefs = EcoRunners.getAppContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPrefs.edit();

                editor.putLong("timeTillNextNotification", timeTillNextNotification);

                editor.commit();

            } catch (ParseException e) {

                e.printStackTrace();
            }
        }

        return  timeTillNextNotification;
    }

    private void getTimeStoredFromDBNextDay(final Calendar calendar) {

        final Date date = calendar.getTime();

        url = FirebaseDatabase.getInstance().getReference();

        SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        uid = preferences.getString("userID","");

        //get the next day and pass it in the DB listener
        String nextDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

        DatabaseReference workingDay = url.child("Rota").child(uid).child("Week:"+getTheCurrentWorkingWeek()).child(nextDay);

        workingDay.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                for (com.google.firebase.database.DataSnapshot childrenOfDay : dataSnapshot.getChildren()) {

                    System.out.println(childrenOfDay);

                    if (childrenOfDay.getKey().equals("Time")) {

                        if (!childrenOfDay.getValue(String.class).equals("OFF")) {

                            timeStored = childrenOfDay.getValue(String.class);

                            setTimeStored(timeStored);

                            getNextTimeForNotificationMillis(calendar);

                            sendNotificationsToUser(uid, "oneHourBeforeShift", calendar.getTimeInMillis(), timeStored);

                            Toast.makeText(LoginActivity.this, "IT IS TRIGGERED", Toast.LENGTH_LONG).show();
                        }

                        else if (childrenOfDay.getValue(String.class).equals("OFF")) {

                            calendar.add(Calendar.DATE, 1);

                            String nextDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.getTime());

                            DatabaseReference nextWorkingDay = url.child("Rota").child(uid).child("Week:" + getTheCurrentWorkingWeek()).child(nextDay);

                            nextWorkingDay.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                                    for (com.google.firebase.database.DataSnapshot childrenOfDay : dataSnapshot.getChildren()) {

                                        if (childrenOfDay.getKey().equals("Time")) {

                                            if (!childrenOfDay.getValue(String.class).equals("OFF")) {

                                                timeStored = childrenOfDay.getValue(String.class);

                                                setTimeStored(timeStored);

                                                getNextTimeForNotificationMillis(calendar);

                                                sendNotificationsToUser(uid, "oneHourBeforeShift", calendar.getTimeInMillis(), timeStored);

                                                Toast.makeText(LoginActivity.this, "IT IS TRIGGERED from OFF: ", Toast.LENGTH_LONG).show();

                                          }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    System.out.println("OnCancelled called" + databaseError);

                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

                System.out.println("OnCancelled called" + databaseError);

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
            //calendar.set(Calendar.DAY_OF_WEEK, 1);
            //and i add 6 days to get the Sunday of the current week
            calendar.add(Calendar.DATE, 6);

            lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            week = firstDayOfTheWeek + " until " + lastDayOfTheWeek;

        }
        return week;
    }

    public static String setTimeStored (String timeStored){

        return  timeStored;

    }

    public static String getTimeStored () {

        return timeStored;

    }

    private void checkUserExists() {

        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(user_id)){

                    url = FirebaseDatabase.getInstance().getReference();

                    DatabaseReference isAdminChild = url.child("Users").child(user_id).child("isAdmin");

                    isAdminChild.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                            //get the value of the child isAdmin
                                isAdminValue = dataSnapshot.getValue(Boolean.class);

                                System.out.println(isAdminValue);

                                SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                                // if the user is admin load the admin layout
                                if (isAdminValue) {

                                    //store in the prefs if is admin and the email so next time they login to retrieve it
                                    SharedPreferences.Editor editor = sharedPrefs.edit();

                                    editor.putBoolean("isAdminUser", isAdminValue);
                                    editor.putString("email", mAuth.getCurrentUser().getEmail());
                                    editor.putString("userID", user_id);

                                    editor.commit();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                    mProgress.dismiss();

                                    startActivity(intent);

                                }

                                //if the user is not admin load the user/courier layout
                                else {

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                    //store in the prefs if is admin and the email so next time they login to retrieve it
                                    SharedPreferences.Editor editor = sharedPrefs.edit();

                                    editor.putBoolean("isAdminUser", isAdminValue);
                                    editor.putString("email", mAuth.getCurrentUser().getEmail());
                                    editor.putString("userID", user_id);

                                    editor.commit();

                                    intent.putExtra("email", mAuth.getCurrentUser().getEmail());

                                    intent.putExtra("isAdmin", isAdminValue);

                                    mProgress.dismiss();

                                    startActivity(intent);
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {

                    Toast.makeText(LoginActivity.this, "You need to setup your account", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void sendNotificationsToUser (final String user_ID, final String typeOfNotification, long time, final String shiftTime) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(time);

        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        /*format.setTimeZone(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        Date timeToShowNotificationDate=null;
        try {
            timeToShowNotificationDate = dateFormatLocal.parse(format.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        final String timeToShowNotification = format.format(calendar.getTime());

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

                        if (typeOfNotification.equals("emergency")) {

                            strJsonBody = "{"
                                    + "\"app_id\": \"4b78e5b2-4259-476d-9d30-af4419c6556f\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + user_ID + "\"}],"

                                    + "\"data\": {\"" + name + "\": \"" + lastname + " called for Emergency\"},"
                                    + "\"contents\": {\"en\": \"Emergency button is clicked!\"}"
                                    + "}";
                        }
                        else if (typeOfNotification.equals("support")){

                            strJsonBody = "{"
                                    + "\"app_id\": \"4b78e5b2-4259-476d-9d30-af4419c6556f\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + user_ID + "\"}],"

                                    + "\"data\": {\"" + name + " \": \"" + lastname + " called for Support\"},"
                                    + "\"contents\": {\"en\": \"Support button is clicked!\"}"
                                    + "}";
                        }

                        else if (typeOfNotification.equals("oneHourBeforeShift")){

                            strJsonBody = "{"
                                    + "\"app_id\": \"4b78e5b2-4259-476d-9d30-af4419c6556f\","

                                    + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + user_ID + "\"}],"

                                    + "\"data\": {\""+"\":\"" + "You have shift in one hour at: " + shiftTime + "\"},"
                                    + "\"contents\": {\"en\": \"You have shift in one hour\"},"
                                    + "\"send_after\": \"" +timeToShowNotification + "\""
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

                    } catch (Throwable t) {

                        t.printStackTrace();
                    }
                }
            }
        });
    }

}
