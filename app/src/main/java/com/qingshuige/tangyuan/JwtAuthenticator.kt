package com.qingshuige.tangyuan;

import com.qingshuige.tangyuan.network.ApiInterface;
import com.qingshuige.tangyuan.network.LoginDto;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class JwtAuthenticator implements Authenticator {
    private TokenManager tm;
    private ApiInterface api;

    public JwtAuthenticator(TokenManager tm, ApiInterface api) {
        this.tm = tm;
        this.api = api;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        LoginDto dto = new LoginDto();
        dto.phoneNumber = tm.getPhoneNumber();
        dto.password = tm.getPassword();
        String newToken = api.login(dto).execute().body().values().iterator().next();
        // 重试原始请求，添加新的 access token
        return response.request().newBuilder()
                .header("Authorization", "Bearer " + newToken)
                .header("X-Refresh-Attempt", "true") // 标记已尝试刷新
                .build();
    }
}
