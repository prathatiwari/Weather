package com.pratha.weather.model;

import java.util.List;

/**
 * City Weather
 * <p>
 * Created by Pratha on 8/21/2016.
 */
public class CityWeather {
    public String cityName;
    public List<DayWeather> dayWeatherList;

    public CityWeather(String cityName, List<DayWeather> dayWeatherList) {
        this.cityName = cityName;
        this.dayWeatherList = dayWeatherList;
    }
}
