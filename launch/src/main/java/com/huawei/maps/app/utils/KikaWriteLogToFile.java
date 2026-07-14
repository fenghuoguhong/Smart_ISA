package com.huawei.maps.app.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KikaWriteLogToFile {
    private static final String TAG = "KikaISA_WriteLogToFile";
    private static final int BATCH_SIZE = 80;       // 每批写入条数
    private static final int FLUSH_INTERVAL_MS = 800;// 刷盘间隔

    // 使用并发队列（无锁设计提升性能）
    private final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();

    // 长期持有的写入流
    private volatile BufferedWriter logWriter;
    private volatile File currentLogFile;

    // 单线程调度池
    private ScheduledExecutorService scheduler;

    private static volatile KikaWriteLogToFile instance;

    private KikaWriteLogToFile() {
        // Private constructor to prevent instantiation
    }

    public static KikaWriteLogToFile getInstance() {
        if (instance == null) {
            synchronized (KikaWriteLogToFile.class) {
                if (instance == null) {
                    instance = new KikaWriteLogToFile();
                }
            }
        }
        return instance;
    }

    // 初始化（需在应用启动时调用）
    public void init(File logFile) {
        currentLogFile = logFile;
        try {
            logWriter = new BufferedWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            Log.e(TAG, "logWriter task error 1", e);
        }

        // 启动定时任务（合并写入+刷盘）
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    flushLogsToDisk();
                } catch (Exception e) {
                    Log.e(TAG, "Scheduled task error", e);
                }
            }, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    // 主线程安全调用（无阻塞）
    public void writeLogToFile(String logMessage) {
        logQueue.offer(logMessage); // 非阻塞入队
    }

    // 定时触发的批量写入
    private void flushLogsToDisk() {
        if (logWriter == null) return;

        try {
            int count = 0;
            while (count < BATCH_SIZE && !logQueue.isEmpty()) {
                String msg = logQueue.poll();
                if (msg != null) {
                    logWriter.write(msg);
                    logWriter.newLine();
                    count++;
                }
            }

            logWriter.flush(); // 控制刷盘频率
        } catch (IOException e) {
            Log.e(TAG, "Batch write failed", e);
            recoverWriter();
        }
    }

    // 流异常恢复
    private void recoverWriter() {
        try {
            if (logWriter != null) logWriter.close();
            logWriter = new BufferedWriter(new FileWriter(currentLogFile, true));
        } catch (IOException ex) {
            Log.e(TAG, "Writer recovery failed", ex);
        }
    }

    // 释放资源（应用退出时调用）
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (logWriter != null) {
                logWriter.close();
            }
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Shutdown error", e);
        }
    }
}
