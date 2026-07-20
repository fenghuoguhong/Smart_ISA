package com.huawei.maps.app.adapter;

import android.car.Car;
import android.car.VehicleAreaType;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.property.CarPropertyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import com.huawei.maps.app.utils.LogUtils;

import java.io.File;

/**
 * 系统接口能力
 */
public class SystemAbility {

    public static final String TAG = "SmartHySystemAbilityImpl";

    private static final String OFFLINE_BMANIFEST = "/render/Bmanifest";

    private static final String OFFLINE_MANIFEST = "/render/manifest";

    private static final String PATH_RULE = "_|\\.";

    public static final int DEVICE_XDSN = 554725992;

    private final Context mContext;

    private Car mCar;

    private long mInitTimeMillis;

    private CarPropertyManager mCarPropertyManager;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // CarPropertyManager调用
            if (mCar.getCarManager(Car.PROPERTY_SERVICE) instanceof CarPropertyManager) {
                mCarPropertyManager = (CarPropertyManager) mCar.getCarManager(Car.PROPERTY_SERVICE);
            }
            LogUtils.getInstance().i(TAG, "mCarPropertyManager onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.getInstance().i(TAG, "mCarPropertyManager onServiceDisconnected");
        }
    };

    public SystemAbility(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        LogUtils.getInstance().i(TAG, "init");
        // 创建car对象
        creatCar();
    }

    private void creatCar() {
        try {
            mCar = Car.createCar(mContext.getApplicationContext(), mServiceConnection);
            mCar.connect();
            mInitTimeMillis = System.currentTimeMillis();
        } catch (Exception e) {
            LogUtils.getInstance().i(TAG, "car connected");
        }
    }

    /**
     * 检查重连CarPropertyManager
     *
     * @return CarPropertyManager是否初始化
     */
    private boolean checkCarPropertyManager() {
        if (mCarPropertyManager == null) {
            LogUtils.getInstance().i(TAG, "mCarPropertyManager is null");
            if (System.currentTimeMillis() - mInitTimeMillis > 10 * 1000) {
                // 和上次初始化时间差大于10s，重新初始化
                LogUtils.getInstance().i(TAG, "mCarPropertyManager is null reCreatCar");
                creatCar();
            }
            return false;
        }
        return true;
    }

    public String getManufacturer() {
        String manufacturer = Build.BRAND;
        LogUtils.getInstance().i(TAG, "getManufacturer:end");
        LogUtils.getInstance().d(TAG, "getManufacturer:" + manufacturer);
        return manufacturer;
    }

    public String getInfoModel() {
        String infoModel = "Smart_02206S1";
        LogUtils.getInstance().i(TAG, "getInfoModel:infoModel= " + infoModel);
        return infoModel;
    }

    public String getCountryCode() {
        //  添加桩代码 setprop ro.petal.test.country
        String countryCode = SysPropertiesHelper.getProperty("ro.petal.test.country");
        LogUtils.getInstance().i(TAG, "testCountry:" + countryCode);
        if (!TextUtils.isEmpty(countryCode)) {
            return countryCode;
        }
        countryCode = getCountryCodeFromPath(getOfflineManifestPath());
        LogUtils.getInstance().i(TAG, "getCountryCode:" + countryCode);
        return countryCode;
    }

    private String getCountryCodeFromPath(String path) {
        File file = new File(path);
        if (file.listFiles() == null) {
            return "";
        }
        try {
            for (File listFile : file.listFiles()) {
                String fileName = listFile.getName();
                if (!TextUtils.isEmpty(fileName) && fileName.endsWith(".area")) {
                    String[] parts = fileName.split(PATH_RULE);
                    int n = parts.length;
                    if (n >= 3) {
                        String result = parts[n - 2];
                        LogUtils.getInstance().i(TAG, "getCountryCode:result=" + result);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.getInstance().i(TAG, "getCountryCode:e= " + e.getMessage());
        }
        LogUtils.getInstance().i(TAG, "getCountryCode: empty");
        return "";
    }

    // 压缩包方案走Bmanifest,非压缩包方案是manifest
    private String getOfflineManifestPath() {
        String path = getOfflineDataPath() + OFFLINE_BMANIFEST;
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return path;
        }
        return getOfflineDataPath() + OFFLINE_MANIFEST;
    }

    public String getOfflineDataPath() {
        //  添加桩代码 setprop ro.petal.test.country
        String path = SysPropertiesHelper.getProperty("ro.petal.test.offlinedatapath");
        LogUtils.getInstance().i(TAG, "getOfflineDataPath test:" + path);
        if (!TextUtils.isEmpty(path)) {
            return path;
        }
        path = "/map/offlinemaps";
        LogUtils.getInstance().i(TAG, "getOfflineDataPath:end");
        LogUtils.getInstance().d(TAG, "getOfflineDataPath:" + path);
        return path;
    }

    public String getInfoVin() {
        LogUtils.getInstance().i(TAG, "called method getInfoVin");

        //  添加桩代码 setprop ro.petal.test.vin
        String testvin = SysPropertiesHelper.getProperty("ro.petal.test.vin");
        LogUtils.getInstance().d(TAG, "getInfoVin:test:" + testvin);
        if (!TextUtils.isEmpty(testvin)) {
            return testvin;
        }
        return getAutoVin();
    }

    public String getAutoVin() {
        if (!checkCarPropertyManager()) {
            LogUtils.getInstance().i(TAG, "getAutoVin: " + "mCarPropertyManager is empty");
            return "";
        }
        CarPropertyValue<String> property =
                mCarPropertyManager.getProperty(DEVICE_XDSN, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL);
        String value = property.getValue();
        LogUtils.getInstance().i(TAG, "getAutoVin:property:value:" + value);
        return value;
    }
}
