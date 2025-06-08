package com.qingshuige.tangyuan.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.viewmodels.CommentInfo;
import com.qingshuige.tangyuan.viewmodels.NotificationInfo;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.qingshuige.tangyuan.viewmodels.UserInfo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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
        new Thread(() -> {
            PostInfo info = getPostInfoById(postId);
            callback.onComplete(info);
        }).start();
    }

    public static PostInfo getPostInfoById(int postId) {
        try {
            PostMetadata metadata = api.getPostMetadata(postId).execute().body();
            PostBody body = api.getPostBody(postId).execute().body();
            User user = api.getUser(metadata.userId).execute().body();
            Category category = api.getCategory(metadata.categoryId).execute().body();

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
                    metadata.sectionId,
                    category.categoryId,
                    category.baseName);

            return info;
        } catch (Exception e) {
            Log.w("TYAPP", "getPostInfoErr: " + e.getMessage());
            return null;
        }
    }

    //TODO: 尽快迁移到通用方法
    public static <S, I> void getInfoFastAsync(List<S> source, InfoConstructable<S, I> constructor, ApiCallback<List<I>> callback) {
        new Thread(() -> {
            int threadCount = 10;//默认10线程
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            List<I> finalList = Collections.synchronizedList(new ArrayList<>());

            List<List<S>> lists = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                lists.add(new ArrayList<>());
            }

            //分派任务
            for (int i = 0; i < source.size(); i++) {
                lists.get(i % threadCount).add(source.get(i));
            }

            AtomicBoolean hasException = new AtomicBoolean(false);

            //开始执行每个列表的异步任务
            for (List<S> l : lists) {
                executor.submit(() -> {
                    try {
                        for (S n : l) {
                            I info = constructor.getInfo(n); //注意到这个方法是同步的
                            finalList.add(info);
                        }
                    } catch (Exception e) {
                        hasException.set(true);
                        latch.countDown();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                hasException.set(true);
            }

            //去除因错误产生的null
            finalList.removeIf(Objects::isNull);

            //判断是否出错，如果出错就报错
            if (hasException.get()) {
                callback.onComplete(null);
            } else {
                callback.onComplete(finalList);
            }
        }).start();
    }

    public static void getPostInfoByMetadataFastAsync(List<PostMetadata> metadata, ApiCallback<List<PostInfo>> callback) {
        new Thread(() -> {
            int threadCount = 10;//默认10线程
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            List<PostInfo> finalList = Collections.synchronizedList(new ArrayList<>());

            List<List<PostMetadata>> lists = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                lists.add(new ArrayList<>());
            }

            //分派任务
            for (int i = 0; i < metadata.size(); i++) {
                lists.get(i % threadCount).add(metadata.get(i));
            }

            AtomicBoolean hasException = new AtomicBoolean(false);

            //开始执行每个列表的异步任务
            for (List<PostMetadata> l : lists) {
                executor.submit(() -> {
                    try {
                        for (PostMetadata n : l) {
                            PostInfo info = getPostInfoById(n.postId); //注意到这个方法是同步的
                            finalList.add(info);
                        }
                    } catch (Exception e) {
                        hasException.set(true);
                        latch.countDown();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                hasException.set(true);
            }

            //判断是否出错，如果出错就报错
            if (hasException.get()) {
                callback.onComplete(null);
            } else {
                callback.onComplete(finalList);
            }
        }).start();
    }

    public static void getNotificationInfoFastAsync(List<NewNotification> notifications, Context context, ApiCallback<List<NotificationInfo>> callback) {
        new Thread(() -> {
            int threadCount = 5;//默认5线程
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            List<NotificationInfo> finalList = Collections.synchronizedList(new ArrayList<>());

            List<List<NewNotification>> lists = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                lists.add(new ArrayList<>());
            }

            //分派任务
            for (int i = 0; i < notifications.size(); i++) {
                lists.get(i % threadCount).add(notifications.get(i));
            }

            AtomicBoolean hasException = new AtomicBoolean(false);

            //开始执行每个列表的异步任务
            for (List<NewNotification> l : lists) {
                executor.submit(() -> {
                    try {
                        for (NewNotification n : l) {
                            NotificationInfo info = constructNotificationInfo(n, context); //注意到这个方法是同步的
                            finalList.add(info);
                        }
                    } catch (Exception e) {
                        hasException.set(true);
                        latch.countDown();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                hasException.set(true);
            }

            //判断是否出错，如果出错就报错
            if (hasException.get()) {
                callback.onComplete(null);
            } else {
                callback.onComplete(finalList);
            }
        }).start();
    }

    private static NotificationInfo constructNotificationInfo(NewNotification n, Context context) {
        try {
            switch (n.type) {
                case "comment":
                    //断言sourceType是comment，所以直接调用获取评论的API
                    Comment c = TangyuanApplication.getApi().getComment(n.sourceId).execute().body();
                    User u = TangyuanApplication.getApi().getUser(c.userId).execute().body();
                    return new NotificationInfo(n,
                            u.nickName,
                            context.getString(R.string.comment),
                            c.content,
                            u.avatarGuid,
                            c.postId,
                            u.userId,
                            context.getColor(R.color.mazarine_blue));
                case "reply":
                    //断言sourceType是comment，所以直接调用获取评论的API
                    Comment cc = TangyuanApplication.getApi().getComment(n.sourceId).execute().body();
                    User uu = TangyuanApplication.getApi().getUser(cc.userId).execute().body();
                    return new NotificationInfo(n,
                            uu.nickName,
                            context.getString(R.string.reply),
                            cc.content,
                            uu.avatarGuid,
                            cc.postId,
                            uu.userId,
                            context.getColor(R.color.nanohanacha_gold));
                case "mention":
                    return null;
                case "notice":
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
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
                                CommentInfo info = new CommentInfo(comment,
                                        user.avatarGuid,
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

    public interface InfoConstructable<S, I> {
        I getInfo(S source);
    }

    public interface ApiCallback<T> {
        void onComplete(T result);
    }

    public static class UserInfoConstructor implements InfoConstructable<User, UserInfo> {

        @Override
        public UserInfo getInfo(User source) {
            if (TangyuanApplication.getTokenManager().getToken() != null) {
                //已登录
                //TODO: 目前没有关注功能，也就无从判断是否关注
                return new UserInfo(source, false);
            } else {
                return new UserInfo(source, false);
            }
        }
    }

    public static class CommentInfoConstructor implements InfoConstructable<Comment, CommentInfo> {

        @Override
        public CommentInfo getInfo(Comment source) {
            try {
                User user = TangyuanApplication.getApi().getUser(source.userId).execute().body();
                boolean hasReply = TangyuanApplication.getApi().getSubComment(source.commentId).execute().code() != 404;
                return new CommentInfo(source,
                        user.avatarGuid,
                        user.nickName,
                        source.content,
                        source.commentDateTime,
                        source.commentId,
                        hasReply,
                        user.userId);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
