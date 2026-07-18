package com.huawei.maps.app.adapter;

import android.content.Context;

import com.huawei.maps.app.utils.LogUtils;
import com.smart.sdk.base.api.ECarXApiClient;
import com.smart.sdk.ehp.EHPAPI;
import com.smart.sdk.ehp.api.IV2Manager;
import com.smart.sdk.ehp.api.IV2Message;

/**
 * EHP消息发送管理类
 *
 */
public class EHPAbility {
    private static final String TAG = "EHPAbility";

    private IV2Manager mIv2Manager;

    private EHPAPI mEhpapi;

    private final ECarXApiClient.Callback mInitCallbac = new ECarXApiClient.Callback() {
        @Override
        public void onAPIReady(boolean b) {
            if (b) {
                LogUtils.getInstance().i(TAG, "init ehpapi success.");
                mIv2Manager = mEhpapi.getEHPV2Manager();
                return;
            }
            LogUtils.getInstance().i(TAG, "init ehpapi failed.");
        }
    };

    public EHPAbility(Context context) {
        init(context);
    }

    public final void init(Context context) {
        LogUtils.getInstance().i(TAG, "init");
        mEhpapi = EHPAPI.get(context);
        mEhpapi.init(context, mInitCallbac);
    }

    public void updadteHznMessage(IV2Message iv2Message) {
        if (mIv2Manager == null) {
            LogUtils.getInstance().i(TAG, "mIv2Manager try re init.");
            mIv2Manager = mEhpapi.getEHPV2Manager();
            return;
        }
        try {
            mIv2Manager.updateHznMessage(iv2Message);
        } catch (EASFrameworkException e) {
            LogUtils.getInstance().i(TAG, "updadteHznMessage EASFrameworkException");
        }
    }
}
