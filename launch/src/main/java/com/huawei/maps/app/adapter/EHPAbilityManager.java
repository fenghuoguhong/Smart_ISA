package com.huawei.maps.app.adapter;

import android.content.Context;

import com.huawei.hmsforcar.calyx.log.LogUtil;

import java.util.Map;

/**
 * Ehp数据入口
 *
 * 使用示例：先init（）接口传入context初始化，然后调用传入数据
 * EHPAbilityManager.getInstance().init(context);
 *         ServiceManager.getInstance().getPetalEHPSerivce().addPetalMapEHPListener(new PetalEHPListener() {
 *             @Override
 *             public void onCalculateEhpSuccess() {
 *                 EHPAbilityManager.getInstance().onCalculateEhpSuccess();
 *             }
 *
 *             @Override
 *             public void onCalculateEhpFailure(int var1) {
 *                 EHPAbilityManager.getInstance().onCalculateEhpFailure(var1);
 *             }
 *
 *             @Override
 *             public void onEhpInfoUpdate(String type, String info) {
 *                 Map<String, String> ehpInfoMap = new HashMap<>();
 *                 ehpInfoMap.put(type, info);
 *                 EHPAbilityManager.getInstance().onEhpInfoUpdate(ehpInfoMap);
 *             }
 *
 *             @Override
 *             public void onLocationChange(NaviLocation naviLocation) {
 *
 *             }
 *
 *             @Override
 *             public void onNaviInfoUpdate(PetalNaviInfo petalNaviInfo) {
 *
 *             }
 *
 *             @Override
 *             public void onFurnitureInfoUpdate(FurnitureInfo[] furnitureInfos) {
 *
 *             }
 *
 *             @Override
 *             public void onLaneInfoShow(PetalLaneInfo petalLaneInfo) {
 *
 *             }
 *
 *             @Override
 *             public void onLaneInfoHide() {
 *
 *             }
 *         });
 */
public class EHPAbilityManager {

    private final static String TAG = "EHPAbilityManager";
    private static volatile EHPAbilityManager sInstance;
    private EHPAbilityHelp mEhpAbilityHelp;

    private EHPAbilityManager() {

    }

    public static EHPAbilityManager getInstance() {
        if (null == sInstance) {
            synchronized (EHPAbilityManager.class) {
                if (null == sInstance) {
                    sInstance = new EHPAbilityManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 初始化
     *
     * @param context 上下文环境
     */
    public void init(Context context) {
        if (mEhpAbilityHelp == null) {
            mEhpAbilityHelp = new EHPAbilityHelp();
            mEhpAbilityHelp.initContext(context);
        }
    }

    /**
     * 检查是否初始化完成
     *
     * @return 是否初始化完成
     */
    private boolean checkInit() {
        if (mEhpAbilityHelp != null) {
            return true;
        }
        return false;
    }

    public void onCalculateEhpSuccess() {
        if (!checkInit()) {
            LogUtil.logI(TAG, "onCalculateEhpSuccess ehpAbilityHelp not init");
            return;
        }
        mEhpAbilityHelp.onCalculateEhpSuccess();
    }

    public void onCalculateEhpFailure(int var1) {
        if (!checkInit()) {
            LogUtil.logI(TAG, "onCalculateEhpFailure ehpAbilityHelp not init");
            return;
        }
        mEhpAbilityHelp.onCalculateEhpFailure(var1);
    }

    public void onEhpInfoUpdate(Map<String, String> ehpInfoMap) {
        if (!checkInit()) {
            LogUtil.logI(TAG, "onEhpInfoUpdate ehpAbilityHelp not init");
            return;
        }
        mEhpAbilityHelp.onEhpInfoUpdate(ehpInfoMap);
    }
}
