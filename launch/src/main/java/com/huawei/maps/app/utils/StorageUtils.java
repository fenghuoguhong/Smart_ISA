package com.huawei.maps.app.utils;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;

import java.io.File;

public class StorageUtils {
    private static String TAG = "StorageUtils";

    // 获取路径剩余空间（MB, double）
    public static double getFreeSpaceMB(String path) {
        try {
            File targetDir = new File(path);
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs()) {
                    return 0.0; // 目录创建失败
                }
            }

            StatFs stat = new StatFs(path);
            long freeBytes;

            // 使用现代API（API 18+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                freeBytes = stat.getAvailableBytes(); // 直接获取可用字节数
            }
            // 兼容旧API（API 1-17）
            else {
                // 使用未废弃的long类型方法
                long blockSize = getBlockSizeLongCompat(stat);
                long availableBlocks = getAvailableBlocksLongCompat(stat);
                freeBytes = availableBlocks * blockSize;
            }

            // 转换为MB (保留小数)
            return bytesToMB(freeBytes);
        } catch (IllegalArgumentException e) {
            return 0.0; // 路径无效
        }
    }

    // 字节转MB (double)
    private static double bytesToMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    // 兼容旧API的块大小获取
    @SuppressWarnings("deprecation")
    private static long getBlockSizeLongCompat(StatFs stat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return stat.getBlockSizeLong();
        } else {
            return stat.getBlockSize(); // 注意：此方法在旧API中未废弃
        }
    }

    // 兼容旧API的可用块获取
    @SuppressWarnings("deprecation")
    private static long getAvailableBlocksLongCompat(StatFs stat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return stat.getAvailableBlocksLong();
        } else {
            return stat.getAvailableBlocks(); // 注意：此方法在旧API中未废弃
        }
    }

    public static double getDownloadingAvailableSpaceMB(Context context) {
        // 应用专属路径
        if (context == null) {
            LogUtils.getInstance().i(TAG, "getDownloadingAvailableSpaceMB context is null...");
            return 1999.0;
        }
        File appDir = context.getExternalFilesDir(null);
        if (appDir != null) {
            LogUtils.getInstance().i(TAG, "getDownloadingAvailableSpaceMB appDir is null...path = "
                    + context.getExternalFilesDir(null).getAbsolutePath());
            return 2000.0;
        }
        return getFreeSpaceMB(appDir.getAbsolutePath());
    }

    public static double getNaviAvailableSpaceMB() {
        String path = LogUtils.getInstance().isaOfflinedtaPath;
        // 添加空路径保护
        if (path == null || path.trim().isEmpty()) {
            return 0.0;
        }
        return getFreeSpaceMB(path);
    }
}
