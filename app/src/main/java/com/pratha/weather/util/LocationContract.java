package com.pratha.weather.util;

/**
 * contact to get location and perform actions
 * <p/>
 * Created by Pratha on 8/27/2016.
 */
public interface LocationContract {

    interface WeatherLocationProvider {

        /**
         * will provide current location in callback
         *
         * @param locationProviderCallback callback to provide result
         */
        void getCurrentLocation(LocationProviderCallback locationProviderCallback);
    }

    interface LocationProviderCallback {
        /**
         * return location result
         *
         * @param latitude
         * @param longitude
         */
        void onGetLocation(String latitude, String longitude);

        /**
         * return error while getting location
         *
         * @param errorCode errorCodes from LocationContract
         */
        void onLocationError(int errorCode);
    }
}
