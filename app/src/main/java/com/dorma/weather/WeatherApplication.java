package com.dorma.weather;

import android.app.Application;

import com.dorma.weather.db.DatabaseHelper;

public class WeatherApplication extends Application {

    private DatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
    }

    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }
}
