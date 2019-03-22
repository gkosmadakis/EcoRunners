package uk.co.ecorunners;

import android.content.Context;

import com.google.firebase.FirebaseApp;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uk.co.ecorunners.ecorunners.NotificationReceivedHandler;
import uk.co.ecorunners.ecorunners.activity.LoginActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class NotificationReceivedHandlerTest {
    private LoginActivity loginActivity;

    @Before
    public void setUp(){

        FirebaseApp.initializeApp(RuntimeEnvironment.application);
        loginActivity = Robolectric.buildActivity(LoginActivity.class)
                .create()
                .visible()
                .get();
    }

    @Test
    public void testGetFirstName() {

        Context context = mock(Context.class);
        Map userIDToNameLastName = new HashMap();
        Set<String> nameAndLastName = new LinkedHashSet<>();
        nameAndLastName.add("name:" + "Adam");
        nameAndLastName.add("lastname:" + "Smith");
        userIDToNameLastName.put("CourierID1", nameAndLastName);

        NotificationReceivedHandler handler = new NotificationReceivedHandler(context);

        assertEquals("Adam", handler.getFirstName(userIDToNameLastName, "CourierID1"));

    }

    @Test
    public void testGetLastName() {

        Context context = mock(Context.class);
        Map userIDToNameLastName = new HashMap();
        Set<String> nameAndLastName = new LinkedHashSet<>();
        nameAndLastName.add("name:" + "Adam");
        nameAndLastName.add("lastname:" + "Smith");
        userIDToNameLastName.put("CourierID1", nameAndLastName);

        NotificationReceivedHandler handler = new NotificationReceivedHandler(context);

        assertEquals("Smith", handler.getLastName(userIDToNameLastName, "CourierID1"));

    }

    @Test
    public void testGetTheCurrentDay(){

        Context context = mock(Context.class);
        NotificationReceivedHandler handler = new NotificationReceivedHandler(context);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
        Calendar calendar = Calendar.getInstance();

        assertEquals(dayFormat.format(calendar.getTime()), handler.getTheCurrentDay());
    }

    @Test
    public void testFindUsersOnCover(){

        Context context = mock(Context.class);
        NotificationReceivedHandler handler = new NotificationReceivedHandler(context);
        Map<String, Set<String>> dayToUserID = new HashMap<>();
        Set<String> expectedUserIDs = new LinkedHashSet<>();
        expectedUserIDs.add("CourierID1");
        expectedUserIDs.add("CourierID2");

        String currentDay = handler.getTheCurrentDay();
        dayToUserID.put(currentDay, expectedUserIDs);

        assertEquals(expectedUserIDs, handler.findUsersOnCover(dayToUserID));
    }

    @Test
    public void testShowNotificationOneHourBeforeShift() throws JSONException {

        String stringToTest = ":You have shift in one hour at: 16:19-21:00";

        JSONObject testData = new JSONObject();

        testData.put("",stringToTest);

        NotificationReceivedHandler handler = new NotificationReceivedHandler(RuntimeEnvironment.application);

        handler.showNotificationOneHourBeforeShift(testData);

        assertTrue(handler.getAlert().isShowing());
    }

    @Test
    public void testShowHasAcceptedSupportRequest() throws JSONException {

        String stringToTest = ":has accepted the support request";

        JSONObject testData = new JSONObject();

        testData.put("",stringToTest);

        NotificationReceivedHandler handler = new NotificationReceivedHandler(RuntimeEnvironment.application);

        handler.showHasAcceptedSupportRequest(testData);

        assertTrue(handler.getAlert().isShowing());
    }

    @Test
    public void testShowSupportNotification() throws JSONException {

        String stringToTest = ":Support";

        JSONObject testData = new JSONObject();

        testData.put("",stringToTest);

        NotificationReceivedHandler handler = new NotificationReceivedHandler(RuntimeEnvironment.application);

        handler.showSupportNotification(testData);

        assertTrue(handler.getAlert().isShowing());
    }

    @Test
    public void testShowEmergencyNotification() throws JSONException {

        String stringToTest = ":Emergency";

        JSONObject testData = new JSONObject();

        testData.put("",stringToTest);

        NotificationReceivedHandler handler = new NotificationReceivedHandler(RuntimeEnvironment.application);

        handler.showEmergencyNotification(testData);

        assertTrue(handler.getAlert().isShowing());
    }
}
