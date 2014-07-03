package com.dorma.weather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String FROM_TIME = "FROM_TIME";
    public static final String TO_TIME = "TO_TIME";
    public static final String FORECAST = "FORECAST";
    public static final String TEMPERATURE = "TEMPERATURE";
    public static final String WARM = "WARM";
    public static final String TABLE_NAME = "WEATHER";

    public DatabaseHelper(Context context) {
        super(context, "weather", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                " " + FROM_TIME + " INTEGER NOT NULL," +
                " " + TO_TIME + " INTEGER NOT NULL," +
                " " + FORECAST + " TEXT NOT NULL," +
                " " + TEMPERATURE + " FLOAT NOT NULL," +
                " " + WARM + " INT NOT NULL," +
                " PRIMARY KEY (" + FROM_TIME + ", " + TO_TIME + ") " +
                ");");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
