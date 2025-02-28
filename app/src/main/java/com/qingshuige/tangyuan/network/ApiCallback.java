package com.qingshuige.tangyuan.network;

public interface ApiCallback<T> {
    void onComplete(ApiMapper.ApiResponse<T> resp);
}
