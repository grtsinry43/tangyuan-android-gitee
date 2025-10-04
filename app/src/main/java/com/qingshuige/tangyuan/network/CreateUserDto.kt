package com.qingshuige.tangyuan.network

data class CreateUserDto(
    var avatarGuid: String? = null,
    var isoRegionName: String? = null,
    var nickName: String? = null,
    var password: String? = null,
    var phoneNumber: String? = null
)
