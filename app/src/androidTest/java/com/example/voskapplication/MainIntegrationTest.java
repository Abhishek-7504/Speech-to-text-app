package com.example.voskapplication;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class MainIntegrationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);



    @Test
    public void testModelLoadsSuccessfully() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                boolean ready = activity.modelReadyLatch.await(200, TimeUnit.SECONDS); // extended wait time
                Assert.assertTrue("Model should be ready after retries", ready);
                Assert.assertTrue("Model state not set as ready", activity.isModelReady());
            } catch (InterruptedException e) {
                Assert.fail("Test interrupted while waiting for model load");
            }
        });
    }






    @Test
    public void testModelFilesExistInInternalStorage() {
        Context context = ApplicationProvider.getApplicationContext();
        File modelDir = new File(context.getFilesDir(), "vosk_model");
        Assert.assertTrue("Model folder should exist", modelDir.exists());
        Assert.assertTrue("Model should not be empty", Objects.requireNonNull(modelDir.list()).length > 0);
    }
}
