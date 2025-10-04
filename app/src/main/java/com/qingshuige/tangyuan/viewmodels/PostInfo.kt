package com.qingshuige.tangyuan.viewmodels

import java.util.Date

/**
 * 一篇帖子的所有信息。
 */
data class PostInfo(
    /**
     * 帖子唯一 ID
     */
    val postId: Int,
    val userId: Int,
    /**
     * 发帖用户昵称
     */
    val userNickname: String,
    val userAvatarGUID: String,
    /**
     * 发帖时间
     */
    val postDate: Date,
    /**
     * 正文内容
     */
    val textContent: String,
    val image1GUID: String?,
    val image2GUID: String?,
    val image3GUID: String?,
    val sectionId: Int,
    val categoryId: Int,
    val categoryName: String
)
