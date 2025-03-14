package com.qingshuige.tangyuan;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class JwtInterceptor implements Interceptor {

    private TokenManager tm;

    public JwtInterceptor(TokenManager tokenManager) {
        tm = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String token = tm.getToken();
        if (token != null) {
            Request modifiedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(modifiedRequest);
        }
        return chain.proceed(originalRequest);
    }
}
