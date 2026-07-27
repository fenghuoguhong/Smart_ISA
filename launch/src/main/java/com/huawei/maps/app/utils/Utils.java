package com.huawei.maps.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static String TAG = "kika_utils";

    public static String getStackTraceAsString(Exception e) {
        // 使用StringWriter和PrintWriter来捕获堆栈跟踪信息
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public static boolean isAutomotive(Context context) {
        if (context == null) {
            LogUtils.getInstance().i(TAG, "isAutomotive context is null !!!");
            return false;
        }
        PackageManager pm = context.getPackageManager();
        boolean isCar = pm.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE);
        // 检查设备是否支持Android Automotive特性
        LogUtils.getInstance().i(TAG, "isAutomotive isCar = " + isCar);
        return isCar;
    }

    /**
     * 从文件中提取以 "-" 分隔的最后一段，并匹配 20 开头的 8 位版本号
     *
     * @param filePath 文件路径
     * @return 版本号（如 20250714），未找到返回 null
     */
    public static String getDataVersionFromFile(String filePath) {
        LogUtils.getInstance().i(TAG, "getDataVersionFromFile" + filePath);
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            LogUtils.getInstance().i(TAG, "版本号文件不存在，请检查路径是否正确: " + filePath);
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }

                // 按 "-" 分割
                String[] parts = line.split("-");
                if (parts.length == 0) {
                    continue;
                }

                // 取最后一段，去除首尾空白
                String candidate = parts[parts.length - 1].trim();

                // 校验：以 "20" 开头、长度为 8、且全部是数字
                if (candidate.startsWith("20") && candidate.length() == 8) {
                    try {
                        Integer.parseInt(candidate); // 确保纯数字
                        return candidate; // 匹配成功
                    } catch (NumberFormatException ignored) {
                        // 不是纯数字则跳过
                    }
                }
            }
        } catch (IOException e) {
            LogUtils.getInstance().i(TAG, "getDataVersionFromFile exception" + Utils.getStackTraceAsString(e));
        }
        LogUtils.getInstance().i(TAG, "getDataVersionFromFile return null");
        return null; // 未找到
    }

    public static boolean isInChina() {
        return false;
    }
}
