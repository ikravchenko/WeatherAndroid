package com.dorma.weather.network;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dorma.weather.WeatherApplication;
import com.dorma.weather.db.DatabaseHelper;
import com.dorma.weather.network.model.Time;
import com.dorma.weather.network.model.WeatherData;

import org.apache.commons.io.IOUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherService extends IntentService {

    public static final String WEATHER_LOADED_ACTION = "com.dorma.weather.loaded";

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
                    SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    for (Time time : data.forecast) {
                        values.put(DatabaseHelper.FROM_TIME, format.parse(time.startTime).getTime());
                        values.put(DatabaseHelper.TO_TIME, format.parse(time.endTime).getTime());
                        values.put(DatabaseHelper.FORECAST, time.symbol.name);
                        values.put(DatabaseHelper.TEMPERATURE, time.temperature.value);
                        values.put(DatabaseHelper.WARM, time.temperature.value > 15);
                        ((WeatherApplication) getApplication()).getDbHelper().getWritableDatabase().replace(DatabaseHelper.TABLE_NAME, null, values);
                    }
                    LocalBroadcastManager.getInstance(WeatherService.this).sendBroadcast(new Intent(WEATHER_LOADED_ACTION));
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
