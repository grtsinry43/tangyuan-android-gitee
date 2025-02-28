package com.qingshuige.tangyuan.network;

import com.qingshuige.tangyuan.viewmodels.PostInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

//用于Retrofit
public interface ApiInterface {

    @GET("post/metadata/{id}")
    Call<PostMetadata> getPostMetadata(@Path("id") int id);

    @GET("post/body/{id}")
    Call<PostBody> getPostBody(@Path("id")int id);

    @GET("user/{id}")
    Call<User> getUser(@Path("id")int id);

}
