package com.dorma.weather.network;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.dorma.weather.WeatherApplication;
import com.dorma.weather.db.DatabaseHelper;
import com.dorma.weather.db.WeatherDAO;
import com.dorma.weather.db.WeatherItem;
import com.dorma.weather.network.model.Time;
import com.dorma.weather.network.model.WeatherData;

import org.apache.commons.io.IOUtils;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public class WeatherService extends IntentService {

    private WeatherDAO dao;

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dao = new WeatherDAO(this);
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
                    SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    for (Time time : data.forecast) {
                        dao.insert(new WeatherItem(
                                format.parse(time.startTime).getTime(),
                                format.parse(time.endTime).getTime(),
                                time.symbol.name,
                                time.temperature.value,
                                time.temperature.value > 15));
                    }
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
