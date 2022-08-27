package com.googlemapapp.app.googlemapmarkerapp;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("maps/api/directions/json")
    Single<com.googlemapapp.app.googlemapmarkerapp.Result> getDirection(@Query("mode") String mode,
                                @Query("transit_routing_preference") String preferance,
                                @Query("origin") String origin,
                                @Query("destination") String destinaiton,
                                @Query("key") String key);

}
