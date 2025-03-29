package com.qingshuige.tangyuan.network;

import com.qingshuige.tangyuan.viewmodels.PostInfo;


import com.google.gson.*;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Url;

//用于Retrofit
public interface ApiInterface {

    @GET("post/metadata/{id}")
    Call<PostMetadata> getPostMetadata(@Path("id") int id);

    @GET("post/body/{id}")
    Call<PostBody> getPostBody(@Path("id") int id);

    @GET("user/{id}")
    Call<User> getUser(@Path("id") int id);

    @GET("post/metadata/user/{id}")
    Call<List<PostMetadata>> getMetadatasByUserID(@Path("id") int userId);

    @GET("post/metadata/random/{count}")
    Call<List<PostMetadata>> getRandomPostMetadata(@Path("count") int count);

    @POST("post/metadata")
    Call<Map<String, Integer>> postPostMetadata(@Body CreatPostMetadataDto metadata);

    @POST("post/body")
    Call<ResponseBody> postPostBody(@Body PostBody body);

    @POST("user")
    Call<ResponseBody> postUser(@Body CreateUserDto user);

    @DELETE("post/{id}")
    Call<ResponseBody> deletePost(@Path("id") int postId);

    @PUT("user/{id}")
    Call<ResponseBody> putUser(@Path("id") int id, @Body User userInfo);

    @GET("comment/post/{postId}")
    Call<List<Comment>> getCommentForPost(@Path("postId") int postId);

    @GET("comment/{id}")
    Call<Comment> getComment(@Path("id") int id);

    @GET("comment/sub/{parentCommentId}")
    Call<List<Comment>> getSubComment(@Path("parentCommentId") int parentCommentId);

    @Multipart
    @POST("image/uploadjpg")
    Call<Map<String, String>> postImage(@Part MultipartBody.Part file);

    @POST("auth/login")
    Call<Map<String, String>> login(@Body LoginDto loginDto);

    @POST("comment")
    Call<Map<String, String>> postComment(@Body CreateCommentDto dto);

    @GET
    Call<ResponseBody> getFromUrl(@Url String Url);

    @FormUrlEncoded
    @POST("https://api.pgyer.com/apiv2/app/check")
    Call<JsonObject> checkUpdate(@FieldMap Map<String, String> params);

}
