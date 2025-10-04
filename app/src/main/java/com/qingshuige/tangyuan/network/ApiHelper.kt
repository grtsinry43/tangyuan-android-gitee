package com.qingshuige.tangyuan.network

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.viewmodels.CommentInfo
import com.qingshuige.tangyuan.viewmodels.NotificationInfo
import com.qingshuige.tangyuan.viewmodels.PostInfo
import com.qingshuige.tangyuan.viewmodels.UserInfo
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.Date

/**
 * 对一些常用但复杂的 Api 调用进行封装，方便使用。
 */
object ApiHelper {

    private val api: ApiInterface = TangyuanApplication.getApi()
    private const val THREAD_COUNT = 10
    private const val TAG = "TYAPP"

    /**
     * 根据给定 PostID，返回 PostInfo，然后呼叫回调并传送 PostInfo 对象。此方法可以立即返回。
     */
    fun getPostInfoByIdAsync(postId: Int, callback: ApiCallback<PostInfo>) {
        CoroutineScope(Dispatchers.IO).launch {
            val info = getPostInfoById(postId)
            withContext(Dispatchers.Main) {
                callback.onComplete(info)
            }
        }
    }

    fun getPostInfoById(postId: Int): PostInfo? {
        return try {
            val metadata = api.getPostMetadata(postId).execute().body() ?: return null
            val body = api.getPostBody(postId).execute().body() ?: return null
            val user = api.getUser(metadata.userId).execute().body() ?: return null
            val category = api.getCategory(metadata.categoryId).execute().body() ?: return null

            PostInfo(
                postId = postId,
                userId = user.userId,
                userNickname = user.nickName,
                userAvatarGUID = user.avatarGuid,
                postDate = metadata.postDateTime ?: Date(),
                textContent = body.textContent ?: "",
                image1GUID = body.image1UUID,
                image2GUID = body.image2UUID,
                image3GUID = body.image3UUID,
                sectionId = metadata.sectionId,
                categoryId = category.categoryId,
                categoryName = category.baseName ?: ""
            )
        } catch (e: Exception) {
            Log.w(TAG, "getPostInfoErr: ${e.message}")
            null
        }
    }

