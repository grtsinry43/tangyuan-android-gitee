package com.qingshuige.tangyuan;

import android.app.Application;

import com.qingshuige.tangyuan.network.ApiInterface;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TangyuanApplication extends Application {

    private static Retrofit retrofit;
    private static ApiInterface api;

    @Override
    public void onCreate(){
        super.onCreate();

        retrofit=new Retrofit.Builder()
                .baseUrl("https://ty.qingshuige.ink/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api=retrofit.create(ApiInterface.class);
    }

    public static Retrofit getRetrofit(){
        return retrofit;
    }

    public static ApiInterface getApi() {
        return api;
    }

}
