package uk.co.ecorunners;

import android.app.Activity;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.co.ecorunners.ecorunners.activity.MainActivity;
import uk.co.ecorunners.ecorunners.utils.MainActivityUtil;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainActivityTests {

    @Test
    public void testFindUsersOnCoverAndNotifyOnEmergency(){

        MainActivityUtil util = new MainActivityUtil();
        /*The case where loggedIn user is ID1 and Monday is the current day couriers on cover are on Tuesday and Wednesday so no notification will be sent*/
        String weekDay = "Monday";
        String typeOfNotification = "emergency";
        LinkedHashSet userIDToNotify = new LinkedHashSet();
        String loggedInUser = "courierID1";
        Map<String, Set<String>> dayToUserID = new HashMap<>();
        Set<String> userIDs = new LinkedHashSet<>();
        userIDs.add("courierID1");
        dayToUserID.put("Tuesday", userIDs);
        userIDs = new LinkedHashSet<>();
        userIDs.add("courierID2");
        dayToUserID.put("Wednesday", userIDs);

        final Activity mMainActivity = mock(MainActivity.class);
        Map<String, Set<String>> userIDToNameLastNameNonStatic = new HashMap<>();
        when(((MainActivity) mMainActivity).getUserIDToNameLastNameNonStatic()).thenReturn(userIDToNameLastNameNonStatic);
        when(((MainActivity) mMainActivity).getDayToUserIDOnCover()).thenReturn(dayToUserID);

        boolean expectedvalue = util.findUsersOnCoverAndNotify(weekDay, typeOfNotification, userIDToNotify, loggedInUser, mMainActivity);
        assertFalse(expectedvalue);

        /*The case where loggedIn user is ID1 and wednesday is the current day and the day where courier ID2 is on cover so notification will be sent*/
        weekDay = "Wednesday";
        expectedvalue = util.findUsersOnCoverAndNotify(weekDay, typeOfNotification, userIDToNotify, loggedInUser, mMainActivity);
        assertTrue(expectedvalue);
    }

    @Test
    public void testFindAdminsAndNotifyOnEmergency(){

        MainActivityUtil util = new MainActivityUtil();
        String typeOfNotification = "emergency";
        Set<String> adminUsersID = new LinkedHashSet<>();
        adminUsersID.add("courierIDAdmin1");
        adminUsersID.add("courierIDAdmin2");
        Map<String, Set<String>> userIDToNameLastNameNonStatic = new HashMap<>();

        final Activity mMainActivity = mock(MainActivity.class);
        when(((MainActivity) mMainActivity).getUserIDToNameLastNameNonStatic()).thenReturn(userIDToNameLastNameNonStatic);

        /* the case where 2 admin users exist logged in user is courierIDAdmin1 so notify the courierIDAdmin2*/
        boolean expectedBoolean = util.findAdminsAndNotify(typeOfNotification, adminUsersID, "courierIDAdmin1", mMainActivity);
        assertTrue(expectedBoolean);

    }
}


