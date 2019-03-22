package uk.co.ecorunners.ecorunners.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.ecorunners.ecorunners.R;

import static uk.co.ecorunners.ecorunners.utils.Constants.USERS;

public class CouriersContactDetailsActivity extends AppCompatActivity {

    TextView courierName;
    TextView courierLastname;
    TextView courierPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couriers_contact_details);

        SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String uid = sharedPrefs.getString("userID", "");

        DatabaseReference url = FirebaseDatabase.getInstance().getReference();

        courierName = findViewById(R.id.courierNameView);

        courierLastname = findViewById(R.id.courierLastNameView);

        courierPhone = findViewById(R.id.courierPhoneView);

        DatabaseReference dailySchedulePlace = url.child(USERS).child(uid);

        dailySchedulePlaceValueEventListener(dailySchedulePlace);


    }

    private void dailySchedulePlaceValueEventListener(DatabaseReference dailySchedulePlace) {
        dailySchedulePlace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {

                    Toast.makeText(CouriersContactDetailsActivity.this, "No contact details found under the user ID", Toast.LENGTH_LONG).show();

                }

                else {

                    populateUserDetails(dataSnapshot);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /* Not used */
            }
        });
    }

    private void populateUserDetails(DataSnapshot dataSnapshot) {

        for (DataSnapshot childrenOfUserID : dataSnapshot.getChildren()) {

            if (childrenOfUserID.getKey().equals("name")) {

                courierName.setText("Name:"+ childrenOfUserID.getValue(String.class));
            }

            if (childrenOfUserID.getKey().equals("lastname")) {

                courierLastname.setText("Last Name:"+ childrenOfUserID.getValue(String.class));
            }

            if (childrenOfUserID.getKey().equals("phone")) {

                courierPhone.setText("Phone:"+ childrenOfUserID.getValue(String.class));
            }
        }
    }
}
