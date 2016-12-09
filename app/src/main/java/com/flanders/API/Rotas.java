package com.flanders.API;


import com.google.gson.JsonObject;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface Rotas {

    @POST("/rotas/")
    void postRoute(
            @Body JsonObject bean, Callback<Response> callback);


}
