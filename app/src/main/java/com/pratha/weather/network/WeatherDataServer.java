package com.pratha.weather.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.pratha.weather.model.CityWeather;
import com.pratha.weather.model.DayWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * will fetch weather data from server
 * <p/>
 * Created by Pratha on 8/20/2016.
 */
public class WeatherDataServer {
    private Context mContext = null;
    private final String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?APPID=4c4c5ca2e47a58221409f63bd703025c&units=metric&cnt=14";
    private final String cityTemplate = "&q=<CITY>";
    private final String locationTemplate = "&lat=<LAT>&lon=<LON>";


    private WeatherDataCallback mCallback = new WeatherDataCallback() {
        @Override
        public void onWeatherDataReceived(CityWeather cityWeather) {
            Log.w("WeatherDataServer", "WeatherDataCallback not implemented");
        }

        @Override
        public void onWeatherDataError(String error) {
            Log.w("WeatherDataServer", "WeatherDataCallback not implemented");
        }
    };

    /**
     * constructor
     *
     * @param context context
     */
    public WeatherDataServer(Context context) {
        mContext = context;
    }

    /**
     * will provide weather data for city
     *
     * @param cityName name of the city
     * @param callback call back to get weather data
     */
    public void getWeatherDataForCity(String cityName, WeatherDataCallback callback) {
        mCallback = callback != null ? callback : mCallback;
        fetchWeatherData(baseUrl + cityTemplate.replace("<CITY>", cityName));
    }

    /**
     * get weather data for location
     *
     * @param latitude  location latitude
     * @param longitude location longitude
     * @param callback  callback to get weather data
     */
    public void getWeatherDataForLocation(String latitude, String longitude, WeatherDataCallback callback) {
        mCallback = callback != null ? callback : mCallback;
        fetchWeatherData(baseUrl + locationTemplate.replace("<LAT>", latitude).replace("<LON>", longitude));
    }

    /**
     * interface to get weather data
     */
    public interface WeatherDataCallback {
        void onWeatherDataReceived(CityWeather cityWeather);

        void onWeatherDataError(String error);
    }

    /*
     * will get weather data from server
     */
    private void fetchWeatherData(String url) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        mCallback.onWeatherDataReceived(parseWeatherData(response));
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mCallback.onWeatherDataError(error.getMessage());
                    }
                });

        VolleyRequest.getInstance(mContext).addToRequestQueue(jsObjRequest);
    }


    /*
     * will parse weather data and return as model class
     */
    private CityWeather parseWeatherData(JSONObject response) {
        String imageUrlTemplate = "http://openweathermap.org/img/w/<ID>.png";

        try {
            List<DayWeather> dayWeatherList = new ArrayList<>();
            JSONArray arr = response.getJSONArray("list");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                Date date = new Date(obj.getLong("dt") * 1000);
                @SuppressLint("SimpleDateFormat")// need not to localize
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String formattedDate = simpleDateFormat.format(date);

                DayWeather dayWeather = new DayWeather(
                        obj.getJSONArray("weather").getJSONObject(0).getString("description"),
                        obj.getJSONObject("temp").getString("min") + "°",
                        obj.getJSONObject("temp").getString("max") + "°",
                        formattedDate,
                        imageUrlTemplate.replace("<ID>",
                                obj.getJSONArray("weather").getJSONObject(0).getString("icon"))
                );
                dayWeatherList.add(dayWeather);
            }
            JSONObject city = response.getJSONObject("city");
            return new CityWeather(city.getString("name"), dayWeatherList);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
