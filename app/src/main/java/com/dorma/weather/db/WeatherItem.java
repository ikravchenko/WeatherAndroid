package com.dorma.weather.db;

public class WeatherItem {
    public final long startTime;
    public final long endTime;
    public String forecast;
    public float temperature;
    public boolean warm;

    public WeatherItem(long startTime, long endTime, String forecast, float temperature, boolean warm) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.forecast = forecast;
        this.temperature = temperature;
        this.warm = warm;
    }
}
