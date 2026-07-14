package com.huawei.maps.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DownloadPrefs {
    private static final String PREFS_NAME = "download_prefs";
    private static final String KEY_DOWNLOAD_SIZE = "month_downloaded_size";
    private static final String KEY_START_TIME = "start_download_time";
    private static Context mContext;
    private static String TAG = "DownloadPrefs";

    public static void initContext(Context context) {
        mContext = context.getApplicationContext();
    }

    public static void saveDownloadData(float monthDownloadedSize, long startDownloadTime) {
        LogUtils.getInstance().i(TAG, "saveDownloadData monthDownloadedSize: " + monthDownloadedSize + ", startDownloadTime: " + startDownloadTime);
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putFloat(KEY_DOWNLOAD_SIZE, monthDownloadedSize);
            editor.putLong(KEY_START_TIME, startDownloadTime);
            editor.apply();
        } else {
            LogUtils.getInstance().i(TAG, "saveDownloadData mContext is null ！！！");
        }
    }

    public static void saveMonthDownloadedSize(float monthDownloadedSize) {
        LogUtils.getInstance().i(TAG, " saveMonthDownloadedSize monthDownloadedSize: " + monthDownloadedSize);
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putFloat(KEY_DOWNLOAD_SIZE, monthDownloadedSize);
            editor.apply();
        } else {
            LogUtils.getInstance().i(TAG, "saveMonthDownloadedSize mContext is null ！！！");
        }
    }

    public static float getMonthDownloadedSize() {
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            float monthDownloadedSize = prefs.getFloat(KEY_DOWNLOAD_SIZE, 0f);
            LogUtils.getInstance().i(TAG, " getMonthDownloadedSize monthDownloadedSize = " + monthDownloadedSize);
            return monthDownloadedSize;
        } else {
            LogUtils.getInstance().i(TAG, "getMonthDownloadedSize mContext is null ！！！");
            return 0f;
        }
    }

    public static long getStartDownloadTime() {
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long startTime = prefs.getLong(KEY_START_TIME, 0L);
            LogUtils.getInstance().i(TAG, " getStartDownloadTime startTime = " + startTime);
            return startTime;
        } else {
            LogUtils.getInstance().i(TAG, "getStartDownloadTime mContext is null ！！！");
            return 0L;
        }
    }
}
