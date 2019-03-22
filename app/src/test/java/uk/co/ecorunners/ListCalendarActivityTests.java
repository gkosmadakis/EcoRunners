package uk.co.ecorunners;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import uk.co.ecorunners.ecorunners.activity.ListCalendarActivity;
import uk.co.ecorunners.ecorunners.utils.Utils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ListCalendarActivityTests {

    private ListCalendarActivity listCalendarActivity;

    @Before
    public void setUp(){

        FirebaseApp.initializeApp(RuntimeEnvironment.application);
        listCalendarActivity = Robolectric.buildActivity(ListCalendarActivity.class)
                .create()
                .visible()
                .get();
    }

    @Test
    public void testClearSchedule(){

        final ListCalendarActivity activity = mock(ListCalendarActivity.class);
        ListView listView = mock(ListView.class);
        when(activity.getListView()).thenReturn(listView);

        Utils utils = new Utils();
        assertTrue(utils.clearSchedule(activity));
    }

    @Test
    public void testTriggerNotificationOnShiftChangeSDK24(){

        try {
            setFinalStatic(Build.VERSION.class.getField("SDK_INT"),24);

        } catch (Exception e) {

            Log.e("Exception", e.getMessage());
        }

        String testAction = "test";

        Utils utils = new Utils();
        utils.triggerNotificationOnShiftChange(testAction,listCalendarActivity);
    }

    @Test
    public void testTriggerNotificationOnShiftChangeSDK26(){

        try {
            setFinalStatic(Build.VERSION.class.getField("SDK_INT"),26);

        } catch (Exception e) {

            Log.e("Exception", e.getMessage());
        }

        String testAction = "test";

        Utils utils = new Utils();
        utils.triggerNotificationOnShiftChange(testAction,listCalendarActivity);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {

        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
