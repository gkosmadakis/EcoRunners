package uk.co.ecorunners.ecorunners;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.LinkedHashSet;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameField, mLastNameField, mPhoneField, mFullAddress, mNino, mEmailField, mPasswordField;
    private Button mRegisterBtn;
    private TextView registerLabel;
    private CheckBox registerCheckBox;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, url;
    private ProgressDialog mProgress;
    private String email;
    private LinkedHashSet<String> adminUsersID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        registerLabel = (TextView) findViewById(R.id.registerView);

        String register = "Register";

        SpannableString spanString = new SpannableString(register);

        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);

        registerLabel.setText(spanString);

        mNameField = (EditText) findViewById(R.id.nameField);

        mLastNameField = (EditText) findViewById(R.id.lastNameField);

        mPhoneField = (EditText) findViewById(R.id.phoneNum);

        mFullAddress = (EditText) findViewById(R.id.fullAddressField);

        mNino = (EditText) findViewById(R.id.ninoField);

        mEmailField = (EditText) findViewById(R.id.emailField);

        mPasswordField = (EditText) findViewById(R.id.passwordField);

        registerCheckBox = (CheckBox) findViewById(R.id.registerCheckBox);

        mRegisterBtn = (Button) findViewById(R.id.registerBtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startRegister();

            }
        });

        readDBToFindAdminUsers();
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

                mProgress.setMessage("Signing up...");

                mProgress.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            String user_id = mAuth.getCurrentUser().getUid();

                            System.out.println("I AM IN");

                            DatabaseReference current_user_db = mDatabase.child(user_id);

                            current_user_db.child("name").setValue(name);

                            current_user_db.child("lastname").setValue(lastName);

                            current_user_db.child("phone").setValue(phone);

                            current_user_db.child("address").setValue(fullAddress);

                            current_user_db.child("nino").setValue(nino);

                            //this is hardcoded that means that this register activity will produce only non Admin users
                            current_user_db.child("isAdmin").setValue(false);

                            mProgress.dismiss();

                            SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = sharedPrefs.edit();

                            //store in preferences the email
                            editor.putString("email", email);

                            // check if registered user is admin. this might change when we know if they want to register users
                            //using another activity. For the time now i will do a check on the database to see what admin users see
                            if (adminUsersID.contains(user_id)) {

                                //store in preferences the boolean isAdmin
                                editor.putBoolean("isAdminUser", true);
                            }

                            else {

                                editor.putBoolean("isAdminUser", false);

                            }

                            editor.putString("userID", user_id);

                            editor.commit();

                            Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);

                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(mainIntent);

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
}
