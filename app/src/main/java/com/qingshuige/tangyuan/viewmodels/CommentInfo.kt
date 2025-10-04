package com.qingshuige.tangyuan.viewmodels

import com.qingshuige.tangyuan.network.Comment
import java.util.Date

data class CommentInfo(
    val comment: Comment?,
    val userAvatarGuid: String,
    val userNickname: String,
    val commentText: String,
    val commentDateTime: Date,
    val commentId: Int,
    val isHasReplies: Boolean,
    val userId: Int
)
