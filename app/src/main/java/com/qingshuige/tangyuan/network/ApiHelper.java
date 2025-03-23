package com.qingshuige.tangyuan.network;

import android.util.Log;

import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.util.Map;

import retrofit2.*;

/**
 * 对一些常用但复杂的Api调用进行封装，方便使用。
 */
public class ApiHelper {

    private static final ApiInterface api = TangyuanApplication.getApi();

    /**
     * 根据给定PostID，返回PostInfo，然后呼叫回调并传送PostInfo对象。此方法可以立即返回。
     *
     * @param postId   需要获取的PostID。
     * @param callback 取得PostID后呼叫的回调，接受获取的PostInfo对象。
     */
    public static void getPostInfoByIdAsync(int postId, ApiCallback<PostInfo> callback) {
        api.getPostMetadata(postId).enqueue(new Callback<PostMetadata>() {
            @Override
            public void onResponse(Call<PostMetadata> call, Response<PostMetadata> response) {
                PostMetadata metadata = response.body();
                api.getPostBody(postId).enqueue(new Callback<PostBody>() {
                    @Override
                    public void onResponse(Call<PostBody> call, Response<PostBody> response) {
                        PostBody body = response.body();
                        Log.i("TY", String.valueOf(metadata.postId));
                        Log.i("TY", body.textContent);
                        api.getUser(metadata.userId).enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                User user = response.body();
                                PostInfo info = new PostInfo(
                                        postId,
                                        user.nickName,
                                        user.avatarGuid,
                                        metadata.postDateTime,
                                        body.textContent,
                                        body.image1UUID,
                                        body.image2UUID,
                                        body.image3UUID);
                                callback.onComplete(info);
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable throwable) {
                                //TODO
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<PostBody> call, Throwable throwable) {
                        //TODO
                    }
                });
            }

            @Override
            public void onFailure(Call<PostMetadata> call, Throwable throwable) {
                //TODO
            }
        });
    }

    public static String getFullImageURL(String imageGuid) {
        return TangyuanApplication.getCoreDomain() + "images/" + imageGuid + ".jpg";
    }

    public static void judgeIfUserExistsAsync(String phoneNumber, ApiCallback<Boolean> callback) {
        LoginDto dto = new LoginDto();
        dto.phoneNumber = phoneNumber;
        dto.password = "d25402a3-83b3-4e7f-a17e-2fa6dbda3d92";//谁设这个密码也是天才级别的
        api.login(dto).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                switch (response.code()) {
                    case 400:
                        callback.onComplete(true);
                        break;
                    case 404:
                        callback.onComplete(false);
                        break;
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable throwable) {
                //TODO
            }
        });
    }

    public interface ApiCallback<T> {
        public void onComplete(T result);
    }
}
