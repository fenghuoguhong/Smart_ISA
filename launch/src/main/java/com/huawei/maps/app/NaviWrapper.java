package com.huawei.maps.app;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

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

public class NaviWrapper {

    public static final String TAG = "NaviWrapper";

    private NaviAPI mNaviAPI;
    private Context mContext;

    private static final long DR_POIS_HANDLE_INTERVAL = 1000L;

    private Location myLocation = new Location("NaviWrapper");
    private boolean needCheckOfflinedataUpdate = true;
    private long mLastDrPoisHandleTime = 0L;
    private double mLastSentLatitude = Double.NaN;
    private double mLastSentLongitude = Double.NaN;

    public NaviWrapper(Context context) {
        mContext = context;
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
        LogUtils.getInstance().i(TAG, "naviBaseModel.getProtocolID: " + naviBaseModel.getProtocolID());
        if (naviBaseModel == null) {
            LogUtils.getInstance().i(TAG, "naviBaseModel is null!");
            return;
        }

        switch (naviBaseModel.getProtocolID()) {
            case NaviProtocolID.NAVI_NTF_DR_POIS_INFO:
                RspDrPoisInfo rspDrPoisInfo = (RspDrPoisInfo) naviBaseModel;
                boolean coordChanged = (rspDrPoisInfo.getLatitude() != mLastSentLatitude
                        || rspDrPoisInfo.getLongitude() != mLastSentLongitude);
                long now = SystemClock.elapsedRealtime();
                // 经纬度变化:直接发送;经纬度相同:固定 1s 最多发送一次
                if (!coordChanged && now - mLastDrPoisHandleTime < DR_POIS_HANDLE_INTERVAL) {
                    break;
                }
                mLastDrPoisHandleTime = now;
                mLastSentLatitude = rspDrPoisInfo.getLatitude();
                mLastSentLongitude = rspDrPoisInfo.getLongitude();
                Location converted = convertDrPoisInfoToLocation(rspDrPoisInfo);
                if (Utils.isInChina()) {
                    LogUtils.getInstance().d("LocationService", "Old location: " + GsonUtil.toJson(converted));
                    LocationUtils.convertLocationCoordTo02(converted);
                }
                // handle new location
                PetalSDKManager.getInstance().getPetalEHPService().setEHPLocation(converted);
                LogUtils.getInstance().i(TAG, "New location before =" + rspDrPoisInfo);
                if (needCheckOfflinedataUpdate && OfflineDataUtils.getInstance().notTimeException()) {
                    needCheckOfflinedataUpdate = false;
                    new Thread(() -> {
                        try {
                            LogUtils.getInstance().i(TAG, "handleNewLocation start check...");
                            OfflineDataUtils.getInstance().checkUpdate(mContext, converted.getLatitude(), converted.getLongitude());
                        } catch (Exception e) {
                            LogUtils.getInstance().i(TAG, "checkUpdateOfflinedata error...e = " + Utils.getStackTraceAsString(e));
                        }
                    }).start();
                }
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
        if (fields != null && !fields[7].isEmpty() && fields.length > 7) {
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
