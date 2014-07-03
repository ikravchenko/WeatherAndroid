package com.dorma.weather.network;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.dorma.weather.WeatherApplication;
import com.dorma.weather.db.DatabaseHelper;
import com.dorma.weather.network.model.WeatherData;

import org.apache.commons.io.IOUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService extends IntentService {

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        HttpURLConnection connection = null;
        try {
            try {
                URL url = new URL("http", "api.openweathermap.org" ,"data/2.5/forecast?q=Bonn&mode=xml");
                connection = (HttpURLConnection) url.openConnection();
                InputStream input = connection.getInputStream();
                try {
                    String s = IOUtils.toString(input);
                    Serializer serializer = new Persister();
                    WeatherData data = serializer.read(WeatherData.class, s);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.FROM_TIME, 1);
                    values.put(DatabaseHelper.TO_TIME, 2);
                    values.put(DatabaseHelper.FORECAST, "clouds");
                    values.put(DatabaseHelper.TEMPERATURE, 23.3);
                    values.put(DatabaseHelper.WARM, 1);
                    long value = ((WeatherApplication) getApplication()).getDbHelper().getWritableDatabase().replace(DatabaseHelper.TABLE_NAME, null, values);
                    Log.w(getClass().getSimpleName(), "INSERTED " + value);
                } finally {
                    IOUtils.closeQuietly(input);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
