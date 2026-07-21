package com.huawei.maps.app;

import android.app.Application;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import com.huawei.hms.navi.navibase.model.FurnitureInfo;
import com.huawei.hms.navi.navibase.model.locationstruct.NaviLocation;
import com.huawei.maps.app.activation.api.PetalActivationService;
import com.huawei.maps.app.activation.api.constants.ActivationStatus;
import com.huawei.maps.app.activation.api.enums.ActivationMode;
import com.huawei.maps.app.activation.api.enums.ApplicationType;
import com.huawei.maps.app.activation.api.listener.PetalActivateObserver;
import com.huawei.maps.app.activation.api.model.ActivationInitParam;
import com.huawei.maps.app.adapter.EHPAbilityManager;
import com.huawei.maps.app.adapter.SystemAbility;
import com.huawei.maps.app.common.utils.SharedPreUtil;
import com.huawei.maps.app.common.utils.task.TaskManager;
import com.huawei.maps.app.ehp.api.listener.PetalEHPListener;
import com.huawei.maps.app.ehp.api.model.PetalEHPInitParam;
import com.huawei.maps.app.guide.api.model.PetalLaneInfo;
import com.huawei.maps.app.guide.api.model.PetalNaviInfo;
import com.huawei.maps.app.utils.DownloadPrefs;
import com.huawei.maps.app.utils.LogUtils;
import com.huawei.maps.app.utils.OfflineDataUtils;
import com.huawei.maps.app.utils.Utils;
import com.huawei.maps.auto.petalsdk.InitParam;
import com.huawei.maps.auto.petalsdk.PetalLaunchModel;
import com.huawei.maps.auto.petalsdk.PetalSDKManager;
import com.huawei.maps.auto.sdk.businessbase.offline.bean.OfflineDataInitParam;
import com.huawei.maps.log.AutoLogConstants;
import com.huawei.maps.log.AutoLogInitConf;
import com.huawei.maps.log.AutoLogModuleConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyApplication extends Application {
    public PetalActivationService mActivationService;
    public static AtomicBoolean isActivationFail = new AtomicBoolean(true);
    private AtomicBoolean isTaskRunning = new AtomicBoolean(false);
    private String TAG = "KikaISA_MyApplication";
    private String mCountryCode = "CN";
    private PetalEHPListener isaPetalEHPListener;

    private ScheduledExecutorService checkActiveStatusExecutor;

    public static boolean isEhpSuccess = false;
    private SystemAbility mSystemAbility;

    private NaviWrapper mNaviWrapper;

    // 激活 初始化重试次数上限
    private static final int EHP_RETRY_MAX_TIMES = 3;

    // 激活 初始化首次执行延迟（毫秒）
    private static final long EHP_INIT_DELAY_MS = 10_000L;

    // 激活 初始化重试间隔（毫秒）
    private static final long EHP_RETRY_INTERVAL_MS = 30_000L;

    // 激活 初始化当前重试次数
    private int mEHPRetryCount = 0;
    private boolean sdkIsInit = false;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            LogUtils.getInstance().initWriteLogFile(this);
            mCountryCode = getCountryCode();
            initPetalSDK();
            activateSdk();
            monitorNetwork();
            initMapService();
            OfflineDataUtils.getInstance().startCheckOfflineData();
        } catch (Exception e) {
            LogUtils.getInstance().i(TAG, "init sdk error!!! e = " + Utils.getStackTraceAsString(e));
        }
        DownloadPrefs.initContext(this);
        mNaviWrapper = new NaviWrapper(this);
        mNaviWrapper.initNaviAPI();
    }

    private PetalActivateObserver activateObserver = new PetalActivateObserver() {
        @Override
        public void success() {
            isActivationFail.set(false);
            stopCheckActiveStatus();
            LogUtils.getInstance().i(TAG, "激活成功，停止继续尝试激活");
            initMapService();
        }

        @Override
        public void fail(String s) {
            isActivationFail.set(true);
            startCheckActiveStatus();
            LogUtils.getInstance().i(TAG, "激活失败,请重启进程 错误码 = " + s);
        }
    };

    private void activateSdk() {
        mSystemAbility = new SystemAbility(getApplicationContext());
        TaskManager.postDelayed(TaskManager.createTaskRunnable(TAG, "activateSdk", () -> {
            tryActivateSdk();
            initMapService();
        }), EHP_INIT_DELAY_MS);
    }

    private void tryActivateSdk() {
        ActivationInitParam activationInitParam = new ActivationInitParam();
        mActivationService = PetalSDKManager.getInstance().getPetalActivationService();
        if (mActivationService != null) {
            activationInitParam.setActivationMode(ActivationMode.ONLINE_ACTIVATION);
            activationInitParam.setDeviceId(mSystemAbility.getInfoVin());
            activationInitParam.setCountryCode(mSystemAbility.getCountryCode());
            activationInitParam.setVehicleType(mSystemAbility.getInfoModel());
            activationInitParam.setManufacturer(mSystemAbility.getManufacturer());
            LogUtils.getInstance().i(TAG, "mVehicleService is null.. + InfoVin is null:" +
                    mSystemAbility.getInfoVin() + "  getCountryCode:" + mSystemAbility.getCountryCode() +
                    "  getInfoModel:" + mSystemAbility.getInfoModel() + "  getManufacturer:" + mSystemAbility.getManufacturer());
            try {
                String activePath = getExternalFilesDir(null).getCanonicalPath();
                LogUtils.getInstance().i(TAG, "activePath = " + activePath);
                activationInitParam.setActivationFilePath(activePath);
            } catch (IOException e) {
                LogUtils.getInstance().i(TAG, "active exception e = " + Utils.getStackTraceAsString(e));
            }
            activationInitParam.setApplicationType(ApplicationType.EHP_ACTIVATION);
            int result = mActivationService.init(activationInitParam);
            if (getActivateStatus()) {
                isActivationFail.set(false);
                stopCheckActiveStatus();
            } else {
                try {
                    mActivationService.removeActivateObserver();
                } catch (Exception e) {
                    LogUtils.getInstance().i(TAG, "getActivateStatus exception e = " + Utils.getStackTraceAsString(e));
                }
                mActivationService.setActivateObserver(activateObserver);
                LogUtils.getInstance().i(TAG, "result = " + result);
                LogUtils.getInstance().i(TAG, "mActivationService.activate() = " + mActivationService.activate());
            }
            LogUtils.getInstance().i(TAG, "getActivateStatus() = " + getActivateStatus());

            LogUtils.getInstance().i(TAG, "isaversion = " + BuildConfig.VERSION_NAME + " 20260630_A12_3.5");
        }
    }

    ;

    public synchronized void initMapService() {
        //初始化offlineMapService
        //int type = 0;
        if (!getActivateStatus() || sdkIsInit) {
            LogUtils.getInstance().i(TAG, "SDK is not activated successfully, cannot initMapService");
            return;
        }
        OfflineDataInitParam mInitParam = new OfflineDataInitParam();
        //mInitParam.setActivateDataType(type == 1 ? ActivateDataType.ACTIVATE_DATA_EHP : ActivateDataType.ACTIVATE_DATA_MAP);
        //Locale locale = new Locale("", mCountryCode);
        mInitParam.setLanguageCode("en");
        mInitParam.setPoliticalCode("CN");
        PetalSDKManager.getInstance().getOfflineMapService().init(getApplicationContext(), mInitParam);
        SharedPreUtil.putString("filling_offline_maps_data_recovery", "CN_en_true", this);
        PetalSDKManager.getInstance().getOfflineMapService().getMapAreaService().loadFillingData("CN", aBoolean -> {
            LogUtils.getInstance().i(TAG, "mukuitest111");
        });
        initEHP();
        isaPetalEHPListener = new PetalEHPListener() {
            @Override
            public void onCalculateEhpSuccess() {
                LogUtils.getInstance().i(TAG, "onCalculateEhpSuccess");
                isEhpSuccess = true;
                EHPAbilityManager.getInstance().onCalculateEhpSuccess();
            }

            @Override
            public void onCalculateEhpFailure(int i) {
                LogUtils.getInstance().i(TAG, "onCalculateEhpFailure");
                EHPAbilityManager.getInstance().onCalculateEhpFailure(i);
            }

            @Override
            public void onEhpInfoUpdate(String type, String info) {
                //这里转发ehp信号
                Map<String, String> ehpInfoMap = new HashMap<>();
                ehpInfoMap.put(type, info);
                EHPAbilityManager.getInstance().onEhpInfoUpdate(ehpInfoMap);
            }

            @Override
            public void onNaviInfoUpdate(PetalNaviInfo petalNaviInfo) {

            }

            @Override
            public void onFurnitureInfoUpdate(FurnitureInfo[] furnitureInfos) {

            }

            @Override
            public void onLaneInfoShow(PetalLaneInfo petalLaneInfo) {

            }

            @Override
            public void onLaneInfoHide() {

            }

            @Override
            public void onLocationChange(NaviLocation naviLocation) {

            }
        };
        PetalSDKManager.getInstance().getPetalEHPService().addPetalMapEHPListener(isaPetalEHPListener);
        LogUtils.getInstance().i(TAG, "注册了 isaPetalEHPListener");
        PetalSDKManager.getInstance().getPetalEHPService().setCanAutoStartPureEHP(true);
        PetalSDKManager.getInstance().getPetalEHPService().startPureEHP();
        sdkIsInit = true;
    }

    /**
     * 查询激活状态
     *
     * @return true - 已激活 ，  false - 未激活
     */
    private boolean getActivateStatus() {
        if (mActivationService != null) {
            int activateStatus = mActivationService.getActivateStatus();
            return activateStatus == ActivationStatus.ACTIVATE_SUCCESS;
        }
        return false;
    }


    private void initPetalSDK() {
        InitParam initParam = new InitParam();
        initParam.setLaunchModel(PetalLaunchModel.ONLINE);
        if (mCountryCode != null) {
            Locale locale = new Locale("", mCountryCode);
            initParam.setPolitical(mCountryCode);
            initParam.setLanguage(locale.getLanguage());
            initParam.setCountryCode(mCountryCode);
        }
        //正式版本
        initParam.setOfflineDataPath(LogUtils.getInstance().isaOfflinedtaPath);
        if (Utils.isInChina()) {
            initParam.setGroupMask(AutoLogConstants.GroupMask.GROUP_MASK_ALL);
            initParam.setLogLevel(AutoLogConstants.GroupMask.GROUP_MASK_ALL);
        } else {
            initParam.setGroupMask(AutoLogConstants.GroupMask.GROUP_MASK_HMI_AE);
        }
        PetalSDKManager.getInstance().init(this, initParam);
        LogUtils.getInstance().i(TAG, "isa offlinemaps path = " + LogUtils.getInstance().isaOfflinedtaPath);
        //LogConfig logConfig = new LogConfig().setLevel(L.ASSERT).setLogPath(LogUtils.LOG_DIR_SDK);
        //L.getInstance(LConstants.Category.DEFAULT).init(logConfig);
        //initParam.setLogPath(LogUtils.LOG_DIR_SDK);
        //PetalSDKManager.getInstance().init(this, initParam);
        PetalSDKManager.getInstance().setLogPath(LogUtils.getInstance().LOG_DIR_SDK);
        AutoLogModuleConfig autoLogHmiBaseConfig = new AutoLogModuleConfig().setFileNum(40).setFileSize(5 * 1024 * 1024).setOn(true);
        AutoLogInitConf.configModule(AutoLogConstants.ModuleType.TYPE_HMI_BASE, autoLogHmiBaseConfig);

        AutoLogModuleConfig autoLogHmiDebugConfig = new AutoLogModuleConfig().setOn(false);
        AutoLogInitConf.configModule(AutoLogConstants.ModuleType.TYPE_HMI_DEBUG, autoLogHmiDebugConfig);

        AutoLogModuleConfig autoLogMapkitConfig = new AutoLogModuleConfig().setOn(false);
        AutoLogInitConf.configModule(AutoLogConstants.ModuleType.TYPE_MAPKIT, autoLogMapkitConfig);

        AutoLogModuleConfig autoLogLocationConfig = new AutoLogModuleConfig().setOn(false);
        AutoLogInitConf.configModule(AutoLogConstants.ModuleType.TYPE_LOCATION_SDK, autoLogLocationConfig);

        AutoLogModuleConfig autoLogSiteKitConfig = new AutoLogModuleConfig().setOn(false);
        AutoLogInitConf.configModule(AutoLogConstants.ModuleType.TYPE_SITE_SDK, autoLogSiteKitConfig);
    }

    private void initEHP() {
        PetalEHPInitParam config = new PetalEHPInitParam();
        config.setOfflineMode(true);
        if (mCountryCode != null) {
            config.setCountryCode(mCountryCode);
        }
        config.setMinSegmentInterval(50);
        config.setMinStubInterval(50);
        config.setMinProfileLongInterval(50);
        config.setOfflineMode(true);
        PetalSDKManager.getInstance().getPetalEHPService().init(config);
        EHPAbilityManager.getInstance().init(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PetalSDKManager.getInstance().getPetalEHPService().stopPrueEHP();
        unMonitorNetwork();
        //SdkManager.getInstance().unInit();
    }

    /**
     * 获取销售区域
     *
     * @return {@link VehicleService.SaleArea}
     */
    public int getSaleArea() {
        return 0;
    }

    public String getCountryCode() {
        String countryCode = "CN";
        return countryCode;
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
                    LogUtils.getInstance().i(TAG, "the network change to available..");
                    if (isActivationFail.get()) {
                        activateSdk();
                        LogUtils.getInstance().i(TAG, "network is available, try to activate sdk again..");
                    }
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    LogUtils.getInstance().i(TAG, "the network change to unavailable..");
                }
            };
        }

        // 添加防止重复注册的检查
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } catch (Exception e) {
            LogUtils.getInstance().e(TAG, "application 注册网络回调失败 e = " + Utils.getStackTraceAsString(e));
        }
    }

    public void unMonitorNetwork() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    // 锁方法级别，简单粗暴，线程安全
    public synchronized void startCheckActiveStatus() {
        LogUtils.getInstance().i(TAG, "startCheckActiveStatus..");
        if (!isTaskRunning.get() && checkActiveStatusExecutor == null) {
            checkActiveStatusExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "Active-Thread");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            });
            isTaskRunning.set(true);

            checkActiveStatusExecutor.scheduleWithFixedDelay(() -> {
                try {
                    if (isActivationFail.get()) {
                        activateSdk();
                        LogUtils.getInstance().i(TAG, "定时任务: 尝试重新激活 SDK..");
                    } else {
                        // 这里的调用是异步的，不会持有 start 方法的锁，所以不会死锁
                        stopCheckActiveStatus();
                    }
                } catch (Exception e) {
                    LogUtils.getInstance().e(TAG, "定时任务执行异常: " + Utils.getStackTraceAsString(e));
                }
            }, 30, 30, TimeUnit.SECONDS);
        }
    }

    // 同样只锁方法级别
    public synchronized void stopCheckActiveStatus() {
        LogUtils.getInstance().i(TAG, "stopCheckActiveStatus..");
        if (checkActiveStatusExecutor != null) {
            // 不需要内部 synchronized，因为方法本身已经加了锁
            checkActiveStatusExecutor.shutdownNow();
            checkActiveStatusExecutor = null;
            isTaskRunning.set(false);
        }
    }

}

