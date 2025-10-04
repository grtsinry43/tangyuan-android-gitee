package com.qingshuige.tangyuan.network;


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

    @Deprecated
    @GET("post/metadata/random/{count}")
    Call<List<PostMetadata>> getRandomPostMetadata(@Path("count") int count);

    @POST("philotaxis/postmetadata/{sectionId}")
    Call<List<PostMetadata>> phtPostMetadata(@Path("sectionId") int sectionId, @Body List<Integer> exceptedIds);

    @POST("post/metadata")
    Call<Map<String, Integer>> postPostMetadata(@Body CreatPostMetadataDto metadata);

    @POST("post/body")
    Call<ResponseBody> postPostBody(@Body PostBody body);

    @GET("post/metadata/notice")
    Call<PostMetadata> getNotice();

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

    @DELETE("comment/{id}")
    Call<ResponseBody> deleteComment(@Path("id") int commentId);

    @Multipart
    @POST("image/uploadjpg")
    Call<Map<String, String>> postImage(@Part MultipartBody.Part file);

    @POST("auth/login")
    Call<Map<String, String>> login(@Body LoginDto loginDto);

    @Deprecated
    @GET("notification/user/{userId}")
    Call<List<Notification>> getAllUnreadNotificationsOf(@Path("userId") int userId);

    @Deprecated
    @GET("notification/mark/{notificationId}")
    Call<ResponseBody> markNotificationAsRead(@Path("notificationId") int notificationId);

    @GET("newnotification/{userId}")
    Call<List<NewNotification>> getAllNotificationsByUserId(@Path("userId") int userId);

    @GET("newnotification/markasread/{id}")
    Call<ResponseBody> markNewNotificationAsRead(@Path("id") int id);

    @POST("comment")
    Call<Map<String, String>> postComment(@Body CreateCommentDto dto);

    @GET("category/{id}")
    Call<Category> getCategory(@Path("id") int id);

    @GET("category/all")
    Call<List<Category>> getAllCategories();

    @GET("category/count/{id}")
    Call<Integer> getPostCountOfCategory(@Path("id") int id);

    @GET("category/weeklynewcount/{id}")
    Call<Integer> getWeeklyNewPostCountOfCategory(@Path("id") int id);

    @GET("post/metadata/category/{categoryId}")
    Call<List<PostMetadata>> getAllMetadatasByCategoryId(@Path("categoryId") int categoryId);

    @GET("post/count/category/24h/{categoryId}")
    Call<Integer> get24hNewPostCountByCategoryId(@Path("categoryId") int categoryId);

    @GET("post/count/category/7d/{categoryId}")
    Call<Integer> get7dNewPostCountByCategoryId(@Path("categoryId") int categoryId);

    @GET("search/post/{keyword}")
    Call<List<PostMetadata>> searchPostByKeyword(@Path("keyword") String keyword);

    @GET("search/user/{keyword}")
    Call<List<User>> searchUserByKeyword(@Path("keyword") String keyword);

    @GET("search/comment/{keyword}")
    Call<List<Comment>> searchCommentByKeyword(@Path("keyword") String keyword);

    /////以下为非后端方法

    @GET
    Call<ResponseBody> getFromUrl(@Url String Url);

    @FormUrlEncoded
    @POST("https://api.pgyer.com/apiv2/app/check")
    Call<JsonObject> checkUpdate(@FieldMap Map<String, String> params);

}
