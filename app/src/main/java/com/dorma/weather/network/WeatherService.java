package com.dorma.weather.network;

import android.app.IntentService;
import android.content.Intent;

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
