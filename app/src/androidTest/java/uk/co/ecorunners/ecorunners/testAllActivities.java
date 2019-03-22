package uk.co.ecorunners.ecorunners;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class testAllActivities {

    private String currentDay;

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<>(SplashScreen.class);

    @Before
    public void setUp(){
        final Calendar calendar = Calendar.getInstance();
        int currentDayInt = calendar.get(Calendar.DAY_OF_MONTH);
        currentDay = String.format("%02d", currentDayInt);
    }

    @Test
    public void testAdminNoAdmin() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(4000);

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.emailField),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("cousma@yahoo.com"));


        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.passwordField),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText6.perform(replaceText("password"));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.loginBtn), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(1000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.rotaCalendarBtn), withText("Rota Calendar"),
                        childAtPosition(
                                allOf(withId(R.id.relativeViewID),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatButton2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(1000);


        DataInteraction linearLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.linerLayoutID),
                                3)))
                .atPosition(0);
        linearLayout.perform(scrollTo(), click());

       sleep(1000);

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.deliveryNum),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                3)),
                                1),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("2"), closeSoftKeyboard());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.nextShiftBtn), withText("Next Shift"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.deliveryNum),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                3)),
                                1),
                        isDisplayed()));
        appCompatEditText7.perform(replaceText("2"), closeSoftKeyboard());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.nextShiftBtn), withText("Next Shift"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.nextShiftBtn), withText("Next Shift"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton5.perform(click());

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.deliveryNum),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                3)),
                                1),
                        isDisplayed()));
        appCompatEditText8.perform(replaceText("2"), closeSoftKeyboard());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.nextShiftBtn), withText("Next Shift"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton6.perform(click());

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.nextShiftBtn), withText("Next Shift"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton7.perform(click());

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.deliveryNum),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                3)),
                                1),
                        isDisplayed()));
        appCompatEditText9.perform(replaceText("3"), closeSoftKeyboard());

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(R.id.nextShiftBtn), withText("Next Shift"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton8.perform(click());

        ViewInteraction appCompatButton9 = onView(
                allOf(withId(R.id.submitBtn), withText("Submit"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatButton9.perform(click());



        DataInteraction linearLayout3 = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.linerLayoutID),
                                3)))
                .atPosition(1);
        linearLayout3.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        DataInteraction linearLayout2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.linerLayoutID),
                                3)))
                .atPosition(2);
        linearLayout2.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        DataInteraction linearLayout4 = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.linerLayoutID),
                                3)))
                .atPosition(3);
        linearLayout4.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        DataInteraction linearLayout5 = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.linerLayoutID),
                                3)))
                .atPosition(4);
        linearLayout5.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton4.perform(click());


        DataInteraction linearLayout7 = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.linerLayoutID),
                                3)))
                .atPosition(5);
        linearLayout7.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton5 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton5.perform(click());

        DataInteraction linearLayout6 = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.linerLayoutID),
                                3)))
                .atPosition(6);
        linearLayout6.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton6 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton6.perform(click());



        ViewInteraction appCompatButton10 = onView(
                allOf(withId(R.id.nextWeekBtn), withText("Next Week"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.linerLayoutID),
                                        4),
                                1)));
        appCompatButton10.perform(scrollTo(), click());

        ViewInteraction appCompatButton11 = onView(
                allOf(withId(R.id.previousWeekBtn), withText("previous week"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.linerLayoutID),
                                        4),
                                0)));
        appCompatButton11.perform(scrollTo(), click());

        ViewInteraction appCompatButton12 = onView(
                allOf(withId(R.id.homeBtn), withText("Home"),
                        childAtPosition(
                                allOf(withId(R.id.linerLayoutID),
                                        childAtPosition(
                                                withClassName(is("android.widget.ScrollView")),
                                                0)),
                                5)));
        appCompatButton12.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(1000);

        ViewInteraction appCompatButton13 = onView(
                allOf(withId(R.id.guidesBtn), withText("Guides"),
                        childAtPosition(
                                allOf(withId(R.id.relativeViewID),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton13.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(1000);

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.courierDetails), withText("Couriers Contact Details"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatImageButton7 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton7.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(500);

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.courierRates), withText("Couriers Rates"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatTextView2.perform(click());

        ViewInteraction appCompatImageButton8 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton8.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(500);

        ViewInteraction appCompatTextView3 = onView(
                allOf(withId(R.id.shopsGuides), withText("Shops Guides"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        appCompatTextView3.perform(click());

        ViewInteraction appCompatImageButton9 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton9.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(500);

        ViewInteraction appCompatTextView4 = onView(
                allOf(withId(R.id.spareBicycleRecord), withText("Spare Bicycle Record"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                8),
                        isDisplayed()));
        appCompatTextView4.perform(click());

        ViewInteraction appCompatImageButton10 = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton10.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(1000);

        ViewInteraction appCompatButton14 = onView(
                allOf(withId(R.id.homeBtn), withText("Home"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                9),
                        isDisplayed()));
        appCompatButton14.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(500);

        ViewInteraction appCompatButton15 = onView(
                allOf(withId(R.id.managementRotaBtn), withText("Management Rota"),
                        childAtPosition(
                                allOf(withId(R.id.relativeViewID),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                4),
                        isDisplayed()));
        appCompatButton15.perform(click());

        ViewInteraction appCompatButton16 = onView(
                allOf(withId(R.id.managementChatBtn), withText("Management Chat"),
                        childAtPosition(
                                allOf(withId(R.id.relativeViewID),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                5),
                        isDisplayed()));
        appCompatButton16.perform(click());

        ViewInteraction appCompatButton17 = onView(
                allOf(withId(R.id.emergencyBtn), withText("Emergency"),
                        childAtPosition(
                                allOf(withId(R.id.relativeViewID),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                6),
                        isDisplayed()));
        appCompatButton17.perform(click());

        ViewInteraction appCompatButton18 = onView(
                allOf(withId(R.id.supportBtn), withText("Support"),
                        childAtPosition(
                                allOf(withId(R.id.relativeViewID),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                7),
                        isDisplayed()));
        appCompatButton18.perform(click());

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView5 = onView(
                allOf(withId(R.id.title), withText("Logout"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView5.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(1000);

        ViewInteraction appCompatButton19 = onView(
                allOf(withId(R.id.registerBtn), withText("Need a New Account? Click Here"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton19.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        sleep(500);

        ViewInteraction appCompatButton21 = onView(
                allOf(withId(R.id.registerBtn), withText("Sign Up"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                6)));
        appCompatButton21.perform(scrollTo(), click());

        ViewInteraction appCompatEditText10 = onView(
                allOf(withId(R.id.nameField),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                0)));
        appCompatEditText10.perform(scrollTo(), replaceText("test"));

        ViewInteraction appCompatEditText11 = onView(
                allOf(withId(R.id.lastNameField),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                8)));
        appCompatEditText11.perform(scrollTo(), replaceText("test"));

        ViewInteraction appCompatEditText12 = onView(
                allOf(withId(R.id.phoneNum),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                1)));
        appCompatEditText12.perform(scrollTo(), replaceText("07923234567"));

        ViewInteraction appCompatEditText13 = onView(
                allOf(withId(R.id.fullAddressField),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                9)));
        appCompatEditText13.perform(scrollTo(), replaceText("test"));

        ViewInteraction appCompatEditText14 = onView(
                allOf(withId(R.id.ninoField),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                10)));
        appCompatEditText14.perform(scrollTo(), replaceText("SP34567D"));

        ViewInteraction appCompatEditText15 = onView(
                allOf(withId(R.id.emailField),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                4)));
        /*i added the current day to test email because it does not allow to register with the same email */
        appCompatEditText15.perform(scrollTo(), replaceText("test"+currentDay+"@gmail.com"));

        ViewInteraction appCompatEditText16 = onView(
                allOf(withId(R.id.passwordField),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                5)));
        appCompatEditText16.perform(scrollTo(), replaceText("password"));

        ViewInteraction appCompatCheckBox = onView(
                allOf(withId(R.id.registerCheckBox),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                11)));
        appCompatCheckBox.perform(scrollTo(), click());

        ViewInteraction appCompatButton20 = onView(
                allOf(withId(R.id.registerBtn), withText("Sign Up"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                6)));
        appCompatButton20.perform(scrollTo(), click());

        sleep(1000);
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
