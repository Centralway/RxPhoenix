package com.centralway.blade.sample.rest;

import com.google.gson.JsonElement;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface FakeApiInterface {

    /**
     * Calls a web service that sleeps for at least 3 seconds before returning.
     */
    @GET("http://fake-response.appspot.com/")
    Observable<JsonElement> sleep(@Query("sleep") String sleep);

}
