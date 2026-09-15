package com.huawei.maps.app;

import android.content.Context;
import android.location.Location;
import android.os.Process;

import com.huawei.maps.app.utils.GsonUtil;
import com.huawei.maps.app.utils.LogUtils;
import com.huawei.maps.app.utils.OfflineDataUtils;
import com.huawei.maps.app.utils.Utils;
import com.huawei.maps.auto.petalsdk.PetalSDKManager;
import com.huawei.maps.location.utils.LocationUtils;
import com.smart.sdk.base.api.ECarXApiClient;
import com.smart.sdk.navi.INaviEventListener;
import com.smart.sdk.navi.NaviAPI;
import com.smart.sdk.navi.model.base.NaviProtocolID;
import com.smart.sdk.navi.model.client.NaviEventConfig;
import com.smart.sdk.navi.model.service.RspDrPoisInfo;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NaviWrapper {

    public static final String TAG = "NaviWrapper";

    private NaviAPI mNaviAPI;
    private Context mContext;

    /**
     * 固定 1s 向 EHP 上报一次位置
     */
    private static final long EHP_LOCATION_SEND_INTERVAL_MS = 1000L;
    private boolean needCheckOfflinedataUpdate = true;

    /**
     * 固定 1s 执行一次 setEHPLocation 的调度线程池,线程优先级最高,避免被系统降级或误杀
     */
    private volatile ScheduledExecutorService mEhpLocationScheduler;

    RspDrPoisInfo rspDrPoisInfo;

    /**
     * 每个线程只需设置一次系统级优先级
     */
    private static final ThreadLocal<AtomicBoolean> sNativePrioritySet =
            new ThreadLocal<AtomicBoolean>() {
                @Override
                protected AtomicBoolean initialValue() {
                    return new AtomicBoolean(false);
                }
            };

    public NaviWrapper(Context context) {
        mContext = context;
        startEhpLocationScheduler();
    }

    /**
     * 启动固定 1s 上报 EHP 位置的调度线程池。
     * 关键点:
     * 1) 单线程池,减少上下文切换,保证上报节奏稳定;
     * 2) 非守护线程 + Java 层 Thread.MAX_PRIORITY,避免应用切后台后线程被回收;
     * 3) 线程内部再调用 Process.setThreadPriority(THREAD_PRIORITY_URGENT_AUDIO),
     * 将系统 nice 值拉到 -19(Android 允许的最高优先级),降低被系统调度器降级/误杀概率。
     */
    private void startEhpLocationScheduler() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                1,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "NaviWrapper-EhpLocation");
                        // Java 层最高优先级
                        thread.setPriority(Thread.MAX_PRIORITY);
                        // 非守护线程,避免 JVM 认为空闲时被回收
                        thread.setDaemon(false);
                        thread.setUncaughtExceptionHandler((t, e) -> LogUtils.getInstance().i(TAG,
                                "EhpLocation thread uncaught e = " + Utils.getStackTraceAsString(e)));
                        return thread;
                    }
                });
        // 取消任务时立即从队列移除,防止残留堆积
        executor.setRemoveOnCancelPolicy(true);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        mEhpLocationScheduler = executor;

        executor.scheduleWithFixedDelay(this::reportEhpLocation,
                0,
                EHP_LOCATION_SEND_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
        LogUtils.getInstance().i(TAG, "EhpLocation scheduler started, interval = "
                + EHP_LOCATION_SEND_INTERVAL_MS + "ms");
    }

    /**
     * 定时任务体:每 1s 读取一次全局最新位置并下发给 EHP
     */
    private void reportEhpLocation() {
        try {
            // 提升当前线程系统级优先级到 Android 允许的最高档 (nice = -19)
            if (sNativePrioritySet.get().compareAndSet(false, true)) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                LogUtils.getInstance().i(TAG, "EhpLocation thread native priority raised to URGENT_AUDIO");
            }

            if (rspDrPoisInfo == null) {
                LogUtils.getInstance().i(TAG, "rspDrPoisInfo is null");
                return;
            }

            Location converted = convertDrPoisInfoToLocation(rspDrPoisInfo);
            if (converted == null) {
                LogUtils.getInstance().i(TAG, "convertDrPoisInfoToLocation failed");
                return;
            }
            if (Utils.isInChina()) {
                LogUtils.getInstance().d("LocationService", "Old location: " + converted);
                LocationUtils.convertLocationCoordTo02(converted);
            }

            PetalSDKManager.getInstance().getPetalEHPService().setEHPLocation(converted);
            LogUtils.getInstance().i(TAG, "New location after =" + converted);
            if (needCheckOfflinedataUpdate && OfflineDataUtils.getInstance().notTimeException()) {
                needCheckOfflinedataUpdate = false;
                final Location locForCheck = converted;
                new Thread(() -> {
                    try {
                        LogUtils.getInstance().i(TAG, "handleNewLocation start check...");
                        OfflineDataUtils.getInstance().checkUpdate(mContext,
                                locForCheck.getLatitude(), locForCheck.getLongitude());
                    } catch (Exception e) {
                        LogUtils.getInstance().i(TAG, "checkUpdateOfflinedata error...e = " + Utils.getStackTraceAsString(e));
                    }
                }).start();
            }
        } catch (Throwable e) {
            // 兜底捕获,防止异常导致调度任务被 ScheduledExecutorService 静默取消
            LogUtils.getInstance().i(TAG, "Scheduled setEHPLocation error...e = "
                    + Utils.getStackTraceAsString(e));
        }
    }

    /**
     * 释放调度线程池,避免服务销毁后线程泄漏
     */
    public void release() {
        ScheduledExecutorService scheduler = mEhpLocationScheduler;
        mEhpLocationScheduler = null;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            LogUtils.getInstance().i(TAG, "EhpLocation scheduler shutdown");
        }
        rspDrPoisInfo = null;
    }

    public void initNaviAPI() {
        new Thread(() -> {
            mNaviAPI = NaviAPI.get(mContext);
            mNaviAPI.init(mContext, mNaviApiClientCallback);
        }).start();
    }

    private final ECarXApiClient.Callback mNaviApiClientCallback = new ECarXApiClient.Callback() {
        @Override
        public void onAPIReady(boolean b) {
            LogUtils.getInstance().i(TAG, "onAPIReady NaviAPI : " + b);
            if (b) {
                NaviEventConfig eventConfig = new NaviEventConfig();
                eventConfig.setHighFrequencyEventConfig(NaviEventConfig.NAVI_GUIDE_EVENT_ON
                        | NaviEventConfig.NAVI_STATUS_ORIGINAL | NaviEventConfig.NAVI_DR_POIS_INFO | NaviEventConfig.NAVI_LANES_EVENT_ON);
                mNaviAPI.setNaviEventListener(eventConfig, mINaviEventListener);
            }
        }
    };

    private final INaviEventListener mINaviEventListener = naviBaseModel -> {
        if (naviBaseModel == null) {
            LogUtils.getInstance().i(TAG, "naviBaseModel is null!");
            return;
        }
        LogUtils.getInstance().i(TAG, "naviBaseModel.getProtocolID: " + naviBaseModel.getProtocolID());

        switch (naviBaseModel.getProtocolID()) {
            case NaviProtocolID.NAVI_NTF_DR_POIS_INFO:
                rspDrPoisInfo = (RspDrPoisInfo) naviBaseModel;
                LogUtils.getInstance().i(TAG, "New location before =" + rspDrPoisInfo);
                break;
        }
    };

    /**
     * 从 GPRMC 语句中提取对地速度(节)
     * gprmc 示例: $GPRMC,024448.29,A,3020.272702,N,12115.645634,E,15.6,350.2876,050926,,,*1E
     * 按逗号分隔后,速度位于第 8 个字段(索引 7)
     */
    private float parseGprmcSpeed(String gprmc) {
        if (gprmc == null || gprmc.isEmpty()) {
            return 0f;
        }
        // 兼容带 "gprmc=" 前缀或前后有其他内容的输入
        int start = gprmc.indexOf("$GPRMC");
        if (start < 0) {
            return 0f;
        }
        String sentence = gprmc.substring(start);
        String[] fields = sentence.split(",");
        if (fields != null && fields.length > 7 && !fields[7].isEmpty()) {
            try {
                float speed = Float.parseFloat(fields[7]);
                LogUtils.getInstance().i(TAG, "parseGprmcSpeed: " + speed);
                return speed;
            } catch (NumberFormatException e) {
                LogUtils.getInstance().i(TAG, "parseGprmcSpeed error...e = " + Utils.getStackTraceAsString(e));
            }
        }
        return 0f;
    }

    /**
     * convert RspDrPoisInfo to android.location.Location
     * speed: SDK 使用 m/s
     */
    private Location convertDrPoisInfoToLocation(RspDrPoisInfo drPoisInfo) {
        if (drPoisInfo == null) {
            LogUtils.getInstance().i(TAG, "drPoisInfo is null!");
            return null;
        }
        // 每次切换使用不同对象
        Location myLocation = new Location("NaviWrapper");
        myLocation.setLatitude(drPoisInfo.getLatitude());
        myLocation.setLongitude(drPoisInfo.getLongitude());
        myLocation.setBearing(drPoisInfo.getCourse());
        myLocation.setAccuracy(drPoisInfo.getPosAcc());
        // GPRMC 速度为节(knots),Location 需要 m/s(1 节 ≈ 0.514444 m/s)
        float gprmcSpeed = parseGprmcSpeed(drPoisInfo.getGprmc());
        float speed = gprmcSpeed > 0 ? gprmcSpeed * 0.514444f : drPoisInfo.getSpeed();
        myLocation.setSpeed(speed);
        myLocation.setTime(drPoisInfo.getTime());
        if (drPoisInfo.isDeltaAltValid()) {
            myLocation.setAltitude(drPoisInfo.getDeltaAlt());
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            myLocation.setSpeedAccuracyMetersPerSecond(drPoisInfo.getSpeedAcc() / 3.6f);
            myLocation.setBearingAccuracyDegrees(drPoisInfo.getCourseAcc());
            myLocation.setVerticalAccuracyMeters(drPoisInfo.getDeltaAltAcc());
        }
        LogUtils.getInstance().i(TAG, "New locacion convertDrPoisInfoToLocation: lat=" + drPoisInfo.getLatitude()
                + ", lon=" + drPoisInfo.getLongitude()
                + ", course=" + drPoisInfo.getCourse()
                + ", posAcc=" + drPoisInfo.getPosAcc()
                + ", speed=" + drPoisInfo.getSpeed()
                + ", gprmcSpeed=" + gprmcSpeed
                + ", speed=" + speed
                + ", time=" + drPoisInfo.getTime()
                + ", deltaAltValid=" + drPoisInfo.isDeltaAltValid()
                + ", deltaAlt=" + drPoisInfo.getDeltaAlt()
                + ", speedAcc=" + drPoisInfo.getSpeedAcc()
                + ", courseAcc=" + drPoisInfo.getCourseAcc()
                + ", deltaAltAcc=" + drPoisInfo.getDeltaAltAcc());
        return myLocation;
    }
}
