package com.flanders.API;


import com.flanders.Model.rotas;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Rotas {

    @FormUrlEncoded
    @POST("/rotas")
    Call<ResponseBody> sendRoutes(
            @Field("latitude") Double latitude,
            @Field("longitude") Double longitude,
            @Field("time") String time,
            @Field("speed") int speed,
            @Field("rvc_name") String rvc_name);


    @GET("/rotas")
    Call<rotas> getModel();
}