    fun <S, I> getInfoFastAsync(
        source: List<S>,
        constructor: InfoConstructable<S, I>,
        callback: ApiCallback<List<I>>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = ConcurrentHashMap<Int, I>()
                val jobs = mutableListOf<Job>()

                // 使用协程替代线程池，更高效的并发处理
                source.chunked((source.size + THREAD_COUNT - 1) / THREAD_COUNT)
                    .forEachIndexed { chunkIndex, chunk ->
                        val job = launch {
                            chunk.forEachIndexed { itemIndex, item ->
                                try {
                                    val info = constructor.getInfo(item)
                                    if (info != null) {
                                        results[chunkIndex * chunk.size + itemIndex] = info
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error processing item: ${e.message}")
                                }
                            }
                        }
                        jobs.add(job)
                    }

                // 等待所有任务完成
                jobs.joinAll()

                // 按原始顺序返回结果
                val finalList = results.toSortedMap().values.toList()

                withContext(Dispatchers.Main) {
                    callback.onComplete(finalList)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getInfoFastAsync error: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback.onComplete(emptyList())
                }
            }
        }
    }

    fun getCommentInfoByIdAsync(commentId: Int, callback: ApiCallback<CommentInfo>) {
        api.getComment(commentId).enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                val comment = response.body() ?: return

                api.getUser(comment.userId).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        val user = response.body() ?: return

                        api.getSubComment(commentId).enqueue(object : Callback<List<Comment>> {
                            override fun onResponse(
                                call: Call<List<Comment>>,
                                response: Response<List<Comment>>
                            ) {
                                val info = CommentInfo(
                                    comment = comment,
                                    userAvatarGuid = user.avatarGuid,
                                    userNickname = user.nickName,
                                    commentText = comment.content ?: "",
                                    commentDateTime = comment.commentDateTime ?: Date(),
                                    commentId = commentId,
                                    isHasReplies = response.code() != 404,
                                    userId = user.userId
                                )
                                callback.onComplete(info)
                            }

                            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                                Log.e(TAG, "Failed to get sub comments: ${t.message}")
                            }
                        })
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.e(TAG, "Failed to get user: ${t.message}")
                    }
                })
            }

            override fun onFailure(call: Call<Comment>, t: Throwable) {
                Log.e(TAG, "Failed to get comment: ${t.message}")
            }
        })
    }

    fun getFullImageURL(imageGuid: String): String {
        return "${TangyuanApplication.coreDomain}images/$imageGuid.jpg"
    }

    fun judgeIfUserExistsAsync(phoneNumber: String, callback: ApiCallback<Boolean>) {
        val dto = LoginDto(
            phoneNumber = phoneNumber,
            password = "d25402a3-83b3-4e7f-a17e-2fa6dbda3d92" // TODO: 移除硬编码密码
        )

        api.login(dto).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                when (response.code()) {
                    400 -> callback.onComplete(true)
                    404 -> callback.onComplete(false)
                    else -> callback.onComplete(null)
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e(TAG, "Failed to check user existence: ${t.message}")
                callback.onComplete(null)
            }
        })
    }

    fun updateBitmapAsync(bitmap: Bitmap, callback: ApiCallback<String>) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val bytes = stream.toByteArray()
        val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
        val part = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

        api.postImage(part).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                val result = response.body()?.values?.firstOrNull()
                callback.onComplete(result)
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e(TAG, "Failed to upload image: ${t.message}")
                callback.onComplete(null)
            }
        })
    }

    fun interface InfoConstructable<S, I> {
        fun getInfo(source: S): I?
    }

    fun interface ApiCallback<T> {
        fun onComplete(result: T?)
    }

    class PostInfoConstructor : InfoConstructable<PostMetadata, PostInfo> {
        override fun getInfo(source: PostMetadata): PostInfo? {
            return try {
                val metadata = api.getPostMetadata(source.postId).execute().body() ?: return null
                val body = api.getPostBody(source.postId).execute().body() ?: return null
                val user = api.getUser(metadata.userId).execute().body() ?: return null
                val category = api.getCategory(metadata.categoryId).execute().body() ?: return null

                PostInfo(
                    postId = source.postId,
                    userId = user.userId,
                    userNickname = user.nickName,
                    userAvatarGUID = user.avatarGuid,
                    postDate = metadata.postDateTime ?: Date(),
                    textContent = body.textContent ?: "",
                    image1GUID = body.image1UUID,
                    image2GUID = body.image2UUID,
                    image3GUID = body.image3UUID,
                    sectionId = metadata.sectionId,
                    categoryId = category.categoryId,
                    categoryName = category.baseName ?: ""
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error constructing PostInfo: ${e.message}")
                null
            }
        }
    }

    class UserInfoConstructor : InfoConstructable<User, UserInfo> {
        override fun getInfo(source: User): UserInfo {
            // TODO: 实现关注功能判断逻辑
            return UserInfo(source, false)
        }
    }

    class CommentInfoConstructor : InfoConstructable<Comment, CommentInfo> {
        override fun getInfo(source: Comment): CommentInfo? {
            return try {
                val user = api.getUser(source.userId).execute().body() ?: return null
                val hasReply = api.getSubComment(source.commentId).execute().code() != 404

                CommentInfo(
                    comment = source,
                    userAvatarGuid = user.avatarGuid,
                    userNickname = user.nickName,
                    commentText = source.content ?: "",
                    commentDateTime = source.commentDateTime ?: Date(),
                    commentId = source.commentId,
                    isHasReplies = hasReply,
                    userId = user.userId
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error constructing CommentInfo: ${e.message}")
                null
            }
        }
    }

    class NotificationInfoConstructor(private val context: Context) :
        InfoConstructable<NewNotification, NotificationInfo> {
        override fun getInfo(n: NewNotification): NotificationInfo? {
            return try {
                when (n.type) {
                    "comment" -> {
                        val comment = api.getComment(n.sourceId).execute().body() ?: return null
                        val user = api.getUser(comment.userId).execute().body() ?: return null

                        NotificationInfo(
                            notification = n,
                            title = user.nickName,
                            type = context.getString(R.string.comment),
                            message = comment.content ?: "",
                            avatarGuid = user.avatarGuid,
                            relatedPostId = comment.postId,
                            relatedUserId = user.userId,
                            typeColor = context.getColor(R.color.mazarine_blue)
                        )
                    }

                    "reply" -> {
                        val comment = api.getComment(n.sourceId).execute().body() ?: return null
                        val user = api.getUser(comment.userId).execute().body() ?: return null

                        NotificationInfo(
                            notification = n,
                            title = user.nickName,
                            type = context.getString(R.string.reply),
                            message = comment.content ?: "",
                            avatarGuid = user.avatarGuid,
                            relatedPostId = comment.postId,
                            relatedUserId = user.userId,
                            typeColor = context.getColor(R.color.nanohanacha_gold)
                        )
                    }

                    "mention", "notice" -> null // TODO: 实现这些类型的处理
                    else -> null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error constructing NotificationInfo: ${e.message}")
                null
            }
        }
    }
}