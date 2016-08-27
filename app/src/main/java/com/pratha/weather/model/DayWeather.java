package com.pratha.weather.model;

/**
 * Day Weather
 * <p/>
 * Created by Pratha on 8/20/2016.
 */
public class DayWeather {
    public String title;
    public String min;
    public String max;
    public String date;
    public String imageUrl;

    public DayWeather(String title, String min, String max, String date, String imageUrl) {
        this.title = title;
        this.min = min;
        this.max = max;
        this.date = date;
        this.imageUrl = imageUrl;
    }
}
