package com.pratha.weather.cityweather;

import android.support.annotation.NonNull;

import com.pratha.weather.model.CityWeather;
import com.pratha.weather.network.WeatherDataServer;
import com.pratha.weather.util.LocationContract;

/**
 * listens to UI events and retrieves the data and updates UI
 * <p/>
 * Created by Pratha on 8/20/2016.
 */
public class WeatherPresenter implements WeatherContract.Presenter,
        WeatherDataServer.WeatherDataCallback, LocationContract.LocationProviderCallback {

    private WeatherDataServer mWeatherDataServer;
    private WeatherContract.View mView;
    private LocationContract.WeatherLocationProvider mWeatherLocationProvider;

    public WeatherPresenter(@NonNull WeatherDataServer weatherDataServer,
                            WeatherContract.View weatherView, LocationContract.WeatherLocationProvider weatherLocationProvider) {
        mWeatherDataServer = weatherDataServer;
        mView = weatherView;
        mView.setPresenter(this);
        mWeatherLocationProvider = weatherLocationProvider;
    }


    @Override
    public void loadWeather(String city) {
        if (mView.isActive()) {
            mView.setLoadingIndicator(true);
            mWeatherDataServer.getWeatherDataForCity(city, this);
        }
    }

    @Override
    public void loadWeatherForCurrentLocation() {
        mView.setLoadingIndicator(true);
        mWeatherLocationProvider.getCurrentLocation(this);

    }

    @Override
    public void start() {
        loadWeatherForCurrentLocation();
    }

    @Override
    public void onWeatherDataReceived(CityWeather cityWeather) {
        if (mView.isActive()) {
            mView.setLoadingIndicator(false);
            if (cityWeather != null) {
                mView.showWeather(cityWeather);
            } else {
                mView.showLoadingWeatherError(WeatherFragment.ERROR_DATA_INCORRECT);
            }
        }
    }

    @Override
    public void onWeatherDataError(String error) {
        if (mView.isActive()) {
            mView.setLoadingIndicator(false);
            mView.showLoadingWeatherError(WeatherFragment.ERROR_NETWORK_PROBLEM);
        }
    }

    @Override
    public void onGetLocation(String latitude, String longitude) {
        if (mView.isActive()) {
            mWeatherDataServer.getWeatherDataForLocation(latitude, longitude, this);
        }
    }

    @Override
    public void onLocationError(int errorCode) {
        if (mView.isActive()) {
            mView.setLoadingIndicator(false);
            mView.showLoadingWeatherError(errorCode);
        }
    }
}
