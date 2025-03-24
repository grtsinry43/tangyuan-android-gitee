package com.qingshuige.tangyuan.data;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Base64;

public class DataTools {

    public static int decodeJwtTokenUserId(String token) {
        // 分割 JWT，获取 payload（第二部分）
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        // 解码 Base64 URL 编码的 payload
        String payload = parts[1];
        String decodedPayload = new String(Base64.getUrlDecoder().decode(payload));
        JsonObject jsonObject = JsonParser.parseString(decodedPayload).getAsJsonObject();
        return jsonObject.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name").getAsInt();
    }
}
