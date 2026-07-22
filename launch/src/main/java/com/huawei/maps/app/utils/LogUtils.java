package com.huawei.maps.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.huawei.maps.app.adapter.SystemAbility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogUtils {
    private static final String TAG = "KikaISA_LogUtils";
    public static String LOG_DIR = ""; // 日志存储在内部存储的 AppLogs 文件夹
    public static final String LOG_DIR_SDK = "/data/log"; // 日志存储在内部存储华为的日志
    private static final String LOG_FILE_FORMAT = "yyyy-MM-dd_HH-mm-ss";
    private static final String LOG_FILE_EXT = ".log";
    private static final String COMPRESSED_FILE_EXT = ".zip";
    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat(LOG_FILE_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final long MAX_LOG_SIZE = 200 * 1024 * 1024; // 200MB
    private long logIntervalTime = 3600 * 2;
    private static volatile LogUtils instance;
    private File currentLogFile;
    private ScheduledExecutorService scheduler;

    private boolean supportWriteLogFile = false;

    private boolean supportConsoleOutput = false;

    private KikaWriteLogToFile mKikaWriteLogToFile = KikaWriteLogToFile.getInstance();

    public String isaOfflinedtaPath = "";

    private LogUtils() {
        // Private constructor to prevent instantiation
    }

    public static LogUtils getInstance() {
        if (instance == null) {
            synchronized (LogUtils.class) {
                if (instance == null) {
                    instance = new LogUtils();
                }
            }
        }
        return instance;
    }

    public void initWriteLogFile(Context context) {
        supportWriteLogFile = true;
        supportConsoleOutput = true;
        String packageName = context.getPackageName();
        LOG_DIR = LOG_DIR_SDK + "/kikalogs";
        SystemAbility mSystemAbility = new SystemAbility(context);
        isaOfflinedtaPath = mSystemAbility.getOfflineDataPath();
        File logDir = new File(LOG_DIR);
        Log.i(TAG, "Log directory path: " + logDir.getAbsolutePath());
        // Create log directory if not exists
        if (!logDir.exists() && !logDir.mkdirs()) {
            Log.e(TAG, "Failed to create log directory: " + LOG_DIR);
            return;
        }
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::rotateLogs, 0, logIntervalTime, TimeUnit.SECONDS);

        SwitchLogBroadcastReceiver mSwitchLogBroadcastReceiver = new SwitchLogBroadcastReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("open_console_output");
        mIntentFilter.addAction("close_console_output");
        mIntentFilter.addAction("open_write_log_file");
        mIntentFilter.addAction("close_write_log_file");
        context.registerReceiver(mSwitchLogBroadcastReceiver, mIntentFilter);
    }

    public void init(Context context) {
        supportConsoleOutput = true;
    }

    private void rotateLogs() {
        compressAndDeleteOldLogs();
        createNewLogFile();
        mKikaWriteLogToFile.init(currentLogFile);
        checkAndLimitLogSize();
    }

    private void createNewLogFile() {
        String fileName = FILE_NAME_FORMAT.format(new Date()) + LOG_FILE_EXT;
        currentLogFile = new File(LOG_DIR, fileName);
        if (!currentLogFile.exists()) {
            try {
                File parentDir = currentLogFile.getParentFile();
                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    // 创建所有缺失的父目录失败
                    Log.e(TAG, "Failed to create new parentDir...");
                }
                currentLogFile.createNewFile();
                Log.i(TAG, "New log file created: " + currentLogFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Failed to create new log file", e);
            }
        }
    }

    private void compressAndDeleteOldLogs() {
        File logDir = new File(LOG_DIR);
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(LOG_FILE_EXT));

        if (logFiles != null) {
            for (File logFile : logFiles) {
                if (logFile.equals(currentLogFile)) {
                    continue; // Skip the current log file
                }

                // Check if the log file has already been compressed
                File compressedFile = new File(logFile.getAbsolutePath() + COMPRESSED_FILE_EXT);
                if (!compressedFile.exists()) {
                    // Compress the log file
                    compressFile(logFile);

                    // Delete the original log file
                    if (!logFile.delete()) {
                        Log.e(TAG, "Failed to delete log file: " + logFile.getAbsolutePath());
                    }
                } else if (logFile.getName().endsWith(LOG_FILE_EXT) && !logFile.delete()) {
                    Log.e(TAG, "Failed to delete log file when compressed: " + logFile.getAbsolutePath());
                }
            }
        }

        // Delete old compressed logs (older than 7 days)
        deleteOldCompressedLogs();
    }

    private void compressFile(File file) {
        String zipFileName = file.getAbsolutePath() + COMPRESSED_FILE_EXT;
        try (FileInputStream fis = new FileInputStream(file);
             FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[4 * 1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            // Close the zip entry
            zipOut.closeEntry();
        } catch (IOException e) {
            Log.e(TAG, "Failed to compress log file: " + file.getAbsolutePath(), e);
        }
    }

    private void deleteOldCompressedLogs() {
        File logDir = new File(LOG_DIR);
        File[] compressedLogs = logDir.listFiles((dir, name) -> name.endsWith(COMPRESSED_FILE_EXT));

        if (compressedLogs != null) {
            long now = System.currentTimeMillis();
            long oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000);

            for (File compressedLog : compressedLogs) {
                if (compressedLog.lastModified() < oneWeekAgo) {
                    if (!compressedLog.delete()) {
                        Log.e(TAG, "Failed to delete old compressed log: " + compressedLog.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void checkAndLimitLogSize() {
        File logDir = new File(LOG_DIR);
        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(LOG_FILE_EXT) || name.endsWith(COMPRESSED_FILE_EXT));

        if (logFiles != null) {
            // Sort files by last modified time (oldest first)
            Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));

            long totalSize = 0;
            for (File file : logFiles) {
                totalSize += file.length();
            }

            // Delete the oldest files until total size is below MAX_LOG_SIZE
            while (totalSize >= MAX_LOG_SIZE && logFiles.length > 0) {
                File oldestFile = logFiles[0];
                totalSize -= oldestFile.length();
                if (!oldestFile.delete()) {
                    Log.e(TAG, "Failed to delete old log file: " + oldestFile.getAbsolutePath());
                }
                logFiles = Arrays.copyOfRange(logFiles, 1, logFiles.length); // Remove the deleted file from the array
            }
        }
    }

    // 获取带毫秒的时间戳
    private class TimestampCache {
        // 缓存秒级时间字符串（如 "2023-10-05 14:30:45"）
        private volatile String cachedSecondPart = "";
        // 缓存当前秒的时间戳（秒级）
        private volatile long cachedSecond = -1;
        // --- 成员变量用于缓存日期字段 ---
        private volatile int year, month, day, hour, minute, second;
        private StringBuilder cachedSecondPartSB = new StringBuilder(19);
        private StringBuilder mTimestampSB = new StringBuilder(23);

        private final ThreadLocal<Calendar> calendarThreadLocal = ThreadLocal.withInitial(() -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getDefault());
            return cal;
        });

        public String getTimestamp() {
            long currentTimeMillis = System.currentTimeMillis();
            long currentSecond = currentTimeMillis / 1000;
            int millis = (int) (currentTimeMillis % 1000);

            if (currentSecond != cachedSecond) {
                updateCache(currentTimeMillis); // 更新缓存
            }

            // ---使用成员变量拼接字符串 ---
            mTimestampSB.setLength(0);
            return mTimestampSB
                    .append(cachedSecondPart)
                    .append('.')
                    .append(String.format(Locale.US, "%03d", millis))
                    .toString();
        }

        private void updateCache(long currentTimeMillis) {
            Calendar cal = calendarThreadLocal.get();
            cal.setTimeInMillis(currentTimeMillis);

            // --- 将计算结果赋给成员变量 ---
            this.year = cal.get(Calendar.YEAR);
            this.month = cal.get(Calendar.MONTH) + 1;
            this.day = cal.get(Calendar.DAY_OF_MONTH);
            this.hour = cal.get(Calendar.HOUR_OF_DAY);
            this.minute = cal.get(Calendar.MINUTE);
            this.second = cal.get(Calendar.SECOND);

            // --- 使用成员变量生成字符串 ---
            cachedSecondPartSB.setLength(0);
            cachedSecondPart = cachedSecondPartSB
                    .append(year).append('-')
                    .append(month < 10 ? "0" : "").append(month).append('-')
                    .append(day < 10 ? "0" : "").append(day).append(' ')
                    .append(hour < 10 ? "0" : "").append(hour).append(':')
                    .append(minute < 10 ? "0" : "").append(minute).append(':')
                    .append(second < 10 ? "0" : "").append(second)
                    .toString();

            cachedSecond = currentTimeMillis / 1000;
        }
    }

    // 全局缓存实例
    private final TimestampCache timestampCache = new TimestampCache();

    // 优化后的日志格式化方法
    private StringBuilder formatLogMessage = new StringBuilder();

    private synchronized String formatLogMessage(String level, String tag, String message) {
        String timestamp = timestampCache.getTimestamp();
        formatLogMessage.setLength(0);
        formatLogMessage.append(timestamp).append(" ")
                .append(level).append("/")
                .append(tag).append(": ")
                .append(message);
        return formatLogMessage.toString();
    }

    public synchronized String getFormatTime() {
        return timestampCache.getTimestamp();
    }

    public void i(String tag, String message) {
        if (supportConsoleOutput) {
            Log.i(tag, message);
        }
        if (supportWriteLogFile) {
            mKikaWriteLogToFile.writeLogToFile(formatLogMessage("INFO", tag, message));
        }
    }

    public void d(String tag, String message) {
        if (supportConsoleOutput) {
            Log.d(tag, message);
        }
        if (supportWriteLogFile) {
            mKikaWriteLogToFile.writeLogToFile(formatLogMessage("DEBUG", tag, message));
        }
    }

    public void e(String tag, String message) {
        if (supportConsoleOutput) {
            Log.e(tag, message);
        }
        if (supportWriteLogFile) {
            mKikaWriteLogToFile.writeLogToFile(formatLogMessage("ERROR", tag, message));
        }
    }

    public void w(String tag, String message) {
        if (supportConsoleOutput) {
            Log.w(tag, message);
        }
        if (supportWriteLogFile) {
            mKikaWriteLogToFile.writeLogToFile(formatLogMessage("WARN", tag, message));
        }
    }

    public void v(String tag, String message) {
        if (supportConsoleOutput) {
            Log.v(tag, message);
        }
        if (supportWriteLogFile) {
            mKikaWriteLogToFile.writeLogToFile(formatLogMessage("VERBOSE", tag, message));
        }
    }

    private class SwitchLogBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case "open_console_output":
                    supportConsoleOutput = true;
                    LogUtils.getInstance().i(TAG, "open_console_output");
                    break;
                case "close_console_output":
                    supportConsoleOutput = false;
                    LogUtils.getInstance().i(TAG, "close_console_output");
                    break;
                case "open_write_log_file":
                    supportWriteLogFile = true;
                    LogUtils.getInstance().i(TAG, "open_write_log_file");
                    break;
                case "close_write_log_file":
                    supportWriteLogFile = false;
                    LogUtils.getInstance().i(TAG, "close_write_log_file");
                    break;
            }

        }
    }

    public void stopLogging() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (mKikaWriteLogToFile != null) {
            mKikaWriteLogToFile.shutdown();
        }
    }
}