package com.qingshuige.tangyuan.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.core.graphics.scale

object MediaTools {
    fun compressToSize(context: Context?, bitmap: Bitmap?, megabytes: Float): Bitmap? {
        if (bitmap == null) return null

        // 初始质量设为 100
        var quality = 100
        var minQuality = 0
        var maxQuality = 100
        var byteArray: ByteArray?

        // 目标大小 1MB = 1048576 bytes
        val MAX_SIZE = 1048576 * megabytes

        // 创建副本避免修改原始 Bitmap
        var compressedBitmap: Bitmap

        // 获取原始图片的宽高
        var width = bitmap.width
        var height = bitmap.height

        if (width < height) {
            // 处理竖向图片
            width = bitmap.height
            height = bitmap.width
        }

        // 计算缩放比例
        val scaleWidth = (1920f) / width
        val scaleHeight = (1080f) / height

        // 取较小的缩放比例，确保图片完整显示
        val scale = min(scaleWidth, scaleHeight)

        // 计算缩放后的尺寸
        val scaledWidth = (width * scale).roundToInt()
        val scaledHeight = (height * scale).roundToInt()

        // 创建缩放后的 Bitmap
        // 处理横竖图片
        if (bitmap.width > bitmap.getHeight()) {
            compressedBitmap = bitmap.scale(scaledWidth, scaledHeight)
        } else {
            compressedBitmap = bitmap.scale(scaledHeight, scaledWidth)
        }

        do {
            // 使用 ByteArrayOutputStream 将 Bitmap 转为字节数组
            val baos = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            byteArray = baos.toByteArray()

            // 如果太大，降低质量；如果太小，提高质量
            if (byteArray.size > MAX_SIZE && quality > minQuality) {
                maxQuality = quality
                quality = (quality + minQuality) / 2
            } else if (byteArray.size < MAX_SIZE * 0.8 && quality < maxQuality) { // 留一些余量
                minQuality = quality
                quality = (quality + maxQuality) / 2
            }

            // 回收临时 Bitmap 并创建新压缩版本
            if (!compressedBitmap.isRecycled()) {
                compressedBitmap.recycle()
            }
            compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            // 清理
            baos.reset()
        } while ((byteArray?.size ?: 0) > MAX_SIZE && quality > 0 && maxQuality - minQuality > 1)

        return compressedBitmap
    }
}
