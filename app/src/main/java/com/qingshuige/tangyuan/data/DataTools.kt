package com.qingshuige.tangyuan.data;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qingshuige.tangyuan.R;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
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

    public static Date getCurrentUtcTime() {
        // 获取当前UTC时间的Instant对象
        Instant instant = Instant.now();

        // 将Instant对象转换为ZonedDateTime对象（使用UTC时区）
        ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);

        // 将ZonedDateTime对象转换为Date对象
        return Date.from(zonedDateTime.toInstant());
    }


    public static String getLocalFriendlyDateTime(Date utcDate, Context context) {
        String utcDateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(utcDate);
        ZonedDateTime utcDateTime = ZonedDateTime.parse(utcDateString);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:MM");
        DateTimeFormatter noYearFormatter = DateTimeFormatter.ofPattern("M-d HH:mm");
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");

        ZonedDateTime localdt = utcDateTime.withZoneSameInstant(ZoneId.systemDefault());
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
        //今天
        if (localdt.toLocalDate().isEqual(currentLocalDt.toLocalDate())) {
            return context.getString(R.string.today) + " " + localdt.format(timeFormatter);
        }
        //昨天
        if (localdt.toLocalDate().isEqual(currentLocalDt.toLocalDate().minusDays(1))) {
            return context.getString(R.string.yesterday) + " " + localdt.format(timeFormatter);
        }
        //前天
        if (localdt.toLocalDate().isEqual(currentLocalDt.toLocalDate().minusDays(2))) {
            return context.getString(R.string.the_day_before_yesterday) + " " + localdt.format(timeFormatter);
        }
        //日期（不带年）
        if (localdt.getYear() == currentLocalDt.getYear()) {
            return localdt.format(noYearFormatter);
        } else { //日期（带年）
            return localdt.format(yearFormatter);
        }
    }

    public static String deleteBlankLines(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        StringBuilder result = new StringBuilder();
        int newlineCount = 0;

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\n') {
                newlineCount++;
                if (newlineCount <= 2) {
                    result.append(c);
                }
            } else {
                if (newlineCount > 0) {
                    newlineCount = 0;
                }
                result.append(c);
            }
        }

        return result.toString();
    }
}
