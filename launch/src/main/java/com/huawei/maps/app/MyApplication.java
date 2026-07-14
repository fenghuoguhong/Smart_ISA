package com.huawei.maps.app;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.huawei.hms.navi.navibase.model.FurnitureInfo;
import com.huawei.hms.navi.navibase.model.locationstruct.NaviLocation;
import com.huawei.maps.app.activation.api.PetalActivationService;
import com.huawei.maps.app.activation.api.constants.ActivationStatus;
import com.huawei.maps.app.activation.api.enums.ActivationMode;
import com.huawei.maps.app.activation.api.enums.ApplicationType;
import com.huawei.maps.app.activation.api.listener.PetalActivateObserver;
import com.huawei.maps.app.activation.api.model.ActivationInitParam;
import com.huawei.maps.app.common.utils.SharedPreUtil;
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
import com.leapmotor.autosdk.ServiceManager;
import com.leapmotor.autosdk.module.ServiceCallback;
import com.leapmotor.autosdk.module.ServiceName;
import com.leapmotor.autosdk.module.navi.NaviService;
import com.leapmotor.autosdk.module.vehicle.VehicleService;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyApplication extends Application {
    public PetalActivationService mActivationService;
    public static AtomicBoolean isActivationFail = new AtomicBoolean(true);
    private AtomicBoolean isTaskRunning = new AtomicBoolean(false);
    private NaviService mNaviService = (NaviService) ServiceManager.getInstance().getService(ServiceName.NAVI);
    private VehicleService mVehicleService = (VehicleService) ServiceManager.getInstance().getService(ServiceName.VEHICLE_CONFIG);
    private String TAG = "KikaISA_MyApplication";
    private String mCountryCode = "CN";
    private PetalEHPListener isaPetalEHPListener;

    private ScheduledExecutorService checkActiveStatusExecutor;

    public static boolean isEhpSuccess = false;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (mVehicleService != null){
                if(!mVehicleService.isIsaSupport()) {
                    LogUtils.getInstance().i(TAG, "当前车不支持ISA功能, 停止所有ISA相关服务!");
                    stopService(new Intent(this, HwLocationAndIsaService.class));
                    return;
                }
            }
            LogUtils.getInstance().initWriteLogFile(this);
            initLeapMotorService();
            mCountryCode = getCountryCode();
            initPetalSDK();
            activateSdk();
            monitorNetwork();
            initMapService();
            OfflineDataUtils.getInstance().startCheckOfflineData();
            if (mVehicleService != null) {
                mVehicleService.setOnEventListener(new VehicleService.OnEventListener() {
                    @Override
                    public void onDrivingMode(int i) {

                    }

                    @Override
                    public void onDrivingModeAcceleration(int i) {

                    }

                    @Override
                    public void onDrivingModeEnergyRecovery(int i) {

                    }

                    @Override
                    public void onDrivingModeSteering(int i) {

                    }

                    @Override
                    public void onElectricityMileageDashboard(float v) {

                    }

                    @Override
                    public void onFuelMileageDashboard(float v) {

                    }

                    @Override
                    public void onMileageUnitTypeDashboard(int i) {

                    }

                    @Override
                    public void onSOC(float v) {

                    }

                    @Override
                    public void onFuelPercent(float v) {

                    }

                    @Override
                    public void onSpeedDashboard(float v) {

                    }

                    @Override
                    public void onSpeedUnitTypeDashboard(int i) {

                    }

                    @Override
                    public void onSpeed(float v) {

                    }

                    @Override
                    public void onGear(int i) {

                    }

                    @Override
                    public void onUnit(boolean b) {

                    }

                    @Override
                    public void onChargeState(int i) {

                    }

                    @Override
                    public void onAirConditionerOnOffState(boolean b) {

                    }

                    @Override
                    public void onAirConditionerTempFrontLeft(float v) {

                    }

                    @Override
                    public void onAirConditionerTempFrontRight(float v) {

                    }

                    @Override
                    public void onAirConditionerTempRear(float v) {

                    }

                    @Override
                    public void onOutsideTemp(float v) {

                    }

                    @Override
                    public void onSteeringWheelAngle(float v) {

                    }

                    @Override
                    public void onScreenState(boolean b) {
                        LogUtils.getInstance().i(TAG, "I received a screen event b = " + b);
                        if (!b) {
                            LogUtils.getInstance().i(TAG, "Data saving has been completed here...");
                        }
                    }

                    @Override
                    public void onTurnLeftSwitchStatus(int i) {

                    }

                    @Override
                    public void onTurnRightSwitchStatus(int i) {

                    }

                    @Override
                    public void onShowCarModeStatus(int i) {

                    }
                });
            }
        } catch (Exception e) {
            LogUtils.getInstance().i(TAG, "init sdk error!!! e = " + Utils.getStackTraceAsString(e));
        }
        DownloadPrefs.initContext(this);
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
        ActivationInitParam activationInitParam = new ActivationInitParam();
        mActivationService = PetalSDKManager.getInstance().getPetalActivationService();
        if (mActivationService != null) {
            activationInitParam.setActivationMode(ActivationMode.ONLINE_ACTIVATION);
            if (mVehicleService != null) {
                try {
                    String mMapActiveCode = mVehicleService.getMapActiveCode();
                    String mVehicleModel = mVehicleService.getVehicleModel();
                    String mManufacturer = mVehicleService.getManufacturer();

                    activationInitParam.setDeviceId(mMapActiveCode);
                    activationInitParam.setCountryCode(mCountryCode);
                    activationInitParam.setVehicleType(mVehicleModel);
                    activationInitParam.setManufacturer(mManufacturer);
                    LogUtils.getInstance().i(TAG, "mVehicleService is not null.. getMapActiveCode = " + mMapActiveCode +
                            " getVehicleModel = " + mVehicleModel + " getManufacturer = " + mManufacturer + ", mCountryCode = " + mCountryCode);
                } catch (Exception e) {
                    LogUtils.getInstance().e(TAG, "mVehicleService error e = " + Utils.getStackTraceAsString(e));
                }
            } else {
                activationInitParam.setDeviceId("apptest123456789");
                activationInitParam.setCountryCode("CN");
                activationInitParam.setVehicleType("HUAWEI");
                activationInitParam.setManufacturer("HUAWEI");
                LogUtils.getInstance().i(TAG, "mVehicleService is null..");
            }
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

    public void initMapService() {
        //初始化offlineMapService
        //int type = 0;
        if (!getActivateStatus()) {
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
            }

            @Override
            public void onCalculateEhpFailure(int i) {
                LogUtils.getInstance().i(TAG, "onCalculateEhpFailure");
            }

            @Override
            public void onEhpInfoUpdate(String s, String s1) {
                //这里转发ehp信号
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
    }

    public void initLeapMotorService() {
        ServiceManager.getInstance().init();
        if (mNaviService != null) {
            mNaviService.init(getApplicationContext(), new ServiceCallback() {
                @Override
                public void onInitSucceed() {
                    LogUtils.getInstance().i(TAG, "NaviService初始化成功");
                }

                @Override
                public void onInitFailed(int i) {
                    LogUtils.getInstance().i(TAG, "NaviService初始化失败");
                }
            });
        }
        if (mVehicleService != null) {
            mVehicleService.init(getApplicationContext(), new ServiceCallback() {
                @Override
                public void onInitSucceed() {
                    LogUtils.getInstance().i(TAG, "VehicleService初始化成功");
                }

                @Override
                public void onInitFailed(int i) {
                    LogUtils.getInstance().i(TAG, "VehicleService初始化失败");
                }
            });
        }
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
        if (mVehicleService == null) {
            return 0;
        }
        int area = 0;
        //Log.i("kikaisatest", "getSaleArea isVehicleServiceInit: " + isVehicleServiceInit);

        try {
            area = mVehicleService.getSaleArea();
            LogUtils.getInstance().i(TAG, "getSaleArea - " + area);
        } catch (Error | Exception e) {
            LogUtils.getInstance().e(TAG, "getSaleArea err: " + e.getMessage());
        }
        return area;
    }

    public String getCountryCode() {
        String countryCode = "CN";

        try {
            File targetFile = getExternalFilesDir("CountryCode");
            LogUtils.getInstance().i(TAG, "getCountryCode filePath: " + targetFile.getPath());
            File file = new File(targetFile, "county.text");
            boolean isExist = file.exists();

            LogUtils.getInstance().i(TAG, "getCountryCode isExit: " + isExist + " finalPath: " + file.getPath());

            if (isExist) {
                String[] arr = new String[]{"CN", "AU", "DE", "IL", "SA", "NZ", "PH", "TR", "GB", "CL", "DZ", "MA"};
                List<String> list = Arrays.asList(arr);
                String content = Utils.readTextFile(file.getPath());

                if (!TextUtils.isEmpty(content) && list.contains(content)) {
                    countryCode = content;
                }
                LogUtils.getInstance().i(TAG, "getCountryCode readContent " + content + " countryCode: " + countryCode);
                return countryCode;
            }
        } catch (Exception e) {
            LogUtils.getInstance().i(TAG, "getCountryCode err: " + e.getMessage());
        }
        int area = getSaleArea();
        switch (area) {
            case VehicleService.SaleArea.CHINA:
            case VehicleService.SaleArea.HONG_KONG:
                countryCode = "CN";
                break;
            case VehicleService.SaleArea.AUSTRALIA:
                countryCode = "AU";
                break;
            case VehicleService.SaleArea.EU:
                // 欧盟
                countryCode = "DE";
                break;
            case VehicleService.SaleArea.ISRAEL:
                countryCode = "IL";
                break;
            case VehicleService.SaleArea.MIDDLE_EAST:
                // 中东
                countryCode = "SA";
                break;
            case VehicleService.SaleArea.NEW_ZEALAND:
                countryCode = "NZ";
                break;
            case VehicleService.SaleArea.SOUTHEAST_ASIA:
                // 东南亚
                countryCode = "PH";
                break;
            case VehicleService.SaleArea.TURKEY:
                countryCode = "TR";
                break;
            case VehicleService.SaleArea.UK:
                countryCode = "GB";
                break;
            case VehicleService.SaleArea.SOUTH_AMERICA:
                countryCode = "CL";
                break;
            case VehicleService.SaleArea.AFRICA:
                countryCode = "DZ";
                break;
            case VehicleService.SaleArea.MOROCCO:
                countryCode = "MA";
                break;
            default:
                LogUtils.getInstance().i(TAG, " getCountryCode default area: " + area);
                countryCode = "DE";
                break;
        }

        LogUtils.getInstance().i(TAG, "getCountryCode area: " + area + " countryCode: " + countryCode);
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

