package com.pratha.weather.cityweather;

import com.pratha.weather.model.CityWeather;
import com.pratha.weather.util.BasePresenter;
import com.pratha.weather.util.BaseView;

/**
 * Weather contract between view and presenter
 * <p/>
 * Created by Pratha on 8/20/2016.
 */
public interface WeatherContract {
    interface View extends BaseView<Presenter> {

        /**
         * ser loading indicator
         *
         * @param active if false will hide indicator, true will show indicator
         */
        void setLoadingIndicator(boolean active);

        /**
         * show weather data
         *
         * @param cityWeather
         */
        void showWeather(CityWeather cityWeather);

        /**
         * show error
         *
         * @param errorCode
         */
        void showLoadingWeatherError(int errorCode);

        /**
         * check if view is active
         *
         * @return
         */
        boolean isActive();
    }

    interface Presenter extends BasePresenter {

        /**
         * load weather
         *
         * @param city city for which weather data is required
         */
        void loadWeather(String city);

        /**
         * load weather data for current location
         */
        void loadWeatherForCurrentLocation();
    }
}
