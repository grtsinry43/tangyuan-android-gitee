package com.qingshuige.tangyuan.viewmodels

import com.qingshuige.tangyuan.network.NewNotification

data class NotificationInfo(
    val notification: NewNotification,
    val title: String,
    val type: String,
    val message: String,
    val avatarGuid: String,
    val relatedPostId: Int,
    val relatedUserId: Int,
    val typeColor: Int
)
