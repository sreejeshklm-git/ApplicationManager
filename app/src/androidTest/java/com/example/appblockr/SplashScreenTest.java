package com.example.appblockr;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SplashScreenTest {

    @Rule
    public ActivityScenarioRule<SplashScreen> mActivityScenarioRule =
            new ActivityScenarioRule<>(SplashScreen.class);

    @Test
    public void splashScreenTest() {
        Intents.init();
//        onView(isRoot()).perform(wa);


    }
}
