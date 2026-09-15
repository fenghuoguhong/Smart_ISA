package com.huawei.maps.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.huawei.maps.app.utils.LogUtils;

public class BootReceiver extends BroadcastReceiver {

    /**
     * 自定义广播: 通知启动 EHP ISA 前台服务
     */
    private static final String ACTION_EHP_START_NOTIFICATION = "intent.action.navigation.ehpstart.notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // 开机广播: 拉起 ISA 服务
            LogUtils.getInstance().i("mukuitesttest", "我收到了开机广播，我这里启动isa服务");
            startIsaServiceIfNeeded(context);
        } else if (ACTION_EHP_START_NOTIFICATION.equals(action)) {
            // 自定义广播: 服务未运行时才拉起前台服务,避免重复启动
            LogUtils.getInstance().i("mukuitesttest", "收到 ehpstart.notification 广播，检查 ISA 服务是否已启动");
            startIsaServiceIfNeeded(context);
        }
    }

    /**
     * 服务未运行时才拉起前台服务,服务已在运行则跳过
     */
    private void startIsaServiceIfNeeded(Context context) {
        if (HwLocationAndIsaService.isServiceRunning()) {
            LogUtils.getInstance().i("mukuitesttest", "HwLocationAndIsaService 已在运行，无需重复启动");
            return;
        }
        LogUtils.getInstance().i("mukuitesttest", "HwLocationAndIsaService 未运行，现在启动前台服务");
        Intent serviceIntent = new Intent(context, HwLocationAndIsaService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}