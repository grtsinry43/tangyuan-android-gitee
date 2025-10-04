package com.qingshuige.tangyuan;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.*;
import com.qingshuige.tangyuan.network.ApiInterface;

import java.io.IOException;
import java.security.GeneralSecurityException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TangyuanApplication extends Application {

    private static Retrofit retrofit;
    private static ApiInterface api;
    private static final String coreDomain = "https://ty.qingshuige.ink/";
    private static SharedPreferences sharedPreferences;
    private static TokenManager tokenManager;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            sharedPreferences = EncryptedSharedPreferences
                    .create(
                            "Tangyuan",
                            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                            this,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tokenManager = new TokenManager();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 匹配 "2024-04-08T00:00:00"
                .create();
        Retrofit pureRetrofit = new Retrofit.Builder()
                .baseUrl(coreDomain + "api/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        ApiInterface pureApi = pureRetrofit.create(ApiInterface.class);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new JwtInterceptor(tokenManager))
                .authenticator(new JwtAuthenticator(tokenManager, pureApi))
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(coreDomain + "api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        api = retrofit.create(ApiInterface.class);

    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static ApiInterface getApi() {
        return api;
    }

    public static String getCoreDomain() {
        return coreDomain;
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static TokenManager getTokenManager() {
        return tokenManager;
    }
}
