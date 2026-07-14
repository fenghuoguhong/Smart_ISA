package com.huawei.maps.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.huawei.maps.app.utils.LogUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 启动服务或者执行其他操作
            LogUtils.getInstance().i("mukuitesttest", "我收到了开机广播，我这里启动isa服务");
            // 启动你的 App
            /*Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);*/
            //context.startForegroundService(new Intent(context, HwLocationAndIsaService.class));
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                LogUtils.getInstance().i("mukuitesttest", "设备已启动，现在启动 HwLocationAndIsaService");

                Intent serviceIntent = new Intent(context, HwLocationAndIsaService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}