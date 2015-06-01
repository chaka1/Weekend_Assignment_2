package com.example.chaka.weekendassignment2.interfaces;

import com.example.chaka.weekendassignment2.models.Result;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Chaka on 29/05/2015.
 */
public interface JustEatApiService {

    @Headers({
            "Accept-Tenant: uk",
            "Accept-Language: en-GB",
            "Authorization: Basic VGVjaFRlc3RBUEk6dXNlcjI=",
            "Host: api-interview.just-eat.com"
    })
    @GET("/restaurants")
    void getResult(@Query("q") String location, Callback<Result> cb);
}
