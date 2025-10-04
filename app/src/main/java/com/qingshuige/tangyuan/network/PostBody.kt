package com.qingshuige.tangyuan.network

data class PostBody(
    var postId: Int = 0,
    var textContent: String? = null,
    var image1UUID: String? = null,
    var image2UUID: String? = null,
    var image3UUID: String? = null
)
