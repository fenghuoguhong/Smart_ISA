package com.huawei.maps.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.huawei.maps.app.utils.GsonUtil;
import com.huawei.maps.app.utils.LogUtils;
import com.huawei.maps.app.utils.OfflineDataUtils;
import com.huawei.maps.app.utils.Utils;
import com.huawei.maps.auto.petalsdk.PetalSDKManager;
import com.huawei.maps.auto.sdk.businessbase.offline.bean.OfflineConstants;
import com.huawei.maps.auto.sdk.businessbase.offline.bean.OfflineMapsInfo;
import com.huawei.maps.location.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能描述
 *
 * @author pingguo
 * @since 2024-04-17
 */
public class HwLocationAndIsaService extends Service {
    private static final String CHANNEL_ID = "location_service_channel";
    private static final int NOTIFICATION_ID = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final String TAG = "KikaISA_HwLocationAndIsaService";

    private boolean needCheckOfflinedataUpdate = true;

    private List<OfflineMapsInfo> downloadRecords = new ArrayList<>();

    private long lastResumeTime = 0;

    private static int logCounter = 0;

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 添加定位监听移除
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                LogUtils.getInstance().e(TAG, "移除定位监听失败 e = " + Utils.getStackTraceAsString(e));
            }
        }
        unMonitorNetwork();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.getInstance().i(TAG, "我起来了·");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = location -> {
            // 处理新的定位信息
            handleNewLocation(location);
        };

        // 创建通知通道（仅适用于 Android 8.0+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        monitorNetwork();
        LocationUtils.setInChinaNeedConvert(() -> Utils.isInChina());
    }

    private final HwIsaAidlSupport.Stub binder = new HwIsaAidlSupport.Stub() {
        @Override
        public String getIsaDataVersion() throws RemoteException {
            //return "20250106";
            return OfflineDataUtils.getInstance().getIsaDataVersion();
        }

        @Override
        public String getIsaServiceVersion() throws RemoteException {
            return BuildConfig.VERSION_NAME;
        }

        @Override
        public boolean checkEHPOfflineData() throws RemoteException {
            return OfflineDataUtils.getInstance().getCheckOfflineDataState();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification());

        // 请求定位更新
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000, // 更新间隔（毫秒）
                        0, // 最小位移变化（米）
                        locationListener
                );
            }
        }
        return START_STICKY;
    }

    private ConnectivityManager.NetworkCallback networkCallback;
    ConnectivityManager connectivityManager;
    NetworkRequest networkRequest;

    public void monitorNetwork() {
        if (connectivityManager == null) {
            connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        }
        if (networkRequest == null) {
            networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
        }
        if (networkCallback == null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    LogUtils.getInstance().i(TAG, "HwLocationAndIsaService the network change to available..  needCheckOfflinedataUpdate = " + needCheckOfflinedataUpdate);

                    long time = System.currentTimeMillis() - lastResumeTime;
                    if (time < 2 * 60 * 1000) {
                        LogUtils.getInstance().i(TAG, "HwLocationAndIsaService less than 2 minutes since the last resume,  time = " + time);
                        return;
                    }
                    OfflineDataUtils.getInstance().kikaTryResumeDownload(HwLocationAndIsaService.this, downloadRecords);
                    lastResumeTime = System.currentTimeMillis();
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);

                    if (!OfflineDataUtils.getInstance().notTimeException()) {
                        LogUtils.getInstance().i(TAG, "onLost kikaTryResumeDownload timeException");
                        return;
                    }
                    LogUtils.getInstance().i(TAG, "HwLocationAndIsaService the network change to unavailable2..");
                    for (OfflineMapsInfo info : downloadRecords) {
                        if (info != null && info.getStatus() != OfflineConstants.OfflineDataStatus.FINISH || info.getStatus() != OfflineConstants.OfflineDataStatus.PAUSE) {
                            PetalSDKManager.getInstance().getOfflineMapService().getMapAreaService().pauseDownloadAreaData(OfflineDataUtils.getInstance().getDownloadingInfo(info));
                            LogUtils.getInstance().i(TAG, "HwLocationAndIsaService onLost pauseDownloadAreaData info = " + GsonUtil.toJson(info));
                        }
                    }
                }
            };
        }

        // 添加防止重复注册的检查
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } catch (Exception e) {
            LogUtils.getInstance().e(TAG, "注册网络回调失败 e = " + Utils.getStackTraceAsString(e));
        }
    }

    public void unMonitorNetwork() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Running in background")
                .setSmallIcon(R.drawable.hwmap_navi_car_icon)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void handleNewLocation(Location location) {
        if (location == null || TextUtils.isEmpty(GsonUtil.toJson(location))) {
            LogUtils.getInstance().i("LocationService", "get invalid location");
            return;
        }
        logCounter = ++logCounter % 10;
        if (logCounter == 5) {
            //原始gps信号降频，改为10s打印1次日志
            LogUtils.getInstance().i("LocationService", "New location: " + GsonUtil.toJson(location));
        }
    }
}
