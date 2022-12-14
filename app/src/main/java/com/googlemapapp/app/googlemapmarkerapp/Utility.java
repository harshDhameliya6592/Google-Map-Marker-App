package com.googlemapapp.app.googlemapmarkerapp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Utility {

    public static void PrintLog(String key, String value) {
        if (BuildConfig.DEBUG)
            if (value != null) {
                Log.e(key, value);

            } else {
                Log.e(key, "NULL VALUE");
            }
    }

    public static List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));

            poly.add(p);
        }
        return poly;
    }

    public static LatLng getNewLatLng(LatLng latLng) {

        LatLng newLatLng;

        if (latLng != null) {
            int precision = (int) Math.pow(10, 5);
            double new_Latitude = (double) ((int) (precision * latLng.latitude)) / precision;
            double new_Longitude = (double) ((int) (precision * latLng.longitude)) / precision;
            newLatLng = new LatLng(new_Latitude, new_Longitude);
            return newLatLng;

        } else {
            PrintLog("LanLng is ", "Null");
            return null;
        }

    }


}
