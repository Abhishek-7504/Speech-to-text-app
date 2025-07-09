package com.example.voskapplication;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testSelectButtonIsDisplayed() {
        onView(withId(R.id.selectButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testInitialTextViewIsDisplayed() {
        onView(withId(R.id.resultText)).check(matches(isDisplayed()));
    }

    @Test
    public void testButtonClickDoesNotCrash() {
        onView(withId(R.id.selectButton)).perform(click());
    }
}
