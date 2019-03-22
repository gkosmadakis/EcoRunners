package uk.co.ecorunners.ecorunners.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uk.co.ecorunners.ecorunners.NotificationReceivedHandler;
import uk.co.ecorunners.ecorunners.R;
import uk.co.ecorunners.ecorunners.utils.MainActivityUtil;
import uk.co.ecorunners.ecorunners.utils.Utils;

import static uk.co.ecorunners.ecorunners.utils.Constants.IS_ADMIN_USER;
import static uk.co.ecorunners.ecorunners.utils.Constants.PREFERENCES;
import static uk.co.ecorunners.ecorunners.utils.Constants.USERS;
import static uk.co.ecorunners.ecorunners.utils.Constants.USER_ID;


public class MainActivity extends AppCompatActivity {

    private String loggedInUserID;
    private Map<String, Set<String>> dayToUserIDOnCover;
    private Map<String, Set<String>> userIDToNameLastNameNonStatic;
    private Set<String> adminUsersID;
    private Set<String> userIDToNotify;
    private String uid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

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

        SharedPreferences sharedPrefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        Boolean isAdmin = sharedPrefs.getBoolean(IS_ADMIN_USER, false);

        uid = sharedPrefs.getString(USER_ID, "");

        // send tags with the ID of the current user
        FirebaseUser user = mAuth.getCurrentUser();

        loggedInUserID = user.getUid();

        OneSignal.sendTag("User_ID", loggedInUserID);

        Utils util = new Utils();
        final MainActivityUtil mainUtil = new MainActivityUtil();

        if (isAdmin) {

            setContentView(R.layout.activity_home_screen);

            Button managementRotaBtn = findViewById(R.id.managementRotaBtn);

            Button managementChatBtn = findViewById(R.id.managementChatBtn);

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

        TextView welcomeText = findViewById(R.id.welcomeText);

        Button rotaCalendarBtn = findViewById(R.id.rotaCalendarBtn);

        Button guidesBtn = findViewById(R.id.guidesBtn);

        Button emergencyBtn = findViewById(R.id.emergencyBtn);

        Button supportBtn = findViewById(R.id.supportBtn);

        rotaCalendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ListCalendarActivity.class);

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

                Set<String> userIDToNotifyLocal = new LinkedHashSet<>();

                // retrieve which users by user ID are on cover the current day
                boolean sentToCovers = false;
                boolean sentToAdmins = false;

                sentToCovers = mainUtil.findUsersOnCoverAndNotify(weekDay, typeOfNotification, userIDToNotifyLocal, loggedInUserID, MainActivity.this);
                // retrieve which users by user ID are admins. Send them notification too
                sentToAdmins = mainUtil.findAdminsAndNotify(typeOfNotification, adminUsersID, loggedInUserID, MainActivity.this);

                //add a confirmation popup that the Emergency request has been sent
                if (sentToCovers && sentToAdmins) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, android.R.style.Theme_Holo_Light_Dialog))
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

                userIDToNotify = new LinkedHashSet<>();

                // retrieve which users by user ID are on cover the current day
                mainUtil.findUsersOnCoverAndNotify(weekDay, typeOfNotification, userIDToNotify, loggedInUserID, MainActivity.this);

            }
        });

        dayToUserIDOnCover = mainUtil.readDBToFindCouriersOnCover();

        DatabaseReference url = FirebaseDatabase.getInstance().getReference();

        adminUsersID = util.readDBToFindAdminUsers(url);

        userIDToNameLastNameNonStatic = mainUtil.readDBToFindNameLastNameByID(FirebaseDatabase.getInstance().getReference().child(USERS),loggedInUserID,welcomeText);

    }// end of onCreate

    @Override
    public void onStart() {

        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {

        mAuth.signOut();

    }

    public Map<String, Set<String>> getDayToUserIDOnCover() {

        return dayToUserIDOnCover;
    }

    public Map<String, Set<String>> getUserIDToNameLastNameNonStatic() {
        return userIDToNameLastNameNonStatic;
    }

}
