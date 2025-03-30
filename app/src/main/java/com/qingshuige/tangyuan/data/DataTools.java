package com.qingshuige.tangyuan.data;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qingshuige.tangyuan.R;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Pattern;

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

    public static boolean isValidPhoneNumber(String phoneNumber, String isoRegionName) {
        String REGEX;
        switch (isoRegionName) {
            case "CN":
                REGEX = "^1[3-9]\\d{9}$";
                break;
            default:
                return false;
        }

        return Pattern.compile(REGEX).matcher(phoneNumber).matches();
    }

    public static String getLocalFriendlyDateTime(Date utcDate, Context context) {
        ZonedDateTime zdt = utcDate.toInstant().atZone(ZoneId.of("UTC"));
        DateTimeFormatter noYearFormatter = DateTimeFormatter.ofPattern("M-d HH:mm");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

        ZonedDateTime localdt = zdt.withZoneSameInstant(ZoneId.systemDefault());
        ZonedDateTime currentLocalDt = ZonedDateTime.now();
        Duration duration = Duration.between(localdt, currentLocalDt);

        long minutes = duration.toMinutes();
        //刚刚=[0,1]
        if (minutes <= 1) {
            return context.getString(R.string.just_now);
        }
        //n分钟前=(1,60)
        if (minutes < 60) {
            return minutes + context.getString(R.string.minutes_before);
        }
        //n小时前=[60,180]
        if (minutes <= 180) {
            return duration.toHours() + context.getString(R.string.hours_before);
        }
        //日期
        if (localdt.getYear() == currentLocalDt.getYear()) {
            return localdt.format(noYearFormatter);
        } else {
            return localdt.format(yearFormatter);
        }
    }
}
