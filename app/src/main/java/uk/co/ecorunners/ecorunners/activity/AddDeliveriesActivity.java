package uk.co.ecorunners.ecorunners.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.ecorunners.ecorunners.R;

import static uk.co.ecorunners.ecorunners.utils.Constants.COUNTER_IS;
import static uk.co.ecorunners.ecorunners.utils.Constants.FIRST_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.ROTA;
import static uk.co.ecorunners.ecorunners.utils.Constants.SECOND_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.THIRD_COLUMN;

public class AddDeliveriesActivity extends AppCompatActivity {

    TextView dayAndTime;
    EditText numberOfDeliveries;
    Button nextShiftBtn;
    Button submitBtn;
    Button homeBtn;
    private String mondayDelivery;
    private String tuesdayDelivery;
    private String wednesdayDelivery;
    private String thursdayDelivery;
    private String fridayDelivery;
    private String saturdayDelivery;
    private String sundayDelivery;
    Map<String, String> map;
    Map<String, String> map2;
    Map<String, String> map3;
    Map<String, String> map4;
    Map<String, String> map5;
    Map<String, String> map6;
    Map<String, String> map7;
    private int stringListCounter = 0;
    List<Map<String, String>> listOfMaps = new ArrayList<>();
    DatabaseReference url;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deliveries);

        SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String uid = sharedPrefs.getString("userID", "");

        //retrieve the data Day, place time and firstDay-lastDay of the week from ListCalendarActivity Activity
        Bundle bundle = getIntent().getExtras();

        String dayStored = bundle.getString("day");

        String placeStored = bundle.getString("place");

        String timeStored = bundle.getString("time");

        String firstDayOfTheWeek = bundle.getString("firstDayOfTheWeek");

        String lastDayOfTheWeek = bundle.getString("lastDayOfTheWeek");

        url = FirebaseDatabase.getInstance().getReference();

        ref = url.child(ROTA).child(uid)
                .child("Week:"+ firstDayOfTheWeek +" until "+ lastDayOfTheWeek);

        dayAndTime = (TextView) findViewById(R.id.DayAndTimeID);

        numberOfDeliveries = (EditText) findViewById(R.id.deliveryNum);

        // Buttons
        nextShiftBtn = (Button) findViewById(R.id.nextShiftBtn);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        homeBtn = (Button) findViewById(R.id.homeBtn);

        submitBtn.setVisibility(View.INVISIBLE);

        SpannableStringBuilder displayString = new SpannableStringBuilder(dayStored +"\n"+ "\n"+ placeStored +"\n" + timeStored);

        displayString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, dayStored.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        displayString.setSpan(new RelativeSizeSpan(1.5f), 0, dayStored.length(), 0);

        dayAndTime.setText(displayString);

        //retrieve all the maps from the ListCalendarActivity Activity
        map = (HashMap<String, String>)bundle.getSerializable("map");
        map2 = (HashMap<String, String>)bundle.getSerializable("map2");
        map3 = (HashMap<String, String>)bundle.getSerializable("map3");
        map4 = (HashMap<String, String>)bundle.getSerializable("map4");
        map5 = (HashMap<String, String>)bundle.getSerializable("map5");
        map6 = (HashMap<String, String>)bundle.getSerializable("map6");
        map7 = (HashMap<String, String>)bundle.getSerializable("map7");

        stringListCounter = bundle.getInt("itemPosition", 1);

        // the case when user presses Sunday
        if (stringListCounter == 6) {

            submitBtn.setVisibility(View.VISIBLE);
        }

        listOfMaps.add(map);
        listOfMaps.add(map2);
        listOfMaps.add(map3);
        listOfMaps.add(map4);
        listOfMaps.add(map5);
        listOfMaps.add(map6);
        listOfMaps.add(map7);

        nextShiftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(stringListCounter < listOfMaps.size() - 1) {

                    stringListCounter++;
                }

                Map tempMap = listOfMaps.get(stringListCounter);

                SpannableStringBuilder displayString = new SpannableStringBuilder
                        (tempMap.get(FIRST_COLUMN)+"\n"+ "\n"+ tempMap.get(SECOND_COLUMN) +"\n" + tempMap.get(THIRD_COLUMN));

                displayString.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0,
                        tempMap.get(FIRST_COLUMN).toString().length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                displayString.setSpan(new RelativeSizeSpan(1.5f), 0, tempMap.get(FIRST_COLUMN).toString().length(), 0);

                dayAndTime.setText(displayString);

                switch (stringListCounter){

                   case 1:
                       mondayDelivery = numberOfDeliveries.getText().toString();
                       addDeliveriesToDB(map);
                       Log.i(COUNTER_IS+ stringListCounter+" "+dayAndTime.getText().toString(), numberOfDeliveries.getText().toString());
                       numberOfDeliveries.setText("");
                       break;

                   case 2:
                       tuesdayDelivery = numberOfDeliveries.getText().toString();
                       addDeliveriesToDB(map2);
                       Log.i(COUNTER_IS+ stringListCounter+" "+dayAndTime.getText().toString(), numberOfDeliveries.getText().toString());
                       numberOfDeliveries.setText("");
                       break;

                   case 3:
                       wednesdayDelivery = numberOfDeliveries.getText().toString();
                       addDeliveriesToDB(map3);
                       Log.i(COUNTER_IS+ stringListCounter+" "+dayAndTime.getText().toString(), numberOfDeliveries.getText().toString());
                       numberOfDeliveries.setText("");
                       break;

                   case 4:
                       thursdayDelivery = numberOfDeliveries.getText().toString();
                       addDeliveriesToDB(map4);
                       Log.i(COUNTER_IS+ stringListCounter+" "+dayAndTime.getText().toString(), numberOfDeliveries.getText().toString());
                       numberOfDeliveries.setText("");
                       break;

                   case 5:
                       fridayDelivery = numberOfDeliveries.getText().toString();
                       addDeliveriesToDB(map5);
                       Log.i(COUNTER_IS+ stringListCounter+" "+dayAndTime.getText().toString(), numberOfDeliveries.getText().toString());
                       numberOfDeliveries.setText("");
                       break;

                   case 6:
                       saturdayDelivery = numberOfDeliveries.getText().toString();
                       addDeliveriesToDB(map6);
                       Log.i(COUNTER_IS+ stringListCounter+" "+dayAndTime.getText().toString(), numberOfDeliveries.getText().toString());
                       submitBtn.setVisibility(View.VISIBLE);
                       numberOfDeliveries.setText("");
                       break;

                    case 7:
                        sundayDelivery = numberOfDeliveries.getText().toString();
                        addDeliveriesToDB(map7);
                        Log.i(COUNTER_IS+ stringListCounter+" "+dayAndTime.getText().toString(), numberOfDeliveries.getText().toString());
                        submitBtn.setVisibility(View.VISIBLE);
                        numberOfDeliveries.setText("");
                        break;

                        default:
                        Log.i("nextShiftBtn listener ", String.valueOf(stringListCounter));
                            break;
               }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dayAndTime.getText().toString().startsWith("Sunday")) {

                    //add Sunday Deliveries to DB
                    sundayDelivery = numberOfDeliveries.getText().toString();

                    addDeliveriesToDB(map7);

                    //add code to check if they have added all the deliveries
                    Intent intent = new Intent(AddDeliveriesActivity.this, ListCalendarActivity.class);

                    intent.putExtra("mondayDeliveries", mondayDelivery);
                    intent.putExtra("tuesdayDeliveries", tuesdayDelivery);
                    intent.putExtra("wednesdayDeliveries", wednesdayDelivery);
                    intent.putExtra("thursdayDeliveries", thursdayDelivery);
                    intent.putExtra("fridayDeliveries", fridayDelivery);
                    intent.putExtra("saturdayDeliveries", saturdayDelivery);
                    intent.putExtra("sundayDeliveries", sundayDelivery);

                    startActivity(intent);
                }
            }

        });

        homeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(AddDeliveriesActivity.this, LoginActivity.class);

                startActivity(intent);
            }
        });

    }// end of onCreate

    private void addDeliveriesToDB (Map<String, String> mapData) {

        DatabaseReference dayRef = ref.child(mapData.get(FIRST_COLUMN));

        Map<String, Object> userData = new HashMap<>();

        if(!numberOfDeliveries.getText().toString().equals("") && !mapData.get(THIRD_COLUMN).equals("OFF")) {

            userData.put("Deliveries", Integer.parseInt(numberOfDeliveries.getText().toString()));
        }

        dayRef.updateChildren(userData);

    }
}
