package uk.co.ecorunners.ecorunners.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.util.Calendar;

import uk.co.ecorunners.ecorunners.R;
import uk.co.ecorunners.ecorunners.utils.LoginActivityUtil;

import static uk.co.ecorunners.ecorunners.utils.Constants.EMAIL;
import static uk.co.ecorunners.ecorunners.utils.Constants.IS_ADMIN;
import static uk.co.ecorunners.ecorunners.utils.Constants.IS_ADMIN_USER;
import static uk.co.ecorunners.ecorunners.utils.Constants.PREFERENCES;
import static uk.co.ecorunners.ecorunners.utils.Constants.USERS;
import static uk.co.ecorunners.ecorunners.utils.Constants.USER_ID;


public class LoginActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String email;
    private boolean isAdminValue;
    DatabaseReference url;
    DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private LoginActivityUtil loginActivityUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        RelativeLayout layout = findViewById(R.id.loginactivitylayout);

        progressBar = new ProgressBar(LoginActivity.this, null,android.R.attr.progressBarStyleLarge);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);

        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        layout.addView(progressBar,params);

        progressBar.setVisibility(View.GONE);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(USERS);

        mEmailField = findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);

        Button mloginBtn = findViewById(R.id.loginBtn);
        Button registerBtn = findViewById(R.id.registerBtn);

        url = FirebaseDatabase.getInstance().getReference();

        loginActivityUtil = new LoginActivityUtil(this);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {

                    email = firebaseAuth.getCurrentUser().getEmail();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    // retrieve the boolean isAdminUser. it is stored in RegisterActivity
                    SharedPreferences sharedPrefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

                    isAdminValue = sharedPrefs.getBoolean(IS_ADMIN_USER, false);

                    intent.putExtra(EMAIL, email);

                    intent.putExtra(IS_ADMIN, isAdminValue);

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
        loginActivityUtil.getTimeStoredFromDB(calendar, url);

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
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);

        }
    }

    public void startSignIn () {

        String emailLocal = mEmailField.getText().toString();

        String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(emailLocal) || TextUtils.isEmpty(password)) {

            Toast.makeText(LoginActivity.this, "Fields are empty", Toast.LENGTH_LONG).show();
        }

        if (password.length() < 6){

            Toast.makeText(LoginActivity.this, "Passwords must be at least 6 characters long", Toast.LENGTH_LONG).show();
        }

        else {

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(emailLocal, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {

                        Toast.makeText(LoginActivity.this, "Sign in Problem", Toast.LENGTH_LONG).show();

                        Log.e("Login Failed ", "with exception " + task.getException().getMessage());
                    }
                    else {

                        progressBar.setVisibility(View.GONE);

                        checkUserExists();

                        //call it again here as i see that when the user first logs in is not called
                        // initial calendar with the current date the time when courier has shift
                        final Calendar calendar = Calendar.getInstance();

                        loginActivityUtil.getTimeStoredFromDB(calendar, url);
                    }
                }
            });
        }
    }

    public void checkUserExists() {

        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(user_id)){

                    url = FirebaseDatabase.getInstance().getReference();

                    DatabaseReference isAdminChild = url.child(USERS).child(user_id).child(IS_ADMIN);

                    isAdminChild.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                            //get the value of the child isAdmin
                                isAdminValue = dataSnapshot.getValue(Boolean.class);

                                Log.i("isAdminValue " , String.valueOf(isAdminValue));

                                SharedPreferences sharedPrefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

                                // if the user is admin load the admin layout
                                if (isAdminValue) {

                                    //store in the prefs if is admin and the email so next time they login to retrieve it
                                    SharedPreferences.Editor editor = sharedPrefs.edit();

                                    editor.putBoolean(IS_ADMIN_USER, isAdminValue);
                                    editor.putString(EMAIL, mAuth.getCurrentUser().getEmail());
                                    editor.putString(USER_ID, user_id);

                                    editor.apply();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                    startActivity(intent);

                                }

                                //if the user is not admin load the user/courier layout
                                else {

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                                    //store in the prefs if is admin and the email so next time they login to retrieve it
                                    SharedPreferences.Editor editor = sharedPrefs.edit();

                                    editor.putBoolean(IS_ADMIN_USER, isAdminValue);
                                    editor.putString(EMAIL, mAuth.getCurrentUser().getEmail());
                                    editor.putString(USER_ID, user_id);

                                    editor.apply();

                                    intent.putExtra(EMAIL, mAuth.getCurrentUser().getEmail());

                                    intent.putExtra(IS_ADMIN, isAdminValue);

                                    startActivity(intent);
                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            /*Not used */
                        }
                    });
                }
                else {

                    Toast.makeText(LoginActivity.this, "You need to setup your account", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /*Not used */
            }
        });
    }

}
