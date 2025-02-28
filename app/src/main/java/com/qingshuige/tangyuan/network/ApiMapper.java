package com.qingshuige.tangyuan.network;


import com.qingshuige.tangyuan.models.*;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * WebApi的抽象。
 */
public class ApiMapper {

    public class ApiResponse<T> {

        private String statusCode;

        private T data;

        private ApiResponse(){}

        /**
         * 根据HTML返回序列化一个ApiResponse。
         */
        private ApiResponse(String context)
        {
            //序列化各项数据
        }

        public String getStatusCode() {
            return statusCode;
        }

        public T getData() {
            return data;
        }
    }

    private final static String domain="ty.qingshuige.ink";

    public static void getPostMetadataByIDAsync(int id, ApiCallback<PostMetadata> callback) {

    }
}
