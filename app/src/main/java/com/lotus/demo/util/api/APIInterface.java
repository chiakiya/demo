package com.lotus.demo.util.api;

import com.lotus.demo.model.Client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("/api/client/clientID")
    Call<Client> getClientID(@Query("socketID") String socketID);
}
