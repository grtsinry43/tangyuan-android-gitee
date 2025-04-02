package com.qingshuige.tangyuan.network;

import android.graphics.Bitmap;

import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.viewmodels.CommentInfo;
import com.qingshuige.tangyuan.viewmodels.NotificationInfo;
import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
                if (response.code() == 404) {
                    callback.onComplete(null);
                    return;
                }
                PostMetadata metadata = response.body();
                api.getPostBody(postId).enqueue(new Callback<PostBody>() {
                    @Override
                    public void onResponse(Call<PostBody> call, Response<PostBody> response) {
                        PostBody body = response.body();
                        api.getUser(metadata.userId).enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                User user = response.body();
                                PostInfo info = new PostInfo(
                                        postId,
                                        user.userId,
                                        user.nickName,
                                        user.avatarGuid,
                                        metadata.postDateTime,
                                        body.textContent,
                                        body.image1UUID,
                                        body.image2UUID,
                                        body.image3UUID,
                                        metadata.sectionId);
                                callback.onComplete(info);
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable throwable) {
                                callback.onComplete(null);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<PostBody> call, Throwable throwable) {
                        callback.onComplete(null);
                    }
                });
            }

            @Override
            public void onFailure(Call<PostMetadata> call, Throwable throwable) {
                callback.onComplete(null);
            }
        });
    }

    public static void getCommentInfoByIdAsync(int commentId, ApiCallback<CommentInfo> callback) {
        api.getComment(commentId).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                Comment comment = response.body();
                api.getUser(comment.userId).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        User user = response.body();
                        api.getSubComment(commentId).enqueue(new Callback<List<Comment>>() {
                            @Override
                            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                                CommentInfo info = new CommentInfo(user.avatarGuid,
                                        user.nickName,
                                        comment.content,
                                        comment.commentDateTime,
                                        commentId,
                                        response.code() != 404,
                                        user.userId);
                                callback.onComplete(info);
                            }

                            @Override
                            public void onFailure(Call<List<Comment>> call, Throwable throwable) {

                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable throwable) {

                    }
                });
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable throwable) {

            }
        });
    }

    public static void getNotificationInfoAsync(Notification n, ApiCallback<NotificationInfo> callback) {
        api.getUser(n.sourceUserId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    User sourceUser = response.body();
                    api.getComment(n.sourceCommentId).enqueue(new Callback<Comment>() {
                        @Override
                        public void onResponse(Call<Comment> call, Response<Comment> response) {
                            if (response.code() == 200) {
                                Comment sourceComment = response.body();

                                NotificationInfo info = new NotificationInfo(
                                        n.notificationDateTime,
                                        n.notificationId,
                                        sourceComment.content,
                                        sourceUser.avatarGuid,
                                        sourceUser.nickName,
                                        n.targetCommentId,
                                        n.targetPostId
                                );
                                callback.onComplete(info);
                            }
                        }

                        @Override
                        public void onFailure(Call<Comment> call, Throwable throwable) {
                            callback.onComplete(null);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {
                callback.onComplete(null);
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
                callback.onComplete(null);
            }
        });
    }

    public static void updateBitmapAsync(Bitmap bitmap, ApiCallback callback) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
        MultipartBody.Part part =
                MultipartBody.Part.createFormData("file", "image.jpg", requestBody);
        TangyuanApplication.getApi().postImage(part).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                callback.onComplete(response.body().values().iterator().next());
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable throwable) {

            }
        });
    }

    public interface ApiCallback<T> {
        public void onComplete(T result);
    }
}
