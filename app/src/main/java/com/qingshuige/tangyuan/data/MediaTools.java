package com.qingshuige.tangyuan.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import id.zelory.compressor.Compressor;

public class MediaTools {

    private MediaTools() {
    }

    public static Bitmap compressToSize(Context context, Bitmap bitmap, float megabytes) {
        if (bitmap == null)
            return null;

        // 初始质量设为100
        int quality = 100;
        int minQuality = 0;
        int maxQuality = 100;
        byte[] byteArray;

        // 目标大小 1MB = 1048576 bytes
        final float MAX_SIZE = 1048576 * megabytes;

        // 创建副本避免修改原始Bitmap
        Bitmap compressedBitmap = bitmap.copy(bitmap.getConfig(), true);

        do {
            // 使用ByteArrayOutputStream将Bitmap转为字节数组
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byteArray = baos.toByteArray();

            // 如果太大，降低质量；如果太小，提高质量
            if (byteArray.length > MAX_SIZE && quality > minQuality) {
                maxQuality = quality;
                quality = (quality + minQuality) / 2;
            } else if (byteArray.length < MAX_SIZE * 0.8 && quality < maxQuality) { // 留一些余量
                minQuality = quality;
                quality = (quality + maxQuality) / 2;
            }

            // 回收临时Bitmap并创建新压缩版本
            if (!compressedBitmap.isRecycled()) {
                compressedBitmap.recycle();
            }
            compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            // 清理
            baos.reset();

        } while (byteArray.length > MAX_SIZE && quality > 0 && maxQuality - minQuality > 1);

        return compressedBitmap;
    }

}
