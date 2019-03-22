package uk.co.ecorunners.ecorunners.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uk.co.ecorunners.ecorunners.R;
import uk.co.ecorunners.ecorunners.utils.ListViewAdapter;
import uk.co.ecorunners.ecorunners.utils.RowItem;
import uk.co.ecorunners.ecorunners.utils.Utils;

import static uk.co.ecorunners.ecorunners.utils.Constants.DATE_FORMAT;
import static uk.co.ecorunners.ecorunners.utils.Constants.FIRST_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.FOURTH_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.FRIDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.ITEM_POSITION;
import static uk.co.ecorunners.ecorunners.utils.Constants.MONDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.PLACE;
import static uk.co.ecorunners.ecorunners.utils.Constants.ROTA;
import static uk.co.ecorunners.ecorunners.utils.Constants.SATURDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.SECOND_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.SUNDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.THIRD_COLUMN;
import static uk.co.ecorunners.ecorunners.utils.Constants.THURSDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.TUESDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.UNTIL;
import static uk.co.ecorunners.ecorunners.utils.Constants.WEDNESDAY;
import static uk.co.ecorunners.ecorunners.utils.Constants.WEEK;

public class ListCalendarActivity extends AppCompatActivity {

    ListView listView ;
    DatabaseReference url;
    private String uid;
    private String firstDayOfTheWeek;
    private String lastDayOfTheWeek;
    private ListViewAdapter adapter;
    HashMap<String,String> map;
    HashMap<String,String> map2;
    HashMap<String,String> map3;
    HashMap<String,String> map4;
    HashMap<String,String> map5;
    HashMap<String,String> map6;
    HashMap<String,String> map7;
    private TextView datesView;
    private Button previousWeekBtn;
    Calendar calendar;
    private boolean scheduleIsCleared = false;
    // icons for the tick red x and pending icons
    public static final Integer TICK_ICON =  R.drawable.ic_check_circle_green_24dp_withoutbackground;
    public static final Integer X_ICON = R.drawable.ic_highlight_off_red_24dp_without_background;
    public static final Integer PENDING_ICON = R.drawable.ic_indeterminate_check_box_orange_24dp;
    List<RowItem> rowItems;
    protected static List<Map> daysCourierIsOFF = new ArrayList<>();
    protected static List<Map> daysCourierIsON = new ArrayList<>();
    public static final String ACTION_ONE = "1";
    public static final String ACTION_TWO = "2";
    private boolean courierHasNotAddedDeliveriesOnAtLeastOneDay;
    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_calendar);

        utils = new Utils();

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);

        SharedPreferences sharedPrefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        uid = sharedPrefs.getString("userID","");

        //Week View
        TextView weekView = (TextView) findViewById(R.id.weekView);

        //The dates view
        datesView = (TextView) findViewById(R.id.datesView);

        //get the first day of the week and the last
        calendar = Calendar.getInstance(Locale.US);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

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
            //and i add 6 days to get the Sunday of the current week
            calendar.add(Calendar.DATE, 6);

            lastDayOfTheWeek = simpleDateFormat.format(calendar.getTime());

            //set the days in the weekView and in the datesView
            weekView.setText("Week");
        }

        datesView.setText(firstDayOfTheWeek+UNTIL+lastDayOfTheWeek);

        //check if the app has connection to Firebase namely if the device is connected to Internet
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                boolean connected = snapshot.getValue(Boolean.class);

                if (connected) {

                    Log.i("LstClndrActv connectedL","connected");

                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ListCalendarActivity.this, android.R.style.Theme_Holo_Light_Dialog))
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
                    Log.i("LstClndrActv connectedL",String.valueOf(connected));
                    alert1.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Log.e("ListCalendarActivity ","Listener was cancelled");

            }
        });

        String currentWeek = firstDayOfTheWeek.replaceAll("/", "-") + UNTIL+lastDayOfTheWeek.replaceAll("/", "-");

        updateView(currentWeek);

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        map = new HashMap<>();
        map.put(FIRST_COLUMN, MONDAY);
        list.add(map);

        map2 = new HashMap<>();
        map2.put(FIRST_COLUMN, TUESDAY);
        list.add(map2);

        map3 = new HashMap<>();
        map3.put(FIRST_COLUMN, WEDNESDAY);
        list.add(map3);

        map4 = new HashMap<>();
        map4.put(FIRST_COLUMN, THURSDAY);
        list.add(map4);

        map5 = new HashMap<>();
        map5.put(FIRST_COLUMN, FRIDAY);
        list.add(map5);

        map6 = new HashMap<>();
        map6.put(FIRST_COLUMN, SATURDAY);
        list.add(map6);

        map7 = new HashMap<>();
        map7.put(FIRST_COLUMN, SUNDAY);
        list.add(map7);

        // call the method to initialize the stauts Icon, the fourth column.
        hasCourierAddedDeliveries();

        adapter = new ListViewAdapter(this, list, R.layout.colmn_row, rowItems);

        // Assign adapter to ListView
        listView.setAdapter(adapter);


        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            String mondayDelivery = bundle.getString("mondayDeliveries");
            String tuesdayDelivery = bundle.getString("tuesdayDeliveries");
            String wednesdayDelivery = bundle.getString("wednesdayDeliveries");
            String thursdayDelivery = bundle.getString("thursdayDeliveries");
            String fridayDelivery = bundle.getString("fridayDeliveries");
            String saturdayDelivery = bundle.getString("saturdayDeliveries");
            String sundayDelivery = bundle.getString("sundayDeliveries");

            updateStatusIcon(mondayDelivery, tuesdayDelivery, wednesdayDelivery, thursdayDelivery, fridayDelivery, saturdayDelivery, sundayDelivery);

        }

        //Previous Week Button
        previousWeekBtn = (Button) findViewById(R.id.previousWeekBtn);

        //set it to be invisible by default
        previousWeekBtn.setVisibility(View.INVISIBLE);

        //Next Week Button
        Button nextWeekBtn = (Button) findViewById(R.id.nextWeekBtn);

        //Home Button
        Button homeBtn = (Button) findViewById(R.id.homeBtn);

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ListCalendarActivity.this, LoginActivity.class);

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

                datesView.setText(firstDayOfTheWeek+UNTIL+lastDayOfTheWeek);

                previousWeekBtn.setVisibility(View.VISIBLE);

                // pass the current week to the method that populates the schedule
                String currentWeek = firstDayOfTheWeek.replaceAll("/", "-") + UNTIL+lastDayOfTheWeek.replaceAll("/", "-");

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

                datesView.setText(firstDayOfTheWeek+UNTIL+lastDayOfTheWeek);

                // pass the current week to the method that populates the schedule
                String currentWeek = firstDayOfTheWeek.replaceAll("/", "-") + UNTIL+lastDayOfTheWeek.replaceAll("/", "-");

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

                Intent intent = new Intent(ListCalendarActivity.this, AddDeliveriesActivity.class);

                switch (itemPosition) {

                    case 1:
                        intent.putExtra("day", "Monday");
                        intent.putExtra(PLACE, map.get(SECOND_COLUMN));
                        intent.putExtra("time", map.get(THIRD_COLUMN));
                        intent.putExtra(ITEM_POSITION, itemPosition-1);
                        break;

                    case 2:
                        intent.putExtra("day", "Tuesday");
                        intent.putExtra(PLACE, map2.get(SECOND_COLUMN));
                        intent.putExtra("time", map2.get(THIRD_COLUMN));
                        intent.putExtra(ITEM_POSITION, itemPosition-1);
                        break;

                    case 3:
                        intent.putExtra("day", "Wednesday");
                        intent.putExtra(PLACE, map3.get(SECOND_COLUMN));
                        intent.putExtra("time", map3.get(THIRD_COLUMN));
                        intent.putExtra(ITEM_POSITION, itemPosition-1);
                        break;

                    case 4:
                        intent.putExtra("day", "Thursday");
                        intent.putExtra(PLACE, map4.get(SECOND_COLUMN));
                        intent.putExtra("time", map4.get(THIRD_COLUMN));
                        intent.putExtra(ITEM_POSITION, itemPosition-1);
                        break;

                    case 5:
                        intent.putExtra("day", "Friday");
                        intent.putExtra(PLACE, map5.get(SECOND_COLUMN));
                        intent.putExtra("time", map5.get(THIRD_COLUMN));
                        intent.putExtra(ITEM_POSITION, itemPosition-1);
                        break;

                    case 6:
                        intent.putExtra("day", "Saturday");
                        intent.putExtra(PLACE, map6.get(SECOND_COLUMN));
                        intent.putExtra("time", map6.get(THIRD_COLUMN));
                        intent.putExtra(ITEM_POSITION, itemPosition-1);
                        break;

                    case 7:
                        intent.putExtra("day", "Sunday");
                        intent.putExtra(PLACE, map7.get(SECOND_COLUMN));
                        intent.putExtra("time", map7.get(THIRD_COLUMN));
                        intent.putExtra(ITEM_POSITION, itemPosition-1);
                        break;

                        default:
                            Log.i("ListCalendarActivity ", String.valueOf(itemPosition));
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
            }
        });

    }//end of onCreate


    public void updateView(String week){

        url = FirebaseDatabase.getInstance().getReference();

            DatabaseReference dailySchedulePlace = url.child(ROTA).child(uid).child(WEEK+week);

            dailySchedulePlace.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.exists()) {

                    Toast.makeText(ListCalendarActivity.this, "No data have been supplied", Toast.LENGTH_SHORT).show();

                    utils.clearSchedule(ListCalendarActivity.this);

                    }

                    for (DataSnapshot dayChild : dataSnapshot.getChildren()) {

                        switch (dayChild.getKey()) {

                            case MONDAY:
                                    populateSingleDaySchedule(dayChild, map);

                                break;

                            case TUESDAY:
                                    populateSingleDaySchedule(dayChild, map2);

                                break;

                            case WEDNESDAY:
                                    populateSingleDaySchedule(dayChild, map3);

                                break;

                            case THURSDAY:
                                    populateSingleDaySchedule(dayChild, map4);

                                break;

                            case FRIDAY:
                                    populateSingleDaySchedule(dayChild, map5);

                                break;

                            case SATURDAY:
                                    populateSingleDaySchedule(dayChild, map6);

                                break;

                            case SUNDAY:
                                   populateSingleDaySchedule(dayChild, map7);

                                break;

                                default:
                                    Log.i("ListClndar updateView ", dayChild.getKey());
                                    break;

                            }

                        listView.setAdapter(adapter);

                        }
                    }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                /* Not used */
                }
            });

        String [] daysToCheck = {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY};

        for (int i=0; i <daysToCheck.length; i++) {

            DatabaseReference dayToListenForChanges = url.child(ROTA).child(uid).child("Week:" + week).child(daysToCheck[i]);

            dayToListenForChanges.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    /*Not used */
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    Toast.makeText(ListCalendarActivity.this, "Notification change Listener triggered", Toast.LENGTH_LONG).show();

                    utils.triggerNotificationOnShiftChange(ACTION_ONE, ListCalendarActivity.this);

                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    /*Not used */
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    /*Not used */
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    /*Not used */
                }

            });
        }
    }

    private void populateSingleDaySchedule(DataSnapshot dayChild, HashMap<String, String> tempMap) {

        for (DataSnapshot childOfDay : dayChild.getChildren()) {

            if (childOfDay.getKey().equals("Place")) {

                String placeStored = childOfDay.getValue(String.class);
                tempMap.put(SECOND_COLUMN, placeStored);
                adapter.notifyDataSetChanged();

            } else if (childOfDay.getKey().equals("Time")) {

                String timeStored = childOfDay.getValue(String.class);

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

    private void hasCourierAddedDeliveries () {

         rowItems = new ArrayList<>();

         url = FirebaseDatabase.getInstance().getReference();

         DatabaseReference dayToCheck = url.child(ROTA).child(uid)
                 .child("Week:"+firstDayOfTheWeek.replaceAll("/", "-") + UNTIL+lastDayOfTheWeek.replaceAll("/", "-"));

         dayToCheck.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {

                 for (DataSnapshot dayChild : dataSnapshot.getChildren()) {

                     switch (dayChild.getKey()) {

                         case MONDAY:

                             readFromDBStatusIcon(map, dayChild);
                             break;

                         case TUESDAY:

                             readFromDBStatusIcon(map2, dayChild);
                             break;

                         case WEDNESDAY:

                             readFromDBStatusIcon(map3, dayChild);
                             break;

                         case THURSDAY:

                             readFromDBStatusIcon(map4, dayChild);
                             break;

                         case FRIDAY:

                             readFromDBStatusIcon(map5, dayChild);
                             break;

                         case SATURDAY:

                             readFromDBStatusIcon(map6, dayChild);
                             break;

                         case SUNDAY:

                             readFromDBStatusIcon(map7, dayChild);
                             break;

                             default:
                                 Log.i("hasCourierAddDeliveries", dayChild.getKey());
                                 break;
                        }
                    }
                }
             @Override
             public void onCancelled(DatabaseError databaseError) {
                /*Not used */
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

        rowItems = new ArrayList<>();

        if (!daysCourierIsON.isEmpty()) {

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

                RowItem item = new RowItem(X_ICON);

                tempMap.put(FOURTH_COLUMN, String.valueOf(X_ICON));

                rowItems.add(item);

            }
        }
    }

        if (dayChild.hasChild("Deliveries")) {

            RowItem item = new RowItem(TICK_ICON);

            tempMap.put(FOURTH_COLUMN, String.valueOf(TICK_ICON));

            rowItems.add(item);

            courierHasNotAddedDeliveriesOnAtLeastOneDay = false;

        }

        else if (!dayChild.hasChild("Deliveries") && !timeStored.equals("OFF")) {

            RowItem item = new RowItem(PENDING_ICON);

            tempMap.put(FOURTH_COLUMN, String.valueOf(PENDING_ICON));

            rowItems.add(item);

            courierHasNotAddedDeliveriesOnAtLeastOneDay = true;

    }

        // send the notification only if they haven't filled all the deliveries and it is Sunday
        if (courierHasNotAddedDeliveriesOnAtLeastOneDay && day == 1) {

            utils.triggerNotificationOnShiftChange(ACTION_TWO, ListCalendarActivity.this);

        }
    }

    private void updateStatusIconAfterClickOnSubmit (String day, Map tempMap) {

        if (day != null) {

            if (day.equals("")) {

                RowItem item = new RowItem(PENDING_ICON);

                tempMap.put(FOURTH_COLUMN, String.valueOf(PENDING_ICON));

                rowItems.add(item);

            }

            else if (day.matches(".*\\d+.*")) {

                RowItem item = new RowItem(TICK_ICON);

                tempMap.put(FOURTH_COLUMN, String.valueOf(TICK_ICON));

                rowItems.add(item);

            }
        }
    }

    private void populateWithPendingIcon (Map tempMap) {

        RowItem item = new RowItem(PENDING_ICON);

        tempMap.put(FOURTH_COLUMN, String.valueOf(PENDING_ICON));

        rowItems.add(item);
    }

    public boolean isScheduleCleared() {
        return scheduleIsCleared;
    }

    public void setScheduleIsCleared(boolean scheduleIsCleared) {
        this.scheduleIsCleared = scheduleIsCleared;
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public HashMap<String, String> getMap2() {
        return map2;
    }

    public HashMap<String, String> getMap3() {
        return map3;
    }

    public HashMap<String, String> getMap4() {
        return map4;
    }

    public HashMap<String, String> getMap5() {
        return map5;
    }

    public HashMap<String, String> getMap6() {
        return map6;
    }

    public HashMap<String, String> getMap7() {
        return map7;
    }

    public ListView getListView() {
        return listView;
    }

    public ListViewAdapter getAdapter() {
        return adapter;
    }

    public List<RowItem> getRowItems() {
        return rowItems;
    }
}
