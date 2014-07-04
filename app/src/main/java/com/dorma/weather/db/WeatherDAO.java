package com.dorma.weather.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import com.dorma.weather.WeatherApplication;


public class WeatherDAO implements DAO<WeatherItem> {
    public static final String WEATHER_UPDATE_ACTION = "com.dorma.weather.loaded";
    private final DatabaseHelper helper;
    private final Context context;

    public WeatherDAO(Context context) {
        this.context = context;
        helper = ((WeatherApplication)context.getApplicationContext()).getDbHelper();
    }

    @Override
    public WeatherItem getById(long id) {
        Cursor cursor = helper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME, new String[]{"*"}, "ROWID=?", new String[]{String.valueOf(id)}, null, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            long from = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.FROM_TIME));
            long to = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.TO_TIME));
            String forecast = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FORECAST));
            float t = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.TEMPERATURE));
            boolean warm = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.WARM)) > 0;

            return new WeatherItem(from, to, forecast, t, warm);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public long insert(WeatherItem newItem) {
        try {
            return helper.getWritableDatabase().insert(DatabaseHelper.TABLE_NAME, null, toContentValues(newItem));
        } finally {
            notifyChange();
        }
    }

    private void notifyChange() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(WEATHER_UPDATE_ACTION));
    }

    @Override
    public void update(WeatherItem newItem) {
        try {
            helper.getWritableDatabase().update(
                    DatabaseHelper.TABLE_NAME,
                    toContentValues(newItem),
                    DatabaseHelper.FROM_TIME + "=? AND " + DatabaseHelper.TO_TIME + "=?",
                    new String[] {String.valueOf(newItem.startTime), String.valueOf(newItem.endTime)});
        } finally {
            notifyChange();
        }
    }

    @Override
    public void deleteById(long id) {
        try {
            helper.getWritableDatabase().delete(DatabaseHelper.TABLE_NAME,
                    "ROWID=?",
                    new String[] {String.valueOf(id)});
        } finally {
            notifyChange();
        }
    }

    @Override
    public ContentValues toContentValues(WeatherItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.FROM_TIME, item.startTime);
        values.put(DatabaseHelper.TO_TIME, item.endTime);
        values.put(DatabaseHelper.FORECAST, item.forecast);
        values.put(DatabaseHelper.TEMPERATURE, item.temperature);
        values.put(DatabaseHelper.WARM, item.warm);

        return values;
    }
}
