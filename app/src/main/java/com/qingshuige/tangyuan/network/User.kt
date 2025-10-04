package com.qingshuige.tangyuan.network

data class User(
    var userId: Int = 0,
    var nickName: String = "",
    var phoneNumber: String = "",
    var isoRegionName: String = "",
    var email: String = "",
    var bio: String = "",
    var avatarGuid: String = "",
    var password: String = ""
)