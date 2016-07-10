package com.centralway.rxphoenix.sample;

import com.google.gson.JsonElement;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiInterface {

    /**
     * Calls a web service that sleeps for provided seconds before returning.
     */
    @GET("http://fake-response.appspot.com/")
    Observable<JsonElement> sleep(@Query("sleep") String sleep);

}
