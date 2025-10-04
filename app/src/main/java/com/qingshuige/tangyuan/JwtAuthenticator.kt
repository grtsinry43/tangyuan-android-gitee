package com.qingshuige.tangyuan

import com.qingshuige.tangyuan.network.ApiInterface
import com.qingshuige.tangyuan.network.LoginDto
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

class JwtAuthenticator(private val tm: TokenManager, private val api: ApiInterface) :
    Authenticator {
    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        val dto = LoginDto()
        dto.phoneNumber = tm.phoneNumber
        dto.password = tm.password
        val newToken = api.login(dto).execute().body()!!.values.iterator().next()
        // 重试原始请求，添加新的 access token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .header("X-Refresh-Attempt", "true") // 标记已尝试刷新
            .build()
    }
}
