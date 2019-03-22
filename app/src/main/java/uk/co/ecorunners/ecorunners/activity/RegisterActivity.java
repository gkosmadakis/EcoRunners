package uk.co.ecorunners.ecorunners.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Set;

import uk.co.ecorunners.ecorunners.R;
import uk.co.ecorunners.ecorunners.utils.Utils;

import static uk.co.ecorunners.ecorunners.utils.Constants.USERS;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mLastNameField;
    private EditText mPhoneField;
    private EditText mFullAddress;
    private EditText mNino;
    private EditText mEmailField;
    private EditText mPasswordField;
    private CheckBox registerCheckBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private String email;
    private Set<String> adminUsersID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        ConstraintLayout layout = findViewById(R.id.registeractivitylayout);

        progressBar = new ProgressBar(RegisterActivity.this, null,android.R.attr.progressBarStyleLarge);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);

        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        layout.addView(progressBar,params);

        progressBar.setVisibility(View.GONE);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(USERS);

        TextView registerLabel = findViewById(R.id.registerView);

        String register = "Register";

        SpannableString spanString = new SpannableString(register);

        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);

        registerLabel.setText(spanString);

        mNameField = findViewById(R.id.nameField);

        mLastNameField = findViewById(R.id.lastNameField);

        mPhoneField = findViewById(R.id.phoneNum);

        mFullAddress = findViewById(R.id.fullAddressField);

        mNino = findViewById(R.id.ninoField);

        mEmailField = findViewById(R.id.emailField);

        mPasswordField = findViewById(R.id.passwordField);

        registerCheckBox = findViewById(R.id.registerCheckBox);

        Button mRegisterBtn = findViewById(R.id.registerBtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startRegister();

            }
        });
        Utils util = new Utils();

        DatabaseReference url = FirebaseDatabase.getInstance().getReference();

        adminUsersID = util.readDBToFindAdminUsers(url);
    }// end of Oncreate

    private void startRegister() {

        final String name = mNameField.getText().toString().trim();

        final String lastName = mLastNameField.getText().toString().trim();

        final String phone = mPhoneField.getText().toString().trim();

        final String fullAddress = mFullAddress.getText().toString().trim();

        final String nino = mNino.getText().toString().trim();

        email = mEmailField.getText().toString().trim();

        String password = mPasswordField.getText().toString().trim();

        boolean registerBox = registerCheckBox.isChecked();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(lastName)&& !TextUtils.isEmpty(phone)&&
                !TextUtils.isEmpty(fullAddress) && !TextUtils.isEmpty(nino) && !TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)) {

            if (!registerBox){

                Toast.makeText(RegisterActivity.this, "You have not checked the declaration checkbox", Toast.LENGTH_LONG).show();

            }

            if (password.length() < 6){

                Toast.makeText(RegisterActivity.this, "Passwords must be at least 6 characters long", Toast.LENGTH_LONG).show();

            }

            else {

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            String userId = mAuth.getCurrentUser().getUid();

                            Log.i("Logged in ", "I AM IN");

                            DatabaseReference currentUserDb = mDatabase.child(userId);

                            currentUserDb.child("name").setValue(name);

                            currentUserDb.child("lastname").setValue(lastName);

                            currentUserDb.child("phone").setValue(phone);

                            currentUserDb.child("address").setValue(fullAddress);

                            currentUserDb.child("nino").setValue(nino);

                            //this is hardcoded that means that this register activity will produce only non Admin users
                            currentUserDb.child("isAdmin").setValue(false);

                            SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPrefs.edit();

                            //store in preferences the email
                            editor.putString("email", email);

                            // check if registered user is admin. this might change when we know if they want to register users
                            //using another activity. For the time now i will do a check on the database to see what admin users see

                            //store in preferences the boolean isAdmin
                            editor.putBoolean("isAdminUser", adminUsersID.contains(userId));

                            editor.putString("userID", userId);

                            editor.apply();

                            progressBar.setVisibility(View.GONE);

                            Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);

                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(mainIntent);

                        }
                        else {

                            Log.e("Error", task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

        else if(TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName)|| TextUtils.isEmpty(phone)||
                TextUtils.isEmpty(fullAddress)|| TextUtils.isEmpty(nino)|| TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password)) {

            Toast.makeText(RegisterActivity.this, "Some of the Register fields are empty. Please complete all the fields.",
                    Toast.LENGTH_LONG).show();

        }
    }

}
