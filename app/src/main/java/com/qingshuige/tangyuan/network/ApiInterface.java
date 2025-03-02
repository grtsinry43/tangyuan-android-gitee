package com.qingshuige.tangyuan.network;

import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

//用于Retrofit
public interface ApiInterface {

    @GET("post/metadata/{id}")
    Call<PostMetadata> getPostMetadata(@Path("id") int id);

    @GET("post/body/{id}")
    Call<PostBody> getPostBody(@Path("id")int id);

    @GET("user/{id}")
    Call<User> getUser(@Path("id")int id);

    @GET("post/metadata/random/{count}")
    Call<List<PostMetadata>> getRandomPostMetadata(@Path("count")int count);

    @POST("post/metadata")
    Call<Map<String,Integer>> postPostMetadata(CreatPostMetadataDto metadata);

}
