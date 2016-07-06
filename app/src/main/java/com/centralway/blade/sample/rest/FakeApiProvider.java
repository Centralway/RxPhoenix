package com.centralway.blade.sample.rest;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Network API Provider that only knows about slow web services.
 */
public final class FakeApiProvider {

    /**
     * Returns an interface to a slow web service
     */
    public static FakeApiInterface getFakeApiInterface() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://fake-response.appspot.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(FakeApiInterface.class);
    }

}

