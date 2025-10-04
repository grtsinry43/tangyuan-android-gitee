package com.qingshuige.tangyuan.viewmodels


/**
 * 用户资料
 */
data class UserProfile(
    /**
     * 用户唯一 ID
     */
    val userId: Int = 0,

    /**
     * 用户昵称
     */
    val nickname: String? = null,

    /**
     * 用户个性签名
     */
    val bio: String? = null
)
