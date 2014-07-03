package com.dorma.weather.network;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="weatherdata", strict = false)
public class WeatherData {

    @ElementList(name="forecast", required = true, entry = "time", type = Time.class)
    public List<Time> forecast;

    public WeatherData(@ElementList(name="forecast", required = true, entry = "time", type = Time.class) List<Time> forecast) {
        this.forecast = forecast;
    }
}
