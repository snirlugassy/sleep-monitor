package com.aimlab.sleepmonitor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    SharedPreferences preferences;
    Logger logger;
    FragmentManager supportFragmentManager;
    NavHostFragment navHostFragment;
    NavController navController;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        supportFragmentManager = getSupportFragmentManager();
        navHostFragment = (NavHostFragment) supportFragmentManager.findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        bottomNav = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(bottomNav, navController);

        Resources resources = getResources();

        // CREATE RECORDINGS FOLDER IF DOESN'T EXISTS
        File filesDir = new File(getExternalFilesDir(null), "recordings");
        System.out.println(filesDir.getPath());
        if (filesDir.exists()==false) {
            filesDir.mkdir();
        }

        // CREATE LOGGER
        logger = new Logger(this);

        // DEFAULT SHARED PREFERENCES
        preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = preferences.edit();


        // Use prefEditor.clear(); in order to clear the default preferences

        if (preferences.contains(resources.getString(R.string.recording_frequency_key)) == false) {
            prefEditor.putFloat(resources.getString(R.string.recording_frequency_key), 1);
        }

        if (preferences.contains(resources.getString(R.string.recording_delay_key)) == false) {
            prefEditor.putInt(resources.getString(R.string.recording_delay_key), 5);
        }

        if (preferences.contains(resources.getString(R.string.therapy_mode_key)) == false) {
            prefEditor.putBoolean(resources.getString(R.string.therapy_mode_key), false);
        }

        prefEditor.apply();

        // HANDLE UNCAUGHT EXCEPTIONS
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                logger.write("--- UNCAUGHT EXCEPTION ---");
                e.printStackTrace(logger.getPrintWriter());
                logger.write("--------------------------");
            }
        });
    }

    public void setNavigationEnabled(boolean enabled) {
        int size = bottomNav.getMenu().size();
        for (int i=0; i<size; i++ ) {
            bottomNav.getMenu().getItem(i).setEnabled(enabled);
        }
    }
}