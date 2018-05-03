package uk.co.ecorunners.ecorunners;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleTimerTask extends TimerTask {

    static Context context;
    private Date oneHourBeforeShift;
    private static String timeStored;
    public final String ACTION_THREE = "3";

    public ScheduleTimerTask (Context context) {
        this.context = context;
    }

    public void run() {

        final Calendar calendar = Calendar.getInstance();

        Date date = calendar.getTime();

        SimpleDateFormat hour = new SimpleDateFormat("HH:mm");

        hour.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));

        String localTime = hour.format(date);

        System.out.println("Start time:" + localTime);

        triggerTimer();

        // get the time when shift starts
        String timeWhenShiftStarts = timeStored.substring(0, timeStored.indexOf("-"));

        Date dateWhenShiftStarts = null;

        // Calculate one hour before
        try {

            // pass the current date 21/09/2017 into the timeWhenShiftStarts
            SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");

            String dateToday = formatDate.format(calendar.getTime());

            timeWhenShiftStarts = dateToday + " " + timeWhenShiftStarts + ":00";

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            dateWhenShiftStarts = format.parse(timeWhenShiftStarts);

            System.out.println("Date when shift starts is: " + dateWhenShiftStarts);

            calendar.setTime(dateWhenShiftStarts);

            calendar.add(Calendar.HOUR, -1);

            // get the time to get notified
            oneHourBeforeShift = calendar.getTime();

            System.out.println("One hour before shift is: " + oneHourBeforeShift);

        } catch (ParseException e) {

            e.printStackTrace();

        }

        // this is to send notification only ONCE one hour before shift not every time the user logs in where time is less than
        // an hour. For example shift is at 12:30 user logs in at 12:00 and get the notification
        if (new Date().getTime() >= oneHourBeforeShift.getTime() && new Date().getTime() < dateWhenShiftStarts.getTime()) {

            Timer timer2 = new Timer();

            timer2.schedule(new TimerTask() {
                @Override
                public void run() {

                    System.out.println("Task invoked one hour before shift at: " + oneHourBeforeShift);

                    triggerNotificationOneHourBeforeShift();
                }
            }, oneHourBeforeShift);
        }
    }

    public static void main(String[] args) throws ParseException {

        //the Date and time at which i want to start to execute
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = dateFormatter.parse("2017-10-18 06:00:00");

        Timer timer = new Timer();

        //this is  the interval time between every test. i set it to one day
        //int period = 86400000;//24 hours
        int period = 3600000;

        //Schedule it to run/execute it repeatedly
        timer.schedule(new ScheduleTimerTask(context), date, period);

        //Use this if you want to execute it once
        //timer.schedule(new RunnerTest(), date);
    }


    private void triggerTimer() {

        try {

            timeStored = LoginActivity.getTimeStored();

            System.out.println("Time stored is: "+timeStored);

            Thread.sleep(100);

        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }

    private void triggerNotificationOneHourBeforeShift()  {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(context, AlarmReceiver.class);

        notificationIntent.setAction(ACTION_THREE);

        notificationIntent.addCategory("android.intent.category.DEFAULT");

        notificationIntent.putExtra("timeStored", timeStored);

        PendingIntent broadcast = PendingIntent.getBroadcast(context, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + AlarmManager.RTC_WAKEUP, AlarmManager.RTC_WAKEUP, broadcast);

        System.out.println("Notification triggered at: " +new Date());

    }

}