package com.qingshuige.tangyuan.data

import android.content.Context
import com.google.gson.JsonParser
import com.qingshuige.tangyuan.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Date
import java.util.regex.Pattern

object DataTools {
    fun decodeJwtTokenUserId(token: String): Int {
        // 分割 JWT，获取 payload（第二部分）
        val parts: Array<String?> =
            token.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        require(parts.size == 3) { "Invalid JWT format" }

        // 解码 Base64 URL 编码的 payload
        val payload = parts[1]
        val decodedPayload = String(Base64.getUrlDecoder().decode(payload))
        val jsonObject = JsonParser.parseString(decodedPayload).getAsJsonObject()
        return jsonObject.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name")
            .getAsInt()
    }

    fun isValidPhoneNumber(phoneNumber: String, isoRegionName: String): Boolean {
        val REGEX: String?
        when (isoRegionName) {
            "CN" -> REGEX = "^1[3-9]\\d{9}$"
            else -> return false
        }

        return Pattern.compile(REGEX).matcher(phoneNumber).matches()
    }

    val currentUtcTime: Date?
        get() {
            // 获取当前UTC时间的Instant对象
            val instant = Instant.now()

            // 将Instant对象转换为ZonedDateTime对象（使用UTC时区）
            val zonedDateTime = instant.atZone(ZoneOffset.UTC)

            // 将ZonedDateTime对象转换为Date对象
            return Date.from(zonedDateTime.toInstant())
        }


    @JvmStatic
    fun getLocalFriendlyDateTime(utcDate: Date, context: Context): String? {
        val utcDateString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(utcDate)
        val utcDateTime = ZonedDateTime.parse(utcDateString)

        val timeFormatter = DateTimeFormatter.ofPattern("HH:MM")
        val noYearFormatter = DateTimeFormatter.ofPattern("M-d HH:mm")
        val yearFormatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm")

        val localdt = utcDateTime.withZoneSameInstant(ZoneId.systemDefault())
        val currentLocalDt = ZonedDateTime.now()
        val duration = Duration.between(localdt, currentLocalDt)

        val minutes = duration.toMinutes()
        //刚刚=[0,1]
        if (minutes <= 1) {
            return context.getString(R.string.just_now)
        }
        //n分钟前=(1,60)
        if (minutes < 60) {
            return minutes.toString() + context.getString(R.string.minutes_before)
        }
        //n小时前=[60,180]
        if (minutes <= 180) {
            return duration.toHours().toString() + context.getString(R.string.hours_before)
        }
        //今天
        if (localdt.toLocalDate().isEqual(currentLocalDt.toLocalDate())) {
            return context.getString(R.string.today) + " " + localdt.format(timeFormatter)
        }
        //昨天
        if (localdt.toLocalDate().isEqual(currentLocalDt.toLocalDate().minusDays(1))) {
            return context.getString(R.string.yesterday) + " " + localdt.format(timeFormatter)
        }
        //前天
        if (localdt.toLocalDate().isEqual(currentLocalDt.toLocalDate().minusDays(2))) {
            return context.getString(R.string.the_day_before_yesterday) + " " + localdt.format(
                timeFormatter
            )
        }
        //日期（不带年）
        if (localdt.getYear() == currentLocalDt.getYear()) {
            return localdt.format(noYearFormatter)
        } else { //日期（带年）
            return localdt.format(yearFormatter)
        }
    }

    @JvmStatic
    fun deleteBlankLines(source: String?): String? {
        if (source == null || source.isEmpty()) {
            return source
        }

        val result = StringBuilder()
        var newlineCount = 0

        for (i in 0..<source.length) {
            val c = source.get(i)
            if (c == '\n') {
                newlineCount++
                if (newlineCount <= 2) {
                    result.append(c)
                }
            } else {
                if (newlineCount > 0) {
                    newlineCount = 0
                }
                result.append(c)
            }
        }

        return result.toString()
    }
}
