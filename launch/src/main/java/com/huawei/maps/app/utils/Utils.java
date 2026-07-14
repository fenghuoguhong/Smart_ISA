package com.huawei.maps.app.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

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

    public static String readTextFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            FileInputStream fileInputStream = null;
            BufferedReader reader = null;
            try {
                fileInputStream = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(fileInputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                LogUtils.getInstance().i(TAG, "readTextFile exception" + Utils.getStackTraceAsString(e));
                return null;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        LogUtils.getInstance().i(TAG, "readTextFile close reader exception" + Utils.getStackTraceAsString(e));
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e) {
                        LogUtils.getInstance().i(TAG, "readTextFile close fileInputStream exception" + Utils.getStackTraceAsString(e));
                    }
                }

            }
        }

        return null; // File does not exist
    }

    public static boolean isInChina() {
        return false;
    }
}
