package com.huawei.maps.app;

import android.content.Context;
import android.location.Location;

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

    private Location myLocation = new Location("NaviWrapper");
    private boolean needCheckOfflinedataUpdate = true;

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
                LogUtils.getInstance().i(TAG, "onNaviEvent NAVI_DR_POSCHANGED:" + rspDrPoisInfo);
                Location converted = convertDrPoisInfoToLocation(rspDrPoisInfo);
                if (Utils.isInChina()) {
                    LogUtils.getInstance().d("LocationService", "Old location: " + GsonUtil.toJson(converted));
                    LocationUtils.convertLocationCoordTo02(converted);
                }
                // handle new location
                PetalSDKManager.getInstance().getPetalEHPService().setEHPLocation(converted);
                LogUtils.getInstance().i("NaviWrapper", "New location: " + GsonUtil.toJson(converted));
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
     * convert RspDrPoisInfo to android.location.Location
     * speed: SDK uses km/h, Location needs m/s
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
        myLocation.setSpeed(drPoisInfo.getSpeed() / 3.6f);
        myLocation.setTime(drPoisInfo.getTime());
        if (drPoisInfo.isDeltaAltValid()) {
            myLocation.setAltitude(drPoisInfo.getDeltaAlt());
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            myLocation.setSpeedAccuracyMetersPerSecond(drPoisInfo.getSpeedAcc() / 3.6f);
            myLocation.setBearingAccuracyDegrees(drPoisInfo.getCourseAcc());
            myLocation.setVerticalAccuracyMeters(drPoisInfo.getDeltaAltAcc());
        }
        LogUtils.getInstance().i(TAG, "New locacion convertDrPoisInfoToLocation: " + GsonUtil.toJson(drPoisInfo));
        return myLocation;
    }
}
