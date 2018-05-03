package uk.co.ecorunners.ecorunners;

import static uk.co.ecorunners.ecorunners.Constants.FIRST_COLUMN;
import static uk.co.ecorunners.ecorunners.Constants.FOURTH_COLUMN;
import static uk.co.ecorunners.ecorunners.Constants.SECOND_COLUMN;
import static uk.co.ecorunners.ecorunners.Constants.THIRD_COLUMN;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ListCalendar extends AppCompatActivity {

    ListView listView ;
    DatabaseReference url;
    private String uid, placeStoredMon,placeStoredTue,placeStoredWed,placeStoredThu,placeStoredFri,placeStoredSat,
            placeStoredSun,timeStoredMon,timeStoredTue,timeStoredWed,timeStoredThu,timeStoredFri,timeStoredSat,timeStoredSun,
    firstDayOfTheWeek, lastDayOfTheWeek,mondayDelivery, tuesdayDelivery,wednesdayDelivery,thursdayDelivery,fridayDelivery,
             saturdayDelivery, sundayDelivery;
    private ListViewAdapter adapter;
    private ArrayList<HashMap<String, String>> list;
    HashMap<String,String> map, map2, map3, map4, map5, map6, map7;
    private TextView weekView,datesView;
    private Button homeBtn, nextWeekBtn,previousWeekBtn;
    Calendar calendar;
    public static boolean scheduleIsCleared = false;
    // icons for the tick red x and pending icons
    public static final Integer tickIcon =  R.drawable.ic_check_circle_green_24dp_withoutbackground;
    public static final Integer xIcon = R.drawable.ic_highlight_off_red_24dp_without_background;
    public static final Integer pendingIcon = R.drawable.ic_indeterminate_check_box_orange_24dp;
    List<RowItem> rowItems;
    public static ArrayList<Map> daysCourierIsOFF = new ArrayList<Map>();
    public static ArrayList<Map> daysCourierIsON = new ArrayList<Map>();
    public final String ACTION_ONE = "1";
    public final String ACTION_TWO = "2";
    public boolean courierHasNotAddedDeliveriesOnAtLeastOneDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_calendar);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);

        //uid = bundle.getString("userID");
        SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        uid = sharedPrefs.getString("userID","");

        //Week View
        weekView = (TextView) findViewById(R.id.weekView);

        //The dates view
        datesView = (TextView) findViewById(R.id.datesView);

        //get the first day of the week and the last
        calendar = Calendar.getInstance(Locale.US);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        //the case when the current day is Sunday I need to show the current week MON-SUN
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        if (day == Calendar.SUNDAY) {

            lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            calendar.add(Calendar.DATE, -6);

            firstDayOfTheWeek = simpleDateFormat.format(calendar.getTime());
        }

        else {

            //get the Monday of the current week
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            firstDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            //here i get the first day of the week which is Sunday but is the Sunday of the previous week
            //calendar.set(Calendar.DAY_OF_WEEK, 1);
            //and i add 6 days to get the Sunday of the current week
            calendar.add(Calendar.DATE, 6);

            lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            //set the days in the weekView and in the datesView
            weekView.setText("Week");
        }

        datesView.setText(firstDayOfTheWeek+" until "+lastDayOfTheWeek);

        //check if the app has connection to Firebase namely if the device is connected to Internet
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                boolean connected = snapshot.getValue(Boolean.class);

                if (connected) {

                    System.out.println("connected");

                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ListCalendar.this, AlertDialog.THEME_HOLO_LIGHT)
                            .setTitle("No internet connection")
                            .setMessage("The app is not connected to Firebase, check your internet connection");

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

            @Override
            public void onCancelled(DatabaseError error) {

                System.err.println("Listener was cancelled");

            }
        });

        String currentWeek = firstDayOfTheWeek.replaceAll("/", "-") + " until "+lastDayOfTheWeek.replaceAll("/", "-");

        updateView(currentWeek);

        list = new ArrayList<HashMap<String,String>>();

        map =new HashMap<String, String>();
        map.put(FIRST_COLUMN, "Monday");
        list.add(map);

        map2 =new HashMap<String, String>();
        map2.put(FIRST_COLUMN, "Tuesday");
        list.add(map2);

        map3 =new HashMap<String, String>();
        map3.put(FIRST_COLUMN, "Wednesday");
        list.add(map3);

        map4 =new HashMap<String, String>();
        map4.put(FIRST_COLUMN, "Thursday");
        list.add(map4);

        map5 =new HashMap<String, String>();
        map5.put(FIRST_COLUMN, "Friday");
        list.add(map5);

        map6 =new HashMap<String, String>();
        map6.put(FIRST_COLUMN, "Saturday");
        list.add(map6);

        map7 =new HashMap<String, String>();
        map7.put(FIRST_COLUMN, "Sunday");
        list.add(map7);

        // call the method to initialize the stauts Icon, the fourth column.
        hasCourierAddedDeliveries();

        adapter = new ListViewAdapter(this, list, R.layout.colmn_row, rowItems);

        // Assign adapter to ListView
        listView.setAdapter(adapter);


        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            mondayDelivery = bundle.getString("mondayDeliveries");
            tuesdayDelivery = bundle.getString("tuesdayDeliveries");
            wednesdayDelivery = bundle.getString("wednesdayDeliveries");
            thursdayDelivery = bundle.getString("thursdayDeliveries");
            fridayDelivery = bundle.getString("fridayDeliveries");
            saturdayDelivery = bundle.getString("saturdayDeliveries");
            sundayDelivery = bundle.getString("sundayDeliveries");

            updateStatusIcon(mondayDelivery,tuesdayDelivery,wednesdayDelivery,thursdayDelivery,fridayDelivery,saturdayDelivery,sundayDelivery);

        }

        //Previous Week Button
        previousWeekBtn = (Button) findViewById(R.id.previousWeekBtn);

        //set it to be invisible by default
        previousWeekBtn.setVisibility(View.INVISIBLE);

        //Next Week Button
        nextWeekBtn = (Button) findViewById(R.id.nextWeekBtn);

        //Home Button
        homeBtn = (Button) findViewById(R.id.homeBtn);

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ListCalendar.this, LoginActivity.class);

                startActivity(intent);
            }
        });

        nextWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // change the week diplayed on the top of the schedule
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

                //set it on the Sunday of the current week
                calendar.set(Calendar.DAY_OF_WEEK, 1);

                //add 1 days to get the Monday of the next week
                calendar.add(Calendar.DATE, 1);

                firstDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

                //add 6 days to get the Sunday of the next week
                calendar.add(Calendar.DATE, 6);

                lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

                datesView.setText(firstDayOfTheWeek+" until "+lastDayOfTheWeek);

                previousWeekBtn.setVisibility(View.VISIBLE);

                // pass the current week to the method that populates the schedule
                String currentWeek = firstDayOfTheWeek.replaceAll("/", "-") + " until "+lastDayOfTheWeek.replaceAll("/", "-");

                updateView(currentWeek);
            }
        });

        previousWeekBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // change the week displayed on the top of the schedule
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

                //set it on the Sunday of the current week
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

                lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

                //subtract 6 days to get the Monday of the previous week
                calendar.add(Calendar.DATE, -6);

                firstDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

                datesView.setText(firstDayOfTheWeek+" until "+lastDayOfTheWeek);

                // pass the current week to the method that populates the schedule
                String currentWeek = firstDayOfTheWeek.replaceAll("/", "-") + " until "+lastDayOfTheWeek.replaceAll("/", "-");

                updateView(currentWeek);

                // get the current date and
                Date currentMonday = calendar.getTime();

                Date currentSunday = new Date(currentMonday.getTime() + +6*24*60*60*1000);

                Calendar c = Calendar.getInstance();

                boolean isInTheCurrentWeek = c.getTime().after(currentMonday) && c.getTime().before(currentSunday);

                //if it is inside the current week then dont display the previous week btn
                if (isInTheCurrentWeek) {

                    //update the icons - read from Firebase
                    hasCourierAddedDeliveries();

                    previousWeekBtn.setVisibility(View.INVISIBLE);

                    // this is to implement 1.2A till 1.2C user should not be able to click on a schedule ahead from the current week
                    scheduleIsCleared = false;
                }
            }
        });

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // ListView Clicked item index
                int itemPosition = position+1;

                Intent intent = new Intent(ListCalendar.this, AddDeliveriesActivity.class);

                switch (itemPosition) {

                    case 1:
                        intent.putExtra("day", "Monday");
                        intent.putExtra("place", map.get(SECOND_COLUMN));
                        intent.putExtra("time", map.get(THIRD_COLUMN));
                        intent.putExtra("itemPosition", itemPosition-1);
                        break;

                    case 2:
                        intent.putExtra("day", "Tuesday");
                        intent.putExtra("place", map2.get(SECOND_COLUMN));
                        intent.putExtra("time", map2.get(THIRD_COLUMN));
                        intent.putExtra("itemPosition", itemPosition-1);
                        break;

                    case 3:
                        intent.putExtra("day", "Wednesday");
                        intent.putExtra("place", map3.get(SECOND_COLUMN));
                        intent.putExtra("time", map3.get(THIRD_COLUMN));
                        intent.putExtra("itemPosition", itemPosition-1);
                        break;

                    case 4:
                        intent.putExtra("day", "Thursday");
                        intent.putExtra("place", map4.get(SECOND_COLUMN));
                        intent.putExtra("time", map4.get(THIRD_COLUMN));
                        intent.putExtra("itemPosition", itemPosition-1);
                        break;

                    case 5:
                        intent.putExtra("day", "Friday");
                        intent.putExtra("place", map5.get(SECOND_COLUMN));
                        intent.putExtra("time", map5.get(THIRD_COLUMN));
                        intent.putExtra("itemPosition", itemPosition-1);
                        break;

                    case 6:
                        intent.putExtra("day", "Saturday");
                        intent.putExtra("place", map6.get(SECOND_COLUMN));
                        intent.putExtra("time", map6.get(THIRD_COLUMN));
                        intent.putExtra("itemPosition", itemPosition-1);
                        break;

                    case 7:
                        intent.putExtra("day", "Sunday");
                        intent.putExtra("place", map7.get(SECOND_COLUMN));
                        intent.putExtra("time", map7.get(THIRD_COLUMN));
                        intent.putExtra("itemPosition", itemPosition-1);
                        break;

                }

                // send the maps to the AddDeliveries activity containing Day, place and time
                intent.putExtra("map", map);
                intent.putExtra("map2", map2);
                intent.putExtra("map3", map3);
                intent.putExtra("map4", map4);
                intent.putExtra("map5", map5);
                intent.putExtra("map6", map6);
                intent.putExtra("map7", map7);

                //send the week, the dates to be used on the AddDeliveries activity
                intent.putExtra("firstDayOfTheWeek", firstDayOfTheWeek);
                intent.putExtra("lastDayOfTheWeek", lastDayOfTheWeek);

                startActivity(intent);

                // Show Alert
                Toast.makeText(ListCalendar.this, Integer.toString(itemPosition)+" Clicked", Toast.LENGTH_LONG).show();
            }
        });

    }//end of onCreate

    @Override
    protected void onResume() {

        super.onResume();

    }


    private void updateView(String week){

        url = FirebaseDatabase.getInstance().getReference();

            DatabaseReference dailySchedulePlace = url.child("Rota").child(uid).child("Week:"+week);

            dailySchedulePlace.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.exists()) {

                    Toast.makeText(ListCalendar.this, "No data have been supplied", Toast.LENGTH_SHORT).show();

                    clearSchedule();

                    }

                    for (DataSnapshot dayChild : dataSnapshot.getChildren()) {

                        switch (dayChild.getKey()) {

                            case "Monday":
                                    populateSingleDaySchedule(placeStoredMon, timeStoredMon, dayChild, map);

                                break;

                            case "Tuesday":
                                    populateSingleDaySchedule(placeStoredTue, timeStoredTue, dayChild, map2);

                                break;

                            case "Wednesday":
                                    populateSingleDaySchedule(placeStoredWed, timeStoredWed, dayChild, map3);

                                break;

                            case "Thursday":
                                    populateSingleDaySchedule(placeStoredThu, timeStoredThu, dayChild, map4);

                                break;

                            case "Friday":
                                    populateSingleDaySchedule(placeStoredFri, timeStoredFri, dayChild, map5);

                                break;

                            case "Saturday":
                                    populateSingleDaySchedule(placeStoredSat, timeStoredSat, dayChild, map6);

                                break;

                            case "Sunday":
                                   populateSingleDaySchedule(placeStoredSun, timeStoredSun, dayChild, map7);

                                break;

                            }

                        listView.setAdapter(adapter);

                        }
                    }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        String daysToCheck []= {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i=0; i <daysToCheck.length; i++) {

            DatabaseReference dayToListenForChanges = url.child("Rota").child(uid).child("Week:" + week).child(daysToCheck[i]);

            dayToListenForChanges.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    Toast.makeText(ListCalendar.this, "Notification change Listener triggered", Toast.LENGTH_LONG).show();

                    triggerNotificationOnShiftChange();

                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });
        }
    }

    private void populateSingleDaySchedule(String placeStored, String timeStored, DataSnapshot dayChild, HashMap<String,String> tempMap) {

        for (DataSnapshot childOfDay : dayChild.getChildren()) {

            if (childOfDay.getKey().equals("Place")) {

                placeStored = childOfDay.getValue(String.class);
                tempMap.put(SECOND_COLUMN, placeStored);
                adapter.notifyDataSetChanged();

            } else if (childOfDay.getKey().equals("Time")) {

                timeStored = childOfDay.getValue(String.class);

                tempMap.put(THIRD_COLUMN, timeStored);

                // if the courier is off, store in an arraylist the map so that the icon won't be displayed
                if (timeStored.equals("OFF")) {

                    daysCourierIsOFF.add(tempMap);
                }

                else {

                    daysCourierIsON.add(tempMap);
                }

                    adapter.notifyDataSetChanged();

                }

            }
        }

     private void clearSchedule () {

         placeStoredMon = "";
         placeStoredTue = "";
         placeStoredWed = "";
         placeStoredThu = "";
         placeStoredFri = "";
         placeStoredSat = "";
         placeStoredSun = "";
         timeStoredMon = "";
         timeStoredTue = "";
         timeStoredWed = "";
         timeStoredThu = "";
         timeStoredFri = "";
         timeStoredSat = "";
         timeStoredSun = "";

         map.put(SECOND_COLUMN, placeStoredMon);
         map2.put(SECOND_COLUMN, placeStoredTue);
         map3.put(SECOND_COLUMN, placeStoredWed);
         map4.put(SECOND_COLUMN, placeStoredThu);
         map5.put(SECOND_COLUMN, placeStoredFri);
         map6.put(SECOND_COLUMN, placeStoredSat);
         map7.put(SECOND_COLUMN, placeStoredSun);

         map.put(THIRD_COLUMN, timeStoredMon);
         map2.put(THIRD_COLUMN, timeStoredTue);
         map3.put(THIRD_COLUMN, timeStoredWed);
         map4.put(THIRD_COLUMN, timeStoredThu);
         map5.put(THIRD_COLUMN, timeStoredFri);
         map6.put(THIRD_COLUMN, timeStoredSat);
         map7.put(THIRD_COLUMN, timeStoredSun);
         populateWithPendingIcon(map);
         populateWithPendingIcon(map2);
         populateWithPendingIcon(map3);
         populateWithPendingIcon(map4);
         populateWithPendingIcon(map5);
         populateWithPendingIcon(map6);
         populateWithPendingIcon(map7);

         // update the list view
         listView.setAdapter(adapter);

         // this is to implement 1.2A till 1.2C user should not be able to click on a schedule ahead from the current week
         scheduleIsCleared = true;
     }

     private void hasCourierAddedDeliveries () {

         rowItems = new ArrayList<RowItem>();

         url = FirebaseDatabase.getInstance().getReference();

         DatabaseReference dayToCheck = url.child("Rota").child(uid)
                 .child("Week:"+firstDayOfTheWeek.replaceAll("/", "-") + " until "+lastDayOfTheWeek.replaceAll("/", "-"));

         dayToCheck.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                 for (DataSnapshot dayChild : dataSnapshot.getChildren()) {

                     switch (dayChild.getKey()) {

                         case "Monday":

                             readFromDBStatusIcon(map, dayChild);
                             break;

                         case "Tuesday":

                             readFromDBStatusIcon(map2, dayChild);
                             break;

                         case "Wednesday":

                             readFromDBStatusIcon(map3, dayChild);
                             break;

                         case "Thursday":

                             readFromDBStatusIcon(map4, dayChild);
                             break;

                         case "Friday":

                             readFromDBStatusIcon(map5, dayChild);
                             break;

                         case "Saturday":

                             readFromDBStatusIcon(map6, dayChild);
                             break;

                         case "Sunday":

                             readFromDBStatusIcon(map7, dayChild);
                             break;

                        }
                    }
                }
             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

         populateWithPendingIcon(map);
         populateWithPendingIcon(map2);
         populateWithPendingIcon(map3);
         populateWithPendingIcon(map4);
         populateWithPendingIcon(map5);
         populateWithPendingIcon(map6);
         populateWithPendingIcon(map7);

         listView.setAdapter(adapter);
     }

    private void updateStatusIcon (String monStr, String tueStr, String wedStr, String thuStr,
                                   String friStr, String satStr, String sunStr) {

        rowItems = new ArrayList<RowItem>();

        if (daysCourierIsON.size() > 0) {

            for (int i = 0; i < daysCourierIsON.size(); i++) {

                updateStatusIconAfterClickOnSubmit(monStr, map);
                updateStatusIconAfterClickOnSubmit(tueStr, map2);
                updateStatusIconAfterClickOnSubmit(wedStr, map3);
                updateStatusIconAfterClickOnSubmit(thuStr, map4);
                updateStatusIconAfterClickOnSubmit(friStr, map5);
                updateStatusIconAfterClickOnSubmit(satStr, map6);
                updateStatusIconAfterClickOnSubmit(sunStr, map7);
            }

            listView.setAdapter(adapter);
        }


    }

    private void readFromDBStatusIcon (Map tempMap, DataSnapshot dayChild) {

        String timeStored = "";
        Calendar cal = Calendar.getInstance(Locale.US);
        int day = cal.get(Calendar.DAY_OF_WEEK);

    for (DataSnapshot childrenOfDay : dayChild.getChildren()) {

        if (childrenOfDay.getKey().equals("Time")) {

            timeStored = childrenOfDay.getValue(String.class);

            if (timeStored.equals("OFF")) {

                RowItem item = new RowItem(xIcon);

                tempMap.put(FOURTH_COLUMN, String.valueOf(xIcon));

                rowItems.add(item);

            }
        }
    }

        if (dayChild.hasChild("Deliveries")) {

            RowItem item = new RowItem(tickIcon);

            tempMap.put(FOURTH_COLUMN, String.valueOf(tickIcon));

            rowItems.add(item);

            courierHasNotAddedDeliveriesOnAtLeastOneDay = false;

        }

        else if (!dayChild.hasChild("Deliveries") && !timeStored.equals("OFF")) {

            RowItem item = new RowItem(pendingIcon);

            tempMap.put(FOURTH_COLUMN, String.valueOf(pendingIcon));

            rowItems.add(item);

            courierHasNotAddedDeliveriesOnAtLeastOneDay = true;

    }

        // send the notification only if they haven't filled all the deliveries and it is Sunday
        if (courierHasNotAddedDeliveriesOnAtLeastOneDay && day == 1) {

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent(this, AlarmReceiver.class);

            notificationIntent.setAction(ACTION_TWO);

            notificationIntent.addCategory("android.intent.category.DEFAULT");

            PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + AlarmManager.RTC_WAKEUP, AlarmManager.RTC_WAKEUP, broadcast);

        }
    }

    private void updateStatusIconAfterClickOnSubmit (String day, Map tempMap) {

        if (day != null) {

            if (day.equals("")) {

                RowItem item = new RowItem(pendingIcon);

                tempMap.put(FOURTH_COLUMN, String.valueOf(pendingIcon));

                rowItems.add(item);

            }

            else if (day.matches(".*\\d+.*")) {

                RowItem item = new RowItem(tickIcon);

                tempMap.put(FOURTH_COLUMN, String.valueOf(tickIcon));

                rowItems.add(item);

            }
        }
    }

    private void populateWithPendingIcon (Map tempMap) {

        RowItem item = new RowItem(pendingIcon);

        tempMap.put(FOURTH_COLUMN, String.valueOf(pendingIcon));

        rowItems.add(item);
    }

    private void triggerNotificationOnShiftChange ()  {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        Intent notificationIntent = new Intent(this, AlarmReceiver.class);

        notificationIntent.setAction(ACTION_ONE);

        notificationIntent.addCategory("android.intent.category.DEFAULT");

        PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + AlarmManager.RTC_WAKEUP, AlarmManager.RTC_WAKEUP, broadcast);

    }

}
